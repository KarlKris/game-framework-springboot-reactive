package com.li.gamenetty.reactive.protocol;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

/**
 * @author li-yuanwen
 * 消息推送中消息体的封装
 */
@Getter
@AllArgsConstructor
public class PushResponse {

    /** 推送目标标识 **/
    private Collection<Long> targets;

    /** 推送内容 **/
    private Map<String, Object> content;

}
