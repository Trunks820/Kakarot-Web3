import { defineStore } from 'pinia'

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    list: [],
    unreadCount: 0,
    loading: false
  }),
  
  getters: {
    // 最近5条通知
    recentNotifications: (state) => {
      return state.list.slice(0, 5)
    },
    
    // 未读通知
    unreadNotifications: (state) => {
      return state.list.filter(n => !n.isRead)
    }
  },
  
  actions: {
    // 添加通知（WebSocket 接收实时通知时调用）
    addNotification(notification) {
      const newNotification = {
        id: notification.id || `ws_${Date.now()}`,
        createTime: notification.createTime || new Date().toISOString(),
        isRead: notification.isRead || false,
        ...notification
      }
      
      // 检查是否已存在（避免重复）
      const exists = this.list.some(n => n.id === newNotification.id)
      if (exists) {
        console.log('⚠️  通知已存在，跳过添加:', newNotification.id)
        return
      }
      
      this.list.unshift(newNotification)
      if (!newNotification.isRead) {
        this.unreadCount++
      }
      
      // 限制列表长度，最多保留100条
      if (this.list.length > 100) {
        this.list = this.list.slice(0, 100)
      }
      
      console.log('✅ 通知已添加到Store:', newNotification.title)
    },
    
    // 批量设置通知（用于初始加载）
    setNotifications(notifications) {
      this.list = notifications
      this.unreadCount = notifications.filter(n => !n.isRead).length
    },
    
    // 标记为已读
    markAsRead(id) {
      const item = this.list.find(n => n.id === id)
      if (item && !item.isRead) {
        item.isRead = true
        this.unreadCount--
      }
    },
    
    // 全部标记为已读
    markAllAsRead() {
      this.list.forEach(item => {
        item.isRead = true
      })
      this.unreadCount = 0
    },
    
    // 清空通知
    clear() {
      this.list = []
      this.unreadCount = 0
    }
  }
})

