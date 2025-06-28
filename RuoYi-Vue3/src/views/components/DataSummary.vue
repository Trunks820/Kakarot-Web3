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
      <el-card class="data-card" v-loading="botLoading" element-loading-background="rgba(0, 0, 0, 0.1)" element-loading-text="加载中...">
        <template #header>
          <div class="card-header">
            <span>机器人状态</span>
            <div>
              <el-button 
                type="text" 
                @click="fetchBotStatus()" 
                :loading="botLoading"
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
            <div class="col-actions">操作</div>
          </div>
          <div class="bot-table-row">
            <div class="col-bot">
              <div class="bot-info">
                <span class="bot-name">Telegram</span>
                <span class="bot-pid">PID: {{ tgBotStatus.pid || '-' }}</span>
              </div>
            </div>
            <div class="col-status">
              <el-tag :type="getBotStatusType(tgBotStatus.status)" size="small">
                {{ getBotStatusText(tgBotStatus.status) }}
              </el-tag>
            </div>
            <div class="col-uptime">{{ currentUptime.tg }}</div>
            <div class="col-actions">
              <el-button 
                type="primary"
                size="small"
                @click="showBotDetails"
              >
                <el-icon><InfoFilled /></el-icon>
                详情
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>

  <!-- 机器人详情弹窗 -->
  <el-dialog 
    v-model="botDetailVisible" 
    title="机器人详情" 
    width="70%" 
    class="bot-detail-dialog"
    :close-on-click-modal="false"
  >
    <el-tabs v-model="currentTab" class="bot-detail-tabs">
      <!-- 基础信息 -->
      <el-tab-pane label="基础信息" name="info">
        <div class="bot-info-panel">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="机器人类型">
              <el-tag type="primary">Telegram Bot</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="运行状态">
              <el-tag :type="getBotStatusType(tgBotStatus.status)">
                {{ getBotStatusText(tgBotStatus.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="进程ID">
              <el-text type="info" style="font-family: monospace;">
                {{ tgBotStatus.pid || '未知' }}
              </el-text>
            </el-descriptions-item>
            <el-descriptions-item label="启动时间">
              {{ tgBotStatus.start_time || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="运行时长">
              <el-text type="success">{{ currentUptime.tg || '未知' }}</el-text>
            </el-descriptions-item>
            <el-descriptions-item label="重启次数">
              <el-text :type="tgBotStatus.restart_count > 3 ? 'warning' : 'info'">
                {{ tgBotStatus.restart_count || 0 }} 次
              </el-text>
            </el-descriptions-item>
            <el-descriptions-item label="最后检查">
              {{ tgBotStatus.last_check_time || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="操作">
              <div class="dialog-actions">
                <el-button 
                  :type="getStartStopButtonType(tgBotStatus.status)"
                  size="small"
                  :loading="tgActionLoading"
                  @click="handleTgBotAction(getStartStopAction(tgBotStatus.status))"
                  :disabled="isActionDisabled(tgBotStatus.status)"
                >
                  <el-icon><component :is="getStartStopIcon(tgBotStatus.status)" /></el-icon>
                  {{ getStartStopText(tgBotStatus.status) }}
                </el-button>
                <el-button 
                  type="warning"
                  size="small"
                  :loading="tgActionLoading"
                  @click="handleTgBotAction('restart')"
                  :disabled="isRestartDisabled(tgBotStatus.status)"
                >
                  <el-icon><RefreshRight /></el-icon>
                  重启
                </el-button>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-tab-pane>

      <!-- 历史消息 -->
      <el-tab-pane label="历史消息" name="messages">
        <div class="message-history-panel">
          <div class="message-header">
            <div class="message-stats">
              <el-statistic title="总消息数" :value="messageHistory.length" />
            </div>
            <el-button 
              type="primary" 
              size="small"
              :loading="messagesLoading"
              @click="fetchMessageHistory"
            >
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          
          <div class="message-list" v-loading="messagesLoading">
            <el-empty v-if="!messagesLoading && messageHistory.length === 0" description="暂无历史消息" />
            <div 
              v-else
              v-for="(message, index) in messageHistory" 
              :key="index"
              class="message-item"
            >
              <div class="message-header-info">
                <div class="message-type">
                  <el-tag 
                    :type="getMessageTypeTag(message.type).type" 
                    size="small"
                  >
                    {{ getMessageTypeTag(message.type).text }}
                  </el-tag>
                </div>
                <div class="message-time">
                  {{ formatMessageTime(message.timestamp) }}
                </div>
              </div>
              <div class="message-content">
                <div class="message-details">
                  <p v-if="message.wallet_address" class="message-wallet">
                    <strong>钱包地址：</strong>
                    <el-text type="info" style="font-family: monospace; font-size: 12px;">
                      {{ message.wallet_address }}
                    </el-text>
                  </p>
                  <p v-if="message.token_address" class="message-token">
                    <strong>代币地址：</strong>
                    <el-text type="primary" style="font-family: monospace; font-size: 12px;">
                      {{ message.token_address }}
                    </el-text>
                  </p>
                  <p v-if="message.amount" class="message-amount">
                    <strong>金额：</strong>{{ message.amount }}
                  </p>
                  <p v-if="message.message" class="message-text">
                    <strong>消息：</strong>{{ message.message }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Search, User, Refresh, RefreshRight, ArrowRight, VideoPlay, VideoPause, InfoFilled } from '@element-plus/icons-vue'
import useSettingsStore from '@/store/modules/settings'
import { ElMessage } from 'element-plus'
import { getDailyActivityStats, getTgBotStatus, getUserRange, restartTgBot, startTgBot, stopTgBot, getMessages } from '@/api/crypto/caRecord'

const settingsStore = useSettingsStore()

// 为每个卡片添加独立的loading状态
const platformLoading = ref(false)
const botLoading = ref(false)

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

// 弹窗相关状态
const botDetailVisible = ref(false)
const messagesLoading = ref(false)
const messageHistory = ref([])
const currentTab = ref('info')

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
  tg: '-'
})

// 更新运行时长显示
const updateUptime = () => {
  currentUptime.value.tg = formatUptime(tgBotStatus.value.start_time)
}

// 处理TG机器人操作
const handleTgBotAction = async (action) => {
  // 检查是否有PID
  if (!tgBotStatus.value.pid || tgBotStatus.value.pid === '-') {
    ElMessage.error('无法获取机器人PID，请先刷新状态')
    return
  }

  tgActionLoading.value = true
  try {
    let response
    const pid = tgBotStatus.value.pid
    
    switch (action) {
      case 'restart':
        response = await restartTgBot(pid)
        break
      case 'start':
        response = await startTgBot(pid)
        break
      case 'stop':
        response = await stopTgBot(pid)
        break
      default:
        throw new Error('不支持的操作类型')
    }
    
    if (response.success || response.code === 200) {
      ElMessage.success(response.message || response.msg || `TG机器人${getActionText(action)}成功`)
      // 重新获取机器人状态
      await fetchBotStatus()
    } else {
      throw new Error(response.message || response.msg || `TG机器人${getActionText(action)}失败`)
    }
  } catch (error) {
    console.error('TG机器人操作失败:', error)
    ElMessage.error(error.message || `TG机器人${getActionText(action)}失败`)
  } finally {
    tgActionLoading.value = false
  }
}

// 获取操作文本
const getActionText = (action) => {
  switch (action) {
    case 'restart': return '重启'
    case 'start': return '启动'
    case 'stop': return '停止'
    default: return '操作'
  }
}

// 根据机器人状态获取启动/停止按钮的类型
const getStartStopButtonType = (status) => {
  switch (status?.toLowerCase()) {
    case 'running':
      return 'danger'  // 运行中显示停止按钮（红色）
    case 'stopped':
    case 'error':
      return 'success' // 停止状态显示启动按钮（绿色）
    default:
      return 'primary'
  }
}

// 根据机器人状态获取启动/停止按钮的操作
const getStartStopAction = (status) => {
  switch (status?.toLowerCase()) {
    case 'running':
      return 'stop'    // 运行中显示停止操作
    case 'stopped':
    case 'error':
      return 'start'   // 停止状态显示启动操作
    default:
      return 'start'
  }
}

// 根据机器人状态获取启动/停止按钮的文本
const getStartStopText = (status) => {
  switch (status?.toLowerCase()) {
    case 'running':
      return '停止'
    case 'stopped':
    case 'error':
      return '启动'
    default:
      return '启动'
  }
}

// 根据机器人状态获取启动/停止按钮的图标
const getStartStopIcon = (status) => {
  switch (status?.toLowerCase()) {
    case 'running':
      return VideoPause  // 运行中显示暂停图标
    case 'stopped':
    case 'error':
      return VideoPlay   // 停止状态显示播放图标
    default:
      return VideoPlay
  }
}

// 判断启动/停止按钮是否禁用
const isActionDisabled = (status) => {
  // 在启动中、重启中等过渡状态时禁用按钮
  return ['starting', 'stopping', 'restarting'].includes(status?.toLowerCase())
}

// 判断重启按钮是否禁用
const isRestartDisabled = (status) => {
  // 在任何过渡状态时禁用重启按钮
  return ['starting', 'stopping', 'restarting'].includes(status?.toLowerCase())
}

// 显示机器人详情
const showBotDetails = () => {
  botDetailVisible.value = true
  currentTab.value = 'info'
  // 打开弹窗时自动加载历史消息
  fetchMessageHistory()
}

// 获取历史消息
const fetchMessageHistory = async () => {
  messagesLoading.value = true
  try {
    const res = await getMessages({
      limit: 100,
      offset: 0
    })
    
    if (res.code === 200 && res.data) {
      messageHistory.value = res.data.transactions || []
    } else {
      ElMessage.warning('获取历史消息失败: ' + (res.msg || '未知错误'))
      messageHistory.value = []
    }
  } catch (error) {
    console.error('获取历史消息失败:', error)
    ElMessage.error('获取历史消息异常，请检查网络连接')
    messageHistory.value = []
  } finally {
    messagesLoading.value = false
  }
}

// 格式化消息时间
const formatMessageTime = (timestamp) => {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  if (isNaN(date.getTime())) return '-'
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 获取消息类型标签
const getMessageTypeTag = (type) => {
  switch (type) {
    case 'buy': return { type: 'success', text: '买入' }
    case 'sell': return { type: 'danger', text: '卖出' }
    case 'query': return { type: 'info', text: '查询' }
    default: return { type: 'warning', text: '其他' }
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
  // 每60秒更新一次状态
  statusTimer = setInterval(() => {
    fetchBotStatus()
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
  width: 150px;
  flex-shrink: 0;
  padding-left: 16px;
}

.col-status {
  width: 100px;
  flex-shrink: 0;
  text-align: center;
}

.col-uptime {
  width: 120px;
  flex-shrink: 0;
  text-align: center;
}

.col-actions {
  width: 80px;
  flex-shrink: 0;
  text-align: center;
  padding-right: 16px;
  
  .el-button {
    margin: 0;
    font-size: 12px;
    
    .el-icon {
      font-size: 12px;
      margin-right: 3px;
    }
  }
}

/* 新增的机器人信息样式 */
.bot-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  
  .bot-name {
    font-weight: 600;
    color: var(--el-text-color-primary);
    font-size: 14px;
  }
  
  .bot-pid {
    font-size: 11px;
    color: var(--el-text-color-secondary);
    font-family: 'Consolas', 'Monaco', monospace;
  }
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

/* 弹窗样式 */
.bot-detail-dialog {
  .el-dialog__body {
    padding: 10px 20px 20px;
  }
}

.bot-detail-tabs {
  .el-tabs__content {
    padding-top: 15px;
  }
}

.bot-info-panel {
  .el-descriptions {
    .el-descriptions__body {
      .el-descriptions__table {
        .el-descriptions__cell {
          padding: 12px 16px;
        }
      }
    }
  }
  
  .dialog-actions {
    display: flex;
    gap: 8px;
    
    .el-button {
      margin: 0;
    }
  }
}

.message-history-panel {
  .message-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    padding: 12px 16px;
    background-color: var(--el-fill-color-light);
    border-radius: 6px;
    
    .message-stats {
      .el-statistic {
        .el-statistic__content {
          font-size: 16px;
          font-weight: 600;
        }
      }
    }
  }
  
  .message-list {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    
    .message-item {
      padding: 12px 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      transition: background-color 0.2s;
      
      &:hover {
        background-color: var(--el-fill-color-light);
      }
      
      &:last-child {
        border-bottom: none;
      }
      
      .message-header-info {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .message-time {
          font-size: 12px;
          color: var(--el-text-color-secondary);
          font-family: monospace;
        }
      }
      
      .message-content {
        .message-details {
          p {
            margin: 4px 0;
            font-size: 13px;
            line-height: 1.4;
            
            strong {
              color: var(--el-text-color-primary);
              margin-right: 8px;
            }
          }
          
          .message-wallet, .message-token {
            .el-text {
              word-break: break-all;
            }
          }
          
          .message-text {
            background-color: var(--el-fill-color-lighter);
            padding: 8px 12px;
            border-radius: 4px;
            border-left: 3px solid var(--el-color-primary);
          }
        }
      }
    }
  }
}

/* 滚动条样式 */
.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
  
  &:hover {
    background: var(--el-border-color-dark);
  }
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
  
  /* 机器人信息暗黑模式适配 */
  .bot-info {
    .bot-name {
      color: #ffffff !important;
    }
    
    .bot-pid {
      color: #a8a8a8 !important;
    }
  }
  
  /* 表格头部暗黑模式 */
  .bot-table-header {
    background-color: #141414 !important;
    border-color: #434343 !important;
    color: #ffffff !important;
  }
  
  /* 表格行暗黑模式 */
  .bot-table-row {
    border-color: #434343 !important;
    
    &:hover {
      background-color: rgba(255, 255, 255, 0.05) !important;
    }
  }
  
  /* 弹窗暗黑模式 */
  .bot-detail-dialog {
    .el-dialog {
      background-color: #1d1e1f !important;
      
      .el-dialog__header {
        background-color: #1d1e1f !important;
        border-color: #434343 !important;
      }
      
      .el-dialog__body {
        background-color: #1d1e1f !important;
      }
    }
  }
  
  .message-history-panel {
    .message-header {
      background-color: #141414 !important;
      border-color: #434343 !important;
    }
    
    .message-list {
      border-color: #434343 !important;
      background-color: #1d1e1f !important;
      
      .message-item {
        border-color: #434343 !important;
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.05) !important;
        }
        
        .message-content .message-details .message-text {
          background-color: #141414 !important;
          border-left-color: #409EFF !important;
        }
      }
    }
  }
}
</style>
