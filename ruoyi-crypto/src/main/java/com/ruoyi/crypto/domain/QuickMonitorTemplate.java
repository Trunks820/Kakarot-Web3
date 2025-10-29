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

    /** 用户ID */
    private Long userId;

    /** 链类型 */
    private String chainType;

    /** 最低市值门槛 */
    private BigDecimal minMarketCap;

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

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
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

    @Override
    public String toString() {
        return "QuickMonitorTemplate{" +
                "id=" + id +
                ", userId=" + userId +
                ", chainType='" + chainType + '\'' +
                ", minMarketCap=" + minMarketCap +
                ", configName='" + configName + '\'' +
                ", eventsConfig='" + eventsConfig + '\'' +
                ", notifyMethods='" + notifyMethods + '\'' +
                ", triggerLogic='" + triggerLogic + '\'' +
                ", sortOrder=" + sortOrder +
                ", status='" + status + '\'' +
                '}';
    }
}

