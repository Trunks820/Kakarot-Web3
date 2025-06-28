package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import com.ruoyi.crypto.service.CryptoMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 监控配置Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/crypto/monitor")
public class CryptoMonitorController extends BaseController {
    
    @Autowired
    private CryptoMonitorService cryptoMonitorService;

    /**
     * 查询监控配置列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(CryptoMonitorConfig cryptoMonitorConfig) {
        startPage();
        List<CryptoMonitorConfig> list = cryptoMonitorService.selectCryptoMonitorConfigList(cryptoMonitorConfig);
        return getDataTable(list);
    }

    /**
     * 获取监控配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(cryptoMonitorService.selectCryptoMonitorConfigById(id));
    }

    /**
     * 新增监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:add')")
    @Log(title = "监控配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CryptoMonitorConfig cryptoMonitorConfig) {
        cryptoMonitorConfig.setCreateBy(getUsername());
        return toAjax(cryptoMonitorService.insertCryptoMonitorConfig(cryptoMonitorConfig));
    }

    /**
     * 保存监控配置 (专门用于前端监控弹窗)
     */
    @Log(title = "保存监控配置", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody CryptoMonitorConfig cryptoMonitorConfig) {
        try {
            String currentUser = getUsername();
            cryptoMonitorConfig.setCreateBy(currentUser);
            // 检查是否已经存在相同的监控配置
            if (cryptoMonitorService.checkTokenMonitored(cryptoMonitorConfig.getCoinAddress(), currentUser)) {
                return error("该代币已存在监控配置，请勿重复添加");
            }

            int result = cryptoMonitorService.insertCryptoMonitorConfig(cryptoMonitorConfig);
            if (result > 0) {
                return success("监控配置已保存成功");
            } else {
                return error("保存监控配置失败");
            }
        } catch (Exception e) {
            logger.error("保存监控配置异常", e);
            return error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 修改监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "监控配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CryptoMonitorConfig cryptoMonitorConfig) {
        cryptoMonitorConfig.setUpdateBy(getUsername());
        return toAjax(cryptoMonitorService.updateCryptoMonitorConfig(cryptoMonitorConfig));
    }

    /**
     * 删除监控配置
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:remove')")
    @Log(title = "监控配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(cryptoMonitorService.deleteCryptoMonitorConfigByIds(ids));
    }

    /**
     * 改变监控状态
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:edit')")
    @Log(title = "监控配置状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody CryptoMonitorConfig cryptoMonitorConfig) {
        return toAjax(cryptoMonitorService.changeStatus(cryptoMonitorConfig));
    }

    /**
     * 检查代币是否已被监控
     */
    @GetMapping("/checkMonitored")
    public AjaxResult checkMonitored(@RequestParam String coinAddress) {
        try {
            String currentUser = SecurityUtils.getUsername();
            boolean isMonitored = cryptoMonitorService.checkTokenMonitored(coinAddress, currentUser);
            
            Map<String, Object> data = new HashMap<>();
            data.put("monitored", isMonitored);
            data.put("coinAddress", coinAddress);
            data.put("createBy", currentUser);
            
            return success(data);
        } catch (Exception e) {
            logger.error("检查监控状态异常", e);
            return error("检查监控状态失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的监控配置列表
     */
    @GetMapping("/myMonitors")
    public AjaxResult getMyMonitors() {
        try {
            String currentUser = SecurityUtils.getUsername();
            CryptoMonitorConfig queryConfig = new CryptoMonitorConfig();
            queryConfig.setCreateBy(currentUser);
            List<CryptoMonitorConfig> monitors = cryptoMonitorService.selectCryptoMonitorConfigList(queryConfig);
            return success(monitors);
        } catch (Exception e) {
            logger.error("获取用户监控配置异常", e);
            return error("获取监控配置失败：" + e.getMessage());
        }
    }

    /**
     * 获取激活的监控配置（用于定时任务等）
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/active")
    public AjaxResult getActiveMonitors() {
        List<CryptoMonitorConfig> activeMonitors = cryptoMonitorService.selectActiveCryptoMonitorConfig();
        return success(activeMonitors);
    }

    // === 兼容旧版本的接口 ===

    /**
     * 查询单个监控 (兼容旧版本)
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/getConfig")
    @Deprecated
    public AjaxResult getConfig(CryptoMonitorConfig config) {
        CryptoMonitorConfig cryptoMonitorConfig = cryptoMonitorService.getConfig(config);
        return success(cryptoMonitorConfig);
    }

}
