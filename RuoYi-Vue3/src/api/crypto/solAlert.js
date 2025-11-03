import request from '@/utils/request'

/**
 * 获取最近的 SOL 告警记录
 * @param {Number} limit 
 * @returns 
 */
export function getRecentSolAlerts(limit = 10) {
  return request({
    url: '/crypto/solAlert/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 查询 SOL 告警记录列表
 * @param {Object} query 
 * @returns 
 */
export function listSolAlert(query) {
  return request({
    url: '/crypto/solAlert/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取 SOL 告警记录详情
 * @param {Number} id 
 * @returns 
 */
export function getSolAlert(id) {
  return request({
    url: '/crypto/solAlert/' + id,
    method: 'get'
  })
}

/**
 * 删除 SOL 告警记录
 * @param {Number} id 
 * @returns 
 */
export function delSolAlert(id) {
  return request({
    url: '/crypto/solAlert/' + id,
    method: 'delete'
  })
}

