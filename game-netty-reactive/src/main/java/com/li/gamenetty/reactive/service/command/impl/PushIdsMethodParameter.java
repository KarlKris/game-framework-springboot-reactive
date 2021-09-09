package com.li.gamenetty.reactive.service.command.impl;


import com.li.gamenetty.reactive.service.command.MethodParameter;

import java.lang.reflect.Type;
import java.util.Collection;

/**
 * @author li-yuanwen
 */
public class PushIdsMethodParameter implements MethodParameter {

    public static final String TYPE = "PushIds";

    @Override
    public String type() {
        return TYPE;
    }

    public static final PushIdsMethodParameter PUSH_IDS_METHOD_PARAMETER = new PushIdsMethodParameter();

    public Type getParameterType() {
        return Collection.class;
    }
}
