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

// 查询指定批次的Token列表
export function getTokensInBatch(batchId, query) {
  return request({
    url: `/crypto/batch/tokens/${batchId}`,
    method: 'get',
    params: query
  })
}

// 删除批次
export function deleteBatch(batchId, query) {
  return request({
    url: `/crypto/batch/${batchId}`,
    method: 'delete',
    params: query
  })
}

// 启用/停用批次
export function toggleBatchStatus(batchId, data) {
  return request({
    url: `/crypto/batch/status/${batchId}`,
    method: 'put',
    params: data
  })
}

// 获取批量监控统计信息
export function getBatchStats() {
  return request({
    url: '/crypto/batch/stats',
    method: 'get'
  })
}

