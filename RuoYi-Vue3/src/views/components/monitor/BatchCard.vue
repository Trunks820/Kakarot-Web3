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
      <el-button 
        size="small" 
        icon="Refresh" 
        :loading="loading"
        circle
        @click="$emit('refresh')"
      />
    </div>
    
    <div class="card-body">
      <!-- 批次数量显示 -->
      <div class="count-display">
        <div class="count-number">{{ stats.running || 0 }}</div>
        <div class="count-label">运行批次</div>
      </div>
      
      <!-- 健康度显示 -->
      <div class="health-indicator">
        <div class="health-bar-container">
          <div 
            class="health-bar" 
            :style="{ width: healthPercent + '%' }"
            :class="healthClass"
          ></div>
        </div>
        <div class="health-text">
          <span>健康度</span>
          <span class="health-value" :class="healthClass">{{ healthPercent }}%</span>
        </div>
      </div>
      
      <!-- 状态统计 -->
      <div class="stats-grid">
        <div class="stat-item success">
          <div class="stat-value">{{ stats.heartbeatNormal || 0 }}</div>
          <div class="stat-label">心跳正常</div>
        </div>
        <div class="stat-item warning">
          <div class="stat-value">{{ stats.paused || 0 }}</div>
          <div class="stat-label">已暂停</div>
        </div>
        <div class="stat-item danger">
          <div class="stat-value">{{ stats.heartbeatTimeout || 0 }}</div>
          <div class="stat-label">心跳超时</div>
        </div>
      </div>
      
      <!-- 最后更新 -->
      <div class="last-update">
        最后更新：{{ formatTime(stats.lastUpdate) }}
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
      title="批次列表"
      width="1100px"
      append-to-body
    >
      <el-table
        v-loading="batchListLoading"
        :data="batchList"
        stripe
        style="width: 100%"
        max-height="500px"
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
      
      <template #footer>
        <el-button @click="batchListDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 批次详情弹窗 -->
    <el-dialog
      v-model="batchDetailDialogVisible"
      title="批次详情"
      width="900px"
      append-to-body
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
    >
      <el-alert
        title="实时监控"
        type="info"
        :closable="false"
        style="margin-bottom: 16px;"
      >
        批次运行状态实时监控，每5秒自动刷新
      </el-alert>

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

// 计算健康度 - 修正逻辑：使用 running 和 heartbeatNormal
const healthPercent = computed(() => {
  const total = props.stats.running || 0
  if (total === 0) return 100
  const normal = props.stats.heartbeatNormal || 0
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

.count-display {
  text-align: center;
  margin-bottom: 20px;
}

.count-number {
  font-size: 48px;
  font-weight: 700;
  color: #E6A23C;
  line-height: 1;
}

.count-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}

/* 健康度指示器 */
.health-indicator {
  width: 100%;
  margin-bottom: 20px;
}

.health-bar-container {
  height: 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 8px;
}

.health-bar {
  height: 100%;
  border-radius: 6px;
  transition: all 0.5s ease;
}

.health-bar.excellent {
  background: linear-gradient(90deg, #67C23A, #85CE61);
}

.health-bar.good {
  background: linear-gradient(90deg, #409EFF, #66B1FF);
}

.health-bar.warning {
  background: linear-gradient(90deg, #E6A23C, #EBB563);
}

.health-bar.danger {
  background: linear-gradient(90deg, #F56C6C, #F78989);
}

.health-text {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.health-value {
  font-weight: 600;
  font-size: 16px;
}

.health-value.excellent {
  color: #67C23A;
}

.health-value.good {
  color: #409EFF;
}

.health-value.warning {
  color: #E6A23C;
}

.health-value.danger {
  color: #F56C6C;
}

.stats-grid {
  width: 100%;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  border-radius: 6px;
  transition: all 0.3s;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.12);
}

.stat-item.success {
  background: var(--el-color-success-light-9);
  border: 1px solid var(--el-color-success-light-7);
}

.stat-item.warning {
  background: var(--el-color-warning-light-9);
  border: 1px solid var(--el-color-warning-light-7);
}

.stat-item.danger {
  background: var(--el-color-danger-light-9);
  border: 1px solid var(--el-color-danger-light-7);
}

.stat-item .stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-item.success .stat-value {
  color: #67C23A;
}

.stat-item.warning .stat-value {
  color: #E6A23C;
}

.stat-item.danger .stat-value {
  color: #F56C6C;
}

.stat-item .stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.last-update {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
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
</style>

