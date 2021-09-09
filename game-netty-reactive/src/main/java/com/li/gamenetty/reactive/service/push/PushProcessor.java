package com.li.gamenetty.reactive.service.push;


import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.protocol.PushResponse;
import com.li.gamenetty.reactive.service.command.Command;
import com.li.gamenetty.reactive.service.session.Session;

/**
 * @author li-yuanwen
 * 收到推送逻辑处理
 */
public interface PushProcessor {


    /**
     * 推送消息处理
     * @param message 推送消息
     */
    void processPushMessage(IMessage message);


    /**
     * 推送至外部
     * @param pushResponse 推送内容
     * @param command 命令
     */
    void pushToOuter(PushResponse pushResponse, Command command);

    /**
     * 内网推送
     * @param session 推送目标
     * @param pushResponse 推送内容
     * @param command 推送命令
     */
    void pushToInner(Session session, PushResponse pushResponse, Command command);

}
