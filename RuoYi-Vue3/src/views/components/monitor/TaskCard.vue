<!-- 监控任务管理卡片 -->
<template>
  <div class="monitor-card">
    <div class="card-header">
      <div class="card-title">
        <span class="icon">🎯</span>
        <h3>监控任务</h3>
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
      <!-- 任务数量显示 -->
      <div class="count-display">
        <div class="count-number">{{ stats.total || 0 }}</div>
        <div class="count-label">个任务</div>
      </div>
      
      <!-- 状态统计 -->
      <div class="stats-list">
        <div class="stat-row">
          <span class="status-dot running"></span>
          <span class="label">运行中</span>
          <span class="value">{{ stats.running || 0 }} 个</span>
        </div>
        <div class="stat-row">
          <span class="status-dot paused"></span>
          <span class="label">已暂停</span>
          <span class="value">{{ stats.paused || 0 }} 个</span>
        </div>
        <div class="stat-row" v-if="stats.error > 0">
          <span class="status-dot error"></span>
          <span class="label">异常</span>
          <span class="value error-text">{{ stats.error }} 个</span>
        </div>
      </div>
      
      <!-- 类型统计 -->
      <div class="type-stats">
        <el-tag size="small">智能监控 {{ stats.smart || 0 }}</el-tag>
        <el-tag size="small" type="success">批量监控 {{ stats.batch || 0 }}</el-tag>
      </div>
    </div>
    
    <div class="card-footer">
      <el-dropdown split-button type="primary" @click="handleCreateSmart">
        <el-icon><Plus /></el-icon>
        新建任务
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="handleCreateSmart">
              <el-icon><MagicStick /></el-icon>
              智能监控
            </el-dropdown-item>
            <el-dropdown-item @click="handleCreateBatch">
              <el-icon><List /></el-icon>
              批量监控
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button icon="Management" @click="handleManage">
        任务列表
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Plus, MagicStick, List, Management } from '@element-plus/icons-vue'

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      total: 0,
      running: 0,
      paused: 0,
      error: 0,
      smart: 0,
      batch: 0
    })
  },
  loading: Boolean
})

const emit = defineEmits(['refresh'])
const router = useRouter()

const handleCreateSmart = () => {
  router.push('/monitor/task/create?type=smart')
}

const handleCreateBatch = () => {
  router.push('/monitor/task/create?type=batch')
}

const handleManage = () => {
  router.push('/monitor/task/list')
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
  color: #67C23A;
  line-height: 1;
}

.count-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.stats-list {
  width: 100%;
  margin-bottom: 16px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background 0.3s;
}

.stat-row:hover {
  background: #F5F7FA;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.status-dot.running {
  background: #67C23A;
}

.status-dot.paused {
  background: #E6A23C;
}

.status-dot.error {
  background: #F56C6C;
}

.stat-row .label {
  flex: 1;
  font-size: 13px;
  color: #606266;
}

.stat-row .value {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.stat-row .value.error-text {
  color: #F56C6C;
}

.type-stats {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.card-footer {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.card-footer .el-button,
.card-footer .el-dropdown {
  flex: 1;
}
</style>

