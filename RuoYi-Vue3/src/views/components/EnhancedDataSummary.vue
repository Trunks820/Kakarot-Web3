<template>
  <el-row :gutter="20">
    <!-- 预警统计 -->
    <el-col :span="5">
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
    <el-col :span="5">
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
    <el-col :span="5">
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
    <el-col :span="5">
      <el-card class="data-card">
        <template #header>
          <div class="card-header">
            <span>机器人状态</span>
            <el-tag type="success" size="small">在线</el-tag>
          </div>
        </template>
        <div class="card-body">
          <div class="stat-item">
            <div class="stat-label">
              <el-icon><Service /></el-icon>
              <span>TG机器人</span>
            </div>
            <h3 class="success">运行中</h3>
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
    <el-col :span="4">
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
</template>

<script setup name="EnhancedDataSummary">
import { ref, onMounted } from 'vue'
import { 
  Warning, Clock, Monitor, TrendCharts, 
  DataLine, Timer, Service, Cpu, Operation 
} from '@element-plus/icons-vue'

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
  uptime: '2天15小时'
})

const systemStats = ref({
  cpuUsage: 45,
  memoryUsage: 67
})

// 模拟实时数据更新
onMounted(() => {
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
})
</script>

<style scoped lang="scss">
.data-card {
  height: 140px;
  border: 1px solid var(--el-border-color-light);
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--el-color-primary);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-card__header) {
    padding: 12px 16px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    span {
      font-size: 14px;
      color: var(--el-text-color-primary);
      font-weight: 600;
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

/* 响应式设计 */
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
</style> 