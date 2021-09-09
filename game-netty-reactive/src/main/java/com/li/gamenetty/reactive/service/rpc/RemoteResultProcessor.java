package com.li.gamenetty.reactive.service.rpc;


import com.li.gamenetty.reactive.protocol.IMessage;

/**
 * @author li-yuanwen
 * Rpc 结果处理
 */
public interface RemoteResultProcessor<T extends SnCtx> {

    /**
     * 类型
     * @return 类型
     */
    byte getType();

    /**
     * 结果处理
     * @param t 上下文
     * @param msg 消息
     */
    void process(T t, IMessage msg);

}
