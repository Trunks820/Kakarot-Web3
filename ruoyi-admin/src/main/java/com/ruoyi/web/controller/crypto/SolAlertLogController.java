package com.ruoyi.web.controller.crypto;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.crypto.domain.SolAlertLog;
import com.ruoyi.crypto.service.ISolAlertLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * SOL告警日志Controller
 * 
 * @author ruoyi
 * @date 2025-11-03
 */
@RestController
@RequestMapping("/crypto/solAlert")
public class SolAlertLogController extends BaseController
{
    @Autowired
    private ISolAlertLogService solAlertLogService;

    /**
     * 查询最近的SOL告警记录（用于首页通知中心显示）
     */
    @GetMapping("/recent")
    public AjaxResult recent(@RequestParam(defaultValue = "10") Integer limit)
    {
        List<SolAlertLog> list = solAlertLogService.selectRecentSolAlerts(limit);
        return success(list);
    }

    /**
     * 查询SOL告警日志列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:solAlert:list')")
    @GetMapping("/list")
    public TableDataInfo list(SolAlertLog solAlertLog)
    {
        startPage();
        List<SolAlertLog> list = solAlertLogService.selectSolAlertLogList(solAlertLog);
        return getDataTable(list);
    }

    /**
     * 导出SOL告警日志列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:solAlert:export')")
    @Log(title = "SOL告警日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SolAlertLog solAlertLog)
    {
        List<SolAlertLog> list = solAlertLogService.selectSolAlertLogList(solAlertLog);
        ExcelUtil<SolAlertLog> util = new ExcelUtil<SolAlertLog>(SolAlertLog.class);
        util.exportExcel(response, list, "SOL告警日志数据");
    }

    /**
     * 获取SOL告警日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:solAlert:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(solAlertLogService.selectSolAlertLogById(id));
    }

    /**
     * 新增SOL告警日志
     */
    @PreAuthorize("@ss.hasPermi('crypto:solAlert:add')")
    @Log(title = "SOL告警日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SolAlertLog solAlertLog)
    {
        return toAjax(solAlertLogService.insertSolAlertLog(solAlertLog));
    }

    /**
     * 修改SOL告警日志
     */
    @PreAuthorize("@ss.hasPermi('crypto:solAlert:edit')")
    @Log(title = "SOL告警日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SolAlertLog solAlertLog)
    {
        return toAjax(solAlertLogService.updateSolAlertLog(solAlertLog));
    }

    /**
     * 删除SOL告警日志
     */
    @PreAuthorize("@ss.hasPermi('crypto:solAlert:remove')")
    @Log(title = "SOL告警日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(solAlertLogService.deleteSolAlertLogByIds(ids));
    }
}

