<template>
  <el-row :gutter="20">
    <el-col :span="6">
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

    <el-col :span="6">
      <el-card class="data-card" v-loading="rankLoading" element-loading-background="rgba(0, 0, 0, 0.1)" element-loading-text="加载中...">
        <template #header>
          <div class="card-header">
            <span>用户胜率 TOP3</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchUserRankings" 
                :loading="rankLoading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-tag type="warning" size="small" class="status-tag">实时</el-tag>
            </div>
          </div>
        </template>
        <div class="card-body rank-top3">
          <div class="rank-list">
            <div v-for="(user, index) in top3Users" :key="user.user_id" class="rank-item">
              <div class="rank-number" :class="`rank-${index + 1}`">{{ index + 1 }}</div>
              <div class="user-info">
                <div class="username">{{ user.user_id }}</div>
                <div class="stats">
                  <span :class="getRateClass((user.success/user.count) * 100)">
                    {{ ((user.success/user.count) * 100).toFixed(1) }}%
                  </span>
                  <span class="prediction-count">({{ user.success }}/{{ user.count }})</span>
                </div>
              </div>
            </div>
          </div>
<!--          <div class="view-all">-->
<!--            <router-link to="/rank/predict" class="el-link el-link&#45;&#45;primary">-->
<!--              <span>查看完整排行榜</span>-->
<!--              <el-icon class="el-icon&#45;&#45;right"><arrow-right /></el-icon>-->
<!--            </router-link>-->
<!--          </div>-->
        </div>
      </el-card>
    </el-col>

    <el-col :span="12">
      <el-card class="data-card" v-loading="botLoading || wxBotLoading" element-loading-background="rgba(0, 0, 0, 0.1)" element-loading-text="加载中...">
        <template #header>
          <div class="card-header">
            <span>机器人状态</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchBotStatus(); fetchWxBotStatus()" 
                :loading="botLoading || wxBotLoading"
                class="refresh-btn"
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-tag type="success" size="small" class="status-tag">实时</el-tag>
            </div>
          </div>
        </template>
        <div class="card-body bot-table">
          <div class="bot-table-header">
            <div class="col-bot">机器人</div>
            <div class="col-status">状态</div>
            <div class="col-uptime">运行时长</div>
            <div class="col-lastcheck">最后检查</div>
            <div class="col-errors">错误次数</div>
            <div class="col-actions">重启按钮</div>
          </div>
          <div class="bot-table-row">
            <div class="col-bot">TG</div>
            <div class="col-status">
              <el-tag :type="getBotStatusType(tgBotStatus.status)" size="small">
                {{ getBotStatusText(tgBotStatus.status) }}
              </el-tag>
            </div>
            <div class="col-uptime">{{ currentUptime.tg }}</div>
            <div class="col-lastcheck">{{ tgBotStatus.last_check_time || '-' }}</div>
            <div class="col-errors">{{ tgBotStatus.restart_count || 0 }}次</div>
            <div class="col-actions">
              <el-button 
                type="primary"
                link
                :loading="tgActionLoading"
                @click="handleTgBotAction('restart')"
              >
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>
          </div>
          <div class="bot-table-row">
            <div class="col-bot">WX</div>
            <div class="col-status">
              <el-tag :type="wxBotStatus.online ? 'success' : 'danger'" size="small">
                {{ wxBotStatus.online ? '在线' : '离线' }}
              </el-tag>
            </div>
            <div class="col-uptime">{{ currentUptime.wx }}</div>
            <div class="col-lastcheck">{{ wxBotStatus.lastCheckTime || '-' }}</div>
            <div class="col-errors">{{ wxBotStatus.restartCount || 0 }}次</div>
            <div class="col-actions">
              <el-button 
                type="primary"
                link
                :loading="wxActionLoading"
                @click="handleWxBotAction('restart')"
              >
                <el-icon><RefreshRight /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Search, User, Refresh, RefreshRight, ArrowRight } from '@element-plus/icons-vue'
import useSettingsStore from '@/store/modules/settings'
import { ElMessage } from 'element-plus'
import { getDailyActivityStats, getTgBotStatus, getUserRange } from '@/api/crypto/index'

const settingsStore = useSettingsStore()

// 为每个卡片添加独立的loading状态
const platformLoading = ref(false)
const botLoading = ref(false)
const wxBotLoading = ref(false)

// 数据统计
const queryCount = ref(0)
const activeUsers = ref(0)
const tgBotStatus = ref({
  status: 'unknown',
  pid: '-',
  restart_count: 0,
  start_time: '-',
  last_check_time: '-'
})
const wxBotStatus = ref({
  online: true,
  pid: '-',
  restartCount: 0,
  startTime: '-',
  lastCheckTime: '-'
})

// 告警统计数据
const alertStats = ref({
  total: 24,
  pending: 8
})

// 系统状态相关
const systemLoading = ref(false)
const systemStatus = ref({
  memory: {
    total: '2G',
    used: '1G',
    percentage: 50
  },
  uptime: '-',
  lastOperation: null
})

// 操作相关的loading状态
const tgActionLoading = ref(false)
const wxActionLoading = ref(false)

// 添加用户排行相关的状态
const rankLoading = ref(false)
const userRankings = ref([
  { username: '超哥', rate: 87.5, successCount: 14, totalCount: 16 },
  { username: '铁头娃', rate: 75.0, successCount: 9, totalCount: 12 },
  { username: '三点水', rate: 66.7, successCount: 6, totalCount: 9 }
])

// 添加格式化运行时长的函数
const formatUptime = (startTimeStr) => {
  if (!startTimeStr || startTimeStr === '-') return '-'
  
  const startTime = new Date(startTimeStr).getTime()
  const now = new Date().getTime()
  const diff = now - startTime
  
  // 如果时间差异无效，返回 '-'
  if (diff < 0) return '-'
  
  // 计算时、分、秒
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  // 构建显示字符串
  let uptimeStr = ''
  if (hours > 0) uptimeStr += `${hours}h`
  if (minutes > 0) uptimeStr += `${minutes}min`
  if (seconds > 0 && hours === 0) uptimeStr += `${seconds}s`
  
  return uptimeStr || '刚刚启动'
}

// 添加自动更新运行时长的功能
const uptimeTimer = ref(null)
const currentUptime = ref({
  tg: '-',
  wx: '-'
})

// 更新运行时长显示
const updateUptime = () => {
  currentUptime.value.tg = formatUptime(tgBotStatus.value.start_time)
  currentUptime.value.wx = formatUptime(wxBotStatus.value.startTime)
}

// 处理TG机器人操作
const handleTgBotAction = async (action) => {
  tgActionLoading.value = true
  try {
    // 这里添加实际的API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`TG机器人${action === 'restart' ? '重启' : '停止'}成功`)
    fetchBotStatus()
  } catch (error) {
    ElMessage.error(`TG机器人${action === 'restart' ? '重启' : '停止'}失败`)
  } finally {
    tgActionLoading.value = false
  }
}

// 处理微信机器人操作
const handleWxBotAction = async (action) => {
  wxActionLoading.value = true
  try {
    // 这里添加实际的API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success(`微信机器人${action === 'restart' ? '重启' : '停止'}成功`)
    fetchWxBotStatus()
  } catch (error) {
    ElMessage.error(`微信机器人${action === 'restart' ? '重启' : '停止'}失败`)
  } finally {
    wxActionLoading.value = false
  }
}

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

// 添加状态类型判断方法
const getBotStatusType = (status) => {
  switch (status?.toLowerCase()) {
    case 'running':
      return 'success'
    case 'starting':
      return 'info'
    case 'restarting':
      return 'warning'
    case 'stopped':
      return 'danger'
    case 'error':
      return 'danger'
    default:
      return 'info'
  }
}

// 获取状态显示文本
const getBotStatusText = (status) => {
  switch (status?.toLowerCase()) {
    case 'running':
      return '运行中'
    case 'starting':
      return '启动中'
    case 'restarting':
      return '重启中'
    case 'stopped':
      return '已停止'
    case 'error':
      return '错误'
    default:
      return '未知'
  }
}

// 添加格式化日期的函数
const formatDateTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  if (isNaN(d.getTime())) return '-'
  
  const pad = (num) => String(num).padStart(2, '0')
  
  const year = d.getFullYear()
  const month = pad(d.getMonth() + 1)
  const day = pad(d.getDate())
  const hours = pad(d.getHours())
  const minutes = pad(d.getMinutes())
  const seconds = pad(d.getSeconds())
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 修改微信机器人状态的模拟数据
const fetchWxBotStatus = async () => {
  wxBotLoading.value = true
  try {
    // 这里添加实际的API调用
    // 模拟数据更新
    setTimeout(() => {
      wxBotStatus.value = {
        online: true,
        pid: '12345',
        restartCount: Math.floor(Math.random() * 5),
        startTime: '2025-05-24 08:30:00',
        lastCheckTime: formatDateTime(new Date())
      }
      wxBotLoading.value = false
    }, 600)
  } catch (error) {
    wxBotStatus.value = {
      online: false,
      pid: '-',
      restartCount: 0,
      startTime: '-',
      lastCheckTime: '-'
    }
    ElMessage.error('获取微信机器人状态异常')
    wxBotLoading.value = false
  }
}

// 更新TG机器人状态的模拟数据格式
const fetchBotStatus = async () => {
  botLoading.value = true
  try {
    const res = await getTgBotStatus()
    if (res.code === 200 && res.data) {
      tgBotStatus.value = {
        ...res.data,
        last_check_time: formatDateTime(res.data.last_check_time)
      }
    } else {
      // 请求失败时设置为错误状态
      tgBotStatus.value = {
        status: 'error',
        pid: '-',
        restart_count: 0,
        start_time: '-',
        last_check_time: '-'
      }
      ElMessage.warning('获取机器人状态失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    // 异常时设置为错误状态
    tgBotStatus.value = {
      status: 'error',
      pid: '-',
      restart_count: 0,
      start_time: '-',
      last_check_time: '-'
    }
    ElMessage.error('获取机器人状态异常，请检查网络连接或后端服务')
  } finally {
    botLoading.value = false
  }
}

// 修改刷新所有数据的方法
const refreshAllData = () => {
  fetchDailyStats()
  fetchBotStatus()
  fetchWxBotStatus()
}

// 添加定时刷新
let statusTimer = null

// 获取胜率样式
const getRateClass = (rate) => {
  if (rate >= 80) return 'rate-excellent'
  if (rate >= 60) return 'rate-good'
  return 'rate-normal'
}

// 获取用户排行数据
const fetchUserRankings = async () => {
  rankLoading.value = true
  try {
    const res = await getUserRange()
    if (res.code === 200) {
      userRankings.value = res.data
    }
  } catch (error) {
    console.error('获取用户排行失败:', error)
  } finally {
    rankLoading.value = false
  }
}

// 计算前三名用户
const top3Users = computed(() => {
  return [...userRankings.value]
    .sort((a, b) => {
      // 首先按胜率排序
      const rateA = (a.success / a.count) * 100;
      const rateB = (b.success / b.count) * 100;
      if (rateB !== rateA) {
        return rateB - rateA;  // 胜率降序
      }
      // 胜率相同时，按查询总数降序
      return b.count - a.count;
    })
    .slice(0, 3)
})

onMounted(() => {
  fetchBotStatus()
  fetchWxBotStatus()
  // 每60秒更新一次状态
  statusTimer = setInterval(() => {
    fetchBotStatus()
    fetchWxBotStatus()
  }, 60000)

  // 组件挂载时获取数据
  refreshAllData()

  // 添加运行时长更新定时器
  updateUptime() // 立即执行一次
  uptimeTimer.value = setInterval(updateUptime, 1000)

  // 添加定时刷新(每分钟刷新一次)
  const timer = setInterval(() => {
    refreshAllData()
  }, 60 * 1000)

  // 组件卸载时清除定时器
  onUnmounted(() => {
    if (timer) clearInterval(timer)
    if (statusTimer) {
      clearInterval(statusTimer)
    }
    if (uptimeTimer.value) {
      clearInterval(uptimeTimer.value)
      uptimeTimer.value = null
    }
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

  :deep(.el-card__body) {
    padding: 0;  /* 移除默认内边距 */
    height: calc(100% - 45px);  /* 减去header高度 */
    display: flex;
    flex-direction: column;
  }

  .card-header {
    height: 45px;  /* 固定header高度 */
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 16px;
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
    padding: 4px;
    background-color: #ffffff;

    .stat-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

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

    .stat-item-bot {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
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

/* 添加新的状态标签样式 */
.el-tag--info {
  background: #f4f4f5;
  color: #909399;
  border: none;
  box-shadow: 0 1px 3px rgba(144, 147, 153, 0.2);
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
  
  .el-tag--info {
    background: rgba(144, 147, 153, 0.25) !important;
    color: #909399 !important;
  }
}

.action-btn {
  margin-right: 4px;
  padding: 2px;
  font-size: 14px;
  color: #909399;
  transition: all 0.3s;
  
  &:hover {
    color: #409EFF;
    transform: scale(1.1);
  }
}

.alert-text {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  cursor: pointer;
}

.service-status {
  display: flex;
  gap: 8px;
  align-items: center;
}

.mr4 {
  margin-right: 4px;
}

.memory-usage {
  width: 120px;
  .el-progress {
    margin: 0;
  }
}

.operation-text {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #606266;
  cursor: pointer;
}

.bot-table {
  padding: 0 !important;
}

.bot-table-header {
  display: flex;
  align-items: center;
  padding: 8px 0;
  background-color: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
  font-weight: 500;
  color: var(--el-text-color-regular);
  font-size: 13px;
}

.bot-table-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background-color: var(--el-fill-color-lighter);
  }
}

.col-bot {
  width: 80px;
  flex-shrink: 0;
  text-align: center;
  padding-left: 20px;
}

.col-status {
  width: 80px;
  flex-shrink: 0;
  text-align: center;
}

.col-uptime {
  width: 120px;
  flex-shrink: 0;
  text-align: center;
}

.col-lastcheck {
  width: 160px;
  flex-shrink: 0;
  text-align: center;
}

.col-errors {
  width: 80px;
  flex-shrink: 0;
  text-align: center;
}

.col-actions {
  width: 80px;
  flex-shrink: 0;
  text-align: center;
  padding-right: 20px;
}

:deep(.el-button--link) {
  padding: 0;
  height: auto;
  
  .el-icon {
    font-size: 16px;
  }
}

.rank-top3 {
  height: 100%;  /* 填充整个card body */
  display: flex;
  flex-direction: column;
}

.rank-list {
  flex: 1;
  padding: 8px 16px 0;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 4px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  
  &:last-child {
    border-bottom: none;
  }
  
  .rank-number {
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    border-radius: 50%;
    margin-right: 12px;
    font-weight: bold;
    
    &.rank-1 {
      background-color: #FFD700;
      color: #fff;
    }
    
    &.rank-2 {
      background-color: #C0C0C0;
      color: #fff;
    }
    
    &.rank-3 {
      background-color: #CD7F32;
      color: #fff;
    }
  }
  
  .user-info {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .username {
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
    
    .stats {
      display: flex;
      align-items: center;
      gap: 4px;
      
      .rate-excellent {
        color: #67C23A;
        font-weight: bold;
      }
      
      .rate-good {
        color: #E6A23C;
        font-weight: bold;
      }
      
      .rate-normal {
        color: #909399;
        font-weight: bold;
      }
      
      .prediction-count {
        color: var(--el-text-color-secondary);
        font-size: 13px;
      }
    }
  }
}

.view-all {
  height: 32px;  /* 固定底部按钮区域高度 */
  padding: 4px 0;
  margin-top: auto;  /* 推到底部 */
  text-align: center;
  border-top: 1px solid var(--el-border-color-lighter);
  background-color: #fff;
}

.view-all .el-link {
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  height: 24px;
}

/* 暗黑模式适配 */
:global(html.dark) {
  .rank-number {
    &.rank-1 {
      background-color: rgba(255, 215, 0, 0.8) !important;
    }
    
    &.rank-2 {
      background-color: rgba(192, 192, 192, 0.8) !important;
    }
    
    &.rank-3 {
      background-color: rgba(205, 127, 50, 0.8) !important;
    }
  }
}
</style>
