package com.li.gamenetty.reactive.protocol;

import com.li.gamesocket.service.command.impl.IdentityMethodParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * @author li-yuanwen
 * @date 2021/7/31 12:24
 * 消息请求中消息体的封装
 **/
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    /** 请求业务所需数据 **/
    private Map<String, Object> params;

    /**
     * 请求中是否有身份标识
     * @return true 有
     */
    public boolean hasIdentity() {
        return params.containsKey(IdentityMethodParameter.TYPE);
    }

    /** 获取身份标识 **/
    public Long getIdentity() {
        Object identity;
        return (identity = params.get(IdentityMethodParameter.TYPE)) == null ? null : (Long) identity;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

}
