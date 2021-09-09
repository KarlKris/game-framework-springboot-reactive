package com.li.gamenetty.reactive.handler.impl;

import com.li.gamecommon.ApplicationContextHolder;
import com.li.gamenetty.reactive.codec.MessageDecoder;
import com.li.gamenetty.reactive.codec.MessageEncoder;
import com.li.gamenetty.reactive.codec.WebSocketDecoder;
import com.li.gamenetty.reactive.codec.WebSocketEncoder;
import com.li.gamenetty.reactive.protocol.ProtocolConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author li-yuanwen
 * 通讯双方协议选择
 */
@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ProtocolSelectorHandler extends ByteToMessageDecoder {

    @Autowired
    private HttpServerCodec httpServerCodec;
    @Autowired
    private HttpObjectAggregator httpObjectAggregator;
    @Autowired
    private ChunkedWriteHandler chunkedWriteHandler;
    @Autowired
    private WebSocketFrameAggregator webSocketFrameAggregator;
    @Autowired
    private WebSocketServerCompressionHandler webSocketServerCompressionHandler;
    @Autowired
    private WebSocketServerProtocolHandler webSocketServerProtocolHandler;
    @Autowired
    private WebSocketEncoder webSocketEncoder;
    @Autowired
    private WebSocketDecoder webSocketDecoder;


    @Autowired
    private MessageEncoder messageEncoder;
    @Autowired
    private HeartBeatHandler heartBeatHandler;


    /** WEBSOCKET 握手数据包头 **/
    public final static short WEBSOCKET_HANDSHAKE_PREFIX = ('G' << 8) + 'E';
    /** 协议头字节数 **/
    public final static short PROTOCOL_BYTES_SIZE = Short.BYTES;

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext
            , ByteBuf byteBuf, List<Object> list) throws Exception {

        // 可读字节数小于协议头字节数,忽略
        if (byteBuf.readableBytes() < PROTOCOL_BYTES_SIZE) {
            if (log.isDebugEnabled()) {
                log.debug("可读字节数小于协议头字节数[{}],断开连接", PROTOCOL_BYTES_SIZE);
            }

            // 释放ByteBuf
            ReferenceCountUtil.release(byteBuf);
            channelHandlerContext.close();
            return;
        }

        // 读取协议头
        short protocolPrefix = ProtocolConstant.getProtocolHeaderIdentity(byteBuf);
        if (protocolPrefix == WEBSOCKET_HANDSHAKE_PREFIX) {
            // 客户端是websocket连接

            String idleStateHandlerName = IdleStateHandler.class.getSimpleName();

            // HttpServerCodec：将请求和应答消息解码为HTTP消息
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , HttpServerCodec.class.getSimpleName(), this.httpServerCodec);

            // HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息
            // netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , HttpObjectAggregator.class.getSimpleName(), this.httpObjectAggregator);

            // 主要用于处理大数据流，
            // 比如一个1G大小的文件如果你直接传输肯定会撑暴jvm内存的,增加之后就不用考虑这个问题了
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , ChunkedWriteHandler.class.getSimpleName(), this.chunkedWriteHandler);

            // 针对websocket帧进行聚合解码
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , WebSocketFrameAggregator.class.getSimpleName(), this.webSocketFrameAggregator);

            // websocket数据压缩
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , WebSocketServerCompressionHandler.class.getSimpleName(), this.webSocketServerCompressionHandler);

            // websocket连接处理
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , WebSocketServerProtocolHandler.class.getSimpleName(), this.webSocketServerProtocolHandler);

            // 编解码器
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , WebSocketEncoder.class.getSimpleName(), this.webSocketEncoder);
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , WebSocketDecoder.class.getSimpleName(), this.webSocketDecoder);

            // 心跳
            channelHandlerContext.pipeline().addBefore(VocationalWorkHandler.class.getSimpleName()
                    , HeartBeatHandler.class.getSimpleName(), this.heartBeatHandler);

            // 移除自身,完成协议选择
            channelHandlerContext.pipeline().remove(this.getClass().getSimpleName());

            return;
        }


        if (protocolPrefix == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY
                || protocolPrefix == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
            // 自定义协议
            String idleStateHandlerName = IdleStateHandler.class.getSimpleName();

            // 编解码器
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , MessageEncoder.class.getSimpleName(), this.messageEncoder);
            channelHandlerContext.pipeline().addBefore(idleStateHandlerName
                    , MessageDecoder.class.getSimpleName()
                    , ApplicationContextHolder.getBean(MessageDecoder.class));

            // 心跳
            channelHandlerContext.pipeline().addBefore(VocationalWorkHandler.class.getSimpleName()
                    , HeartBeatHandler.class.getSimpleName(), this.heartBeatHandler);

            // 移除自身,完成协议选择
            channelHandlerContext.pipeline().remove(this.getClass().getSimpleName());

            return;
        }

        // 不支持的协议,忽略
        if (log.isDebugEnabled()) {
            log.debug("接收到协议头[{}],暂不支持该协议,断开连接", protocolPrefix);
        }

        // 释放ByteBuf
        ReferenceCountUtil.release(byteBuf);
        channelHandlerContext.close();
    }

}
