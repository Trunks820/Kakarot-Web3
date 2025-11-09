<template>
  <div class="app-container home">
    <!-- 监控系统 V2.0 头部 -->
    <div class="dashboard-header">
      <div class="header-left">
        <h1>监控系统</h1>
        <el-tag class="version-tag" type="primary">V2.0</el-tag>
      </div>
      
      <div class="header-right">
        <el-button icon="Refresh" @click="refreshAll" :loading="loading">刷新数据</el-button>
        <el-badge :value="pendingAlerts" :hidden="pendingAlerts === 0">
          <el-button icon="Bell" @click="goToAlerts">告警中心</el-button>
        </el-badge>
      </div>
    </div>

    <!-- 三个核心卡片 - 简洁版 -->
    <el-row :gutter="20" class="mt20 widget-row">
      <!-- 卡片1：监控配置 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
        <ConfigCard :stats="configStats" :loading="loading" @refresh="loadConfigStats" />
      </el-col>
      
      <!-- 卡片2：监控任务 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
        <TaskCard :stats="taskStats" :loading="loading" @refresh="loadTaskStats" />
      </el-col>
      
      <!-- 卡片3：批次执行 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
        <BatchCard :stats="batchStats" :loading="loading" @refresh="loadBatchStats" />
      </el-col>
    </el-row>

    <!-- 通知中心 - 双流监控 -->
    <el-row :gutter="16" class="mt20 notification-row">
      <!-- BSC 全局监控动态 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <NotificationCenter />
      </el-col>
      
      <!-- SOL 智能监控动态 -->
      <el-col :xs="24" :sm="24" :md="12" :lg="12">
        <SolNotificationCenter />
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import ConfigCard from '@/views/components/monitor/ConfigCard.vue'
import TaskCard from '@/views/components/monitor/TaskCard.vue'
import BatchCard from '@/views/components/monitor/BatchCard.vue'
import NotificationCenter from '@/views/components/NotificationCenter.vue'
import SolNotificationCenter from '@/views/components/SolNotificationCenter.vue'

const router = useRouter()
const loading = ref(false)
const configStats = ref({})
const taskStats = ref({})
const batchStats = ref({})
const pendingAlerts = ref(28)

// 加载所有数据
const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadConfigStats(),
      loadTaskStats(),
      loadBatchStats()
    ])
  } finally {
    loading.value = false
  }
}

// 加载配置统计
const loadConfigStats = async () => {
  // TODO: 替换为实际API调用
  // const res = await axios.get('/api/v2/monitor/config/stats')
  
  // Mock数据
  await new Promise(resolve => setTimeout(resolve, 300))
  configStats.value = {
    total: 15,
    preset: 3,
    custom: 12,
    lastUpdate: new Date().toISOString()
  }
}

// 加载任务统计
const loadTaskStats = async () => {
  // TODO: 替换为实际API调用
  // const res = await axios.get('/api/v2/monitor/task/stats')
  
  // Mock数据
  await new Promise(resolve => setTimeout(resolve, 300))
  taskStats.value = {
    total: 32,
    running: 28,
    paused: 2,
    error: 2,
    smart: 18,
    batch: 14
  }
}

// 加载批次统计
const loadBatchStats = async () => {
  // TODO: 替换为实际API调用
  // const res = await axios.get('/api/v2/monitor/batch/stats')
  
  // Mock数据
  await new Promise(resolve => setTimeout(resolve, 300))
  batchStats.value = {
    active: 312,
    normal: 285,
    delayed: 20,
    timeout: 7,
    lastUpdate: new Date().toISOString()
  }
}

// 刷新所有数据
const refreshAll = () => {
  loadAllData()
}

// 跳转到告警中心
const goToAlerts = () => {
  router.push('/monitor/alert/list')
}

// 模拟实时更新
let updateTimer = null
const setupRealtimeUpdate = () => {
  updateTimer = setInterval(() => {
    // 随机更新一些数据
    if (Math.random() > 0.7) {
      pendingAlerts.value = Math.floor(Math.random() * 50)
    }
  }, 5000)
}

onMounted(() => {
  console.log('监控系统 V2.0 首页加载完成')
  loadAllData()
  setupRealtimeUpdate()
  
  // 定时刷新
  const refreshTimer = setInterval(() => {
    loadAllData()
  }, 30000)
  
  onUnmounted(() => {
    clearInterval(refreshTimer)
    if (updateTimer) clearInterval(updateTimer)
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
  
  // Dashboard头部
  .dashboard-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    background: white;
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
      color: #303133;
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
  
  // Widget行样式
  .widget-row {
    .widget-col {
      margin-bottom: 20px;
    }
  }
  
  // 通知中心行样式
  .notification-row {
    .el-col {
      margin-bottom: 20px;
    }
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .home {
    .el-row .el-col {
      margin-bottom: 16px;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .home {
    padding: 10px;
    
    .mt20 {
      margin-top: 16px;
    }

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
    
    .widget-row {
      .widget-col {
        margin-bottom: 16px;
      }
    }
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .home {
    padding: 8px;
    
    .mt20 {
      margin-top: 12px;
    }

    .dashboard-header {
      .header-left h1 {
        font-size: 20px;
      }
    }
  }
}
</style>
