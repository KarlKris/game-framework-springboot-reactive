package com.li.gamenetty.reactive.handler.config;

import com.li.gamenetty.reactive.codec.MessageDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolConfig;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.TimeUnit;

/**
 * @author li-yuanwen
 * @date 2021/7/29 21:56
 * channelHandler注解方式配置Bean
 **/
@Configuration
public class ChannelHandlerConfiguration {


    /** 服务端心跳检测ChannelHandler **/
    @Bean("serverIdleStateHandler")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IdleStateHandler serverIdleStateHandler() {
        return new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS);
    }

    /** 客户端心跳检测ChannelHandler **/
    @Bean("clientIdleStateHandler")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IdleStateHandler clientIdleStateHandler() {
        return new IdleStateHandler(0, 25, 0, TimeUnit.SECONDS);
    }

    // ------- WebSocket 协议相关ChannelHandler ----------------------------------


    /** HttpObjectAggregator 消息最大长度 **/
    @Value("${netty.server.websocket.http.aggregator.maxContentLength:1048756}")
    private int maxContentLengthInAggregator;

    /** WebSocketFrameAggregator 消息最大长度 **/
    @Value("${netty.server.websocket.frame.aggregator.maxContentLength:1048756}")
    private int maxContentLengthInFrame;

    /** websocket访问路径前缀 **/
    @Value("${netty.server.websocket.contextPath:/}")
    private String contextPath;


    /** HttpServerCodec：将请求和应答消息解码为HTTP消息 **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HttpServerCodec httpServerCodec() {
        return new HttpServerCodec();
    }

    /** HttpObjectAggregator：将HTTP消息的多个部分合成一条完整的HTTP消息 **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public HttpObjectAggregator httpObjectAggregator() {
        return new HttpObjectAggregator(this.maxContentLengthInAggregator);
    }

    /** 主要用于处理大数据流,防止因为大文件撑爆JVM **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ChunkedWriteHandler chunkedWriteHandler() {
        return new ChunkedWriteHandler();
    }

    /** WebSocketFrameAggregator 通过对消息进行分类进行聚合,解码为WebSocket帧 **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebSocketFrameAggregator webSocketFrameAggregator() {
        return new WebSocketFrameAggregator(this.maxContentLengthInFrame);
    }

    /** WebSocket数据压缩 **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebSocketServerCompressionHandler webSocketServerCompressionHandler() {
        return new WebSocketServerCompressionHandler();
    }

    /** 处理websocket连接 **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public WebSocketServerProtocolHandler webSocketServerProtocolHandler() {
        WebSocketServerProtocolConfig config = WebSocketServerProtocolConfig.newBuilder()
                .allowExtensions(true)
                .websocketPath(this.contextPath)
                .build();
        return new WebSocketServerProtocolHandler(config);
    }

    /** 消息最大长度 **/
    @Value("${netty.server.message.maxContentLength:1048756}")
    private int maxMessageLength;

    /** 消息解码器 原型模式创建 **/
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MessageDecoder messageDecoder() {
        return new MessageDecoder(maxMessageLength, 2, 4);
    }
}
