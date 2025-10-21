package com.ruoyi.web.controller.notification;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.web.controller.websocket.NotificationWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知推送接口（供 Python 调用）
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationPushController extends BaseController {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationPushController.class);
    
    /**
     * 推送接口的密钥（应与 Python 配置一致）
     * 生产环境应使用环境变量或配置中心管理
     */
    private static final String PUSH_SECRET_TOKEN = "Wy1997@Kakarot";
    
    /**
     * Python 推送通知接口
     * 
     * @param payload 包含 token、data 的请求体
     * @return 推送结果
     */
    @PostMapping("/push")
    public AjaxResult push(@RequestBody Map<String, Object> payload) {
        try {
            // 1. 验证请求 token
            String token = (String) payload.get("token");
            if (StringUtils.isEmpty(token) || !PUSH_SECRET_TOKEN.equals(token)) {
                log.warn("推送请求被拒绝：token 无效");
                return error("Token 无效");
            }
            
            // 2. 提取通知数据
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null || data.isEmpty()) {
                return error("通知数据不能为空");
            }
            
            // 3. 验证必填字段
            if (!data.containsKey("ca") || !data.containsKey("tokenSymbol")) {
                return error("缺少必填字段：ca 或 tokenSymbol");
            }
            
            // 4. 构建通知对象（与前端 NotificationCenter 格式一致）
            Map<String, Object> notification = new java.util.HashMap<>();
            notification.put("id", "alert_" + System.currentTimeMillis());
            notification.put("module", "token-monitor");
            notification.put("moduleName", "Token监控");
            notification.put("type", "alert");
            notification.put("title", data.get("tokenSymbol") + " 触发监控预警");
            notification.put("content", data.getOrDefault("triggerEvents", "触发监控条件"));
            notification.put("actionUrl", "/crypto/tokenMonitor?ca=" + data.get("ca") + "&chain=sol");
            notification.put("isRead", 0);
            notification.put("createTime", data.getOrDefault("alertTime", 
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())));
            
            // 5. 广播给所有在线用户（生产环境可根据 ca 或用户配置精准推送）
            NotificationWebSocket.broadcast(notification);
            
            log.info("通知推送成功：{}", notification);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("message", "推送成功");
            result.put("notificationId", notification.get("id"));
            return success(result);
            
        } catch (Exception e) {
            log.error("推送通知失败", e);
            return error("推送失败：" + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public AjaxResult health() {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("status", "ok");
        result.put("timestamp", System.currentTimeMillis());
        return success(result);
    }
}

