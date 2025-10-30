package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.domain.QuickMonitorTemplate;
import com.ruoyi.crypto.service.IQuickMonitorTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Token智能监控配置模板Controller
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
@RestController
@RequestMapping("/crypto/quickMonitor")
public class QuickMonitorTemplateController extends BaseController
{
    @Autowired
    private IQuickMonitorTemplateService quickMonitorTemplateService;

    /**
     * 查询配置模板列表
     */
    @GetMapping("/list")
    public AjaxResult list(QuickMonitorTemplate quickMonitorTemplate)
    {
        List<QuickMonitorTemplate> list = quickMonitorTemplateService.selectQuickMonitorTemplateList(quickMonitorTemplate);
        return success(list);
    }

    /**
     * 根据链类型获取配置列表
     */
    @GetMapping("/chain/{chainType}")
    public AjaxResult getByChainType(@PathVariable("chainType") String chainType)
    {
        List<QuickMonitorTemplate> list = quickMonitorTemplateService.selectQuickMonitorTemplateByChainType(chainType);
        return success(list);
    }

    /**
     * 获取配置模板详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(quickMonitorTemplateService.selectQuickMonitorTemplateById(id));
    }

    /**
     * 新增配置模板
     */
    @Log(title = "Token智能监控配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QuickMonitorTemplate quickMonitorTemplate)
    {
        return toAjax(quickMonitorTemplateService.insertQuickMonitorTemplate(quickMonitorTemplate));
    }

    /**
     * 修改配置模板
     */
    @Log(title = "Token智能监控配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QuickMonitorTemplate quickMonitorTemplate)
    {
        return toAjax(quickMonitorTemplateService.updateQuickMonitorTemplate(quickMonitorTemplate));
    }

    /**
     * 删除配置模板
     */
    @Log(title = "Token智能监控配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(quickMonitorTemplateService.deleteQuickMonitorTemplateByIds(ids));
    }

    /**
     * 批量保存配置模板
     */
    @Log(title = "Token智能监控配置", businessType = BusinessType.UPDATE)
    @PostMapping("/batchSave")
    public AjaxResult batchSave(@RequestBody BatchSaveRequest request)
    {
        return toAjax(quickMonitorTemplateService.batchSaveQuickMonitorTemplate(
                request.getChainType(), 
                request.getTemplates()
        ));
    }

    /**
     * 获取配置统计（包含每个配置段匹配的Token数量）
     */
    @GetMapping("/stats/{chainType}")
    public AjaxResult getConfigStats(@PathVariable("chainType") String chainType)
    {
        return success(quickMonitorTemplateService.getConfigStatsWithTokenCount(chainType));
    }

    /**
     * 预测配置的Token匹配数量（用于编辑时实时预测）
     * 注意：此接口不使用防重复提交注解，由前端防抖控制请求频率
     */
    @PostMapping("/predict")
    public AjaxResult predictTokenCounts(@RequestBody PredictRequest request)
    {
        return success(quickMonitorTemplateService.predictTokenCounts(request.getMarketCapList()));
    }

    /**
     * 批量保存请求对象
     */
    public static class BatchSaveRequest {
        private String chainType;
        private List<QuickMonitorTemplate> templates;

        public String getChainType() {
            return chainType;
        }

        public void setChainType(String chainType) {
            this.chainType = chainType;
        }

        public List<QuickMonitorTemplate> getTemplates() {
            return templates;
        }

        public void setTemplates(List<QuickMonitorTemplate> templates) {
            this.templates = templates;
        }
    }

    /**
     * 预测请求对象
     */
    public static class PredictRequest {
        private List<Long> marketCapList;

        public List<Long> getMarketCapList() {
            return marketCapList;
        }

        public void setMarketCapList(List<Long> marketCapList) {
            this.marketCapList = marketCapList;
        }
    }
}

