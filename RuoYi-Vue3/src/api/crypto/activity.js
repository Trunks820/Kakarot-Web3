import request from '@/utils/request'

// 获取钱包交易活动
export function getWalletActivity(address, chainType) {
    return request({
        url: '/crypto/api/getWalletActivity',
        method: 'get',
        params: {
            address,
            chainType
        }
    })
}

// 关注钱包
export function walletFollow(address, chainType) {
    return request({
        url: '/crypto/api/walletFollow',
        method: 'get',
        params: {
            address,
            chainType
        }
    })
}

// 取消关注钱包
export function walletUnfollow(address, chainType) {
    return request({
        url: '/crypto/api/walletUnfollow',
        method: 'get',
        params: {
            address,
            chainType
        }
    })
}