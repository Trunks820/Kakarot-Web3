package com.ruoyi.crypto.service.impl;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import com.ruoyi.crypto.domain.vo.CryptoApiResultVo;
import com.ruoyi.crypto.mapper.CryptoMonitorConfigMapper;
import com.ruoyi.crypto.service.CryptoMonitorService;
import com.ruoyi.crypto.utils.ChainApiUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CryptoMonitorServiceImpl implements CryptoMonitorService {

    @Resource
    private CryptoMonitorConfigMapper cryptoMonitorConfigMapper;

    @Resource
    private ChainApiUtils chainApiUtils;

    /**
     * 查询监控配置列表
     * 
     * @param cryptoMonitorConfig 监控配置
     * @return 监控配置
     */
    @Override
    public List<CryptoMonitorConfig> selectCryptoMonitorConfigList(CryptoMonitorConfig cryptoMonitorConfig) {
        List<CryptoMonitorConfig> list = cryptoMonitorConfigMapper.selectCryptoMonitorConfigList(cryptoMonitorConfig);
        return list != null ? list : new ArrayList<>();
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
        String coinAddress = cryptoMonitorConfig.getCoinAddress();
        AjaxResult dexPairInfo = chainApiUtils.getDexPairInfo(coinAddress);
        if(dexPairInfo.isSuccess()){
            // 修复：正确处理返回的数据结构
            Object data = dexPairInfo.get("data");
            if (data instanceof CryptoApiResultVo) {
                CryptoApiResultVo vo = (CryptoApiResultVo) data;
                // 优先使用name（代币名称），如果为空则使用symbol（代币符号）
                String tokenName = vo.getName();
                if (tokenName == null || tokenName.trim().isEmpty()) {
                    tokenName = vo.getSymbol();
                }
                cryptoMonitorConfig.setTokenName(tokenName);
            } else if (data != null) {
                // 兜底：如果数据是其他格式，尝试JSON解析
                try {
                    JSONObject jsonObject = JSONUtil.parseObj(data.toString());
                    String tokenName = jsonObject.getStr("name");
                    if (tokenName == null || tokenName.trim().isEmpty()) {
                        tokenName = jsonObject.getStr("symbol");
                    }
                    cryptoMonitorConfig.setTokenName(tokenName);
                } catch (Exception e) {
                    // 如果解析失败，记录日志但不影响保存
                    System.err.println("Failed to parse token info: " + e.getMessage());
                }
            }
        }
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
        List<CryptoMonitorConfig> list = cryptoMonitorConfigMapper.selectActiveCryptoMonitorConfig();
        return list != null ? list : new ArrayList<>();
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
