package com.li.gamenetty.reactive.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author li-yuanwen
 * @date 2021/7/31 14:56
 * 业务方法参数注解
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface InBody {

    /** 属性名 **/
    String name();

    /** 是否必须 **/
    boolean required() default true;
}
