package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 监控任务对象 monitor_task
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
public class MonitorTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long id;

    /** 任务名称 */
    private String taskName;

    /** 任务类型：smart-智能/batch-批量/block-区块 */
    private String taskType;

    /** 链类型：sol/bsc/eth */
    private String chainType;

    /** 最小市值(仅smart) */
    private BigDecimal minMarketCap;

    /** 最大市值(仅smart) */
    private BigDecimal maxMarketCap;

    /** 是否有Twitter(仅smart) */
    private Integer hasTwitter;

    /** 是否自动同步目标(仅smart) */
    private Integer autoSyncTargets;

    /** 同步间隔分钟数(仅smart) */
    private Integer syncIntervalMinutes;

    /** 目标数量(仅batch) */
    private Integer targetCount;

    /** 市场类型(仅block)：internal-内盘/external-外盘 */
    private String marketType;

    /** 单笔最小交易金额(USD)(仅block) */
    private BigDecimal minTransactionUsd;

    /** 累计最小金额(USD)(仅block) */
    private BigDecimal cumulativeMinAmountUsd;

    /** 定时规则(可选) */
    private String scheduleCron;

    /** 状态：1-启用/0-停用/2-暂停 */
    private Integer status;

    /** 最后执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastRunTime;

    /** 下次执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date nextRunTime;

    /** 任务描述 */
    private String description;

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

    /** 关联的配置列表（非数据库字段） */
    private List<MonitorConfig> configs;

    /** 配置ID列表（用于创建/更新） */
    private List<Long> configIds;

    // Getters and Setters

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setChainType(String chainType) {
        this.chainType = chainType;
    }

    public String getChainType() {
        return chainType;
    }

    public void setMinMarketCap(BigDecimal minMarketCap) {
        this.minMarketCap = minMarketCap;
    }

    public BigDecimal getMinMarketCap() {
        return minMarketCap;
    }

    public void setMaxMarketCap(BigDecimal maxMarketCap) {
        this.maxMarketCap = maxMarketCap;
    }

    public BigDecimal getMaxMarketCap() {
        return maxMarketCap;
    }

    public void setHasTwitter(Integer hasTwitter) {
        this.hasTwitter = hasTwitter;
    }

    public Integer getHasTwitter() {
        return hasTwitter;
    }

    public void setAutoSyncTargets(Integer autoSyncTargets) {
        this.autoSyncTargets = autoSyncTargets;
    }

    public Integer getAutoSyncTargets() {
        return autoSyncTargets;
    }

    public void setSyncIntervalMinutes(Integer syncIntervalMinutes) {
        this.syncIntervalMinutes = syncIntervalMinutes;
    }

    public Integer getSyncIntervalMinutes() {
        return syncIntervalMinutes;
    }

    public void setTargetCount(Integer targetCount) {
        this.targetCount = targetCount;
    }

    public Integer getTargetCount() {
        return targetCount;
    }

    public void setScheduleCron(String scheduleCron) {
        this.scheduleCron = scheduleCron;
    }

    public String getScheduleCron() {
        return scheduleCron;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }

    public void setLastRunTime(Date lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public Date getLastRunTime() {
        return lastRunTime;
    }

    public void setNextRunTime(Date nextRunTime) {
        this.nextRunTime = nextRunTime;
    }

    public Date getNextRunTime() {
        return nextRunTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
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

    public void setConfigs(List<MonitorConfig> configs) {
        this.configs = configs;
    }

    public List<MonitorConfig> getConfigs() {
        return configs;
    }

    public void setConfigIds(List<Long> configIds) {
        this.configIds = configIds;
    }

    public List<Long> getConfigIds() {
        return configIds;
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

    @Override
    public String toString() {
        return "MonitorTask{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", taskType='" + taskType + '\'' +
                ", chainType='" + chainType + '\'' +
                ", minMarketCap=" + minMarketCap +
                ", maxMarketCap=" + maxMarketCap +
                ", targetCount=" + targetCount +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}

