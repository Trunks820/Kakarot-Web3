import request from '@/utils/request'

/**
 * 获取首页统计数据
 */
export function getDashboardStats() {
  return request({
    url: '/dashboard/stats',
    method: 'get'
  })
}

/**
 * 获取今日新增Token数量
 */
export function getTodayNewTokens() {
  return request({
    url: '/dashboard/stats/today-tokens',
    method: 'get'
  })
}

/**
 * 获取监控中Token数量
 */
export function getMonitoringCount() {
  return request({
    url: '/dashboard/stats/monitoring-count',
    method: 'get'
  })
}

/**
 * 获取待处理事项统计
 */
export function getPendingStats() {
  return request({
    url: '/dashboard/stats/pending',
    method: 'get'
  })
}

