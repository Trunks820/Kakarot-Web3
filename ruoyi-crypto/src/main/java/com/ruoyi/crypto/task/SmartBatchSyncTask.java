package com.ruoyi.crypto.task;

import com.ruoyi.crypto.domain.MonitorTask;
import com.ruoyi.crypto.mapper.MonitorTaskMapper;
import com.ruoyi.crypto.service.ISmartBatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 智能批次自动同步定时任务
 * 
 * 核心逻辑：
 * 1. 每分钟扫描一次所有智能任务
 * 2. 检查哪些任务需要执行（auto_sync_targets=1 且到达执行时间）
 * 3. 根据每个任务自己的 sync_interval_minutes 配置独立执行
 * 4. 执行完成后更新 last_run_time 和 next_run_time
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@Component
public class SmartBatchSyncTask {
    
    private static final Logger logger = LoggerFactory.getLogger(SmartBatchSyncTask.class);
    
    @Autowired
    private ISmartBatchService smartBatchService;
    
    @Autowired
    private MonitorTaskMapper monitorTaskMapper;
    
    /**
     * 定时扫描任务（每分钟执行一次）
     * 
     * 检查哪些任务需要执行：
     * - task_type = 'smart'
     * - status = 1（启用）
     * - auto_sync_targets = 1（启用自动同步）
     * - next_run_time <= NOW()（到达执行时间）
     */
    @Scheduled(cron = "0 * * * * ?")  // 每分钟执行一次
    public void scanAndExecuteTasks() {
        try {
            // 1. 查询所有需要执行的任务
            List<MonitorTask> tasksToRun = findTasksToRun();
            
            if (tasksToRun.isEmpty()) {
                logger.debug("⏭ 当前没有需要执行的智能任务");
                return;
            }
            
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║   智能批次自动同步：发现 {} 个任务需要执行", tasksToRun.size());
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
            // 2. 逐个执行任务
            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;
            
            for (MonitorTask task : tasksToRun) {
                try {
                    logger.info("→ 开始执行任务：id={}, name={}, interval={}分钟", 
                        task.getId(), task.getTaskName(), task.getSyncIntervalMinutes());
                    
                    // 执行同步
                    Map<String, Object> result = smartBatchService.syncTargetsAndAllocateBatches(task.getId());
                    
                    if (Boolean.TRUE.equals(result.get("success"))) {
                        successCount++;
                        logger.info("✅ 任务执行成功：id={}, epoch={}, 耗时={}", 
                            task.getId(), result.get("newEpoch"), result.get("duration"));
                        
                        // 更新任务的执行时间
                        updateTaskRunTime(task);
                        
                    } else {
                        String message = (String) result.get("message");
                        if (message != null && message.contains("分布式锁失败")) {
                            skipCount++;
                            logger.warn("⏭ 任务跳过（锁冲突）：id={}", task.getId());
                            // 锁冲突不更新执行时间，下次继续尝试
                        } else {
                            failCount++;
                            logger.error("❌ 任务执行失败：id={}, error={}", task.getId(), message);
                            // 失败也更新执行时间，避免频繁重试
                            updateTaskRunTime(task);
                        }
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    logger.error("❌ 任务执行异常：id=" + task.getId(), e);
                    // 异常也更新执行时间，避免频繁重试
                    updateTaskRunTime(task);
                }
            }
            
            // 3. 输出汇总统计
            logger.info("╔════════════════════════════════════════════════════════════╗");
            logger.info("║   执行完成：成功={}, 失败={}, 跳过={}                    ", 
                successCount, failCount, skipCount);
            logger.info("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            logger.error("❌ 定时任务扫描失败", e);
        }
    }
    
    /**
     * 查询需要执行的任务
     */
    private List<MonitorTask> findTasksToRun() {
        // 构建查询条件
        MonitorTask query = new MonitorTask();
        query.setTaskType("smart");
        query.setStatus(1);  // 启用
        query.setAutoSyncTargets(1);  // 启用自动同步
        
        // 查询所有符合条件的任务
        List<MonitorTask> allTasks = monitorTaskMapper.selectMonitorTaskList(query);
        
        // 过滤出到达执行时间的任务
        Date now = new Date();
        return allTasks.stream()
            .filter(task -> {
                Date nextRunTime = task.getNextRunTime();
                // 如果 next_run_time 为空或者已到达执行时间
                return nextRunTime == null || nextRunTime.before(now) || nextRunTime.equals(now);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 更新任务的执行时间
     */
    private void updateTaskRunTime(MonitorTask task) {
        try {
            Date now = new Date();
            Integer intervalMinutes = task.getSyncIntervalMinutes();
            
            // 如果没有配置间隔，默认30分钟
            if (intervalMinutes == null || intervalMinutes <= 0) {
                intervalMinutes = 30;
                logger.warn("任务{}未配置同步间隔，使用默认值30分钟", task.getId());
            }
            
            // 计算下次执行时间
            long nextRunTimeMillis = now.getTime() + (intervalMinutes * 60 * 1000L);
            Date nextRunTime = new Date(nextRunTimeMillis);
            
            // 更新数据库
            MonitorTask updateTask = new MonitorTask();
            updateTask.setId(task.getId());
            updateTask.setLastRunTime(now);
            updateTask.setNextRunTime(nextRunTime);
            updateTask.setUpdateTime(now);
            
            monitorTaskMapper.updateMonitorTask(updateTask);
            
            logger.debug("更新任务执行时间：id={}, next_run_time={}", task.getId(), nextRunTime);
            
        } catch (Exception e) {
            logger.error("更新任务执行时间失败：id=" + task.getId(), e);
        }
    }
    
    /**
     * 手动触发同步（提供给管理接口调用）
     */
    public Map<String, Object> manualSync() {
        logger.info("🔧 手动触发智能批次同步");
        return smartBatchService.syncTargetsAndAllocateBatches();
    }
}

