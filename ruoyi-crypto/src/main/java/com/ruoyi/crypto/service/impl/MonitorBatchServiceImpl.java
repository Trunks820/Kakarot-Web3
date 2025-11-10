package com.ruoyi.crypto.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.crypto.mapper.MonitorBatchMapper;
import com.ruoyi.crypto.domain.MonitorBatch;
import com.ruoyi.crypto.domain.MonitorBatchItem;
import com.ruoyi.crypto.service.IMonitorBatchService;

/**
 * 批次管理Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-11-10
 */
@Service
public class MonitorBatchServiceImpl implements IMonitorBatchService 
{
    @Autowired
    private MonitorBatchMapper monitorBatchMapper;

    /**
     * 获取批次统计信息
     * 
     * @return 批次统计数据
     */
    @Override
    public Map<String, Object> getBatchStats()
    {
        Map<String, Object> stats = new HashMap<>();
        
        // 查询总数
        int total = monitorBatchMapper.countTotal();
        stats.put("total", total);
        
        // 查询各状态数量
        int running = monitorBatchMapper.countByStatus("running");
        int paused = monitorBatchMapper.countByStatus("paused");
        int error = monitorBatchMapper.countByStatus("error");
        int pending = monitorBatchMapper.countByStatus("pending");
        
        stats.put("running", running);
        stats.put("paused", paused);
        stats.put("error", error);
        stats.put("pending", pending);
        
        // 查询目标总数
        int targetCount = monitorBatchMapper.countTotalTargets();
        stats.put("targetCount", targetCount);
        
        // 查询心跳正常数量（最近5分钟有心跳）
        int heartbeatNormal = monitorBatchMapper.countHeartbeatNormal();
        stats.put("heartbeatNormal", heartbeatNormal);
        
        // 心跳超时数量
        int heartbeatTimeout = total - heartbeatNormal;
        stats.put("heartbeatTimeout", heartbeatTimeout);
        
        // 平均延迟（如果有的话）
        stats.put("avgLatency", 0);
        
        // 异常批次列表
        List<Map<String, Object>> errorBatches = monitorBatchMapper.selectErrorBatches();
        stats.put("errorBatches", errorBatches);
        
        // 最后更新时间
        stats.put("lastUpdate", DateUtils.getNowDate());
        
        return stats;
    }

    /**
     * 查询批次
     * 
     * @param id 批次主键
     * @return 批次
     */
    @Override
    public MonitorBatch selectMonitorBatchById(Long id)
    {
        return monitorBatchMapper.selectMonitorBatchById(id);
    }

    /**
     * 查询批次列表
     * 
     * @param monitorBatch 批次
     * @return 批次
     */
    @Override
    public List<MonitorBatch> selectMonitorBatchList(MonitorBatch monitorBatch)
    {
        return monitorBatchMapper.selectMonitorBatchList(monitorBatch);
    }

    /**
     * 查询批次项列表
     * 
     * @param batchId 批次ID
     * @return 批次项集合
     */
    @Override
    public List<MonitorBatchItem> selectBatchItems(Long batchId)
    {
        return monitorBatchMapper.selectBatchItems(batchId);
    }

    /**
     * 新增批次
     * 
     * @param monitorBatch 批次
     * @return 结果
     */
    @Override
    public int insertMonitorBatch(MonitorBatch monitorBatch)
    {
        monitorBatch.setCreateTime(DateUtils.getNowDate());
        return monitorBatchMapper.insertMonitorBatch(monitorBatch);
    }

    /**
     * 修改批次
     * 
     * @param monitorBatch 批次
     * @return 结果
     */
    @Override
    public int updateMonitorBatch(MonitorBatch monitorBatch)
    {
        monitorBatch.setUpdateTime(DateUtils.getNowDate());
        return monitorBatchMapper.updateMonitorBatch(monitorBatch);
    }

    /**
     * 批量删除批次
     * 
     * @param ids 需要删除的批次主键
     * @return 结果
     */
    @Override
    public int deleteMonitorBatchByIds(Long[] ids)
    {
        return monitorBatchMapper.deleteMonitorBatchByIds(ids);
    }

    /**
     * 删除批次信息
     * 
     * @param id 批次主键
     * @return 结果
     */
    @Override
    public int deleteMonitorBatchById(Long id)
    {
        return monitorBatchMapper.deleteMonitorBatchById(id);
    }

    /**
     * 重启批次
     * 
     * @param id 批次主键
     * @return 结果
     */
    @Override
    public int restartBatch(Long id)
    {
        MonitorBatch batch = new MonitorBatch();
        batch.setId(id);
        batch.setStatus("pending");
        batch.setWorkerId(null);
        batch.setWorkerPid(null);
        batch.setLastHeartbeat(null);
        batch.setErrorCount(0);
        batch.setLastError(null);
        batch.setUpdateTime(DateUtils.getNowDate());
        
        return monitorBatchMapper.updateMonitorBatch(batch);
    }

    /**
     * 获取批次心跳状态
     * 
     * @param id 批次主键
     * @return 心跳信息
     */
    @Override
    public Map<String, Object> getBatchHeartbeat(Long id)
    {
        MonitorBatch batch = monitorBatchMapper.selectMonitorBatchById(id);
        Map<String, Object> heartbeat = new HashMap<>();
        
        if (batch != null) {
            heartbeat.put("batchId", batch.getId());
            heartbeat.put("status", batch.getStatus());
            heartbeat.put("workerId", batch.getWorkerId());
            heartbeat.put("workerPid", batch.getWorkerPid());
            heartbeat.put("lastHeartbeat", batch.getLastHeartbeat());
            
            // 计算心跳延迟
            if (batch.getLastHeartbeat() != null) {
                long delay = System.currentTimeMillis() - batch.getLastHeartbeat().getTime();
                heartbeat.put("heartbeatDelay", delay);
                heartbeat.put("isTimeout", delay > 300000); // 5分钟超时
            } else {
                heartbeat.put("heartbeatDelay", -1);
                heartbeat.put("isTimeout", true);
            }
        }
        
        return heartbeat;
    }

    /**
     * 更新批次心跳
     * 
     * @param id 批次主键
     * @param data 心跳数据
     * @return 结果
     */
    @Override
    public int updateBatchHeartbeat(Long id, Map<String, Object> data)
    {
        MonitorBatch batch = new MonitorBatch();
        batch.setId(id);
        batch.setLastHeartbeat(DateUtils.getNowDate());
        
        // 更新状态
        if (data.containsKey("status")) {
            batch.setStatus(data.get("status").toString());
        }
        
        // 更新worker信息
        if (data.containsKey("workerId")) {
            batch.setWorkerId(data.get("workerId").toString());
        }
        
        if (data.containsKey("workerPid")) {
            batch.setWorkerPid(Integer.parseInt(data.get("workerPid").toString()));
        }
        
        // 更新错误信息
        if (data.containsKey("error")) {
            batch.setErrorCount(batch.getErrorCount() != null ? batch.getErrorCount() + 1 : 1);
            batch.setLastError(data.get("error").toString());
        }
        
        return monitorBatchMapper.updateMonitorBatch(batch);
    }
}

