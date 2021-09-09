package com.li.gamenetty.reactive.service.command;

import lombok.Getter;

/**
 * @author li-yuanwen
 * @date 2021/7/31 14:11
 * 命令调用上下文
 **/
@Getter
public class MethodInvokeCtx {

    /** 目标对象 **/
    private Object target;
    /** 方法上下文 **/
    private MethodCtx methodCtx;
    /** 是否需要身份标识 **/
    private boolean identity;

    MethodInvokeCtx(Object target, MethodCtx methodCtx) {
        this.target = target;
        this.identity = methodCtx.identity();
        this.methodCtx = methodCtx;

    }
}
