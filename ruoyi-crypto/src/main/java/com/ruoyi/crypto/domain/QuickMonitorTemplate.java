package com.ruoyi.crypto.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

/**
 * Token智能监控配置模板对象 quick_monitor_template
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
public class QuickMonitorTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 配置类型：smart智能模板/batch批量配置 */
    private String sourceType;

    /** 用户ID */
    private Long userId;

    /** 链类型 */
    private String chainType;

    /** 最低市值门槛 */
    private BigDecimal minMarketCap;

    /** Twitter筛选条件 */
    private String hasTwitter;

    /** 时间周期：1m/5m/1h */
    private String timeInterval;

    /** 前十持仓过滤阈值(%) */
    private java.math.BigDecimal topHoldersThreshold;

    /** 配置名称 */
    private String configName;

    /** 监控事件配置JSON */
    private String eventsConfig;

    /** 通知方式 */
    private String notifyMethods;

    /** 触发逻辑 */
    private String triggerLogic;

    /** 排序序号 */
    private Integer sortOrder;

    /** 状态 */
    private String status;

    /** 删除标志（0代表存在 2代表删除） */
    private String delFlag;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setSourceType(String sourceType) 
    {
        this.sourceType = sourceType;
    }

    public String getSourceType() 
    {
        return sourceType;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setChainType(String chainType) 
    {
        this.chainType = chainType;
    }

    public String getChainType() 
    {
        return chainType;
    }

    public void setMinMarketCap(BigDecimal minMarketCap) 
    {
        this.minMarketCap = minMarketCap;
    }

    public BigDecimal getMinMarketCap() 
    {
        return minMarketCap;
    }

    public void setHasTwitter(String hasTwitter) 
    {
        this.hasTwitter = hasTwitter;
    }

    public String getHasTwitter() 
    {
        return hasTwitter;
    }

    public void setTimeInterval(String timeInterval) 
    {
        this.timeInterval = timeInterval;
    }

    public String getTimeInterval() 
    {
        return timeInterval;
    }

    public void setTopHoldersThreshold(java.math.BigDecimal topHoldersThreshold) 
    {
        this.topHoldersThreshold = topHoldersThreshold;
    }

    public java.math.BigDecimal getTopHoldersThreshold() 
    {
        return topHoldersThreshold;
    }

    public void setConfigName(String configName) 
    {
        this.configName = configName;
    }

    public String getConfigName() 
    {
        return configName;
    }

    public void setEventsConfig(String eventsConfig) 
    {
        this.eventsConfig = eventsConfig;
    }

    public String getEventsConfig() 
    {
        return eventsConfig;
    }

    public void setNotifyMethods(String notifyMethods) 
    {
        this.notifyMethods = notifyMethods;
    }

    public String getNotifyMethods() 
    {
        return notifyMethods;
    }

    public void setTriggerLogic(String triggerLogic) 
    {
        this.triggerLogic = triggerLogic;
    }

    public String getTriggerLogic() 
    {
        return triggerLogic;
    }

    public void setSortOrder(Integer sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() 
    {
        return sortOrder;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return "QuickMonitorTemplate{" +
                "id=" + id +
                ", sourceType='" + sourceType + '\'' +
                ", userId=" + userId +
                ", chainType='" + chainType + '\'' +
                ", minMarketCap=" + minMarketCap +
                ", hasTwitter='" + hasTwitter + '\'' +
                ", timeInterval='" + timeInterval + '\'' +
                ", topHoldersThreshold=" + topHoldersThreshold +
                ", configName='" + configName + '\'' +
                ", eventsConfig='" + eventsConfig + '\'' +
                ", notifyMethods='" + notifyMethods + '\'' +
                ", triggerLogic='" + triggerLogic + '\'' +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                ", delFlag='" + delFlag + '\'' +
                '}';
    }
}

