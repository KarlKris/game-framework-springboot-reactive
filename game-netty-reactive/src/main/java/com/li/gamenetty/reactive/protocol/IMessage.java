package com.li.gamenetty.reactive.protocol;


import com.li.gamenetty.reactive.service.command.Command;

/**
 * @author li-yuanwen
 * 自定义协议消息接口
 */
public interface IMessage {

    /**
     * 返回协议头标识
     * @return /
     */
    short getProtocolHeaderIdentity();
    /**
     * 查询消息类型
     * @return 消息类型
     */
    byte getMessageType();

    /**
     * 获取请求指令号
     * @return command
     */
    Command getCommand();

    /**
     * 获取消息体解析类型
     * @return 消息体解析类型
     */
    byte getSerializeType();

    /**
     * 获取消息体
     * @return 消息体二进制数组
     */
    byte[] getBody();

    /**
     * 获取消息序号
     * @return 消息序号
     */
    long getSn();

    /**
     * 消息是否压缩
     * @return
     */
    default boolean zip() {
        return ProtocolConstant.zip(getMessageType());
    }

    /**
     * 消息是否是请求
     * @return true 请求
     */
    default boolean isRequest() {
        return ProtocolConstant.isRequest(getMessageType());
    }

    /**
     * 查询是否是心跳请求包
     * @return true 心跳请求包
     */
    default boolean isHeartBeatRequest() {
        return ProtocolConstant.isHeartBeatReq(getMessageType());
    }

    /**
     * 查询是否是心跳响应包
     * @return true 心跳响应包
     */
    default boolean isHeartBeatResponse() {
        return ProtocolConstant.isHeartBeatRes(getMessageType());
    }


    /**
     * 是否是外部消息
     * @return true OuterMessage
     */
    default boolean isOuterMessage() {
        return getProtocolHeaderIdentity() == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY;
    }

    /**
     * 是否是内部消息
     * @return true InnerMessage
     */
    default boolean isInnerMessage() {
        return getProtocolHeaderIdentity() == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY;
    }
}
