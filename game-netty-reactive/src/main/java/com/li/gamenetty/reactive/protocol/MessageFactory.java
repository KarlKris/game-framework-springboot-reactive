package com.li.gamenetty.reactive.protocol;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ZipUtil;
import com.li.gamenetty.reactive.service.command.Command;
import com.li.gamenetty.reactive.service.command.impl.IdentityMethodParameter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author li-yuanwen
 * 消息工厂
 * (推送,应答前只能通过工厂构建消息,不允许私自调用对应消息的构造方法或静态创建方法)
 */
public class MessageFactory {

    /** 服务器内部心跳消息包 **/
    public static final InnerMessage HEART_BEAT_REQ_INNER_MSG = InnerMessage.of(
            InnerMessageHeader.of(ProtocolConstant.HEART_BEAT_REQ, null, false, SerializeType.PROTO_STUFF.getType(),  0, null)
            , null);
    public static final InnerMessage HEART_BEAT_RES_INNER_MSG = InnerMessage.of(
            InnerMessageHeader.of(ProtocolConstant.HEART_BEAT_RES, null, false, SerializeType.PROTO_STUFF.getType(),  0, null)
            , null);

    /** 服务器外部心跳消息包 **/
    public static final OuterMessage HEART_BEAT_REQ_OUTER_MSG = OuterMessage.of(
            OuterMessageHeader.of(0, ProtocolConstant.HEART_BEAT_REQ, null, false, SerializeType.JSON.getType())
            , null);
    public static final OuterMessage HEART_BEAT_RES_OUTER_MSG = OuterMessage.of(
            OuterMessageHeader.of(0, ProtocolConstant.HEART_BEAT_RES, null, false, SerializeType.JSON.getType())
            , null);



    /**
     * 构建内部消息
     * @param sn 消息序号
     * @param type 消息类型
     * @param command 命令
     * @param serializeType 序列化类型
     * @param zip 消息体是否压缩
     * @param body 消息体
     * @param ip ip
     * @return 内部消息
     */
    public static InnerMessage toInnerMessage(long sn, byte type, Command command
            , byte serializeType, boolean zip, byte[] body, String ip) {
        byte[] ipBytes = StringUtils.isEmpty(ip) ? null : ip.getBytes();
        InnerMessageHeader header = InnerMessageHeader.of(type, command, zip, serializeType, sn, ipBytes);
        return InnerMessage.of(header, body);
    }


    /**
     * 构建外部消息
     * @param sn 消息序号
     * @param type 消息类型
     * @param command 命令
     * @param serializeType 序列化类型
     * @param zip 消息体是否压缩
     * @param body 消息体
     * @return 外部消息
     */
    public static OuterMessage toOuterMessage(long sn, byte type, Command command
            , byte serializeType, boolean zip, byte[] body) {
        OuterMessageHeader header = OuterMessageHeader.of(sn, type, command, zip, serializeType);
        return OuterMessage.of(header, body);
    }

    /**
     * 将请求消息体增加Identity身份标识参数
     * @param identity 身份标识
     * @param body 原消息体
     * @param zip 消息体是否压缩
     * @param serializer 序列化器
     * @return 增加后消息体
     */
    public static byte[] addIdentityInRequestBody(long identity, byte[] body, boolean zip, Serializer serializer) {
        if (zip) {
            body = ZipUtil.unGzip(body);
        }

        Map<String, Object> params;
        if (!ArrayUtil.isEmpty(body)) {
            Request request = serializer.deserialize(body, Request.class);
            params = new HashMap<>(request.getParams());
            params.put(IdentityMethodParameter.TYPE, identity);
        }else {
            params = Collections.singletonMap(IdentityMethodParameter.TYPE, identity);
        }

        body = serializer.serialize(new Request(params));

        if (zip) {
            body = ZipUtil.gzip(body);
        }

        return body;
    }

}
