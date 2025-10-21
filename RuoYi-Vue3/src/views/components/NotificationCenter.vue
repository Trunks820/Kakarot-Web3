<template>
  <div class="notification-center">
    <el-card shadow="never">
      <template #header>
        <div class="notification-header">
          <div class="header-left">
            <el-icon class="bell-icon"><Bell /></el-icon>
            <span class="title">最新动态</span>
            <el-badge 
              v-if="unreadCount > 0" 
              :value="unreadCount" 
              :max="99"
              class="badge"
            />
          </div>
          <div class="header-right">
            <el-button 
              text 
              size="small" 
              @click="handleMarkAllRead"
              :disabled="unreadCount === 0"
            >
              标记已读
            </el-button>
            <el-button 
              text 
              size="small" 
              @click="handleViewAll"
            >
              查看全部
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="notification-content">
        <el-empty 
          v-if="notifications.length === 0" 
          description="暂无通知"
          :image-size="120"
        >
          <template #image>
            <el-icon :size="120" color="#C0C4CC"><MessageBox /></el-icon>
          </template>
        </el-empty>

        <div v-else class="notification-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: !item.isRead }"
            @click="handleNotificationClick(item)"
          >
            <!-- 左侧图标和时间线 -->
            <div class="notification-left">
              <div 
                class="notification-dot"
                :class="`type-${item.type}`"
              >
                <el-icon :size="14">
                  <component :is="getNotificationIcon(item.type)" />
                </el-icon>
              </div>
              <div v-if="!isLast(item)" class="notification-line"></div>
            </div>

            <!-- 右侧内容 -->
            <div class="notification-right">
              <div class="notification-time">
                <span>{{ formatTime(item.createTime) }}</span>
                <el-tag 
                  :type="getModuleTagType(item.module)" 
                  size="small"
                  effect="plain"
                >
                  {{ item.moduleName }}
                </el-tag>
              </div>
              <div class="notification-title">{{ item.title }}</div>
              <div class="notification-content-text">{{ item.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Bell, 
  MessageBox,
  WarningFilled,
  SuccessFilled,
  InfoFilled,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useNotificationStore } from '@/store/modules/notification'

const router = useRouter()
const notificationStore = useNotificationStore()

// 获取最新通知（从token_monitor_alert_log表）
const getRecentNotifications = (limit = 10) => {
  return request({
    url: '/crypto/monitorAlert/recent',
    method: 'get',
    params: { limit }
  })
}

// 通知列表（从 Pinia store 获取）
const loading = ref(false)
const notifications = computed(() => notificationStore.list.slice(0, 10))

// 已读ID集合（使用localStorage持久化）
const readIds = ref(new Set(JSON.parse(localStorage.getItem('readNotificationIds') || '[]')))

// 保存已读ID到localStorage
const saveReadIds = () => {
  localStorage.setItem('readNotificationIds', JSON.stringify([...readIds.value]))
}

// 未读数量（结合 store 和 localStorage）
const unreadCount = computed(() => {
  return notifications.value.filter(item => !item.isRead && !readIds.value.has(item.id)).length
})

// 获取通知图标
const getNotificationIcon = (type) => {
  const iconMap = {
    'alert': WarningFilled,
    'success': SuccessFilled,
    'warning': Warning,
    'info': InfoFilled,
    'error': WarningFilled
  }
  return iconMap[type] || InfoFilled
}

// 获取模块标签类型
const getModuleTagType = (module) => {
  const typeMap = {
    'token-monitor': 'warning',
    'twitter': 'primary',
    'system': 'info',
    'wechat-bot': 'success',
    'global-monitor': 'danger'
  }
  return typeMap[module] || 'info'
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  
  const time = new Date(timeStr)
  const now = new Date()
  const diff = Math.floor((now - time) / 1000) // 秒
  
  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  if (diff < 259200) return `${Math.floor(diff / 86400)}天前`
  
  // 超过3天显示具体日期
  const month = time.getMonth() + 1
  const date = time.getDate()
  const hour = time.getHours().toString().padStart(2, '0')
  const minute = time.getMinutes().toString().padStart(2, '0')
  return `${month}月${date}日 ${hour}:${minute}`
}

// 判断是否最后一项
const isLast = (item) => {
  return notifications.value.indexOf(item) === notifications.value.length - 1
}

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    // 调用真实API获取系统通知和预警记录
    const response = await getRecentNotifications(10)
    if (response.code === 200 && response.data) {
      // 将数据设置到 Pinia store
      notificationStore.setNotifications(response.data)
      
      // 应用本地已读状态
      notificationStore.list.forEach(item => {
        if (readIds.value.has(item.id)) {
          item.isRead = 1
        }
      })
    } else {
      notificationStore.setNotifications([])
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    notificationStore.setNotifications([])
  } finally {
    loading.value = false
  }
}

// 点击通知
const handleNotificationClick = async (item) => {
  // 标记为已读（前端状态 + localStorage持久化）
  if (!item.isRead) {
    item.isRead = 1
    readIds.value.add(item.id)
    saveReadIds()
  }
  
  // 不跳转，只标记已读
  // if (item.actionUrl) {
  //   router.push(item.actionUrl)
  // }
}

// 标记全部已读
const handleMarkAllRead = async () => {
  // 前端标记所有为已读 + localStorage持久化
  notifications.value.forEach(item => {
    item.isRead = 1
    readIds.value.add(item.id)
  })
  saveReadIds()
  ElMessage.success('已全部标记为已读')
}

// 查看全部
const handleViewAll = () => {
  // TODO: 跳转到通知列表页
  ElMessage.info('通知列表页面开发中')
}

// 定时器
let refreshTimer = null

onMounted(() => {
  loadNotifications()
  
  // 每30秒刷新（只刷新数据库的历史通知，WebSocket 会实时推送新通知）
  refreshTimer = setInterval(loadNotifications, 30000)
})

onUnmounted(() => {
  // 清理定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})

// 暴露方法供外部调用
defineExpose({
  loadNotifications
})
</script>

<style scoped lang="scss">
.notification-center {
  :deep(.el-card__header) {
    padding: 16px 20px;
  }
  
  :deep(.el-card__body) {
    padding: 0;
  }
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .bell-icon {
      font-size: 20px;
      color: #409EFF;
    }
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.notification-content {
  min-height: 200px;
  max-height: 500px;
  overflow-y: auto;
}

.notification-list {
  padding: 20px;
  
  .notification-item {
    display: flex;
    gap: 16px;
    cursor: pointer;
    transition: all 0.3s;
    position: relative;
    
    &.unread {
      .notification-right {
        .notification-title {
          font-weight: 600;
          color: #303133;
        }
      }
      
      &::before {
        content: '';
        position: absolute;
        left: -12px;
        top: 8px;
        width: 6px;
        height: 6px;
        background: #F56C6C;
        border-radius: 50%;
      }
    }
    
    &:hover {
      .notification-right {
        background: #F5F7FA;
      }
    }
    
    .notification-left {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      
      .notification-dot {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        
        &.type-alert {
          background: #FEF0F0;
          color: #F56C6C;
        }
        
        &.type-warning {
          background: #FDF6EC;
          color: #E6A23C;
        }
        
        &.type-success {
          background: #F0F9FF;
          color: #67C23A;
        }
        
        &.type-info {
          background: #F4F4F5;
          color: #909399;
        }
        
        &.type-error {
          background: #FEF0F0;
          color: #F56C6C;
        }
      }
      
      .notification-line {
        flex: 1;
        width: 2px;
        background: #DCDFE6;
        min-height: 40px;
      }
    }
    
    .notification-right {
      flex: 1;
      padding: 8px 12px;
      border-radius: 8px;
      transition: all 0.3s;
      
      .notification-time {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        span {
          font-size: 12px;
          color: #909399;
        }
      }
      
      .notification-title {
        font-size: 14px;
        color: #606266;
        margin-bottom: 4px;
      }
      
      .notification-content-text {
        font-size: 13px;
        color: #909399;
        line-height: 1.6;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .notification-content {
    max-height: 400px;
  }
  
  .notification-list {
    padding: 16px;
    
    .notification-item {
      gap: 12px;
      
      .notification-left {
        .notification-dot {
          width: 28px;
          height: 28px;
          
          .el-icon {
            font-size: 12px;
          }
        }
      }
      
      .notification-right {
        padding: 6px 10px;
        
        .notification-title {
          font-size: 13px;
        }
        
        .notification-content-text {
          font-size: 12px;
        }
      }
    }
  }
}
</style>

