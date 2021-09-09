package com.li.gamenetty.reactive.handler.impl;

import com.li.gamenetty.reactive.protocol.IMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author li-yuanwen
 * 业务逻辑ChannelHandler
 */
@Slf4j
@ChannelHandler.Sharable
@Component
public class VocationalWorkHandler extends SimpleChannelInboundHandler<IMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMessage msg) throws Exception {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.error("服务器发生IOException,与客户端[{}]断开连接", ctx.channel().id(), cause);
        }else {
            log.error("服务器发生未知异常", cause);
        }
        ctx.close();
    }
}
