<template>
  <el-card class="widget-card batch-monitor-card" shadow="hover">
    <template #header>
      <div class="widget-header">
        <div class="widget-title">
          <el-icon class="widget-icon"><DocumentAdd /></el-icon>
          <span>批量添加监控</span>
        </div>
        <el-button 
          type="primary" 
          size="small" 
          @click="openBatchDialog"
          :icon="Plus"
        >
          快速添加
        </el-button>
      </div>
    </template>

    <!-- 统计信息 -->
    <div class="stats-grid">
      <div class="stat-item">
        <div class="stat-icon">
          <el-icon color="#409EFF"><Coin /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.totalTokenCount }}</div>
          <div class="stat-label">总监控</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon">
          <el-icon color="#67C23A"><Collection /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.solBatchCount + stats.bscBatchCount }}</div>
          <div class="stat-label">批次数</div>
        </div>
      </div>

      <div class="stat-item">
        <div class="stat-icon">
          <el-icon color="#E6A23C"><Bell /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.todayAlerts || 0 }}</div>
          <div class="stat-label">今日告警</div>
        </div>
      </div>
    </div>

    <!-- 链类型统计 -->
    <el-divider style="margin: 16px 0;" />
    
    <div class="chain-stats">
      <div class="chain-item">
        <el-tag type="primary" effect="plain">SOL</el-tag>
        <span class="chain-count">
          {{ stats.solBatchCount }}批次 / {{ stats.solTokenCount }}个Token
        </span>
      </div>
      <div class="chain-item">
        <el-tag type="success" effect="plain">BSC</el-tag>
        <span class="chain-count">
          {{ stats.bscBatchCount }}批次 / {{ stats.bscTokenCount }}个Token
        </span>
      </div>
    </div>

    <template #footer>
      <div class="widget-footer">
        <el-button 
          size="small" 
          @click="goToManagePage"
          style="width: 100%;"
        >
          <el-icon><View /></el-icon>
          <span>查看全部</span>
        </el-button>
      </div>
    </template>
  </el-card>

  <!-- 批量监控弹窗 -->
  <BatchMonitorDialog 
    v-model="showDialog" 
    @success="handleSuccess"
  />
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  DocumentAdd, 
  Plus, 
  Coin, 
  Collection, 
  Bell, 
  View 
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getBatchStats } from '@/api/crypto/batchMonitor'
import BatchMonitorDialog from './BatchMonitorDialog.vue'

const router = useRouter()
const showDialog = ref(false)
const stats = ref({
  solBatchCount: 0,
  solTokenCount: 0,
  bscBatchCount: 0,
  bscTokenCount: 0,
  totalTokenCount: 0,
  todayAlerts: 0
})

// 打开批量添加对话框
const openBatchDialog = () => {
  showDialog.value = true
}

// 加载统计数据
const loadStats = async () => {
  try {
    const response = await getBatchStats()
    if (response.code === 200) {
      stats.value = response.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 添加成功后刷新统计
const handleSuccess = () => {
  loadStats()
  ElMessage.success('批量监控添加成功！')
}

// 跳转到管理页面
const goToManagePage = () => {
  router.push('/crypto/monitor')
}

// 初始化加载统计
onMounted(() => {
  loadStats()
})
</script>

<style scoped lang="scss">
.batch-monitor-card {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-card__body) {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
}

.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .widget-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;

    .widget-icon {
      font-size: 20px;
      color: #409EFF;
    }
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 8px;

  .stat-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px;
    background: linear-gradient(135deg, #f5f7fa 0%, #ffffff 100%);
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    .stat-icon {
      font-size: 28px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-content {
      flex: 1;

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}

.chain-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .chain-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    background: #f5f7fa;
    border-radius: 6px;

    .chain-count {
      font-size: 13px;
      color: #606266;
      font-weight: 500;
    }
  }
}

.widget-footer {
  padding: 0;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
  margin-top: auto;
}

// 响应式调整
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;

    .stat-item {
      flex-direction: column;
      text-align: center;
      padding: 8px;

      .stat-icon {
        font-size: 24px;
      }

      .stat-content .stat-value {
        font-size: 20px;
      }
    }
  }
}
</style>

