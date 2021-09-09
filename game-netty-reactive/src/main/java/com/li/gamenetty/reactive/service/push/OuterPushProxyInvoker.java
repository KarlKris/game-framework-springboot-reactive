package com.li.gamenetty.reactive.service.push;

import com.li.gamecommon.ApplicationContextHolder;
import com.li.gamenetty.reactive.protocol.PushResponse;
import com.li.gamenetty.reactive.service.command.MethodCtx;
import com.li.gamenetty.reactive.utils.CommandUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author li-yuanwen
 * 推送外网代理实际执行对象
 */
public class OuterPushProxyInvoker implements InvocationHandler {

    /** 方法参数上下文 **/
    private final Map<Method, PushMethodCtx> methodCtxHolder;
    /** 推送执行器 **/
    private final PushProcessor pushProcessor;

    OuterPushProxyInvoker(List<MethodCtx> methodCtxes) {
        this.methodCtxHolder = new HashMap<>(methodCtxes.size());
        methodCtxes.forEach(k -> this.methodCtxHolder.putIfAbsent(k.getMethod(), new PushMethodCtx(k)));
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
        pushProcessor.pushToOuter(pushResponse, methodCtx.getCommand());

        return null;
    }
}
