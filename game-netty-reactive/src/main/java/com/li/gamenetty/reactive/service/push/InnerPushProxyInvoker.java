package com.li.gamenetty.reactive.service.push;

import com.li.gamecommon.ApplicationContextHolder;
import com.li.gamenetty.reactive.protocol.PushResponse;
import com.li.gamenetty.reactive.service.command.MethodCtx;
import com.li.gamenetty.reactive.service.session.Session;
import com.li.gamenetty.reactive.service.session.SessionManager;
import com.li.gamenetty.reactive.utils.CommandUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author li-yuanwen
 * 内网间推送代理实际执行对象
 */
@Slf4j
public class InnerPushProxyInvoker implements InvocationHandler {


    /** Session管理 **/
    private final SessionManager sessionManager;
    /** 方法参数上下文 **/
    private final Map<Method, PushMethodCtx> methodCtxHolder;
    /** 推送处理器 **/
    private final PushProcessor pushProcessor;

    InnerPushProxyInvoker(List<MethodCtx> methodCtxes) {
        this.methodCtxHolder = new HashMap<>(methodCtxes.size());
        methodCtxes.forEach(k -> this.methodCtxHolder.putIfAbsent(k.getMethod(), new PushMethodCtx(k)));
        this.sessionManager = ApplicationContextHolder.getBean(SessionManager.class);
        this.pushProcessor = ApplicationContextHolder.getBean(PushProcessor.class);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        PushMethodCtx pushMethodCtx = this.methodCtxHolder.get(method);
        if (pushMethodCtx == null) {
            throw new IllegalArgumentException("推送方法[" + method.getName() + "]没有添加 @SocketPush 注解");
        }
        MethodCtx methodCtx = pushMethodCtx.getMethodCtx();

        PushResponse pushResponse = CommandUtils.encodePushResponse(methodCtx.getParams(), args);

        // 构建每个Channel需要发送的目标
        Map<Session, Set<Long>> session2Identities = new HashMap<>(pushResponse.getTargets().size());
        for (long identity : pushResponse.getTargets()) {
            Session session = sessionManager.getIdentitySession(identity);
            if (session == null) {
                continue;
            }
            session2Identities.computeIfAbsent(session, k -> new HashSet<>()).add(identity);
        }

        for (Map.Entry<Session, Set<Long>> entry : session2Identities.entrySet()) {
            PushResponse response = new PushResponse(entry.getValue(), pushResponse.getContent());
            pushProcessor.pushToInner(entry.getKey(), response, methodCtx.getCommand());
        }

        return null;
    }
}
