package com.ruoyi.crypto.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Monitor V2 WebSocket配置
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@Configuration
@EnableWebSocket
public class MonitorWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private MonitorWebSocketHandler monitorWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册WebSocket端点
        registry.addHandler(monitorWebSocketHandler, "/websocket/monitor")
                .setAllowedOrigins("*");  // 允许跨域（生产环境建议配置具体域名）
    }
}

