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
import com.ruoyi.crypto.domain.MonitorAlertLog;
import com.ruoyi.crypto.service.IMonitorAlertLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 监控告警日志Controller
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@RestController
@RequestMapping("/crypto/monitor-v2/alert")
public class MonitorAlertLogController extends BaseController
{
    @Autowired
    private IMonitorAlertLogService monitorAlertLogService;

    /**
     * 查询监控告警日志列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MonitorAlertLog monitorAlertLog)
    {
        startPage();
        List<MonitorAlertLog> list = monitorAlertLogService.selectMonitorAlertLogList(monitorAlertLog);
        return getDataTable(list);
    }

    /**
     * 导出监控告警日志列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:export')")
    @Log(title = "监控告警日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MonitorAlertLog monitorAlertLog)
    {
        List<MonitorAlertLog> list = monitorAlertLogService.selectMonitorAlertLogList(monitorAlertLog);
        ExcelUtil<MonitorAlertLog> util = new ExcelUtil<MonitorAlertLog>(MonitorAlertLog.class);
        util.exportExcel(response, list, "监控告警日志数据");
    }

    /**
     * 获取监控告警日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(monitorAlertLogService.selectMonitorAlertLogById(id));
    }

    /**
     * 新增监控告警日志
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:add')")
    @Log(title = "监控告警日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MonitorAlertLog monitorAlertLog)
    {
        return toAjax(monitorAlertLogService.insertMonitorAlertLog(monitorAlertLog));
    }

    /**
     * 修改监控告警日志
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:edit')")
    @Log(title = "监控告警日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorAlertLog monitorAlertLog)
    {
        return toAjax(monitorAlertLogService.updateMonitorAlertLog(monitorAlertLog));
    }

    /**
     * 删除监控告警日志
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:remove')")
    @Log(title = "监控告警日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(monitorAlertLogService.deleteMonitorAlertLogByIds(ids));
    }

    /**
     * 标记告警为已读
     */
    @PutMapping("/{id}/read")
    public AjaxResult markAsRead(@PathVariable Long id)
    {
        return toAjax(monitorAlertLogService.markAsRead(id));
    }

    /**
     * 标记告警为已处理
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:handle')")
    @Log(title = "处理告警", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/handle")
    public AjaxResult markAsHandled(@PathVariable Long id, @RequestBody(required = false) Map<String, String> params)
    {
        String handleRemark = params != null ? params.get("handleRemark") : null;
        String handleBy = SecurityUtils.getUsername();
        return toAjax(monitorAlertLogService.markAsHandled(id, handleBy, handleRemark));
    }

    /**
     * 批量标记告警为已处理
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:alert:handle')")
    @Log(title = "批量处理告警", businessType = BusinessType.UPDATE)
    @PutMapping("/batch-handle")
    public AjaxResult batchMarkAsHandled(@RequestBody Long[] ids)
    {
        String handleBy = SecurityUtils.getUsername();
        return toAjax(monitorAlertLogService.batchMarkAsHandled(ids, handleBy));
    }

    /**
     * 获取告警统计
     */
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        Map<String, Object> stats = monitorAlertLogService.selectAlertStats();
        return success(stats);
    }
}

