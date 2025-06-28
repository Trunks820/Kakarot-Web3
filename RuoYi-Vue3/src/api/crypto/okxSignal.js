import request from '@/utils/request'

// 获取OKX信号列表
export function getOkxSignalList(params) {
  return request({
    url: '/crypto/okxSignal/list',
    method: 'get',
    params
  })
}

// 获取信号统计数据
export function getOkxSignalStatistics() {
  return request({
    url: '/crypto/okxSignal/statistics',
    method: 'get'
  })
} 