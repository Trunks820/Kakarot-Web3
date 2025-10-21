package com.ruoyi.web.config;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.server.ServerContainer;

/**
 * WebSocket 配置类
 * 
 * @author ruoyi
 */
@Configuration
public class WebSocketConfig {
    
    /**
     * 注入 ServerEndpointExporter
     * 该 Bean 会自动注册使用 @ServerEndpoint 注解的 WebSocket 端点
     * 
     * 注意：如果使用外部 Tomcat 部署，需要注释掉此 Bean
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        System.out.println("========================================");
        System.out.println("📡 ServerEndpointExporter Bean 正在创建...");
        System.out.println("📡 WebSocket 端点将被自动注册");
        System.out.println("========================================");
        return new ServerEndpointExporter();
    }
    
    /**
     * 确保 WebSocket 端点被正确注册到 Servlet 容器
     */
    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext) throws ServletException {
                System.out.println("========================================");
                System.out.println("🔌 ServletContext 初始化，检查 WebSocket 容器...");
                
                ServerContainer serverContainer = (ServerContainer) servletContext.getAttribute("javax.websocket.server.ServerContainer");
                if (serverContainer != null) {
                    System.out.println("✅ WebSocket ServerContainer 已找到: " + serverContainer.getClass().getName());
                    System.out.println("📡 默认最大消息大小: " + serverContainer.getDefaultMaxTextMessageBufferSize());
                } else {
                    System.out.println("❌ WebSocket ServerContainer 未找到！");
                }
                
                System.out.println("========================================");
            }
        };
    }
}

