package com.li.gamecommon.exception.code;

/**
 * @author li-yuanwen
 * @date 2021/7/31 18:57
 * 响应体消息状态码
 **/
public interface ResultCode {

    /** 请求响应成功码 **/
    int SUCCESS = 0;

    /** 小于0 服务器错误 大于0 业务逻辑错误 **/

    /** 默认失败,不对客户端展示具体原因 **/
    int UNKNOWN = -1;

    /** 无身份标识 **/
    int NO_IDENTITY = -2;

    /** 序列化消息体失败 **/
    int SERIALIZE_FAIL = -3;

    /** 方法参数类型转换失败 **/
    int CONVERT_FAIL = -4;

    /** 方法参数解析异常 **/
    int PARAM_ANALYSIS_ERROR = -5;

    /** 无效操作 **/
    int INVALID_OP = -6;

    /** 超时 **/
    int TIME_OUT = -7;

    /** 无法连接远程服务 **/
    int CANT_CONNECT_REMOTE = -8;


}
