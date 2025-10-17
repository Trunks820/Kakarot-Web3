package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.TokenMonitorConfig;

import java.util.List;

/**
 * Token监控配置服务接口
 *
 * @author ruoyi
 */
public interface ITokenMonitorConfigService {

    /**
     * 查询Token监控配置列表
     *
     * @param config Token监控配置
     * @return Token监控配置集合
     */
    List<TokenMonitorConfig> selectMonitorConfigList(TokenMonitorConfig config);

    /**
     * 查询Token监控配置详情
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
    List<TokenMonitorConfig> selectMonitorConfigByCa(String ca);

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
     * 保存或更新Token监控配置（根据CA判断新增还是更新）
     *
     * @param config Token监控配置
     * @return 结果
     */
    int saveOrUpdateMonitorConfig(TokenMonitorConfig config);

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
     * 启用监控
     *
     * @param id 监控配置主键
     * @return 结果
     */
    int enableMonitor(Long id);

    /**
     * 停用监控
     *
     * @param id 监控配置主键
     * @return 结果
     */
    int disableMonitor(Long id);

    /**
     * 批量启用监控
     *
     * @param ids 监控配置主键集合
     * @return 结果
     */
    int batchEnableMonitor(Long[] ids);

    /**
     * 批量停用监控
     *
     * @param ids 监控配置主键集合
     * @return 结果
     */
    int batchDisableMonitor(Long[] ids);
}

