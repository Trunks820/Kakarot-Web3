package com.ruoyi.crypto.service;

import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import com.ruoyi.crypto.domain.vo.CryptoCaRecordVO;

import java.util.List;

public interface CryptoMonitorService {

    /**
     * 查询监控配置列表
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 监控配置集合
     */
    List<CryptoMonitorConfig> selectCryptoMonitorConfigList(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 查询监控配置详情
     * 
     * @param id 监控配置ID
     * @return 监控配置
     */
    CryptoMonitorConfig selectCryptoMonitorConfigById(Long id);

    /**
     * 新增监控配置
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    int insertCryptoMonitorConfig(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 修改监控配置
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    int updateCryptoMonitorConfig(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 批量删除监控配置
     * 
     * @param ids 需要删除的监控配置ID
     * @return 结果
     */
    int deleteCryptoMonitorConfigByIds(Long[] ids);

    /**
     * 删除监控配置信息
     * 
     * @param id 监控配置ID
     * @return 结果
     */
    int deleteCryptoMonitorConfigById(Long id);

    /**
     * 根据代币地址检查是否已被监控
     * 
     * @param coinAddress 代币地址
     * @param createBy 创建者
     * @return 是否已被监控
     */
    boolean checkTokenMonitored(String coinAddress, String createBy);

    /**
     * 更改监控状态
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    int changeStatus(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 查询激活的监控配置
     * 
     * @return 监控配置集合
     */
    List<CryptoMonitorConfig> selectActiveCryptoMonitorConfig();

    // === 兼容旧版本的方法 ===
    
    /**
     * @deprecated 使用 selectCryptoMonitorConfigList 替代
     */
    @Deprecated
    List<CryptoMonitorConfig> select(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * @deprecated 使用 selectCryptoMonitorConfigById 替代
     */
    @Deprecated
    CryptoMonitorConfig getConfig(CryptoMonitorConfig cryptoMonitorConfig);
}
