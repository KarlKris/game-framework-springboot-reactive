package com.li.gamenetty.reactive.server;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author li-yuanwen
 * Netty服务器相关配置
 */
@Getter
@Configuration
public class NettyServerConfig {

    /** 唯一服务器标识(数字) **/
    @Value("${netty.server.id}")
    private short serverId;
    /** Socket绑定端口号 **/
    @Value("${netty.server.port}")
    private int port;
    /** Boss线程池线程数 **/
    @Value("${netty.server.bossGroup.threadNum:1}")
    private int bossGroupThreadNum;
    /** NIO线程池线程数 **/
    @Value("${netty.server.nioGroup.threadNum:16}")
    private int nioGroupThreadNum;
    /** TCP参数SO_BACKLOG **/
    @Value("${netty.server.tcp.backlog:1024}")
    private int backLog;
}
