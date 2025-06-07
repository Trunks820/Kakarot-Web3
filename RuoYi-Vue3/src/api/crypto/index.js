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

