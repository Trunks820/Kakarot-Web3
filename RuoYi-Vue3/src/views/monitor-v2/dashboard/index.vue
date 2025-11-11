<!-- 监控系统 V2.0 - 监控总览（Dashboard） -->
<template>
  <div class="monitor-dashboard">
    <!-- 顶部标题 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h1>监控总览</h1>
        <el-tag type="info" effect="plain" class="version-tag">V2.0</el-tag>
      </div>
      <div class="header-right">
        <el-button icon="Refresh" :loading="loading" @click="loadAllData">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 核心统计数据 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <el-statistic title="总配置数" :value="configStats.total">
            <template #prefix>
              <el-icon color="#409EFF"><Setting /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <el-statistic title="运行任务数" :value="taskStats.running">
            <template #prefix>
              <el-icon color="#67C23A"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <el-statistic title="运行批次数" :value="batchStats.running">
            <template #prefix>
              <el-icon color="#E6A23C"><Lightning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 配置-任务-批次关系图 -->
    <el-row :gutter="20" class="mt20">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Connection /></el-icon>
                配置应用链路
              </span>
              <el-button text icon="Refresh" @click="loadRelationData">刷新</el-button>
            </div>
          </template>
          <relation-table :data="relationData" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 统计图表区 -->
    <el-row :gutter="20" class="mt20">
      <el-col :xs="24" :sm="12" :md="12" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><PieChart /></el-icon>
                批次状态分布
              </span>
            </div>
          </template>
          <batch-status-chart :data="batchStats" />
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="12" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Histogram /></el-icon>
                任务类型分布
              </span>
            </div>
          </template>
          <task-type-chart :data="taskTypeData" />
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">
                <el-icon><Bell /></el-icon>
                最近告警 (Top 10)
              </span>
              <el-button 
                text 
                type="primary" 
                size="small"
                @click="handleViewAllAlerts"
              >
                查看全部
              </el-button>
            </div>
          </template>
          <recent-alerts 
            :alerts="recentAlerts" 
            @alert-click="handleAlertClick"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Connection, 
  PieChart, 
  Histogram, 
  Bell,
  Setting,
  CircleCheck,
  Lightning
} from '@element-plus/icons-vue'
import RelationTable from './components/RelationTable.vue'
import BatchStatusChart from './components/BatchStatusChart.vue'
import TaskTypeChart from './components/TaskTypeChart.vue'
import RecentAlerts from './components/RecentAlerts.vue'
import { getConfigStats, getTaskStats, getBatchStats, getDashboardRelation } from '@/api/crypto/monitor-v2'
import { monitorWS } from '@/utils/monitor-websocket'

const router = useRouter()
const loading = ref(false)

// 核心指标数据
const configStats = ref({
  total: 0,
  enabled: 0,
  system: 0,
  custom: 0,
  lastUpdate: null
})

const taskStats = ref({
  total: 0,
  running: 0,
  stopped: 0,
  smart: 0,
  batch: 0,
  block: 0,
  lastUpdate: null
})

const batchStats = ref({
  total: 0,
  running: 0,
  paused: 0,
  heartbeatNormal: 0,
  heartbeatTimeout: 0,
  completed: 0,
  lastUpdate: null
})

// 关系图数据
const relationData = ref({
  nodes: [],
  links: []
})

// 任务类型数据（用于柱状图）
const taskTypeData = ref({
  smart: 0,
  batch: 0,
  block: 0
})

// 最近告警数据
const recentAlerts = ref([])

// 加载配置统计
const loadConfigData = async () => {
  try {
    const response = await getConfigStats()
    if (response.code === 200) {
      configStats.value = {
        ...response.data,
        lastUpdate: new Date().toISOString()
      }
    }
  } catch (error) {
    console.error('加载配置统计失败:', error)
  }
}

// 加载任务统计
const loadTaskData = async () => {
  try {
    const response = await getTaskStats()
    if (response.code === 200) {
      taskStats.value = {
        ...response.data,
        lastUpdate: new Date().toISOString()
      }
      // 更新任务类型数据
      taskTypeData.value = {
        smart: response.data.smart || 0,
        batch: response.data.batch || 0,
        block: response.data.block || 0,
        total: response.data.total || 0
      }
    }
  } catch (error) {
    console.error('加载任务统计失败:', error)
  }
}

// 加载批次统计
const loadBatchData = async () => {
  try {
    const response = await getBatchStats()
    if (response.code === 200) {
      batchStats.value = {
        ...response.data,
        lastUpdate: new Date().toISOString()
      }
    }
  } catch (error) {
    console.error('加载批次统计失败:', error)
  }
}

// 加载关系图数据
const loadRelationData = async () => {
  try {
    const response = await getDashboardRelation()
    if (response.code === 200 && response.data) {
      relationData.value = response.data
    } else {
      // 如果没有数据，使用空状态
      relationData.value = { nodes: [], links: [] }
    }
  } catch (error) {
    console.error('加载关系图数据失败:', error)
    // 出错时使用空数据
    relationData.value = { nodes: [], links: [] }
  }
}

// 加载最近告警
const loadRecentAlerts = async () => {
  // TODO: 实现告警数据加载
  // 这里先用模拟数据
  recentAlerts.value = [
    {
      id: 1,
      tokenSymbol: 'SOL-ABC',
      message: '价格上涨 25%，交易量激增',
      level: 'high',
      levelText: '高级别',
      alertTime: new Date(Date.now() - 5 * 60000).toISOString()
    },
    {
      id: 2,
      tokenSymbol: 'SOL-XYZ',
      message: '批次心跳超时',
      level: 'medium',
      levelText: '中级别',
      alertTime: new Date(Date.now() - 10 * 60000).toISOString()
    },
    {
      id: 3,
      tokenSymbol: 'SOL-DEF',
      message: '持仓集中度超过 70%',
      level: 'medium',
      levelText: '中级别',
      alertTime: new Date(Date.now() - 30 * 60000).toISOString()
    }
  ]
}

// 加载所有数据（优化：分批加载）
const loadAllData = async () => {
  loading.value = true
  try {
    // 第一批：快速加载统计数据
    await Promise.all([
      loadConfigData(),
      loadTaskData(),
      loadBatchData()
    ])
    
    // 第二批：延迟加载复杂数据（避免阻塞页面渲染）
    setTimeout(() => {
      loadRelationData()
      loadRecentAlerts()
    }, 100)
  } finally {
    loading.value = false
  }
}

// 查看所有告警
const handleViewAllAlerts = () => {
  router.push('/monitor-v2/alert')
}

// 点击告警
const handleAlertClick = (alert) => {
  console.log('查看告警详情:', alert)
  // TODO: 打开告警详情弹窗或跳转详情页
}

// 定时刷新
let refreshTimer = null

onMounted(() => {
  loadAllData()
  
  // 每60秒刷新一次
  refreshTimer = setInterval(() => {
    loadAllData()
  }, 60000)

  // 连接WebSocket实时推送
  monitorWS
    .onBatchStatus((data) => {
      console.log('收到批次状态更新:', data)
      // 刷新批次统计
      loadBatchData()
    })
    .onTaskStatus((data) => {
      console.log('收到任务状态更新:', data)
      // 刷新任务统计
      loadTaskData()
    })
    .onAlert((data) => {
      console.log('收到告警通知:', data)
      // 刷新最近告警
      loadRecentAlerts()
    })
    .onConnected(() => {
      console.log('✅ Monitor WebSocket 已连接')
    })
    .connect()
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
  
  // 关闭WebSocket连接
  monitorWS.close()
})
</script>

<style scoped lang="scss">
.monitor-dashboard {
  padding: 20px;
  
  .mt20 {
    margin-top: 20px;
  }
  
  // Dashboard 头部
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: var(--el-bg-color);
    border-radius: 8px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    margin-bottom: 20px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h1 {
      font-size: 24px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      margin: 0;
    }
  }

  .version-tag {
    font-weight: 600;
  }

  .header-right {
    display: flex;
    gap: 12px;
    align-items: center;
  }
  
  // 统计行
  .stats-row {
    .el-col {
      margin-bottom: 20px;
    }
  }
  
  // 卡片头部
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .card-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: 600;
      color: var(--el-text-color-primary);
      
      .el-icon {
        font-size: 20px;
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .monitor-dashboard {
    .el-row .el-col {
      margin-bottom: 16px;
    }
  }
}

@media (max-width: 768px) {
  .monitor-dashboard {
    padding: 10px;
    
    .dashboard-header {
      flex-direction: column;
      gap: 16px;
      padding: 16px;

      .header-left {
        width: 100%;
      }

      .header-right {
        width: 100%;
        justify-content: center;
      }
    }
    
    .card-row {
      .el-col {
        margin-bottom: 16px;
      }
    }
  }
}
</style>

