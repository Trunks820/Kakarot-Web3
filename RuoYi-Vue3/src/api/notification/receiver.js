/**
 * 通知接收API
 * 供Python脚本调用
 */

/**
 * 接收通知的Mock API
 * 实际不走HTTP，直接调用全局方法
 */
export function receiveNotification(data, token) {
  if (typeof window.__receiveNotification === 'function') {
    return window.__receiveNotification(data, token)
  }
  
  console.error('通知接收器未初始化')
  return { success: false, error: 'Receiver not initialized' }
}

/**
 * 测试通知
 * 用于前端调试
 */
export function testNotification() {
  const testData = {
    module: 'token-monitor',
    moduleName: 'Token监控',
    type: 'alert',
    title: 'DEAR触发涨幅预警',
    content: 'Token DEAR 在过去5分钟内涨幅达到 +15.2%，超过设定阈值 10%',
    actionUrl: '/crypto/token-monitor?ca=HM15KRP',
    extraData: {
      ca: 'HM15KRP',
      symbol: 'DEAR',
      change: 15.2,
      threshold: 10
    }
  }
  
  return receiveNotification(testData, 'your-secret-token-here-change-it')
}

