import request from '@/utils/request'

// 查询历史播报列表
export function listAlertLog(query) {
  return request({
    url: '/crypto/block/list',
    method: 'get',
    params: query
  })
}

// 查询历史播报详细
export function getAlertLog(id) {
  return request({
    url: '/crypto/block/' + id,
    method: 'get'
  })
}

// 删除历史播报
export function delAlertLog(id) {
  return request({
    url: '/crypto/block/' + id,
    method: 'delete'
  })
}

// 导出历史播报
export function exportAlertLog(query) {
  return request({
    url: '/crypto/block/export',
    method: 'get',
    params: query
  })
}

