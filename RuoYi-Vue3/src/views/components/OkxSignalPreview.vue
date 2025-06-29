<template>
  <el-card class="okx-signal-preview">
    <template #header>
      <div class="panel-header">
        <div class="header-left">
          <el-icon><DataLine /></el-icon>
          <span>OKX信号预览</span>
        </div>
        <div class="header-right">
          <el-tag type="primary" size="small">实时更新</el-tag>
        </div>
      </div>
    </template>

    <div class="signal-content">
      <!-- 信号列表 -->
      <div class="signal-list">
        <div v-for="signal in latestSignals" :key="signal.id" 
             class="signal-item" @click="handleSignalClick(signal)">
                     <div class="signal-header">
             <div class="signal-token">
               <span class="token-symbol">{{ signal.symbol }}</span>
               <el-tag :type="getSignalType(signal.signalType)" size="small">
                 {{ getSignalTypeText(signal.signalType) }}
               </el-tag>
             </div>
             <div class="signal-time">{{ formatTime(signal.timestamp) }}</div>
           </div>
           
           <div class="signal-body">
             <div class="signal-price">
               <span class="label">价格:</span>
               <span class="value">${{ signal.price }}</span>
             </div>
             <div class="signal-change">
               <span class="label">24h涨跌:</span>
               <span class="value" :class="getChangeClass(signal.change24h)">
                 {{ signal.change24h > 0 ? '+' : '' }}{{ signal.change24h.toFixed(2) }}%
               </span>
             </div>
           </div>
           
           <div class="signal-footer">
             <div class="smart-money">
               <el-icon><User /></el-icon>
               <span>{{ signal.smartMoney.length }}个聪明钱</span>
             </div>
             <div class="signal-volume">
               <span class="volume-label">24h交易量:</span>
               <span class="volume-value">{{ formatVolume(signal.volume24h) }}</span>
             </div>
           </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="latestSignals.length === 0" class="empty-state">
        <el-icon><DataAnalysis /></el-icon>
        <p>暂无OKX信号数据</p>
      </div>

      <!-- 底部操作 -->
      <div class="panel-footer">
        <el-button type="text" @click="viewAllSignals">
          查看完整信号
          <el-icon><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup name="OkxSignalPreview">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  DataLine, User, DataAnalysis, ArrowRight 
} from '@element-plus/icons-vue'

// 真实OKX信号数据
const latestSignals = ref([
  {
    id: 1,
    ca: 'HeLp6NuQkmYB4pYWo2zYs22mESHXPQYzXbB8n4V98jwC',
    symbol: 'PEPE',
    signalType: 'buy',
    chain: 'SOL',
    price: '0.00001234',
    change24h: 15.67,
    smartMoney: [
      'GDvKRMakqM1jBoVtiVY36KhQ5eJ4h7W5jKYmTcPmQTGK',
      'Hf4daCT7tCq6VyMn5NpPv1LY1LG6YfJPbMrqwLzhQ1az',
      'K2x8DuNqaP3Yt9VgQjFpL8nR5wEr7mH4cN6sT1bA9oZx'
    ],
    volume24h: 2340000,
    marketCap: 12450000,
    timestamp: Date.now() - 2 * 60 * 1000,
    strength: 85
  },
  {
    id: 2,
    ca: 'EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v',
    symbol: 'DOGE',
    signalType: 'sell', 
    chain: 'SOL',
    price: '0.082156',
    change24h: -8.45,
    smartMoney: [
      'B3xQ9tCrP8fG2mN1VsWjY4nE6dR7sA5oK9vX0z',
      'C4yR0wDsQ9gH3nO2XtYkZ5oF7eS8tB6pL0wY1a'
    ],
    volume24h: 890000,
    marketCap: 8900000,
    timestamp: Date.now() - 8 * 60 * 1000,
    strength: 72
  },
  {
    id: 3,
    ca: 'CXk2AMBfi3TwaEL2468s6zP8xq9NxTXjp9gjMgzeUynFS',
    symbol: 'SHIB',
    signalType: 'buy',
    chain: 'SOL', 
    price: '0.000012',
    change24h: 12.34,
    smartMoney: [
      'D5zS1xFtQ0hJ4pP3YuZlA6rG8cV9nM2bX7wE0k',
      'E6aT2yGsR1iK5qQ4ZvAmB7sH9dW0oN3cY8xF1l',
      'F7bU3zHtS2jL6rR5AwBnC8tI0eX1pO4dZ9yG2m'
    ],
    volume24h: 2100000,
    marketCap: 15600000,
    timestamp: Date.now() - 15 * 60 * 1000,
    strength: 91
  },
  {
    id: 4,
    ca: 'DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263',
    symbol: 'BONK',
    signalType: 'watch',
    chain: 'SOL',
    price: '0.000034', 
    change24h: -3.21,
    smartMoney: [
      'G8cV4aIsU3kM7sS6CwDrF9uH2fY5nQ0bZ1xH3o'
    ],
    volume24h: 560000,
    marketCap: 4500000,
    timestamp: Date.now() - 25 * 60 * 1000,
    strength: 45
  }
])

// 获取信号类型样式
const getSignalType = (signalType) => {
  switch (signalType) {
    case 'buy': return 'success'
    case 'sell': return 'danger'
    case 'watch': return 'warning'
    default: return 'info'
  }
}

// 获取信号类型文本
const getSignalTypeText = (signalType) => {
  switch (signalType) {
    case 'buy': return '买入'
    case 'sell': return '卖出'
    case 'watch': return '观察'
    default: return '未知'
  }
}

// 获取涨跌样式
const getChangeClass = (change) => {
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
}

// 格式化交易量
const formatVolume = (volume) => {
  if (volume >= 1000000) {
    return `$${(volume / 1000000).toFixed(1)}M`
  } else if (volume >= 1000) {
    return `$${(volume / 1000).toFixed(1)}K`
  }
  return `$${volume}`
}

// 获取强度颜色
const getStrengthColor = (strength) => {
  if (strength >= 80) return '#67C23A' // 绿色
  if (strength >= 60) return '#E6A23C' // 黄色
  if (strength >= 40) return '#F56C6C' // 红色
  return '#909399' // 灰色
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

// 处理信号点击
const handleSignalClick = (signal) => {
  ElMessage.info(`查看 ${signal.symbol} 信号详情`)
}

// 查看全部信号
const viewAllSignals = () => {
  ElMessage.info('跳转到OKX信号页面')
}

// 模拟实时信号更新
onMounted(() => {
  let signalId = 5
  setInterval(() => {
    if (Math.random() > 0.92) { // 8%概率生成新信号
      const symbols = ['BTC', 'ETH', 'SOL', 'AVAX', 'MATIC']
      const signalTypes = ['buy', 'sell', 'watch']
      const chains = ['SOL', 'ETH', 'BSC']
      
      // 生成随机CA地址
      const generateRandomCA = () => {
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
        let result = ''
        for (let i = 0; i < 44; i++) {
          result += chars.charAt(Math.floor(Math.random() * chars.length))
        }
        return result
      }
      
      // 生成随机聪明钱地址
      const generateSmartMoneyAddresses = (count) => {
        const addresses = []
        for (let i = 0; i < count; i++) {
          addresses.push(generateRandomCA())
        }
        return addresses
      }
      
      const smartMoneyCount = Math.floor(Math.random() * 5) + 1 // 1-5个聪明钱
      
      const newSignal = {
        id: signalId++,
        ca: generateRandomCA(),
        symbol: symbols[Math.floor(Math.random() * symbols.length)],
        signalType: signalTypes[Math.floor(Math.random() * signalTypes.length)],
        chain: chains[Math.floor(Math.random() * chains.length)],
        price: (Math.random() * 10).toFixed(6),
        change24h: (Math.random() - 0.5) * 40, // -20% 到 +20%
        smartMoney: generateSmartMoneyAddresses(smartMoneyCount),
        volume24h: Math.floor(Math.random() * 5000000) + 100000, // 10万到500万
        marketCap: Math.floor(Math.random() * 50000000) + 1000000, // 100万到5000万
        timestamp: Date.now(),
        strength: Math.floor(Math.random() * 60) + 40 // 40-100%
      }
      
      latestSignals.value.unshift(newSignal)
      
      // 只保留最新的4条
      if (latestSignals.value.length > 4) {
        latestSignals.value.pop()
      }
    }
  }, 8000) // 每8秒检查一次
})
</script>

<style scoped lang="scss">
.okx-signal-preview {
  height: 400px;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
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

  .signal-content {
    padding: 0;
    height: calc(100% - 60px);
    display: flex;
    flex-direction: column;

    .signal-list {
      flex: 1;
      overflow-y: auto;
      padding: 0 20px;

      .signal-item {
        padding: 16px 0;
        border-bottom: 1px solid var(--el-border-color-lighter);
        cursor: pointer;
        transition: background-color 0.2s ease;

        &:hover {
          background: var(--el-bg-color-page);
        }

        &:last-child {
          border-bottom: none;
        }

        .signal-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;

          .signal-token {
            display: flex;
            align-items: center;
            gap: 8px;

            .token-symbol {
              font-weight: 600;
              color: var(--el-text-color-primary);
              font-size: 16px;
            }
          }

          .signal-time {
            color: var(--el-text-color-secondary);
            font-size: 12px;
          }
        }

        .signal-body {
          display: flex;
          justify-content: space-between;
          margin-bottom: 8px;

          .signal-price,
          .signal-change {
            display: flex;
            align-items: center;
            gap: 4px;

            .label {
              color: var(--el-text-color-secondary);
              font-size: 12px;
            }

            .value {
              font-weight: 500;
              font-size: 13px;

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
        }

        .signal-footer {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .smart-money {
            display: flex;
            align-items: center;
            gap: 4px;
            color: var(--el-text-color-secondary);
            font-size: 12px;

            .el-icon {
              font-size: 14px;
            }
          }

                     .signal-volume {
             display: flex;
             flex-direction: column;
             align-items: flex-end;

             .volume-label {
               color: var(--el-text-color-secondary);
               font-size: 11px;
             }

             .volume-value {
               color: var(--el-color-primary);
               font-size: 12px;
               font-weight: 500;
               font-family: 'Courier New', monospace;
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

      .el-icon {
        font-size: 48px;
        color: var(--el-color-info);
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

      .el-button {
        color: var(--el-color-primary);
        font-size: 13px;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .okx-signal-preview {
    height: auto;
    min-height: 300px;
    
    :deep(.el-card__header) {
      padding: 12px 16px;
    }
    
    .panel-header {
      .header-left {
        gap: 6px;
        font-size: 14px;
        
        .el-icon {
          font-size: 16px;
        }
      }
      
      .el-tag {
        font-size: 10px;
        height: 16px;
        line-height: 16px;
        padding: 0 4px;
      }
    }
    
    .signal-content {
      .signal-list {
        padding: 0 16px;
        
        .signal-item {
          padding: 12px 0;
          
          .signal-header {
            margin-bottom: 6px;
            
            .signal-token {
              gap: 6px;
              
              .token-symbol {
                font-size: 14px;
              }
            }
            
            .signal-time {
              font-size: 11px;
            }
          }
          
          .signal-body {
            margin-bottom: 6px;
            
            .signal-price,
            .signal-change {
              gap: 3px;
              
              .label {
                font-size: 11px;
              }
              
              .value {
                font-size: 12px;
              }
            }
          }
          
          .signal-footer {
            flex-direction: column;
            gap: 6px;
            align-items: flex-start;
            
            .smart-money {
              gap: 3px;
              font-size: 11px;
              
              .el-icon {
                font-size: 12px;
              }
            }
            
            .signal-volume {
              align-items: flex-start;
              
              .volume-label {
                font-size: 10px;
              }
              
              .volume-value {
                font-size: 11px;
              }
            }
          }
        }
      }
      
      .empty-state {
        .el-icon {
          font-size: 36px;
          margin-bottom: 8px;
        }
        
        p {
          font-size: 13px;
        }
      }
      
      .panel-footer {
        padding: 10px 16px;
        
        .el-button {
          font-size: 12px;
        }
      }
    }
  }
}

/* 小屏幕设备优化 */
@media (max-width: 480px) {
  .okx-signal-preview {
    min-height: 250px;
    
    :deep(.el-card__header) {
      padding: 10px 12px;
    }
    
    .panel-header {
      .header-left {
        font-size: 13px;
        
        .el-icon {
          font-size: 14px;
        }
      }
      
      .el-tag {
        font-size: 9px;
        height: 14px;
        line-height: 14px;
        padding: 0 3px;
      }
    }
    
    .signal-content {
      .signal-list {
        padding: 0 12px;
        
        .signal-item {
          padding: 10px 0;
          
          .signal-header {
            margin-bottom: 4px;
            
            .signal-token {
              gap: 4px;
              
              .token-symbol {
                font-size: 13px;
              }
            }
            
            .signal-time {
              font-size: 10px;
            }
          }
          
          .signal-body {
            margin-bottom: 4px;
            
            .signal-price,
            .signal-change {
              gap: 2px;
              
              .label {
                font-size: 10px;
              }
              
              .value {
                font-size: 11px;
              }
            }
          }
          
          .signal-footer {
            gap: 4px;
            
            .smart-money {
              gap: 2px;
              font-size: 10px;
              
              .el-icon {
                font-size: 11px;
              }
            }
            
            .signal-volume {
              .volume-label {
                font-size: 9px;
              }
              
              .volume-value {
                font-size: 10px;
              }
            }
          }
        }
      }
      
      .empty-state {
        .el-icon {
          font-size: 32px;
          margin-bottom: 6px;
        }
        
        p {
          font-size: 12px;
        }
      }
      
      .panel-footer {
        padding: 8px 12px;
        
        .el-button {
          font-size: 11px;
        }
      }
    }
  }
}
</style> 