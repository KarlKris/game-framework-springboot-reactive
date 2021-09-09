package com.li.gamenetty.reactive.client;

import cn.hutool.core.util.ZipUtil;
import com.li.gamecommon.ApplicationContextHolder;
import com.li.gamecommon.exception.BadRequestException;
import com.li.gamecommon.exception.SocketException;
import com.li.gamecommon.exception.code.ResultCode;
import com.li.gamenetty.reactive.protocol.*;
import com.li.gamenetty.reactive.service.VocationalWorkConfig;
import com.li.gamenetty.reactive.service.command.MethodCtx;
import com.li.gamenetty.reactive.service.rpc.SnCtxManager;
import com.li.gamenetty.reactive.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author li-yuanwen
 * rpc代理实际执行器
 */
@Slf4j
public class SendProxyInvoker implements InvocationHandler {

    /** 连接对方的Client **/
    private final NettyClient client;
    /** 方法参数上下文 **/
    private final Map<Method, MethodCtx> methodCtxHolder;
    /** 默认序列化/反序列化工具 **/
    private final Serializer serializer = ApplicationContextHolder.getBean(SerializerManager.class).getDefaultSerializer();
    /** 默认压缩body阙值 **/
    private final int bodyZipLength = ApplicationContextHolder.getBean(VocationalWorkConfig.class).getBodyZipLength();
    /** 消息管理器 **/
    private final SnCtxManager snCtxManager = ApplicationContextHolder.getBean(SnCtxManager.class);
    /** 超时时间(秒) **/
    private final int timeoutSecond = ApplicationContextHolder.getBean(VocationalWorkConfig.class).getTimeoutSecond();

    public SendProxyInvoker(NettyClient client, List<MethodCtx> methodCtxes) {
        this.client = client;
        this.methodCtxHolder = new HashMap<>(methodCtxes.size());
        methodCtxes.forEach(k -> this.methodCtxHolder.putIfAbsent(k.getMethod(), k));
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        MethodCtx methodCtx = this.methodCtxHolder.get(method);
        if (methodCtx == null) {
            throw new IllegalArgumentException("远程方法[" + method.getName() + "]没有添加 @SocketCommand 注解");
        }

        Request request = CommandUtils.encodeRpcRequest(methodCtx.getParams(), args);

        byte[] body = serializer.serialize(request);

        boolean zip = false;
        if (body.length > bodyZipLength) {
            body = ZipUtil.gzip(body);
            zip = true;
        }

        boolean responseType = method.getReturnType() == Response.class;

        InnerMessage message = MessageFactory.toInnerMessage(this.snCtxManager.nextSn()
                , ProtocolConstant.VOCATIONAL_WORK_REQ
                , methodCtx.getCommand()
                , serializer.getSerializerType()
                , zip
                , body
                , null);

        try {
            CompletableFuture<Response> future = client.send(message, (msg, completableFuture)
                    -> snCtxManager.send(msg.getSn(), completableFuture));

            Response response = future.get(timeoutSecond, TimeUnit.SECONDS);
            if (response.success()) {
                return responseType ? response : response.getContent();
            } else if (!response.isVocationalException()) {
                throw new SocketException(response.getCode());
            } else {
                throw new BadRequestException(response.getCode());
            }
        } catch (InterruptedException | TimeoutException e) {
            log.error("SendProxyInvoker超时中断", e);
            throw new SocketException(ResultCode.TIME_OUT);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SocketException) {
                throw (SocketException) cause;
            }
            if (cause instanceof BadRequestException) {
                throw (BadRequestException) cause;
            }
            log.error("SendProxyInvoker发生未知ExecutionException异常", e);
            throw new SocketException(ResultCode.UNKNOWN);
        } catch (Exception e) {
            log.error("SendProxyInvoker发生未知异常", e);
            throw new SocketException(ResultCode.UNKNOWN);
        } finally {
            // 移除序号
            snCtxManager.remove(message.getSn());
        }
    }
}
