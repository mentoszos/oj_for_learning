package com.codecollab.oj.Filters;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import java.util.Map;

@Component
public class CodeHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            // 从 URL 路径中提取参数（假设路径是 /ws/code/{roomId}/{userId}）
            String path = servletRequest.getURI().getPath();
            String[] parts = path.split("/");
            if (parts.length >= 5) {
                attributes.put("roomId", parts[3]);
                attributes.put("userId", parts[4]);
            }
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}