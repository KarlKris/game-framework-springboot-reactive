package com.li.gamenetty.reactive.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author li-yuanwen
 * netty 服务端childhandler
 */
@Slf4j
@Component
public class NettyServerMessageHandler extends ChannelInitializer<SocketChannel> {

    /** ssl协议ClientHello协议头 **/
    static final short SSL_CLIENT_HELLO = 0x1603;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

    }
}
