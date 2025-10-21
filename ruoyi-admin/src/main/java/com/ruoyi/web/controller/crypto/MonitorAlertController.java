package com.ruoyi.web.controller.crypto;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 监控预警Controller
 * 
 * @author ruoyi
 * @date 2025-10-21
 */
@RestController
@RequestMapping("/crypto/monitorAlert")
public class MonitorAlertController extends BaseController
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 查询最新预警记录（用于首页通知中心显示）
     * 合并监控预警和配置变更记录
     */
    @GetMapping("/recent")
    public AjaxResult recent(@RequestParam(defaultValue = "10") int limit)
    {
        // 1. 查询监控预警记录
        String alertSql = "SELECT " +
                "id, ca, token_name, token_symbol, trigger_time, " +
                "trigger_events, stats_data, notify_methods, notify_status " +
                "FROM token_monitor_alert_log " +
                "ORDER BY trigger_time DESC " +
                "LIMIT ?";
        
        List<Map<String, Object>> alerts = jdbcTemplate.queryForList(alertSql, limit);
        
        // 2. 查询配置变更记录（全局监控配置）
        String configSql = "SELECT " +
                "id, chain_type, config_name, status, update_time, update_by " +
                "FROM global_monitor_config " +
                "ORDER BY update_time DESC " +
                "LIMIT ?";
        
        List<Map<String, Object>> configs = jdbcTemplate.queryForList(configSql, limit);
        
        // 转换为前端通知格式
        List<Map<String, Object>> notifications = new ArrayList<>();
        
        // 转换监控预警
        for (Map<String, Object> alert : alerts) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", "alert_" + alert.get("id"));
            notification.put("module", "token-monitor");
            notification.put("moduleName", "Token监控");
            notification.put("type", "alert");
            
            // 构建标题
            String tokenSymbol = (String) alert.get("token_symbol");
            String title = tokenSymbol + " 触发监控预警";
            notification.put("title", title);
            
            // 解析trigger_events获取详情
            String triggerEvents = (String) alert.get("trigger_events");
            notification.put("content", parseAlertContent(triggerEvents));
            
            // 跳转链接
            String ca = (String) alert.get("ca");
            notification.put("actionUrl", "/crypto/tokenMonitor?ca=" + ca + "&chain=sol");
            
            notification.put("isRead", 0);
            notification.put("createTime", alert.get("trigger_time"));
            
            notifications.add(notification);
        }
        
        // 转换配置变更记录
        for (Map<String, Object> config : configs) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("id", "config_" + config.get("id"));
            notification.put("module", "global-monitor");
            notification.put("moduleName", "链监控");
            notification.put("type", "success");
            
            String chainType = (String) config.get("chain_type");
            String configName = (String) config.get("config_name");
            String status = (String) config.get("status");
            
            String title = chainType.toUpperCase() + "链监控配置" + 
                          ("1".equals(status) ? "已启用" : "已更新");
            notification.put("title", title);
            notification.put("content", "配置名称: " + configName);
            notification.put("actionUrl", "");
            notification.put("isRead", 0);
            notification.put("createTime", config.get("update_time"));
            
            notifications.add(notification);
        }
        
        // 按时间倒序排序
        notifications.sort((a, b) -> {
            Object timeA = a.get("createTime");
            Object timeB = b.get("createTime");
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.toString().compareTo(timeA.toString());
        });
        
        // 限制返回数量
        if (notifications.size() > limit) {
            notifications = notifications.subList(0, limit);
        }
        
        return success(notifications);
    }
    
    /**
     * 解析预警内容
     */
    private String parseAlertContent(String triggerEvents) {
        if (triggerEvents == null || triggerEvents.isEmpty()) {
            return "触发监控条件";
        }
        
        // 简单解析JSON数组
        // 格式: [{"type":"价格上涨","value":12.74,"threshold":10,"description":"涨幅 +12.74%"}]
        try {
            // 提取description字段
            if (triggerEvents.contains("description")) {
                int start = triggerEvents.indexOf("description") + 15; // "description": "
                int end = triggerEvents.indexOf("\"", start);
                if (start > 0 && end > start) {
                    String content = triggerEvents.substring(start, end);
                    // 解码Unicode转义字符
                    return decodeUnicode(content);
                }
            }
            return "触发监控条件";
        } catch (Exception e) {
            return "触发监控条件";
        }
    }
    
    /**
     * 解码Unicode转义字符
     * 例如: \u4ef7\u683c\u4e0a\u6da8 -> 价格上涨
     */
    private String decodeUnicode(String str) {
        if (str == null || !str.contains("\\u")) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (i < str.length() - 5 && str.charAt(i) == '\\' && str.charAt(i + 1) == 'u') {
                try {
                    String hex = str.substring(i + 2, i + 6);
                    int code = Integer.parseInt(hex, 16);
                    sb.append((char) code);
                    i += 6;
                } catch (Exception e) {
                    sb.append(str.charAt(i));
                    i++;
                }
            } else {
                sb.append(str.charAt(i));
                i++;
            }
        }
        return sb.toString();
    }
}

