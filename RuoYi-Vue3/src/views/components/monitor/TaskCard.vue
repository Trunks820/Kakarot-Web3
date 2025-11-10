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
        <el-tag size="small" type="warning">区块监控 {{ stats.block || 0 }}</el-tag>
      </div>
    </div>
    
    <div class="card-footer">
      <el-dropdown 
        v-hasPermi="['crypto:monitor-v2:task:add']"
        split-button 
        type="primary" 
        @click="openTaskDialog('smart')"
      >
        <el-icon><Plus /></el-icon>
        新建任务
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="openTaskDialog('smart')">
              <el-icon><MagicStick /></el-icon>
              智能监控
            </el-dropdown-item>
            <el-dropdown-item @click="openTaskDialog('batch')">
              <el-icon><List /></el-icon>
              批量监控
            </el-dropdown-item>
            <el-dropdown-item @click="openTaskDialog('block')">
              <el-icon><Histogram /></el-icon>
              区块监控
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <el-button 
        v-hasPermi="['crypto:monitor-v2:task:list']"
        icon="Management" 
        @click="openManageDialog"
      >
        任务列表
      </el-button>
    </div>

    <!-- 新建任务弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="getDialogTitle"
      width="800px"
      append-to-body
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        
        <el-form-item label="链类型" prop="chainType">
          <el-radio-group v-model="form.chainType">
            <el-radio label="sol">
              <el-icon><Star /></el-icon> Solana
            </el-radio>
            <el-radio label="bsc">
              <el-icon><Coin /></el-icon> BSC
            </el-radio>
            <el-radio label="eth">
              <el-icon><Coin /></el-icon> Ethereum
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 智能监控专属字段 -->
        <template v-if="taskType === 'smart'">
          <el-form-item label="市值范围" prop="marketCapRange">
            <el-row :gutter="10">
              <el-col :span="11">
                <el-input v-model.number="form.minMarketCap" placeholder="最小市值">
                  <template #append>USD</template>
                </el-input>
              </el-col>
              <el-col :span="2" style="text-align: center">-</el-col>
              <el-col :span="11">
                <el-input v-model.number="form.maxMarketCap" placeholder="最大市值(可选)">
                  <template #append>USD</template>
                </el-input>
              </el-col>
            </el-row>
          </el-form-item>
          
          <el-form-item label="Twitter筛选">
            <el-radio-group v-model="form.hasTwitter">
              <el-radio :label="null">不限</el-radio>
              <el-radio :label="1">有Twitter</el-radio>
              <el-radio :label="0">无Twitter</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="自动同步">
            <el-switch v-model="form.autoSyncTargets" />
            <span class="form-tip">开启后将自动同步符合条件的Token</span>
          </el-form-item>
          
          <el-form-item label="同步间隔" v-if="form.autoSyncTargets">
            <el-input-number v-model="form.syncIntervalMinutes" :min="5" :max="1440" />
            <span class="form-tip">分钟</span>
          </el-form-item>
        </template>
        
        <!-- 批量监控专属字段 -->
        <template v-if="taskType === 'batch'">
          <el-form-item label="Token地址" prop="caList">
            <el-input
              v-model="form.caList"
              type="textarea"
              :rows="6"
              placeholder="请输入Token地址，每行一个（最多99个）"
            />
            <div class="form-tip">
              已输入: {{ caCount }} 个地址
              <span v-if="caCount > 99" style="color: #F56C6C">（超过99个，将只创建前99个）</span>
            </div>
          </el-form-item>
        </template>
        
        <!-- 区块监控专属字段 -->
        <template v-if="taskType === 'block'">
          <el-alert
            title="区块监控说明"
            type="info"
            :closable="false"
            style="margin-bottom: 16px;"
          >
            <template #default>
              <div style="font-size: 13px; line-height: 1.6;">
                • 全网监听区块交易事件，无需指定监控目标<br>
                • 内外盘和金额阈值由配置决定<br>
                • 请选择合适的监控配置（配置中已包含market_type和金额阈值）
              </div>
            </template>
          </el-alert>
          
        </template>
        
        <el-form-item label="关联配置" prop="configId">
          <el-select
            v-model="form.configId"
            placeholder="请选择监控配置"
            style="width: 100%"
          >
            <el-option
              v-for="config in configList"
              :key="config.id"
              :label="config.configName"
              :value="config.id"
            >
              <span>{{ config.configName }}</span>
              <el-tag size="small" style="margin-left: 8px">{{ config.chainType }}</el-tag>
              <el-tag 
                v-if="config.marketType" 
                :type="config.marketType === 'external' ? 'success' : 'warning'" 
                size="small" 
                style="margin-left: 4px"
              >
                {{ config.marketType === 'external' ? '外盘' : '内盘' }}
              </el-tag>
            </el-option>
          </el-select>
          <div class="form-tip">💡 选择1个监控配置，配置中可包含多个事件监控规则</div>
        </el-form-item>
        
        <el-form-item label="任务描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入任务描述（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ taskType === 'smart' ? '创建并同步目标' : '创建任务' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务列表弹窗 -->
    <el-dialog
      v-model="manageDialogVisible"
      title="任务列表"
      width="1000px"
      append-to-body
    >
      <el-table
        v-loading="manageLoading"
        :data="taskList"
        stripe
        style="width: 100%"
        max-height="500px"
      >
        <el-table-column label="任务名称" prop="taskName" width="200" show-overflow-tooltip />
        <el-table-column label="任务类型" prop="taskType" width="100" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.taskType === 'smart'" type="primary" size="small">智能监控</el-tag>
            <el-tag v-else-if="scope.row.taskType === 'batch'" type="success" size="small">批量监控</el-tag>
            <el-tag v-else-if="scope.row.taskType === 'block'" type="warning" size="small">区块监控</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="链类型" prop="chainType" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.chainType === 'sol' ? 'success' : 'warning'" size="small">
              {{ scope.row.chainType.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目标数量" prop="targetCount" width="90" align="center">
          <template #default="scope">
            <span v-if="scope.row.taskType !== 'block'">{{ scope.row.targetCount || 0 }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="配置数量" prop="configCount" width="90" align="center" />
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 0 ? 'info' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '运行中' : scope.row.status === 0 ? '已暂停' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后运行" prop="lastRunTime" width="150" />
        <el-table-column label="描述" prop="description" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-hasPermi="['crypto:monitor-v2:task:query']"
              text 
              type="primary" 
              size="small" 
              @click="handleTaskDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:task:start', 'crypto:monitor-v2:task:stop']"
              text 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              size="small" 
              @click="handleTaskToggle(scope.row)"
            >
              {{ scope.row.status === 1 ? '暂停' : '启动' }}
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:task:remove']"
              text 
              type="danger" 
              size="small" 
              @click="handleTaskDelete(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <template #footer>
        <el-button @click="manageDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务详情"
      width="700px"
      append-to-body
    >
      <div v-if="taskDetail" class="task-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称" :span="2">
            {{ taskDetail.taskName }}
          </el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag v-if="taskDetail.taskType === 'smart'" type="primary" size="small">智能监控</el-tag>
            <el-tag v-else-if="taskDetail.taskType === 'batch'" type="success" size="small">批量监控</el-tag>
            <el-tag v-else-if="taskDetail.taskType === 'block'" type="warning" size="small">区块监控</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="链类型">
            <el-tag :type="taskDetail.chainType === 'sol' ? 'success' : 'warning'" size="small">
              {{ taskDetail.chainType.toUpperCase() }}
            </el-tag>
          </el-descriptions-item>
          
          <!-- 智能监控特有字段 -->
          <template v-if="taskDetail.taskType === 'smart'">
            <el-descriptions-item label="最小市值">
              {{ taskDetail.minMarketCap ? (taskDetail.minMarketCap / 10000).toFixed(0) + '万' : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="最大市值">
              {{ taskDetail.maxMarketCap ? (taskDetail.maxMarketCap / 10000).toFixed(0) + '万' : '不限' }}
            </el-descriptions-item>
            <el-descriptions-item label="Twitter要求">
              {{ taskDetail.hasTwitter === 1 ? '必须有' : '不限制' }}
            </el-descriptions-item>
            <el-descriptions-item label="自动同步">
              {{ taskDetail.autoSyncTargets === 1 ? '是' : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="同步间隔" v-if="taskDetail.autoSyncTargets === 1">
              {{ taskDetail.syncIntervalMinutes }}分钟
            </el-descriptions-item>
          </template>
          
          <!-- 区块监控特有字段 -->
          <template v-if="taskDetail.taskType === 'block'">
            <el-descriptions-item label="说明" :span="2">
              全网区块监听任务，具体监控规则由关联配置决定
            </el-descriptions-item>
          </template>
          
          <el-descriptions-item label="目标数量" v-if="taskDetail.taskType !== 'block'">
            {{ taskDetail.targetCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="配置数量">
            {{ taskDetail.configCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="taskDetail.status === 1 ? 'success' : 'info'" size="small">
              {{ taskDetail.status === 1 ? '运行中' : '已暂停' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后运行时间">
            {{ taskDetail.lastRunTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="下次运行时间">
            {{ taskDetail.nextRunTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ taskDetail.createTime }}
          </el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">
            {{ taskDetail.description || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, getCurrentInstance, watch } from 'vue'
import { addSmartTask, addBatchTask, addBlockTask, listTask, delTask, startTask, stopTask } from '@/api/crypto/monitor-v2'
import { listConfig } from '@/api/crypto/monitor-v2'
import { Plus, MagicStick, List as ListIcon, Histogram, Management, Star, Coin } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

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
const dialogVisible = ref(false)
const submitting = ref(false)
const taskType = ref('smart')
const formRef = ref(null)
const configList = ref([])

// 任务列表管理弹窗
const manageDialogVisible = ref(false)
const manageLoading = ref(false)
const taskList = ref([])

const form = reactive({
  taskName: '',
  chainType: 'sol',
  // 智能监控字段
  minMarketCap: 10000,
  maxMarketCap: null,
  hasTwitter: null,
  autoSyncTargets: true,
  syncIntervalMinutes: 30,
  // 批量监控字段
  caList: '',
  // 通用字段
  configId: null, // 配置ID（所有任务类型统一使用单选）
  description: ''
})

const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' }
  ],
  chainType: [
    { required: true, message: '请选择链类型', trigger: 'change' }
  ],
  configId: [
    { required: true, message: '请选择监控配置', trigger: 'change' }
  ],
  caList: [
    { required: true, message: '请输入Token地址', trigger: 'blur' }
  ]
}

// 计算弹窗标题
const getDialogTitle = computed(() => {
  const titles = {
    'smart': '新建智能监控任务',
    'batch': '新建批量监控任务',
    'block': '新建区块监控任务'
  }
  return titles[taskType.value] || '新建任务'
})

// 计算CA数量
const caCount = computed(() => {
  if (!form.caList) return 0
  return form.caList.split('\n').filter(line => line.trim()).length
})

// 监听链类型变化，过滤配置列表
watch(() => form.chainType, (newVal) => {
  loadConfigList(newVal)
})

const openManageDialog = async () => {
  console.log('打开任务列表弹窗')
  manageDialogVisible.value = true
  manageLoading.value = true
  try {
    const response = await listTask({ pageNum: 1, pageSize: 100 })
    console.log('任务列表响应:', response)
    taskList.value = response.rows || []
    console.log('任务列表数据:', taskList.value)
  } catch (error) {
    console.error('加载任务列表失败:', error)
    proxy.$modal.msgError('加载任务列表失败: ' + (error.message || '未知错误'))
  } finally {
    manageLoading.value = false
  }
}

const detailDialogVisible = ref(false)
const taskDetail = ref(null)

const handleTaskDetail = (row) => {
  console.log('查看任务详情:', row)
  taskDetail.value = row
  detailDialogVisible.value = true
}

const handleTaskToggle = async (row) => {
  const isRunning = row.status === 1
  const action = isRunning ? '暂停' : '启动'
  
  try {
    if (isRunning) {
      await stopTask(row.id)
    } else {
      await startTask(row.id)
    }
    proxy.$modal.msgSuccess(`${action}成功`)
    openManageDialog() // 重新加载列表
    emit('refresh') // 刷新卡片统计
  } catch (error) {
    console.error(`${action}失败:`, error)
    if (error.code === 'ERR_BAD_REQUEST' && error.response?.status === 404) {
      proxy.$modal.msgWarning(`后端接口未实现，${action}操作暂不可用`)
    } else {
      proxy.$modal.msgError(`${action}失败`)
    }
  }
}

const handleTaskDelete = (row) => {
  proxy.$modal.confirm(`确定删除任务"${row.taskName}"吗？删除后相关目标和批次也将被删除。`).then(async () => {
    try {
      await delTask(row.id)
      proxy.$modal.msgSuccess('删除成功')
      openManageDialog() // 重新加载列表
      emit('refresh') // 刷新卡片统计
    } catch (error) {
      console.error('删除失败:', error)
      if (error.code === 'ERR_BAD_REQUEST' && error.response?.status === 404) {
        proxy.$modal.msgWarning('后端接口未实现，删除操作暂不可用')
      } else {
        proxy.$modal.msgError('删除失败')
      }
    }
  })
}

const openTaskDialog = (type) => {
  taskType.value = type
  dialogVisible.value = true
  loadConfigList(form.chainType)
}

const loadConfigList = (chainType) => {
  listConfig({ chainType, status: 1 }).then(response => {
    configList.value = response.rows || []
  })
}

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      submitting.value = true
      
      if (taskType.value === 'smart') {
        // 创建智能监控任务
        const data = {
          taskName: form.taskName,
          taskType: 'smart',
          chainType: form.chainType,
          minMarketCap: form.minMarketCap,
          maxMarketCap: form.maxMarketCap,
          hasTwitter: form.hasTwitter,
          autoSyncTargets: form.autoSyncTargets ? 1 : 0,
          syncIntervalMinutes: form.syncIntervalMinutes,
          configIds: form.configId ? [form.configId] : [], // 转换为数组
          description: form.description,
          status: 1
        }
        
        addSmartTask(data).then(response => {
          proxy.$modal.msgSuccess('智能监控任务创建成功，正在同步目标...')
          dialogVisible.value = false
          emit('refresh')
        }).finally(() => {
          submitting.value = false
        })
      } else if (taskType.value === 'batch') {
        // 创建批量监控任务
        const caArray = form.caList.split('\n')
          .map(line => line.trim())
          .filter(line => line)
          .slice(0, 99) // 限制最多99个
        
        const data = {
          taskName: form.taskName,
          taskType: 'batch',
          chainType: form.chainType,
          configIds: form.configId ? [form.configId] : [], // 转换为数组
          targetList: caArray,
          description: form.description,
          status: 1
        }
        
        addBatchTask(data).then(response => {
          proxy.$modal.msgSuccess(`批量监控任务创建成功，已添加 ${caArray.length} 个目标`)
          dialogVisible.value = false
          emit('refresh')
        }).finally(() => {
          submitting.value = false
        })
      } else if (taskType.value === 'block') {
        // 创建区块监控任务
        const data = {
          taskName: form.taskName,
          taskType: 'block',
          chainType: form.chainType,
          configIds: form.configId ? [form.configId] : [], // 转换为数组
          description: form.description,
          status: 1
        }
        
        addBlockTask(data).then(response => {
          proxy.$modal.msgSuccess('区块监控任务创建成功')
          dialogVisible.value = false
          emit('refresh')
        }).finally(() => {
          submitting.value = false
        })
      }
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    taskName: '',
    chainType: 'sol',
    minMarketCap: 10000,
    maxMarketCap: null,
    hasTwitter: null,
    autoSyncTargets: true,
    syncIntervalMinutes: 30,
    caList: '',
    configId: null,
    description: ''
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
  color: var(--el-text-color-secondary);
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
  background: var(--el-fill-color-light);
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
  color: var(--el-text-color-regular);
}

.stat-row .value {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
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

/* 弹窗表单样式 */
.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>

