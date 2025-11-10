package com.ruoyi.crypto.service.impl;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.crypto.service.IMonitorDashboardService;
import com.ruoyi.crypto.mapper.MonitorConfigMapper;
import com.ruoyi.crypto.mapper.MonitorTaskMapper;
import com.ruoyi.crypto.mapper.MonitorBatchMapper;
import com.ruoyi.crypto.domain.MonitorConfig;
import com.ruoyi.crypto.domain.MonitorTask;
import com.ruoyi.crypto.domain.MonitorBatch;

/**
 * 监控总览Dashboard服务实现
 * 
 * @author ruoyi
 */
@Service
public class MonitorDashboardServiceImpl implements IMonitorDashboardService
{
    @Autowired
    private MonitorConfigMapper configMapper;

    @Autowired
    private MonitorTaskMapper taskMapper;

    @Autowired
    private MonitorBatchMapper batchMapper;

    /**
     * 获取配置-任务-批次关系数据
     */
    @Override
    public Map<String, Object> getRelationData()
    {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> links = new ArrayList<>();

        // 1. 获取所有配置（限制数量，提升性能）
        MonitorConfig configQuery = new MonitorConfig();
        configQuery.setStatus(1); // 只查启用的配置
        List<MonitorConfig> configs = configMapper.selectMonitorConfigList(configQuery);
        // 性能优化：只显示前5个配置
        if (configs.size() > 5) {
            configs = configs.subList(0, 5);
        }

        for (MonitorConfig config : configs) {
            Map<String, Object> node = new HashMap<>();
            node.put("name", "配置：" + config.getConfigName());
            node.put("id", "config_" + config.getId());
            node.put("type", "config");
            nodes.add(node);
        }

        // 2. 获取所有任务（性能优化：限制数量）
        MonitorTask taskQuery = new MonitorTask();
        List<MonitorTask> tasks = taskMapper.selectMonitorTaskList(taskQuery);
        // 性能优化：只显示前10个任务
        if (tasks.size() > 10) {
            tasks = tasks.subList(0, 10);
        }

        Map<Long, String> taskIdMap = new HashMap<>();
        for (MonitorTask task : tasks) {
            String taskName = "任务：" + task.getTaskName();
            taskIdMap.put(task.getId(), "task_" + task.getId());
            
            Map<String, Object> node = new HashMap<>();
            node.put("name", taskName);
            node.put("id", "task_" + task.getId());
            node.put("type", "task");
            nodes.add(node);

            // 3. 查询任务关联的配置并创建连接
            // 从中间表 monitor_task_config_v2 查询关联的配置
            List<Long> configIds = getTaskConfigIds(task.getId());
            if (configIds != null && !configIds.isEmpty()) {
                // 为每个关联的配置创建一条连接
                for (Long configId : configIds) {
                    String configName = getConfigName(configId, configs);
                    if (!"未知配置".equals(configName)) {
                        Map<String, Object> link = new HashMap<>();
                        link.put("source", "配置：" + configName);
                        link.put("target", taskName);
                        link.put("value", 1);
                        links.add(link);
                    }
                }
            }
        }

        // 4. 获取批次（性能优化：减少数量）
        MonitorBatch batchQuery = new MonitorBatch();
        batchQuery.setStatus("running");
        List<MonitorBatch> batches = batchMapper.selectMonitorBatchList(batchQuery);
        // 性能优化：只显示前5个批次
        if (batches.size() > 5) {
            batches = batches.subList(0, 5);
        }

        for (MonitorBatch batch : batches) {
            String batchName = "批次：" + batch.getBatchNo();
            
            Map<String, Object> node = new HashMap<>();
            node.put("name", batchName + "（" + batch.getItemCount() + "项）");
            node.put("id", "batch_" + batch.getId());
            node.put("type", "batch");
            nodes.add(node);

            // 5. 创建任务到批次的连接
            if (batch.getTaskId() != null && taskIdMap.containsKey(batch.getTaskId())) {
                String taskName = "任务：" + getTaskName(batch.getTaskId(), tasks);
                Map<String, Object> link = new HashMap<>();
                link.put("source", taskName);
                link.put("target", batchName + "（" + batch.getItemCount() + "项）");
                link.put("value", batch.getItemCount());
                links.add(link);
            }
        }

        result.put("nodes", nodes);
        result.put("links", links);
        return result;
    }

    /**
     * 获取Dashboard综合统计
     */
    @Override
    public Map<String, Object> getDashboardSummary()
    {
        Map<String, Object> summary = new HashMap<>();

        // 配置统计
        Map<String, Object> configStats = configMapper.getConfigStats();
        summary.put("configStats", configStats);

        // 任务统计
        Map<String, Object> taskStats = taskMapper.getTaskStats();
        summary.put("taskStats", taskStats);

        // 批次统计
        Map<String, Object> batchStats = batchMapper.getBatchStats();
        summary.put("batchStats", batchStats);

        return summary;
    }

    /**
     * 根据ID获取配置名称
     */
    private String getConfigName(Long configId, List<MonitorConfig> configs)
    {
        return configs.stream()
                .filter(c -> c.getId().equals(configId))
                .map(MonitorConfig::getConfigName)
                .findFirst()
                .orElse("未知配置");
    }

    /**
     * 根据ID获取任务名称
     */
    private String getTaskName(Long taskId, List<MonitorTask> tasks)
    {
        return tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .map(MonitorTask::getTaskName)
                .findFirst()
                .orElse("未知任务");
    }
    
    /**
     * 获取任务关联的配置ID列表
     */
    private List<Long> getTaskConfigIds(Long taskId)
    {
        return taskMapper.selectConfigIdsByTaskId(taskId);
    }
}

