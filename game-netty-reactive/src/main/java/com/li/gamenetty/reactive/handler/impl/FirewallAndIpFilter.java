package com.li.gamenetty.reactive.handler.impl;

import com.li.gamecommon.rpc.ServerInfoUpdateService;
import com.li.gamecommon.utils.IpUtils;
import com.li.gamesocket.channelhandler.FirewallService;
import com.li.gamesocket.channelhandler.NioNettyFilter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author li-yuanwen
 * 黑白名单过滤器,防火墙
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class FirewallAndIpFilter extends ChannelInboundHandlerAdapter implements NioNettyFilter, FirewallService {

    /** 黑名单(,逗号分隔) **/
    @Value("${netty.server.blackIps:}")
    private String blackIpStr;

    /** 白名单(,逗号分隔) **/
    @Value("${netty.server.whiteIps:}")
    private String whiteIpStr;

    /** 同时最大连接数 **/
    @Value("${netty.server.maxConnectNum}")
    private int maxConnectNum;

    @Autowired(required = false)
    private ServerInfoUpdateService serverInfoUpdateService;


    /** 当前连接数 **/
    private AtomicInteger connectNum = new AtomicInteger(0);
    /** 黑名单 **/
    private CopyOnWriteArraySet<String> blackIps;
    /** 白名单 **/
    private CopyOnWriteArraySet<String> whiteIps;
    /** 防火墙开启标识 **/
    private volatile boolean open = true;


    @PostConstruct
    private void init() {
        this.blackIps = new CopyOnWriteArraySet<>();
        if (!StringUtils.isEmpty(this.blackIpStr)) {
            this.blackIps.addAll(Arrays.asList(this.blackIpStr.split(",")));
        }
        this.whiteIps = new CopyOnWriteArraySet<>();
        if (!StringUtils.isEmpty(this.whiteIpStr)) {
            this.whiteIps.addAll(Arrays.asList(this.whiteIpStr.split(",")));
        }
    }

    @Override
    public String getName() {
        return FirewallAndIpFilter.class.getSimpleName();
    }


    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String ip = IpUtils.getIp(ctx.channel().remoteAddress());
        if (StringUtils.isEmpty(ip)) {
            if (log.isDebugEnabled()) {
                log.debug("客户端无IP地址,拒绝连接");
            }

            ctx.close();
            return;
        }
        // 白名单判断
        if (!CollectionUtils.isEmpty(this.whiteIps)) {
            for (String whiteIp : this.whiteIps) {
                if (PatternMatchUtils.simpleMatch(whiteIp, ip)) {
                    this.connectNum.incrementAndGet();
                    super.channelRegistered(ctx);
                    return;
                }
            }
        }

        // 防火墙判断
        if (open) {
            if (log.isDebugEnabled()) {
                log.debug("防火墙已开启,拒绝客户端[{}]连接", ip);
            }

            ctx.close();
            return;
        }

        // 黑名单判断
        if (!CollectionUtils.isEmpty(this.blackIps)) {
            for (String blackIp : this.blackIps) {
                if (PatternMatchUtils.simpleMatch(blackIp, ip)) {
                    if (log.isDebugEnabled()) {
                        log.debug("客户端IP地址[{}]处于黑名单内,拒绝连接", ip);
                    }

                    ctx.close();
                    return;
                }
            }
        }

        // 判断连接数量
        int curNum = this.connectNum.incrementAndGet();
        if (curNum > this.maxConnectNum) {

            if (log.isDebugEnabled()) {
                log.debug("当前连接数量[{}]大于最大连接数量[{}],拒绝连接客户端IP地址[{}]连接", curNum, this.maxConnectNum, ip);
            }

            // 移除连接数
            this.connectNum.decrementAndGet();
            ctx.close();
            return;
        }

        super.channelRegistered(ctx);

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.connectNum.decrementAndGet();
        updateConnectNum();
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        updateConnectNum();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String ip = IpUtils.getIp(ctx.channel().remoteAddress());
        if (checkBlackIpContains(ip)) {
            if (log.isDebugEnabled()) {
                log.debug("客户端IP地址[{}]处于黑名单内,断开连接", ip);
            }

            // 释放
            ByteBuf byteBuf = (ByteBuf) msg;
            ReferenceCountUtil.release(byteBuf);

            ctx.close();
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void open() {
        this.open = true;
    }

    @Override
    public void close() {
        this.open = false;
    }

    @Override
    public void addWhiteIp(String ip) {
        this.whiteIps.add(ip);
    }

    @Override
    public void removeWhiteIp(String ip) {
        this.whiteIps.remove(ip);
    }

    @Override
    public void addBlackIp(String ip) {
        this.blackIps.add(ip);
    }

    private boolean checkBlackIpContains(String ip) {
        return this.blackIps.contains(ip);
    }

    @Override
    public int getMaxConnectNum() {
        return maxConnectNum;
    }

    private void updateConnectNum() {
        if (this.serverInfoUpdateService != null) {
            this.serverInfoUpdateService.updateConnectNum(this.connectNum.get());
        }
    }
}
