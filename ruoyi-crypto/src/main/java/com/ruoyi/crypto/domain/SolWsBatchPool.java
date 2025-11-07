package com.ruoyi.crypto.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * SOL WS批次池对象 sol_ws_batch_pool
 * 
 * @author ruoyi
 * @date 2025-01-27
 */
public class SolWsBatchPool extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 批次ID（1-20） */
    private Integer batchId;

    /** 来源类型：smart智能匹配/batch批量手动 */
    private String sourceType;

    /** 链类型：sol/bsc/eth */
    private String chainType;

    /** Token CA地址 */
    private String ca;

    /** Token符号 */
    private String tokenSymbol;

    /** Token名称 */
    private String tokenName;

    /** 交易对地址 */
    private String pairAddress;

    /** 市值 */
    private BigDecimal marketCap;

    /** Twitter URL */
    private String twitterUrl;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 时间周期 */
    private String timeInterval;

    /** 事件配置JSON */
    private String eventsConfig;

    /** 触发逻辑 */
    private String triggerLogic;

    /** 优先级 */
    private Integer priority;

    /** 排序 */
    private Integer sortOrder;

    /** 是否激活 */
    private Integer isActive;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBatchId(Integer batchId) 
    {
        this.batchId = batchId;
    }

    public Integer getBatchId() 
    {
        return batchId;
    }

    public void setSourceType(String sourceType) 
    {
        this.sourceType = sourceType;
    }

    public String getSourceType() 
    {
        return sourceType;
    }

    public void setChainType(String chainType) 
    {
        this.chainType = chainType;
    }

    public String getChainType() 
    {
        return chainType;
    }

    public void setCa(String ca) 
    {
        this.ca = ca;
    }

    public String getCa() 
    {
        return ca;
    }

    public void setTokenSymbol(String tokenSymbol) 
    {
        this.tokenSymbol = tokenSymbol;
    }

    public String getTokenSymbol() 
    {
        return tokenSymbol;
    }

    public void setTokenName(String tokenName) 
    {
        this.tokenName = tokenName;
    }

    public String getTokenName() 
    {
        return tokenName;
    }

    public void setPairAddress(String pairAddress) 
    {
        this.pairAddress = pairAddress;
    }

    public String getPairAddress() 
    {
        return pairAddress;
    }

    public void setMarketCap(BigDecimal marketCap) 
    {
        this.marketCap = marketCap;
    }

    public BigDecimal getMarketCap() 
    {
        return marketCap;
    }

    public void setTwitterUrl(String twitterUrl) 
    {
        this.twitterUrl = twitterUrl;
    }

    public String getTwitterUrl() 
    {
        return twitterUrl;
    }

    public void setTemplateId(Long templateId) 
    {
        this.templateId = templateId;
    }

    public Long getTemplateId() 
    {
        return templateId;
    }

    public void setTemplateName(String templateName) 
    {
        this.templateName = templateName;
    }

    public String getTemplateName() 
    {
        return templateName;
    }

    public void setTimeInterval(String timeInterval) 
    {
        this.timeInterval = timeInterval;
    }

    public String getTimeInterval() 
    {
        return timeInterval;
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

    public void setPriority(Integer priority) 
    {
        this.priority = priority;
    }

    public Integer getPriority() 
    {
        return priority;
    }

    public void setSortOrder(Integer sortOrder) 
    {
        this.sortOrder = sortOrder;
    }

    public Integer getSortOrder() 
    {
        return sortOrder;
    }

    public void setIsActive(Integer isActive) 
    {
        this.isActive = isActive;
    }

    public Integer getIsActive() 
    {
        return isActive;
    }

    @Override
    public Date getCreateTime() 
    {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }

    @Override
    public Date getUpdateTime() 
    {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime) 
    {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "SolWsBatchPool{" +
                "id=" + id +
                ", batchId=" + batchId +
                ", sourceType='" + sourceType + '\'' +
                ", chainType='" + chainType + '\'' +
                ", ca='" + ca + '\'' +
                ", tokenSymbol='" + tokenSymbol + '\'' +
                ", tokenName='" + tokenName + '\'' +
                ", pairAddress='" + pairAddress + '\'' +
                ", marketCap=" + marketCap +
                ", twitterUrl='" + twitterUrl + '\'' +
                ", templateId=" + templateId +
                ", templateName='" + templateName + '\'' +
                ", timeInterval='" + timeInterval + '\'' +
                ", eventsConfig='" + eventsConfig + '\'' +
                ", triggerLogic='" + triggerLogic + '\'' +
                ", priority=" + priority +
                ", sortOrder=" + sortOrder +
                ", isActive=" + isActive +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}

