package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.TokenMonitorConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Token监控配置Mapper接口
 *
 * @author ruoyi
 */
public interface TokenMonitorConfigMapper {

    /**
     * 查询Token监控配置列表
     *
     * @param config Token监控配置
     * @return Token监控配置集合
     */
    List<TokenMonitorConfig> selectMonitorConfigList(TokenMonitorConfig config);

    /**
     * 查询Token监控配置
     *
     * @param id 监控配置主键
     * @return Token监控配置
     */
    TokenMonitorConfig selectMonitorConfigById(Long id);

    /**
     * 根据CA查询监控配置列表
     *
     * @param ca 合约地址
     * @return Token监控配置集合
     */
    List<TokenMonitorConfig> selectMonitorConfigByCa(@Param("ca") String ca);

    /**
     * 新增Token监控配置
     *
     * @param config Token监控配置
     * @return 结果
     */
    int insertMonitorConfig(TokenMonitorConfig config);

    /**
     * 修改Token监控配置
     *
     * @param config Token监控配置
     * @return 结果
     */
    int updateMonitorConfig(TokenMonitorConfig config);

    /**
     * 删除Token监控配置
     *
     * @param id 监控配置主键
     * @return 结果
     */
    int deleteMonitorConfigById(Long id);

    /**
     * 批量删除Token监控配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    int deleteMonitorConfigByIds(Long[] ids);

    /**
     * 更新监控状态
     *
     * @param id 监控配置主键
     * @param status 状态
     * @return 结果
     */
    int updateMonitorStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * 批量更新监控状态
     *
     * @param ids 监控配置主键集合
     * @param status 状态
     * @return 结果
     */
    int batchUpdateMonitorStatus(@Param("ids") Long[] ids, @Param("status") String status);

    /**
     * 统计某个CA的监控配置数量
     *
     * @param ca 合约地址
     * @return 数量
     */
    int countByCa(@Param("ca") String ca);

    /**
     * 获取监控中Token数量
     *
     * @return 监控中数量
     */
    Long getMonitoringCount();
}

