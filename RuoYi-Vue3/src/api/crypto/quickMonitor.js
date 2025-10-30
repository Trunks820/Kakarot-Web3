import request from '@/utils/request'

// 查询配置列表
export function listQuickMonitor(query) {
  return request({
    url: '/crypto/quickMonitor/list',
    method: 'get',
    params: query
  })
}

// 根据链类型获取配置
export function getQuickMonitorByChain(chainType) {
  return request({
    url: `/crypto/quickMonitor/chain/${chainType}`,
    method: 'get'
  })
}

// 查询配置详细
export function getQuickMonitor(id) {
  return request({
    url: '/crypto/quickMonitor/' + id,
    method: 'get'
  })
}

// 新增配置
export function addQuickMonitor(data) {
  return request({
    url: '/crypto/quickMonitor',
    method: 'post',
    data: data
  })
}

// 修改配置
export function updateQuickMonitor(data) {
  return request({
    url: '/crypto/quickMonitor',
    method: 'put',
    data: data
  })
}

// 删除配置
export function delQuickMonitor(ids) {
  return request({
    url: '/crypto/quickMonitor/' + ids,
    method: 'delete'
  })
}

// 批量保存配置
export function batchSaveQuickMonitor(chainType, templates) {
  return request({
    url: '/crypto/quickMonitor/batchSave',
    method: 'post',
    data: {
      chainType: chainType,
      templates: templates
    }
  })
}

// 获取配置统计（包含Token数量）
export function getQuickMonitorStats(chainType) {
  return request({
    url: `/crypto/quickMonitor/stats/${chainType}`,
    method: 'get'
  })
}

// 预测配置的Token匹配数量（用于编辑时实时预测）
export function predictTokenCounts(marketCapList) {
  return request({
    url: '/crypto/quickMonitor/predict',
    method: 'post',
    data: {
      marketCapList: marketCapList
    }
  })
}

