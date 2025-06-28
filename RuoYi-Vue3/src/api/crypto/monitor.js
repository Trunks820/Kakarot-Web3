import request from '@/utils/request'

// 查询监控配置列表
export function listCryptoMonitorConfig(query) {
  return request({
    url: '/crypto/monitor/list',
    method: 'get',
    params: query
  })
}

// 查询监控配置详细
export function getCryptoMonitorConfig(id) {
  return request({
    url: '/crypto/monitor/' + id,
    method: 'get'
  })
}

// 新增监控配置
export function addCryptoMonitorConfig(data) {
  return request({
    url: '/crypto/monitor',
    method: 'post',
    data: data
  })
}

// 修改监控配置
export function updateCryptoMonitorConfig(data) {
  return request({
    url: '/crypto/monitor',
    method: 'put',
    data: data
  })
}

// 删除监控配置
export function delCryptoMonitorConfig(id) {
  return request({
    url: '/crypto/monitor/' + id,
    method: 'delete'
  })
}

// 修改监控配置状态
export function changeCryptoMonitorStatus(id, status) {
  const data = {
    id,
    status
  }
  return request({
    url: '/crypto/monitor/changeStatus',
    method: 'put',
    data: data
  })
}

// 批量启用监控配置
export function batchEnableCryptoMonitor(ids) {
  return request({
    url: '/crypto/monitor/batchEnable',
    method: 'put',
    data: ids
  })
}

// 批量停用监控配置
export function batchDisableCryptoMonitor(ids) {
  return request({
    url: '/crypto/monitor/batchDisable',
    method: 'put',
    data: ids
  })
}

// 测试通知
export function testCryptoMonitorNotify(id) {
  return request({
    url: '/crypto/monitor/testNotify/' + id,
    method: 'post'
  })
}

// 获取监控统计信息
export function getCryptoMonitorStats() {
  return request({
    url: '/crypto/monitor/stats',
    method: 'get'
  })
} 