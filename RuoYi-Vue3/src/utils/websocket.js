/**
 * WebSocket 实时通知客户端
 */

import { ElNotification } from 'element-plus'

class NotificationWebSocket {
  constructor(options = {}) {
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = options.maxReconnectAttempts || 5
    this.reconnectInterval = options.reconnectInterval || 5000 // 5秒
    this.heartbeatInterval = options.heartbeatInterval || 30000 // 30秒心跳
    this.autoReconnect = options.autoReconnect !== false // 默认开启自动重连
    this.url = ''
    this.token = options.token || ''
    this.onMessageCallback = null
    this.isManualClose = false
  }

  /**
   * 连接 WebSocket
   * @param {string} token - 用户身份令牌（可选，如果构造时已提供）
   * @param {function} onMessage - 收到消息的回调函数（可选）
   */
  connect(token, onMessage) {
    // 如果传入了 token，则使用传入的；否则使用构造时的 token
    if (token) {
      this.token = token
    }
    
    if (!this.token) {
      console.error('WebSocket 连接失败：token 为空')
      return
    }

    if (onMessage) {
      this.onMessageCallback = onMessage
    }
    
    this.isManualClose = false

    // 构建 WebSocket URL（开发环境和生产环境自动适配）
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    
    // 开发环境：直连后端 8080
    // 生产环境：通过 Nginx 代理
    let host
    if (import.meta.env.DEV) {
      // 开发环境直连后端
      host = 'localhost:8080'
    } else if (import.meta.env.VITE_APP_BASE_API) {
      // 生产环境使用配置的 API 地址
      host = import.meta.env.VITE_APP_BASE_API.replace(/^https?:\/\//, '').replace(/\/$/, '')
    } else {
      // 回退到当前 host
      host = window.location.host
    }
    
    this.url = `${protocol}//${host}/ws/notification/${this.token}`

    console.log('正在连接 WebSocket:', this.url)
    this._createConnection()
  }

  /**
   * 创建 WebSocket 连接
   */
  _createConnection() {
    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = () => {
        console.log('✅ WebSocket 连接成功')
        this.reconnectAttempts = 0
        this._startHeartbeat()

        // 申请浏览器通知权限
        if (Notification.permission === 'default') {
          Notification.requestPermission()
        }
      }

      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('📩 收到 WebSocket 消息:', message)

          // 处理不同类型的消息
          switch (message.type) {
            case 'notification':
              this._handleNotification(message.data)
              break
            case 'pong':
              console.log('💓 心跳响应')
              break
            case 'success':
              console.log('✅ 服务器消息:', message.message)
              break
            default:
              console.log('未知消息类型:', message.type)
          }
        } catch (error) {
          console.error('解析 WebSocket 消息失败:', error)
        }
      }

      this.ws.onerror = (error) => {
        console.error('❌ WebSocket 错误:', error)
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket 连接关闭:', event.code, event.reason)
        this._stopHeartbeat()

        // 非手动关闭且开启自动重连时尝试重连
        if (!this.isManualClose && this.autoReconnect && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          console.log(`尝试第 ${this.reconnectAttempts} 次重连（${this.reconnectInterval/1000}秒后）...`)
          this.reconnectTimer = setTimeout(() => {
            this._createConnection()
          }, this.reconnectInterval)
        } else if (this.autoReconnect && this.reconnectAttempts >= this.maxReconnectAttempts) {
          console.error('WebSocket 重连失败次数过多，已停止重连')
          console.error('⚠️ 实时通知功能不可用，但系统其他功能正常')
          // 暂时不弹窗，避免影响用户使用
          // ElNotification({
          //   title: '提示',
          //   message: '实时通知暂时不可用，但不影响系统使用',
          //   type: 'info',
          //   duration: 5000
          // })
        }
      }
    } catch (error) {
      console.error('创建 WebSocket 连接失败:', error)
    }
  }

  /**
   * 处理通知消息
   */
  _handleNotification(notification) {
    console.log('📩 收到通知数据:', notification)
    
    // 1. 构建通知内容（包含额外数据）
    let notificationBody = notification.content
    
    // 如果有额外数据，追加到通知内容中
    if (notification.extraData) {
      const extraData = notification.extraData
      console.log('📊 额外数据:', extraData)
      
      const bodyParts = [notification.content, ''] // 空行分隔
      
      // 价格信息
      if (extraData.price) {
        const priceStr = typeof extraData.price === 'number' 
          ? extraData.price.toFixed(8).replace(/\.?0+$/, '')  // 去除末尾的0
          : extraData.price
        bodyParts.push(`💰 价格: $${priceStr}`)
      }
      
      // 涨跌幅（带标识）
      if (extraData.priceChange !== null && extraData.priceChange !== undefined) {
        const change = parseFloat(extraData.priceChange)
        const changeText = change > 0 
          ? `📈 涨幅: +${change.toFixed(2)}%` 
          : `📉 跌幅: ${change.toFixed(2)}%`
        bodyParts.push(changeText)
      }
      
      // 市值
      if (extraData.marketCap !== null && extraData.marketCap !== undefined) {
        const marketCapFormatted = this._formatNumber(extraData.marketCap)
        bodyParts.push(`💎 市值: $${marketCapFormatted}`)
      }
      
      // 24小时交易量
      if (extraData.volume24h !== null && extraData.volume24h !== undefined) {
        const volumeFormatted = this._formatNumber(extraData.volume24h)
        bodyParts.push(`📊 24h成交: $${volumeFormatted}`)
      }
      
      // 持币人数
      if (extraData.holders !== null && extraData.holders !== undefined) {
        bodyParts.push(`👥 持币人: ${extraData.holders}`)
      }
      
      notificationBody = bodyParts.join('\n')
      console.log('📝 最终通知内容:', notificationBody)
    }
    
    // 2. 触发浏览器原生通知
    if (Notification.permission === 'granted') {
      // 使用 Token 头像作为通知图标（如果有）
      const notificationIcon = notification.extraData?.avatar || 
                               notification.extraData?.logo || 
                               '/favicon.ico'
      
      const browserNotification = new Notification(notification.title, {
        body: notificationBody,
        icon: notificationIcon,
        tag: notification.id, // 防止重复通知
        requireInteraction: false,
        badge: '/favicon.ico', // 小徽章图标
        vibrate: [200, 100, 200] // 震动模式（移动设备）
      })

      // 点击通知时跳转到对应页面
      browserNotification.onclick = () => {
        window.focus()
        if (notification.actionUrl) {
          // 支持外部链接和内部路由
          if (notification.actionUrl.startsWith('http')) {
            window.open(notification.actionUrl, '_blank')
          } else {
            window.location.href = notification.actionUrl
          }
        }
        browserNotification.close()
      }

      // 5秒后自动关闭（增加到5秒，因为内容更多）
      setTimeout(() => {
        browserNotification.close()
      }, 5000)
    }

    // 3. 显示 Element Plus 通知（页面内提示，带额外数据）
    const elMessage = this._buildElNotificationMessage(notification)
    
    ElNotification({
      title: notification.title,
      dangerouslyUseHTMLString: true,
      message: elMessage,
      type: notification.type || 'info',
      duration: 5000,
      onClick: () => {
        if (notification.actionUrl) {
          // 支持外部链接和内部路由
          if (notification.actionUrl.startsWith('http')) {
            window.open(notification.actionUrl, '_blank')
          } else {
            window.location.href = notification.actionUrl
          }
        }
      }
    })

    // 4. 触发回调（更新 Pinia store）
    if (this.onMessageCallback) {
      this.onMessageCallback(notification)
    }

    // 5. 播放提示音（可选）
    this._playNotificationSound()
  }

  /**
   * 构建 Element Plus 通知消息（HTML 格式，带额外数据）
   */
  _buildElNotificationMessage(notification) {
    let html = `<div style="line-height: 1.6;">`
    html += `<p style="margin: 0 0 8px 0; font-weight: 500;">${notification.content}</p>`
    
    if (notification.extraData) {
      const extraData = notification.extraData
      html += `<div style="font-size: 12px; color: #606266; margin-top: 8px;">`
      
      // 价格和涨跌幅在同一行
      if (extraData.price || extraData.priceChange !== null) {
        html += `<div style="margin-bottom: 4px;">`
        if (extraData.price) {
          html += `<span style="margin-right: 12px;">💰 <strong>$${extraData.price}</strong></span>`
        }
        if (extraData.priceChange !== null && extraData.priceChange !== undefined) {
          const color = extraData.priceChange > 0 ? '#67C23A' : '#F56C6C'
          const icon = extraData.priceChange > 0 ? '📈' : '📉'
          html += `<span style="color: ${color};">${icon} <strong>${extraData.priceChange > 0 ? '+' : ''}${extraData.priceChange}%</strong></span>`
        }
        html += `</div>`
      }
      
      // 市值
      if (extraData.marketCap) {
        const marketCapFormatted = this._formatNumber(extraData.marketCap)
        html += `<div style="margin-bottom: 4px;">💎 市值: <strong>$${marketCapFormatted}</strong></div>`
      }
      
      // 24小时交易量
      if (extraData.volume24h) {
        const volumeFormatted = this._formatNumber(extraData.volume24h)
        html += `<div style="margin-bottom: 4px;">📊 24h成交: <strong>$${volumeFormatted}</strong></div>`
      }
      
      // 持币人数
      if (extraData.holders) {
        html += `<div>👥 持币人: <strong>${extraData.holders}</strong></div>`
      }
      
      html += `</div>`
    }
    
    html += `</div>`
    return html
  }

  /**
   * 格式化数字（K, M, B 单位）
   */
  _formatNumber(num) {
    if (num === null || num === undefined) return '0'
    
    const number = parseFloat(num)
    if (isNaN(number)) return '0'
    
    if (number >= 1e9) {
      return (number / 1e9).toFixed(2) + 'B'
    } else if (number >= 1e6) {
      return (number / 1e6).toFixed(2) + 'M'
    } else if (number >= 1e3) {
      return (number / 1e3).toFixed(2) + 'K'
    } else {
      return number.toFixed(2)
    }
  }

  /**
   * 播放通知提示音
   */
  _playNotificationSound() {
    try {
      const audio = new Audio('/static/notification.mp3')
      audio.volume = 0.3
      audio.play().catch(() => {
        // 浏览器可能阻止自动播放，忽略错误
      })
    } catch (error) {
      // 提示音文件不存在或播放失败，忽略
    }
  }

  /**
   * 启动心跳
   */
  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ type: 'ping' }))
        console.log('💓 发送心跳')
      }
    }, this.heartbeatInterval)
  }

  /**
   * 停止心跳
   */
  _stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  /**
   * 手动关闭连接
   */
  close() {
    this.isManualClose = true
    this._stopHeartbeat()

    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.ws) {
      this.ws.close()
      this.ws = null
    }

    console.log('WebSocket 已手动关闭')
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('WebSocket 未连接，无法发送消息')
    }
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 导出类（供需要创建新实例的场景使用）
export { NotificationWebSocket }

// 导出单例（供一般场景使用）
export const notificationWS = new NotificationWebSocket()

export default notificationWS
