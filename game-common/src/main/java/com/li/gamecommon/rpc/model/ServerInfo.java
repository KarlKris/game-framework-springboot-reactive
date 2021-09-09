package com.li.gamecommon.rpc.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * @author li-yuanwen
 * @date 2021/8/7 22:16
 * 服务器信息
 **/
@Getter
@AllArgsConstructor
public class ServerInfo {

    /** 服务器标识 **/
    private String id;
    /** 服务器ip地址 **/
    private String ip;
    /** 服务器端口 **/
    private int port;
    /** 服务器所负责的模块号 **/
    private Set<Short> modules;

}
