package com.ruoyi.crypto.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.crypto.domain.MonitorTask;
import com.ruoyi.crypto.mapper.MonitorTaskMapper;
import com.ruoyi.crypto.mapper.MonitorTaskConfigMapper;
import com.ruoyi.crypto.mapper.MonitorTaskTargetMapper;
import com.ruoyi.crypto.service.IMonitorTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 监控任务Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-09
 */
@Service
public class MonitorTaskServiceImpl implements IMonitorTaskService 
{
    @Autowired
    private MonitorTaskMapper monitorTaskMapper;

    @Autowired(required = false)
    private MonitorTaskConfigMapper monitorTaskConfigMapper;

    @Autowired(required = false)
    private MonitorTaskTargetMapper monitorTaskTargetMapper;

    /**
     * 查询监控任务
     * 
     * @param id 监控任务主键
     * @return 监控任务
     */
    @Override
    public MonitorTask selectMonitorTaskById(Long id)
    {
        return monitorTaskMapper.selectMonitorTaskById(id);
    }

    /**
     * 查询监控任务列表
     * 
     * @param monitorTask 监控任务
     * @return 监控任务
     */
    @Override
    public List<MonitorTask> selectMonitorTaskList(MonitorTask monitorTask)
    {
        return monitorTaskMapper.selectMonitorTaskList(monitorTask);
    }

    /**
     * 新增智能监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    @Override
    @Transactional
    public int insertSmartTask(MonitorTask monitorTask)
    {
        monitorTask.setTaskType("smart");
        monitorTask.setCreateBy(SecurityUtils.getUsername());
        monitorTask.setCreateTime(new Date());
        
        if (monitorTask.getStatus() == null) {
            monitorTask.setStatus(1); // 默认启用
        }
        
        int result = monitorTaskMapper.insertMonitorTask(monitorTask);
        
        // 关联配置
        if (result > 0 && monitorTask.getConfigIds() != null && !monitorTask.getConfigIds().isEmpty()) {
            if (monitorTaskConfigMapper != null) {
                for (Long configId : monitorTask.getConfigIds()) {
                    monitorTaskConfigMapper.insertTaskConfig(monitorTask.getId(), configId);
                }
            }
        }
        
        return result;
    }

    /**
     * 新增批量监控任务
     * 
     * @param monitorTask 监控任务
     * @param targetAddresses 目标地址列表
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBatchTask(MonitorTask monitorTask, List<String> targetAddresses)
    {
        monitorTask.setTaskType("batch");
        monitorTask.setCreateBy(SecurityUtils.getUsername());
        monitorTask.setCreateTime(new Date());
        
        if (monitorTask.getStatus() == null) {
            monitorTask.setStatus(1); // 默认启用
        }
        
        int result = monitorTaskMapper.insertMonitorTask(monitorTask);
        
        if (result > 0) {
            // 关联配置
            if (monitorTask.getConfigIds() != null && !monitorTask.getConfigIds().isEmpty() 
                && monitorTaskConfigMapper != null) {
                for (Long configId : monitorTask.getConfigIds()) {
                    monitorTaskConfigMapper.insertTaskConfig(monitorTask.getId(), configId);
                }
            }
            
            // 添加目标地址
            if (targetAddresses != null && !targetAddresses.isEmpty() 
                && monitorTaskTargetMapper != null) {
                for (String address : targetAddresses) {
                    monitorTaskTargetMapper.insertTaskTarget(monitorTask.getId(), 
                        monitorTask.getChainType(), address);
                }
            }
        }
        
        return result;
    }

    /**
     * 新增区块监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    @Override
    @Transactional
    public int insertBlockTask(MonitorTask monitorTask)
    {
        monitorTask.setTaskType("block");
        monitorTask.setCreateBy(SecurityUtils.getUsername());
        monitorTask.setCreateTime(new Date());
        
        if (monitorTask.getStatus() == null) {
            monitorTask.setStatus(1); // 默认启用
        }
        
        int result = monitorTaskMapper.insertMonitorTask(monitorTask);
        
        // 关联配置
        if (result > 0 && monitorTask.getConfigIds() != null && !monitorTask.getConfigIds().isEmpty()
            && monitorTaskConfigMapper != null) {
            for (Long configId : monitorTask.getConfigIds()) {
                monitorTaskConfigMapper.insertTaskConfig(monitorTask.getId(), configId);
            }
        }
        
        return result;
    }

    /**
     * 修改监控任务
     * 
     * @param monitorTask 监控任务
     * @return 结果
     */
    @Override
    public int updateMonitorTask(MonitorTask monitorTask)
    {
        monitorTask.setUpdateBy(SecurityUtils.getUsername());
        monitorTask.setUpdateTime(new Date());
        return monitorTaskMapper.updateMonitorTask(monitorTask);
    }

    /**
     * 批量删除监控任务
     * 
     * @param ids 需要删除的监控任务主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMonitorTaskByIds(Long[] ids)
    {
        // TODO: 删除关联的配置和目标
        if (monitorTaskConfigMapper != null) {
            for (Long id : ids) {
                monitorTaskConfigMapper.deleteTaskConfigByTaskId(id);
            }
        }
        if (monitorTaskTargetMapper != null) {
            for (Long id : ids) {
                monitorTaskTargetMapper.deleteTaskTargetByTaskId(id);
            }
        }
        return monitorTaskMapper.deleteMonitorTaskByIds(ids);
    }

    /**
     * 删除监控任务信息
     * 
     * @param id 监控任务主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteMonitorTaskById(Long id)
    {
        // 删除关联的配置和目标
        if (monitorTaskConfigMapper != null) {
            monitorTaskConfigMapper.deleteTaskConfigByTaskId(id);
        }
        if (monitorTaskTargetMapper != null) {
            monitorTaskTargetMapper.deleteTaskTargetByTaskId(id);
        }
        return monitorTaskMapper.deleteMonitorTaskById(id);
    }

    /**
     * 启动任务
     * 
     * @param id 任务ID
     * @return 结果
     */
    @Override
    public int startTask(Long id)
    {
        MonitorTask task = new MonitorTask();
        task.setId(id);
        task.setStatus(1);
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(new Date());
        return monitorTaskMapper.updateMonitorTask(task);
    }

    /**
     * 停止任务
     * 
     * @param id 任务ID
     * @return 结果
     */
    @Override
    public int stopTask(Long id)
    {
        MonitorTask task = new MonitorTask();
        task.setId(id);
        task.setStatus(0);
        task.setUpdateBy(SecurityUtils.getUsername());
        task.setUpdateTime(new Date());
        return monitorTaskMapper.updateMonitorTask(task);
    }

    /**
     * 根据链类型查询任务列表
     * 
     * @param chainType 链类型
     * @return 监控任务集合
     */
    @Override
    public List<MonitorTask> selectMonitorTaskByChainType(String chainType)
    {
        return monitorTaskMapper.selectMonitorTaskByChainType(chainType);
    }

    /**
     * 统计任务数量
     * 
     * @param taskType 任务类型
     * @param chainType 链类型
     * @param status 状态
     * @return 任务数量
     */
    @Override
    public int countMonitorTask(String taskType, String chainType, Integer status)
    {
        MonitorTask condition = new MonitorTask();
        condition.setTaskType(taskType);
        condition.setChainType(chainType);
        condition.setStatus(status);
        return monitorTaskMapper.countMonitorTask(condition);
    }
}

