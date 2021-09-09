package com.li.gamenetty.reactive.handler;

/**
 * @author li-yuanwen
 * 防火墙Service层
 */
public interface FirewallService {


    /** 开启防火墙(除白名单外不可连接) **/
    void open();

    /** 关闭防火墙(除黑名单外都可连接) **/
    void close();

    /**
     * 添加白名单
     * @param ip ip地址
     */
    void addWhiteIp(String ip);

    /**
     * 移除白名单
     * @param ip ip地址
     */
    void removeWhiteIp(String ip);

    /**
     * 添加黑名单
     * @param ip ip地址
     */
    void addBlackIp(String ip);

    /**
     * 查询最大连接数
     * @return 最大连接数
     */
    int getMaxConnectNum();


}
