package com.codecollab.oj.Aspect;

import com.codecollab.oj.Manager.RedisLimiterManager;
import com.codecollab.oj.annotations.RateLimit;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.exception.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RateLimitAspect {
    @Autowired
    private RedisLimiterManager redisLimiterManager;
    private final String LIMIT_KEY_PRE = "limit:";


    @Around("@annotation(rateLimit)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        Integer userId = UserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"未登录");
        String methodName = joinPoint.getSignature().getName();
        int count = rateLimit.count();
        int period = rateLimit.period();
        redisLimiterManager.doLimit(LIMIT_KEY_PRE+methodName+":"+userId,count,period);
        return joinPoint.proceed();
    }
}
