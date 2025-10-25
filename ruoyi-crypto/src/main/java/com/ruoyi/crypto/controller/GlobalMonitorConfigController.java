package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.crypto.domain.GlobalMonitorConfig;
import com.ruoyi.crypto.service.IGlobalMonitorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 链级全局监控配置Controller
 * 
 * @author ruoyi
 * @date 2025-10-21
 */
@RestController
@RequestMapping("/crypto/globalMonitor")
public class GlobalMonitorConfigController extends BaseController
{
    @Autowired
    private IGlobalMonitorConfigService globalMonitorConfigService;

    /**
     * 查询链级全局监控配置列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:globalMonitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(GlobalMonitorConfig globalMonitorConfig)
    {
        startPage();
        List<GlobalMonitorConfig> list = globalMonitorConfigService.selectGlobalMonitorConfigList(globalMonitorConfig);
        return getDataTable(list);
    }

    /**
     * 导出链级全局监控配置列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:globalMonitor:export')")
    @Log(title = "链级全局监控配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, GlobalMonitorConfig globalMonitorConfig)
    {
        List<GlobalMonitorConfig> list = globalMonitorConfigService.selectGlobalMonitorConfigList(globalMonitorConfig);
        ExcelUtil<GlobalMonitorConfig> util = new ExcelUtil<GlobalMonitorConfig>(GlobalMonitorConfig.class);
        util.exportExcel(response, list, "链级全局监控配置数据");
    }

    /**
     * 获取链级全局监控配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:globalMonitor:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(globalMonitorConfigService.selectGlobalMonitorConfigById(id));
    }

    /**
     * 根据链类型和市场类型获取配置
     */
    @GetMapping(value = "/chain/{chainType}")
    public AjaxResult getByChainType(@PathVariable("chainType") String chainType, 
                                      @RequestParam(value = "marketType", defaultValue = "external") String marketType)
    {
        return success(globalMonitorConfigService.selectGlobalMonitorConfigByChainTypeAndMarket(chainType, marketType));
    }

    /**
     * 新增链级全局监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:globalMonitor:add')")
    @Log(title = "链级全局监控配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody GlobalMonitorConfig globalMonitorConfig)
    {
        return toAjax(globalMonitorConfigService.insertGlobalMonitorConfig(globalMonitorConfig));
    }

    /**
     * 修改链级全局监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:globalMonitor:edit')")
    @Log(title = "链级全局监控配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody GlobalMonitorConfig globalMonitorConfig)
    {
        return toAjax(globalMonitorConfigService.updateGlobalMonitorConfig(globalMonitorConfig));
    }

    /**
     * 保存或更新链级全局监控配置
     */
    @Log(title = "链级全局监控配置", businessType = BusinessType.UPDATE)
    @PostMapping("/saveOrUpdate")
    public AjaxResult saveOrUpdate(@RequestBody GlobalMonitorConfig globalMonitorConfig)
    {
        // 设置默认值
        if (globalMonitorConfig.getStatus() == null)
        {
            globalMonitorConfig.setStatus("1");
        }
        if (globalMonitorConfig.getTriggerLogic() == null)
        {
            globalMonitorConfig.setTriggerLogic("any");
        }
        if (globalMonitorConfig.getPriority() == null)
        {
            globalMonitorConfig.setPriority(0);
        }
        
        return toAjax(globalMonitorConfigService.saveOrUpdateGlobalMonitorConfig(globalMonitorConfig));
    }

    /**
     * 切换配置状态
     */
    @Log(title = "链级全局监控配置状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{id}/{status}")
    public AjaxResult changeStatus(@PathVariable("id") Long id, @PathVariable("status") String status)
    {
        return toAjax(globalMonitorConfigService.changeStatus(id, status));
    }

    /**
     * 删除链级全局监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:globalMonitor:remove')")
    @Log(title = "链级全局监控配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(globalMonitorConfigService.deleteGlobalMonitorConfigByIds(ids));
    }

    /**
     * 获取配置操作日志
     */
    @GetMapping("/logs")
    public AjaxResult getLogs(@RequestParam(defaultValue = "20") int limit)
    {
        GlobalMonitorConfig query = new GlobalMonitorConfig();
        List<GlobalMonitorConfig> list = globalMonitorConfigService.selectGlobalMonitorConfigList(query);
        
        // 按更新时间倒序，取前N条
        list.sort((a, b) -> {
            if (a.getUpdateTime() == null) return 1;
            if (b.getUpdateTime() == null) return -1;
            return b.getUpdateTime().compareTo(a.getUpdateTime());
        });
        
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        
        return success(list);
    }
}

