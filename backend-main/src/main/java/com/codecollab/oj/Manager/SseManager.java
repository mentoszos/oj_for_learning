package com.codecollab.oj.Manager;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SseManager {
    private static final Map<Integer, SseEmitter> emitters = new HashMap<>();
    public SseEmitter get(Integer userId){
        if (emitters.getOrDefault(userId,null) != null) return emitters.get(userId);
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId,emitter);
        emitter.onCompletion(()->emitters.remove(userId));
        emitter.onTimeout(()->emitters.remove(userId));
        return emitter;
    }
    public void sendMessage(Integer userId,Object data){
        SseEmitter emitter = emitters.get(userId);
        if (emitter!=null){
            try {
                emitter.send(
                        SseEmitter.event().name("judge_result")
                                .data(data, MediaType.APPLICATION_JSON)
                                .reconnectTime(3000)
                );
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(userId);
                System.err.println("用户 " + userId + " 连接已断开，清理完毕");
            }
        }
    }
}
