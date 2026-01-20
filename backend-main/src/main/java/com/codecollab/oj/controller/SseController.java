package com.codecollab.oj.controller;

import com.codecollab.oj.Manager.SseManager;
import com.codecollab.oj.context.UserHolder;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public SseEmitter getEmitter(){
        log.info("调用sse获取接口");
        Integer userId = UserHolder.getUserId();
        return sseManager.get(userId);
    }
}
