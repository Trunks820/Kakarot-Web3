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
    // 添加通知（前端接收实时通知时调用）
    addNotification(notification) {
      const newNotification = {
        id: Date.now(),
        createTime: new Date().toISOString(),
        isRead: false,
        ...notification
      }
      
      this.list.unshift(newNotification)
      this.unreadCount++
      
      // 限制列表长度，最多保留100条
      if (this.list.length > 100) {
        this.list = this.list.slice(0, 100)
      }
      
      console.log('✅ 通知已添加到Store:', newNotification.title)
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

