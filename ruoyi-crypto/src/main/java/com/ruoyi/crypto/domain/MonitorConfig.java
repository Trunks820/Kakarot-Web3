package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 监控配置对象 monitor_config
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public class MonitorConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置ID */
    private Long id;

    /** 配置名称 */
    private String configName;

    /** 配置类型：price/holder/volume/composite */
    private String configType;

    /** 链类型：sol/bsc/eth */
    private String chainType;

    /** 数据源筛选：all/pump/fourmeme */
    private String source;

    /** 市场类型：internal-内盘/external-外盘 */
    private String marketType;

    /** 单笔最小交易金额(USD) */
    private BigDecimal minTransactionUsd;

    /** 累计最小金额(USD) */
    private BigDecimal cumulativeMinAmountUsd;

    /** 时间周期：1m/5m/1h/24h */
    private String timeInterval;

    /** 前十持仓阈值% */
    private BigDecimal topHoldersThreshold;

    /** 事件配置JSON */
    private String eventsConfig;

    /** 触发逻辑：any-任一满足/all-全部满足 */
    private String triggerLogic;

    /** 通知方式：telegram,wechat,email */
    private String notifyMethods;

    /** 配置版本号 */
    private Integer version;

    /** 配置描述 */
    private String description;

    /** 状态：1-启用/0-停用 */
    private Integer status;

    /** 创建者 */
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新者 */
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /** 删除标志：0-未删除/1-已删除 */
    private Integer delFlag;

    // Getters and Setters

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConfigType() {
        return configType;
    }

    public void setChainType(String chainType) {
        this.chainType = chainType;
    }

    public String getChainType() {
        return chainType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    public String getMarketType() {
        return marketType;
    }

    public void setMinTransactionUsd(BigDecimal minTransactionUsd) {
        this.minTransactionUsd = minTransactionUsd;
    }

    public BigDecimal getMinTransactionUsd() {
        return minTransactionUsd;
    }

    public void setCumulativeMinAmountUsd(BigDecimal cumulativeMinAmountUsd) {
        this.cumulativeMinAmountUsd = cumulativeMinAmountUsd;
    }

    public BigDecimal getCumulativeMinAmountUsd() {
        return cumulativeMinAmountUsd;
    }

    public void setTimeInterval(String timeInterval) {
        this.timeInterval = timeInterval;
    }

    public String getTimeInterval() {
        return timeInterval;
    }

    public void setTopHoldersThreshold(BigDecimal topHoldersThreshold) {
        this.topHoldersThreshold = topHoldersThreshold;
    }

    public BigDecimal getTopHoldersThreshold() {
        return topHoldersThreshold;
    }

    public void setEventsConfig(String eventsConfig) {
        this.eventsConfig = eventsConfig;
    }

    public String getEventsConfig() {
        return eventsConfig;
    }

    public void setTriggerLogic(String triggerLogic) {
        this.triggerLogic = triggerLogic;
    }

    public String getTriggerLogic() {
        return triggerLogic;
    }

    public void setNotifyMethods(String notifyMethods) {
        this.notifyMethods = notifyMethods;
    }

    public String getNotifyMethods() {
        return notifyMethods;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    @Override
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    @Override
    public String getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    @Override
    public String getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    @Override
    public String toString() {
        return "MonitorConfig{" +
                "id=" + id +
                ", configName='" + configName + '\'' +
                ", configType='" + configType + '\'' +
                ", chainType='" + chainType + '\'' +
                ", source='" + source + '\'' +
                ", marketType='" + marketType + '\'' +
                ", timeInterval='" + timeInterval + '\'' +
                ", version=" + version +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}

