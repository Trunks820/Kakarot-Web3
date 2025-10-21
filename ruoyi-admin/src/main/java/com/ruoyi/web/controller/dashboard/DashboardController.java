package com.ruoyi.web.controller.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.service.TokenLaunchHistoryService;
import com.ruoyi.crypto.service.ITokenMonitorConfigService;

/**
 * Dashboard统计数据Controller
 * 
 * @author ruoyi
 * @date 2025-10-21
 */
@RestController
@RequestMapping("/dashboard/stats")
public class DashboardController extends BaseController
{
    @Autowired
    private TokenLaunchHistoryService tokenLaunchHistoryService;

    @Autowired
    private ITokenMonitorConfigService tokenMonitorConfigService;

    /**
     * 获取今日新增Token数量
     */
    @GetMapping("/today-tokens")
    public AjaxResult getTodayNewTokens()
    {
        Long count = tokenLaunchHistoryService.getTodayNewCount();
        return success(count);
    }

    /**
     * 获取监控中Token数量
     */
    @GetMapping("/monitoring-count")
    public AjaxResult getMonitoringCount()
    {
        Long count = tokenMonitorConfigService.getMonitoringCount();
        return success(count);
    }
}

