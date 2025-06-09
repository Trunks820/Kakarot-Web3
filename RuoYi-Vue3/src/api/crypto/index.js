import request from '@/utils/request'

// 查询代币信息
export function tokenInfo(address) {
  return request({
    url: '/crypto/api/tokenInfo',
    method: 'get',
    params: {
      address: address
    }
  })
}

// 查询代币安全信息
export function securityInfo(address) {
  return request({
    url: '/crypto/api/securityInfo',
    method: 'get',
    params: {
      address: address
    }
  })
}

// 查询代币钱包信息
export function getWalletInfo(address) {
  return request({
    url: '/crypto/api/getWalletInfo',
    method: 'get',
    params: {
      address: address
    }
  })
}

// 查询主流代币
export function getTopCoin(coin) {
  return request({
    url: '/crypto/api/getTopCoin',
    method: 'get',
    params: {
      coin: coin
    }
  })
}

// 保存监控配置
export function saveCryptoMonitorConfig(data) {
  return request({
    url: '/crypto/monitor/save',
    method: 'post',
    data: data
  })
}

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

// 启用/停用监控配置
export function changeMonitorStatus(id, status) {
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

// 批量删除监控配置
export function delCryptoMonitorConfigs(ids) {
  return request({
    url: '/crypto/monitor/' + ids,
    method: 'delete'
  })
}

// 检查代币是否已被监控
export function checkTokenMonitored(contractAddress) {
  return request({
    url: '/crypto/monitor/check',
    method: 'get',
    params: {
      contractAddress: contractAddress
    }
  })
}



