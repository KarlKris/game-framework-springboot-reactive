package com.li.gamenetty.reactive.service.dispatcher.impl;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.thread.NamedThreadFactory;
import cn.hutool.core.util.ZipUtil;
import com.li.gamecommon.exception.BadRequestException;
import com.li.gamecommon.exception.SerializeFailException;
import com.li.gamecommon.thread.MonitoredThreadPoolExecutor;
import com.li.gamenetty.reactive.handler.ChannelAttributeKeys;
import com.li.gamenetty.reactive.protocol.*;
import com.li.gamenetty.reactive.service.VocationalWorkConfig;
import com.li.gamenetty.reactive.service.command.Command;
import com.li.gamenetty.reactive.service.command.CommandManager;
import com.li.gamenetty.reactive.service.command.MethodCtx;
import com.li.gamenetty.reactive.service.command.MethodInvokeCtx;
import com.li.gamenetty.reactive.service.dispatcher.DispatcherReactiveExecutor;
import com.li.gamenetty.reactive.service.dispatcher.Dispatcher;
import com.li.gamenetty.reactive.service.rpc.RpcService;
import com.li.gamenetty.reactive.service.session.Session;
import com.li.gamenetty.reactive.service.session.SessionManager;
import com.li.gamenetty.reactive.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author li-yuanwen
 * 业务消息执行
 */
@Slf4j
@Component
public class DispatcherImpl implements Dispatcher, DispatcherReactiveExecutor {


    @Autowired
    private VocationalWorkConfig vocationalWorkConfig;
    @Autowired
    private CommandManager commandManager;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private SerializerManager serializerManager;
    @Autowired
    private RpcService rpcService;

    /** reactor 调度器 **/
    private Scheduler[] schedulers;

    @PostConstruct
    private void init() {
        // 保证线程池数量是2的N次幂
        int i = (Runtime.getRuntime().availableProcessors() >> 1) << 1;
        this.schedulers = new Scheduler[i];

        for (int j = 0; j < i; j++) {
            // 单线程池,减少加锁频率
            this.schedulers[j] = Schedulers.fromExecutorService(
                    new MonitoredThreadPoolExecutor(1, 1,
                    0, TimeUnit.SECONDS
                        , new ArrayBlockingQueue<>(vocationalWorkConfig.getMaxQueueLength())
                        , new NamedThreadFactory("业务线程池", false)
                        , new ThreadPoolExecutor.DiscardPolicy())
            );
        }
    }


    @Override
    public void dispatch(IMessage message, Session session) {
        int index = canAndGetSchedulersArrayIndex(session);

        executeRequest(session, ()-> handleMessage(message, session))
                // 异步
                .subscribeOn(this.schedulers[index])
                // 结果处理
                .subscribe(response -> {
                    if (response == null) {
                        return;
                    }
                    Serializer serializer = serializerManager
                            .getSerializer(
                                    session.getChannel()
                                            .attr(ChannelAttributeKeys.LAST_SERIALIZE_TYPE)
                                            .get()
                            );
                    // 响应消息序列化
                    byte[] responseBody = serializer.serialize(response);
                    boolean zip = responseBody.length > vocationalWorkConfig.getBodyZipLength();
                    if (zip) {
                        responseBody = ZipUtil.gzip(responseBody);
                    }
                    response(session, message, zip, serializer.getSerializerType(), responseBody);
                });

    }

    @Override
    public Mono<Response<Object>> executeRequest(Session session, Callable<Mono<Response<Object>>> callable) {
        int index = canAndGetSchedulersArrayIndex(session);
        try {
            return callable.call().subscribeOn(this.schedulers[index]);
        } catch (Exception e) {
            log.error("DispatcherExecutorService executeRequest() 发生未知异常", e);
            return Mono.error(e);
        }
    }


    /** 业务处理 **/
    private Mono<Response<Object>> handleMessage(IMessage message, Session session) {
        // 查询序列化/反序列化方式
        byte serializeType = message.getSerializeType();
        Serializer serializer = serializerManager.getSerializer(serializeType);
        if (serializer == null) {
            if (log.isWarnEnabled()) {
                log.warn("请求消息序列化类型[{}],找不到对应的序列化工具,忽略", serializeType);
            }
            return Mono.empty();
        }

        Command command = message.getCommand();
        if (log.isDebugEnabled()) {
            log.debug("收到消息[{}-{}],协议头[{}]", command.getModule(), command.getInstruction(), message.getProtocolHeaderIdentity());
        }

        // 记录序列化/反序列化方式
        session.getChannel().attr(ChannelAttributeKeys.LAST_SERIALIZE_TYPE).set(serializeType);

        // 方法调用上下文
        MethodInvokeCtx methodInvokeCtx = commandManager.getMethodInvokeCtx(command);
        if (methodInvokeCtx == null) {
            // 尝试RPC
            if (!rpcService.forward(session, message)) {
                return Mono.just(Response.INVALID_OP);
            }
            // RPC发送成功
            return Mono.empty();
        }

        MethodCtx methodCtx = methodInvokeCtx.getMethodCtx();

        byte[] body = message.getBody();
        if (message.zip()) {
            body = ZipUtil.unGzip(body);
        }

        Object[] args = null;
        try {
            Request request = serializer.deserialize(body, Request.class);

            if (message.isInnerMessage()) {
                // 内部消息发送源标识放置在方法体中
                if (methodInvokeCtx.isIdentity() && !request.hasIdentity()) {
                    // 没有标识
                    return Mono.just(Response.NO_IDENTITY);
                }

                Long identity;
                if ((identity = request.getIdentity()) != null && sessionManager.online(identity)) {
                    // 绑定身份
                    sessionManager.bindIdentity(session, request.getIdentity(), true);
                }

                args = CommandUtils.decodeRequest(session, -1, methodCtx.getParams(), request);

            } else {
                // 外部消息发送源标识放置在Session中
                if (methodInvokeCtx.isIdentity() && !session.identity()) {
                    // 没有标识
                    return Mono.just(Response.NO_IDENTITY);
                }

                args = CommandUtils.decodeRequest(session, session.getIdentity(), methodCtx.getParams(), request);
            }

        } catch (SerializeFailException e) {
            log.error("发生序列化/反序列化异常", e);
            return Mono.just(Response.SERIALIZE_FAIL);
        } catch (ConvertException e) {
            log.error("发生类型转换异常", e);
            return Mono.just(Response.CONVERT_FAIL);
        } catch (Exception e) {
            log.error("发生未知异常", e);
            return Mono.just(Response.UNKNOWN);
        }

        return invokeMethod(methodCtx.getMethod(), methodInvokeCtx.getTarget(), args).map(result -> {
            if (result != null) {
                if (result instanceof Response) {
                    return (Response<Object>) result;
                }
                return Response.SUCCESS(result);
            }
            return Response.DEFAULT_SUCCESS;
        });

    }

    private Mono<Object> invokeMethod(Method method, Object target, Object[] args) {
        Object result = null;
        try {
            result = ReflectionUtils.invokeMethod(method, target, args);
        } catch (BadRequestException e) {
            if (log.isDebugEnabled()) {
                log.debug("发生异常请求异常,异常码[{}]", e.getErrorCode(), e);
            }
            result = Response.ERROR(e.getErrorCode());
        } catch (Exception e){
            log.error("发生未知异常", e);
            result = Response.UNKNOWN;
        }

        if (result == null) {
            return Mono.just(Response.DEFAULT_SUCCESS);
        }
        // 异步消息
        if (result instanceof Mono) {
            return (Mono<Object>) result;
        }

        return Mono.just(result);
    }



    /** 响应 **/
    private void response(Session session, IMessage requestMessage, boolean zip, byte serializeType, byte[] responseBody) {
        IMessage message = null;
        if (requestMessage.isInnerMessage()) {
            message = MessageFactory.toInnerMessage(requestMessage.getSn()
                    , ProtocolConstant.transformResponse(requestMessage.getMessageType())
                    , requestMessage.getCommand()
                    , serializeType
                    , zip
                    , responseBody
                    , session.ip());
        }else if (requestMessage.isOuterMessage()) {
            message = MessageFactory.toOuterMessage(requestMessage.getSn()
                    , ProtocolConstant.transformResponse(requestMessage.getMessageType())
                    , requestMessage.getCommand()
                    , serializeType
                    , zip
                    , responseBody);
        }
        sessionManager.writeAndFlush(session, message);
    }


    /** 根据hash找到对应的线程池下标,仿HashMap **/
    private int canAndGetSchedulersArrayIndex(Session session) {
        long id = session.getSessionId();
        if (session.identity()) {
            id = session.getIdentity();
        }

        int hash = hash(id);
        int length = this.schedulers.length;
        return (length - 1) & hash;
    }

    /** 计算hash值 **/
    static int hash(Long key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
}
