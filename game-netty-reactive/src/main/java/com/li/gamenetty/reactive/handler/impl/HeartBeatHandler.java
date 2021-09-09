package com.li.gamenetty.reactive.handler.impl;

import com.li.gamenetty.reactive.handler.ChannelAttributeKeys;
import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.protocol.MessageFactory;
import com.li.gamenetty.reactive.protocol.ProtocolConstant;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author li-yuanwen
 * 心跳处理
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    /** 是否开启心跳 **/
    @Value("${netty.server.heartBeat.enable:true}")
    private boolean heartBeatEnable;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof IMessage) {
            IMessage message = (IMessage) msg;
            // 修改通信协议
            ctx.channel().attr(ChannelAttributeKeys.LAST_PROTOCOL_HEADER_IDENTITY).set(message.getProtocolHeaderIdentity());

            if (message.isHeartBeatRequest()) {
                if (message.isInnerMessage()) {
                    // 发生心跳响应包
                    ctx.channel().writeAndFlush(MessageFactory.HEART_BEAT_RES_INNER_MSG);
                    return;
                }

                if (message.isOuterMessage()) {
                    // 发生心跳响应包
                    ctx.channel().writeAndFlush(MessageFactory.HEART_BEAT_RES_OUTER_MSG);
                    return;
                }

                if (log.isWarnEnabled()) {
                    log.warn("收到协议头[{}],暂不支持进行心跳响应,忽略", message.getProtocolHeaderIdentity());
                }

                return;
            }

            if (message.isHeartBeatResponse()) {
                if (log.isDebugEnabled()) {
                    log.debug("收到心跳响应包,忽略");
                }
                return;
            }

        }
        super.channelRead(ctx, msg);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 开启心跳,则向对方发送心跳检测包
            if (heartBeatEnable) {
                Short protocolHeaderIdentity = ctx.channel().attr(ChannelAttributeKeys.LAST_PROTOCOL_HEADER_IDENTITY).get();
                if (protocolHeaderIdentity == null) {
                    // 未正常通信过,忽略
                    return;
                }
                if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY) {
                    // 发生心跳检测包
                    ctx.channel().writeAndFlush(MessageFactory.HEART_BEAT_REQ_INNER_MSG);
                    return;
                }

                if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
                    // 发生心跳检测包
                    ctx.channel().writeAndFlush(MessageFactory.HEART_BEAT_RES_OUTER_MSG);
                    return;
                }

                if (log.isWarnEnabled()) {
                    log.warn("收到协议头[{}],暂不支持进行心跳检测,断开连接", protocolHeaderIdentity);
                }

                // 关闭连接
                ctx.close();

            }else {
                // 关闭对方连接
                ctx.close();
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
