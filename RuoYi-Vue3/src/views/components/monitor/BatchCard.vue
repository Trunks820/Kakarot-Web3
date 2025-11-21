<!-- 批次执行状态卡片 -->
<template>
  <div class="monitor-card">
    <div class="card-header">
      <div class="card-title">
        <span class="icon">⚡</span>
        <h3>批次执行</h3>
        <el-tag 
          v-if="isRealtime"
          size="small" 
          type="success"
          effect="plain"
        >
          <el-icon class="pulse"><VideoCamera /></el-icon>
          实时
        </el-tag>
      </div>
      <el-tooltip content="刷新数据（1.5秒内只能刷新一次）" placement="top">
        <el-button 
          size="small" 
          icon="Refresh" 
          :loading="loading"
          :disabled="refreshDisabled"
          circle
          @click="handleRefresh"
        />
      </el-tooltip>
    </div>
    
    <div class="card-body">
      <div class="dashboard-container">
        <!-- 左侧：圆形仪表盘 -->
        <div class="gauge-section">
          <div class="gauge-wrapper">
            <svg class="gauge-svg" viewBox="0 0 200 200">
              <!-- 背景圆 -->
              <circle cx="100" cy="100" r="90" class="gauge-bg"></circle>
              
              <!-- 健康度进度圆 -->
              <circle 
                cx="100" 
                cy="100" 
                r="90" 
                class="gauge-progress"
                :class="[healthClass, { 'no-running': stats.running === 0 }]"
                :style="{ strokeDashoffset: stats.running === 0 ? 565.49 : calculateDashOffset }"
              ></circle>
              
              <!-- 刻度线 -->
              <g class="gauge-ticks">
                <line x1="100" y1="15" x2="100" y2="25" stroke="currentColor" stroke-width="1"/>
                <line x1="100" y1="175" x2="100" y2="185" stroke="currentColor" stroke-width="1"/>
              </g>
            </svg>
            
            <!-- 中心显示 -->
            <div class="gauge-center">
              <div class="gauge-value" :class="healthClass">{{ healthPercent }}%</div>
              <div class="gauge-label">健康度</div>
            </div>
          </div>
        </div>

        <!-- 右侧：统计信息 -->
        <div class="stats-section">
          <!-- 运行批次 -->
          <div class="stat-card running-batch" :class="{ 'empty-state': stats.running === 0 }">
            <div v-if="stats.running > 0" class="stat-content">
              <div class="stat-icon">⚡</div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.running }}</div>
                <div class="stat-label">运行批次</div>
              </div>
            </div>
            <div v-else class="empty-content">
              <div class="empty-icon">✓</div>
              <div class="empty-text">暂无运行</div>
            </div>
          </div>

          <!-- 三个状态指标 -->
          <div class="status-indicators">
            <div class="indicator-item success">
              <div class="indicator-dot"></div>
              <span class="indicator-label">正常</span>
              <span class="indicator-value">{{ stats.heartbeatNormal || 0 }}</span>
            </div>
            <div class="indicator-item warning">
              <div class="indicator-dot"></div>
              <span class="indicator-label">暂停</span>
              <span class="indicator-value">{{ stats.paused || 0 }}</span>
            </div>
            <div class="indicator-item danger">
              <div class="indicator-dot"></div>
              <span class="indicator-label">超时</span>
              <span class="indicator-value">{{ stats.heartbeatTimeout || 0 }}</span>
            </div>
          </div>

          <!-- 更新时间 -->
          <div class="last-update">
            <span>最后更新</span>
            <span class="update-time">{{ formatTime(stats.lastUpdate) }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <div class="card-footer">
      <el-button 
        v-hasPermi="['crypto:monitor-v2:batch:list']"
        icon="DataAnalysis" 
        @click="openBatchListDialog"
      >
        批次列表
      </el-button>
      <el-button 
        v-hasPermi="['crypto:monitor-v2:batch:list']"
        icon="Monitor" 
        @click="openMonitorDialog"
      >
        运行监控
      </el-button>
    </div>

    <!-- 批次列表弹窗 -->
    <el-dialog
      v-model="batchListDialogVisible"
      title="批次列表 (点击行查看详情)"
      width="1100px"
      append-to-body
      destroy-on-close
      class="dialog-lg batch-list-dialog"
      aria-label="批次列表"
    >
      <el-table
        v-if="batchList.length > 0"
        v-loading="batchListLoading"
        :data="batchList"
        stripe
        style="width: 100%"
        max-height="500px"
        class="dialog-table"
      >
        <el-table-column label="批次ID" prop="id" width="80" align="center" />
        <el-table-column label="任务名称" prop="taskName" width="180" show-overflow-tooltip />
        <el-table-column label="批次编号" prop="batchNo" width="90" align="center" />
        <el-table-column label="链类型" prop="chainType" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.chainType === 'sol' ? 'success' : 'warning'" size="small">
              {{ scope.row.chainType?.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="项数" prop="itemCount" width="70" align="center">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.itemCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="scope">
            <el-tag 
              :type="getStatusType(scope.row.status)" 
              size="small"
            >
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="执行节点" prop="workerId" width="120" show-overflow-tooltip />
        <el-table-column label="心跳状态" width="100" align="center">
          <template #default="scope">
            <el-tag 
              :type="isHeartbeatNormal(scope.row) ? 'success' : 'danger'" 
              size="small"
              :effect="isHeartbeatNormal(scope.row) ? 'light' : 'dark'"
            >
              {{ isHeartbeatNormal(scope.row) ? '正常' : '超时' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="累计告警" prop="totalAlerts" width="90" align="center">
          <template #default="scope">
            <span :style="{ color: scope.row.totalAlerts > 0 ? '#E6A23C' : '#909399' }">
              {{ scope.row.totalAlerts || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="最后心跳" prop="lastHeartbeat" width="140" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-hasPermi="['crypto:monitor-v2:batch:query']"
              text 
              type="primary" 
              size="small" 
              @click="handleBatchDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:batch:restart']"
              text 
              type="warning" 
              size="small" 
              @click="handleBatchRestart(scope.row)"
              :disabled="scope.row.status === 'running'"
            >
              重启
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 空态提示 -->
      <div v-else class="dialog-empty">
        <div class="empty-icon">📦</div>
        <div class="empty-text">暂无批次</div>
        <div class="empty-action">
          <el-button type="primary" size="small" @click="batchListDialogVisible = false">
            返回
          </el-button>
        </div>
      </div>
      
      <template #footer class="dialog-footer">
        <el-button @click="batchListDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 批次详情弹窗 -->
    <el-dialog
      v-model="batchDetailDialogVisible"
      title="批次详情及心跳监控"
      width="900px"
      append-to-body
      destroy-on-close
      class="dialog-lg batch-detail-dialog"
      aria-label="批次详情"
    >
      <div v-if="batchDetail">
        <!-- 基本信息 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="批次ID">{{ batchDetail.id }}</el-descriptions-item>
          <el-descriptions-item label="批次编号">{{ batchDetail.batchNo }}</el-descriptions-item>
          <el-descriptions-item label="任务名称" :span="2">
            {{ batchDetail.taskName }}
          </el-descriptions-item>
          <el-descriptions-item label="链类型">
            <el-tag :type="batchDetail.chainType === 'sol' ? 'success' : 'warning'" size="small">
              {{ batchDetail.chainType?.toUpperCase() }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="项数">
            {{ batchDetail.itemCount || 0 }} 个
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(batchDetail.status)" size="small">
              {{ getStatusLabel(batchDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="心跳状态">
            <el-tag 
              :type="isHeartbeatNormal(batchDetail) ? 'success' : 'danger'" 
              size="small"
            >
              {{ isHeartbeatNormal(batchDetail) ? '正常' : '超时' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行节点">
            {{ batchDetail.workerId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="进程ID">
            {{ batchDetail.workerPid || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="累计告警">
            {{ batchDetail.totalAlerts || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="错误次数">
            <span :style="{ color: batchDetail.errorCount > 0 ? '#F56C6C' : '#909399' }">
              {{ batchDetail.errorCount || 0 }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="最后心跳">
            {{ batchDetail.lastHeartbeat || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="最后告警">
            {{ batchDetail.lastAlertTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ batchDetail.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="最后错误" :span="2" v-if="batchDetail.lastError">
            <el-text type="danger">{{ batchDetail.lastError }}</el-text>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 批次项列表 -->
        <el-divider content-position="left">批次项列表</el-divider>
        <el-table
          v-loading="batchItemsLoading"
          :data="batchItems"
          stripe
          style="width: 100%"
          max-height="300px"
        >
          <el-table-column label="序号" prop="itemOrder" width="80" />
          <el-table-column label="代币地址" prop="ca" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <el-link :href="`https://gmgn.ai/sol/token/${scope.row.ca}`" target="_blank" type="primary">
                {{ scope.row.ca }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="代币名称" prop="tokenName" width="150" show-overflow-tooltip />
          <el-table-column label="代币符号" prop="tokenSymbol" width="120" />
          <el-table-column label="市值" prop="marketCap" width="150">
            <template #default="scope">
              {{ scope.row.marketCap ? `$${formatNumber(scope.row.marketCap)}` : 'N/A' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <template #footer>
        <el-button @click="batchDetailDialogVisible = false">关闭</el-button>
        <el-button 
          v-hasPermi="['crypto:monitor-v2:batch:restart']"
          type="warning" 
          @click="handleBatchRestart(batchDetail)"
          :disabled="batchDetail?.status === 'running'"
        >
          重启批次
        </el-button>
      </template>
    </el-dialog>

    <!-- 运行监控弹窗（简化版） -->
    <el-dialog
      v-model="monitorDialogVisible"
      title="批次运行监控"
      width="900px"
      append-to-body
      destroy-on-close
      class="dialog-lg monitor-dialog"
    >
      <!-- 实时刷新指示 -->
      <div class="monitor-header">
        <el-alert
          title="实时监控"
          type="info"
          :closable="false"
          style="margin-bottom: 0;"
        >
          <template #default>
            <span>批次运行状态实时监控</span>
            <el-tag style="margin-left: 8px;" effect="light" size="small">
              <span class="refresh-pulse">●</span> 每 5 秒自动刷新
            </el-tag>
          </template>
        </el-alert>
      </div>

      <div class="monitor-dashboard">
        <el-row :gutter="16">
          <el-col :span="6">
            <div class="monitor-stat">
              <div class="stat-label">总批次</div>
              <div class="stat-value primary">{{ stats.total || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="monitor-stat">
              <div class="stat-label">运行中</div>
              <div class="stat-value success">{{ stats.running || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="monitor-stat">
              <div class="stat-label">暂停</div>
              <div class="stat-value warning">{{ stats.paused || 0 }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="monitor-stat">
              <div class="stat-label">异常</div>
              <div class="stat-value danger">{{ stats.error || 0 }}</div>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <el-row :gutter="16">
          <el-col :span="12">
            <div class="monitor-stat">
              <div class="stat-label">监控目标</div>
              <div class="stat-value">{{ stats.targetCount || 0 }} 个</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="monitor-stat">
              <div class="stat-label">心跳正常</div>
              <div class="stat-value success">
                {{ stats.heartbeatNormal || 0 }} / {{ stats.total || 0 }}
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 异常批次列表 -->
        <div v-if="stats.errorBatches && stats.errorBatches.length > 0">
          <el-divider content-position="left">异常批次</el-divider>
          <el-table :data="stats.errorBatches" stripe size="small">
            <el-table-column label="批次ID" prop="batchId" width="80" />
            <el-table-column label="任务名称" prop="taskName" min-width="150" />
            <el-table-column label="错误信息" prop="error" min-width="200" />
            <el-table-column label="最后心跳" prop="lastHeartbeat" width="160" />
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="monitorDialogVisible = false">关闭</el-button>
        <el-button type="primary" icon="Refresh" @click="$emit('refresh')">
          刷新数据
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, getCurrentInstance } from 'vue'
import { VideoCamera } from '@element-plus/icons-vue'
import { listBatch, getBatch, listBatchItems, restartBatch } from '@/api/crypto/monitor-v2'

const { proxy } = getCurrentInstance()

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      total: 0,
      running: 0,
      paused: 0,
      error: 0,
      pending: 0,
      targetCount: 0,
      heartbeatNormal: 0,
      heartbeatTimeout: 0,
      errorBatches: [],
      lastUpdate: null
    })
  },
  loading: Boolean
})

const emit = defineEmits(['refresh'])

// 防止高频刷新
const refreshDisabled = ref(false)
const handleRefresh = () => {
  if (refreshDisabled.value) return
  emit('refresh')
  refreshDisabled.value = true
  setTimeout(() => {
    refreshDisabled.value = false
  }, 1500)
}

// 弹窗状态
const batchListDialogVisible = ref(false)
const batchListLoading = ref(false)
const batchList = ref([])

const batchDetailDialogVisible = ref(false)
const batchDetail = ref(null)
const batchItemsLoading = ref(false)
const batchItems = ref([])

const monitorDialogVisible = ref(false)

// 是否实时
const isRealtime = computed(() => {
  if (!props.stats.lastUpdate) return false
  const now = new Date()
  const last = new Date(props.stats.lastUpdate)
  return (now - last) < 10000 // 10秒内
})

// 计算健康度 - 使用心跳状态的总数（正常 + 超时）
const healthPercent = computed(() => {
  const normal = props.stats.heartbeatNormal || 0
  const timeout = props.stats.heartbeatTimeout || 0
  const total = normal + timeout
  
  // 如果没有心跳数据，回退到使用 running
  if (total === 0) {
    const running = props.stats.running || 0
    if (running === 0) return 100
    return Math.round((normal / running) * 100)
  }
  
  return Math.round((normal / total) * 100)
})

// 健康度样式
const healthClass = computed(() => {
  const percent = healthPercent.value
  if (percent >= 90) return 'excellent'
  if (percent >= 70) return 'good'
  if (percent >= 50) return 'warning'
  return 'danger'
})

// 计算 SVG 圆形进度条的偏移值
const calculateDashOffset = computed(() => {
  const circumference = 2 * Math.PI * 90 // 半径 90
  const percent = healthPercent.value / 100
  return circumference * (1 - percent)
})

const formatTime = (time) => {
  if (!time) return '-'
  const now = new Date()
  const target = new Date(time)
  const diff = Math.floor((now - target) / 1000)
  
  if (diff < 10) return '刚刚'
  if (diff < 60) return `${diff}秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  return `${Math.floor(diff / 3600)}小时前`
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('en-US', {
    notation: 'compact',
    compactDisplay: 'short'
  }).format(num)
}

// 批次状态相关
const getStatusType = (status) => {
  const statusMap = {
    'running': 'success',
    'paused': 'warning',
    'pending': 'info',
    'stopped': 'info',
    'error': 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const labelMap = {
    'running': '运行中',
    'paused': '已暂停',
    'pending': '待运行',
    'stopped': '已停止',
    'error': '异常'
  }
  return labelMap[status] || status
}

const isHeartbeatNormal = (batch) => {
  if (!batch.lastHeartbeat) return false
  const now = new Date()
  const last = new Date(batch.lastHeartbeat)
  const diff = (now - last) / 1000
  return diff < 300 // 5分钟内
}

// 打开批次列表弹窗
const openBatchListDialog = async () => {
  console.log('打开批次列表弹窗')
  batchListDialogVisible.value = true
  batchListLoading.value = true
  try {
    const response = await listBatch({ pageNum: 1, pageSize: 100 })
    console.log('批次列表响应:', response)
    batchList.value = response.rows || []
    console.log('批次列表数据:', batchList.value)
  } catch (error) {
    console.error('加载批次列表失败:', error)
    proxy.$modal.msgError('加载批次列表失败: ' + (error.message || '未知错误'))
  } finally {
    batchListLoading.value = false
  }
}

// 打开运行监控弹窗
const openMonitorDialog = () => {
  console.log('打开运行监控弹窗')
  monitorDialogVisible.value = true
}

// 查看批次详情
const handleBatchDetail = async (row) => {
  console.log('查看批次详情:', row)
  batchDetail.value = row
  batchDetailDialogVisible.value = true
  
  // 加载批次项
  batchItemsLoading.value = true
  try {
    const response = await listBatchItems(row.id, { pageNum: 1, pageSize: 100 })
    batchItems.value = response.rows || []
  } catch (error) {
    console.error('加载批次项失败:', error)
    proxy.$modal.msgError('加载批次项失败')
  } finally {
    batchItemsLoading.value = false
  }
}

// 重启批次
const handleBatchRestart = (row) => {
  proxy.$modal.confirm(`确定要重启批次 ${row.batchNo} 吗？`).then(async () => {
    try {
      await restartBatch(row.id)
      proxy.$modal.msgSuccess('批次重启成功')
      
      // 刷新列表
      if (batchListDialogVisible.value) {
        openBatchListDialog()
      }
      
      // 刷新卡片统计
      emit('refresh')
    } catch (error) {
      console.error('重启批次失败:', error)
      proxy.$modal.msgError('重启批次失败')
    }
  })
}
</script>

<style scoped lang="scss">
.monitor-card {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.3s;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.monitor-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title .icon {
  font-size: 24px;
}

.card-title h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--el-text-color-primary);
}

.pulse {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.dashboard-container {
  width: 100%;
  display: flex;
  gap: 24px;
  align-items: center;
  justify-content: space-between;
}


.card-footer {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.card-footer .el-button {
  flex: 1;
}

/* 监控面板样式 */
.monitor-dashboard {
  padding: 16px 0;
}

.monitor-stat {
  text-align: center;
  padding: 20px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  transition: all 0.3s;
}

.monitor-stat:hover {
  background: var(--el-fill-color);
}

.monitor-stat .stat-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
}

.monitor-stat .stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.monitor-stat .stat-value.primary {
  color: #409EFF;
}

.monitor-stat .stat-value.success {
  color: #67C23A;
}

.monitor-stat .stat-value.warning {
  color: #E6A23C;
}

.monitor-stat .stat-value.danger {
  color: #F56C6C;
}

/* 仪表盘样式 */
.gauge-section {
  flex: 0 0 160px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.gauge-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
}

.gauge-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.1));
}

.gauge-bg {
  fill: none;
  stroke: var(--el-fill-color);
  stroke-width: 12;
}

.gauge-progress {
  fill: none;
  stroke-width: 12;
  stroke-linecap: round;
  stroke-dasharray: 565.48; /* 2*PI*90 */
  transform: rotate(-90deg);
  transform-origin: 100px 100px;
  transition: stroke-dashoffset 0.8s cubic-bezier(0.4, 0.0, 0.2, 1);
}

.gauge-progress.excellent {
  stroke: #67C23A;
  filter: drop-shadow(0 0 8px rgba(103, 194, 58, 0.4));
}

.gauge-progress.good {
  stroke: #409EFF;
  filter: drop-shadow(0 0 8px rgba(64, 158, 255, 0.4));
}

.gauge-progress.warning {
  stroke: #E6A23C;
  filter: drop-shadow(0 0 8px rgba(230, 162, 60, 0.4));
}

.gauge-progress.danger {
  stroke: #F56C6C;
  filter: drop-shadow(0 0 8px rgba(245, 108, 108, 0.4));
}

/* 无运行批次时的灰色虚线 */
.gauge-progress.no-running {
  stroke: #D3D3D3;
  stroke-dasharray: 5, 5;
  filter: drop-shadow(0 0 0px rgba(0, 0, 0, 0)) !important;
  opacity: 0.6;
}

.gauge-ticks {
  opacity: 0.3;
}

.gauge-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: var(--el-bg-color);
  border-radius: 50%;
  width: 100px;
  height: 100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.gauge-value {
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
  margin-bottom: 4px;
}

.gauge-value.excellent {
  color: #67C23A;
}

.gauge-value.good {
  color: #409EFF;
}

.gauge-value.warning {
  color: #E6A23C;
}

.gauge-value.danger {
  color: #F56C6C;
}

.gauge-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* 右侧统计区域 */
.stats-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  transition: all 0.3s;
}

.stat-card:hover {
  background: var(--el-fill-color);
  transform: translateX(2px);
}

.stat-card.running-batch {
  background: linear-gradient(135deg, rgba(230, 162, 60, 0.1), rgba(230, 162, 60, 0.05));
  border-left: 3px solid #E6A23C;
  
  &.empty-state {
    background: var(--el-fill-color-light);
    border-left-color: #D3D3D3;
  }
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.empty-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  
  .empty-icon {
    font-size: 20px;
    color: #67C23A;
  }
  
  .empty-text {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
}

.stat-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat-info .stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #E6A23C;
  line-height: 1;
}

.stat-info .stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 状态指示器 */
.status-indicators {
  display: flex;
  gap: 8px;
}

.indicator-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  font-size: 12px;
  transition: all 0.3s;
}

.indicator-item:hover {
  transform: translateY(-1px);
}

.indicator-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.indicator-item.success {
  background: rgba(103, 194, 58, 0.1);
  color: #67C23A;
}

.indicator-item.success .indicator-dot {
  background: #67C23A;
  box-shadow: 0 0 4px rgba(103, 194, 58, 0.6);
}

.indicator-item.warning {
  background: rgba(230, 162, 60, 0.1);
  color: #E6A23C;
}

.indicator-item.warning .indicator-dot {
  background: #E6A23C;
  box-shadow: 0 0 4px rgba(230, 162, 60, 0.6);
}

.indicator-item.danger {
  background: rgba(245, 108, 108, 0.1);
  color: #F56C6C;
}

.indicator-item.danger .indicator-dot {
  background: #F56C6C;
  box-shadow: 0 0 4px rgba(245, 108, 108, 0.6);
}

.indicator-label {
  flex: 1;
}

.indicator-value {
  font-weight: 700;
  font-size: 13px;
}

/* 更新时间 */
.last-update {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.update-time {
  font-family: monospace;
  color: var(--el-text-color-regular);
  font-weight: 500;
}

/* 卡片底部按钮区 */
.card-footer {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-light);
}

.card-footer .el-button {
  flex: 1;
  height: 32px;
}

/* BatchCard 弹窗样式 */
.batch-list-dialog,
.batch-detail-dialog,
.monitor-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.dialog-footer) {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    padding: 12px 20px;
    border-top: 1px solid var(--el-border-color-light);
    margin: 0 -20px -20px;
    background: var(--el-fill-color-light);
  }
}

.monitor-dialog {
  :deep(.el-dialog__body) {
    .monitor-header {
      margin-bottom: 16px;
      
      .refresh-pulse {
        display: inline-block;
        font-size: 8px;
        color: #F56C6C;
        animation: pulse 1s infinite;
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

/* 响应式布局 */
@media (max-width: 768px) {
  .dashboard-container {
    flex-direction: column;
    gap: 16px;
  }
  
  .gauge-section {
    flex: 0 0 120px;
  }
  
  .stats-section {
    width: 100%;
  }
}
</style>

