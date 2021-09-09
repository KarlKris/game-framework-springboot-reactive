package com.li.gamenetty.reactive.service.push;

import com.li.gamenetty.reactive.service.command.MethodCtx;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 推送方法上下文
 */
@Getter
public class PushMethodCtx {

    /**
     * 方法上下文
     **/
    private final MethodCtx methodCtx;

    PushMethodCtx(MethodCtx methodCtx) {
        if (!methodCtx.getCommand().push()) {
            throw new IllegalArgumentException("推送方法["
                    + methodCtx.getMethod().getName()
                    + "]的命令号["
                    + methodCtx.getCommand().getInstruction()
                    + "]>0");
        }
        this.methodCtx = methodCtx;
    }

}
