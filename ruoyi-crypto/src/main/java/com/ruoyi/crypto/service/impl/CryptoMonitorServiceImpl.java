package com.ruoyi.crypto.service.impl;


import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import com.ruoyi.crypto.mapper.CryptoMonitorConfigMapper;
import com.ruoyi.crypto.service.CryptoMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CryptoMonitorServiceImpl implements CryptoMonitorService {

    @Autowired
    private CryptoMonitorConfigMapper cryptoMonitorConfigMapper;

    /**
     * 查询监控配置列表
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 监控配置
     */
    @Override
    public List<CryptoMonitorConfig> selectCryptoMonitorConfigList(CryptoMonitorConfig cryptoMonitorConfig) {
        return cryptoMonitorConfigMapper.selectCryptoMonitorConfigList(cryptoMonitorConfig);
    }

    /**
     * 查询监控配置详情
     * 
     * @param id 监控配置ID
     * @return 监控配置
     */
    @Override
    public CryptoMonitorConfig selectCryptoMonitorConfigById(Long id) {
        return cryptoMonitorConfigMapper.selectCryptoMonitorConfigById(id);
    }

    /**
     * 新增监控配置
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int insertCryptoMonitorConfig(CryptoMonitorConfig cryptoMonitorConfig) {
        // 默认状态为启用
        if (cryptoMonitorConfig.getStatus() == null) {
            cryptoMonitorConfig.setStatus("1");
        }
        
        return cryptoMonitorConfigMapper.insertCryptoMonitorConfig(cryptoMonitorConfig);
    }

    /**
     * 修改监控配置
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int updateCryptoMonitorConfig(CryptoMonitorConfig cryptoMonitorConfig) {
        return cryptoMonitorConfigMapper.updateCryptoMonitorConfig(cryptoMonitorConfig);
    }

    /**
     * 批量删除监控配置
     * 
     * @param ids 需要删除的监控配置ID
     * @return 结果
     */
    @Override
    public int deleteCryptoMonitorConfigByIds(Long[] ids) {
        return cryptoMonitorConfigMapper.deleteCryptoMonitorConfigByIds(ids);
    }

    /**
     * 删除监控配置信息
     * 
     * @param id 监控配置ID
     * @return 结果
     */
    @Override
    public int deleteCryptoMonitorConfigById(Long id) {
        return cryptoMonitorConfigMapper.deleteCryptoMonitorConfigById(id);
    }

    /**
     * 根据代币地址检查是否已被监控
     * 
     * @param coinAddress 代币地址
     * @param userId 用户ID
     * @return 是否已被监控
     */
    @Override
    public boolean checkTokenMonitored(String coinAddress, String createBy) {
        CryptoMonitorConfig queryConfig = new CryptoMonitorConfig();
        queryConfig.setCoinAddress(coinAddress);
        queryConfig.setCreateBy(createBy);
        queryConfig.setStatus("1"); // 只查询启用状态的监控
        
        List<CryptoMonitorConfig> existingConfigs = cryptoMonitorConfigMapper.selectCryptoMonitorConfigList(queryConfig);
        return existingConfigs != null && !existingConfigs.isEmpty();
    }

    /**
     * 更改监控状态
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    @Override
    public int changeStatus(CryptoMonitorConfig cryptoMonitorConfig) {
        return cryptoMonitorConfigMapper.updateCryptoMonitorConfig(cryptoMonitorConfig);
    }



    /**
     * 查询激活的监控配置
     * 
     * @return 监控配置集合
     */
    @Override
    public List<CryptoMonitorConfig> selectActiveCryptoMonitorConfig() {
        return cryptoMonitorConfigMapper.selectActiveCryptoMonitorConfig();
    }

    // === 兼容旧版本的方法实现 ===
    
    @Override
    @Deprecated
    public List<CryptoMonitorConfig> select(CryptoMonitorConfig cryptoMonitorConfig) {
        return selectCryptoMonitorConfigList(cryptoMonitorConfig);
    }

    @Override
    @Deprecated
    public CryptoMonitorConfig getConfig(CryptoMonitorConfig cryptoMonitorConfig) {
        if (cryptoMonitorConfig.getId() != null) {
            return selectCryptoMonitorConfigById(cryptoMonitorConfig.getId());
        }
        
        List<CryptoMonitorConfig> configs = selectCryptoMonitorConfigList(cryptoMonitorConfig);
        return configs != null && !configs.isEmpty() ? configs.get(0) : null;
    }
}
