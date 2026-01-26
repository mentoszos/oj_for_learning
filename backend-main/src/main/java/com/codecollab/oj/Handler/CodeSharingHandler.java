package com.codecollab.oj.Handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * 协同编程核心处理器
 * 方案：WebSocket + Redis 状态快照
 */
@Component
@Slf4j
public class CodeSharingHandler extends TextWebSocketHandler {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // 1. 核心路由表：roomId -> Set<Session> (CopyOnWriteArraySet 适合读多写少)
    private static final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 2. 会话辅助表：sessionId -> roomId (用于断开连接时快速定位房间)
    private static final Map<String, String> sessionToRoom = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String REDIS_ROOM_CODE_PREFIX = "oj:room:code:";

    /**
     * 连接建立成功：入场即同步
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // 从 Interceptor 存入的 Attributes 中获取参数
        String roomId = (String) session.getAttributes().get("roomId");
        String userId = (String) session.getAttributes().get("userId");

        if (roomId == null || userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        // 加入内存映射
        roomSessions.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionToRoom.put(session.getId(), roomId);

        // --- 同步初始化快照 ---
        String currentCode = stringRedisTemplate.opsForValue().get(REDIS_ROOM_CODE_PREFIX + roomId);
        if (currentCode != null) {
            // 发送 INIT 类型的消息，告知客户端当前全量代码
            String initMsg = objectMapper.writeValueAsString(Map.of(
                    "type", "INIT",
                    "data", currentCode,
                    "userId", "SYSTEM"
            ));
            session.sendMessage(new TextMessage(initMsg));
        }

        log.info("用户 [{}] 进入房间 [{}], 当前在线: {} 人", userId, roomId, roomSessions.get(roomId).size());
    }

    /**
     * 收到消息：实时转发 + 异步缓存
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        String roomId = (String) session.getAttributes().get("roomId");

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.get("type").asText();

            // 1. 广播给房间内其他人 (EDIT, CURSOR, etc.)
            broadcastToRoom(roomId, session.getId(), message);

            // 2. 如果是代码变动，更新 Redis 缓存 (设置 2 小时过期)
            if ("EDIT".equals(type)) {
                String fullCode = jsonNode.get("data").asText();
                // 建议：对于 3000 并发，可以使用线程池异步写入 Redis，避免阻塞 WS 线程
                stringRedisTemplate.opsForValue().set(
                        REDIS_ROOM_CODE_PREFIX + roomId,
                        fullCode,
                        2, TimeUnit.HOURS
                );
            }
        } catch (Exception e) {
            log.error("处理协同消息失败: {}", e.getMessage());
        }
    }

    /**
     * 广播逻辑：排除发送者
     */
    private void broadcastToRoom(String roomId, String senderSessionId, TextMessage message) {
        Set<WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions == null) return;

        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.getId().equals(senderSessionId)) {
                try {
                    s.sendMessage(message);
                } catch (IOException e) {
                    log.error("推送消息给 Session {} 失败", s.getId());
                }
            }
        }
    }

    /**
     * 连接断开：清理内存占用
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = sessionToRoom.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                // 房间没人了，清理 Key，释放内存
                if (sessions.isEmpty()) {
                    roomSessions.remove(roomId);
                    log.info("房间 [{}] 已空，执行清理", roomId);
                }
            }
        }
    }

    /**
     * 传输错误处理
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        log.error("WebSocket 传输错误: {}", exception.getMessage());
    }
}