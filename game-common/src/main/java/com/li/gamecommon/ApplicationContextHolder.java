package com.li.gamecommon;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;

/**
 * @author li-yuanwen
 * ApplicationContext持有对象工具类
 */
@Order(1)
public class ApplicationContextHolder implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    protected static ApplicationContext applicationContext;

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ApplicationContextHolder.applicationContext = applicationContext;
    }



    /** 获取Bean **/
    public static <T> T getBean(Class<T> tClass) {
        return applicationContext.getBean(tClass);
    }

    /** 获取Bean **/
    public static <T> T getBean(String beanName, Class<T> tClass) {
        return applicationContext.getBean(beanName, tClass);
    }

}
