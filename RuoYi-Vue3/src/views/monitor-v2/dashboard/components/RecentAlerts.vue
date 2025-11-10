<!-- 最近告警列表 -->
<template>
  <div class="recent-alerts">
    <el-empty v-if="alerts.length === 0" description="暂无告警记录" :image-size="80">
      <template #image>
        <el-icon :size="80" color="#67C23A"><SuccessFilled /></el-icon>
      </template>
    </el-empty>
    
    <div v-else class="alert-list">
      <div
        v-for="alert in alerts"
        :key="alert.id"
        class="alert-item"
        @click="handleAlertClick(alert)"
      >
        <div class="alert-icon">
          <el-icon :size="20" :color="getAlertColor(alert.level)">
            <Warning />
          </el-icon>
        </div>
        <div class="alert-content">
          <div class="alert-title">{{ alert.tokenSymbol || 'Unknown Token' }}</div>
          <div class="alert-desc">{{ alert.message }}</div>
          <div class="alert-footer">
            <el-tag :type="getAlertTagType(alert.level)" size="small">
              {{ alert.levelText }}
            </el-tag>
            <span class="alert-time">{{ formatTime(alert.alertTime) }}</span>
          </div>
        </div>
        <div class="alert-action">
          <el-button text type="primary" size="small">
            查看详情
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Warning, SuccessFilled } from '@element-plus/icons-vue'

defineProps({
  alerts: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['alert-click'])

const getAlertColor = (level) => {
  const colors = {
    high: '#F56C6C',
    medium: '#E6A23C',
    low: '#409EFF'
  }
  return colors[level] || '#909399'
}

const getAlertTagType = (level) => {
  const types = {
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return types[level] || 'info'
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const time = new Date(timeStr)
  const now = new Date()
  const diff = now - time
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  return `${days}天前`
}

const handleAlertClick = (alert) => {
  emit('alert-click', alert)
}
</script>

<style scoped lang="scss">
.recent-alerts {
  .alert-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .alert-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: var(--el-fill-color-light);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: var(--el-fill-color);
      transform: translateX(4px);
    }
    
    .alert-icon {
      flex-shrink: 0;
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--el-bg-color);
      border-radius: 50%;
    }
    
    .alert-content {
      flex: 1;
      min-width: 0;
      
      .alert-title {
        font-size: 14px;
        font-weight: 600;
        color: var(--el-text-color-primary);
        margin-bottom: 4px;
      }
      
      .alert-desc {
        font-size: 13px;
        color: var(--el-text-color-regular);
        margin-bottom: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .alert-footer {
        display: flex;
        align-items: center;
        gap: 8px;
        
        .alert-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
        }
      }
    }
    
    .alert-action {
      flex-shrink: 0;
    }
  }
}
</style>

