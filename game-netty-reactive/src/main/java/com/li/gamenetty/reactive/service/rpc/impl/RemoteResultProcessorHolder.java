package com.li.gamenetty.reactive.service.rpc.impl;

import com.li.gamenetty.reactive.service.rpc.RemoteResultProcessor;
import com.li.gamenetty.reactive.service.rpc.SnCtx;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li-yuanwen
 */
@Component
public class RemoteResultProcessorHolder {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<Byte, RemoteResultProcessor> processorHolder = new HashMap<>();

    @PostConstruct
    private void init() {
        for (RemoteResultProcessor processor : applicationContext.getBeansOfType(RemoteResultProcessor.class).values()) {
            if (processorHolder.putIfAbsent(processor.getType(), processor) != null) {
                throw new BeanInitializationException("RemoteResultProcessor存在相同类型:" + processor.getType());
            }
        }
    }

    /** 远程调用结果处理器 **/
    public RemoteResultProcessor getProcessor(SnCtx snCtx) {
        return processorHolder.get(snCtx.getType());
    }

}
