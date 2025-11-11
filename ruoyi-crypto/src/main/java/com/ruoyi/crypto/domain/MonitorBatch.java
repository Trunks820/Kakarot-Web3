package com.ruoyi.crypto.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 批次对象 monitor_batch_v2
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
public class MonitorBatch extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 批次ID */
    private Long id;

    /** 任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 批次编号(从1开始) */
    @Excel(name = "批次编号")
    private Integer batchNo;

    /** 本批次项数(≤99) */
    @Excel(name = "批次项数")
    private Integer itemCount;

    /** 状态：pending/running/paused/stopped */
    @Excel(name = "状态")
    private String status;

    /** 执行节点ID */
    @Excel(name = "执行节点ID")
    private String workerId;

    /** 进程ID */
    @Excel(name = "进程ID")
    private Integer workerPid;

    /** 最后心跳时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后心跳时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastHeartbeat;

    /** 累计告警数 */
    @Excel(name = "累计告警数")
    private Integer totalAlerts;

    /** 最后告警时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后告警时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastAlertTime;

    /** 错误次数 */
    @Excel(name = "错误次数")
    private Integer errorCount;

    /** 最后错误信息 */
    @Excel(name = "最后错误信息")
    private String lastError;

    /** 任务名称（关联查询） */
    private String taskName;

    /** 任务类型（关联查询） */
    private String taskType;

    /** 链类型（关联查询） */
    private String chainType;

    /** Consumer ID（一致性哈希分配） */
    @Excel(name = "Consumer ID")
    private String consumerId;

    /** 批次版本号（每次重新分配递增） */
    @Excel(name = "批次版本号")
    private Integer epoch;

    /** 目标数量（本批次包含的CA数量） */
    @Excel(name = "目标数量")
    private Integer targetCount;

    /** 归档时间（旧epoch批次标记归档） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date archivedTime;

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

    public void setBatchNo(Integer batchNo) 
    {
        this.batchNo = batchNo;
    }

    public Integer getBatchNo() 
    {
        return batchNo;
    }

    public void setItemCount(Integer itemCount) 
    {
        this.itemCount = itemCount;
    }

    public Integer getItemCount() 
    {
        return itemCount;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setWorkerId(String workerId) 
    {
        this.workerId = workerId;
    }

    public String getWorkerId() 
    {
        return workerId;
    }

    public void setWorkerPid(Integer workerPid) 
    {
        this.workerPid = workerPid;
    }

    public Integer getWorkerPid() 
    {
        return workerPid;
    }

    public void setLastHeartbeat(Date lastHeartbeat) 
    {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Date getLastHeartbeat() 
    {
        return lastHeartbeat;
    }

    public void setTotalAlerts(Integer totalAlerts) 
    {
        this.totalAlerts = totalAlerts;
    }

    public Integer getTotalAlerts() 
    {
        return totalAlerts;
    }

    public void setLastAlertTime(Date lastAlertTime) 
    {
        this.lastAlertTime = lastAlertTime;
    }

    public Date getLastAlertTime() 
    {
        return lastAlertTime;
    }

    public void setErrorCount(Integer errorCount) 
    {
        this.errorCount = errorCount;
    }

    public Integer getErrorCount() 
    {
        return errorCount;
    }

    public void setLastError(String lastError) 
    {
        this.lastError = lastError;
    }

    public String getLastError() 
    {
        return lastError;
    }

    public String getTaskName() 
    {
        return taskName;
    }

    public void setTaskName(String taskName) 
    {
        this.taskName = taskName;
    }

    public String getTaskType() 
    {
        return taskType;
    }

    public void setTaskType(String taskType) 
    {
        this.taskType = taskType;
    }

    public String getChainType() 
    {
        return chainType;
    }

    public void setChainType(String chainType) 
    {
        this.chainType = chainType;
    }

    public String getConsumerId() 
    {
        return consumerId;
    }

    public void setConsumerId(String consumerId) 
    {
        this.consumerId = consumerId;
    }

    public Integer getEpoch() 
    {
        return epoch;
    }

    public void setEpoch(Integer epoch) 
    {
        this.epoch = epoch;
    }

    public Integer getTargetCount() 
    {
        return targetCount;
    }

    public void setTargetCount(Integer targetCount) 
    {
        this.targetCount = targetCount;
    }

    public Date getArchivedTime() 
    {
        return archivedTime;
    }

    public void setArchivedTime(Date archivedTime) 
    {
        this.archivedTime = archivedTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("taskId", getTaskId())
            .append("batchNo", getBatchNo())
            .append("itemCount", getItemCount())
            .append("status", getStatus())
            .append("workerId", getWorkerId())
            .append("workerPid", getWorkerPid())
            .append("lastHeartbeat", getLastHeartbeat())
            .append("totalAlerts", getTotalAlerts())
            .append("lastAlertTime", getLastAlertTime())
            .append("errorCount", getErrorCount())
            .append("lastError", getLastError())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}

