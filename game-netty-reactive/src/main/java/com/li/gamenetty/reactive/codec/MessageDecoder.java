package com.li.gamenetty.reactive.codec;

import com.li.gamenetty.reactive.protocol.InnerMessage;
import com.li.gamenetty.reactive.protocol.OuterMessage;
import com.li.gamenetty.reactive.protocol.ProtocolConstant;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author li-yuanwen
 * 自定义协议消息解码
 */
@Slf4j
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    /** 最大包长度,长度字段位移字节数,长度字段所占字节数 **/
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf buf = (ByteBuf) super.decode(ctx, in);
        if (buf == null) {
            return null;
        }

        short protocolHeaderIdentity = ProtocolConstant.getProtocolHeaderIdentity(buf);
        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_INNER_HEADER_IDENTITY) {
            return InnerMessage.readIn(buf);
        }

        if (protocolHeaderIdentity == ProtocolConstant.PROTOCOL_OUTER_HEADER_IDENTITY) {
            return OuterMessage.readIn(buf);
        }

        if (log.isDebugEnabled()) {
            log.debug("收到协议头[{}],暂不支持该协议", protocolHeaderIdentity);
        }

        return null;
    }
}
