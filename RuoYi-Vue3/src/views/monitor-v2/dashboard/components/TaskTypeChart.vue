<!-- 任务类型分布图（柱状图） -->
<template>
  <div class="task-type-wrapper">
    <div v-if="hasData" ref="chartRef" class="task-type-chart"></div>
    <el-empty 
      v-else 
      description="暂无任务数据"
      :image-size="100"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, computed, nextTick } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({
      smart: 0,
      batch: 0,
      block: 0,
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
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['智能监控', '批量监控', '区块监控'],
      axisLine: {
        lineStyle: {
          color: 'inherit'
        }
      },
      axisLabel: {
        color: 'inherit'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: 'inherit'
        }
      },
      axisLabel: {
        color: 'inherit'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(128, 128, 128, 0.2)'
        }
      }
    },
    series: [
      {
        name: '任务数量',
        type: 'bar',
        data: [
          props.data.smart || 0,
          props.data.batch || 0,
          props.data.block || 0
        ],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409EFF' },
            { offset: 1, color: '#67C23A' }
          ]),
          borderRadius: [10, 10, 0, 0]
        },
        label: {
          show: true,
          position: 'top',
          color: 'inherit'
        }
      }
    ]
  }
  
  chartInstance.setOption(option)
}

const resizeChart = () => {
  chartInstance?.resize()
}

watch(() => props.data, async () => {
  if (hasData.value) {
    await nextTick()
    initChart()
  }
}, { deep: true })

watch(hasData, async (newVal) => {
  if (newVal) {
    await nextTick()
    initChart()
  }
})

onMounted(async () => {
  if (hasData.value) {
    await nextTick()
    initChart()
  }
  window.addEventListener('resize', resizeChart)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeChart)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style scoped>
.task-type-wrapper {
  width: 100%;
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.task-type-chart {
  width: 100%;
  height: 300px;
}
</style>

