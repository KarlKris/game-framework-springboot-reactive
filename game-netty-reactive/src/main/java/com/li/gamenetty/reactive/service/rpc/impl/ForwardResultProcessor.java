package com.li.gamenetty.reactive.service.rpc.impl;

import com.li.gamenetty.reactive.handler.ChannelAttributeKeys;
import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.protocol.MessageFactory;
import com.li.gamenetty.reactive.protocol.ProtocolConstant;
import com.li.gamenetty.reactive.service.rpc.RemoteResultProcessor;
import com.li.gamenetty.reactive.service.session.Session;
import com.li.gamenetty.reactive.service.session.SessionManager;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author li-yuanwen
 * 转发结果处理
 */
@Slf4j
@Component
public class ForwardResultProcessor implements RemoteResultProcessor<ForwardSnCtx> {

    @Autowired
    private SessionManager sessionManager;

    @Override
    public byte getType() {
        return ForwardSnCtx.TYPE;
    }

    @Override
    public void process(ForwardSnCtx forwardSnCtx, IMessage msg) {
        Channel channel = forwardSnCtx.getChannel();
        Session session = channel.attr(ChannelAttributeKeys.SESSION).get();

        if (log.isDebugEnabled()) {
            log.debug("转发响应消息[{}]至[{}]", msg, session.ip());
        }

        IMessage message = MessageFactory.toOuterMessage(forwardSnCtx.getSn()
                , ProtocolConstant.toOriginMessageType(msg.getMessageType())
                , msg.getCommand()
                , msg.getSerializeType()
                , msg.zip()
                , msg.getBody());

        sessionManager.writeAndFlush(session, message);
    }
}
