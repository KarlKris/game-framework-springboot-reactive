package com.li.gamecommon.rpc;

import com.li.gamecommon.rpc.model.Address;

/**
 * @author li-yuanwen
 * 远程服务器查找接口
 */
public interface RemoteServerSeekService {


    /**
     * 查询指定模块号所属的服务
     *
     * @param module 模块号
     * @return 服务名
     */
    String seekServiceNameByModule(short module);

    /**
     * 查询指定模块号所属的服务并根据身份标识筛选出一个特定的服务器地址
     *
     * @param module   模块号
     * @param identity 请求的client的session#identity
     * @return ip地址
     */
    Address seekApplicationAddressByModule(short module, long identity);

    /**
     * 查询指定模块号所属的服务并根据服标识筛选出一个特定的服务器地址
     * @param module 模块号
     * @param id 服标识
     * @return /
     */
    Address seekApplicationAddressById(short module, String id);


}
