import request from '@/utils/request'

// 查询代币列表
export function listCoins(query) {
  return request({
    url: '/crypto/coin/list',
    method: 'get',
    params: query
  })
}

// 查询代币详情
export function getCoin(coinId) {
  return request({
    url: '/crypto/coin/' + coinId,
    method: 'get'
  })
}

// 新增代币
export function addCoin(data) {
  return request({
    url: '/crypto/coin',
    method: 'post',
    data: data
  })
}

// 修改代币信息
export function updateCoin(data) {
  return request({
    url: '/crypto/coin',
    method: 'put',
    data: data
  })
}

// 删除代币
export function delCoin(coinIds) {
  return request({
    url: '/crypto/coin/' + coinIds,
    method: 'delete'
  })
}

// 导出代币
export function exportCoin(query) {
  return request({
    url: '/crypto/coin/export',
    method: 'get',
    params: query
  })
}

// 获取币种统计数据
export function getCoinStatistics() {
  return request({
    url: '/crypto/statistics',
    method: 'get'
  })
}

// 获取最近查询记录
export function getRecentQueries(params) {
  return request({
    url: '/crypto/records/recent',
    method: 'get',
    params: params
  })
}

// 获取排行榜数据
export function getRankingData(type) {
  return request({
    url: '/crypto/ranking/' + type,
    method: 'get'
  })
}

// 获取监控配置列表
export function getMonitorConfigs(query) {
  return request({
    url: '/crypto/monitor/list',
    method: 'get',
    params: query
  })
}

// 获取每日活动统计数据
export function getDailyActivityStats() {
  return request({
    url: '/crypto/record/getDailyActivityStats',
    method: 'get'
  })
}

// 获取每日活动统计数据
export function getHotCaByWechat() {
  return request({
    url: '/crypto/record/getHotCaByWechat',
    method: 'get'
  })
}

// 获取每日活动统计数据
export function getHotCaByTelegram() {
  return request({
    url: '/crypto/record/getHotCaByTelegram',
    method: 'get'
  })
}

// 获取机器人状态
export function getTgBotStatus() {
  return request({
    url: '/crypto/record/getTgBotStatus',
    method: 'get'
  })
}

// 获取系统信息
export function getSystemInfo() {
  return request({
    url: '/crypto/system/info',
    method: 'get'
  })
}