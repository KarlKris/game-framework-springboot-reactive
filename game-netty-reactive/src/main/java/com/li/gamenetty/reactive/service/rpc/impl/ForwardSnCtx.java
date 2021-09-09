package com.li.gamenetty.reactive.service.rpc.impl;

import com.li.gamenetty.reactive.service.rpc.SnCtx;
import io.netty.channel.Channel;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 转发消息的消息上下文
 */
@Getter
public class ForwardSnCtx extends SnCtx {

    public static final byte TYPE = 0x1;

    /** 请求消息序号 **/
    private long sn;
    /** 源目标 **/
    private Channel channel;

    public ForwardSnCtx(long innerSn, long sn, Channel channel) {
        super(innerSn);
        this.sn = sn;
        this.channel = channel;
    }

    @Override
    public byte getType() {
        return TYPE;
    }
}
