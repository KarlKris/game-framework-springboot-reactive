package com.li.gamecommon.rpc;

/**
 * @author li-yuanwen
 * 服务信息更新服务(服务器->zookeeper)
 */
public interface ServerInfoUpdateService {

    /**
     * 更新服务器连接数
     * @param connectNum 连接数
     */
    void updateConnectNum(int connectNum);

}
