package com.codecollab.oj.config;

import com.codecollab.oj.Filters.CodeHandshakeInterceptor;

import com.codecollab.oj.Handler.CodeSharingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket // 开启 WebSocket 支持
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private CodeSharingHandler codeSharingHandler;

    @Autowired
    private CodeHandshakeInterceptor codeHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(codeSharingHandler, "/ws/code/{roomId}/{userId}") // 注册处理路径
                .addInterceptors(codeHandshakeInterceptor) // 注入拦截器，用于获取 URL 参数
                .setAllowedOrigins("*"); // 允许跨域（生产环境建议指定具体域名）
    }
}