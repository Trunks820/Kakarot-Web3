<template>
  <el-row :gutter="20">
    <el-col :span="8">
      <el-card class="data-card" v-loading="platformLoading" element-loading-background="rgba(0, 0, 0, 0.1)" element-loading-text="加载中...">
        <template #header>
          <div class="card-header">
            <span>平台数据</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchDailyStats" 
                :loading="platformLoading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-tag type="warning" size="small" class="status-tag">今日</el-tag>
            </div>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon class="mr8"><Search /></el-icon>
              <span class="label">查询次数</span>
            </div>
            <h3>{{ queryCount }}</h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon class="mr8"><User /></el-icon>
              <span class="label">活跃用户</span>
            </div>
            <h3>{{ activeUsers }}</h3>
          </div>
        </div>
      </el-card>
    </el-col>

    <el-col :span="8">
      <el-card class="data-card" v-loading="botLoading" element-loading-background="rgba(0, 0, 0, 0.1)" element-loading-text="加载中...">
        <template #header>
          <div class="card-header">
            <span>机器人状态</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchBotStatus" 
                :loading="botLoading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-tag type="success" size="small" class="status-tag">实时</el-tag>
            </div>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <span class="label">Telegram</span>
            <el-tag :type="tgBotStatus.online ? 'success' : 'danger'" size="small">
              {{ tgBotStatus.online ? '在线' : '离线' }}
            </el-tag>
          </div>
          <div class="stat-item">
            <span class="label">微信</span>
            <el-tag :type="wxBotStatus.online ? 'success' : 'danger'" size="small">
              {{ wxBotStatus.online ? '在线' : '离线' }}
            </el-tag>
          </div>
        </div>
      </el-card>
    </el-col>
    
    <el-col :span="8">
      <el-card class="data-card" v-loading="alertLoading" element-loading-background="rgba(0, 0, 0, 0.1)" element-loading-text="加载中...">
        <template #header>
          <div class="card-header">
            <span>告警统计</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchAlertStats" 
                :loading="alertLoading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-tag type="danger" size="small" class="status-tag">今日</el-tag>
            </div>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon class="mr8"><Warning /></el-icon>
              <span class="label">总告警数</span>
            </div>
            <h3 class="danger">{{ alertStats.total }}</h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon class="mr8"><Warning /></el-icon>
              <span class="label">待处理</span>
            </div>
            <h3 :class="{ 'warning': alertStats.pending > 5 }">{{ alertStats.pending }}</h3>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { Search, User, Warning, Refresh } from '@element-plus/icons-vue'
import useSettingsStore from '@/store/modules/settings'
import { ElMessage } from 'element-plus'
import { getDailyActivityStats } from '@/api/crypto/index'

const settingsStore = useSettingsStore()

// 为每个卡片添加独立的loading状态
const platformLoading = ref(false)
const botLoading = ref(false)
const alertLoading = ref(false)

// 数据统计
const queryCount = ref(0)
const activeUsers = ref(0)
const tgBotStatus = ref({ online: true })
const wxBotStatus = ref({ online: true })

// 告警统计数据
const alertStats = ref({
  total: 24,
  pending: 8
})

// 添加API请求方法 - 获取每日统计数据
const fetchDailyStats = async () => {
  platformLoading.value = true
  try {
    // 添加模拟延迟，使行为与其他卡片一致
    const res = await new Promise((resolve) => {
      setTimeout(async () => {
        try {
          const apiRes = await getDailyActivityStats()
          resolve(apiRes)
        } catch (err) {
          resolve({ code: 500, msg: err.message })
        }
      }, 600)
    })

    if (res.code === 200 && res.data) {
      // 更新组件数据
      queryCount.value = res.data.totalQueries || 0
      activeUsers.value = res.data.activeUsers || 0
    } else {
      ElMessage.warning('获取平台数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取平台数据异常，请检查网络连接或后端服务')
  } finally {
    platformLoading.value = false
  }
}

// 添加获取机器人状态的方法
const fetchBotStatus = async () => {
  botLoading.value = true
  try {
    // 这里可以添加实际的API调用
    // 模拟数据更新
    setTimeout(() => {
      tgBotStatus.value = { online: Math.random() > 0.2 }
      wxBotStatus.value = { online: Math.random() > 0.2 }
      botLoading.value = false
    }, 600)
  } catch (error) {
    ElMessage.error('获取机器人状态异常')
    botLoading.value = false
  }
}

// 添加获取告警统计的方法
const fetchAlertStats = async () => {
  alertLoading.value = true
  try {
    // 这里可以添加实际的API调用
    // 模拟数据更新
    setTimeout(() => {
      alertStats.value = {
        total: Math.floor(Math.random() * 50) + 10,
        pending: Math.floor(Math.random() * 20)
      }
      alertLoading.value = false
    }, 600)
  } catch (error) {
    ElMessage.error('获取告警统计异常')
    alertLoading.value = false
  }
}

// 刷新所有数据
const refreshAllData = () => {
  fetchDailyStats()
  fetchBotStatus()
  fetchAlertStats()
}

// 组件挂载时获取数据
onMounted(() => {
  refreshAllData()

  // 添加定时刷新(每半分钟刷新一次)
  const timer = setInterval(() => {
    refreshAllData()
  }, 30 * 1000)

  // 组件卸载时清除定时器
  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })
})
</script>

<style scoped lang="scss">
/* 添加全局loading样式 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(1px);
}

:deep(.el-loading-spinner) {
  .circular {
    width: 30px;
    height: 30px;
  }
  
  .el-loading-text {
    font-size: 14px;
    margin-top: 8px;
    color: #409EFF;
  }
}

/* Dark theme loading style */
:global(html.dark) {
  :deep(.el-loading-mask) {
    background-color: rgba(29, 30, 31, 0.7) !important;
  }
  
  :deep(.el-loading-spinner) {
    .el-loading-text {
      color: #409EFF !important;
    }
  }
}

/* Light theme styles */
.data-card {
  height: 200px;
  background-color: #ffffff;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease-in-out;
  
  :deep(.el-card__header) {
    padding: 0;
    border: none;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    border-bottom: 1px solid #e4e7ed;
    background-color: #f5f7fa;

    span {
      font-size: 14px;
      color: #303133;
      font-weight: 600;
    }

    .el-tag {
      padding: 0 8px;
      height: 20px;
      line-height: 20px;
      font-size: 11px;
    }
    
    /* 标签样式统一 */
    .status-tag {
      border-radius: 4px;
      padding: 0 8px;
      height: 22px;
      line-height: 22px;
      font-size: 12px;
      font-weight: normal;
      box-shadow: 0 0 4px rgba(0, 0, 0, 0.1);
      opacity: 0.95;
    }
    
    /* 改进刷新按钮样式 */
    .refresh-btn {
      margin-right: 8px;
      padding: 2px;
      font-size: 14px;
      color: #909399;
      transition: all 0.3s;
      
      &:hover {
        color: #409EFF;
        transform: rotate(90deg);
      }
      
      &:active {
        transform: rotate(180deg);
      }
    }
  }

  .card-body {
    padding: 16px;
    background-color: #ffffff;

    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      &:last-child {
        margin-bottom: 0;
      }

      .stat-label {
        display: flex;
        align-items: center;
        
        .mr8 {
          margin-right: 8px;
          color: #606266;
        }
      }

      .label {
        color: #606266;
        font-size: 13px;
      }

      h3 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
        color: #303133;
        font-family: 'Roboto', sans-serif;

        &.warning {
          color: #E6A23C;
        }
        
        &.danger {
          color: #F56C6C;
        }
      }

      .el-tag {
        padding: 0 8px;
        height: 22px;
        line-height: 22px;
        font-size: 12px;
        border-radius: 3px;
      }
    }
  }
}

:deep(.el-card) {
  background-color: #ffffff;
  border-color: #e4e7ed;
  color: #303133;
  transition: all 0.3s ease-in-out;
  box-shadow: none;
}

.el-tag {
  transition: all 0.3s ease-in-out;
  position: relative;
  
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.15);
  }
}

.el-tag--warning {
  background: #fdf6ec;
  color: #E6A23C;
  border: none;
  box-shadow: 0 1px 3px rgba(230, 162, 60, 0.2);
}

.el-tag--success {
  background: #f0f9eb;
  color: #67C23A;
  border: none;
  box-shadow: 0 1px 3px rgba(103, 194, 58, 0.2);
}

.el-tag--danger {
  background: #fef0f0;
  color: #F56C6C;
  border: none;
  box-shadow: 0 1px 3px rgba(245, 108, 108, 0.2);
}

/* Dark theme styles - with !important to override any other styles */
:global(html.dark) {
  .data-card {
    background-color: #1d1e1f !important;
    border-color: #434343 !important;
    
    .card-header {
      background-color: #141414 !important;
      border-color: #434343 !important;
      
      span {
        color: #ffffff !important;
      }
      
      /* 暗黑模式下刷新按钮样式 */
      .refresh-btn {
        color: #909399 !important;
        
        &:hover {
          color: #409EFF !important;
        }
      }
      
      /* 标签样式优化 */
      .el-tag {
        border: none !important;
        box-shadow: 0 0 5px rgba(255, 255, 255, 0.1) !important;
      }
      
      .el-tag--warning {
        background-color: rgba(230, 162, 60, 0.25) !important;
        border-color: transparent !important;
        
        .el-tag__content {
          color: #E6A23C !important;
        }
      }
      
      .el-tag--success {
        background-color: rgba(103, 194, 58, 0.25) !important;
        border-color: transparent !important;
        
        .el-tag__content {
          color: #67C23A !important;
        }
      }
      
      .el-tag--danger {
        background-color: rgba(245, 108, 108, 0.25) !important;
        border-color: transparent !important;
        
        .el-tag__content {
          color: #F56C6C !important;
        }
      }
      
      /* 确保今日标签文字颜色正确 */
      .el-tag span:not([class]) {
        color: inherit !important;
      }
      
      /* 确保标签样式统一 */
      .status-tag {
        border-radius: 4px !important;
      }
    }
    
    .card-body {
      background-color: #1d1e1f !important;
      
      .stat-label .mr8 {
        color: #d0d0d0 !important;
      }
      
      .label {
        color: #d0d0d0 !important;
      }
      
      h3 {
        color: #ffffff !important;
        
        &.warning {
          color: #E6A23C !important;
        }
        
        &.danger {
          color: #F56C6C !important;
        }
      }
    }
  }
  
  :deep(.el-card) {
    background-color: #1d1e1f !important;
    border-color: #434343 !important;
    color: #ffffff !important;
  }
  
  .el-tag--warning {
    background: rgba(230, 162, 60, 0.25) !important;
    color: #E6A23C !important;
  }
  
  .el-tag--success {
    background: rgba(103, 194, 58, 0.25) !important;
    color: #67C23A !important;
  }
  
  .el-tag--danger {
    background: rgba(245, 108, 108, 0.25) !important;
    color: #F56C6C !important;
  }
}
</style> 