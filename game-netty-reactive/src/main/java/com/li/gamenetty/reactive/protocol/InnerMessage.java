package com.li.gamenetty.reactive.protocol;

import cn.hutool.core.util.ArrayUtil;
import com.li.gamenetty.reactive.service.command.Command;
import io.netty.buffer.ByteBuf;

/**
 * @author li-yuanwen
 * 自定义协议消息
 */
public class InnerMessage implements IMessage {

    /** 消息头 **/
    private InnerMessageHeader header;
    /** 消息体 **/
    private byte[] body;

    @Override
    public short getProtocolHeaderIdentity() {
        return header.getProtocolId();
    }

    @Override
    public byte getMessageType() {
        return header.getType();
    }

    @Override
    public Command getCommand() {
        return header.getCommand();
    }

    @Override
    public byte getSerializeType() {
        return header.getSerializeType();
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public long getSn() {
        return header.getSn();
    }

    /** 写入至ByteBuf **/
    public void writeTo(ByteBuf out) {
        header.writeTo(out);

        // 消息体有数据才写入
        if (ArrayUtil.isEmpty(body)) {
            return;
        }

        out.writeShort(body.length);
        out.writeBytes(body);
    }

    /** 从ByteBuf中读取 **/
    public static InnerMessage readIn(ByteBuf in) {
        InnerMessage message = new InnerMessage();
        message.header = InnerMessageHeader.readIn(in);
        if (in.readableBytes() > 0) {
            message.body = new byte[in.readShort()];
            in.readBytes(message.body);
        }
        return message;
    }

    static InnerMessage of(InnerMessageHeader header, byte[] body) {
        InnerMessage message = new InnerMessage();
        message.header = header;
        message.body = body;
        return message;
    }

}
