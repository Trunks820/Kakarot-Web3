package com.ruoyi.crypto.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 监控任务目标对象 monitor_task_target_v2
 * 
 * @author ruoyi
 * @date 2025-11-11
 */
@Data
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

    @Excel(name = "合约名称")
    private String tokenName;

    @Excel(name = "名称缩写")
    private String tokenSymbol;

    @Excel(name = "合约市值")
    private BigDecimal marketCap;

    /** 状态：active-活跃, removed-已移除 */
    @Excel(name = "状态")
    private String status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

}

