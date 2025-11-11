package com.ruoyi.crypto.websocket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控系统 WebSocket 处理器
 * 
 * 支持的消息类型：
 * - batch_status: 批次状态更新
 * - task_status: 任务状态更新
 * - alert: 告警通知
 * - heartbeat: 心跳
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@Component
public class MonitorWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(MonitorWebSocketHandler.class);

    /**
     * 存储所有WebSocket会话
     * key: sessionId, value: WebSocketSession
     */
    private static final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    /**
     * 在线用户计数器
     */
    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 建立连接
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        onlineCount.incrementAndGet();

        log.info("WebSocket连接建立: sessionId={}, 当前在线: {}", sessionId, onlineCount.get());

        // 发送欢迎消息
        JSONObject welcome = new JSONObject();
        welcome.put("type", "connected");
        welcome.put("sessionId", sessionId);
        welcome.put("message", "WebSocket连接成功");
        welcome.put("timestamp", System.currentTimeMillis());
        sendMessage(session, welcome);
    }

    /**
     * 接收客户端消息
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("收到消息: sessionId={}, payload={}", session.getId(), payload);

        try {
            JSONObject msg = JSON.parseObject(payload);
            String type = msg.getString("type");

            // 处理心跳
            if ("ping".equals(type)) {
                JSONObject pong = new JSONObject();
                pong.put("type", "pong");
                pong.put("timestamp", System.currentTimeMillis());
                sendMessage(session, pong);
            }
            // 处理订阅
            else if ("subscribe".equals(type)) {
                String topic = msg.getString("topic");
                log.info("客户端订阅: sessionId={}, topic={}", session.getId(), topic);
                
                // 在session中记录订阅的主题
                session.getAttributes().put("topic", topic);
                
                JSONObject response = new JSONObject();
                response.put("type", "subscribed");
                response.put("topic", topic);
                response.put("message", "订阅成功");
                sendMessage(session, response);
            }
            // 其他消息类型
            else {
                log.warn("未知消息类型: {}", type);
            }

        } catch (Exception e) {
            log.error("处理消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 连接关闭
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        onlineCount.decrementAndGet();

        log.info("WebSocket连接关闭: sessionId={}, status={}, 当前在线: {}", 
                sessionId, status, onlineCount.get());
    }

    /**
     * 传输错误
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket传输错误: sessionId={}, error={}", sessionId, exception.getMessage());

        if (session.isOpen()) {
            session.close();
        }
        sessions.remove(sessionId);
        onlineCount.decrementAndGet();
    }

    /**
     * 发送消息给指定会话
     */
    private void sendMessage(WebSocketSession session, JSONObject message) {
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(message.toJSONString()));
                }
            } catch (IOException e) {
                log.error("发送消息失败: sessionId={}, error={}", session.getId(), e.getMessage());
            }
        }
    }

    /**
     * 广播消息给所有客户端
     */
    public static void broadcast(String type, Object data) {
        JSONObject message = new JSONObject();
        message.put("type", type);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());

        String jsonMessage = message.toJSONString();
        log.debug("广播消息: type={}, clients={}", type, sessions.size());

        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(new TextMessage(jsonMessage));
                    }
                } catch (IOException e) {
                    log.error("广播消息失败: sessionId={}, error={}", session.getId(), e.getMessage());
                }
            }
        });
    }

    /**
     * 发送批次状态更新
     */
    public static void sendBatchStatus(Long batchId, String status, Map<String, Object> details) {
        JSONObject data = new JSONObject();
        data.put("batchId", batchId);
        data.put("status", status);
        data.put("details", details);
        
        broadcast("batch_status", data);
        log.info("发送批次状态: batchId={}, status={}", batchId, status);
    }

    /**
     * 发送任务状态更新
     */
    public static void sendTaskStatus(Long taskId, Integer status, String message) {
        JSONObject data = new JSONObject();
        data.put("taskId", taskId);
        data.put("status", status);
        data.put("message", message);
        
        broadcast("task_status", data);
        log.info("发送任务状态: taskId={}, status={}", taskId, status);
    }

    /**
     * 发送告警通知
     */
    public static void sendAlert(Long alertId, String alertType, Map<String, Object> alertData) {
        JSONObject data = new JSONObject();
        data.put("alertId", alertId);
        data.put("alertType", alertType);
        data.put("alertData", alertData);
        
        broadcast("alert", data);
        log.info("发送告警通知: alertId={}, type={}", alertId, alertType);
    }

    /**
     * 获取在线用户数
     */
    public static int getOnlineCount() {
        return onlineCount.get();
    }

    /**
     * 获取所有会话
     */
    public static Map<String, WebSocketSession> getSessions() {
        return sessions;
    }

    /**
     * 广播消息给所有客户端（Map参数）
     * 
     * @param message 消息内容
     */
    public void broadcastMessage(Map<String, Object> message) {
        if (message == null) {
            return;
        }
        
        String type = (String) message.get("type");
        Object data = message.get("data");
        
        if (type != null) {
            broadcast(type, data != null ? data : message);
        } else {
            // 如果没有type字段，直接使用message作为data
            broadcast("message", message);
        }
    }
}

