<template>
  <el-card class="market-chart">
    <template #header>
      <div class="card-header">
        <div class="left">
          <span>市场行情</span>
          <el-radio-group v-model="currentChart" size="small" class="ml20" @change="handleChartChange">
            <el-radio-button label="BTC">BTC</el-radio-button>
            <el-radio-button label="ETH">ETH</el-radio-button>
            <el-radio-button label="BNB">BNB</el-radio-button>
            <el-radio-button label="SOL">SOL</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </template>
    <div class="chart-container">
      <TradingViewWidget 
        :symbol="currentSymbol" 
        :interval="timeframe"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed } from 'vue'
import TradingViewWidget from '@/components/TradingViewWidget.vue'
import useSettingsStore from '@/store/modules/settings'

const settingsStore = useSettingsStore()

const currentChart = ref('BTC')
const timeframe = ref('60')

// 计算当前交易对
const currentSymbol = computed(() => {
  const symbols = {
    'BTC': 'BINANCE:BTCUSDT',
    'ETH': 'BINANCE:ETHUSDT',
    'BNB': 'BINANCE:BNBUSDT',
    'SOL': 'BINANCE:SOLUSDT'
  }
  return symbols[currentChart.value]
})

// 处理图表切换
const handleChartChange = (value) => {
  currentChart.value = value
}

</script>

<style scoped lang="scss">
/* Light theme styles */
.market-chart {
  border: 1px solid #e4e7ed;
  background-color: #ffffff;
  transition: all 0.3s ease-in-out;
  height: 655px;

  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background-color: #f5f7fa;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
      gap: 1rem;

      span {
        color: #303133;
        font-weight: 600;
      }
    }
  }

  .chart-container {
    height: 600px;
    background-color: #ffffff;
    transition: all 0.3s ease-in-out;
  }
}

.ml20 {
  margin-left: 20px;
}

:deep(.el-radio-button__inner) {
  background-color: #ffffff;
  color: #303133;
  border-color: #e4e7ed;

  &:hover {
    color: #409EFF;
  }
}

:deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background-color: #409EFF;
  color: #ffffff;
  border-color: #409EFF;
}

/* Dark theme styles - with !important to override */
:global(html.dark) {
  .market-chart {
    border: 1px solid #434343 !important;
    background-color: #1d1e1f !important;

    :deep(.el-card__header) {
      border-bottom: 1px solid #434343 !important;
      background-color: #141414 !important;
    }

    .card-header {
      .left {
        span {
          color: #ffffff !important;
        }
      }
    }

    .chart-container {
      background-color: #1d1e1f !important;
    }
  }

  :deep(.el-radio-button__inner) {
    background-color: #1d1e1f !important;
    color: #d0d0d0 !important;
    border-color: #434343 !important;

    &:hover {
      color: #409EFF !important;
    }
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    background-color: #409EFF !important;
    color: #ffffff !important;
    border-color: #409EFF !important;
  }
}
</style> 