package com.li.gamenetty.reactive.protocol.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.li.gamecommon.exception.SerializeFailException;
import com.li.gamenetty.reactive.protocol.SerializeType;
import com.li.gamenetty.reactive.protocol.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author li-yuanwen
 * @date 2021/8/8 09:33
 * json序列化与反序列化  jackson
 **/
@Component
@Slf4j
public class JsonSerializer implements Serializer {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public byte getSerializerType() {
        return SerializeType.JSON.getType();
    }

    @Override
    public <T> byte[] serialize(T obj) throws SerializeFailException {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化对象[{}]出现未知异常", obj.getClass().getSimpleName(), e);
            throw new SerializeFailException("序列化对象[" + obj.getClass().getSimpleName() + "]出现未知异常", e);
        }
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws SerializeFailException {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (IOException e) {
            log.error("反序列化对象[{}]出现未知异常", clazz.getSimpleName(), e);
            throw new SerializeFailException("反序列化对象[" + clazz.getSimpleName() + "]出现未知异常", e);
        }
    }
}
