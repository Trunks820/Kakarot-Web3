package com.ruoyi.crypto.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import com.ruoyi.crypto.service.CryptoMonitorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/crypto/monitor")
@RestController
public class CryptoMonitorController extends BaseController {
    @Resource
    private CryptoMonitorService cryptoMonitorService;

    /**
     * 查询监控列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/list")
    public TableDataInfo list(CryptoMonitorConfig config)
    {
        startPage();
        List<CryptoMonitorConfig> list = cryptoMonitorService.select(config);
        return getDataTable(list);
    }

    /**
     * 查询单个监控
     */
    @PreAuthorize("@ss.hasPermi('crypto:monitor:list')")
    @GetMapping("/getConfig")
    public AjaxResult getConfig(CryptoMonitorConfig config)
    {
        startPage();
        CryptoMonitorConfig cryptoMonitorConfig = cryptoMonitorService.getConfig(config);
        return success(cryptoMonitorConfig);
    }


}
