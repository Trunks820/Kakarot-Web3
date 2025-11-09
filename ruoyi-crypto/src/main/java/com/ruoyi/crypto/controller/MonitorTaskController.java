package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.MonitorTask;
import com.ruoyi.crypto.service.IMonitorTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @Autowired
    private IMonitorTaskService monitorTaskService;

    /**
     * 查询监控任务列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MonitorTask monitorTask)
    {
        startPage();
        List<MonitorTask> list = monitorTaskService.selectMonitorTaskList(monitorTask);
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
        // 解析任务信息和目标地址列表
        MonitorTask monitorTask = new MonitorTask();
        // TODO: 从params中提取任务信息
        List<String> targetAddresses = (List<String>) params.get("targetAddresses");
        
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
    public AjaxResult getStats(@RequestParam(required = false) String taskType,
                                @RequestParam(required = false) String chainType,
                                @RequestParam(required = false) Integer status)
    {
        int count = monitorTaskService.countMonitorTask(taskType, chainType, status);
        return success(count);
    }
}

