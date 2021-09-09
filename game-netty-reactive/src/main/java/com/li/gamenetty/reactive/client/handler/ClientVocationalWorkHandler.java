package com.li.gamenetty.reactive.client.handler;

import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.protocol.MessageFactory;
import com.li.gamenetty.reactive.service.push.PushProcessor;
import com.li.gamenetty.reactive.service.rpc.RemoteResultProcessor;
import com.li.gamenetty.reactive.service.rpc.SnCtx;
import com.li.gamenetty.reactive.service.rpc.SnCtxManager;
import com.li.gamenetty.reactive.service.rpc.impl.RemoteResultProcessorHolder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author li-yuanwen
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class ClientVocationalWorkHandler extends SimpleChannelInboundHandler<IMessage> {

    @Autowired
    private SnCtxManager snCtxManager;
    @Autowired
    private PushProcessor pushProcessor;
    @Autowired
    private RemoteResultProcessorHolder remoteResultProcessorHolder;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IMessage msg) throws Exception {
        // 处理从服务端收到的信息
        if (msg.isRequest()) {
            if (log.isDebugEnabled()) {
                log.debug("客户端ClientVocationalWorkHandler收到请求信息,忽略");
            }

            return;
        }

        // 处理收到的推送消息
        if (msg.getCommand().push()) {
            pushProcessor.processPushMessage(msg);
            return;
        }

        SnCtx snCtx = this.snCtxManager.remove(msg.getSn());
        if (snCtx == null) {
            if (log.isDebugEnabled()) {
                log.debug("客户端ClientVocationalWorkHandler收到过期信息,序号[{}],忽略", msg.getSn());
            }

            return;
        }

        RemoteResultProcessor processor = remoteResultProcessorHolder.getProcessor(snCtx);
        if (processor != null) {
            processor.process(snCtx, msg);
            return;
        }

        if (log.isWarnEnabled()) {
            log.warn("客户端ClientVocationalWorkHandler收到信息[{}],但不处理", snCtx);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            log.error("客户端发生IOException,与服务端断开连接", cause);
            ctx.close();
        } else {
            log.error("客户端发生未知异常", cause);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            // 开启心跳,则向对方发送心跳检测包
            if (event.state() == IdleState.WRITER_IDLE) {
                // 发生心跳检测包
                ctx.channel().writeAndFlush(MessageFactory.HEART_BEAT_REQ_INNER_MSG);
                return;
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
