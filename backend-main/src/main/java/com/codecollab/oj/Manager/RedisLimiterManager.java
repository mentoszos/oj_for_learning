package com.codecollab.oj.Manager;

import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.exception.BusinessException;
import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class RedisLimiterManager {
    @Autowired
    private RedissonClient redissonClient;
    public void doLimit(String key,Integer count, Integer period){
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);

        rateLimiter.trySetRate(RateType.OVERALL,count,period, RateIntervalUnit.SECONDS);
        rateLimiter.expire(Duration.ofMinutes(5));
        boolean canPass = rateLimiter.tryAcquire(1);
        if (!canPass) throw  new BusinessException(ErrorCode.OPERATION_ERROR,"请求太多了，歇会");
    }
}
