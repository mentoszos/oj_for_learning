package com.codecollab.oj.exception;

import com.codecollab.oj.common.constants.ErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常类
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    /**
     * 构造方法一：接受错误码枚举
     */
    public BusinessException(ErrorCode errorCode) {
        // 调用父类构造器，传入错误消息
        super(errorCode.getMessage()); 
        this.code = errorCode.getCode();
    }

    /**
     * 构造方法二：接受错误码和自定义消息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        // 调用父类构造器，传入自定义消息
        super(message); 
        this.code = errorCode.getCode();
    }

    /**
     * 构造方法三：仅接受状态码和消息（不推荐，但有时需要）
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}