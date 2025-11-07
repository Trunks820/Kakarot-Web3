package com.ruoyi.crypto.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.crypto.service.IBatchMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 批量监控Controller
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
@RestController
@RequestMapping("/crypto/batch")
public class BatchMonitorController extends BaseController
{
    @Autowired
    private IBatchMonitorService batchMonitorService;

    /**
     * 智能批量添加监控
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:add')")
    @Log(title = "批量监控", businessType = BusinessType.INSERT)
    @PostMapping("/smartAdd")
    public AjaxResult smartAdd(@RequestBody Map<String, Object> request)
    {
        try {
            @SuppressWarnings("unchecked")
            List<String> addresses = (List<String>) request.get("addresses");
            @SuppressWarnings("unchecked")
            Map<String, Object> config = (Map<String, Object>) request.get("config");
            
            if (addresses == null || addresses.isEmpty()) {
                return AjaxResult.error("地址列表不能为空");
            }
            
            if (addresses.size() > 99) {
                return AjaxResult.error("单次最多支持99个地址");
            }
            
            if (config == null) {
                return AjaxResult.error("监控配置不能为空");
            }
            
            Map<String, Object> result = batchMonitorService.smartBatchAdd(addresses, config);
            
            return AjaxResult.success(result);
        } catch (Exception e) {
            logger.error("批量添加监控失败", e);
            return AjaxResult.error("批量添加监控失败：" + e.getMessage());
        }
    }

    /**
     * 查询批量监控列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) String sourceType,
                              @RequestParam(required = false) String chainType)
    {
        startPage();
        List<Map<String, Object>> list = batchMonitorService.getBatchMonitorList(sourceType, chainType);
        return getDataTable(list);
    }

    /**
     * 查询指定批次的Token列表
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:query')")
    @GetMapping("/tokens/{batchId}")
    public AjaxResult getTokensInBatch(@PathVariable Integer batchId,
                                       @RequestParam(required = false) String sourceType,
                                       @RequestParam(required = false) String chainType)
    {
        List<Map<String, Object>> list = batchMonitorService.getTokensInBatch(batchId, sourceType, chainType);
        return AjaxResult.success(list);
    }

    /**
     * 删除批次
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:remove')")
    @Log(title = "批量监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/{batchId}")
    public AjaxResult remove(@PathVariable Integer batchId,
                            @RequestParam(required = false, defaultValue = "batch") String sourceType,
                            @RequestParam(required = false) String chainType)
    {
        int count = batchMonitorService.deleteBatch(batchId, sourceType, chainType);
        return toAjax(count);
    }

    /**
     * 启用/停用批次
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:edit')")
    @Log(title = "批量监控", businessType = BusinessType.UPDATE)
    @PutMapping("/status/{batchId}")
    public AjaxResult changeStatus(@PathVariable Integer batchId,
                                   @RequestParam Integer isActive,
                                   @RequestParam(required = false, defaultValue = "batch") String sourceType,
                                   @RequestParam(required = false) String chainType)
    {
        int count = batchMonitorService.toggleBatchStatus(batchId, sourceType, chainType, isActive);
        return toAjax(count);
    }

    /**
     * 查询可用的批次ID
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:query')")
    @GetMapping("/availableBatchId")
    public AjaxResult getAvailableBatchId(@RequestParam String chainType)
    {
        // 这个方法可以在Service中实现，这里简化处理
        return AjaxResult.success();
    }

    /**
     * 获取批量监控统计信息
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:query')")
    @GetMapping("/stats")
    public AjaxResult getStats()
    {
        // SOL链统计
        List<Map<String, Object>> solList = batchMonitorService.getBatchMonitorList("batch", "sol");
        
        // BSC链统计
        List<Map<String, Object>> bscList = batchMonitorService.getBatchMonitorList("batch", "bsc");
        
        // 计算总数
        int solTokenCount = solList.stream()
            .mapToInt(m -> ((Number) m.get("tokenCount")).intValue())
            .sum();
        
        int bscTokenCount = bscList.stream()
            .mapToInt(m -> ((Number) m.get("tokenCount")).intValue())
            .sum();
        
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("solBatchCount", solList.size());
        stats.put("solTokenCount", solTokenCount);
        stats.put("bscBatchCount", bscList.size());
        stats.put("bscTokenCount", bscTokenCount);
        stats.put("totalTokenCount", solTokenCount + bscTokenCount);
        
        return AjaxResult.success(stats);
    }

    /**
     * 查询指定批次的Token列表（统一参数）
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:query')")
    @GetMapping("/tokens")
    public AjaxResult getTokens(@RequestParam Integer batchId,
                               @RequestParam String chainType)
    {
        List<Map<String, Object>> list = batchMonitorService.getTokensInBatch(batchId, "batch", chainType);
        return AjaxResult.success(list);
    }

    /**
     * 删除批次（统一参数）
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:remove')")
    @Log(title = "批量监控", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete")
    public AjaxResult deleteBatchByParams(@RequestParam Integer batchId,
                                          @RequestParam String chainType)
    {
        int count = batchMonitorService.deleteBatch(batchId, "batch", chainType);
        return toAjax(count);
    }

    /**
     * 启用/停用批次（统一参数）
     */
    @PreAuthorize("@ss.hasPermi('crypto:batch:edit')")
    @Log(title = "批量监控", businessType = BusinessType.UPDATE)
    @PutMapping("/toggleStatus")
    public AjaxResult toggleStatus(@RequestBody Map<String, Object> params)
    {
        Integer batchId = (Integer) params.get("batchId");
        String chainType = (String) params.get("chainType");
        Integer isActive = (Integer) params.get("isActive");
        
        int count = batchMonitorService.toggleBatchStatus(batchId, "batch", chainType, isActive);
        return toAjax(count);
    }
}

