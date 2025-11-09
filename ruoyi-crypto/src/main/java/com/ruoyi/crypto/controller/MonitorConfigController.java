package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.MonitorConfig;
import com.ruoyi.crypto.mapper.MonitorConfigMapper;
import com.ruoyi.crypto.service.IMonitorConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控配置Controller
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
@RestController
@RequestMapping("/crypto/monitor-v2/config")
public class MonitorConfigController extends BaseController
{
    @Autowired
    private IMonitorConfigService monitorConfigService;
    
    @Autowired
    private MonitorConfigMapper monitorConfigMapper;

    /**
     * 查询监控配置列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MonitorConfig monitorConfig)
    {
        startPage();
        List<MonitorConfig> list = monitorConfigService.selectMonitorConfigList(monitorConfig);
        return getDataTable(list);
    }

    /**
     * 根据链类型获取配置列表（用于任务创建时选择配置）
     */
    @GetMapping("/chain/{chainType}")
    public AjaxResult getByChainType(@PathVariable("chainType") String chainType)
    {
        List<MonitorConfig> list = monitorConfigService.selectMonitorConfigByChainType(chainType);
        return success(list);
    }

    /**
     * 获取监控配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:config:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(monitorConfigService.selectMonitorConfigById(id));
    }

    /**
     * 新增监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:config:add')")
    @Log(title = "监控配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MonitorConfig monitorConfig)
    {
        return toAjax(monitorConfigService.insertMonitorConfig(monitorConfig));
    }

    /**
     * 修改监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:config:edit')")
    @Log(title = "监控配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MonitorConfig monitorConfig)
    {
        return toAjax(monitorConfigService.updateMonitorConfig(monitorConfig));
    }

    /**
     * 删除监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:config:remove')")
    @Log(title = "监控配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(monitorConfigService.deleteMonitorConfigByIds(ids));
    }

    /**
     * 修改配置状态
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor-v2:config:edit')")
    @Log(title = "监控配置状态", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status/{status}")
    public AjaxResult changeStatus(@PathVariable("id") Long id, @PathVariable("status") Integer status)
    {
        return toAjax(monitorConfigService.changeStatus(id, status));
    }

    /**
     * 统计配置数量（用于首页卡片）
     */
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        // 统计各种状态的配置数量
        int total = monitorConfigService.countMonitorConfig(null, null);
        int enabled = monitorConfigService.countMonitorConfig(null, 1);
        int disabled = monitorConfigService.countMonitorConfig(null, 0);
        
        // 按配置类别统计
        MonitorConfig presetCondition = new MonitorConfig();
        presetCondition.setConfigType("preset");
        int preset = monitorConfigMapper.selectMonitorConfigList(presetCondition).size();
        
        MonitorConfig customCondition = new MonitorConfig();
        customCondition.setConfigType("custom");
        int custom = monitorConfigMapper.selectMonitorConfigList(customCondition).size();
        
        // 组装返回数据
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("enabled", enabled);
        stats.put("disabled", disabled);
        stats.put("preset", preset);
        stats.put("custom", custom);
        stats.put("lastUpdate", new java.util.Date());
        
        return success(stats);
    }
}

