package com.ruoyi.crypto.mapper;

import java.util.List;
import com.ruoyi.crypto.domain.CryptoMonitorConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 监控配置Mapper接口
 *
 * @author ruoyi
 */
@Mapper
public interface CryptoMonitorConfigMapper
{
    /**
     * 查询监控配置
     *
     * @param id 监控配置ID
     * @return 监控配置
     */
    public CryptoMonitorConfig selectCryptoMonitorConfigById(Long id);

    /**
     * 查询监控配置列表
     *
     * @param cryptoMonitorConfig 监控配置
     * @return 监控配置集合
     */
    public List<CryptoMonitorConfig> selectCryptoMonitorConfigList(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 查询所有激活的监控配置
     *
     * @return 监控配置集合
     */
    public List<CryptoMonitorConfig> selectActiveCryptoMonitorConfig();

    /**
     * 新增监控配置
     *
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    public int insertCryptoMonitorConfig(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 修改监控配置
     *
     * @param cryptoMonitorConfig 监控配置
     * @return 结果
     */
    public int updateCryptoMonitorConfig(CryptoMonitorConfig cryptoMonitorConfig);

    /**
     * 删除监控配置
     *
     * @param id 监控配置ID
     * @return 结果
     */
    public int deleteCryptoMonitorConfigById(Long id);

    /**
     * 批量删除监控配置
     *
     * @param ids 需要删除的监控配置ID
     * @return 结果
     */
    public int deleteCryptoMonitorConfigByIds(Long[] ids);

    /**
     * 根据代币地址和创建者查询监控配置
     *
     * @param coinAddress 代币地址
     * @param createBy 创建者
     * @return 监控配置集合
     */
    public List<CryptoMonitorConfig> selectByCoinAddressAndCreateBy(String coinAddress, String createBy);

    /**
     * 更新通知时间
     *
     * @param id 监控配置ID
     * @return 结果
     */
    public int updateNotificationTime(Long id);
}