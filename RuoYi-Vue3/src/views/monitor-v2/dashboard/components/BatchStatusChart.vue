<!-- 批次运行状态分布图（饼图） -->
<template>
  <div class="batch-status-wrapper">
    <div v-if="hasData" ref="chartRef" class="batch-status-chart"></div>
    <el-empty 
      v-else 
      description="暂无批次数据"
      :image-size="100"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({
      running: 0,
      paused: 0,
      heartbeatTimeout: 0,
      completed: 0,
      total: 0
    })
  }
})

const chartRef = ref(null)
let chartInstance = null

// 判断是否有数据
const hasData = computed(() => {
  return props.data && props.data.total > 0
})

const initChart = () => {
  if (!chartRef.value) return
  
  // 如果已存在实例，先销毁
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  
  chartInstance = echarts.init(chartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: {
        color: 'inherit'
      }
    },
    series: [
      {
        name: '批次状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          {
            value: props.data.running || 0,
            name: '运行中',
            itemStyle: { color: '#67C23A' }
          },
          {
            value: props.data.paused || 0,
            name: '已暂停',
            itemStyle: { color: '#E6A23C' }
          },
          {
            value: props.data.heartbeatTimeout || 0,
            name: '心跳超时',
            itemStyle: { color: '#F56C6C' }
          },
          {
            value: props.data.completed || 0,
            name: '已完成',
            itemStyle: { color: '#909399' }
          }
        ]
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const resizeChart = () => {
  chartInstance?.resize()
}

watch(() => props.data, () => {
  if (hasData.value && chartInstance) {
    initChart()
  }
}, { deep: true })

onMounted(() => {
  if (hasData.value) {
    initChart()
  }
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  chartInstance?.dispose()
})
</script>

<style scoped>
.batch-status-wrapper {
  width: 100%;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.batch-status-chart {
  width: 100%;
  height: 300px;
}
</style>

