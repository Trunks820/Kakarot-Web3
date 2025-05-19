package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.CryptoGroupStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 群组CA统计Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoGroupStatisticsMapper
{
    /**
     * 查询群组CA统计
     *
     * @param id 群组CA统计ID
     * @return 群组CA统计
     */
    public CryptoGroupStatistics selectCryptoGroupStatisticsById(Long id);

    /**
     * 根据群组ID查询群组CA统计
     *
     * @param groupId 群组ID
     * @return 群组CA统计
     */
    public CryptoGroupStatistics selectCryptoGroupStatisticsByGroupId(String groupId);

    /**
     * 查询群组CA统计列表
     *
     * @param cryptoGroupStatistics 群组CA统计
     * @return 群组CA统计集合
     */
    public List<CryptoGroupStatistics> selectCryptoGroupStatisticsList(CryptoGroupStatistics cryptoGroupStatistics);

    /**
     * 获取胜率排行榜
     *
     * @param limit 限制数量
     * @return 群组CA统计集合
     */
    public List<CryptoGroupStatistics> selectTopGroupsByWinRate(@Param("limit") int limit);

    /**
     * 新增群组CA统计
     *
     * @param cryptoGroupStatistics 群组CA统计
     * @return 结果
     */
    public int insertCryptoGroupStatistics(CryptoGroupStatistics cryptoGroupStatistics);

    /**
     * 修改群组CA统计
     *
     * @param cryptoGroupStatistics 群组CA统计
     * @return 结果
     */
    public int updateCryptoGroupStatistics(CryptoGroupStatistics cryptoGroupStatistics);

    /**
     * 删除群组CA统计
     *
     * @param id 群组CA统计ID
     * @return 结果
     */
    public int deleteCryptoGroupStatisticsById(Long id);

    /**
     * 批量删除群组CA统计
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteCryptoGroupStatisticsByIds(Long[] ids);
}