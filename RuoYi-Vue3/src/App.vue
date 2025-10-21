<template>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted, nextTick, watch } from 'vue'
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'
import { NotificationWebSocket } from '@/utils/websocket'
import useUserStore from '@/store/modules/user'
import { useNotificationStore } from '@/store/modules/notification'

// WebSocket 实例
let wsClient = null

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
    
    // 初次加载时尝试连接（如果已登录）
    initWebSocket()
  })
})

onUnmounted(() => {
  // 组件卸载时关闭 WebSocket
  if (wsClient) {
    wsClient.close()
  }
})

// 监听 token 变化，登录成功后自动连接 WebSocket
const userStore = useUserStore()
watch(
  () => userStore.token,
  (newToken, oldToken) => {
    console.log('🔑 Token 变化检测:', { oldToken: oldToken ? '有值' : '空', newToken: newToken ? '有值' : '空' })
    
    // 从无 token 变为有 token（登录成功）
    if (!oldToken && newToken) {
      console.log('✅ 用户登录成功，开始初始化 WebSocket...')
      initWebSocket()
    }
    
    // 从有 token 变为无 token（退出登录）
    if (oldToken && !newToken) {
      console.log('🚪 用户退出登录，关闭 WebSocket 连接...')
      if (wsClient) {
        wsClient.close()
        wsClient = null
      }
    }
  },
  { immediate: false } // 不立即执行，避免重复初始化
)

/**
 * 初始化 WebSocket 连接
 */
function initWebSocket() {
  try {
    const token = userStore.token
    
    if (!token) {
      console.warn('⚠️ 用户未登录，跳过 WebSocket 初始化')
      return
    }
    
    // 如果已经存在连接且处于连接状态，跳过
    if (wsClient && wsClient.isConnected()) {
      console.log('⚠️ WebSocket 已连接，跳过重复初始化')
      return
    }
    
    // 如果存在旧连接，先关闭
    if (wsClient) {
      console.log('🔄 关闭旧的 WebSocket 连接...')
      wsClient.close()
    }
    
    const notificationStore = useNotificationStore()
    
    console.log('🚀 开始初始化 WebSocket，Token:', token.substring(0, 20) + '...')
    
    // 创建 WebSocket 实例
    wsClient = new NotificationWebSocket({
      token: token,
      autoReconnect: true,
      reconnectInterval: 5000,
      heartbeatInterval: 30000
    })
    
    // 连接 WebSocket，并传入通知回调
    wsClient.connect(null, (notification) => {
      // 将通知添加到 Pinia store
      notificationStore.addNotification(notification)
      console.log('✅ 通知已添加到 Store:', notification.title)
    })
    
    console.log('✅ WebSocket 初始化完成')
    
  } catch (error) {
    console.error('❌ WebSocket 初始化失败', error)
  }
}
</script>

<style>
/* 全局样式覆盖 - 亮模式 */
:root {
  --el-card-bg-color: #ffffff;
  --el-bg-color: #ffffff;
  --el-bg-color-overlay: #ffffff;
  --el-text-color-primary: #303133;
  --el-text-color-regular: #606266;
  --el-border-color: #dcdfe6;
  --el-border-color-light: #e4e7ed;
}

/* 全局样式覆盖 - 暗黑模式 */
html.dark {
  --el-card-bg-color: #1d1e1f !important;
  --el-bg-color: #141414 !important;
  --el-bg-color-overlay: #1d1e1f !important;
  --el-text-color-primary: #ffffff !important;
  --el-text-color-regular: #d0d0d0 !important;
  --el-border-color: #434343 !important;
  --el-border-color-light: #434343 !important;
}

/* 强制卡片样式 - 亮模式 */
.el-card {
  background-color: #ffffff;
  color: #303133;
  border-color: #e4e7ed;
}

.el-card .el-card__header {
  background-color: #f5f7fa;
  color: #303133;
  border-color: #e4e7ed;
}

/* 强制卡片样式 - 暗黑模式 */
html.dark .el-card {
  background-color: #1d1e1f !important;
  color: #ffffff !important; 
  border-color: #434343 !important;
}

html.dark .el-card .el-card__header {
  background-color: #141414 !important;
  color: #ffffff !important;
  border-color: #434343 !important;
}

/* 数据卡片组件覆盖 */
html.dark .data-card {
  background-color: #1d1e1f !important;
  border-color: #434343 !important;
}

html.dark .data-card .card-header {
  background-color: #141414 !important;
  border-color: #434343 !important;
}

html.dark .data-card .card-header span {
  color: #ffffff !important;
}

html.dark .data-card .card-body {
  background-color: #1d1e1f !important;
}

html.dark .data-card .stat-item h3 {
  color: #ffffff !important;
}

html.dark .data-card .stat-item .label {
  color: #d0d0d0 !important;
}

/* 市场图表组件覆盖 */
html.dark .market-chart {
  background-color: #1d1e1f !important;
  border-color: #434343 !important;
}

html.dark .market-chart .el-card__header {
  background-color: #141414 !important;
  border-color: #434343 !important;
}

html.dark .market-chart .card-header span {
  color: #ffffff !important;
}

html.dark .market-chart .chart-container {
  background-color: #1d1e1f !important;
}
</style>
