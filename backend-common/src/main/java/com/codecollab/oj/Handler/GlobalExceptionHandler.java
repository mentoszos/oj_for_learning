package com.codecollab.oj.Handler;

import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.constants.ErrorCode;
import com.codecollab.oj.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e){
        log.error("businessException",e);
        return BaseResponse.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        // 如果是未知的系统错误，统一返回 500
        return BaseResponse.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
