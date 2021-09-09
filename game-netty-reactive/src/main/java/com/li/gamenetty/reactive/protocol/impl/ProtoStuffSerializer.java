package com.li.gamenetty.reactive.protocol.impl;

import com.li.gamecommon.exception.SerializeFailException;
import com.li.gamenetty.reactive.protocol.SerializeType;
import com.li.gamenetty.reactive.protocol.Serializer;
import com.li.gamenetty.reactive.protocol.utils.ProtoStuffUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author li-yuanwen
 * @date 2021/7/31 18:35
 * 序列化框架 ProtoStuff
 **/
@Component
@Slf4j
public class ProtoStuffSerializer implements Serializer {

    @Override
    public byte getSerializerType() {
        return SerializeType.PROTO_STUFF.getType();
    }

    @Override
    public <T> byte[] serialize(T obj) {
        try {
            return ProtoStuffUtils.serialize(obj);
        } catch (Exception e) {
            log.error("序列化对象[{}]出现未知异常", obj.getClass().getSimpleName(), e);
            throw new SerializeFailException("序列化对象[" + obj.getClass().getSimpleName() + "]出现未知异常", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        try {
            return ProtoStuffUtils.deserialize(data, clazz);
        } catch (Exception e) {
            log.error("反序列化对象[{}]出现未知异常", clazz.getSimpleName(), e);
            throw new SerializeFailException("反序列化对象[" + clazz.getSimpleName() + "]出现未知异常", e);
        }

    }
}
