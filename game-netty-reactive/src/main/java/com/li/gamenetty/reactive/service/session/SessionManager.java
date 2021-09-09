package com.li.gamenetty.reactive.service.session;

import com.li.gamenetty.reactive.handler.ChannelAttributeKeys;
import com.li.gamenetty.reactive.protocol.IMessage;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author li-yuanwen
 * 客户端连接Session管理
 */
@Component
@Slf4j
public class SessionManager {

    /** session id generator **/
    private final AtomicInteger sessionIdGenerator = new AtomicInteger(0);

    /** 未知身份Session **/
    private final ConcurrentHashMap<Integer, Session> anonymous = new ConcurrentHashMap<>();

    /** 已验明身份Session **/
    private final ConcurrentHashMap<Long, Session> identities = new ConcurrentHashMap<>();

    /** 为Channel注册Session **/
    public Session registerSession(Channel channel) {
        int nextId = this.sessionIdGenerator.incrementAndGet();
        Session session = Session.newInstance(nextId, channel);
        this.anonymous.put(nextId, session);

        // channel绑定属性
        channel.attr(ChannelAttributeKeys.SESSION).set(session);

        return session;
    }


    /** 删除为Channel注册的Session **/
    public Session removeSession(Channel channel) {
        Session session = channel.attr(ChannelAttributeKeys.SESSION).get();
        if (session == null) {
            return null;
        }

        if (session.identity()) {
            this.identities.remove(session.getIdentity());
        }

        this.anonymous.remove(session.getSessionId());

        return session;
    }

    /** 是否在线 **/
    public boolean online(Long identity) {
        return this.identities.containsKey(identity);
    }

    /**
     * 绑定身份
     * @param session 连接Session
     * @param identity 身份标识
     * @param inner true 内网服务器
     */
    public void bindIdentity(Session session, long identity, boolean inner) {
        this.anonymous.remove(session.getSessionId());
        if (log.isDebugEnabled()) {
            log.debug("session[{}]绑定某个身份[{}]", session.getSessionId(), identity);
        }

        if (!inner) {
            session.bind(identity);
        }

        this.identities.put(identity, session);
    }

    /** 断开连接 **/
    public void kickOut(Session session) {
        session.kick();
    }

    /** 断开连接 **/
    public void kickOut(long identity) {
        Session session = this.getIdentitySession(identity);
        if (session != null) {
            session.kick();
        }
    }

    /** 获取指定Session **/
    public Session getIdentitySession(long identity) {
        return this.identities.get(identity);
    }

    /** 获取已绑定身份的标识集 **/
    public Collection<Long> getOnlineIdentities() {
        return Collections.unmodifiableCollection(this.identities.keySet());
    }


    /** 写入Channel **/
    public void writeAndFlush(Session session, IMessage message) {
        if (message == null) {
            if (log.isWarnEnabled()) {
                log.warn("向连接[{}]写入null信息,忽略", session.getChannel().remoteAddress());
            }
            return;
        }


        session.writeAndFlush(message);
    }


}
