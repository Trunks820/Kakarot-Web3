<template>
  <div class="app-container home">
    <!-- 顶部数据卡片 -->
    <DataSummary />

    <!-- 主要内容区域 -->
    <el-row :gutter="12" class="mt20">
      <!-- 左侧K线图面板 -->
      <el-col :span="17">
        <MarketChart />
      </el-col>
      <!-- 右侧热门CA面板 -->
      <el-col :span="7">
        <PopularCA />
      </el-col>
    </el-row>

  </div>
</template>

<script setup name="Index">
import { ref, onMounted, nextTick, computed } from 'vue'
import * as echarts from 'echarts'
import TradingViewWidget from '@/components/TradingViewWidget.vue'
import { ElMessage } from 'element-plus'
import DataSummary from '@/views/components/DataSummary.vue'
import { Search, User, Warning } from '@element-plus/icons-vue'
import PopularCA from "./components/PopularCA.vue";
import MarketChart from "./components/MarketChart.vue";

// K线图配置
const chartRef = ref(null)
const currentChart = ref('BTC')
const timeframe = ref('60')
const theme = ref('dark')
let chart = null


// 初始化K线图
function initChart() {
  if (chart) {
    chart.dispose()
  }
  
  nextTick(() => {
    chart = echarts.init(chartRef.value)
    // 这里添加模拟的K线数据
    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        }
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '15%'
      },
      xAxis: {
        type: 'category',
        data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: [820, 932, 901, 934, 1290, 1330, 1320],
        type: 'line'
      }]
    }
    chart.setOption(option)
  })
}

// 监听图表类型和时间周期变化
watch([currentChart, timeframe], () => {
  // 这里添加获取新数据和更新图表的逻辑
  initChart()
})

// 监听窗口大小变化
window.addEventListener('resize', () => {
  chart && chart.resize()
})

onMounted(() => {
  initChart()
})

onUnmounted(() => {
  chart && chart.dispose()
  window.removeEventListener('resize', () => {
    chart && chart.resize()
  })
})
</script>

<style scoped lang="scss">
.home {
  .mt20 {
    margin-top: 20px;
  }
  
  .mb20 {
    margin-bottom: 20px;
  }

  .ml20 {
    margin-left: 20px;
  }

  .data-card {
    height: 160px;
    background: #1a1a1a;
    border: 1px solid #2a2a2a;
    
    :deep(.el-card__header) {
      padding: 0;
      border: none;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;

      span {
        font-size: 14px;
        color: #8c8c8c;
        font-weight: 600;
      }

      .el-tag {
        padding: 0 8px;
        height: 20px;
        line-height: 20px;
        font-size: 11px;
      }
    }

    .card-body {
      padding: 0 16px 12px;

      .stat-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .stat-label {
          display: flex;
          align-items: center;
          
          .mr8 {
            margin-right: 8px;
          }
        }

        .label {
          color: #8c8c8c;
          font-size: 13px;
        }

        h3 {
          margin: 0;
          font-size: 22px;
          font-weight: 500;
          color: #e6e6e6;
          font-family: 'Roboto', sans-serif;
        }

        .el-tag {
          padding: 0 8px;
          height: 22px;
          line-height: 22px;
          font-size: 12px;
          border-radius: 3px;
          
          &.el-tag--success {
            background: rgba(103, 194, 58, 0.1);
            color: #95eb6c;
          }
          
          &.el-tag--danger {
            background: rgba(245, 108, 108, 0.1);
            color: #ff7875;
          }
        }
      }
    }

    .warning {
      color: #faad14;
    }
  }

  .el-tag--warning {
    background: rgba(250, 173, 20, 0.1);
    color: #ffc53d;
    border: none;
  }

  .el-tag--success {
    background: rgba(103, 194, 58, 0.1);
    color: #95eb6c;
    border: none;
  }

  .el-tag--danger {
    background: rgba(245, 108, 108, 0.1);
    color: #ff7875;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .left {
      display: flex;
      align-items: center;
    }
  }

  .chart-container {
    height: 572px;
    width: 100%;
  }

  .ca-card {
    height: calc(50% - 6px);  // 减去间距的一半

    .card-body {
      padding: 6px;
      height: calc(100% - 50px);  // 减去header的高度
      overflow-y: auto;

      .ca-item {
        background: #1e1e1e;
        border-radius: 4px;
        padding: 6px;
        margin-bottom: 6px;

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
              background: #409EFF;
              color: white;
              border-radius: 3px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-weight: bold;
              font-size: 11px;
              flex-shrink: 0;
            }

            .token-name {
              font-size: 12px;
              font-weight: bold;
              color: #fff;
              margin-right: 4px;
              flex-shrink: 0;
            }
          }

          .ca-count {
            color: #67C23A;
            font-weight: bold;
            white-space: nowrap;
            font-size: 11px;
            padding-left: 4px;
            flex-shrink: 0;
          }
        }

        .ca-address {
          font-family: 'Courier New', monospace;
          background: #2a2a2a;
          padding: 3px 5px;
          border-radius: 3px;
          color: #909399;
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

          &:hover {
            background: #3a3a3a;
          }

          .copy-icon {
            color: #409EFF;
            margin-left: 3px;
            flex-shrink: 0;
            font-size: 11px;
          }
        }
      }
    }
  }

  .mb12 {
    margin-bottom: 12px;
  }
}
</style>

