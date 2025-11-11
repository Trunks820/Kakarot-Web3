/**
 * Monitor V2 WebSocket 客户端
 * 专门用于监控系统的实时推送
 * 
 * 支持的消息类型：
 * - batch_status: 批次状态更新
 * - task_status: 任务状态更新
 * - alert: 告警通知
 * - connected: 连接成功
 * - pong: 心跳响应
 */

import { ElNotification } from 'element-plus'

class MonitorWebSocket {
  constructor(options = {}) {
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = options.maxReconnectAttempts || 5
    this.reconnectInterval = options.reconnectInterval || 5000 // 5秒
    this.heartbeatInterval = options.heartbeatInterval || 30000 // 30秒心跳
    this.autoReconnect = options.autoReconnect !== false
    this.url = ''
    this.isManualClose = false
    
    // 消息回调函数
    this.callbacks = {
      onBatchStatus: null,
      onTaskStatus: null,
      onAlert: null,
      onConnected: null,
      onError: null
    }
  }

  /**
   * 连接 WebSocket
   */
  connect() {
    this.isManualClose = false

    // 构建 WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    
    let host
    if (import.meta.env.DEV) {
      // 开发环境
      host = 'localhost:8080'
    } else {
      // 生产环境
      host = window.location.host
      if (!host.includes(':')) {
        host = host + ':8080'
      }
    }
    
    this.url = `${protocol}//${host}/websocket/monitor`

    console.log('🔌 正在连接 Monitor WebSocket:', this.url)
    this._createConnection()
  }

  /**
   * 创建 WebSocket 连接
   */
  _createConnection() {
    try {
      this.ws = new WebSocket(this.url)

      // 连接成功
      this.ws.onopen = () => {
        console.log('✅ Monitor WebSocket 连接成功')
        this.reconnectAttempts = 0
        this._startHeartbeat()

        if (this.callbacks.onConnected) {
          this.callbacks.onConnected()
        }
      }

      // 接收消息
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          console.log('📩 收到 Monitor WebSocket 消息:', message)

          this._handleMessage(message)
        } catch (error) {
          console.error('解析 WebSocket 消息失败:', error)
        }
      }

      // 连接错误
      this.ws.onerror = (error) => {
        console.error('❌ Monitor WebSocket 错误:', error)
        
        if (this.callbacks.onError) {
          this.callbacks.onError(error)
        }
      }

      // 连接关闭
      this.ws.onclose = (event) => {
        console.log('Monitor WebSocket 连接关闭:', event.code, event.reason)
        this._stopHeartbeat()

        // 非手动关闭且开启自动重连
        if (!this.isManualClose && this.autoReconnect && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.reconnectAttempts++
          console.log(`尝试第 ${this.reconnectAttempts} 次重连（${this.reconnectInterval/1000}秒后）...`)
          this.reconnectTimer = setTimeout(() => {
            this._createConnection()
          }, this.reconnectInterval)
        } else if (this.autoReconnect && this.reconnectAttempts >= this.maxReconnectAttempts) {
          console.error('Monitor WebSocket 重连失败次数过多，已停止重连')
        }
      }
    } catch (error) {
      console.error('创建 Monitor WebSocket 连接失败:', error)
    }
  }

  /**
   * 处理消息
   */
  _handleMessage(message) {
    const { type, data } = message

    switch (type) {
      case 'connected':
        console.log('✅ Monitor WebSocket 连接确认:', message)
        break

      case 'pong':
        console.log('💓 Monitor 心跳响应')
        break

      case 'batch_status':
        this._handleBatchStatus(data)
        break

      case 'task_status':
        this._handleTaskStatus(data)
        break

      case 'alert':
        this._handleAlert(data)
        break

      case 'subscribed':
        console.log('✅ 订阅成功:', data)
        break

      default:
        console.log('未知消息类型:', type, data)
    }
  }

  /**
   * 处理批次状态更新
   */
  _handleBatchStatus(data) {
    console.log('📦 批次状态更新:', data)
    
    if (this.callbacks.onBatchStatus) {
      this.callbacks.onBatchStatus(data)
    }

    // 显示通知
    const statusText = this._getStatusText(data.status)
    const statusType = this._getStatusType(data.status)

    ElNotification({
      title: '批次状态更新',
      message: `批次 #${data.batchId} ${statusText}`,
      type: statusType,
      duration: 3000
    })
  }

  /**
   * 处理任务状态更新
   */
  _handleTaskStatus(data) {
    console.log('📝 任务状态更新:', data)
    
    if (this.callbacks.onTaskStatus) {
      this.callbacks.onTaskStatus(data)
    }

    // 显示通知
    const statusText = data.status === 1 ? '已启动' : '已停止'
    const statusType = data.status === 1 ? 'success' : 'warning'

    ElNotification({
      title: '任务状态更新',
      message: `任务 #${data.taskId} ${statusText}${data.message ? ': ' + data.message : ''}`,
      type: statusType,
      duration: 3000
    })
  }

  /**
   * 处理告警通知
   */
  _handleAlert(data) {
    console.log('🔔 告警通知:', data)
    
    if (this.callbacks.onAlert) {
      this.callbacks.onAlert(data)
    }

    // 播放提示音
    this._playNotificationSound()

    // 显示告警通知
    const alertTypeText = this._getAlertTypeText(data.alertType)
    
    ElNotification({
      title: `🚨 ${alertTypeText}`,
      dangerouslyUseHTMLString: true,
      message: this._buildAlertMessage(data),
      type: 'warning',
      duration: 5000,
      onClick: () => {
        // 点击跳转到告警详情
        window.location.href = `#/monitor-v2/alert?alertId=${data.alertId}`
      }
    })

    // 尝试浏览器原生通知
    if (Notification.permission === 'granted') {
      new Notification(`🚨 ${alertTypeText}`, {
        body: this._buildAlertText(data),
        icon: '/favicon.ico',
        tag: `alert-${data.alertId}`,
        requireInteraction: false
      })
    } else if (Notification.permission === 'default') {
      Notification.requestPermission()
    }
  }

  /**
   * 构建告警消息（HTML）
   */
  _buildAlertMessage(data) {
    const { alertData } = data
    let html = '<div style="line-height: 1.6;">'
    
    // Token信息
    if (alertData.tokenName) {
      html += `<div style="margin-bottom: 8px;"><strong>${alertData.tokenName}</strong>`
      if (alertData.tokenSymbol) {
        html += ` (${alertData.tokenSymbol})`
      }
      html += '</div>'
    }

    // CA地址
    if (alertData.ca) {
      const shortCa = alertData.ca.substring(0, 6) + '...' + alertData.ca.substring(alertData.ca.length - 4)
      html += `<div style="font-size: 12px; color: #909399; margin-bottom: 8px;">CA: ${shortCa}</div>`
    }

    // 具体数据
    if (alertData) {
      html += '<div style="font-size: 13px; color: #606266;">'
      
      // 价格变化
      if (alertData.priceChange !== undefined) {
        const color = alertData.priceChange > 0 ? '#67C23A' : '#F56C6C'
        const icon = alertData.priceChange > 0 ? '📈' : '📉'
        html += `<div>${icon} 价格变化: <span style="color: ${color}; font-weight: bold;">${alertData.priceChange > 0 ? '+' : ''}${alertData.priceChange}%</span></div>`
      }

      // 交易量
      if (alertData.volume !== undefined) {
        html += `<div>📊 交易量: $${this._formatNumber(alertData.volume)}</div>`
      }

      // 持仓变化
      if (alertData.holderChange !== undefined) {
        html += `<div>👥 持仓变化: ${alertData.holderChange > 0 ? '+' : ''}${alertData.holderChange}%</div>`
      }

      html += '</div>'
    }
    
    html += '</div>'
    return html
  }

  /**
   * 构建告警文本（纯文本，用于浏览器通知）
   */
  _buildAlertText(data) {
    const { alertData } = data
    let text = ''
    
    if (alertData.tokenName) {
      text += `${alertData.tokenName}`
      if (alertData.tokenSymbol) {
        text += ` (${alertData.tokenSymbol})`
      }
      text += '\n'
    }

    if (alertData.priceChange !== undefined) {
      text += `价格变化: ${alertData.priceChange > 0 ? '+' : ''}${alertData.priceChange}%\n`
    }

    if (alertData.volume !== undefined) {
      text += `交易量: $${this._formatNumber(alertData.volume)}`
    }

    return text
  }

  /**
   * 获取状态文本
   */
  _getStatusText(status) {
    const statusMap = {
      'pending': '待处理',
      'running': '运行中',
      'paused': '已暂停',
      'stopped': '已停止',
      'completed': '已完成',
      'error': '异常'
    }
    return statusMap[status] || status
  }

  /**
   * 获取状态类型（用于通知）
   */
  _getStatusType(status) {
    const typeMap = {
      'pending': 'info',
      'running': 'success',
      'paused': 'warning',
      'stopped': 'info',
      'completed': 'success',
      'error': 'error'
    }
    return typeMap[status] || 'info'
  }

  /**
   * 获取告警类型文本
   */
  _getAlertTypeText(alertType) {
    const typeMap = {
      'price_change': '价格异动',
      'holder_change': '持仓变化',
      'volume_change': '交易量异常',
      'block_event': '区块事件',
      'liquidity_change': '流动性变化'
    }
    return typeMap[alertType] || '监控告警'
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
      const audioContext = new (window.AudioContext || window.webkitAudioContext)()
      const oscillator = audioContext.createOscillator()
      const gainNode = audioContext.createGain()

      oscillator.connect(gainNode)
      gainNode.connect(audioContext.destination)

      oscillator.frequency.value = 800
      oscillator.type = 'sine'
      gainNode.gain.value = 0.3

      const now = audioContext.currentTime
      oscillator.start(now)
      gainNode.gain.setValueAtTime(0.3, now)
      gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.1)
      gainNode.gain.setValueAtTime(0.3, now + 0.15)
      gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.25)
      oscillator.stop(now + 0.3)

      console.log('🔔 播放告警提示音')
    } catch (error) {
      console.warn('播放提示音失败:', error)
    }
  }

  /**
   * 启动心跳
   */
  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatTimer = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping' })
        console.log('💓 发送 Monitor 心跳')
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
   * 订阅主题
   */
  subscribe(topic) {
    this.send({
      type: 'subscribe',
      topic: topic
    })
    console.log('📡 订阅主题:', topic)
  }

  /**
   * 发送消息
   */
  send(message) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message))
    } else {
      console.warn('Monitor WebSocket 未连接，无法发送消息')
    }
  }

  /**
   * 注册批次状态回调
   */
  onBatchStatus(callback) {
    this.callbacks.onBatchStatus = callback
    return this
  }

  /**
   * 注册任务状态回调
   */
  onTaskStatus(callback) {
    this.callbacks.onTaskStatus = callback
    return this
  }

  /**
   * 注册告警回调
   */
  onAlert(callback) {
    this.callbacks.onAlert = callback
    return this
  }

  /**
   * 注册连接成功回调
   */
  onConnected(callback) {
    this.callbacks.onConnected = callback
    return this
  }

  /**
   * 注册错误回调
   */
  onError(callback) {
    this.callbacks.onError = callback
    return this
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

    console.log('Monitor WebSocket 已手动关闭')
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.ws && this.ws.readyState === WebSocket.OPEN
  }
}

// 导出类（供需要创建新实例的场景使用）
export { MonitorWebSocket }

// 导出单例（供一般场景使用）
export const monitorWS = new MonitorWebSocket()

export default monitorWS

