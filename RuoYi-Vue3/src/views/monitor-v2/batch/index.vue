<!-- 批次管理完整页面 -->
<template>
  <div class="batch-manage">
    <el-card>
      <template #header>
        <div class="page-header">
          <span class="page-title">批次管理</span>
          <el-button type="primary" icon="Refresh" @click="getList">
            刷新
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="批次号">
          <el-input
            v-model="queryParams.batchNo"
            placeholder="请输入批次号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="运行中" value="running" />
            <el-option label="已暂停" value="paused" />
            <el-option label="心跳超时" value="timeout" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="batchList"
        stripe
        style="width: 100%"
      >
        <el-table-column label="批次号" prop="batchNo" min-width="200" show-overflow-tooltip />
        <el-table-column label="任务名称" prop="taskName" min-width="150" show-overflow-tooltip />
        <el-table-column label="Worker" prop="workerId" width="150" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'running'" type="success" size="small">运行中</el-tag>
            <el-tag v-else-if="scope.row.status === 'paused'" type="warning" size="small">已暂停</el-tag>
            <el-tag v-else-if="scope.row.status === 'timeout'" type="danger" size="small">心跳超时</el-tag>
            <el-tag v-else type="info" size="small">已完成</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="项数" prop="itemCount" width="100" />
        <el-table-column label="告警数" prop="totalAlerts" width="100" />
        <el-table-column label="错误数" prop="errorCount" width="100" />
        <el-table-column label="心跳时间" prop="lastHeartbeat" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-hasPermi="['crypto:monitor-v2:batch:query']"
              text 
              type="primary" 
              size="small"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:batch:restart']"
              text 
              type="warning" 
              size="small"
              @click="handleRestart(scope.row)"
              :disabled="scope.row.status === 'running'"
            >
              重启
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="批次详情"
      width="1200px"
      append-to-body
    >
      <el-descriptions v-if="detailData" :column="3" border>
        <el-descriptions-item label="批次号" :span="2">{{ detailData.batchNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailData.status === 'running'" type="success">运行中</el-tag>
          <el-tag v-else-if="detailData.status === 'paused'" type="warning">已暂停</el-tag>
          <el-tag v-else-if="detailData.status === 'timeout'" type="danger">心跳超时</el-tag>
          <el-tag v-else type="info">已完成</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="任务ID">{{ detailData.taskId }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detailData.taskName || '未知任务' }}</el-descriptions-item>
        <el-descriptions-item label="链类型">
          <el-tag size="small">{{ detailData.chainType?.toUpperCase() || 'N/A' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Worker ID" :span="2">{{ detailData.workerId || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="Worker PID">{{ detailData.workerPid || 'N/A' }}</el-descriptions-item>
        <el-descriptions-item label="监控项数">{{ detailData.itemCount }}</el-descriptions-item>
        <el-descriptions-item label="总告警数">{{ detailData.totalAlerts || 0 }}</el-descriptions-item>
        <el-descriptions-item label="错误数">
          <span :style="{ color: detailData.errorCount > 0 ? 'var(--el-color-danger)' : 'inherit' }">
            {{ detailData.errorCount || 0 }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="心跳时间" :span="2">
          {{ detailData.lastHeartbeat || '无心跳' }}
        </el-descriptions-item>
        <el-descriptions-item label="最后告警时间">
          {{ detailData.lastAlertTime || '无告警' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="3">
          {{ detailData.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="最后错误" :span="3" v-if="detailData.lastError">
          <el-alert type="error" :closable="false" show-icon>
            {{ detailData.lastError }}
          </el-alert>
        </el-descriptions-item>
      </el-descriptions>
      
      <!-- 批次项列表 -->
      <el-divider content-position="left">
        <span style="font-weight: 600;">批次项列表</span>
      </el-divider>
      
      <el-table
        v-loading="itemsLoading"
        :data="batchItems"
        stripe
        max-height="400"
        style="width: 100%"
      >
        <el-table-column label="序号" prop="itemOrder" width="80" />
        <el-table-column label="代币地址" prop="ca" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <el-link :href="`https://solscan.io/token/${scope.row.ca}`" target="_blank" type="primary">
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
        <el-table-column label="创建时间" prop="createTime" width="180" show-overflow-tooltip />
      </el-table>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-hasPermi="['crypto:monitor-v2:batch:restart']"
          type="warning" 
          @click="handleRestartFromDetail"
          :disabled="detailData?.status === 'running'"
        >
          重启批次
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listBatch, getBatch, getBatchItems, restartBatch } from '@/api/crypto/monitor-v2'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const batchList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  batchNo: undefined,
  status: undefined
})

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref(null)
const itemsLoading = ref(false)
const batchItems = ref([])

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const response = await listBatch(queryParams.value)
    batchList.value = response.rows
    total.value = response.total
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    batchNo: undefined,
    status: undefined
  }
  getList()
}

// 详情
const handleDetail = async (row) => {
  try {
    // 加载批次详情
    const response = await getBatch(row.id)
    detailData.value = response.data
    
    // 加载批次项列表
    loadBatchItems(row.id)
    
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message)
  }
}

// 加载批次项列表
const loadBatchItems = async (batchId) => {
  itemsLoading.value = true
  try {
    const response = await getBatchItems(batchId)
    batchItems.value = response.rows || []
  } catch (error) {
    console.error('加载批次项失败:', error)
    batchItems.value = []
  } finally {
    itemsLoading.value = false
  }
}

// 重启
const handleRestart = async (row) => {
  try {
    await ElMessageBox.confirm(`确定重启批次"${row.batchNo}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await restartBatch(row.id)
    ElMessage.success('重启成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重启失败：' + error.message)
    }
  }
}

// 从详情对话框重启
const handleRestartFromDetail = async () => {
  if (!detailData.value) return
  await handleRestart(detailData.value)
  detailDialogVisible.value = false
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('en-US', {
    notation: 'compact',
    compactDisplay: 'short'
  }).format(num)
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.batch-manage {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .page-title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>

