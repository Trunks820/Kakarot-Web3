<template>
  <div class="sol-notification-center">
    <el-card shadow="never">
      <template #header>
        <div class="notification-header">
          <div class="header-left">
            <el-icon class="bell-icon"><Bell /></el-icon>
            <span class="title">SOL智能监控动态</span>
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
          description="暂无SOL智能监控告警"
          :image-size="100"
        >
          <template #image>
            <el-icon :size="100" color="#67C23A"><Bell /></el-icon>
          </template>
          <template #description>
            <div style="color: #909399; font-size: 14px; margin-top: 8px;">
              <p>当前没有告警记录</p>
              <p style="font-size: 12px; color: #C0C4CC; margin-top: 4px;">
                智能监控配置生效后将在此显示
              </p>
            </div>
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
                class="notification-dot type-sol"
              >
                <el-icon :size="14">
                  <Bell />
                </el-icon>
              </div>
              <div v-if="!isLast(item)" class="notification-line"></div>
            </div>

            <!-- 右侧内容 -->
            <div class="notification-right">
              <div class="notification-time">
                <span>{{ formatExactTime(item.alertTime) }}</span>
                <div style="display: flex; gap: 4px; align-items: center;">
                  <el-tag 
                    type="success" 
                    size="small"
                    effect="plain"
                  >
                    ⚡ SOL
                  </el-tag>
                  <el-tag 
                    v-if="item.templateName" 
                    type="info" 
                    size="small"
                    effect="plain"
                  >
                    {{ item.templateName }}
                  </el-tag>
                </div>
              </div>
              <div class="notification-title">{{ item.tokenSymbol || 'Unknown' }}</div>
              <div class="notification-content-text">{{ formatAlertMessage(item) }}</div>
              
              <!-- Token监控专属信息：CA、市值、GMGN链接 -->
              <div class="notification-extra">
                <div class="extra-item ca-item">
                  <span class="extra-label">CA:</span>
                  <span class="extra-value ca-address" @click.stop="handleCopyCA(item.ca)">
                    {{ item.ca }}
                    <el-icon class="copy-icon"><CopyDocument /></el-icon>
                  </span>
                </div>
                <div v-if="item.marketCap" class="extra-item">
                  <span class="extra-label">市值:</span>
                  <span class="extra-value">${{ formatNumber(item.marketCap) }}</span>
                </div>
                <div class="extra-item">
                  <el-button 
                    type="primary" 
                    size="small" 
                    link
                    @click.stop="handleOpenGMGN(item.ca)"
                  >
                    <el-icon><Link /></el-icon>
                    在GMGN查看
                  </el-button>
                </div>
              </div>
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
  CopyDocument,
  Link
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getRecentSolAlerts } from '@/api/crypto/solAlert'

const router = useRouter()

// 通知列表（本地状态，不使用 Pinia）
const loading = ref(false)
const notifications = ref([])

// 已读ID集合（使用localStorage持久化，独立的key）
const readIds = ref(new Set(JSON.parse(localStorage.getItem('solReadNotificationIds') || '[]')))

// 保存已读ID到localStorage
const saveReadIds = () => {
  localStorage.setItem('solReadNotificationIds', JSON.stringify([...readIds.value]))
}

// 未读数量
const unreadCount = computed(() => {
  return notifications.value.filter(item => !readIds.value.has(item.id)).length
})

// 格式化精确时间（用于显示）
const formatExactTime = (timeStr) => {
  if (!timeStr) return ''
  
  const time = new Date(timeStr)
  const now = new Date()
  const isToday = time.toDateString() === now.toDateString()
  const isYesterday = new Date(now.getTime() - 86400000).toDateString() === time.toDateString()
  
  const hour = time.getHours().toString().padStart(2, '0')
  const minute = time.getMinutes().toString().padStart(2, '0')
  const second = time.getSeconds().toString().padStart(2, '0')
  
  if (isToday) {
    return `今天 ${hour}:${minute}:${second}`
  } else if (isYesterday) {
    return `昨天 ${hour}:${minute}:${second}`
  } else {
    const month = (time.getMonth() + 1).toString().padStart(2, '0')
    const date = time.getDate().toString().padStart(2, '0')
    return `${month}-${date} ${hour}:${minute}:${second}`
  }
}

// 格式化告警消息
const formatAlertMessage = (item) => {
  const parts = []
  
  if (item.priceChange1m) {
    parts.push(`1分钟: ${item.priceChange1m > 0 ? '+' : ''}${item.priceChange1m.toFixed(2)}%`)
  }
  if (item.priceChange5m) {
    parts.push(`5分钟: ${item.priceChange5m > 0 ? '+' : ''}${item.priceChange5m.toFixed(2)}%`)
  }
  if (item.priceChange1h) {
    parts.push(`1小时: ${item.priceChange1h > 0 ? '+' : ''}${item.priceChange1h.toFixed(2)}%`)
  }
  
  return parts.length > 0 ? parts.join(' | ') : '价格波动告警'
}

// 判断是否最后一项
const isLast = (item) => {
  return notifications.value.indexOf(item) === notifications.value.length - 1
}

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await getRecentSolAlerts(10)
    if (response.code === 200 && response.data) {
      notifications.value = response.data
    } else {
      notifications.value = []
    }
  } catch (error) {
    console.error('加载SOL通知失败:', error)
    notifications.value = []
  } finally {
    loading.value = false
  }
}

// 点击通知
const handleNotificationClick = async (item) => {
  // 标记为已读（前端状态 + localStorage持久化）
  if (!readIds.value.has(item.id)) {
    readIds.value.add(item.id)
    saveReadIds()
  }
}

// 标记全部已读
const handleMarkAllRead = async () => {
  notifications.value.forEach(item => {
    readIds.value.add(item.id)
  })
  saveReadIds()
  ElMessage.success('已全部标记为已读')
}

// 查看全部
const handleViewAll = () => {
  // 跳转到SOL监控历史页面
  router.push('/crypto/blockMonitor')
}

// 复制 CA 地址
const handleCopyCA = async (ca) => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(ca)
      ElMessage.success('CA地址已复制')
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = ca
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        ElMessage.success('CA地址已复制')
      } catch (err) {
        console.error('复制失败:', err)
        ElMessage.error('复制失败，请手动复制')
      }
      document.body.removeChild(textarea)
    }
  } catch (error) {
    console.error('复制失败:', error)
    ElMessage.error('复制失败，请手动复制')
  }
}

// 格式化数字（市值）
const formatNumber = (num) => {
  if (!num) return '0'
  const value = Number(num)
  if (value >= 1e9) return (value / 1e9).toFixed(2) + 'B'
  if (value >= 1e6) return (value / 1e6).toFixed(2) + 'M'
  if (value >= 1e3) return (value / 1e3).toFixed(2) + 'K'
  return value.toFixed(2)
}

// 打开 GMGN 链接（固定为 SOL）
const handleOpenGMGN = (ca) => {
  const url = `https://gmgn.ai/sol/token/${ca}`
  window.open(url, '_blank')
}

// 定时器
let refreshTimer = null

onMounted(() => {
  loadNotifications()
  
  // 每30秒刷新
  refreshTimer = setInterval(loadNotifications, 30000)
})

onUnmounted(() => {
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
.sol-notification-center {
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
      color: #67C23A; // SOL 用绿色
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
  min-height: 400px;
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
        background: #67C23A; // SOL 用绿色
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
        
        &.type-sol {
          background: #f0f9ff; // SOL 专属绿色背景
          color: #67C23A;
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
        margin-bottom: 8px;
      }
      
      .notification-extra {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
        align-items: center;
        padding-top: 8px;
        border-top: 1px solid #EBEEF5;
        margin-top: 8px;
        
        .extra-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          
          .extra-label {
            color: #909399;
            font-weight: 500;
          }
          
          .extra-value {
            color: #606266;
            
            &.ca-address {
              display: inline-flex;
              align-items: center;
              gap: 4px;
              padding: 2px 8px;
              background: #F5F7FA;
              border-radius: 4px;
              cursor: pointer;
              transition: all 0.3s;
              
              &:hover {
                background: #E6E8EB;
                color: #409EFF;
                
                .copy-icon {
                  opacity: 1;
                }
              }
              
              .copy-icon {
                font-size: 12px;
                opacity: 0.6;
                transition: opacity 0.3s;
              }
            }
          }
        }
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

