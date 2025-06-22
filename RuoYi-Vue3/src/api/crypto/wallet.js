import request from '@/utils/request'

// 查询钱包列表
export function listWallets(query) {
    return request({
        url: '/crypto/wallet/list',
        method: 'get',
        params: query
    })
}

// 查询wallet详情
export function getWallet(id) {
    return request({
        url: '/crypto/wallet/' + id,
        method: 'get'
    })
}

// 新增钱包
export function addWallet(data) {
    return request({
        url: '/crypto/wallet',
        method: 'post',
        data: data
    })
}

// 修改钱包信息
export function updateWallet(data) {
    return request({
        url: '/crypto/wallet',
        method: 'put',
        data: data
    })
}

// 删除钱包
export function delWallet(ids) {
    return request({
        url: '/crypto/wallet/' + ids,
        method: 'delete'
    })
}

