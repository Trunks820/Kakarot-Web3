package com.ruoyi.crypto.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;

/**
 * 链级全局监控配置对象 global_monitor_config
 * 
 * @author ruoyi
 * @date 2025-10-21
 */
public class GlobalMonitorConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 配置名称 */
    private String configName;

    /** 链类型：sol/bsc/eth/polygon等 */
    private String chainType;

    /** 数据源筛选：all/pump/fourmeme */
    private String source;

    /** 最小交易金额(USD) */
    private BigDecimal minTransactionUsd;

    /** 监控事件配置JSON */
    private String eventsConfig;

    /** 触发逻辑：any/all */
    private String triggerLogic;

    /** 通知方式 */
    private String notifyMethods;

    /** 状态：0停用 1启用 */
    private String status;

    /** 优先级：数字越大优先级越高 */
    private Integer priority;

    /** 备注说明 */
    private String remark;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setConfigName(String configName) 
    {
        this.configName = configName;
    }

    public String getConfigName() 
    {
        return configName;
    }

    public void setChainType(String chainType) 
    {
        this.chainType = chainType;
    }

    public String getChainType() 
    {
        return chainType;
    }

    public void setSource(String source) 
    {
        this.source = source;
    }

    public String getSource() 
    {
        return source;
    }

    public void setMinTransactionUsd(BigDecimal minTransactionUsd) 
    {
        this.minTransactionUsd = minTransactionUsd;
    }

    public BigDecimal getMinTransactionUsd() 
    {
        return minTransactionUsd;
    }

    public void setEventsConfig(String eventsConfig) 
    {
        this.eventsConfig = eventsConfig;
    }

    public String getEventsConfig() 
    {
        return eventsConfig;
    }

    public void setTriggerLogic(String triggerLogic) 
    {
        this.triggerLogic = triggerLogic;
    }

    public String getTriggerLogic() 
    {
        return triggerLogic;
    }

    public void setNotifyMethods(String notifyMethods) 
    {
        this.notifyMethods = notifyMethods;
    }

    public String getNotifyMethods() 
    {
        return notifyMethods;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setPriority(Integer priority) 
    {
        this.priority = priority;
    }

    public Integer getPriority() 
    {
        return priority;
    }

    public void setRemark(String remark) 
    {
        this.remark = remark;
    }

    public String getRemark() 
    {
        return remark;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("configName", getConfigName())
            .append("chainType", getChainType())
            .append("source", getSource())
            .append("minTransactionUsd", getMinTransactionUsd())
            .append("eventsConfig", getEventsConfig())
            .append("triggerLogic", getTriggerLogic())
            .append("notifyMethods", getNotifyMethods())
            .append("status", getStatus())
            .append("priority", getPriority())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

