package com.li.gamenetty.reactive.service.session;

import com.li.gamecommon.utils.IpUtils;
import com.li.gamenetty.reactive.protocol.IMessage;
import io.netty.channel.Channel;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 连接Session封装
 */
@Getter
public class Session {

    /** session标识 **/
    private int sessionId;
    /** 身份标识 **/
    private long identity;
    /** channel **/
    private Channel channel;

    public boolean identity() {
        return identity > 0;
    }

    public String ip() {
        return IpUtils.getIp(this.channel.remoteAddress());
    }

    /** 绑定标识 **/
    void bind(long identity) {
        this.identity = identity;
    }

    /** 写入消息 **/
    void writeAndFlush(IMessage message) {
        channel.writeAndFlush(message);
    }

    void kick() {
        this.channel.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Session)) {
            return false;
        }

        Session session = (Session) o;

        return sessionId == session.sessionId;
    }

    @Override
    public int hashCode() {
        return sessionId * 31;
    }

    static Session newInstance(int sessionId, Channel channel) {
        Session session = new Session();
        session.sessionId = sessionId;
        session.channel = channel;
        return session;
    }

}
