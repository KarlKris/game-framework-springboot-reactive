package com.li.gamenetty.reactive.service.dispatcher;


import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.service.session.Session;

/**
 * @author li-yuanwen
 * @date 2021/7/31 15:40
 * 消息分发器接口
 **/
public interface Dispatcher {

    /**
     * 消息分发
     *
     * @param message 消息
     * @param session session
     */
    void dispatch(IMessage message, Session session);

}
