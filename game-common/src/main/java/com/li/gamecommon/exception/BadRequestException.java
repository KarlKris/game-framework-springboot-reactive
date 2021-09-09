package com.li.gamecommon.exception;

import lombok.Getter;

/**
 * @author li-yuanwen
 * @date 2021/7/31 15:49
 * 错误请求异常
 **/
@Getter
public class BadRequestException extends RuntimeException {

    private int errorCode;
    private String message;

    public BadRequestException(int errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public BadRequestException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }


}
