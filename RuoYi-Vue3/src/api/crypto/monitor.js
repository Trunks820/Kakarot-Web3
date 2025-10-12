import request from '@/utils/request'

// 查询Token监控配置列表
export function listMonitorConfig(query) {
  return request({
    url: '/crypto/tokenMonitor/list',
    method: 'get',
    params: query
  })
}

// 别名：兼容旧的函数名
export const listCryptoMonitorConfig = listMonitorConfig

// 查询Token监控配置详情
export function getMonitorConfig(id) {
  return request({
    url: '/crypto/tokenMonitor/' + id,
    method: 'get'
  })
}

// 别名：兼容旧的函数名
export const getCryptoMonitorConfig = getMonitorConfig

// 根据CA查询监控配置
export function getMonitorConfigByCa(ca) {
  return request({
    url: '/crypto/tokenMonitor/ca/' + ca,
    method: 'get'
  })
}

// 新增Token监控配置
export function addMonitorConfig(data) {
  return request({
    url: '/crypto/tokenMonitor',
    method: 'post',
    data: data
  })
}

// 别名：兼容旧的函数名
export const addCryptoMonitorConfig = addMonitorConfig

// 修改Token监控配置
export function updateMonitorConfig(data) {
  return request({
    url: '/crypto/tokenMonitor',
    method: 'put',
    data: data
  })
}

// 别名：兼容旧的函数名
export const updateCryptoMonitorConfig = updateMonitorConfig

// 删除Token监控配置
export function delMonitorConfig(ids) {
  return request({
    url: '/crypto/tokenMonitor/' + ids,
    method: 'delete'
  })
}

// 别名：兼容旧的函数名
export const delCryptoMonitorConfig = delMonitorConfig

// 启用监控
export function enableMonitor(id) {
  return request({
    url: '/crypto/tokenMonitor/enable/' + id,
    method: 'put'
  })
}

// 停用监控
export function disableMonitor(id) {
  return request({
    url: '/crypto/tokenMonitor/disable/' + id,
    method: 'put'
  })
}

// 修改监控状态
export function changeCryptoMonitorStatus(id, status) {
  return request({
    url: '/crypto/tokenMonitor/changeStatus',
    method: 'put',
    data: { id, status }
  })
}

// 测试监控通知
export function testCryptoMonitorNotify(id) {
  return request({
    url: '/crypto/tokenMonitor/testNotify/' + id,
    method: 'post'
  })
}

// 批量启用监控
export function batchEnableMonitor(ids) {
  return request({
    url: '/crypto/tokenMonitor/batchEnable',
    method: 'put',
    data: ids
  })
}

// 批量停用监控
export function batchDisableMonitor(ids) {
  return request({
    url: '/crypto/tokenMonitor/batchDisable',
    method: 'put',
    data: ids
  })
}
