import request from '@/utils/request'

// 查询Token发射历史列表
export function listToken(query) {
  return request({
    url: '/crypto/token/list',
    method: 'get',
    params: query
  })
}

// 查询Token发射历史详情
export function getToken(ca) {
  return request({
    url: '/crypto/token/' + ca,
    method: 'get'
  })
}

// 查询Token统计数据
export function getTokenStats() {
  return request({
    url: '/crypto/token/stats',
    method: 'get'
  })
}

// 导出Token发射历史
export function exportToken(query) {
  return request({
    url: '/crypto/token/export',
    method: 'get',
    params: query
  })
}

// 关注Twitter
export function followTwitter(twitterUrl) {
  return request({
    url: '/crypto/token/follow',
    method: 'post',
    data: { twitterUrl }
  })
}

// 取消关注Twitter
export function unfollowTwitter(twitterUrl) {
  return request({
    url: '/crypto/token/unfollow',
    method: 'post',
    data: { twitterUrl }
  })
}

// 批量关注Twitter
export function batchFollowTwitter(twitterUrls) {
  return request({
    url: '/crypto/token/batchFollow',
    method: 'post',
    data: { twitterUrls }
  })
}

// 批量取消关注Twitter
export function batchUnfollowTwitter(twitterUrls) {
  return request({
    url: '/crypto/token/batchUnfollow',
    method: 'post',
    data: { twitterUrls }
  })
}

// 批量获取Twitter账号信息
export function getTwitterAccounts(twitterUrls) {
  return request({
    url: '/crypto/token/getTwitterAccounts',
    method: 'post',
    data: { twitterUrls }
  })
}

// 获取Twitter推送配置
export function getPushConfig(twitterUrl) {
  return request({
    url: '/crypto/token/getPushConfig',
    method: 'get',
    params: { twitterUrl }
  })
}

// 更新Twitter推送配置
export function updatePushConfig(data) {
  return request({
    url: '/crypto/token/updatePushConfig',
    method: 'post',
    data: data
  })
}
