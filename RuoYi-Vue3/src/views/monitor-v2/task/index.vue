<!-- 任务管理完整页面 -->
<template>
  <div class="task-manage">
    <el-card>
      <template #header>
        <div class="page-header">
          <span class="page-title">任务管理</span>
          <el-dropdown 
            v-hasPermi="['crypto:monitor-v2:task:add']"
            split-button 
            type="primary"
            @click="handleAdd('smart')"
          >
            <el-icon><Plus /></el-icon>
            新建任务
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleAdd('smart')">
                  <el-icon><MagicStick /></el-icon>
                  智能监控
                </el-dropdown-item>
                <el-dropdown-item @click="handleAdd('batch')">
                  <el-icon><List /></el-icon>
                  批量监控
                </el-dropdown-item>
                <el-dropdown-item @click="handleAdd('block')">
                  <el-icon><Histogram /></el-icon>
                  区块监控
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="任务名称">
          <el-input
            v-model="queryParams.taskName"
            placeholder="请输入任务名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="queryParams.taskType" placeholder="请选择任务类型" clearable>
            <el-option label="智能监控" value="smart" />
            <el-option label="批量监控" value="batch" />
            <el-option label="区块监控" value="block" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="运行中" :value="1" />
            <el-option label="已停止" :value="0" />
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
        :data="taskList"
        stripe
        style="width: 100%"
      >
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="任务名称" prop="taskName" min-width="150" show-overflow-tooltip />
        <el-table-column label="任务类型" prop="taskType" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.taskType === 'smart'" type="primary" size="small">智能监控</el-tag>
            <el-tag v-else-if="scope.row.taskType === 'batch'" type="success" size="small">批量监控</el-tag>
            <el-tag v-else-if="scope.row.taskType === 'block'" type="warning" size="small">区块监控</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="链类型" prop="chainType" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.chainType?.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '运行中' : '已停止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联批次数" prop="batchCount" width="120" />
        <el-table-column label="创建时间" prop="createTime" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-hasPermi="['crypto:monitor-v2:task:query']"
              text 
              type="primary" 
              size="small"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:task:start', 'crypto:monitor-v2:task:stop']"
              text 
              :type="scope.row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggle(scope.row)"
            >
              {{ scope.row.status === 1 ? '停止' : '启动' }}
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:task:remove']"
              text 
              type="danger" 
              size="small"
              @click="handleDelete(scope.row)"
            >
              删除
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

    <!-- 新增/编辑对话框 - 使用统一的TaskFormDialog组件 -->
    <TaskFormDialog
      v-model="dialogVisible"
      :taskData="taskForm"
      :configList="allConfigs"
      @submit="handleFormSubmit"
    />

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务详情"
      width="900px"
      append-to-body
    >
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="任务ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="任务名称">{{ detailData.taskName }}</el-descriptions-item>
        <el-descriptions-item label="任务类型">
          <el-tag v-if="detailData.taskType === 'smart'" type="primary">智能监控</el-tag>
          <el-tag v-else-if="detailData.taskType === 'batch'" type="success">批量监控</el-tag>
          <el-tag v-else-if="detailData.taskType === 'block'" type="warning">区块监控</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="链类型">
          <el-tag>{{ detailData.chainType?.toUpperCase() }}</el-tag>
        </el-descriptions-item>
        
        <!-- 智能监控特有信息 -->
        <template v-if="detailData.taskType === 'smart'">
          <el-descriptions-item label="市值范围">
            {{ detailData.minMarketCap || 0 }} ~ {{ detailData.maxMarketCap || '不限' }}
          </el-descriptions-item>
          <el-descriptions-item label="Twitter要求">
            {{ detailData.hasTwitter === 1 ? '必须有' : '不限制' }}
          </el-descriptions-item>
          <el-descriptions-item label="自动同步">
            {{ detailData.autoSyncTargets === 1 ? '已启用' : '未启用' }}
          </el-descriptions-item>
          <el-descriptions-item label="同步间隔" v-if="detailData.autoSyncTargets === 1">
            {{ detailData.syncIntervalMinutes }}分钟
          </el-descriptions-item>
        </template>
        
        <!-- 区块监控特有信息 -->
        <template v-if="detailData.taskType === 'block'">
          <el-descriptions-item label="市场类型">
            {{ detailData.marketType === 'internal' ? '内盘交易' : '外盘交易' }}
          </el-descriptions-item>
          <el-descriptions-item label="最小交易金额">
            ${{ detailData.minTransactionUsd }}
          </el-descriptions-item>
          <el-descriptions-item label="累计金额阈值" :span="2">
            ${{ detailData.cumulativeMinAmountUsd }}
          </el-descriptions-item>
        </template>
        
        <el-descriptions-item label="关联配置数">
          {{ detailData.configCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="监控目标数">
          {{ detailData.targetCount || 0 }}
        </el-descriptions-item>
        <el-descriptions-item label="定时规则" :span="2">
          {{ detailData.scheduleCron || '持续运行' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'">
            {{ detailData.status === 1 ? '运行中' : '已停止' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上次执行时间">
          {{ detailData.lastRunTime || '未执行' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ detailData.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ detailData.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-hasPermi="['crypto:monitor-v2:task:start', 'crypto:monitor-v2:task:stop']"
          :type="detailData?.status === 1 ? 'warning' : 'success'"
          @click="handleToggleFromDetail"
        >
          {{ detailData?.status === 1 ? '停止' : '启动' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { Plus, MagicStick, List, Histogram } from '@element-plus/icons-vue'
import { listTask, getTask, addTask, updateTask, delTask, startTask, stopTask } from '@/api/crypto/monitor-v2'
import { listConfig } from '@/api/crypto/monitor-v2'
import { ElMessage, ElMessageBox } from 'element-plus'
import TaskFormDialog from '@/views/components/monitor/TaskFormDialog.vue'

const loading = ref(false)
const taskList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  taskName: undefined,
  taskType: undefined,
  status: undefined
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = computed(() => taskForm.value.id ? '编辑任务' : '新建任务')
const submitLoading = ref(false)
const taskFormRef = ref(null)

// 可用配置列表
const availableConfigs = ref([])
// 所有配置列表（供TaskFormDialog使用）
const allConfigs = ref([])

// 表单数据
const taskForm = ref({
  id: undefined,
  taskName: '',
  taskType: '',
  chainType: '',
  minMarketCap: undefined,
  maxMarketCap: undefined,
  hasTwitter: 0,
  autoSyncTargets: 0,
  syncIntervalMinutes: 30,
  marketType: 'internal',
  minTransactionUsd: undefined,
  cumulativeMinAmountUsd: undefined,
  configIds: [],
  scheduleCron: '',
  description: '',
  status: 1
})

// 表单验证规则
const taskRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  chainType: [{ required: true, message: '请选择链类型', trigger: 'change' }],
  configIds: [{ required: true, message: '请至少选择一个监控配置', trigger: 'change', type: 'array' }]
}

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref(null)

// 监听链类型变化，过滤可用配置
watch(() => taskForm.value.chainType, async (newChainType) => {
  if (newChainType && dialogVisible.value) {
    await loadAvailableConfigs(newChainType)
  }
})

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const response = await listTask(queryParams.value)
    taskList.value = response.rows
    total.value = response.total
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 加载可用配置列表
const loadAvailableConfigs = async (chainType) => {
  try {
    const response = await listConfig({ 
      chainType: chainType,
      status: 1,  // 只加载启用的配置
      pageNum: 1,
      pageSize: 100 
    })
    availableConfigs.value = response.rows || []
  } catch (error) {
    console.error('加载配置列表失败:', error)
    availableConfigs.value = []
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
    taskName: undefined,
    taskType: undefined,
    status: undefined
  }
  getList()
}

// 新增
const handleAdd = (type) => {
  resetForm()
  taskForm.value.taskType = type
  dialogVisible.value = true
}

// 详情
const handleDetail = async (row) => {
  try {
    const response = await getTask(row.id)
    detailData.value = response.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message)
  }
}

// 配置变化
const handleConfigChange = (configIds) => {
  console.log('选中的配置:', configIds)
}

// 处理TaskFormDialog提交
const handleFormSubmit = async (formData) => {
  try {
    submitLoading.value = true
    
    // 构建提交数据
    const submitData = {
      ...formData,
      configIds: formData.configId ? [formData.configId] : []  // 转换为数组格式
    }
    
    // 如果是批量监控，处理caList
    if (submitData.taskType === 'batch' && submitData.caList) {
      submitData.targetAddresses = submitData.caList
        .trim()
        .split('\n')
        .filter(line => line.trim())
        .slice(0, 99)  // 最多99个
    }
    
    if (submitData.id) {
      await updateTask(submitData)
      ElMessage.success('更新成功')
    } else {
      // 根据任务类型调用不同的API
      if (submitData.taskType === 'smart') {
        await addTask({
          ...submitData,
          taskType: 'smart'
        }, 'smart')
      } else if (submitData.taskType === 'batch') {
        await addTask({
          ...submitData,
          taskType: 'batch'
        }, 'batch')
      } else if (submitData.taskType === 'block') {
        await addTask({
          ...submitData,
          taskType: 'block'
        }, 'block')
      }
      ElMessage.success('创建成功')
    }
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('操作失败：' + error.message)
  } finally {
    submitLoading.value = false
  }
}

// 提交表单（保留旧方法以防其他地方引用）
const submitForm = async () => {
  // 已由handleFormSubmit替代
}

// 重置表单
const resetForm = () => {
  taskForm.value = {
    id: undefined,
    taskName: '',
    taskType: '',
    chainType: '',
    minMarketCap: undefined,
    maxMarketCap: undefined,
    hasTwitter: 0,
    autoSyncTargets: 0,
    syncIntervalMinutes: 30,
    marketType: 'internal',
    minTransactionUsd: undefined,
    cumulativeMinAmountUsd: undefined,
    configIds: [],
    scheduleCron: '',
    description: '',
    status: 1
  }
  availableConfigs.value = []
  taskFormRef.value?.resetFields()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 启停切换
const handleToggle = async (row) => {
  const action = row.status === 1 ? '停止' : '启动'
  try {
    await ElMessageBox.confirm(`确定${action}任务"${row.taskName}"吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (row.status === 1) {
      await stopTask(row.id)
    } else {
      await startTask(row.id)
    }
    
    ElMessage.success(`${action}成功`)
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败：` + error.message)
    }
  }
}

// 从详情对话框启停
const handleToggleFromDetail = async () => {
  if (!detailData.value) return
  await handleToggle(detailData.value)
  detailDialogVisible.value = false
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除任务"${row.taskName}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delTask(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 加载所有配置（供TaskFormDialog使用）
const loadAllConfigs = async () => {
  try {
    const response = await listConfig({ 
      status: 1,  // 只加载启用的配置
      pageNum: 1,
      pageSize: 1000 
    })
    allConfigs.value = response.rows || []
  } catch (error) {
    console.error('加载配置列表失败:', error)
    allConfigs.value = []
  }
}

onMounted(() => {
  getList()
  loadAllConfigs()
})
</script>

<style scoped lang="scss">
.task-manage {
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

