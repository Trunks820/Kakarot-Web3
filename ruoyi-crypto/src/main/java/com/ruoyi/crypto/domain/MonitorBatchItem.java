package com.ruoyi.crypto.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 批次项对象 monitor_batch_item_v2
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@Data
public class MonitorBatchItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 批次ID */
    @Excel(name = "批次ID")
    private Long batchId;

    /** 任务ID */
    @Excel(name = "任务ID")
    private Long taskId;

    /** 目标ID（关联monitor_task_target_v2） */
    @Excel(name = "目标ID")
    private Long targetId;

    /** 合约地址/目标标识 */
    @Excel(name = "合约地址")
    private String ca;
    
    /** 批次内序号(0-98) */
    @Excel(name = "批次内序号")
    private Integer itemOrder;
    
    /** 批次编号 */
    @Excel(name = "批次编号")
    private Integer batchNo;
    
    /** Token名称 */
    @Excel(name = "Token名称")
    private String tokenName;
    
    /** Token符号 */
    @Excel(name = "Token符号")
    private String tokenSymbol;
    
    /** 市值(USD) */
    @Excel(name = "市值")
    private java.math.BigDecimal marketCap;

    /** 状态：pending-待处理, processing-处理中, completed-已完成, failed-失败 */
    @Excel(name = "状态")
    private String status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
