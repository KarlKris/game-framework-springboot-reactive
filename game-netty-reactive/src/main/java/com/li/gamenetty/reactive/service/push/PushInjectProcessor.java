package com.li.gamenetty.reactive.service.push;

import com.li.gamenetty.reactive.anno.InnerPushInject;
import com.li.gamenetty.reactive.anno.OuterPushInject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

/**
 * @author li-yuanwen
 * @date 2021/8/7 09:47
 * 注解@InnerPushInject @OuterPushInject注入
 **/
@Component
@Order
public class PushInjectProcessor extends InstantiationAwareBeanPostProcessorAdapter {

    @Autowired
    private PushManager pushManager;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithFields(bean.getClass(), field -> {
            InnerPushInject innerPushInject = field.getAnnotation(InnerPushInject.class);
            if (innerPushInject != null) {
                Object pushProxy = pushManager.getInnerPushProxy(field.getType());
                field.setAccessible(true);
                field.set(bean, pushProxy);
            }

            OuterPushInject outerPushInject = field.getAnnotation(OuterPushInject.class);
            if (outerPushInject != null) {
                Object pushProxy = pushManager.getOuterPushProxy(field.getType());
                field.setAccessible(true);
                field.set(bean, pushProxy);
            }


        });

        return bean;
    }
}
