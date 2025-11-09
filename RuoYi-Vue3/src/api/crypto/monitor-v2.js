import request from '@/utils/request'

// ========================================
// 监控配置 API (Monitor Config)
// ========================================

/**
 * 获取配置统计信息
 */
export function getConfigStats() {
  return request({
    url: '/crypto/monitor-v2/config/stats',
    method: 'get'
  })
}

/**
 * 查询配置列表
 */
export function listConfig(query) {
  return request({
    url: '/crypto/monitor-v2/config/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取配置详情
 */
export function getConfig(id) {
  return request({
    url: `/crypto/monitor-v2/config/${id}`,
    method: 'get'
  })
}

/**
 * 新增配置
 */
export function addConfig(data) {
  return request({
    url: '/crypto/monitor-v2/config',
    method: 'post',
    data: data
  })
}

/**
 * 修改配置
 */
export function updateConfig(data) {
  return request({
    url: '/crypto/monitor-v2/config',
    method: 'put',
    data: data
  })
}

/**
 * 删除配置
 */
export function delConfig(ids) {
  return request({
    url: `/crypto/monitor-v2/config/${ids}`,
    method: 'delete'
  })
}

/**
 * 启用/停用配置
 */
export function changeConfigStatus(id, status) {
  return request({
    url: `/crypto/monitor-v2/config/${id}/status`,
    method: 'put',
    data: { status }
  })
}

/**
 * 复制配置
 */
export function copyConfig(id) {
  return request({
    url: `/crypto/monitor-v2/config/${id}/copy`,
    method: 'post'
  })
}

// ========================================
// 监控任务 API (Monitor Task)
// ========================================

/**
 * 获取任务统计信息
 */
export function getTaskStats() {
  return request({
    url: '/crypto/monitor-v2/task/stats',
    method: 'get'
  })
}

/**
 * 查询任务列表
 */
export function listTask(query) {
  return request({
    url: '/crypto/monitor-v2/task/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取任务详情
 */
export function getTask(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}`,
    method: 'get'
  })
}

/**
 * 获取任务的完整信息（包含配置和目标）
 */
export function getTaskDetail(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/detail`,
    method: 'get'
  })
}

/**
 * 新增任务（通用）
 */
export function addTask(data) {
  return request({
    url: '/crypto/monitor-v2/task',
    method: 'post',
    data: data
  })
}

/**
 * 新增智能监控任务
 */
export function addSmartTask(data) {
  return request({
    url: '/crypto/monitor-v2/task/smart',
    method: 'post',
    data: data
  })
}

/**
 * 新增批量监控任务
 */
export function addBatchTask(data) {
  return request({
    url: '/crypto/monitor-v2/task/batch',
    method: 'post',
    data: data
  })
}

/**
 * 新增区块监控任务
 */
export function addBlockTask(data) {
  return request({
    url: '/crypto/monitor-v2/task/block',
    method: 'post',
    data: data
  })
}

/**
 * 修改任务
 */
export function updateTask(data) {
  return request({
    url: '/crypto/monitor-v2/task',
    method: 'put',
    data: data
  })
}

/**
 * 删除任务
 */
export function delTask(ids) {
  return request({
    url: `/crypto/monitor-v2/task/${ids}`,
    method: 'delete'
  })
}

/**
 * 启动任务
 */
export function startTask(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/start`,
    method: 'put'
  })
}

/**
 * 停止任务
 */
export function stopTask(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/stop`,
    method: 'put'
  })
}

/**
 * 暂停任务
 */
export function pauseTask(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/pause`,
    method: 'put'
  })
}

/**
 * 恢复任务
 */
export function resumeTask(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/resume`,
    method: 'put'
  })
}

/**
 * 手动同步智能监控目标
 */
export function syncTaskTargets(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/sync-targets`,
    method: 'post'
  })
}

/**
 * 重新分配批次
 */
export function reallocateBatches(id) {
  return request({
    url: `/crypto/monitor-v2/task/${id}/reallocate`,
    method: 'post'
  })
}

/**
 * 获取任务的目标列表
 */
export function listTaskTargets(taskId, query) {
  return request({
    url: `/crypto/monitor-v2/task/${taskId}/targets`,
    method: 'get',
    params: query
  })
}

/**
 * 批量添加目标
 */
export function addTaskTargets(taskId, targets) {
  return request({
    url: `/crypto/monitor-v2/task/${taskId}/targets`,
    method: 'post',
    data: targets
  })
}

/**
 * 批量删除目标
 */
export function delTaskTargets(taskId, targetIds) {
  return request({
    url: `/crypto/monitor-v2/task/${taskId}/targets/${targetIds}`,
    method: 'delete'
  })
}

// ========================================
// 批次执行 API (Monitor Batch)
// ========================================

/**
 * 获取批次统计信息
 */
export function getBatchStats() {
  return request({
    url: '/crypto/monitor-v2/batch/stats',
    method: 'get'
  })
}

/**
 * 查询批次列表
 */
export function listBatch(query) {
  return request({
    url: '/crypto/monitor-v2/batch/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取批次详情
 */
export function getBatch(id) {
  return request({
    url: `/crypto/monitor-v2/batch/${id}`,
    method: 'get'
  })
}

/**
 * 获取批次项列表
 */
export function listBatchItems(batchId, query) {
  return request({
    url: `/crypto/monitor-v2/batch/${batchId}/items`,
    method: 'get',
    params: query
  })
}

/**
 * 重启批次
 */
export function restartBatch(id) {
  return request({
    url: `/crypto/monitor-v2/batch/${id}/restart`,
    method: 'post'
  })
}

/**
 * 获取批次心跳状态
 */
export function getBatchHeartbeat(id) {
  return request({
    url: `/crypto/monitor-v2/batch/${id}/heartbeat`,
    method: 'get'
  })
}

/**
 * 获取批次实时统计（WebSocket推送）
 */
export function subscribeBatchStats(callback) {
  // WebSocket实现
  const ws = new WebSocket(`ws://${window.location.host}/ws/monitor/batch/stats`)
  
  ws.onmessage = (event) => {
    const data = JSON.parse(event.data)
    callback(data)
  }
  
  ws.onerror = (error) => {
    console.error('WebSocket error:', error)
  }
  
  return ws
}

// ========================================
// 告警日志 API (Monitor Alert)
// ========================================

/**
 * 查询告警列表
 */
export function listAlert(query) {
  return request({
    url: '/crypto/monitor-v2/alert/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取告警详情
 */
export function getAlert(id) {
  return request({
    url: `/crypto/monitor-v2/alert/${id}`,
    method: 'get'
  })
}

/**
 * 标记告警已处理
 */
export function markAlertHandled(id) {
  return request({
    url: `/crypto/monitor-v2/alert/${id}/handle`,
    method: 'put'
  })
}

/**
 * 批量标记告警已处理
 */
export function batchMarkAlertHandled(ids) {
  return request({
    url: '/crypto/monitor-v2/alert/batch-handle',
    method: 'put',
    data: ids
  })
}

/**
 * 获取告警统计
 */
export function getAlertStats(query) {
  return request({
    url: '/crypto/monitor-v2/alert/stats',
    method: 'get',
    params: query
  })
}

// ========================================
// 系统管理 API
// ========================================

/**
 * 获取系统配置
 */
export function getSystemConfig() {
  return request({
    url: '/crypto/monitor-v2/system/config',
    method: 'get'
  })
}

/**
 * 更新系统配置
 */
export function updateSystemConfig(data) {
  return request({
    url: '/crypto/monitor-v2/system/config',
    method: 'put',
    data: data
  })
}

/**
 * 获取监控健康状态
 */
export function getHealthStatus() {
  return request({
    url: '/crypto/monitor-v2/system/health',
    method: 'get'
  })
}

/**
 * 清理过期数据
 */
export function cleanupExpiredData(days) {
  return request({
    url: '/crypto/monitor-v2/system/cleanup',
    method: 'post',
    data: { days }
  })
}

// ========================================
// 工具函数
// ========================================

/**
 * 预测智能监控匹配的Token数量
 */
export function predictTokenCount(data) {
  return request({
    url: '/crypto/monitor-v2/tool/predict-token-count',
    method: 'post',
    data: data
  })
}

/**
 * 验证CA地址格式
 */
export function validateAddresses(data) {
  return request({
    url: '/crypto/monitor-v2/tool/validate-addresses',
    method: 'post',
    data: data
  })
}

/**
 * 获取可用的配置模板列表
 */
export function listConfigTemplates(chainType) {
  return request({
    url: '/crypto/monitor-v2/tool/config-templates',
    method: 'get',
    params: { chainType }
  })
}

