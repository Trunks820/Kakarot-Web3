package com.ruoyi.crypto.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.TokenMonitorAlertLog;
import com.ruoyi.crypto.service.ITokenMonitorAlertLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 历史播报Controller
 * 
 * @author ruoyi
 * @date 2025-01-24
 */
@RestController
@RequestMapping("/crypto/block")
public class BlockMonitorController extends BaseController
{
    @Autowired
    private ITokenMonitorAlertLogService tokenMonitorAlertLogService;

    /**
     * 查询历史播报列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:block:list')")
    @GetMapping("/list")
    public TableDataInfo list(TokenMonitorAlertLog tokenMonitorAlertLog)
    {
        startPage();
        List<TokenMonitorAlertLog> list = tokenMonitorAlertLogService.selectTokenMonitorAlertLogList(tokenMonitorAlertLog);
        return getDataTable(list);
    }

    /**
     * 导出历史播报列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:block:export')")
    @Log(title = "历史播报", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TokenMonitorAlertLog tokenMonitorAlertLog)
    {
        List<TokenMonitorAlertLog> list = tokenMonitorAlertLogService.selectTokenMonitorAlertLogList(tokenMonitorAlertLog);
        ExcelUtil<TokenMonitorAlertLog> util = new ExcelUtil<TokenMonitorAlertLog>(TokenMonitorAlertLog.class);
        util.exportExcel(response, list, "历史播报数据");
    }

    /**
     * 获取历史播报详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:block:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(tokenMonitorAlertLogService.selectTokenMonitorAlertLogById(id));
    }

    /**
     * 删除历史播报
     */
    @PreAuthorize("@ss.hasPermi('crypto:block:remove')")
    @Log(title = "历史播报", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(tokenMonitorAlertLogService.deleteTokenMonitorAlertLogByIds(ids));
    }
}

