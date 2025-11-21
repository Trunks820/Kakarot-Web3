<template>
  <div class="app-container home">
    <!-- 三个核心卡片 - 增加悬浮感和统一容器 -->
    <div class="core-control-area">
      <div class="area-header">
        <span class="title">控制中心</span>
        <el-tag size="small" type="info" effect="plain">V2.0 Alpha</el-tag>
      </div>
      <el-row :gutter="20" class="widget-row">
        <!-- 卡片1：监控配置 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
          <div class="hover-card">
            <ConfigCard :stats="configStats" :loading="loading" @refresh="loadConfigStats" />
          </div>
        </el-col>
        
        <!-- 卡片2：监控任务 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
          <div class="hover-card">
            <TaskCard :stats="taskStats" :loading="loading" @refresh="loadTaskStats" />
          </div>
        </el-col>
        
        <!-- 卡片3：批次执行 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
          <div class="hover-card">
            <BatchCard :stats="batchStats" :loading="loading" @refresh="loadBatchStats" />
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 通知中心 - 双流监控 -->
    <el-row :gutter="20" class="mt20 notification-row">
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
import { getConfigStats, listConfig } from '@/api/crypto/monitor-v2'
import { getTaskStats } from '@/api/crypto/monitor-v2'
import { getBatchStats } from '@/api/crypto/monitor-v2'
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
  try {
    const response = await getConfigStats()
    const data = response.data || {}
    
    
    // 获取总数
    const total = data.total || 0
    let sol = 0, bsc = 0, eth = 0
    
    // 尝试从 listConfig 接口获取真实的链类型分布
    try {
      const configList = await listConfig({ pageNum: 1, pageSize: 1000 })
      
      if (configList.rows && Array.isArray(configList.rows)) {
        configList.rows.forEach(config => {
          const chainType = (config.chainType || 'sol').toLowerCase()
          if (chainType === 'sol') sol++
          else if (chainType === 'bsc') bsc++
          else if (chainType === 'eth') eth++
        })
      }
    } catch (err) {
      console.warn('【ConfigList 获取失败，使用均分】', err)
      // 如果获取失败，回退到均分
      sol = Math.ceil(total / 3)
      bsc = Math.floor(total / 3)
      eth = total - sol - bsc
    }
    
    configStats.value = {
      total: data.total || 0,
      preset: data.preset || 0,
      custom: data.custom || 0,
      lastUpdate: data.lastUpdate,
      sol,
      bsc,
      eth
    }
    
  } catch (error) {
    console.error('加载配置统计失败:', error)
  }
}

// 加载任务统计
const loadTaskStats = async () => {
  try {
    const response = await getTaskStats()
    const data = response.data || {}
    
    
    // ⚠️ 接口返回 total=6 但 running=8，不合逻辑
    // 应该用 running + paused + error 作为实际总数
    const running = data.running || 0
    const paused = data.paused || 0
    const error = data.error || data.errorCount || 0
    const total = running + paused + error  // 用实际总数，不用接口的 total
    
    taskStats.value = {
      total,
      running,
      paused,
      error,
      smart: data.smart || data.smartCount || 0,
      batch: data.batch || data.batchCount || 0,
      block: data.block || data.blockCount || 0
    }
    
  } catch (error) {
    console.error('加载任务统计失败:', error)
  }
}

// 加载批次统计
const loadBatchStats = async () => {
  try {
    const response = await getBatchStats()
    const data = response.data || {}
    
    // 映射批次状态字段
    batchStats.value = {
      total: data.total || 0,
      running: data.running || 0,
      paused: data.paused || 0,
      pending: data.pending || 0,
      error: data.error || data.errorCount || 0,
      targetCount: data.targetCount || 0,
      heartbeatNormal: data.heartbeatNormal || data.normal || 0,
      heartbeatTimeout: data.heartbeatTimeout || data.timeout || 0,
      lastUpdate: data.lastUpdate,
      errorBatches: data.errorBatches || []
    }
  } catch (error) {
    console.error('加载批次统计失败:', error)
  }
}

// 跳转到告警中心
const goToAlerts = () => {
  router.push('/monitor/alert/list')
}

onMounted(() => {
  loadAllData()
  
  // 定时刷新
  const refreshTimer = setInterval(() => {
    loadAllData()
  }, 30000)
  
  onUnmounted(() => {
    clearInterval(refreshTimer)
  })
})
</script>

<style scoped lang="scss">
.home {
  padding: 20px;
  background-color: #f6f8f9;
  min-height: 100vh;

  .mt20 {
    margin-top: 20px;
  }

  // 1. 顶部 HUD 样式 (已删除，相关样式代码移除以保持清洁)

  // 2. 核心控制区样式
  .core-control-area {
    margin-bottom: 24px;

    .area-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 16px;
      padding-left: 4px;

      .title {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        border-left: 4px solid #409eff;
        padding-left: 12px;
      }
    }

    .widget-row {
      .hover-card {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        height: 100%;
        position: relative;
        border-radius: 8px;
        overflow: hidden;
        
        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: -100%;
          width: 100%;
          height: 100%;
          background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
          transition: left 0.3s ease;
          z-index: 1;
          pointer-events: none;
        }
        
        &:hover {
          transform: translateY(-6px);
          animation: card-glow 2s ease-in-out infinite;
          
          &::before {
            left: 100%;
          }
          
          // 让内部的 el-card 阴影加深，这里假设组件内部是 el-card
          :deep(.el-card) {
            box-shadow: 0 16px 40px rgba(64, 158, 255, 0.2) !important;
          }
        }
      }
    }
  }
  
  @keyframes card-glow {
    0%, 100% {
      filter: drop-shadow(0 0 0px rgba(64, 158, 255, 0)) drop-shadow(0 0 0px rgba(64, 158, 255, 0));
    }
    25% {
      filter: drop-shadow(0 0 4px rgba(64, 158, 255, 0.1)) drop-shadow(0 0 0px rgba(64, 158, 255, 0));
    }
    50% {
      filter: drop-shadow(0 0 12px rgba(64, 158, 255, 0.3)) drop-shadow(0 0 6px rgba(64, 158, 255, 0.15));
    }
    75% {
      filter: drop-shadow(0 0 8px rgba(64, 158, 255, 0.2)) drop-shadow(0 0 0px rgba(64, 158, 255, 0));
    }
  }
  
  /* 刷新时的缩放反馈 */
  @keyframes refresh-pulse {
    0% { transform: scale(1); }
    50% { transform: scale(1.05); }
    100% { transform: scale(1); }
  }
  
  /* 应用到卡片上 - 标记刷新状态 */
  .widget-row {
    .hover-card.refreshing {
      animation: refresh-pulse 0.6s ease-in-out;
    }
  }
  
  // 通知中心行样式
  .notification-row {
    .el-col {
      margin-bottom: 20px;
    }
  }
}

/* 平板/小屏适配 */
@media (max-width: 1200px) {
  .home {
    .el-row .el-col {
      margin-bottom: 16px;
    }
    
    .core-control-area {
      .area-header {
        .title {
          font-size: 14px;
        }
      }
      
      :deep(.concentric-circles) {
        width: 140px !important;
        height: 140px !important;
      }
      
      :deep(.gauge-wrapper) {
        width: 140px !important;
        height: 140px !important;
      }
      
      :deep(.circles-svg) {
        width: 100%;
        height: 100%;
      }
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .home {
    padding: 10px;
    
    .widget-row {
      .widget-col {
        margin-bottom: 16px;
      }
    }
    
    .core-control-area {
      .area-header {
        .title {
          font-size: 13px;
        }
      }
      
      :deep(.chart-title-row) {
        flex-direction: column;
        gap: 4px;
        align-items: flex-start;
      }
      
      :deep(.bars-container) {
        height: 140px;
      }
    }
  }
}
</style>

