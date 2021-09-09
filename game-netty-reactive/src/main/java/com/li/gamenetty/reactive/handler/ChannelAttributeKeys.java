package com.li.gamenetty.reactive.handler;

import com.li.gamenetty.reactive.service.session.Session;
import io.netty.util.AttributeKey;

/**
 * @author li-yuanwen
 * @date 2021/7/31 16:37
 * 自定义的Channel属性
 **/
public interface ChannelAttributeKeys {

    /** Channel绑定属性 最近一次通信使用协议头 **/
    AttributeKey<Short> LAST_PROTOCOL_HEADER_IDENTITY = AttributeKey.newInstance("protocol_header_identity");

    /** Channel绑定属性Session **/
    AttributeKey<Session> SESSION = AttributeKey.newInstance("session");

    /** Channel上次使用的序列化/反序列化类型 **/
    AttributeKey<Byte> LAST_SERIALIZE_TYPE = AttributeKey.newInstance("last_serialize_type");

}
