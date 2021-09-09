package com.li.gamenetty.reactive.service.rpc.impl;

import com.li.gamecommon.exception.BadRequestException;
import com.li.gamecommon.exception.SocketException;
import com.li.gamenetty.reactive.protocol.IMessage;
import com.li.gamenetty.reactive.protocol.Response;
import com.li.gamenetty.reactive.protocol.Serializer;
import com.li.gamenetty.reactive.protocol.SerializerManager;
import com.li.gamenetty.reactive.service.rpc.RemoteResultProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @author li-yuanwen
 */
@Slf4j
@Component
public class RpcResultProcessor implements RemoteResultProcessor<RpcSnCtx> {

    @Autowired
    private SerializerManager serializerManager;

    @Override
    public byte getType() {
        return RpcSnCtx.TYPE;
    }

    @Override
    public void process(RpcSnCtx rpcSnCtx, IMessage msg) {
        CompletableFuture<Response> future = rpcSnCtx.getFuture();

        Serializer serializer = serializerManager.getSerializer(msg.getSerializeType());
        Response response = serializer.deserialize(msg.getBody(), Response.class);
        if (response.success()) {
            future.complete(response);
        } else {
            if (response.isVocationalException()) {
                future.completeExceptionally(new BadRequestException(response.getCode(), "请求远程服务业务异常"));
            } else {
                future.completeExceptionally(new SocketException(response.getCode(), "请求远程服务通讯异常"));
            }
        }
    }
}
