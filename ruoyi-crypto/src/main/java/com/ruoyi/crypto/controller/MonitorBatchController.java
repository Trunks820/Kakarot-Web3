package com.ruoyi.crypto.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.MonitorBatch;
import com.ruoyi.crypto.domain.MonitorBatchItem;
import com.ruoyi.crypto.service.IMonitorBatchService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 批次管理Controller
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@RestController
@RequestMapping("/crypto/monitor-v2/batch")
public class MonitorBatchController extends BaseController
{
    @Autowired
    private IMonitorBatchService monitorBatchService;

    /**
     * 获取批次统计信息
     */
    @GetMapping("/stats")
    public AjaxResult stats()
    {
        // 查询批次统计
        Map<String, Object> batchStats = monitorBatchService.getBatchStats();
        
        // 返回统计数据
        return success(batchStats);
    }

    /**
     * 查询批次列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:list')")
    @GetMapping("/list")
    public TableDataInfo list(MonitorBatch monitorBatch)
    {
        startPage();
        List<MonitorBatch> list = monitorBatchService.selectMonitorBatchList(monitorBatch);
        return getDataTable(list);
    }

    /**
     * 导出批次列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:export')")
    @Log(title = "批次管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MonitorBatch monitorBatch)
    {
        List<MonitorBatch> list = monitorBatchService.selectMonitorBatchList(monitorBatch);
        ExcelUtil<MonitorBatch> util = new ExcelUtil<MonitorBatch>(MonitorBatch.class);
        util.exportExcel(response, list, "批次数据");
    }

    /**
     * 获取批次详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(monitorBatchService.selectMonitorBatchById(id));
    }

    /**
     * 获取批次项列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:query')")
    @GetMapping(value = "/{batchId}/items")
    public TableDataInfo listItems(@PathVariable("batchId") Long batchId)
    {
        startPage();
        List<MonitorBatchItem> list = monitorBatchService.selectBatchItems(batchId);
        return getDataTable(list);
    }

    /**
     * 新增批次
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:add')")
    @Log(title = "批次管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MonitorBatch monitorBatch)
    {
        return toAjax(monitorBatchService.insertMonitorBatch(monitorBatch));
    }

    /**
     * 修改批次
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:edit')")
    @Log(title = "批次管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorBatch monitorBatch)
    {
        return toAjax(monitorBatchService.updateMonitorBatch(monitorBatch));
    }

    /**
     * 删除批次
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:remove')")
    @Log(title = "批次管理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(monitorBatchService.deleteMonitorBatchByIds(ids));
    }

    /**
     * 重启批次
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:batch:restart')")
    @Log(title = "批次管理", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/restart")
    public AjaxResult restart(@PathVariable("id") Long id)
    {
        return toAjax(monitorBatchService.restartBatch(id));
    }

    /**
     * 获取批次心跳状态
     */
    @GetMapping("/{id}/heartbeat")
    public AjaxResult heartbeat(@PathVariable("id") Long id)
    {
        Map<String, Object> heartbeat = monitorBatchService.getBatchHeartbeat(id);
        return success(heartbeat);
    }

    /**
     * 更新批次心跳
     */
    @PostMapping("/{id}/heartbeat")
    public AjaxResult updateHeartbeat(@PathVariable("id") Long id, @RequestBody Map<String, Object> data)
    {
        return toAjax(monitorBatchService.updateBatchHeartbeat(id, data));
    }
}

