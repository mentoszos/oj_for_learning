package com.codecollab.oj.common;

import com.codecollab.oj.common.enums.ErrorCode;
import lombok.Data;

/**
 * 统一响应类
 */
@Data
public class BaseResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ErrorCode.SUCCESS.getCode());
        response.setMessage(ErrorCode.SUCCESS.getMessage());
        response.setData(data);
        return response;
    }
    public static <T> BaseResponse<T> success() {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(ErrorCode.SUCCESS.getCode());
        response.setMessage(ErrorCode.SUCCESS.getMessage());
        return response;
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(message);
        return response;
    }
    public static <T> BaseResponse<T> error(int code, String message) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode(errorCode.getCode());
        response.setMessage(errorCode.getMessage());
        return response;
    }
}

