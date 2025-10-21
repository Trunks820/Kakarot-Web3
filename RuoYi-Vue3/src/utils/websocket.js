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
    // 1. 触发浏览器原生通知
    if (Notification.permission === 'granted') {
      const browserNotification = new Notification(notification.title, {
        body: notification.content,
        icon: '/favicon.ico',
        tag: notification.id, // 防止重复通知
        requireInteraction: false
      })

      // 点击通知时跳转到对应页面
      browserNotification.onclick = () => {
        window.focus()
        if (notification.actionUrl) {
          window.location.href = notification.actionUrl
        }
        browserNotification.close()
      }

      // 3秒后自动关闭
      setTimeout(() => {
        browserNotification.close()
      }, 3000)
    }

    // 2. 显示 Element Plus 通知（页面内提示）
    ElNotification({
      title: notification.title,
      message: notification.content,
      type: notification.type || 'info',
      duration: 5000,
      onClick: () => {
        if (notification.actionUrl) {
          window.location.href = notification.actionUrl
        }
      }
    })

    // 3. 触发回调（更新 Pinia store）
    if (this.onMessageCallback) {
      this.onMessageCallback(notification)
    }

    // 4. 播放提示音（可选）
    this._playNotificationSound()
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
