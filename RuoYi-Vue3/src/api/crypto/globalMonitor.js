import request from '@/utils/request'

// 查询链级全局监控配置列表
export function listGlobalMonitor(query) {
  return request({
    url: '/crypto/globalMonitor/list',
    method: 'get',
    params: query
  })
}

// 查询链级全局监控配置详细
export function getGlobalMonitor(id) {
  return request({
    url: '/crypto/globalMonitor/' + id,
    method: 'get'
  })
}

// 根据链类型和市场类型获取配置
export function getGlobalMonitorByChain(chainType, marketType = 'external') {
  return request({
    url: '/crypto/globalMonitor/chain/' + chainType,
    method: 'get',
    params: { marketType }
  })
}

// 新增链级全局监控配置
export function addGlobalMonitor(data) {
  return request({
    url: '/crypto/globalMonitor',
    method: 'post',
    data: data
  })
}

// 修改链级全局监控配置
export function updateGlobalMonitor(data) {
  return request({
    url: '/crypto/globalMonitor',
    method: 'put',
    data: data
  })
}

// 保存或更新链级全局监控配置
export function saveOrUpdateGlobalMonitor(data) {
  return request({
    url: '/crypto/globalMonitor/saveOrUpdate',
    method: 'post',
    data: data
  })
}

// 切换配置状态
export function changeGlobalMonitorStatus(id, status) {
  return request({
    url: `/crypto/globalMonitor/changeStatus/${id}/${status}`,
    method: 'put'
  })
}

// 删除链级全局监控配置
export function delGlobalMonitor(ids) {
  return request({
    url: '/crypto/globalMonitor/' + ids,
    method: 'delete'
  })
}

// 获取今日预警数量
export function getTodayAlertCount() {
  return request({
    url: '/crypto/monitorAlert/todayCount',
    method: 'get'
  })
}

// 获取配置日志
export function getGlobalMonitorLogs(limit = 20) {
  return request({
    url: '/crypto/globalMonitor/logs',
    method: 'get',
    params: { limit }
  })
}

