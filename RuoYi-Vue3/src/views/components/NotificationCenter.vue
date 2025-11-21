<template>
  <div class="notification-center">
    <el-card shadow="hover" class="panel-card">
      <template #header>
        <div class="notification-header">
          <div class="header-left">
            <div class="icon-box">
              <img :src="BNBIcon" alt="BNB" class="chain-icon" />
            </div>
            <span class="title">BSC 链上动态</span>
            <!-- 未读数胶囊 -->
            <el-tag 
              v-if="notifications.length > 0" 
              :type="unreadCount > 0 ? 'danger' : ''"
              :effect="unreadCount > 0 ? 'dark' : 'light'"
              size="small"
              class="unread-badge"
            >
              <span v-if="unreadCount > 0">{{ unreadCount }} 条未读</span>
              <span v-else>共 {{ notifications.length }} 条</span>
            </el-tag>
          </div>
          <div class="header-right">
            <el-tooltip content="标记全部已读" placement="top">
              <el-button 
                text 
                circle
                size="small" 
                @click="handleMarkAllRead"
                :disabled="unreadCount === 0"
              >
                <el-icon><Check /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="查看全部历史" placement="top">
              <el-button 
                text 
                circle
                size="small" 
                @click="handleViewAll"
              >
                <el-icon><More /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="notification-content custom-scrollbar">
        <el-empty 
          v-if="notifications.length === 0" 
          :description="loading ? '' : '暂无BSC链上告警/持续监听中'"
          :image-size="80"
        >
          <template #default v-if="!loading">
            <el-button type="primary" link @click="loadNotifications">刷新</el-button>
          </template>
        </el-empty>

        <div v-else class="notification-list">
          <div
            v-for="item in notifications"
            :key="item.id"
            class="notification-item"
            :class="{ unread: !item.isRead && !readIds.has(item.id), read: item.isRead || readIds.has(item.id) }"
            @click="handleNotificationClick(item)"
          >
            <!-- 左侧指示器 - BSC 黄色 -->
            <div v-if="!item.isRead" class="item-indicator bsc-indicator"></div>

            <!-- 右侧内容主体 -->
            <div class="item-body">
              <div class="item-header">
                <div class="meta-tags">
                  <el-tag 
                    :type="getModuleTagType(item.module)" 
                    size="small"
                    effect="dark"
                    class="module-tag"
                  >
                    {{ item.moduleName }}
                  </el-tag>
                  <span class="time-label">{{ formatTimeWithDetails(item.createTime) }}</span>
                </div>
                
                <!-- 右上角链接 -->
                <el-tooltip content="在GMGN查看" placement="top" :show-after="500" v-if="item.ca">
                   <el-link :underline="false" class="gmgn-link" @click.stop="handleOpenGMGN(item.ca, item.chainType)">
                     <el-icon><Link /></el-icon>
                   </el-link>
                </el-tooltip>
              </div>
              
              <div class="item-main-content">
                 <span class="title-text strong-text">{{ item.title }}</span>
                 <span class="separator" v-if="item.content">-</span>
                 <span class="desc-text">{{ item.content }}</span>
              </div>

              <!-- 底部信息：市值 + CA -->
              <div class="item-footer" v-if="item.marketCap || item.ca">
                 <span class="stat-item" v-if="item.marketCap">
                   <span class="label">市值:</span>
                   <span class="value strong-text">${{ formatNumber(item.marketCap) }}</span>
                 </span>
                 
                 <!-- CA 地址：点击复制并标记已读 -->
                 <span class="stat-item ca-item clickable" v-if="item.ca" @click.stop="handleCopyCAAndMarkRead(item)">
                   <span class="label">CA:</span>
                   <span class="value strong-text">{{ item.ca }}</span>
                   <el-icon class="copy-icon"><CopyDocument /></el-icon>
                 </span>
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
  WarningFilled,
  SuccessFilled,
  InfoFilled,
  Warning,
  CopyDocument,
  Link,
  Check,
  More
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useNotificationStore } from '@/store/modules/notification'
import BNBIcon from '@/assets/crypto-icons/BNB.png'

const router = useRouter()
const notificationStore = useNotificationStore()

// 获取最新通知
const getRecentNotifications = (limit = 20) => {
  return request({
    url: '/crypto/monitorAlert/recent',
    method: 'get',
    params: { limit }
  })
}

// 通知列表
const loading = ref(false)
const notifications = computed(() => notificationStore.list.slice(0, 20))

// 已读ID集合
const readIds = ref(new Set(JSON.parse(localStorage.getItem('readNotificationIds') || '[]')))

// 保存已读ID
const saveReadIds = () => {
  localStorage.setItem('readNotificationIds', JSON.stringify([...readIds.value]))
}

// 未读数量
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
  // BSC 统一用币安黄色
  return 'warning'
}

// 格式化时间（相对时间 + 完整时间戳）
const formatTimeWithDetails = (timeStr) => {
  if (!timeStr) return ''
  
  const time = new Date(timeStr)
  const now = new Date()
  const diff = Math.floor((now - time) / 1000) // 秒
  
  // 获取完整时间戳 YYYY-MM-DD HH:MM:SS
  const year = time.getFullYear()
  const month = (time.getMonth() + 1).toString().padStart(2, '0')
  const date = time.getDate().toString().padStart(2, '0')
  const hour = time.getHours().toString().padStart(2, '0')
  const minute = time.getMinutes().toString().padStart(2, '0')
  const second = time.getSeconds().toString().padStart(2, '0')
  const detailTime = `${year}-${month}-${date} ${hour}:${minute}:${second}`
  
  // 相对时间
  let relativeTime = ''
  if (diff < 60) relativeTime = '刚刚'
  else if (diff < 3600) relativeTime = `${Math.floor(diff / 60)}分钟前`
  else if (diff < 86400) relativeTime = `${Math.floor(diff / 3600)}小时前`
  else {
    // 昨天/前天/其他日期
    const isYesterday = new Date(now.getTime() - 86400000).toDateString() === time.toDateString()
    const isDayBefore = new Date(now.getTime() - 86400000 * 2).toDateString() === time.toDateString()
    
    if (isYesterday) relativeTime = '昨天'
    else if (isDayBefore) relativeTime = '前天'
    else relativeTime = `${Math.floor(diff / 86400)}天前`
  }
  
  return `${relativeTime} ${detailTime}`
}

// 加载通知列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await getRecentNotifications(20)
    if (response.code === 200 && response.data) {
      notificationStore.setNotifications(response.data)
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
  if (!item.isRead) {
    item.isRead = 1
    readIds.value.add(item.id)
    saveReadIds()
  }
}

// 标记全部已读
const handleMarkAllRead = async () => {
  notifications.value.forEach(item => {
    item.isRead = 1
    readIds.value.add(item.id)
  })
  saveReadIds()
  ElMessage.success('已全部标记为已读')
}

// 查看全部
const handleViewAll = () => {
  router.push('/crypto/blockMonitor')
}

// 复制 CA 地址
const handleCopyCA = async (ca) => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(ca)
      ElMessage.success('CA已复制')
    } else {
      const textarea = document.createElement('textarea')
      textarea.value = ca
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        ElMessage.success('CA已复制')
      } catch (err) {
        ElMessage.error('复制失败')
      }
      document.body.removeChild(textarea)
    }
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 复制 CA 并标记为已读
const handleCopyCAAndMarkRead = async (item) => {
  await handleCopyCA(item.ca)
  if (!item.isRead) {
    item.isRead = 1
    readIds.value.add(item.id)
    saveReadIds()
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  const value = Number(num)
  if (value >= 1e9) return (value / 1e9).toFixed(2) + 'B'
  if (value >= 1e6) return (value / 1e6).toFixed(2) + 'M'
  if (value >= 1e3) return (value / 1e3).toFixed(2) + 'K'
  return value.toFixed(2)
}

// 打开 GMGN 链接
const handleOpenGMGN = (ca, chainType = 'sol') => {
  const chain = chainType ? chainType.toLowerCase() : 'sol'
  const url = `https://gmgn.ai/${chain}/token/${ca}`
  window.open(url, '_blank')
}

let refreshTimer = null

onMounted(() => {
  loadNotifications()
  refreshTimer = setInterval(loadNotifications, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})

defineExpose({
  loadNotifications
})
</script>

<style scoped lang="scss">
.notification-center {
  height: 100%;
  
  .panel-card {
    border: none;
    background: #fff;
    border-radius: 8px;
    
    :deep(.el-card__header) {
      padding: 12px 16px;
      border-bottom: 1px solid #f0f2f5;
    }
    
    :deep(.el-card__body) {
      padding: 0;
    }
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
    
    .icon-box {
      width: 28px;
      height: 28px;
      background: transparent;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      
      .chain-icon {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }
    
    .title {
      font-size: 15px;
      font-weight: 600;
      color: #303133;
    }
    
    /* 未读数胶囊 */
    .unread-badge {
      height: 20px;
      line-height: 20px;
      padding: 0 6px;
      font-size: 11px;
      font-weight: 600;
      border-radius: 10px;
      
      &:deep(.el-tag__content) {
        font-weight: 600;
      }
    }
  }
  
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.notification-content {
  height: 400px;
  overflow-y: auto;
  background: #ffffff;
}

/* 紧凑模式列表 */
.notification-list {
  padding: 0;
  
  .notification-item {
    display: flex;
    gap: 8px;
    padding: 6px 16px;
    border-bottom: 1px solid #f5f7fa;
    cursor: pointer;
    transition: all 0.2s ease;
    position: relative;
    
    &:last-child {
      border-bottom: none;
    }
    
    /* 分组间隔 */
    &:nth-child(5n) {
      margin-bottom: 8px;
      border-bottom: 1px solid #e8eaed !important;
    }
    
    &:hover {
      background: #fffbf7;
      transform: translateX(2px);
      box-shadow: -2px 0 8px rgba(243, 186, 47, 0.15);
      transition: all 0.2s ease;
    }
    
    &.read {
      background: #fafbfc;
      opacity: 0.75;
    }
    
    &.unread {
      background: #fffbf7;
    }

    // 左侧指示器 - 仅未读显示
    .item-indicator {
      flex-shrink: 0;
      width: 3px;
      height: 100%;
      border-radius: 2px;
      transition: all 0.2s ease;
      background: transparent;
      
      &.bsc-indicator {
        background: #F3BA2F;
      }
    }

    // 右侧主体
    .item-body {
      flex: 1;
      min-width: 0;
      margin-left: 8px;

      .item-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1px;
        height: 20px;
        
        .meta-tags {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .module-tag {
            height: 18px;
            line-height: 16px;
            padding: 0 4px;
            border: none;
            font-weight: 600;
            background: #F3BA2F !important;
            color: #fff !important;
          }
          
          :deep(.module-tag) {
            background: #F3BA2F !important;
            color: #fff !important;
          }
          
          .time-label {
            font-size: 12px;
            color: #606266;
            font-family: monospace;
            font-weight: 500;
          }
        }
        
        .gmgn-link {
          font-size: 16px;
          color: #F3BA2F;
          cursor: pointer;
          transition: all 0.2s;
          
          &:hover {
            color: #FFC947;
            transform: scale(1.1);
          }
        }
      }
      
      .item-main-content {
        font-size: 13px;
        color: #606266;
        line-height: 1.4;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        margin-bottom: 1px;
        
        .title-text {
          font-weight: 600;
          color: #303133;
          
          &.strong-text {
            font-weight: 700;
            color: #000;
          }
        }
        
        .separator {
          margin: 0 6px;
          color: #dcdfe6;
        }
        
        .desc-text {
          color: #606266;
        }
      }
      
      .item-footer {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 12px;
        margin-bottom: 0;
        
        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;
          background: #f5f7fa;
          padding: 2px 8px;
          border-radius: 4px;
          
          .label { color: #606266; font-weight: 500; }
          .value { 
              color: #303133; 
              font-family: monospace;
              
              &.strong-text {
                  font-weight: 700;
                  color: #000;
              }
          }
          
          &.ca-item {
            cursor: pointer;
            background: #FEF5E7;
            transition: all 0.2s;
            
            &:hover {
              background: #FCECD1;
              color: #F3BA2F;
            }
          }
          
          &.clickable {
            cursor: pointer;
            transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
            background: #FEF5E7;
            position: relative;
            
            &:hover {
              background: #FCECD1;
              transform: scale(1.03);
              box-shadow: 0 2px 8px rgba(243, 186, 47, 0.2);
              
              .copy-icon {
                opacity: 1;
                color: #F3BA2F;
                animation: copyPulse 0.3s ease-out;
              }
            }
            
            &:active {
              transform: scale(0.98);
            }
            
            .copy-icon {
              font-size: 12px;
              opacity: 0.5;
              transition: all 0.2s;
              display: inline-block;
            }
          }
        }
      }
      
    }
  }
}

/* 暗黑模式适配 */
:root[class~="dark"] {
  .notification-center {
    .panel-card {
      background: #1d1e1f;
      border: 1px solid #363637;
      
      :deep(.el-card__header) {
        border-bottom: 1px solid #363637;
      }
    }
  }
  
  .notification-header {
    .header-left {
      .icon-box {
        background: transparent;
      }
      .title {
        color: #E5EAF3;
      }
    }
  }
  
  .notification-content {
    background: #1d1e1f;
  }
  
  .notification-list {
    .notification-item {
      border-bottom: 1px solid #363637;
      
      &:hover {
        background: #3d3230;
      }
      
      &.read {
        background: #262727;
        opacity: 0.7;
      }
      
      &.unread {
        background: #3d3230;
      }
      
      .item-actions {
        display: none;
      }
      
      .item-icon .dot {
        &.type-alert, &.type-error { background: #2b1d1d; color: #f56c6c; }
        &.type-warning { background: #292218; color: #e6a23c; }
        &.type-success { background: #1e2b1a; color: #67c23a; }
        &.type-info { background: #202121; color: #909399; }
      }
      
      .item-body {
        .item-header {
          .meta-tags {
            .time-label {
              color: #A3A6AD;
            }
          }
          
          .gmgn-link {
            color: #FFC947;
            
            &:hover {
              color: #FFD966;
            }
          }
        }
        
        .item-main-content {
          color: #A3A6AD;
          .title-text {
            color: #E5EAF3;
            
            &.strong-text {
              color: #ffffff;
            }
          }
          .separator { color: #4C4D4F; }
          .desc-text { color: #A3A6AD; }
        }
        
        .item-footer {
          .stat-item {
            background: #262727;
            .label { color: #A3A6AD; }
            .value { 
               color: #E5EAF3;
               
               &.strong-text {
                   color: #ffffff;
               }
            }
            
            &.clickable {
              background: #3d3320;
              
              &:hover {
                background: #4d4330;
                
                .copy-icon {
                  color: #FFC947;
                }
              }
            }
          }
        }
        
        .item-actions {
          :deep(.el-button) {
            &:not(:disabled) {
              color: #FFC947;
              
              &:hover {
                color: #FFD966;
                background: rgba(255, 201, 71, 0.1);
              }
            }
          }
        }
      }
    }
  }
}

/* 自定义滚动条 */
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #e4e7ed;
  border-radius: 2px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

/* 复制按钮脉冲动画 */
@keyframes copyPulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .notification-list {
    .notification-item {
      .item-body {
        .item-footer {
          .stat-item.ca-item {
            /* CA 项改成紧凑胶囊样式 */
            background: transparent;
            padding: 0;
            gap: 2px;
            
            .label {
              display: none; /* 隐藏"CA:"前缀 */
            }
            
            .value {
              display: none; /* 隐藏 CA 地址本身 */
            }
            
            .copy-icon {
              font-size: 14px;
              opacity: 1;
              color: #F3BA2F;
            }
          }
        }
      }
    }
  }
}
</style>
