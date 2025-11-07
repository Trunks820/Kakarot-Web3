package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.SolWsBatchPool;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * SOL WS批次池Mapper接口
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
public interface SolWsBatchPoolMapper 
{
    /**
     * 查询批次池列表
     * 
     * @param solWsBatchPool 批次池
     * @return 批次池集合
     */
    List<SolWsBatchPool> selectSolWsBatchPoolList(SolWsBatchPool solWsBatchPool);

    /**
     * 新增批次池
     * 
     * @param solWsBatchPool 批次池
     * @return 结果
     */
    int insertSolWsBatchPool(SolWsBatchPool solWsBatchPool);

    /**
     * 批量新增批次池
     * 
     * @param list 批次池列表
     * @return 结果
     */
    int batchInsertSolWsBatchPool(List<SolWsBatchPool> list);

    /**
     * 修改批次池
     * 
     * @param solWsBatchPool 批次池
     * @return 结果
     */
    int updateSolWsBatchPool(SolWsBatchPool solWsBatchPool);

    /**
     * 删除批次池
     * 
     * @param id 批次池主键
     * @return 结果
     */
    int deleteSolWsBatchPoolById(Long id);

    /**
     * 批量删除批次池
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteSolWsBatchPoolByIds(Long[] ids);

    /**
     * 根据批次ID删除
     * 
     * @param batchId 批次ID
     * @param sourceType 来源类型
     * @param chainType 链类型
     * @return 结果
     */
    int deleteSolWsBatchPoolByBatchId(@Param("batchId") Integer batchId, 
                                       @Param("sourceType") String sourceType,
                                       @Param("chainType") String chainType);

    /**
     * 查询已使用的批次ID
     * 
     * @param chainType 链类型
     * @param sourceType 来源类型
     * @return 批次ID列表
     */
    List<Integer> selectUsedBatchIds(@Param("chainType") String chainType, 
                                      @Param("sourceType") String sourceType);

    /**
     * 查找最后一个未满的批次
     * 
     * @param params 查询参数
     * @return 批次信息
     */
    Map<String, Object> findLastUnfilledBatch(Map<String, Object> params);

    /**
     * 统计批次中的Token数量
     * 
     * @param batchId 批次ID
     * @param sourceType 来源类型
     * @param chainType 链类型
     * @return Token数量
     */
    int countTokensInBatch(@Param("batchId") Integer batchId,
                           @Param("sourceType") String sourceType,
                           @Param("chainType") String chainType);

    /**
     * 按批次ID分组查询（带统计）
     * 
     * @param solWsBatchPool 查询条件
     * @return 批次列表（带统计信息）
     */
    List<Map<String, Object>> selectBatchListWithStats(SolWsBatchPool solWsBatchPool);
}

