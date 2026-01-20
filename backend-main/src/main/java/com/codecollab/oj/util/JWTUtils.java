package com.codecollab.oj.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.exception.BusinessException;


import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    private static final byte[] SECRET = "mentos".getBytes(StandardCharsets.UTF_8);
    private JWTUtils(){
    }

    public static String createToken(Integer userId, String username){
        Map<String, Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("username",username);
        map.put(JWT.ISSUED_AT, DateUtil.date());
        map.put(JWT.EXPIRES_AT,DateUtil.offsetHour(DateUtil.date(),2));
        return JWTUtil.createToken(map,SECRET);
    }
    public static boolean validate(String token){
        try {
            return JWTUtil.verify(token, SECRET);
        }catch (Exception e){
            return false;
        }
    }
    public static JSONObject parse(String token){
        JSONObject payload = JWTUtil.parseToken(token).getPayloads();
        return payload;
    }
}
