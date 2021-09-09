package com.li.gamenetty.reactive.service.push.impl;

import cn.hutool.core.convert.ConvertException;
import cn.hutool.core.util.ZipUtil;
import com.li.gamecommon.exception.SerializeFailException;
import com.li.gamenetty.reactive.handler.ChannelAttributeKeys;
import com.li.gamenetty.reactive.protocol.*;
import com.li.gamenetty.reactive.service.VocationalWorkConfig;
import com.li.gamenetty.reactive.service.command.Command;
import com.li.gamenetty.reactive.service.command.CommandManager;
import com.li.gamenetty.reactive.service.command.MethodCtx;
import com.li.gamenetty.reactive.service.command.MethodInvokeCtx;
import com.li.gamenetty.reactive.service.dispatcher.DispatcherReactiveExecutor;
import com.li.gamenetty.reactive.service.push.PushProcessor;
import com.li.gamenetty.reactive.service.rpc.SnCtxManager;
import com.li.gamenetty.reactive.service.session.Session;
import com.li.gamenetty.reactive.service.session.SessionManager;
import com.li.gamenetty.reactive.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.util.Objects;

/**
 * @author li-yuanwen
 */
@Slf4j
@Component
public class PushProcessorImpl implements PushProcessor {

    @Autowired
    private CommandManager commandManager;
    @Autowired
    private SerializerManager serializerManager;
    @Autowired
    private SessionManager sessionManager;
    @Autowired
    private SnCtxManager snCtxManager;
    @Autowired
    private VocationalWorkConfig vocationalWorkConfig;
    @Autowired
    private DispatcherReactiveExecutor executor;

    @Override
    public void processPushMessage(IMessage message) {
        MethodInvokeCtx methodInvokeCtx = commandManager.getMethodInvokeCtx(message.getCommand());
        if (methodInvokeCtx == null) {
            // 无处理,即仅是中介,直接推送至外网
            Serializer serializer = serializerManager.getSerializer(message.getSerializeType());
            PushResponse pushResponse = serializer.deserialize(message.getBody(), PushResponse.class);

            doPushToOuter(pushResponse, message.getSn(), message.getMessageType(), message.getCommand());
            return;
        }

        // 查询序列化/反序列化方式
        byte serializeType = message.getSerializeType();
        Serializer serializer = serializerManager.getSerializer(serializeType);
        if (serializer == null) {
            if (log.isWarnEnabled()) {
                log.warn("推送消息序列化类型[{}],找不到对应的序列化工具,忽略", serializeType);
            }
            return;
        }

        try {
            // 推送中介逻辑处理
            PushResponse pushResponse = serializer.deserialize(message.getBody(), PushResponse.class);
            MethodCtx methodCtx = methodInvokeCtx.getMethodCtx();

            Object[] args = CommandUtils.decodePushResponse(methodCtx.getParams(), pushResponse);
            ReflectionUtils.invokeMethod(methodCtx.getMethod(), methodInvokeCtx.getTarget(), args);
        } catch (SerializeFailException e) {
            log.error("发生序列化/反序列化异常", e);
        } catch (ConvertException e) {
            log.error("发生类型转换异常", e);
        } catch (Exception e) {
            log.error("发生未知异常", e);
        }
    }


    @Override
    public void pushToOuter(PushResponse pushResponse, Command command) {
        doPushToOuter(pushResponse, snCtxManager.nextSn(), ProtocolConstant.VOCATIONAL_WORK_RES, command);
    }


    @Override
    public void pushToInner(Session session, PushResponse pushResponse, Command command) {
        Serializer defaultSerializer = serializerManager.getDefaultSerializer();
        byte[] body = defaultSerializer.serialize(pushResponse);
        boolean zip = false;
        if (body.length > vocationalWorkConfig.getBodyZipLength()) {
            body = ZipUtil.gzip(body);
            zip = true;
        }

        InnerMessage message = MessageFactory.toInnerMessage(this.snCtxManager.nextSn()
                , ProtocolConstant.VOCATIONAL_WORK_RES
                , command
                , defaultSerializer.getSerializerType()
                , zip
                , body
                , session.ip());

        if (log.isDebugEnabled()) {
            log.debug("推送消息至内网[{},{}-{}]", message.getSn()
                    , message.getCommand().getModule()
                    , message.getCommand().getInstruction());
        }

        sessionManager.writeAndFlush(session, message);
    }

    private void doPushToOuter(PushResponse pushResponse, long sn, byte messageType, Command command) {
        Response response = Response.SUCCESS(pushResponse.getContent());

        byte serializeType = SerializeType.PROTO_STUFF.getType();
        byte[] body = null;
        boolean zip = false;

        Serializer serializer;
        for (long identity : pushResponse.getTargets()) {
            Session session = sessionManager.getIdentitySession(identity);
            if (session == null) {
                continue;
            }

            Byte type = session.getChannel().attr(ChannelAttributeKeys.LAST_SERIALIZE_TYPE).get();
            if (!Objects.equals(type, serializeType)) {
                serializeType = type;
                serializer = serializerManager.getSerializer(type);
                body = serializer.serialize(response);
                if (body.length > vocationalWorkConfig.getBodyZipLength()) {
                    body = ZipUtil.gzip(body);
                    zip = true;
                }
            }

            IMessage message = MessageFactory.toOuterMessage(sn
                    , ProtocolConstant.toOriginMessageType(messageType)
                    , command
                    , serializeType
                    , zip
                    , body);

            if (log.isDebugEnabled()) {
                log.debug("推送消息至外网[{},{}-{}]", message.getSn()
                        , message.getCommand().getModule()
                        , message.getCommand().getInstruction());
            }

            sessionManager.writeAndFlush(session, message);
        }
    }


}
