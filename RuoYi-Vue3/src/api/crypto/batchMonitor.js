import request from '@/utils/request'

// 智能批量添加监控
export function smartBatchAdd(data) {
  return request({
    url: '/crypto/batch/smartAdd',
    method: 'post',
    data: data
  })
}

// 查询批量监控列表
export function listBatchMonitor(query) {
  return request({
    url: '/crypto/batch/list',
    method: 'get',
    params: query
  })
}

// 查询批量监控列表（别名）
export function getBatchList(query) {
  return listBatchMonitor(query)
}

// 查询指定批次的Token列表
export function getTokensInBatch(batchId, query) {
  return request({
    url: `/crypto/batch/tokens/${batchId}`,
    method: 'get',
    params: query
  })
}

// 查询指定批次的Token列表（统一参数）
export function getBatchTokens(params) {
  return request({
    url: '/crypto/batch/tokens',
    method: 'get',
    params: params
  })
}

// 删除批次
export function deleteBatch(params) {
  return request({
    url: '/crypto/batch/delete',
    method: 'delete',
    params: params
  })
}

// 启用/停用批次
export function toggleBatchStatus(data) {
  return request({
    url: '/crypto/batch/toggleStatus',
    method: 'put',
    data: data
  })
}

// 获取批量监控统计信息
export function getBatchStats() {
  return request({
    url: '/crypto/batch/stats',
    method: 'get'
  })
}

