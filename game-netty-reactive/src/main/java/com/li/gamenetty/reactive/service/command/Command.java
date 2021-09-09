package com.li.gamenetty.reactive.service.command;

import io.netty.buffer.ByteBuf;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 封装请求业务标识
 */
@Getter
public class Command {

    /** 消息所属模块号 **/
    private short module;
    /** 消息所属命令号 **/
    private byte instruction;

    public Command(short module, byte instruction) {
        this.module = module;
        this.instruction = instruction;
    }

    /** 是否是推送命令 **/
    public boolean push() {
        return instruction < 0;
    }

    /** 写入至ByteBuf **/
    public void writeTo(ByteBuf out) {
        out.writeShort(module);
        out.writeByte(instruction);
    }

    /** 从ByteBuf中读取 **/
    public static Command readIn(ByteBuf in) {
        return new Command(in.readShort(), in.readByte());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Command)) {
            return false;
        }

        Command c = (Command) o;

        if (module != c.module) {
            return false;
        }
        return instruction == c.instruction;
    }

    @Override
    public int hashCode() {
        int result = module;
        result = 31 * result + (int) instruction;
        return result;
    }
}
