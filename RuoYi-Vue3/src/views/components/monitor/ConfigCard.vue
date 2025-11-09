<!-- 监控配置管理卡片 -->
<template>
  <div class="monitor-card">
    <div class="card-header">
      <div class="card-title">
        <span class="icon">📊</span>
        <h3>监控配置</h3>
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
      <!-- 配置数量显示 -->
      <div class="count-display">
        <div class="count-number">{{ stats.total || 0 }}</div>
        <div class="count-label">个配置</div>
      </div>
      
      <!-- 分类统计 -->
      <div class="stats-grid">
        <div class="stat-item">
          <span class="label">系统预设</span>
          <span class="value">{{ stats.preset || 0 }} 个</span>
        </div>
        <div class="stat-item">
          <span class="label">用户自定义</span>
          <span class="value">{{ stats.custom || 0 }} 个</span>
        </div>
      </div>
      
      <!-- 最近更新 -->
      <div class="last-update" v-if="stats.lastUpdate">
        最近更新：{{ formatTime(stats.lastUpdate) }}
      </div>
    </div>
    
    <div class="card-footer">
      <el-button type="primary" icon="Plus" @click="handleCreate">
        新建配置
      </el-button>
      <el-button icon="List" @click="handleManage">
        管理配置
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      total: 0,
      preset: 0,
      custom: 0,
      lastUpdate: null
    })
  },
  loading: Boolean
})

const emit = defineEmits(['refresh'])
const router = useRouter()

const formatTime = (time) => {
  if (!time) return '-'
  const now = new Date()
  const target = new Date(time)
  const diff = Math.floor((now - target) / 1000)
  
  if (diff < 60) return `${diff}秒前`
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`
  return `${Math.floor(diff / 86400)}天前`
}

const handleCreate = () => {
  router.push('/monitor/config/create')
}

const handleManage = () => {
  router.push('/monitor/config/list')
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

.card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.count-display {
  text-align: center;
  margin-bottom: 24px;
}

.count-number {
  font-size: 48px;
  font-weight: 700;
  color: #409EFF;
  line-height: 1;
}

.count-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stats-grid {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: #F5F7FA;
  border-radius: 6px;
}

.stat-item .label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-item .value {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
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

