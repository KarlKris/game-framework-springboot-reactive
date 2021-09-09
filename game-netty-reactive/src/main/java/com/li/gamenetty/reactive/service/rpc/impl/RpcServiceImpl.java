package com.li.gamenetty.reactive.service.rpc.impl;

import com.li.gamecommon.exception.BadRequestException;
import com.li.gamecommon.exception.code.ResultCode;
import com.li.gamecommon.rpc.RemoteServerSeekService;
import com.li.gamecommon.rpc.model.Address;
import com.li.gamenetty.reactive.client.NettyClient;
import com.li.gamenetty.reactive.client.NettyClientFactory;
import com.li.gamenetty.reactive.protocol.*;
import com.li.gamenetty.reactive.service.rpc.RpcService;
import com.li.gamenetty.reactive.service.rpc.SnCtxManager;
import com.li.gamenetty.reactive.service.session.Session;
import com.li.gamenetty.reactive.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author li-yuanwen
 * 远程调用服务
 */
@Service
@Slf4j
public class RpcServiceImpl implements RpcService {

    @Autowired
    private NettyClientFactory clientFactory;
    @Autowired
    private SerializerManager serializerManager;
    @Autowired(required = false)
    private RemoteServerSeekService remoteServerSeekService;
    @Autowired(required = false)
    private SnCtxManager snCtxManager;

    @Override
    public boolean forward(Session session, IMessage message) {
        checkAndThrowRemoteService();

        Address address = this.remoteServerSeekService.
                seekApplicationAddressByModule(message.getCommand().getModule()
                , session.getIdentity());

        if (address == null) {
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug("向[{}]转发消息[{},{}]", address.getIp()
                    , message.getCommand().getModule(), message.getCommand().getInstruction());
        }

        NettyClient client = clientFactory.newInstance(address);

        byte[] body = message.getBody();
        boolean zip = message.zip();
        if (session.identity()) {
            Serializer serializer = serializerManager.getSerializer(message.getSerializeType());
            if (serializer != null) {
               body = MessageFactory.addIdentityInRequestBody(session.getIdentity(), body, zip, serializer);
            }else {
                log.warn("远程调用服务将请求消息序列化,无匹配的序列化器[{}],消息原封不动发送"
                        , message.getSerializeType());
            }
        }

        long nextSn = snCtxManager.nextSn();
        // 构建内部消息进行转发
        InnerMessage innerMessage = MessageFactory.toInnerMessage(nextSn
                , ProtocolConstant.toOriginMessageType(message.getMessageType())
                , message.getCommand()
                , message.getSerializeType()
                , zip
                , body
                , null);

        try {
            client.send(innerMessage
                    , (msg, completableFuture)
                            -> snCtxManager.forward(msg.getSn(), nextSn, session.getChannel()));
            return true;
        } catch (InterruptedException e) {
            log.error("消息转发至[{}]发生未知异常", address, e);
            return false;
        }
    }

    @Override
    public <T> T getSendProxy(Class<T> tClass, long identity) {
        checkAndThrowRemoteService();

        short module = CommandUtils.getModule(tClass);
        Address address = this.remoteServerSeekService.seekApplicationAddressByModule(module
                , identity);
        if (address == null) {
            throw new BadRequestException(ResultCode.INVALID_OP);
        }

        return clientFactory.newInstance(address).getSendProxy(tClass);
    }

    @Override
    public <T> T getSendProxy(Class<T> tClass, String serverId) {
        checkAndThrowRemoteService();

        short module = CommandUtils.getModule(tClass);
        Address address = this.remoteServerSeekService.seekApplicationAddressById(module
                , serverId);
        if (address == null) {
            throw new BadRequestException(ResultCode.INVALID_OP);
        }

        return clientFactory.newInstance(address).getSendProxy(tClass);
    }

    private void checkAndThrowRemoteService() {
        if (this.remoteServerSeekService == null) {
            throw new BadRequestException(ResultCode.CANT_CONNECT_REMOTE);
        }
    }


}
