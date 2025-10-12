package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.TokenMonitorConfig;
import com.ruoyi.crypto.service.ITokenMonitorConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Token监控配置Controller
 *
 * @author ruoyi
 */
@Api(tags = "Token监控配置")
@RestController
@RequestMapping("/crypto/tokenMonitor")
public class TokenMonitorConfigController extends BaseController {

    @Resource
    private ITokenMonitorConfigService monitorConfigService;

    /**
     * 查询Token监控配置列表
     */
    @ApiOperation("查询监控配置列表")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(TokenMonitorConfig config) {
        startPage();
        List<TokenMonitorConfig> list = monitorConfigService.selectMonitorConfigList(config);
        return getDataTable(list);
    }

    /**
     * 获取Token监控配置详细信息
     */
    @ApiOperation("获取监控配置详情")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(
            @ApiParam(value = "监控配置ID", required = true)
            @PathVariable("id") Long id) {
        return success(monitorConfigService.selectMonitorConfigById(id));
    }

    /**
     * 根据CA查询监控配置列表
     */
    @ApiOperation("根据CA查询监控配置")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/ca/{ca}")
    public AjaxResult getByCa(
            @ApiParam(value = "合约地址", required = true)
            @PathVariable("ca") String ca) {
        List<TokenMonitorConfig> list = monitorConfigService.selectMonitorConfigByCa(ca);
        return success(list);
    }

    /**
     * 新增Token监控配置
     */
    @ApiOperation("新增监控配置")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:add')")
    @Log(title = "Token监控配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TokenMonitorConfig config) {
        // 参数校验
        if (config.getCa() == null || config.getCa().isEmpty()) {
            return error("合约地址不能为空");
        }
        if (config.getAlertMode() == null || config.getAlertMode().isEmpty()) {
            return error("监控模式不能为空");
        }
        if (config.getNotifyMethods() == null || config.getNotifyMethods().isEmpty()) {
            return error("通知方式不能为空");
        }

        // 根据监控模式验证必填字段
        switch (config.getAlertMode()) {
            case "timer":
                if (config.getTimerInterval() == null || config.getTimerInterval() < 1 || config.getTimerInterval() > 1440) {
                    return error("定时间隔必须在1-1440分钟之间");
                }
                break;
            case "condition":
                if (config.getConditionType() == null || config.getConditionType().isEmpty()) {
                    return error("条件类型不能为空");
                }
                if (config.getConditionValue() == null) {
                    return error("条件阈值不能为空");
                }
                break;
            case "event":
                if (config.getEventType() == null || config.getEventType().isEmpty()) {
                    return error("事件类型不能为空");
                }
                break;
            default:
                return error("不支持的监控模式");
        }

        return toAjax(monitorConfigService.insertMonitorConfig(config));
    }

    /**
     * 修改Token监控配置
     */
    @ApiOperation("修改监控配置")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "Token监控配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TokenMonitorConfig config) {
        if (config.getId() == null) {
            return error("配置ID不能为空");
        }
        return toAjax(monitorConfigService.updateMonitorConfig(config));
    }

    /**
     * 删除Token监控配置
     */
    @ApiOperation("删除监控配置")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:remove')")
    @Log(title = "Token监控配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(
            @ApiParam(value = "监控配置ID数组", required = true)
            @PathVariable Long[] ids) {
        return toAjax(monitorConfigService.deleteMonitorConfigByIds(ids));
    }

    /**
     * 启用监控
     */
    @ApiOperation("启用监控")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "Token监控配置", businessType = BusinessType.UPDATE)
    @PutMapping("/enable/{id}")
    public AjaxResult enable(
            @ApiParam(value = "监控配置ID", required = true)
            @PathVariable Long id) {
        return toAjax(monitorConfigService.enableMonitor(id));
    }

    /**
     * 停用监控
     */
    @ApiOperation("停用监控")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "Token监控配置", businessType = BusinessType.UPDATE)
    @PutMapping("/disable/{id}")
    public AjaxResult disable(
            @ApiParam(value = "监控配置ID", required = true)
            @PathVariable Long id) {
        return toAjax(monitorConfigService.disableMonitor(id));
    }

    /**
     * 批量启用监控
     */
    @ApiOperation("批量启用监控")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "Token监控配置", businessType = BusinessType.UPDATE)
    @PutMapping("/batchEnable")
    public AjaxResult batchEnable(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return error("请选择要启用的监控配置");
        }
        return toAjax(monitorConfigService.batchEnableMonitor(ids));
    }

    /**
     * 批量停用监控
     */
    @ApiOperation("批量停用监控")
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "Token监控配置", businessType = BusinessType.UPDATE)
    @PutMapping("/batchDisable")
    public AjaxResult batchDisable(@RequestBody Long[] ids) {
        if (ids == null || ids.length == 0) {
            return error("请选择要停用的监控配置");
        }
        return toAjax(monitorConfigService.batchDisableMonitor(ids));
    }
}

