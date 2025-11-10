package com.ruoyi.crypto.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 批次项对象 monitor_batch_item_v2
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
public class MonitorBatchItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 记录ID */
    private Long id;

    /** 批次ID */
    @Excel(name = "批次ID")
    private Long batchId;

    /** 目标ID */
    @Excel(name = "目标ID")
    private Long targetId;

    /** 批次内序号(0-98) */
    @Excel(name = "批次内序号")
    private Integer itemOrder;

    /** Token合约地址（冗余字段） */
    @Excel(name = "Token合约地址")
    private String ca;

    /** Token名称（冗余字段） */
    @Excel(name = "Token名称")
    private String tokenName;

    /** Token符号（冗余字段） */
    @Excel(name = "Token符号")
    private String tokenSymbol;

    /** 市值（冗余字段） */
    @Excel(name = "市值")
    private String marketCap;

    /** 任务ID（冗余字段） */
    private Long taskId;

    /** 批次编号（冗余字段） */
    private Integer batchNo;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBatchId(Long batchId) 
    {
        this.batchId = batchId;
    }

    public Long getBatchId() 
    {
        return batchId;
    }

    public void setTargetId(Long targetId) 
    {
        this.targetId = targetId;
    }

    public Long getTargetId() 
    {
        return targetId;
    }

    public void setItemOrder(Integer itemOrder) 
    {
        this.itemOrder = itemOrder;
    }

    public Integer getItemOrder() 
    {
        return itemOrder;
    }

    public void setCa(String ca) 
    {
        this.ca = ca;
    }

    public String getCa() 
    {
        return ca;
    }

    public void setTokenName(String tokenName) 
    {
        this.tokenName = tokenName;
    }

    public String getTokenName() 
    {
        return tokenName;
    }

    public void setTokenSymbol(String tokenSymbol) 
    {
        this.tokenSymbol = tokenSymbol;
    }

    public String getTokenSymbol() 
    {
        return tokenSymbol;
    }

    public void setMarketCap(String marketCap) 
    {
        this.marketCap = marketCap;
    }

    public String getMarketCap() 
    {
        return marketCap;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Integer getBatchNo() 
    {
        return batchNo;
    }

    public void setBatchNo(Integer batchNo) 
    {
        this.batchNo = batchNo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("batchId", getBatchId())
            .append("targetId", getTargetId())
            .append("itemOrder", getItemOrder())
            .append("ca", getCa())
            .append("tokenName", getTokenName())
            .append("tokenSymbol", getTokenSymbol())
            .append("marketCap", getMarketCap())
            .append("taskId", getTaskId())
            .append("batchNo", getBatchNo())
            .append("createTime", getCreateTime())
            .toString();
    }
}

