/**
 * 站内通知接收器
 * 接收Python监控脚本推送的实时通知
 */

import { useNotificationStore } from '@/store/modules/notification'
import { ElNotification } from 'element-plus'

// IP白名单（你的服务器IP）
const ALLOWED_IPS = [
  '127.0.0.1',
  'localhost',
  '47.106.217.116',  // 你的数据库服务器IP
  // 添加其他允许的IP
]

// 简单的Token验证（共享密钥）
const NOTIFICATION_TOKEN = 'your-secret-token-here-change-it'

class NotificationReceiver {
  constructor() {
    this.permissionGranted = false
    this.init()
  }

  /**
   * 初始化
   */
  async init() {
    // 请求浏览器通知权限
    await this.requestPermission()
    
    // 设置全局接收方法（供Python调用）
    this.setupGlobalReceiver()
    
    console.log('📢 通知接收器已启动')
  }

  /**
   * 请求浏览器通知权限
   */
  async requestPermission() {
    if (!('Notification' in window)) {
      console.warn('浏览器不支持通知API')
      return false
    }

    if (Notification.permission === 'granted') {
      this.permissionGranted = true
      return true
    }

    if (Notification.permission !== 'denied') {
      const permission = await Notification.requestPermission()
      this.permissionGranted = permission === 'granted'
      return this.permissionGranted
    }

    return false
  }

  /**
   * 设置全局接收方法
   */
  setupGlobalReceiver() {
    // 暴露全局方法供外部调用
    window.__receiveNotification = (data, token, clientIp) => {
      return this.receiveNotification(data, token, clientIp)
    }
  }

  /**
   * 接收通知（主方法）
   * @param {Object} data - 通知数据
   * @param {String} token - 验证Token
   * @param {String} clientIp - 客户端IP（可选，用于日志）
   */
  receiveNotification(data, token, clientIp) {
    try {
      // 1. 验证Token
      if (token !== NOTIFICATION_TOKEN) {
        console.error('❌ 通知Token验证失败')
        return { success: false, error: 'Invalid token' }
      }

      // 2. 验证数据格式
      if (!this.validateData(data)) {
        console.error('❌ 通知数据格式错误', data)
        return { success: false, error: 'Invalid data format' }
      }

      // 3. 添加到Store
      const notificationStore = useNotificationStore()
      notificationStore.addNotification(data)

      // 4. 浏览器通知弹窗
      this.showBrowserNotification(data)

      // 5. Element Plus通知
      this.showElementNotification(data)

      console.log('✅ 通知接收成功:', data.title)
      return { success: true }

    } catch (error) {
      console.error('❌ 接收通知失败:', error)
      return { success: false, error: error.message }
    }
  }

  /**
   * 验证数据格式
   */
  validateData(data) {
    return data 
      && typeof data === 'object'
      && data.module 
      && data.type 
      && data.title 
      && data.content
  }

  /**
   * 显示浏览器原生通知
   */
  showBrowserNotification(data) {
    if (!this.permissionGranted) {
      return
    }

    const { title, content, module } = data
    
    // 模块图标
    const iconMap = {
      'token-monitor': '📊',
      'twitter': '🐦',
      'system': '⚙️',
      'wechat-bot': '🤖'
    }
    const icon = iconMap[module] || '📌'

    // 创建通知
    const notification = new Notification(`${icon} ${title}`, {
      body: content,
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      tag: data.id || Date.now().toString(),
      requireInteraction: data.type === 'alert', // 预警类通知需要用户交互才消失
      silent: false
    })

    // 点击通知
    notification.onclick = () => {
      window.focus()
      
      // 跳转到对应页面
      if (data.actionUrl) {
        const router = window.__app__.config.globalProperties.$router
        router.push(data.actionUrl)
      }
      
      notification.close()
    }

    // 3秒后自动关闭（非预警类）
    if (data.type !== 'alert') {
      setTimeout(() => notification.close(), 3000)
    }
  }

  /**
   * 显示Element Plus通知
   */
  showElementNotification(data) {
    const { title, content, type, actionUrl } = data

    // 类型映射
    const typeMap = {
      'alert': 'error',
      'warning': 'warning',
      'success': 'success',
      'info': 'info'
    }

    ElNotification({
      title: title,
      message: content,
      type: typeMap[type] || 'info',
      duration: type === 'alert' ? 0 : 4500, // 预警类不自动关闭
      position: 'top-right',
      onClick: () => {
        if (actionUrl) {
          const router = window.__app__.config.globalProperties.$router
          router.push(actionUrl)
        }
      }
    })
  }
}

// 导出单例
export const notificationReceiver = new NotificationReceiver()

// 导出手动申请权限方法
export async function requestNotificationPermission() {
  return notificationReceiver.requestPermission()
}

