package com.ruoyi.web.controller.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 实时通知端点
 * 
 * @author ruoyi
 */
@ServerEndpoint(value = "/ws/notification/{token}")
@Component
public class NotificationWebSocket {
    
    static {
        System.out.println("========================================");
        System.out.println("🔌 NotificationWebSocket 类正在加载...");
        System.out.println("========================================");
    }
    
    private static final Logger log = LoggerFactory.getLogger(NotificationWebSocket.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 存储所有在线客户端：key=token, value=Session集合（支持同一用户多标签页）
     */
    private static final Map<String, CopyOnWriteArraySet<Session>> CLIENT_SESSIONS = new ConcurrentHashMap<>();
    
    /**
     * 当前会话的 token
     */
    private String token;
    
    /**
     * 当前会话的 Session
     */
    private Session session;
    
    /**
     * 连接建立成功调用
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        System.out.println("========================================");
        System.out.println("🎉 WebSocket @OnOpen 被触发！");
        System.out.println("Token: " + token);
        System.out.println("========================================");
        
        this.session = session;
        this.token = token;
        
        // 验证 token（简单验证非空，生产环境应验证 JWT）
        if (StringUtils.isEmpty(token)) {
            log.warn("WebSocket 连接被拒绝：token 为空");
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭非法连接失败", e);
            }
            return;
        }
        
        // 添加到在线客户端
        CLIENT_SESSIONS.computeIfAbsent(token, k -> new CopyOnWriteArraySet<>()).add(session);
        
        log.info("WebSocket 连接成功，token={}, sessionId={}, 当前在线连接数={}", 
                 token, session.getId(), getTotalConnections());
        
        // 发送连接成功消息
        sendMessage(session, createSuccessMessage("连接成功"));
    }
    
    /**
     * 连接关闭调用
     */
    @OnClose
    public void onClose() {
        // 从在线客户端移除
        CopyOnWriteArraySet<Session> sessions = CLIENT_SESSIONS.get(token);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                CLIENT_SESSIONS.remove(token);
            }
        }
        
        log.info("WebSocket 连接关闭，token={}, sessionId={}, 当前在线连接数={}", 
                 token, session.getId(), getTotalConnections());
    }
    
    /**
     * 收到客户端消息调用（可用于心跳、历史消息请求等）
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.debug("收到客户端消息，token={}, message={}", token, message);
        
        try {
            Map<String, Object> msg = objectMapper.readValue(message, Map.class);
            String type = (String) msg.get("type");
            
            // 处理心跳
            if ("ping".equals(type)) {
                sendMessage(session, createPongMessage());
            }
            // 其他消息类型可扩展
            
        } catch (Exception e) {
            log.error("处理客户端消息失败", e);
        }
    }
    
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误，token={}, sessionId={}", token, session.getId(), error);
    }
    
    /**
     * 向指定 token 的所有客户端推送消息
     * 
     * @param token 用户 token
     * @param notification 通知数据
     */
    public static void pushToUser(String token, Map<String, Object> notification) {
        CopyOnWriteArraySet<Session> sessions = CLIENT_SESSIONS.get(token);
        if (sessions == null || sessions.isEmpty()) {
            log.debug("用户不在线，跳过推送，token={}", token);
            return;
        }
        
        try {
            Map<String, Object> messageData = new java.util.HashMap<>();
            messageData.put("type", "notification");
            messageData.put("data", notification);
            String message = objectMapper.writeValueAsString(messageData);
            
            int successCount = 0;
            for (Session session : sessions) {
                if (sendMessage(session, message)) {
                    successCount++;
                }
            }
            
            log.info("向用户推送通知成功，token={}, 推送标签页数={}/{}", 
                     token, successCount, sessions.size());
            
        } catch (Exception e) {
            log.error("向用户推送通知失败，token={}", token, e);
        }
    }
    
    /**
     * 向所有在线客户端广播消息
     * 
     * @param notification 通知数据
     */
    public static void broadcast(Map<String, Object> notification) {
        try {
            Map<String, Object> messageData = new java.util.HashMap<>();
            messageData.put("type", "notification");
            messageData.put("data", notification);
            String message = objectMapper.writeValueAsString(messageData);
            
            int totalSessions = 0;
            int successCount = 0;
            
            for (CopyOnWriteArraySet<Session> sessions : CLIENT_SESSIONS.values()) {
                for (Session session : sessions) {
                    totalSessions++;
                    if (sendMessage(session, message)) {
                        successCount++;
                    }
                }
            }
            
            log.info("广播通知完成，成功={}/{}", successCount, totalSessions);
            
        } catch (Exception e) {
            log.error("广播通知失败", e);
        }
    }
    
    /**
     * 发送消息到指定 Session
     */
    private static boolean sendMessage(Session session, String message) {
        if (session == null || !session.isOpen()) {
            return false;
        }
        
        try {
            synchronized (session) {
                session.getBasicRemote().sendText(message);
            }
            return true;
        } catch (IOException e) {
            log.error("发送消息失败，sessionId={}", session.getId(), e);
            return false;
        }
    }
    
    /**
     * 获取当前在线连接总数
     */
    private static int getTotalConnections() {
        return CLIENT_SESSIONS.values().stream()
                .mapToInt(CopyOnWriteArraySet::size)
                .sum();
    }
    
    /**
     * 创建成功消息
     */
    private String createSuccessMessage(String message) {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("type", "success");
            data.put("message", message);
            data.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"type\":\"success\",\"message\":\"" + message + "\"}";
        }
    }
    
    /**
     * 创建心跳响应消息
     */
    private String createPongMessage() {
        try {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("type", "pong");
            data.put("timestamp", System.currentTimeMillis());
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"type\":\"pong\"}";
        }
    }
}

