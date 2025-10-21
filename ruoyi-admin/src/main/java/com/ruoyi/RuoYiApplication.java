package com.ruoyi;

import com.ruoyi.crypto.config.CryptoMonitorConfig;
import com.ruoyi.web.config.WebSocketConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * 启动程序
 * 
 * @author ruoyi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
@Import({CryptoMonitorConfig.class, WebSocketConfig.class})
public class RuoYiApplication
{
    public static void main(String[] args)
    {
        // 启动Spring Boot应用
        SpringApplication.run(RuoYiApplication.class, args);
        System.out.println("==========================================");
        System.out.println("RuoYi 系统启动成功！");
        System.out.println("WebSocket 端点：ws://localhost:8080/ws/notification/{token}");
        System.out.println("==========================================");
    }
}
