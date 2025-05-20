<template>
  <el-row :gutter="20">
    <el-col :span="8">
      <el-card class="data-card" v-loading="loading">
        <template #header>
          <div class="card-header">
            <span>平台数据</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchDailyStats" 
                :loading="loading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-tag type="warning" size="small">今日</el-tag>
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
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>机器人状态</span>
            <el-tag type="success" size="small">实时</el-tag>
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
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>告警统计</span>
            <el-tag type="danger" size="small">今日</el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon class="mr8"><Warning /></el-icon>
              <span class="label">总告警数</span>
            </div>
            <h3>{{ alertStats.total }}</h3>
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

// 添加loading状态
const loading = ref(false)

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
  loading.value = true
  try {
    const res = await getDailyActivityStats()

    if (res.code === 200 && res.data) {
      // 更新组件数据
      queryCount.value = res.data.totalQueries || 0
      activeUsers.value = res.data.activeUsers || 0
      ElMessage.success('平台数据已更新')
    } else {
      ElMessage.warning('获取平台数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取平台数据异常，请检查网络连接或后端服务')
  } finally {
    loading.value = false
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchDailyStats()

  // 添加定时刷新(每半分钟刷新一次)
  const timer = setInterval(() => {
    fetchDailyStats()
  }, 30 * 1000)

  // 组件卸载时清除定时器
  onUnmounted(() => {
    if (timer) clearInterval(timer)
  })
})
</script>

<style scoped lang="scss">
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
  }

  .card-body {
    padding: 16px;
    background-color: #ffffff !important;

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
          color: #606266 !important;
        }
      }

      .label {
        color: #606266 !important;
        font-size: 13px;
      }

      h3 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
        color: #303133 !important;
        font-family: 'Roboto', sans-serif;

        &.warning {
          color: #E6A23C !important;
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
}

.el-tag--warning {
  background: #fdf6ec;
  color: #E6A23C;
  border: none;
}

.el-tag--success {
  background: #f0f9eb;
  color: #67C23A;
  border: none;
}

.el-tag--danger {
  background: #fef0f0;
  color: #F56C6C;
  border: none;
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
      }
    }
  }
  
  :deep(.el-card) {
    background-color: #1d1e1f !important;
    border-color: #434343 !important;
    color: #ffffff !important;
  }
}

/* 添加刷新按钮样式 */
.refresh-btn {
  margin-right: 8px;
  padding: 2px;
  font-size: 14px;
}
</style> 