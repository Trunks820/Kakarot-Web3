package com.ruoyi.crypto.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 监控任务目标对象 monitor_task_target_v2
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
public class MonitorTaskTarget extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 合约地址/目标标识 */
    @Excel(name = "合约地址")
    private String ca;

    /** 链类型 */
    @Excel(name = "链类型")
    private String chainType;

    /** 状态：active-活跃, removed-已移除 */
    @Excel(name = "状态")
    private String status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }

    public void setCa(String ca) 
    {
        this.ca = ca;
    }

    public String getCa() 
    {
        return ca;
    }

    public void setChainType(String chainType) 
    {
        this.chainType = chainType;
    }

    public String getChainType() 
    {
        return chainType;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }

    public Date getCreateTime() 
    {
        return createTime;
    }

    public void setUpdateTime(Date updateTime) 
    {
        this.updateTime = updateTime;
    }

    public Date getUpdateTime() 
    {
        return updateTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("taskId", getTaskId())
            .append("ca", getCa())
            .append("chainType", getChainType())
            .append("status", getStatus())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

