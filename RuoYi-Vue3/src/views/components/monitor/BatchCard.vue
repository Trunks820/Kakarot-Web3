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
        <div class="count-number">{{ stats.active || 0 }}</div>
        <div class="count-label">活跃批次</div>
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
          <div class="stat-value">{{ stats.normal || 0 }}</div>
          <div class="stat-label">正常</div>
        </div>
        <div class="stat-item warning">
          <div class="stat-value">{{ stats.delayed || 0 }}</div>
          <div class="stat-label">延迟</div>
        </div>
        <div class="stat-item danger">
          <div class="stat-value">{{ stats.timeout || 0 }}</div>
          <div class="stat-label">超时</div>
        </div>
      </div>
      
      <!-- 最后更新 -->
      <div class="last-update">
        最后更新：{{ formatTime(stats.lastUpdate) }}
      </div>
    </div>
    
    <div class="card-footer">
      <el-button icon="DataAnalysis" @click="handleViewDetail">
        批次详情
      </el-button>
      <el-button icon="Monitor" @click="handleViewMonitor">
        实时监控
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { VideoCamera } from '@element-plus/icons-vue'

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      active: 0,
      normal: 0,
      delayed: 0,
      timeout: 0,
      lastUpdate: null
    })
  },
  loading: Boolean
})

const emit = defineEmits(['refresh'])
const router = useRouter()

// 是否实时
const isRealtime = computed(() => {
  if (!props.stats.lastUpdate) return false
  const now = new Date()
  const last = new Date(props.stats.lastUpdate)
  return (now - last) < 10000 // 10秒内
})

// 计算健康度
const healthPercent = computed(() => {
  const total = props.stats.active || 0
  if (total === 0) return 100
  const normal = props.stats.normal || 0
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

const handleViewDetail = () => {
  router.push('/monitor/batch/list')
}

const handleViewMonitor = () => {
  router.push('/monitor/batch/realtime')
}
</script>

<style scoped>
.monitor-card {
  background: white;
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
  color: #303133;
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
  color: #909399;
  margin-top: 8px;
}

/* 健康度指示器 */
.health-indicator {
  width: 100%;
  margin-bottom: 20px;
}

.health-bar-container {
  height: 12px;
  background: #F5F7FA;
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
  color: #606266;
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
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-item.success {
  background: #F0F9FF;
  border: 1px solid #E1F3F8;
}

.stat-item.warning {
  background: #FEF5E7;
  border: 1px solid #FDEBD0;
}

.stat-item.danger {
  background: #FEF0F0;
  border: 1px solid #FDE2E2;
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
  color: #909399;
}

.last-update {
  font-size: 12px;
  color: #909399;
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
</style>

