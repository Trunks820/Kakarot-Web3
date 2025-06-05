package com.ruoyi.crypto.mapper;

import com.ruoyi.crypto.domain.CryptoDynamicConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CryptoDynamicConfigMapper {
    
    /**
     * 查询所有启用的配置
     */
    @Select("SELECT * FROM crypto_dynamic_config WHERE is_enabled = 1 ORDER BY provider, config_group, config_key")
    List<CryptoDynamicConfig> selectAllEnabled();
    
    /**
     * 根据提供商查询配置
     */
    @Select("SELECT * FROM crypto_dynamic_config WHERE provider = #{provider} AND is_enabled = 1")
    List<CryptoDynamicConfig> selectByProvider(String provider);
    
    /**
     * 查询所有配置
     */
    List<CryptoDynamicConfig> selectCryptoDynamicConfigList(CryptoDynamicConfig cryptoDynamicConfig);
    
    /**
     * 新增配置
     */
    int insertCryptoDynamicConfig(CryptoDynamicConfig cryptoDynamicConfig);
    
    /**
     * 修改配置
     */
    int updateCryptoDynamicConfig(CryptoDynamicConfig cryptoDynamicConfig);
    
    /**
     * 删除配置
     */
    int deleteCryptoDynamicConfigById(Long id);
    
    /**
     * 批量删除配置
     */
    int deleteCryptoDynamicConfigByIds(Long[] ids);
} 