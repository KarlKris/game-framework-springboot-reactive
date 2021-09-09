package com.li.gamenetty.reactive.service.rpc;

import com.li.gamecommon.rpc.RemoteServerSeekService;
import com.li.gamenetty.reactive.protocol.Response;
import com.li.gamenetty.reactive.service.rpc.impl.ForwardSnCtx;
import com.li.gamenetty.reactive.service.rpc.impl.RpcSnCtx;
import io.netty.channel.Channel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author li-yuanwen
 * 消息序号管理 rpc
 */
@Component
@ConditionalOnBean(RemoteServerSeekService.class)
public class SnCtxManager {


    /** 消息序号生成器 **/
    private final AtomicLong snGenerator = new AtomicLong(0);

    /** 消息序号回复Session **/
    private final ConcurrentHashMap<Long, SnCtx> snCtxHolder = new ConcurrentHashMap<>(2);

    /** 获取下一个消息序号 **/
    public long nextSn() {
        return this.snGenerator.incrementAndGet();

    }

    public void forward(long msgSn, long innerSn, Channel channel) {
        SnCtx snCtx = new ForwardSnCtx(innerSn, msgSn, channel);
        this.snCtxHolder.put(snCtx.getInnerSn(), snCtx);
    }

    public void send(long innerSn, CompletableFuture<Response> completableFuture) {
        RpcSnCtx snCtx = new RpcSnCtx(innerSn, completableFuture);
        this.snCtxHolder.put(snCtx.getInnerSn(), snCtx);
    }

    public SnCtx remove(long innerSn) {
        return this.snCtxHolder.remove(innerSn);
    }

}
