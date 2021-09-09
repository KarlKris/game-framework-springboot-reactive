package com.li.gamenetty.reactive.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.li.gamecommon.exception.code.ResultCode;
import lombok.Getter;

/**
 * @author li-yuanwen
 * 消息响应中消息体封装
 */
@Getter
public class Response<T> {

    /** 默认成功响应 **/
    public static final Response<Object> DEFAULT_SUCCESS = SUCCESS(null);
    /** 序列化/反序列化失败 **/
    public static final Response<Object> SERIALIZE_FAIL = ERROR(ResultCode.SERIALIZE_FAIL);
    /** 身份未认定 **/
    public static final Response<Object> NO_IDENTITY = ERROR(ResultCode.NO_IDENTITY);
    /** 类型转换 **/
    public static final Response<Object> CONVERT_FAIL = ERROR(ResultCode.CONVERT_FAIL);
    /** 参数解析异常 **/
    public static final Response<Object> PARAM_ANALYSIS_ERROR = ERROR(ResultCode.PARAM_ANALYSIS_ERROR);
    /** 无效操作 **/
    public static final Response<Object> INVALID_OP = ERROR(ResultCode.INVALID_OP);
    /** 未知错误 **/
    public static final Response<Object> UNKNOWN = ERROR(ResultCode.UNKNOWN);


    /** 状态码 **/
    private int code;
    /** 响应内容 **/
    private T content;

    /** 请求是否成功 **/
    public boolean success() {
        return code == ResultCode.SUCCESS;
    }

    /** 是否是业务逻辑失败 **/
    @JsonIgnore
    public boolean isVocationalException() {
        return code > 0;
    }

    public static <T> Response<T> SUCCESS(T content) {
        Response<T> response = new Response();
        response.code = ResultCode.SUCCESS;
        response.content = content;
        return response;
    }

    public static <T> Response<T> ERROR(int error) {
        Response<T> response = new Response();
        response.code = error;
        return response;
    }

}
