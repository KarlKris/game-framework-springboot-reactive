package com.li.gamenetty.reactive.protocol;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li-yuanwen
 * @date 2021/8/4 22:08
 * 序列化与反序列化工具管理
 **/
@Component
public class SerializerManager {

    /** 客户端默认序列化/反序列化类型 **/
    @Value(("${netty.default.serialize.type:PROTO_STUFF}"))
    private SerializeType defaultSerializeType;

    @Autowired
    private ApplicationContext applicationContext;

    /** 消息体序列化器 **/
    private Map<Byte, Serializer> serializerHolder;

    @PostConstruct
    private void init() {
        this.serializerHolder = new HashMap<>(2);
        for (Serializer serializer : applicationContext.getBeansOfType(Serializer.class).values()) {
            if (this.serializerHolder.putIfAbsent(serializer.getSerializerType(), serializer) != null) {
                throw new BeanInitializationException("出现相同类型[" + serializer.getSerializerType() + "]序列化器");
            }
        }
    }

    /** 获取序列化/反序列化工具 **/
    public Serializer getSerializer(byte type) {
        return this.serializerHolder.get(type);
    }

    /** 获取默认序列化/反序列化工具 **/
    public Serializer getDefaultSerializer() {
        return getSerializer(defaultSerializeType.getType());
    }


}
