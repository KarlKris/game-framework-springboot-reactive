package com.li.gamenetty.reactive.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @author li-yuanwen
 * Netty服务器启动
 */
@Slf4j
@Component
public class NettyServerBootstrap implements ApplicationRunner, ApplicationListener<ContextClosedEvent> {

    /** Netty服务器相关配置 **/
    private NettyServerConfig nettyServerConfig;

    /** NIO 线程组 **/
    private EventLoopGroup boss;
    private EventLoopGroup workers;
    /** 服务端channel **/
    private Channel channel;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.boss = new NioEventLoopGroup(nettyServerConfig.getBossGroupThreadNum());
        this.workers = new NioEventLoopGroup(nettyServerConfig.getNioGroupThreadNum());

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(this.boss, this.workers)
                .channel(NioServerSocketChannel.class)
                // ChannelOption.SO_BACKLOG对应的是tcp/ip协议listen函数中的backlog参数
                // 函数listen(int socketfd,int backlog)用来初始化服务端可连接队列
                // 服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接
                //多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
                .option(ChannelOption.SO_BACKLOG, nettyServerConfig.getBackLog())
                // 这个参数表示允许重复使用本地地址和端口
                // 某个服务器进程占用了TCP的80端口进行监听，此时再次监听该端口就会返回错误
                // 使用该参数就可以解决问题，该参数允许共用该端口，这个在服务器程序中比较常使用
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(this.nettyServerMessageHandler);

        // 同步绑定端口
        ChannelFuture channelFuture = serverBootstrap.bind(nettyServerConfig.getPort()).sync();
        // 绑定服务器Channel
        this.channel = channelFuture.channel();

        if (log.isInfoEnabled()) {
            log.info("NIO Reactive Netty 服务器[{}]正常启动成功", nettyServerConfig.getPort());
        }
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        this.boss.shutdownGracefully();
        this.workers.shutdownGracefully();
        this.channel.close();

        log.warn("NIO Reactive Netty 服务器端口[{}]正常关闭", nettyServerConfig.getPort());
    }
}
