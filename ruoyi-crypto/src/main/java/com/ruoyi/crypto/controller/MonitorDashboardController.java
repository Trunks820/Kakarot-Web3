package com.ruoyi.crypto.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.service.IMonitorDashboardService;

/**
 * 监控总览Dashboard控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/crypto/monitor-v2/dashboard")
public class MonitorDashboardController extends BaseController
{
    @Autowired
    private IMonitorDashboardService dashboardService;

    /**
     * 获取配置-任务-批次关系数据
     */
    @GetMapping("/relation")
    public AjaxResult getRelationData()
    {
        Map<String, Object> relationData = dashboardService.getRelationData();
        return success(relationData);
    }

    /**
     * 获取Dashboard综合统计数据
     */
    @GetMapping("/summary")
    public AjaxResult getSummary()
    {
        Map<String, Object> summary = dashboardService.getDashboardSummary();
        return success(summary);
    }
}

