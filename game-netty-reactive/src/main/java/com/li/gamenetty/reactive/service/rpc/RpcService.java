package com.li.gamenetty.reactive.service.rpc;


import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.service.session.Session;

/**
 * @author li-yuanwen
 * 远程调用接口
 */
public interface RpcService {


    /**
     * 消息转发
     * @param session session
     * @param message 转发消息
     * @return true 转发成功
     */
    boolean forward(Session session, IMessage message);

    /**
     * 获取远程代理
     * @param tClass 目标对象
     * @param identity 身份标识
     * @param <T> 类型
     * @return /
     */
    <T> T getSendProxy(Class<T> tClass, long identity);

    /**
     * 获取远程代理
     * @param tClass 目标对象
     * @param serverId 服标识
     * @param <T> 类型
     * @return /
     */
    <T> T getSendProxy(Class<T> tClass, String serverId);

}
