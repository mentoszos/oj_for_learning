package com.codecollab.oj.Handler;

import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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
    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e) throws AccessDeniedException {
        throw e;
    }

    /**
     * 2. 专门处理认证失败 (登录相关)
     */
    @ExceptionHandler(AuthenticationException.class)
    public BaseResponse<?> handleAuthenticationException(AuthenticationException e) throws AuthenticationException {
        throw e;
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        // 如果是未知的系统错误，统一返回 500
        return BaseResponse.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
