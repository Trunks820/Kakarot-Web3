<template>
  <el-card class="alert-panel">
    <template #header>
      <div class="panel-header">
        <div class="header-left">
          <el-icon><Warning /></el-icon>
          <span>预警中心</span>
        </div>
        <div class="header-right">
          <el-tag :type="getAlertLevelType()" size="small">
            {{ getPendingCount() }}条待处理
          </el-tag>
        </div>
      </div>
    </template>

    <div class="alert-content">
      <!-- 预警列表 -->
      <div class="alert-list">
        <div v-for="alert in recentAlerts" :key="alert.id" class="alert-item" 
             @click="handleAlertClick(alert)">
          <div class="alert-icon">
            <el-icon :class="getAlertIconClass(alert.level)">
              <component :is="getAlertIcon(alert.level)" />
            </el-icon>
          </div>
          
          <div class="alert-info">
            <div class="alert-main">
              <span class="alert-token">{{ alert.token }}</span>
              <span class="alert-value" :class="getValueClass(alert.value)">{{ alert.value }}</span>
            </div>
            <div class="alert-meta">
              <span class="alert-type">{{ alert.type }}</span>
              <span class="alert-time">{{ formatTime(alert.time) }}</span>
            </div>
          </div>
          
          <div class="alert-status">
            <div class="status-indicator" :class="alert.level">
              <el-icon>
                <component :is="getAlertIcon(alert.level)" />
              </el-icon>
            </div>
            <div class="alert-actions">
              <el-button v-if="alert.status === 'pending'" size="small" type="text" 
                         @click.stop="handleProcess(alert)">
                处理
              </el-button>
              <span v-else class="processed-text">已处理</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="recentAlerts.length === 0" class="empty-state">
        <el-icon><CircleCheck /></el-icon>
        <p>暂无预警信息</p>
      </div>

      <!-- 底部操作 -->
      <div class="panel-footer">
        <el-button type="text" @click="viewAllAlerts">
          查看全部预警
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup name="AlertPanel">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Warning, CircleCloseFilled, WarningFilled, InfoFilled, 
  CircleCheck, ArrowRight 
} from '@element-plus/icons-vue'

// 模拟预警数据
const recentAlerts = ref([
  {
    id: 1,
    type: '价格预警',
    token: 'PEPE',
    level: 'high',
    description: '价格上涨超过50%，当前价格 $0.00001234',
    value: '+68.5%',
    time: Date.now() - 5 * 60 * 1000,
    status: 'pending'
  },
  {
    id: 2,
    type: '流动性预警',
    token: 'DOGE',
    level: 'medium',
    description: '流动性大幅流出，24小时流出 $2.3M',
    value: '-$2.3M',
    time: Date.now() - 15 * 60 * 1000,
    status: 'pending'
  },
  {
    id: 3,
    type: '交易量预警',
    token: 'SHIB',
    level: 'low',
    description: '24小时交易量异常增长',
    value: '+156%',
    time: Date.now() - 30 * 60 * 1000,
    status: 'processed'
  },
  {
    id: 4,
    type: '市值预警',
    token: 'BONK',
    level: 'high',
    description: '市值快速下跌，跌幅超过预警阈值',
    value: '-45.2%',
    time: Date.now() - 45 * 60 * 1000,
    status: 'pending'
  }
])

// 计算待处理数量
const getPendingCount = () => {
  return recentAlerts.value.filter(alert => alert.status === 'pending').length
}

// 获取预警级别类型
const getAlertLevelType = () => {
  const pendingCount = getPendingCount()
  if (pendingCount >= 3) return 'danger'
  if (pendingCount >= 1) return 'warning'
  return 'success'
}

// 获取预警图标
const getAlertIcon = (level) => {
  switch (level) {
    case 'high': return CircleCloseFilled
    case 'medium': return WarningFilled
    default: return InfoFilled
  }
}

// 获取预警图标样式
const getAlertIconClass = (level) => {
  return `alert-icon-${level}`
}

// 移除了图片相关函数，暂时使用图标替代

// 获取价值样式
const getValueClass = (value) => {
  if (value.includes('+')) return 'positive'
  if (value.includes('-')) return 'negative'
  return 'neutral'
}

// 格式化时间
const formatTime = (timestamp) => {
  const now = Date.now()
  const diff = Math.floor((now - timestamp) / 1000 / 60) // 分钟差

  if (diff < 60) {
    return `${diff}分钟前`
  } else if (diff < 1440) {
    return `${Math.floor(diff / 60)}小时前`
  } else {
    return `${Math.floor(diff / 1440)}天前`
  }
}

// 处理预警点击
const handleAlertClick = (alert) => {
  ElMessage.info(`查看预警详情: ${alert.token} ${alert.type}`)
}

// 处理预警
const handleProcess = (alert) => {
  alert.status = 'processed'
  ElMessage.success(`已处理预警: ${alert.token} ${alert.type}`)
}

// 查看全部预警
const viewAllAlerts = () => {
  ElMessage.info('跳转到预警管理页面')
}

// 模拟新预警
onMounted(() => {
  let alertId = 5
  setInterval(() => {
    if (Math.random() > 0.95) { // 5%概率生成新预警
      const tokens = ['BTC', 'ETH', 'SOL', 'MATIC', 'AVAX']
      const types = ['价格预警', '流动性预警', '交易量预警', '市值预警']
      const levels = ['high', 'medium', 'low']
      
      const newAlert = {
        id: alertId++,
        type: types[Math.floor(Math.random() * types.length)],
        token: tokens[Math.floor(Math.random() * tokens.length)],
        level: levels[Math.floor(Math.random() * levels.length)],
        description: '检测到异常活动，请及时关注',
        value: `+${(Math.random() * 100).toFixed(1)}%`,
        time: Date.now(),
        status: 'pending'
      }
      
      recentAlerts.value.unshift(newAlert)
      
      // 只保留最新的4条
      if (recentAlerts.value.length > 4) {
        recentAlerts.value.pop()
      }
    }
  }, 10000) // 每10秒检查一次
})
</script>

<style scoped lang="scss">
.alert-panel {
  height: 400px;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 0;
  }

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .el-icon {
        color: var(--el-color-warning);
      }
    }
  }

  .alert-content {
    padding: 0;
    height: calc(100% - 60px);
    display: flex;
    flex-direction: column;

    .alert-list {
      flex: 1;
      overflow-y: auto;
      padding: 0 20px;

      .alert-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 12px 0;
        min-height: 70px;
        border-bottom: 1px solid var(--el-border-color-lighter);
        cursor: pointer;
        transition: background-color 0.2s ease;

        &:hover {
          background: var(--el-bg-color-page);
        }

        &:last-child {
          border-bottom: none;
        }

        .alert-icon {
          width: 32px;
          height: 32px;
          flex-shrink: 0;

          img {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            object-fit: cover;
          }

          .el-icon {
            font-size: 16px;
          }

          .alert-icon-high {
            background: var(--el-color-danger-light-9);
            color: var(--el-color-danger);
          }

          .alert-icon-medium {
            background: var(--el-color-warning-light-9);
            color: var(--el-color-warning);
          }

          .alert-icon-low {
            background: var(--el-color-info-light-9);
            color: var(--el-color-info);
          }
        }

        .alert-info {
          flex: 1;
          min-width: 0;

          .alert-main {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2px;

            .alert-token {
              font-weight: 600;
              color: var(--el-text-color-primary);
              font-size: 14px;
            }

            .alert-value {
              font-size: 13px;
              font-weight: 500;
              font-family: 'Courier New', monospace;

              &.positive {
                color: var(--el-color-success);
              }

              &.negative {
                color: var(--el-color-danger);
              }

              &.neutral {
                color: var(--el-text-color-regular);
              }
            }
          }

          .alert-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .alert-type {
              color: var(--el-text-color-secondary);
              font-size: 12px;
            }

            .alert-time {
              color: var(--el-text-color-secondary);
              font-size: 11px;
            }
          }
        }

        .alert-status {
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 2px;
          flex-shrink: 0;

          .status-indicator {
            width: 20px;
            height: 20px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;

            .el-icon {
              font-size: 12px;
            }

            &.high {
              background: var(--el-color-danger-light-9);
              color: var(--el-color-danger);
            }

            &.medium {
              background: var(--el-color-warning-light-9);
              color: var(--el-color-warning);
            }

            &.low {
              background: var(--el-color-info-light-9);
              color: var(--el-color-info);
            }
          }

          .alert-actions {
            .el-button {
              font-size: 11px;
              padding: 2px 6px;
            }

            .processed-text {
              font-size: 11px;
              color: var(--el-color-success);
            }
          }
        }
      }
    }

    .empty-state {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--el-text-color-secondary);
      padding: 0 20px;

      .el-icon {
        font-size: 48px;
        color: var(--el-color-success);
        margin-bottom: 12px;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }

    .panel-footer {
      padding: 12px 20px;
      border-top: 1px solid var(--el-border-color-lighter);
      text-align: center;
      flex-shrink: 0;

      .el-button {
        color: var(--el-color-primary);
        font-size: 13px;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .alert-panel {
    .alert-content .alert-list .alert-item {
      .alert-content-main {
        .alert-description {
          display: none;
        }
      }
    }
  }
}
</style> 