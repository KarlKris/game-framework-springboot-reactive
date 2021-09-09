package com.li.gamenetty.reactive.service.rpc.impl;

import com.li.gamenetty.reactive.protocol.Response;
import com.li.gamenetty.reactive.service.rpc.SnCtx;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

/**
 * @author li-yuanwen
 * 用于调用远方接口的上下文
 */
@Getter
public class RpcSnCtx extends SnCtx {

    public static final byte TYPE = 0x0;

    /** future **/
    private CompletableFuture<Response> future;

    public RpcSnCtx(long innerSn, CompletableFuture<Response> future) {
        super(innerSn);
        this.future = future;
    }

    @Override
    public byte getType() {
        return TYPE;
    }
}
