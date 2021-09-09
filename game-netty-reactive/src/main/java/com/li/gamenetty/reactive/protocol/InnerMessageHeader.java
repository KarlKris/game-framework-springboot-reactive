package com.li.gamenetty.reactive.protocol;

import cn.hutool.core.util.ArrayUtil;
import com.li.gamenetty.reactive.service.command.Command;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 服务内部自定义协议消息头
 */
@Getter
public class InnerMessageHeader {

    /** 协议标识 **/
    private short protocolId = ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY;
    /** 消息字节长度 **/
    private int length;
    /** 消息类型 **/
    private byte type;
    /** 请求业务标识 **/
    private Command command;
    /** 消息体压缩标识(true为压缩) **/
    private boolean zip;
    /** 消息体序列化标识 **/
    private byte serializeType;

    /** 消息序号 **/
    private long sn;
    /** 消息来源IP地址(存在为null的情况) **/
    private byte[] ip;


    /** 写入至ByteBuf **/
    void writeTo(ByteBuf out) {
        out.writeShort(protocolId);
        // 长度占位
        out.writeInt(0);
        // 消息序号
        out.writeLong(sn);

        // 加入序列化标识
        type = ProtocolConstant.addSerializeType(type, serializeType);

        // 加入压缩标识
        if (zip) {
            type = ProtocolConstant.addBodyZipState(type);
        }

        out.writeByte(type);

        // 命令标识
        if (ProtocolConstant.hasState(type, ProtocolConstant.COMMAND_MARK)) {
            command.writeTo(out);
        }

        // 判断是否有ip地址
        if (ArrayUtil.isEmpty(ip)) {
            out.writeByte(0);
            return;
        }

        out.writeByte(ip.length);
        out.writeBytes(ip);
    }

    /** 从ByteBuf中读取 **/
    static InnerMessageHeader readIn(ByteBuf in) {
        InnerMessageHeader header = new InnerMessageHeader();
        header.protocolId = in.readShort();
        header.length = in.readInt();
        header.sn = in.readLong();
        header.type = in.readByte();

        if (ProtocolConstant.hasState(header.type, ProtocolConstant.COMMAND_MARK)) {
            header.command = Command.readIn(in);
        }

        header.zip = ProtocolConstant.hasState(header.type, ProtocolConstant.BODY_ZIP_MARK);
        header.serializeType = ProtocolConstant.getSerializeType(header.type);

        byte ipBytes = in.readByte();
        if (ipBytes > 0) {
            header.ip = new byte[ipBytes];
            in.readBytes(header.ip);
        }

        return header;
    }

    static InnerMessageHeader of(byte msgType, Command command
            , boolean zip, byte serializeType, long sn, byte[] ip) {
        InnerMessageHeader header = new InnerMessageHeader();
        header.type = msgType;
        header.command = command;
        header.zip = zip;
        header.serializeType = serializeType;
        header.sn = sn;
        header.ip = ip;
        return header;
    }

}
