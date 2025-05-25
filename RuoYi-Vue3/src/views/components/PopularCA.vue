<template>
  <div>
    <el-card class="ca-card mb20" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>热门CA</span>
          <div class="header-controls">
            <el-radio-group v-model="currentSource" size="small" class="source-toggle">
              <el-radio-button label="tg">TG播报</el-radio-button>
              <el-radio-button label="wx">微信查询</el-radio-button>
            </el-radio-group>
            <el-button
              type="text"
              @click="refreshData"
              :loading="loading"
              class="refresh-btn"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <div class="card-body">
        <div v-for="(ca, index) in currentCAs" :key="ca.address" 
          class="ca-item"
          :class="{ 'wx-item': currentSource === 'wx' }"
        >
          <div class="ca-header">
            <div class="left">
              <span class="ca-rank" :class="{ 'wx-rank': currentSource === 'wx' }">{{ index + 1 }}</span>
              <span class="token-name">{{ ca.symbol }}</span>
            </div>
            <span class="ca-count" :class="{ 'wx-count': currentSource === 'wx' }">{{ ca.queryCount }}次</span>
          </div>
          <div class="ca-address" @click="copyToClipboard(ca.address)">
            {{ ca.address }}
            <el-icon class="copy-icon"><DocumentCopy /></el-icon>
          </div>
        </div>
      </div>
    </el-card>

    <!-- CA涨幅榜卡片 -->
    <el-card class="ca-card" v-loading="priceLoading">
      <template #header>
        <div class="card-header">
          <span>CA涨幅榜</span>
          <div class="header-controls">
            <el-button
              type="text"
              @click="fetchPriceRankingData"
              :loading="priceLoading"
              class="refresh-btn"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
      <div class="card-body">
        <div v-for="(item, index) in priceRankings" :key="item.address" class="ca-item">
          <div class="ca-header">
            <div class="left">
              <span class="ca-rank">{{ index + 1 }}</span>
              <span class="token-name">{{ item.symbol }}</span>
            </div>
            <div class="price-info">
              <span :class="getPriceChangeClass(item.change)">{{ item.change }}%</span>
              <span class="volume">${{ item.volume }}</span>
            </div>
          </div>
          <div class="ca-address" @click="copyToClipboard(item.address)">
            {{ item.address }}
            <el-icon class="copy-icon"><DocumentCopy /></el-icon>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotCaByWechat, getHotCaByTelegram } from '@/api/crypto/index'
import { Refresh, DocumentCopy } from '@element-plus/icons-vue'

const loading = ref(false)
const currentSource = ref('tg')

// 热门CA数据
const tgPopularCAs = ref([])
const wxPopularCAs = ref([])

// 当前显示的数据
const currentCAs = computed(() => {
  return currentSource.value === 'tg' ? tgPopularCAs.value : wxPopularCAs.value
})

// 涨幅榜数据和加载状态
const priceLoading = ref(false)
const priceRankings = ref([
  { 
    symbol: 'PEPE', 
    address: '0x123...456',
    change: 125.5,
    volume: '256.8K'
  },
  { 
    symbol: 'DOGE',
    address: '0x789...012',
    change: 85.3,
    volume: '128.4K'
  },
  { 
    symbol: 'SHIB',
    address: '0x345...678',
    change: -12.6,
    volume: '64.2K'
  }
])

// 刷新数据
const refreshData = () => {
  if (currentSource.value === 'tg') {
    fetchHotCaByTelegramData()
  } else {
    fetchHotCaByWechatData()
  }
}

// 添加热门Ca微信查询数据
const fetchHotCaByWechatData = async () => {
  loading.value = true
  try {
    const res = await new Promise((resolve) => {
      setTimeout(async () => {
        try {
          const apiRes = await getHotCaByWechat()
          resolve(apiRes)
        } catch (err) {
          resolve({ code: 500, msg: err.message })
        }
      }, 600)
    })

    if (res.code === 200 && res.data) {
      wxPopularCAs.value = res.data
    } else {
      ElMessage.warning('获取微信数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取微信数据异常，请检查网络连接或后端服务')
  } finally {
    loading.value = false
  }
}

const fetchHotCaByTelegramData = async () => {
  loading.value = true
  try {
    const res = await new Promise((resolve) => {
      setTimeout(async () => {
        try {
          const apiRes = await getHotCaByTelegram() // 这里需要改成TG的API
          resolve(apiRes)
        } catch (err) {
          resolve({ code: 500, msg: err.message })
        }
      }, 600)
    })

    if (res.code === 200 && res.data) {
      tgPopularCAs.value = res.data
    } else {
      ElMessage.warning('获取TG数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取TG数据异常，请检查网络连接或后端服务')
  } finally {
    loading.value = false
  }
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage({
      message: '地址已复制到剪贴板',
      type: 'success'
    })
  }).catch(() => {
    ElMessage({
      message: '复制失败，请手动复制',
      type: 'error'
    })
  })
}

// 获取涨幅样式
const getPriceChangeClass = (change) => {
  return {
    'price-up': change > 0,
    'price-down': change < 0,
    'price-unchanged': change === 0
  }
}

// 获取涨幅榜数据
const fetchPriceRankingData = async () => {
  priceLoading.value = true
  try {
    // 这里添加实际的API调用
    await new Promise(resolve => setTimeout(resolve, 600))
    // 模拟数据更新
    priceRankings.value = [
      { 
        symbol: 'PEPE', 
        address: '0x123...456',
        change: Math.random() * 200 - 50,
        volume: '256.8K'
      },
      { 
        symbol: 'DOGE',
        address: '0x789...012',
        change: Math.random() * 150 - 30,
        volume: '128.4K'
      },
      { 
        symbol: 'SHIB',
        address: '0x345...678',
        change: Math.random() * 100 - 20,
        volume: '64.2K'
      },
      { 
        symbol: 'SHIB',
        address: '0x345...678',
        change: Math.random() * 100 - 20,
        volume: '64.2K'
      }
    ]
  } catch (error) {
    ElMessage.error('获取涨幅数据失败')
  } finally {
    priceLoading.value = false
  }
}

// 在组件挂载时获取涨幅数据
onMounted(() => {
  fetchPriceRankingData()
})
</script>

<style scoped lang="scss">
.ca-card {
  height: 321px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    height: 16px;
  }

  .header-controls {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .source-toggle {
    .el-radio-button__inner {
      padding: 4px 12px;
    }
  }

  .card-body {
    padding: 6px;
    height: calc(100% - 50px);
    overflow-y: auto;

    .ca-item {
      border-radius: 4px;
      padding: 6px;
      margin-bottom: 6px;
      transition: all 0.3s ease-in-out;
      background-color: #f5f7fa;

      &:last-child {
        margin-bottom: 0;
      }

     
      .ca-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .left {
          display: flex;
          align-items: center;
          gap: 4px;
          flex: 1;
          min-width: 0;

          .ca-rank {
            min-width: 18px;
            height: 18px;
            background: var(--el-color-primary);
            color: white;
            border-radius: 3px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 11px;
            flex-shrink: 0;

            &.wx-rank {
              background-color: #07C160 !important;
            }
          }

          .token-name {
            font-size: 12px;
            font-weight: bold;
            color: #303133;
            margin-right: 4px;
            flex-shrink: 0;
          }
        }

        .ca-count {
          color: var(--el-color-primary);
          font-weight: bold;
          white-space: nowrap;
          font-size: 11px;
          padding-left: 4px;
          flex-shrink: 0;

        
        }
      }

      .ca-address {
        font-family: 'Courier New', monospace;
        background-color: #ffffff;
        padding: 3px 5px;
        border-radius: 3px;
        color: #606266;
        cursor: pointer;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 11px;
        letter-spacing: -0.3px;
        line-height: 1.1;
        width: 100%;
        box-sizing: border-box;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        transition: all 0.3s ease-in-out;

        &:hover {
          background-color: #e9ecf2;
        }

        .copy-icon {
          color: var(--el-color-primary);
          margin-left: 3px;
          flex-shrink: 0;
          font-size: 11px;
        }
      }
    }
  }
}

/* Dark theme styles */
:global(html.dark) {
  .ca-card .card-body .ca-item {
    background-color: #1d1e1f !important;
    
    .token-name {
      color: #ffffff !important;
    }
    
    .ca-address {
      background-color: #141414 !important;
      color: #d0d0d0 !important;
      
      &:hover {
        background-color: #2d2d2d !important;
      }
    }

    &.wx-item {
      border-left: 2px solid #07C160;
    }
  }
}

.mb20 {
  margin-bottom: 13px !important;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .volume {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }
}

.price-up {
  color: #67C23A;
  font-weight: 600;
}

.price-down {
  color: #F56C6C;
  font-weight: 600;
}

.price-unchanged {
  color: var(--el-text-color-regular);
  font-weight: 600;
}

/* Dark theme styles */
:global(html.dark) {
  .price-info {
    .volume {
      color: #909399;
    }
  }
}
</style> 