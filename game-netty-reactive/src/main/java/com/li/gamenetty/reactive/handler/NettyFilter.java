package com.li.gamenetty.reactive.handler;

import io.netty.channel.ChannelHandler;

/**
 * @author li-yuanwen
 * 连接处理过滤器
 */
public interface NettyFilter extends ChannelHandler {

    /**
     * 过滤器名称
     * @return /
     */
    String getName();
}
