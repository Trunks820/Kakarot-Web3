package com.ruoyi.crypto.service;

import java.util.Map;

/**
 * 监控总览Dashboard服务接口
 * 
 * @author ruoyi
 */
public interface IMonitorDashboardService
{
    /**
     * 获取配置-任务-批次关系数据
     * 
     * @return 关系数据（nodes + links）
     */
    Map<String, Object> getRelationData();

    /**
     * 获取Dashboard综合统计
     * 
     * @return 综合统计数据
     */
    Map<String, Object> getDashboardSummary();
}

