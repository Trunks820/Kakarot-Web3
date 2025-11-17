package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.MonitorBatch;
import com.ruoyi.crypto.domain.MonitorTask;
import com.ruoyi.crypto.mapper.MonitorBatchMapper;
import com.ruoyi.crypto.mapper.MonitorTaskMapper;
import com.ruoyi.crypto.mapper.MonitorTaskTargetMapper;
import com.ruoyi.crypto.service.IMonitorTaskService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 监控任务Controller
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
@RestController
@RequestMapping("/crypto/monitor-v2/task")
public class MonitorTaskController extends BaseController
{
    @Resource
    private IMonitorTaskService monitorTaskService;
    
    @Resource
    private MonitorTaskMapper monitorTaskMapper;
    
    @Resource
    private MonitorTaskTargetMapper monitorTaskTargetMapper;

    @Resource
    private MonitorBatchMapper monitorBatchMapper;

    /**
     * 查询监控任务列表（增强版：包含统计信息）
     */
    @GetMapping("/list")
    public TableDataInfo list(MonitorTask monitorTask)
    {
        startPage();
        List<MonitorTask> list = monitorTaskService.selectMonitorTaskList(monitorTask);
        
        // ⭐ 增强：为每个任务填充统计信息
        for (MonitorTask task : list) {
            try {
                // 1. 查询目标数量（对于智能任务和批量任务）
                if (!"block".equals(task.getTaskType())) {
                    int targetCount = monitorTaskTargetMapper.countByTaskId(task.getId());
                    task.setTargetCount(targetCount);
                }
                
                // 2. 查询配置数量
                List<Long> configIds = monitorTaskMapper.selectConfigIdsByTaskId(task.getId());
                task.setConfigCount(configIds != null ? configIds.size() : 0);
                task.setConfigIds(configIds);

                // 3. 查询批次数量
                MonitorBatch monitorBatch = new MonitorBatch();
                monitorBatch.setTaskId(task.getId());
                List<MonitorBatch> batchIds = monitorBatchMapper.selectMonitorBatchList(monitorBatch);
                Integer sumBatch = 0;
                for (MonitorBatch batch : batchIds) {
                    Integer itemCount = batch.getItemCount();
                    sumBatch += itemCount;
                }
                task.setBatchCount(sumBatch);
            } catch (Exception e) {
                // 如果统计失败，设置为0，不影响主流程
                logger.error("获取任务统计信息失败: taskId=" + task.getId(), e);
                if (task.getTargetCount() == null) {
                    task.setTargetCount(0);
                }
                if (task.getConfigCount() == null) {
                    task.setConfigCount(0);
                }
            }
        }
        
        return getDataTable(list);
    }

    /**
     * 根据链类型获取任务列表
     */
    @GetMapping("/chain/{chainType}")
    public AjaxResult getByChainType(@PathVariable("chainType") String chainType)
    {
        List<MonitorTask> list = monitorTaskService.selectMonitorTaskByChainType(chainType);
        return success(list);
    }

    /**
     * 获取监控任务详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(monitorTaskService.selectMonitorTaskById(id));
    }

    /**
     * 新增智能监控任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:add')")
    @Log(title = "智能监控任务", businessType = BusinessType.INSERT)
    @PostMapping("/smart")
    public AjaxResult addSmart(@RequestBody MonitorTask monitorTask)
    {
        return toAjax(monitorTaskService.insertSmartTask(monitorTask));
    }

    /**
     * 新增批量监控任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:add')")
    @Log(title = "批量监控任务", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult addBatch(@RequestBody Map<String, Object> params)
    {
        // 解析任务信息
        MonitorTask monitorTask = new MonitorTask();
        monitorTask.setTaskName((String) params.get("taskName"));
        monitorTask.setTaskType("batch");
        monitorTask.setChainType((String) params.get("chainType"));
        monitorTask.setDescription((String) params.get("description"));
        
        // 解析 configIds
        Object configIdsObj = params.get("configIds");
        if (configIdsObj instanceof List) {
            List<Long> configIds = new ArrayList<>();
            for (Object id : (List<?>) configIdsObj) {
                if (id instanceof Number) {
                    configIds.add(((Number) id).longValue());
                }
            }
            monitorTask.setConfigIds(configIds);
        }
        
        // 解析目标地址列表
        List<String> targetAddresses = (List<String>) params.get("targetList");
        
        // 解析状态
        Object statusObj = params.get("status");
        if (statusObj instanceof Number) {
            monitorTask.setStatus(((Number) statusObj).intValue());
        }
        
        return toAjax(monitorTaskService.insertBatchTask(monitorTask, targetAddresses));
    }

    /**
     * 新增区块监控任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:add')")
    @Log(title = "区块监控任务", businessType = BusinessType.INSERT)
    @PostMapping("/block")
    public AjaxResult addBlock(@RequestBody MonitorTask monitorTask)
    {
        return toAjax(monitorTaskService.insertBlockTask(monitorTask));
    }

    /**
     * 修改监控任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:edit')")
    @Log(title = "监控任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorTask monitorTask)
    {
        return toAjax(monitorTaskService.updateMonitorTask(monitorTask));
    }

    /**
     * 删除监控任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:remove')")
    @Log(title = "监控任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(monitorTaskService.deleteMonitorTaskByIds(ids));
    }

    /**
     * 启动任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:edit')")
    @Log(title = "启动任务", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/start")
    public AjaxResult start(@PathVariable("id") Long id)
    {
        return toAjax(monitorTaskService.startTask(id));
    }

    /**
     * 停止任务
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:task:edit')")
    @Log(title = "停止任务", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/stop")
    public AjaxResult stop(@PathVariable("id") Long id)
    {
        return toAjax(monitorTaskService.stopTask(id));
    }

    /**
     * 统计任务数量（用于首页卡片）
     */
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        // 统计总数和状态
        int total = monitorTaskService.countMonitorTask(null, null, null);
        int running = monitorTaskService.countMonitorTask(null, null, 1);
        int paused = monitorTaskService.countMonitorTask(null, null, 0);
        int error = monitorTaskService.countMonitorTask(null, null, 2);
        
        // 按任务类型统计
        int smart = monitorTaskService.countMonitorTask("smart", null, null);
        int batch = monitorTaskService.countMonitorTask("batch", null, null);
        int block = monitorTaskService.countMonitorTask("block", null, null);
        
        // 组装返回数据
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("running", running);
        stats.put("paused", paused);
        stats.put("error", error);
        stats.put("smart", smart);
        stats.put("batch", batch);
        stats.put("block", block);
        stats.put("lastUpdate", new java.util.Date());
        
        return success(stats);
    }
}

