<template>
  <el-card class="monitor-tokens">
    <template #header>
      <div class="panel-header">
        <div class="header-left">
          <el-icon><Monitor /></el-icon>
          <span>监控代币</span>
        </div>
        <div class="header-right">
          <el-tag type="success" size="small">{{ activeCount }}个活跃</el-tag>
        </div>
      </div>
    </template>

    <div class="tokens-content">
      <!-- 代币列表 -->
      <div class="tokens-list">
        <div v-for="token in monitorTokens" :key="token.symbol" 
             class="token-item" @click="handleTokenClick(token)">
          <div class="token-icon">
            <img :src="token.logo" :alt="token.symbol" @error="handleImageError">
          </div>
          
          <div class="token-info">
            <div class="token-main">
              <span class="token-symbol">{{ token.symbol }}</span>
              <span class="token-price">${{ token.price }}</span>
            </div>
            <div class="token-meta">
              <span class="token-change" :class="getChangeClass(token.change)">
                {{ token.change > 0 ? '+' : '' }}{{ token.change.toFixed(2) }}%
              </span>
              <span class="token-volume">${{ formatVolume(token.volume) }}</span>
            </div>
          </div>
          
          <div class="token-status">
            <div class="status-indicator" :class="token.alertStatus">
              <el-icon>
                <component :is="getStatusIcon(token.alertStatus)" />
              </el-icon>
            </div>
            <div class="monitor-actions">
              <el-button size="small" type="text" @click.stop="toggleMonitor(token)">
                {{ token.isMonitoring ? '停止' : '监控' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="monitorTokens.length === 0" class="empty-state">
        <el-icon><Coin /></el-icon>
        <p>暂无监控代币</p>
        <el-button type="primary" size="small" @click="addToken">添加监控</el-button>
      </div>

      <!-- 底部操作 -->
      <div class="panel-footer">
        <el-button type="text" @click="manageTokens">
          管理监控代币
          <el-icon><Setting /></el-icon>
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup name="MonitorTokens">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Monitor, Coin, Setting, SuccessFilled, WarningFilled, 
  CircleCloseFilled, InfoFilled 
} from '@element-plus/icons-vue'

// 模拟监控代币数据
const monitorTokens = ref([
  {
    symbol: 'PEPE',
    price: '0.00001234',
    change: 12.45,
    volume: 1250000,
    logo: '/src/assets/crypto-icons/BTC.png',
    isMonitoring: true,
    alertStatus: 'normal'
  },
  {
    symbol: 'DOGE',
    price: '0.082156',
    change: -5.67,
    volume: 890000,
    logo: '/src/assets/crypto-icons/BNB.png',
    isMonitoring: true,
    alertStatus: 'warning'
  },
  {
    symbol: 'SHIB',
    price: '0.000012',
    change: 8.91,
    volume: 2100000,
    logo: '/src/assets/crypto-icons/ETH.png',
    isMonitoring: true,
    alertStatus: 'normal'
  },
  {
    symbol: 'BONK',
    price: '0.000034',
    change: -15.23,
    volume: 560000,
    logo: '/src/assets/crypto-icons/SOL.png',
    isMonitoring: true,
    alertStatus: 'danger'
  }
])

// 计算活跃监控数量
const activeCount = computed(() => {
  return monitorTokens.value.filter(token => token.isMonitoring).length
})

// 获取涨跌样式
const getChangeClass = (change) => {
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
}

// 获取状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case 'normal': return SuccessFilled
    case 'warning': return WarningFilled
    case 'danger': return CircleCloseFilled
    default: return InfoFilled
  }
}

// 格式化交易量
const formatVolume = (volume) => {
  if (volume >= 1000000) {
    return (volume / 1000000).toFixed(1) + 'M'
  } else if (volume >= 1000) {
    return (volume / 1000).toFixed(1) + 'K'
  }
  return volume.toString()
}

// 处理图片错误
const handleImageError = (e) => {
  e.target.src = '/src/assets/crypto-icons/DEFAULT.png'
}

// 处理代币点击
const handleTokenClick = (token) => {
  ElMessage.info(`查看 ${token.symbol} 详细信息`)
}

// 切换监控状态
const toggleMonitor = (token) => {
  token.isMonitoring = !token.isMonitoring
  const status = token.isMonitoring ? '开启' : '停止'
  ElMessage.success(`${status}监控 ${token.symbol}`)
}

// 添加代币
const addToken = () => {
  ElMessage.info('跳转到添加监控代币页面')
}

// 管理代币
const manageTokens = () => {
  ElMessage.info('跳转到监控代币管理页面')
}

// 模拟价格更新
onMounted(() => {
  setInterval(() => {
    monitorTokens.value.forEach(token => {
      if (token.isMonitoring) {
        const changePercent = (Math.random() - 0.5) * 2
        const oldPrice = parseFloat(token.price)
        const newPrice = oldPrice * (1 + changePercent / 100)
        token.price = newPrice.toFixed(6)
        token.change += changePercent * 0.1
      }
    })
  }, 3000)
})
</script>

<style scoped lang="scss">
.monitor-tokens {
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
        color: var(--el-color-primary);
      }
    }
  }

  .tokens-content {
    padding: 0;
    height: calc(100% - 60px);
    display: flex;
    flex-direction: column;

    .tokens-list {
      flex: 1;
      overflow-y: auto;
      padding: 0 20px;

      .token-item {
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

        .token-icon {
          width: 32px;
          height: 32px;
          flex-shrink: 0;

          img {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            object-fit: cover;
          }
        }

        .token-info {
          flex: 1;
          min-width: 0;

          .token-main {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2px;

            .token-symbol {
              font-weight: 600;
              color: var(--el-text-color-primary);
              font-size: 14px;
            }

            .token-price {
              color: var(--el-text-color-primary);
              font-size: 13px;
              font-family: 'Courier New', monospace;
            }
          }

          .token-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .token-change {
              font-size: 12px;
              font-weight: 500;

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

            .token-volume {
              color: var(--el-text-color-secondary);
              font-size: 11px;
            }
          }
        }

        .token-status {
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

            &.normal {
              background: var(--el-color-success-light-9);
              color: var(--el-color-success);
            }

            &.warning {
              background: var(--el-color-warning-light-9);
              color: var(--el-color-warning);
            }

            &.danger {
              background: var(--el-color-danger-light-9);
              color: var(--el-color-danger);
            }

            &.info {
              background: var(--el-color-info-light-9);
              color: var(--el-color-info);
            }
          }

          .monitor-actions {
            .el-button {
              font-size: 11px;
              padding: 2px 6px;
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
      gap: 12px;

      .el-icon {
        font-size: 48px;
        color: var(--el-color-info);
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
  .monitor-tokens {
    .tokens-content .tokens-list .token-item {
      .token-info .token-meta {
        .token-volume {
          display: none;
        }
      }
    }
  }
}
</style> 