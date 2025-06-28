<template>
  <div class="okx-signals-page">
    <!-- 顶部筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <div class="chain-selector">
          <img src="https://static.oklink.com/cdn/wallet/logo/SOL-20220525.png" alt="Solana" class="chain-icon">
          <span>Solana</span>
        </div>
      </div>
      
      <div class="filter-center">
        <el-radio-group v-model="currentFilter" @change="onFilterChange" class="trend-filters">
          <el-radio-button label="">趋势总览</el-radio-button>
          <el-radio-button label="买入">买入</el-radio-button>
          <el-radio-button label="卖出">卖出</el-radio-button>
        </el-radio-group>
      </div>
      
      <div class="filter-right">
        <el-button type="text" class="filter-btn">预警市值</el-button>
        <el-button type="text" class="filter-btn">预警流动性</el-button>
        <el-button type="text" class="filter-btn">筛选</el-button>
      </div>
    </div>

    <!-- 统计信息 -->
    <div class="statistics-bar" v-if="statistics">
      <div class="stat-item">
        <span class="stat-label">今日信号:</span>
        <span class="stat-value">{{ statistics.todayTotal }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">买入:</span>
        <span class="stat-value buy">{{ statistics.buySignals }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">卖出:</span>
        <span class="stat-value sell">{{ statistics.sellSignals }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">活跃代币:</span>
        <span class="stat-value">{{ statistics.activeTokens }}</span>
      </div>
    </div>

    <!-- 信号列表 -->
    <div class="signals-container" v-loading="loading">
      <div v-for="signal in signalList" :key="signal.id" class="signal-item">
        <!-- 左侧信息 -->
        <div class="signal-left">
          <div class="operation-badge" :class="signal.trendText === '买入' ? 'buy' : 'sell'">
            {{ signal.trendText }}
          </div>
          <div class="signal-time">
            {{ formatTime(signal.eventTime) }}
          </div>
          
          <div class="signal-main">
            <div class="token-info">
              <img :src="signal.tokenInfo.tokenLogoUrl" :alt="signal.tokenInfo.tokenSymbol" 
                   class="token-logo" @error="handleImageError">
              <img :src="signal.tokenInfo.chainLogo" alt="chain" class="chain-logo">
            </div>
            
            <div class="signal-details">
              <div class="address-info">
                <span class="address-count clickable" @click="showAddressDetails(signal)">
                  {{ signal.addressNum }} 个聪明钱地址
                </span>
                <span class="time-info">在 {{ formatDuration(signal.duration) }} 内{{ signal.trendText }}</span>
                <span class="amount">${{ signal.volume }} {{ signal.tokenInfo.tokenSymbol }}</span>
                
                <!-- CA地址展示 -->
                <div class="ca-address-row">
                  <span class="ca-label">CA:</span>
                  <span class="ca-address" @click="copyToClipboard(signal.tokenInfo.tokenContractAddress)">
                    {{ formatAddress(signal.tokenInfo.tokenContractAddress) }}
                    <el-icon class="copy-icon"><DocumentCopy /></el-icon>
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧信息 -->
        <div class="signal-right">
          <div class="price-info">
            <div class="price-item">
              <span class="price-label">预警价格</span>
              <span class="price-value">${{ signal.price }}</span>
            </div>
            <div class="price-item">
              <span class="price-label">预警流动性</span>
              <span class="price-value">${{ signal.liquidity }}</span>
            </div>
            <div class="price-item">
              <span class="price-label">预警市值</span>
              <span class="price-value">${{ signal.mcap }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && signalList.length === 0" class="empty-state">
        <el-empty description="暂无信号数据" />
      </div>
    </div>

    <!-- 加载更多 -->
    <div class="load-more" v-if="hasMore && !loading">
      <el-button @click="loadMore" :loading="loadingMore">
        加载更多
      </el-button>
    </div>

    <!-- 钱包地址详情弹窗 -->
    <el-dialog
      v-model="addressDialogVisible"
      title="聪明钱地址详情"
      width="600px"
      :before-close="closeAddressDialog"
    >
      <div class="address-dialog-content" v-if="currentSignal">
        <div class="dialog-header">
          <h4>{{ currentSignal.tokenInfo.tokenSymbol }} - {{ currentSignal.trendText }}信号</h4>
          <p class="signal-summary">
            {{ currentSignal.addressNum }} 个聪明钱地址在 {{ formatDuration(currentSignal.duration) }} 内{{ currentSignal.trendText }} ${{ currentSignal.volume }}
          </p>
        </div>
        
        <div class="address-list">
          <div class="list-header">
            <span>钱包地址列表</span>
            <el-button type="text" @click="copyAllAddresses">
              <el-icon><DocumentCopy /></el-icon>
              复制全部
            </el-button>
          </div>
          
          <div class="address-items">
            <div v-for="(address, index) in walletAddresses" :key="index" class="address-item">
              <div class="address-index">{{ index + 1 }}</div>
              <div class="address-text" :title="address">{{ address }}</div>
              <el-button type="text" size="small" @click="copyToClipboard(address)">
                <el-icon><DocumentCopy /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="OkxSignals">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy } from '@element-plus/icons-vue'
import { getOkxSignalList, getOkxSignalStatistics } from '@/api/crypto/okxSignal'

// 响应式数据
const loading = ref(false)
const loadingMore = ref(false)
const signalList = ref([])
const statistics = ref(null)
const currentFilter = ref('')
const currentPage = ref(1)
const hasMore = ref(true)
let refreshTimer = null

// 地址弹窗相关
const addressDialogVisible = ref(false)
const currentSignal = ref(null)
const walletAddresses = ref([])

// 获取信号列表
const fetchSignalList = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
    currentPage.value = 1
  }

  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: 20
    }
    
    if (currentFilter.value) {
      params.trend = currentFilter.value
    }

    const res = await getOkxSignalList(params)
    
    if (res.code === 200 && res.data) {
      if (isLoadMore) {
        signalList.value.push(...res.data.list)
      } else {
        signalList.value = res.data.list
      }
      
      hasMore.value = res.data.list.length >= 20
      currentPage.value++
    } else {
      ElMessage.error('获取信号数据失败')
    }
  } catch (error) {
    console.error('获取信号列表失败:', error)
    ElMessage.error('获取信号数据异常')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const res = await getOkxSignalStatistics()
    if (res.code === 200 && res.data) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 筛选变化
const onFilterChange = () => {
  fetchSignalList()
}

// 加载更多
const loadMore = () => {
  fetchSignalList(true)
}

// 显示地址详情
const showAddressDetails = (signal) => {
  currentSignal.value = signal
  // 解析地址字符串
  if (signal.addresses) {
    walletAddresses.value = signal.addresses.split(',').filter(addr => addr.trim())
  } else {
    walletAddresses.value = []
  }
  addressDialogVisible.value = true
}

// 关闭地址弹窗
const closeAddressDialog = () => {
  addressDialogVisible.value = false
  currentSignal.value = null
  walletAddresses.value = []
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    // 兼容旧浏览器
    const textArea = document.createElement('textarea')
    textArea.value = text
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    ElMessage.success('已复制到剪贴板')
  })
}

// 复制全部地址
const copyAllAddresses = () => {
  const allAddresses = walletAddresses.value.join('\n')
  copyToClipboard(allAddresses)
}

// 格式化地址显示
const formatAddress = (address) => {
  if (!address) return ''
  if (address.length <= 16) return address
  return `${address.slice(0, 8)}...${address.slice(-8)}`
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = Math.floor((now - date) / 1000 / 60) // 分钟差

  if (diff < 60) {
    return `${diff} 分钟前`
  } else if (diff < 1440) {
    return `${Math.floor(diff / 60)} 小时前`
  } else {
    return `${Math.floor(diff / 1440)} 天前`
  }
}

// 格式化持续时间
const formatDuration = (duration) => {
  const hours = Math.floor(duration / (1000 * 60 * 60))
  const minutes = Math.floor((duration % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 0) {
    return `${hours} 小时`
  } else {
    return `${minutes} 分钟`
  }
}

// 处理图片加载错误
const handleImageError = (e) => {
  e.target.src = 'https://static.oklink.com/cdn/web3/currency/token/default.png'
}

// 启动自动刷新
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    fetchSignalList()
    fetchStatistics()
  }, 30000) // 30秒刷新一次
}

// 组件挂载
onMounted(() => {
  fetchSignalList()
  fetchStatistics()
  startAutoRefresh()
})

// 组件卸载
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped lang="scss">
.okx-signals-page {
  min-height: 100vh;
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);

  .filter-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: var(--el-bg-color-page);
    border-bottom: 1px solid var(--el-border-color-light);

    .filter-left {
      .chain-selector {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--el-text-color-primary);

        .chain-icon {
          width: 24px;
          height: 24px;
          border-radius: 50%;
        }
      }
    }

    .filter-center {
      .trend-filters {
        :deep(.el-radio-button) {
          .el-radio-button__inner {
            background: transparent;
            border: 1px solid var(--el-border-color);
            color: var(--el-text-color-regular);
            padding: 8px 16px;

            &:hover {
              border-color: var(--el-color-primary);
              color: var(--el-color-primary);
            }
          }

          &.is-active .el-radio-button__inner {
            background: var(--el-color-primary);
            border-color: var(--el-color-primary);
            color: #ffffff;
          }
        }
      }
    }

    .filter-right {
      display: flex;
      gap: 12px;

      .filter-btn {
        color: var(--el-text-color-regular);
        padding: 8px 12px;
        
        &:hover {
          color: var(--el-color-primary);
        }
      }
    }
  }

  .statistics-bar {
    display: flex;
    gap: 24px;
    padding: 12px 20px;
    background: var(--el-bg-color-page);
    border-bottom: 1px solid var(--el-border-color-light);

    .stat-item {
      display: flex;
      gap: 4px;
      font-size: 14px;

      .stat-label {
        color: var(--el-text-color-regular);
      }

      .stat-value {
        color: var(--el-text-color-primary);
        font-weight: 500;

        &.buy {
          color: var(--el-color-success);
        }

        &.sell {
          color: var(--el-color-danger);
        }
      }
    }
  }

  .signals-container {
    padding: 0 20px;

    .signal-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 0;
      border-bottom: 1px solid var(--el-border-color-lighter);
      transition: background-color 0.2s ease;

      &:hover {
        background: var(--el-bg-color-page);
      }

      .signal-left {
        display: flex;
        align-items: center;
        gap: 16px;
        flex: 1;

        .operation-badge {
          padding: 4px 8px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: 500;
          min-width: 40px;
          text-align: center;

          &.buy {
            background: var(--el-color-success-light-9);
            color: var(--el-color-success);
          }

          &.sell {
            background: var(--el-color-danger-light-9);
            color: var(--el-color-danger);
          }
        }

        .signal-time {
          color: var(--el-text-color-secondary);
          font-size: 12px;
          min-width: 80px;
        }

        .signal-main {
          display: flex;
          align-items: center;
          gap: 12px;
          flex: 1;

          .token-info {
            position: relative;

            .token-logo {
              width: 32px;
              height: 32px;
              border-radius: 50%;
              background: var(--el-fill-color-light);
            }

            .chain-logo {
              position: absolute;
              bottom: -4px;
              right: -4px;
              width: 16px;
              height: 16px;
              border-radius: 50%;
              border: 2px solid var(--el-bg-color);
            }
          }

          .signal-details {
            .address-info {
              display: flex;
              flex-direction: column;
              gap: 4px;

              .address-count {
                color: var(--el-color-primary);
                font-size: 14px;
                font-weight: 500;
                
                &.clickable {
                  cursor: pointer;
                  text-decoration: underline;
                  
                  &:hover {
                    color: var(--el-color-primary-light-3);
                  }
                }
              }

              .time-info {
                color: var(--el-text-color-primary);
                font-size: 14px;
              }

              .amount {
                color: var(--el-color-success);
                font-size: 13px;
                font-weight: 500;
              }

              .ca-address-row {
                display: flex;
                align-items: center;
                gap: 6px;
                margin-top: 2px;

                .ca-label {
                  color: var(--el-text-color-secondary);
                  font-size: 12px;
                }

                .ca-address {
                  color: var(--el-text-color-regular);
                  font-size: 12px;
                  font-family: 'Courier New', monospace;
                  cursor: pointer;
                  display: flex;
                  align-items: center;
                  gap: 4px;
                  padding: 2px 6px;
                  border-radius: 4px;
                  background: var(--el-fill-color-light);
                  transition: all 0.2s ease;

                  &:hover {
                    background: var(--el-fill-color);
                    color: var(--el-color-primary);
                  }

                  .copy-icon {
                    font-size: 12px;
                  }
                }
              }
            }
          }
        }
      }

      .signal-right {
        display: flex;
        align-items: center;

        .price-info {
          display: flex;
          gap: 40px;

          .price-item {
            display: flex;
            flex-direction: column;
            align-items: flex-end;
            min-width: 80px;

            .price-label {
              color: var(--el-text-color-secondary);
              font-size: 12px;
              margin-bottom: 4px;
            }

            .price-value {
              color: var(--el-text-color-primary);
              font-size: 14px;
              font-weight: 500;
            }
          }
        }
      }
    }

    .empty-state {
      padding: 60px 0;
      text-align: center;
    }
  }

  .load-more {
    text-align: center;
    padding: 24px 0;

    .el-button {
      background: transparent;
      border: 1px solid var(--el-border-color);
      color: var(--el-text-color-regular);

      &:hover {
        border-color: var(--el-color-primary);
        color: var(--el-color-primary);
      }
    }
  }
}

// 地址弹窗样式
.address-dialog-content {
  .dialog-header {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    h4 {
      margin: 0 0 8px 0;
      color: var(--el-text-color-primary);
      font-size: 16px;
    }

    .signal-summary {
      margin: 0;
      color: var(--el-text-color-regular);
      font-size: 14px;
    }
  }

  .address-list {
    .list-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      
      span {
        font-weight: 500;
        color: var(--el-text-color-primary);
      }
    }

    .address-items {
      max-height: 300px;
      overflow-y: auto;

      .address-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px 0;
        border-bottom: 1px solid var(--el-border-color-lighter);

        &:last-child {
          border-bottom: none;
        }

        .address-index {
          min-width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: var(--el-color-primary-light-9);
          color: var(--el-color-primary);
          border-radius: 50%;
          font-size: 12px;
          font-weight: 500;
        }

        .address-text {
          flex: 1;
          font-family: 'Courier New', monospace;
          font-size: 12px;
          color: var(--el-text-color-regular);
          word-break: break-all;
          line-height: 1.4;
        }
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .okx-signals-page {
    .signals-container .signal-item {
      .signal-right .price-info {
        gap: 20px;

        .price-item {
          min-width: 60px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .okx-signals-page {
    .filter-bar {
      flex-direction: column;
      gap: 12px;
      padding: 12px 16px;
    }

    .statistics-bar {
      flex-wrap: wrap;
      gap: 12px;
      padding: 12px 16px;
    }

    .signals-container {
      padding: 0 16px;

      .signal-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;

        .signal-left {
          width: 100%;
        }

        .signal-right {
          width: 100%;
          justify-content: flex-start;

          .price-info {
            gap: 12px;
          }
        }
      }
    }
  }

  .address-dialog-content {
    .address-items {
      .address-item {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }
    }
  }
}
</style> 