<template>
  <div class="app-container batch-monitor-page">
    <!-- 页面标题和统计 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#409EFF"><Coin /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTokenCount }}</div>
              <div class="stat-label">总监控Token</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#67C23A"><Collection /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.solBatchCount + stats.bscBatchCount }}</div>
              <div class="stat-label">总批次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#E6A23C"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.solBatchCount }}</div>
              <div class="stat-label">SOL批次</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <el-icon class="stat-icon" color="#F56C6C"><TrendCharts /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ stats.bscBatchCount }}</div>
              <div class="stat-label">BSC批次</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 搜索和操作栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="来源类型">
          <el-select v-model="queryParams.sourceType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="批量监控" value="batch" />
            <el-option label="智能监控" value="smart" />
                </el-select>
              </el-form-item>
        <el-form-item label="链类型">
          <el-select v-model="queryParams.chainType" placeholder="请选择" clearable style="width: 120px">
            <el-option label="SOL" value="sol" />
            <el-option label="BSC" value="bsc" />
                </el-select>
              </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.isActive" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          <el-button type="success" :icon="Plus" @click="openAddDialog">批量添加</el-button>
              </el-form-item>
            </el-form>
    </el-card>

    <!-- 批次列表 -->
    <el-card shadow="never" class="table-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">📋 批量监控批次列表</span>
        </div>
      </template>

      <el-table :data="batchList" style="width: 100%" row-key="id">
        <!-- 配置名称 -->
        <el-table-column label="配置名称" width="200">
                <template #default="scope">
            <div class="config-name">
              <el-icon class="name-icon"><Document /></el-icon>
              <span>{{ scope.row.configName }}</span>
                  </div>
                </template>
              </el-table-column>

        <!-- 来源类型 -->
        <el-table-column label="来源类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.sourceType === 'batch' ? 'warning' : 'info'">
              {{ scope.row.sourceType === 'batch' ? '批量监控' : '智能监控' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 链类型 -->
        <el-table-column label="链类型" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.chainType === 'sol' ? 'primary' : 'success'">
              {{ scope.row.chainType.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 批次ID -->
        <el-table-column label="批次ID" width="100" align="center">
                <template #default="scope">
            <el-tag type="info"># {{ scope.row.batchId }}</el-tag>
                </template>
              </el-table-column>

        <!-- Token数量 -->
        <el-table-column label="Token数量" width="120" align="center">
                <template #default="scope">
            <el-badge :value="scope.row.tokenCount" :max="99" class="token-badge">
              <el-button size="small" @click="viewTokens(scope.row)">查看Token</el-button>
            </el-badge>
                </template>
              </el-table-column>

        <!-- 配置信息 -->
        <el-table-column label="监控配置" min-width="300">
                <template #default="scope">
            <div class="config-info">
              <el-tag size="small" class="config-tag">
                <el-icon><Clock /></el-icon>
                {{ scope.row.timeInterval }}
              </el-tag>
              <el-tag size="small" class="config-tag" type="warning">
                <el-icon><User /></el-icon>
                Top {{ scope.row.topHoldersThreshold }}
              </el-tag>
              <el-tag size="small" class="config-tag" type="success">
                {{ scope.row.triggerLogic === 'any' ? 'OR逻辑' : 'AND逻辑' }}
              </el-tag>
              <el-popover placement="top" :width="300" trigger="hover">
                <template #reference>
                  <el-tag size="small" class="config-tag" type="info">
                    <el-icon><Bell /></el-icon>
                    {{ getNotifyMethodsText(scope.row.notifyMethods) }}
                  </el-tag>
                </template>
                <div class="events-detail">
                  <div class="event-title">监控事件：</div>
                  {{ formatEventsConfig(scope.row.eventsConfig) }}
                    </div>
              </el-popover>
                  </div>
                </template>
              </el-table-column>

              <!-- 状态 -->
        <el-table-column label="状态" width="100" align="center">
                <template #default="scope">
                  <el-switch
              v-model="scope.row.isActive"
              :active-value="1"
              :inactive-value="0"
                    @change="handleStatusChange(scope.row)"
            />
                </template>
              </el-table-column>

              <!-- 创建时间 -->
        <el-table-column label="创建时间" width="180" align="center">
                <template #default="scope">
            {{ parseTime(scope.row.createTime) }}
                </template>
              </el-table-column>

              <!-- 操作 -->
        <el-table-column label="操作" width="200" align="center" fixed="right">
                <template #default="scope">
            <el-button size="small" type="primary" link @click="viewTokens(scope.row)">
              <el-icon><View /></el-icon> 查看
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
      <el-pagination
              v-show="total > 0" 
              :total="total" 
              v-model:page="queryParams.pageNum" 
              v-model:limit="queryParams.pageSize" 
              @pagination="getList" 
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px"
      />
    </el-card>

    <!-- Token列表弹窗 -->
    <el-dialog
      v-model="tokenDialogVisible"
      :title="`Token列表 - ${currentBatch.configName}`"
      width="80%"
      append-to-body
    >
      <el-table :data="tokenList" max-height="500" v-loading="tokenLoading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column label="Token信息" width="300">
          <template #default="scope">
            <div class="token-item">
              <div class="token-name">{{ scope.row.tokenName || '未知' }}</div>
              <div class="token-symbol">{{ scope.row.tokenSymbol || '-' }}</div>
              <el-input 
                :value="scope.row.ca"
                size="small"
                readonly
                class="ca-input"
              >
                <template #append>
                  <el-button @click="copyText(scope.row.ca)">
                    <el-icon><CopyDocument /></el-icon>
                  </el-button>
      </template>
              </el-input>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="市值" width="150" align="right">
          <template #default="scope">
            <span v-if="scope.row.marketCap">
              ${{ formatNumber(scope.row.marketCap) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getPriorityType(scope.row.priority)">
              {{ scope.row.priority || 0 }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isActive === 1 ? 'success' : 'info'">
              {{ scope.row.isActive === 1 ? '启用' : '停用' }}
            </el-tag>
      </template>
        </el-table-column>
        <el-table-column label="添加时间" width="180" align="center">
          <template #default="scope">
            {{ parseTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 批量添加弹窗 -->
    <BatchMonitorDialog v-model="addDialogVisible" @success="handleAddSuccess" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search,
  Refresh,
  Plus,
  Coin,
  Collection,
  TrendCharts,
  Document,
  Clock,
  User,
  Bell,
  View,
  Delete,
  CopyDocument
} from '@element-plus/icons-vue'
import { getBatchList, getBatchStats, getBatchTokens, deleteBatch, toggleBatchStatus } from '@/api/crypto/batchMonitor'
import { parseTime } from '@/utils/ruoyi'
import BatchMonitorDialog from '@/views/components/BatchMonitorDialog.vue'

// 状态
const loading = ref(false)
const tokenLoading = ref(false)
const total = ref(0)
const batchList = ref([])
const tokenList = ref([])
const tokenDialogVisible = ref(false)
const addDialogVisible = ref(false)
const currentBatch = ref({})

// 查询参数
const queryParams = reactive({
    pageNum: 1,
  pageSize: 20,
  sourceType: undefined,
  chainType: undefined,
  isActive: undefined
})

// 统计数据
const stats = ref({
  solBatchCount: 0,
  solTokenCount: 0,
  bscBatchCount: 0,
  bscTokenCount: 0,
  totalTokenCount: 0
})

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

// 加载批次列表
const getList = async () => {
  loading.value = true
  try {
    const response = await getBatchList(queryParams)
    if (response.code === 200) {
      batchList.value = response.rows
      total.value = response.total
    }
  } catch (error) {
    ElMessage.error('加载批次列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.sourceType = undefined
  queryParams.chainType = undefined
  queryParams.isActive = undefined
  handleQuery()
}

// 打开添加弹窗
const openAddDialog = () => {
  addDialogVisible.value = true
}

// 添加成功回调
const handleAddSuccess = () => {
  loadStats()
    getList()
}

// 查看Token列表
const viewTokens = async (row) => {
  currentBatch.value = row
  tokenDialogVisible.value = true
  tokenLoading.value = true
  
  try {
    const response = await getBatchTokens({
      batchId: row.batchId,
      chainType: row.chainType
    })
    if (response.code === 200) {
      tokenList.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载Token列表失败')
    console.error(error)
  } finally {
    tokenLoading.value = false
  }
}

// 状态切换
const handleStatusChange = async (row) => {
  try {
    await toggleBatchStatus({
      batchId: row.batchId,
      chainType: row.chainType,
      isActive: row.isActive
    })
    ElMessage.success(row.isActive === 1 ? '已启用' : '已停用')
    loadStats()
  } catch (error) {
    ElMessage.error('操作失败')
    row.isActive = row.isActive === 1 ? 0 : 1 // 回滚
    console.error(error)
  }
}

// 删除批次
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除批次 "${row.configName}" 及其包含的所有Token吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteBatch({
      batchId: row.batchId,
      chainType: row.chainType
    })
    
    ElMessage.success('删除成功')
    loadStats()
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 复制文本
const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
    console.error('复制失败:', error)
  }
}

// 格式化数字
const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('en-US').format(num)
}

// 获取通知方式文本
const getNotifyMethodsText = (methods) => {
  if (!methods) return '未设置'
  const methodMap = {
    telegram: 'TG',
    wechat: '微信'
  }
  return methods.split(',').map(m => methodMap[m] || m).join(', ')
}

// 格式化事件配置
const formatEventsConfig = (configStr) => {
  if (!configStr) return '无'
  try {
    const config = JSON.parse(configStr)
    const events = []
    if (config.priceChange?.enabled) events.push(`价格变动${config.priceChange.threshold}%`)
    if (config.holders?.enabled) events.push(`持币人变动${config.holders.threshold}`)
    if (config.volume?.enabled) events.push(`交易量${config.volume.threshold}`)
    return events.join('、') || '无'
  } catch {
    return '解析失败'
  }
}

// 获取优先级类型
const getPriorityType = (priority) => {
  if (priority >= 8) return 'danger'
  if (priority >= 5) return 'warning'
  return 'info'
}

// 初始化
onMounted(() => {
  loadStats()
  getList()
})
</script>

<style scoped lang="scss">
.batch-monitor-page {
  .stats-row {
    margin-bottom: 16px;
  }

  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 16px;

      .stat-icon {
        font-size: 40px;
      }

      .stat-info {
        flex: 1;

        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #303133;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 14px;
          color: #909399;
          margin-top: 4px;
        }
      }
    }
  }

  .search-card {
    margin-bottom: 16px;
  }

  .table-card {
    .card-header {
  display: flex;
      justify-content: space-between;
  align-items: center;

      .card-title {
        font-size: 16px;
        font-weight: 500;
}
    }
}

  .config-name {
  display: flex;
  align-items: center;
    gap: 8px;
    font-weight: 500;

    .name-icon {
      color: #409EFF;
    }
  }

  .config-info {
  display: flex;
    flex-wrap: wrap;
    gap: 8px;

    .config-tag {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  .token-badge {
    :deep(.el-badge__content) {
      top: 5px;
      right: 15px;
    }
  }

  .token-item {
    .token-name {
      font-weight: 500;
      margin-bottom: 4px;
    }

    .token-symbol {
  font-size: 12px;
  color: #909399;
      margin-bottom: 8px;
    }

    .ca-input {
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }
  }

  .events-detail {
    .event-title {
      font-weight: 500;
      margin-bottom: 8px;
      color: #303133;
    }
  }
}
</style>
