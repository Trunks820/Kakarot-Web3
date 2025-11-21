<!-- 监控任务管理卡片 -->
<template>
  <div class="monitor-card">
    <div class="card-header">
      <div class="card-title">
        <span class="icon">🎯</span>
        <h3>监控任务</h3>
      </div>
      <el-tooltip content="刷新数据（1.5秒内只能刷新一次）" placement="top">
        <el-button 
          size="small" 
          icon="Refresh" 
          :loading="loading"
          :disabled="refreshDisabled"
          circle
          @click="handleRefresh"
        />
      </el-tooltip>
    </div>
    
    <div class="card-body">
      <!-- 同心圆进度环 -->
      <div class="concentric-circles">
        <svg class="circles-svg" viewBox="0 0 200 200">
          <!-- 外层圆（总任务） -->
          <circle cx="100" cy="100" r="90" class="circle-bg outer"></circle>
          <circle 
            cx="100" cy="100" r="90" 
            class="circle-progress outer"
            :style="{ strokeDashoffset: calculateDashOffset('outer') }"
          ></circle>
          
          <!-- 中层圆（运行中） -->
          <circle cx="100" cy="100" r="60" class="circle-bg middle"></circle>
          <circle 
            cx="100" cy="100" r="60" 
            class="circle-progress middle"
            :style="{ strokeDashoffset: calculateDashOffset('middle') }"
          ></circle>
          
          <!-- 内层圆（异常） -->
          <circle cx="100" cy="100" r="30" class="circle-bg inner"></circle>
          <circle 
            cx="100" cy="100" r="30" 
            class="circle-progress inner"
            :class="stats.error > 0 ? 'has-error' : ''"
            :style="{ strokeDashoffset: calculateDashOffset('inner') }"
          ></circle>
        </svg>

        <!-- 中心显示 -->
        <div class="circles-center">
          <div class="center-running">
            <div class="center-value">{{ stats.running || 0 }}</div>
            <div class="center-label">运行中</div>
          </div>
          
          <!-- 状态徽章 -->
          <div class="status-badge" :class="{ 'error-state': stats.error > 0, 'normal-state': stats.error === 0 }">
            <span class="badge-text" v-if="stats.error > 0">{{ stats.error }} 异常</span>
            <span class="badge-text" v-else>✓ 全部正常</span>
          </div>
        </div>
      </div>

      <!-- 右侧统计信息 -->
      <div class="task-stats">
        <!-- 状态指标 -->
        <div class="status-indicators">
          <div class="indicator-box running">
            <div class="indicator-dot"></div>
            <span class="indicator-label">运行</span>
            <span class="indicator-value">{{ stats.running || 0 }}</span>
          </div>
          <div class="indicator-box paused">
            <div class="indicator-dot"></div>
            <span class="indicator-label">暂停</span>
            <span class="indicator-value">{{ stats.paused || 0 }}</span>
          </div>
          <div v-if="stats.error > 0" class="indicator-box error">
            <div class="indicator-dot"></div>
            <span class="indicator-label">异常</span>
            <span class="indicator-value">{{ stats.error }}</span>
          </div>
        </div>

        <!-- 类型分布 -->
        <div class="type-distribution">
          <div class="type-item smart-type">
            <span class="type-name">智能</span>
            <span class="type-count">{{ stats.smart || 0 }}</span>
          </div>
          <div class="type-item batch-type">
            <span class="type-name">批量</span>
            <span class="type-count">{{ stats.batch || 0 }}</span>
          </div>
          <div class="type-item block-type">
            <span class="type-name">区块</span>
            <span class="type-count">{{ stats.block || 0 }}</span>
          </div>
        </div>
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
      :title="`${getDialogTitle}`"
      width="800px"
      append-to-body
      destroy-on-close
      @close="resetForm"
      @keydown.enter="handleSubmit"
      @keydown.esc="dialogVisible = false"
      class="dialog-md task-dialog"
      aria-label="任务编辑对话框"
    >
      <!-- 状态标签 -->
      <div v-if="form.id" class="task-status-badge">
        <el-tag 
          :type="form.status === 1 ? 'success' : 'info'" 
          size="small"
        >
          {{ form.status === 1 ? '启用中' : '已停用' }}
        </el-tag>
      </div>
      
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="dialog-form">
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
            <el-select v-model="form.hasTwitter" placeholder="请选择" clearable style="width: 100%">
              <el-option label="不限" :value="null">
                <span>不限</span>
              </el-option>
              <el-option label="推特主页" value="profile">
                <span>推特主页</span>
              </el-option>
              <el-option label="推文" value="tweet">
                <span>推文</span>
              </el-option>
              <el-option label="社区" value="community">
                <span>社区</span>
              </el-option>
              <el-option label="无推特" value="none" />
            </el-select>
            <div class="form-tip">💡 精确筛选Twitter类型，推特主页通常是官方账号</div>
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
              <el-tag v-if="config.marketType" :type="config.marketType === 'external'
                ? 'success': (config.marketType === 'internal'? 'warning': 'info')" size="small" style="margin-left: 4px">
                      {{
                        config.marketType === 'external'
                            ? '外盘'
                            : (config.marketType === 'internal'
                                ? '内盘'
                                : '不限')
                      }}
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
      destroy-on-close
      class="dialog-lg task-list-dialog"
    >
      <el-table
        v-if="taskList.length > 0"
        v-loading="manageLoading"
        :data="taskList"
        stripe
        style="width: 100%"
        max-height="500px"
        class="dialog-table"
      >
        <el-table-column label="任务ID" prop="id" width="50" align="center" />
        <el-table-column label="任务名称" prop="taskName" width="200" align="center" />
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
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 0 ? 'info' : 'danger'" size="small">
              {{ scope.row.status === 1 ? '运行中' : scope.row.status === 0 ? '已暂停' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="250" align="center" fixed="right">
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
              v-hasPermi="['crypto:monitor-v2:task:edit']"
              text 
              type="warning" 
              size="small" 
              @click="handleTaskEdit(scope.row)"
            >
              编辑
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
      
      <!-- 空态提示 -->
      <div v-else class="dialog-empty">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无任务</div>
        <div class="empty-action">
          <el-button type="primary" size="small" @click="manageDialogVisible = false; openCreateDialog('smart')">
            创建第一个任务
          </el-button>
        </div>
      </div>
      
      <template #footer class="dialog-footer">
        <el-button @click="manageDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="任务详情"
      width="800px"
      append-to-body
      destroy-on-close
      class="dialog-md task-detail-dialog"
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
            <el-descriptions-item label="Twitter筛选">
              <el-tag v-if="taskDetail.hasTwitter === 'profile'" type="success">推特主页</el-tag>
              <el-tag v-else-if="taskDetail.hasTwitter === 'tweet'" type="warning">推文</el-tag>
              <el-tag v-else-if="taskDetail.hasTwitter === 'community'" type="info">社区</el-tag>
              <el-tag v-else-if="taskDetail.hasTwitter === 'none'" type="danger">无推特</el-tag>
              <el-tag v-else type="">不限</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="自动同步">
              {{ taskDetail.autoSyncTargets === 1 ? '是' : '否' }}
            </el-descriptions-item>
            <el-descriptions-item label="同步间隔" v-if="taskDetail.autoSyncTargets === 1">
              {{ taskDetail.syncIntervalMinutes }}分钟
            </el-descriptions-item>
            <el-descriptions-item label="批次数量">
              {{ taskDetail.batchCount || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="最后运行时间">
              {{ taskDetail.lastRunTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="下次运行时间">
              {{ taskDetail.nextRunTime || '-' }}
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
import { addSmartTask, addBatchTask, addBlockTask, listTask, updateTask, delTask, startTask, stopTask } from '@/api/crypto/monitor-v2'
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

// 防止高频刷新
const refreshDisabled = ref(false)
const handleRefresh = () => {
  if (refreshDisabled.value) return
  emit('refresh')
  refreshDisabled.value = true
  setTimeout(() => {
    refreshDisabled.value = false
  }, 1500)
}

const dialogVisible = ref(false)
const submitting = ref(false)
const taskType = ref('smart')
const formRef = ref(null)
const configList = ref([])

// 计算同心圆进度条的偏移值
const calculateDashOffset = (layer) => {
  let percent = 0
  let circumference = 0
  
  const total = props.stats.total || 0
  const running = props.stats.running || 0
  const error = props.stats.error || 0
  
  console.log(`【${layer} 层圆】 total=${total}, running=${running}, error=${error}`)
  
  if (layer === 'outer') {
    // 外层圆：总任务数 (100%)
    percent = total > 0 ? 1 : 0
    circumference = 2 * Math.PI * 90
  } else if (layer === 'middle') {
    // 中层圆：运行中 / 总任务
    percent = total > 0 ? running / total : 0
    circumference = 2 * Math.PI * 60
  } else if (layer === 'inner') {
    // 内层圆：异常 / 运行中
    percent = running > 0 ? error / running : 0
    circumference = 2 * Math.PI * 30
  }
  
  console.log(`【${layer} 层圆】 percent=${percent}, dashOffset=${circumference * (1 - Math.max(0, Math.min(1, percent)))}`)
  
  return circumference * (1 - Math.max(0, Math.min(1, percent)))
}

// 任务列表管理弹窗
const manageDialogVisible = ref(false)
const manageLoading = ref(false)
const taskList = ref([])

const form = reactive({
  id: null, // ⭐ 新增：任务ID（编辑时使用）
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
  configId: null, // 配置ID
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
  const isEdit = !!form.id
  const titles = {
    'smart': isEdit ? '编辑智能监控任务' : '新建智能监控任务',
    'batch': isEdit ? '编辑批量监控任务' : '新建批量监控任务',
    'block': isEdit ? '编辑区块监控任务' : '新建区块监控任务'
  }
  return titles[taskType.value] || (isEdit ? '编辑任务' : '新建任务')
})

// 计算CA数量
const caCount = computed(() => {
  if (!form.caList) return 0
  return form.caList.split('\,').filter(line => line.trim()).length
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

// ⭐ 新增：编辑任务
const handleTaskEdit = (row) => {
  console.log('编辑任务:', row)
  
  // 设置任务类型
  taskType.value = row.taskType
  
  // 填充表单数据
  form.id = row.id  // ⭐ 设置ID表示编辑模式
  form.taskName = row.taskName
  form.chainType = row.chainType
  form.description = row.description || ''
  
  // 根据任务类型填充特定字段
  if (row.taskType === 'smart') {
    form.minMarketCap = row.minMarketCap || 10000
    form.maxMarketCap = row.maxMarketCap || null
    form.hasTwitter = row.hasTwitter
    form.autoSyncTargets = row.autoSyncTargets === 1
    form.syncIntervalMinutes = row.syncIntervalMinutes || 30
  } else if (row.taskType === 'batch') {
    // 批量任务的CA列表（如果有）
    form.caList = row.targetList.toString()
    form.caCount = row.targetCount
  }
  
  // 设置配置ID（从configs数组中获取第一个）
  if (row.configIds && row.configIds.length > 0) {
    form.configId = row.configIds[0]
  } else {
    form.configId = null
  }
  
  // 打开弹窗
  dialogVisible.value = true
  loadConfigList(form.chainType)
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
        // 智能监控任务
        if(form.maxMarketCap === '' || form.maxMarketCap === null) {
          form.maxMarketCap = 0
        }
        const data = {
          id: form.id, // ⭐ 编辑时需要ID
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
        
        // ⭐ 判断是编辑还是新增
        const apiCall = form.id ? updateTask(data) : addSmartTask(data)
        const successMsg = form.id ? '任务修改成功' : '智能监控任务创建成功，正在同步目标...'
        
        apiCall.then(response => {
          proxy.$modal.msgSuccess(successMsg)
          dialogVisible.value = false
          emit('refresh')
          openManageDialog() // 刷新任务列表
        }).finally(() => {
          submitting.value = false
        })
      } else if (taskType.value === 'batch') {
        // 批量监控任务
        const caArray = form.caList.split('\,')
          .map(line => line.trim())
          .filter(line => line)
          .slice(0, 99) // 限制最多99个

        const targetCount = caArray.length
        
        const data = {
          id: form.id, // ⭐ 编辑时需要ID
          taskName: form.taskName,
          taskType: 'batch',
          chainType: form.chainType,
          configIds: form.configId ? [form.configId] : [], // 转换为数组
          targetList: caArray,
          targetCount: targetCount,
          description: form.description,
          status: 1
        }

        // ⭐ 判断是编辑还是新增
        const apiCall = form.id ? updateTask(data) : addBatchTask(data)
        const successMsg = form.id ? '任务修改成功' : `批量监控任务创建成功，已添加 ${caArray.length} 个目标`
        
        apiCall.then(response => {
          proxy.$modal.msgSuccess(successMsg)
          dialogVisible.value = false
          emit('refresh')
          openManageDialog() // 刷新任务列表
        }).finally(() => {
          submitting.value = false
        })
      } else if (taskType.value === 'block') {
        // 区块监控任务
        const data = {
          id: form.id, // ⭐ 编辑时需要ID
          taskName: form.taskName,
          taskType: 'block',
          chainType: form.chainType,
          configIds: form.configId ? [form.configId] : [], // 转换为数组
          description: form.description,
          status: 1
        }
        
        // ⭐ 判断是编辑还是新增
        const apiCall = form.id ? updateTask(data) : addBlockTask(data)
        const successMsg = form.id ? '任务修改成功' : '区块监控任务创建成功'
        
        apiCall.then(response => {
          proxy.$modal.msgSuccess(successMsg)
          dialogVisible.value = false
          emit('refresh')
          openManageDialog() // 刷新任务列表
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
    id: null, // ⭐ 重置ID
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

/* 同心圆进度环 */
.concentric-circles {
  position: relative;
  width: 200px;
  height: 200px;
  margin-bottom: 12px;
}

.circles-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 8px rgba(0,0,0,0.1));
}

.circle-bg {
  fill: none;
  stroke: var(--el-fill-color);
  stroke-width: 14;
}

.circle-progress {
  fill: none;
  stroke-width: 14;
  stroke-linecap: round;
  transform: rotate(-90deg);
  transform-origin: 50%;  /* 改为相对值，更稳定 */
  transition: stroke-dashoffset 0.8s cubic-bezier(0.4, 0.0, 0.2, 1);
}

.circle-progress.outer {
  stroke: #67C23A;
  stroke-dasharray: 565.49; /* 2*PI*90 */
  filter: drop-shadow(0 0 6px rgba(103, 194, 58, 0.3));
}

.circle-progress.middle {
  stroke: #409EFF;
  stroke-dasharray: 376.99; /* 2*PI*60 */
  filter: drop-shadow(0 0 6px rgba(64, 158, 255, 0.3));
}

.circle-progress.inner {
  stroke: #E6A23C;
  stroke-dasharray: 188.50; /* 2*PI*30 */
  filter: drop-shadow(0 0 6px rgba(230, 162, 60, 0.3));
}

.circle-progress.inner.has-error {
  stroke: #F56C6C;
  filter: drop-shadow(0 0 8px rgba(245, 108, 108, 0.5));
  animation: pulse-error 1.5s ease-in-out infinite;
}

@keyframes pulse-error {
  0%, 100% { filter: drop-shadow(0 0 8px rgba(245, 108, 108, 0.5)); }
  50% { filter: drop-shadow(0 0 12px rgba(245, 108, 108, 0.8)); }
}

/* 中心显示 */
.circles-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  background: var(--el-bg-color);
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 2px 4px rgba(0,0,0,0.05);
}

.center-running {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.center-value {
  font-size: 26px;
  font-weight: 700;
  color: #67C23A;
  line-height: 1;
  margin-bottom: 2px;
}

.center-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* 错误徽章 */
.status-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: 700;
  transition: all 0.3s;
  
  &.error-state {
    background: #F56C6C;
    color: #fff;
    box-shadow: 0 2px 4px rgba(245, 108, 108, 0.3);
  }
  
  &.normal-state {
    background: #67C23A;
    color: #fff;
    box-shadow: 0 2px 4px rgba(103, 194, 58, 0.3);
  }
}

.badge-text {
  white-space: nowrap;
}

/* 右侧统计 */
.task-stats {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 状态指标 */
.status-indicators {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.indicator-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 10px;
  border-radius: 6px;
  background: var(--el-fill-color-light);
  transition: all 0.3s;
  min-width: 50px;
}

.indicator-box:hover {
  transform: translateY(-2px);
}

.indicator-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.indicator-box.running .indicator-dot {
  background: #67C23A;
  box-shadow: 0 0 6px rgba(103, 194, 58, 0.6);
}

.indicator-box.paused .indicator-dot {
  background: #E6A23C;
}

.indicator-box.error .indicator-dot {
  background: #F56C6C;
  box-shadow: 0 0 6px rgba(245, 108, 108, 0.6);
}

.indicator-label {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.indicator-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.indicator-box.running .indicator-value {
  color: #67C23A;
}

.indicator-box.paused .indicator-value {
  color: #E6A23C;
}

.indicator-box.error .indicator-value {
  color: #F56C6C;
}

/* 类型分布 */
.type-distribution {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 6px 8px;
  border-radius: 4px;
  font-size: 11px;
  transition: all 0.3s;
}

.type-item:hover {
  transform: scale(1.05);
}

.type-name {
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.type-count {
  font-weight: 700;
  font-size: 13px;
}

.smart-type {
  background: rgba(103, 194, 58, 0.1);
  border: 1px solid rgba(103, 194, 58, 0.3);
}

.smart-type .type-count {
  color: #67C23A;
}

.batch-type {
  background: rgba(64, 158, 255, 0.1);
  border: 1px solid rgba(64, 158, 255, 0.3);
}

.batch-type .type-count {
  color: #409EFF;
}

.block-type {
  background: rgba(230, 162, 60, 0.1);
  border: 1px solid rgba(230, 162, 60, 0.3);
}

.block-type .type-count {
  color: #E6A23C;
}

.type-stats {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.card-footer {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-light);
}

.card-footer .el-button,
.card-footer .el-dropdown {
  flex: 1;
  height: 32px;
}

/* 弹窗表单样式 */
.form-tip {
  margin-left: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* TaskCard 弹窗样式 */
.task-dialog {
  :deep(.el-dialog__body) {
    position: relative;
    
    .task-status-badge {
      position: absolute;
      top: 0;
      right: 0;
      padding: 0;
      margin-bottom: 16px;
    }
  }
  
  :deep(.dialog-form) {
    margin-top: 12px;
  }
}

.task-list-dialog,
.task-detail-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  :deep(.dialog-footer) {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
    padding: 12px 20px;
    border-top: 1px solid var(--el-border-color-light);
    margin: 0 -20px -20px;
    background: var(--el-fill-color-light);
  }
}
</style>

