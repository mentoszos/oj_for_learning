package com.codecollab.oj.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.codecollab.oj.Manager.SseManager;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.util.JWTUtils;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SseController {
    @Autowired
    private SseManager sseManager;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEmitter(@RequestParam String token){
        log.info("调用sse获取接口");
        if (StrUtil.isBlank(token)) {
            log.info("sse接口token为空");return null;
        }
        if (JWTUtils.validate(token)){
            JSONObject payload = JWTUtils.parse(token);
            Integer userId = payload.getInt("userId");
            UserHolder.setUserId(userId);
            log.info("sse连接成功，用户id为{}",userId);
        return sseManager.get(userId);
        }
        log.info("sse接口token解析失败");
        return null;
    }
}
