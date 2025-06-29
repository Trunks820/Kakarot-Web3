<template>
  <el-row :gutter="20" class="data-summary-row">
    <!-- 预警统计 -->
    <el-col :span="5" :xs="24" class="data-col">
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>预警统计</span>
            <el-tag type="danger" size="small">实时</el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Warning /></el-icon>
              <span>今日预警</span>
            </div>
            <h3 class="danger">{{ alertStats.todayAlerts }}</h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Clock /></el-icon>
              <span>待处理</span>
            </div>
            <h3 class="warning">{{ alertStats.pendingAlerts }}</h3>
          </div>
        </div>
      </el-card>
    </el-col>

    <!-- 监控代币 -->
    <el-col :span="5" :xs="24" class="data-col">
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>监控代币</span>
            <el-tag type="success" size="small">实时</el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Monitor /></el-icon>
              <span>总监控</span>
            </div>
            <h3>{{ monitorStats.totalTokens }}</h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><TrendCharts /></el-icon>
              <span>活跃中</span>
            </div>
            <h3 class="success">{{ monitorStats.activeTokens }}</h3>
          </div>
        </div>
      </el-card>
    </el-col>

    <!-- OKX信号 -->
    <el-col :span="5" :xs="24" class="data-col">
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>OKX信号</span>
            <el-tag type="primary" size="small">实时</el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><DataLine /></el-icon>
              <span>今日信号</span>
            </div>
            <h3>{{ okxStats.todaySignals }}</h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Timer /></el-icon>
              <span>最新信号</span>
            </div>
            <h3 class="primary">{{ okxStats.latestTime }}分钟前</h3>
          </div>
        </div>
      </el-card>
    </el-col>

    <!-- 机器人状态 -->
    <el-col :span="5" :xs="24" class="data-col">
      <el-card class="data-card clickable" @click="showBotDetails">
        <template #header>
          <div class="card-header">
            <div class="header-left">
              <span>机器人状态</span>
              <el-button type="primary" size="small" @click.stop="showBotDetails" class="header-action-btn">
                详情
              </el-button>
            </div>
            <el-tag :type="botStats.isRunning ? 'success' : 'danger'" size="small">
              {{ botStats.isRunning ? '在线' : '离线' }}
            </el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Service /></el-icon>
              <span>TG机器人</span>
            </div>
            <h3 :class="botStats.isRunning ? 'success' : 'danger'">
              {{ botStats.isRunning ? '运行中' : '已停止' }}
            </h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Timer /></el-icon>
              <span>运行时长</span>
            </div>
            <h3>{{ botStats.uptime }}</h3>
          </div>
        </div>
      </el-card>
    </el-col>

    <!-- 系统状态 -->
    <el-col :span="4" :xs="24" class="data-col">
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>系统状态</span>
            <el-tag type="info" size="small">监控</el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Cpu /></el-icon>
              <span>CPU使用</span>
            </div>
            <h3>{{ systemStats.cpuUsage }}%</h3>
          </div>
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Operation /></el-icon>
              <span>内存使用</span>
            </div>
            <h3>{{ systemStats.memoryUsage }}%</h3>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>

  <!-- 机器人详情弹窗 -->
  <el-dialog 
    v-model="botDialogVisible" 
    title="机器人详情" 
    width="600px"
    :close-on-click-modal="false"
  >
    <div class="bot-details">
      <div class="bot-info">
        <h4>基础信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="状态">
            <el-tag :type="getBotStatusType(botDetails.status)">
              {{ getBotStatusText(botDetails.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="机器人名称">{{ botDetails.name || '-' }}</el-descriptions-item>
          <el-descriptions-item label="运行时长">{{ botDetails.uptime }}</el-descriptions-item>
          <el-descriptions-item label="启动时间">{{ botDetails.startTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="重启次数">{{ botDetails.restartCount || 0 }} 次</el-descriptions-item>
          <el-descriptions-item label="最后检查">{{ botDetails.lastCheck }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="bot-actions" style="margin-top: 20px;">
        <h4>操作控制</h4>
        <div class="action-buttons">
          <el-button 
            v-if="!botDetails.isRunning"
            type="success" 
            @click="startBot"
            :loading="actionLoading"
            icon="VideoPlay"
          >
            启动机器人
          </el-button>
          <el-button 
            v-if="botDetails.isRunning"
            type="warning" 
            @click="restartBot"
            :loading="actionLoading"
            icon="RefreshRight"
          >
            重启机器人
          </el-button>
          <el-button 
            v-if="botDetails.isRunning"
            type="danger" 
            @click="stopBot"
            :loading="actionLoading"
            icon="VideoPause"
          >
            停止机器人
          </el-button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup name="EnhancedDataSummary">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Warning, Clock, Monitor, TrendCharts, 
  DataLine, Timer, Service, Cpu, Operation,
  VideoPlay, VideoPause, RefreshRight
} from '@element-plus/icons-vue'
import { getTgBotStatus, restartTgBot, startTgBot, stopTgBot } from '@/api/crypto/caRecord'

// 模拟数据
const alertStats = ref({
  todayAlerts: 23,
  pendingAlerts: 5
})

const monitorStats = ref({
  totalTokens: 156,
  activeTokens: 34
})

const okxStats = ref({
  todaySignals: 89,
  latestTime: 3
})

const botStats = ref({
  isRunning: false,
  uptime: '-'
})

const systemStats = ref({
  cpuUsage: 45,
  memoryUsage: 67
})

// 机器人详情弹窗
const botDialogVisible = ref(false)
const botDetails = ref({
  isRunning: false,
  pid: null,
  name: null,
  uptime: '-',
  lastCheck: '-',
  status: 'unknown',
  startTime: '-',
  restartCount: 0
})
const actionLoading = ref(false)

// 获取机器人状态
const getBotStatus = async () => {
  try {
    const response = await getTgBotStatus()
    console.log('机器人状态API响应:', response)
    
    if (response.code === 200 && response.data) {
      const data = response.data
      const isRunning = data.status?.toLowerCase() === 'running'
      const uptime = calculateUptime(data.start_time)

      botStats.value.isRunning = isRunning
      botStats.value.uptime = uptime || '-'
      
      // 更新详情数据
      botDetails.value = {
        isRunning: isRunning,
        pid: data.pid,
        name: data.name,
        uptime: uptime || '-',
        lastCheck: new Date().toLocaleString(),
        status: data.status,
        startTime: data.start_time,
        restartCount: data.restart_count || 0
      }
    }
  } catch (error) {
    // 使用模拟数据作为后备
    botStats.value.isRunning = false
    botStats.value.uptime = '-'
  }
}

// 计算运行时长
const calculateUptime = (startTime) => {
  if (!startTime) return '-'
  
  try {
    // 处理不同的日期格式
    let start
    if (typeof startTime === 'string') {
      // 将 "2025-06-29 11:55:58" 格式转换为 "2025-06-29T11:55:58"
      const isoTime = startTime.replace(' ', 'T')
      start = new Date(isoTime)
    } else {
      start = new Date(startTime)
    }
    
    // 检查日期是否有效
    if (isNaN(start.getTime())) {
      console.warn('无效的开始时间格式:', startTime)
      return '-'
    }
    
    const now = new Date()
    const diff = now - start
    
    if (diff < 0) return '-'
    
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    
    if (days > 0) {
      return `${days}天${hours}小时${minutes}分钟`
    } else if (hours > 0) {
      return `${hours}小时${minutes}分钟`
    } else if (minutes > 0) {
      return `${minutes}分钟`
    } else {
      return '不到1分钟'
    }
  } catch (error) {
    console.error('计算运行时长失败:', error)
    return '-'
  }
}

// 显示机器人详情
const showBotDetails = async () => {
  await getBotStatus() // 获取最新状态
  botDialogVisible.value = true
}

// 启动机器人
const startBot = async () => {
  if (!botDetails.value.name) {
    ElMessage.error('机器人名称不存在，无法启动')
    return
  }
  
  actionLoading.value = true
  try {
    const response = await startTgBot(botDetails.value.name)
    
    if (response.code === 200) {
      ElMessage.success('机器人启动成功')
      await getBotStatus() // 刷新状态
    } else {
      ElMessage.error(response.msg || '启动失败')
    }
  } catch (error) {
    ElMessage.error('启动失败：' + error.message)
  } finally {
    actionLoading.value = false
  }
}

// 重启机器人
const restartBot = async () => {
  console.log('重启机器人 - botDetails.value:', botDetails.value)
  console.log('重启机器人 - name值:', botDetails.value.name)
  
  if (!botDetails.value.name) {
    ElMessage.error('机器人名称不存在，无法重启')
    return
  }
  
  actionLoading.value = true
  try {
    console.log('调用重启API，传递name:', botDetails.value.name)
    const response = await restartTgBot(botDetails.value.name)
    
    if (response.code === 200) {
      ElMessage.success('机器人重启成功')
      await getBotStatus() // 刷新状态
    } else {
      ElMessage.error(response.msg || '重启失败')
    }
  } catch (error) {
    ElMessage.error('重启失败：' + error.message)
  } finally {
    actionLoading.value = false
  }
}

// 停止机器人
const stopBot = async () => {
  if (!botDetails.value.name) {
    ElMessage.error('机器人名称不存在，无法停止')
    return
  }
  
  actionLoading.value = true
  try {
    const response = await stopTgBot(botDetails.value.name)
    
    if (response.code === 200) {
      ElMessage.success('机器人停止成功')
      await getBotStatus() // 刷新状态
    } else {
      ElMessage.error(response.msg || '停止失败')
    }
  } catch (error) {
    ElMessage.error('停止失败：' + error.message)
  } finally {
    actionLoading.value = false
  }
}

// 状态判断方法
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

// 初始化和定时更新
onMounted(async () => {
  // 获取初始机器人状态
  await getBotStatus()
  
  // 模拟实时数据更新
  setInterval(() => {
    // 随机更新一些数据，模拟实时变化
    if (Math.random() > 0.8) {
      alertStats.value.todayAlerts += Math.floor(Math.random() * 2)
    }
    if (Math.random() > 0.9) {
      okxStats.value.latestTime = Math.floor(Math.random() * 10) + 1
    }
    systemStats.value.cpuUsage = 40 + Math.floor(Math.random() * 20)
    systemStats.value.memoryUsage = 60 + Math.floor(Math.random() * 15)
  }, 5000)
  
  // 定时获取机器人状态
  setInterval(async () => {
    await getBotStatus()
  }, 30000) // 每30秒更新一次机器人状态
})
</script>

<style scoped lang="scss">
.data-card {
  height: 140px;
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &.clickable {
    cursor: pointer;
    
    &:hover {
      transform: translateY(-2px);
    }
  }

  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    span {
      font-size: 14px;
      color: var(--el-text-color-primary);
      font-weight: 600;
    }

    .header-action-btn {
      font-size: 10px;
      padding: 2px 6px;
      height: 20px;
      border-radius: 10px;
    }

    .el-tag {
      font-size: 11px;
      height: 18px;
      line-height: 18px;
    }
  }

  .card-body {
    padding: 12px 16px;
    height: calc(100% - 50px);

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
        gap: 6px;
        
        .el-icon {
          font-size: 14px;
          color: var(--el-text-color-secondary);
        }

        span {
          color: var(--el-text-color-regular);
          font-size: 12px;
        }
      }

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--el-text-color-primary);

        &.success {
          color: var(--el-color-success);
        }

        &.warning {
          color: var(--el-color-warning);
        }

        &.danger {
          color: var(--el-color-danger);
        }

        &.primary {
          color: var(--el-color-primary);
        }
      }
    }
  }
}

/* 机器人详情弹窗样式 */
.bot-details {
  h4 {
    margin: 0 0 12px 0;
    color: var(--el-text-color-primary);
    font-size: 16px;
    font-weight: 600;
  }

  .action-buttons {
    display: flex;
    gap: 12px;
    
    .el-button {
      flex: 1;
    }
  }
}

/* 响应式设计 - 只在移动端生效 */
@media (max-width: 1200px) {
  .data-card {
    height: 130px;
    
    .card-body {
      .stat-item {
        margin-bottom: 8px;
        
        h3 {
          font-size: 14px;
        }
      }
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .data-summary-row {
    .data-col {
      margin-bottom: 10px;
    }
  }
  
  .data-card {
    height: auto;
    min-height: 100px;
    
    :deep(.el-card__header) {
      padding: 10px 12px;
    }
    
    .card-header {
      span {
        font-size: 13px;
      }
      
      .header-left {
        gap: 6px;
        
        .header-action-btn {
          font-size: 9px;
          padding: 1px 4px;
          height: 16px;
        }
      }
      
      .el-tag {
        font-size: 9px;
        height: 14px;
        line-height: 14px;
        padding: 0 4px;
      }
    }
    
    .card-body {
      padding: 8px 12px;
      height: auto;
      
      .stat-item {
        margin-bottom: 6px;
        
        .stat-label {
          gap: 4px;
          
          .el-icon {
            font-size: 12px;
          }
          
          span {
            font-size: 10px;
          }
        }
        
        h3 {
          font-size: 12px;
        }
      }
    }
  }
}

/* 小屏幕设备优化 */
@media (max-width: 480px) {
  .data-summary-row {
    :deep(.el-row) {
      --el-row-gutter: 8px;
    }
    
    .data-col {
      margin-bottom: 8px;
    }
  }
  
  .data-card {
    .card-header {
      span {
        font-size: 12px;
      }
      
      .el-tag {
        font-size: 8px;
        height: 12px;
        line-height: 12px;
        padding: 0 3px;
      }
    }
    
    .card-body {
      padding: 6px 10px;
      
      .stat-item {
        margin-bottom: 4px;
        
        .stat-label {
          .el-icon {
            font-size: 10px;
          }
          
          span {
            font-size: 9px;
          }
        }
        
        h3 {
          font-size: 11px;
        }
      }
    }
  }
}

/* 暗黑模式适配 */
:global(html.dark) {
  
}
</style> 