package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.MonitorBatchItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 批次项Mapper接口
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
public interface MonitorBatchItemMapper 
{
    /**
     * 查询批次项
     * 
     * @param id 批次项主键
     * @return 批次项
     */
    public MonitorBatchItem selectMonitorBatchItemById(Long id);

    /**
     * 查询批次项列表
     * 
     * @param batchId 批次ID
     * @return 批次项集合
     */
    public List<MonitorBatchItem> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 新增批次项
     * 
     * @param monitorBatchItem 批次项
     * @return 结果
     */
    public int insertMonitorBatchItem(MonitorBatchItem monitorBatchItem);

    /**
     * 批量新增批次项
     * 
     * @param items 批次项列表
     * @return 结果
     */
    public int insertBatchItems(List<MonitorBatchItem> items);

    /**
     * 修改批次项
     * 
     * @param monitorBatchItem 批次项
     * @return 结果
     */
    public int updateMonitorBatchItem(MonitorBatchItem monitorBatchItem);

    /**
     * 删除批次项
     * 
     * @param id 批次项主键
     * @return 结果
     */
    public int deleteMonitorBatchItemById(Long id);

    /**
     * 批量删除批次项
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteMonitorBatchItemByIds(Long[] ids);

    /**
     * 删除批次下的所有项
     * 
     * @param batchId 批次ID
     * @return 结果
     */
    public int deleteByBatchId(@Param("batchId") Long batchId);
}

