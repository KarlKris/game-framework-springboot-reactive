package com.li.gamecommon.rpc;

import com.li.gamecommon.rpc.model.ServerInfo;

import java.net.SocketException;

/**
 * @author li-yuanwen
 * @date 2021/8/7 22:15
 * 服务器本地信息
 **/
public interface LocalServerService {


    /**
     * 查询本服务器的信息
     *
     * @return 服务器的信息
     */
    ServerInfo getLocalServerInfo() throws SocketException;

}
