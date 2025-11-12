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
      <el-button 
        v-hasPermi="['crypto:monitor-v2:config:add']"
        type="primary" 
        icon="Plus" 
        @click="openCreateDialog"
      >
        新建配置
      </el-button>
      <el-button 
        v-hasPermi="['crypto:monitor-v2:config:list']"
        icon="List" 
        @click="openManageDialog"
      >
        管理配置
      </el-button>
    </div>

    <!-- 新建配置弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑监控配置' : '新建监控配置'"
      width="700px"
      append-to-body
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <!-- 基础信息 -->
        <el-divider content-position="left">
          <span style="font-weight: 600;">📋 基础信息</span>
        </el-divider>
        
        <el-form-item label="配置名称" prop="configName">
          <el-input 
            v-model="form.configName" 
            placeholder="如：SOL链价格暴涨预警" 
            maxlength="100"
          />
        </el-form-item>
        
        <el-form-item label="链类型" prop="chainType">
          <el-radio-group v-model="form.chainType">
            <el-radio-button label="sol">Solana</el-radio-button>
            <el-radio-button label="bsc">BSC</el-radio-button>
            <el-radio-button label="eth">Ethereum</el-radio-button>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="市场类型">
          <el-radio-group v-model="form.marketType">
            <el-radio-button label="external">🌍 外盘</el-radio-button>
            <el-radio-button label="internal">🏠 内盘</el-radio-button>
            <el-radio-button label="all">不限</el-radio-button>
          </el-radio-group>
          <div class="form-tip">💡 限制只监控特定市场类型的Token</div>
        </el-form-item>
        
        <el-form-item label="时间周期" prop="timeInterval">
          <el-radio-group v-model="form.timeInterval">
            <el-radio-button label="1m">1分钟</el-radio-button>
            <el-radio-button label="5m">5分钟</el-radio-button>
            <el-radio-button label="1h">1小时</el-radio-button>
            <el-radio-button label="24h">24小时</el-radio-button>
          </el-radio-group>
          <div class="form-tip">💡 监控API选择该时间段的交易量和涨跌幅</div>
        </el-form-item>
        
        <!-- 监控事件配置 -->
        <el-divider content-position="left">
          <span style="font-weight: 600;">⚙️ 监控事件</span>
        </el-divider>

        <!-- 价格监控事件卡片 -->
        <el-card class="event-card" :class="{ disabled: !events.priceChange.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="events.priceChange.enabled">
                📈 涨跌幅变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="events.priceChange.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="涨幅阈值" label-position="top" class="event-field">
                  <el-input-number
                    v-model="events.priceChange.risePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                    placeholder="50"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="跌幅阈值" label-position="top" class="event-field">
                  <el-input-number
                    v-model="events.priceChange.fallPercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                    placeholder="30"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>

        <!-- 持币人数变化事件卡片 -->
        <el-card class="event-card" :class="{ disabled: !events.holders.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="events.holders.enabled">
                👥 持币人数变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="events.holders.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="增长阈值" label-position="top" class="event-field">
                  <el-input-number
                    v-model="events.holders.increasePercent"
                    :min="0"
                    :max="100"
                    :precision="1"
                    style="width: 100%"
                    placeholder="100"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="减少阈值" label-position="top" class="event-field">
                  <el-input-number
                    v-model="events.holders.decreasePercent"
                    :min="0"
                    :max="100"
                    :precision="1"
                    style="width: 100%"
                    placeholder="50"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>

        <!-- 交易量监控事件卡片 -->
        <el-card class="event-card" :class="{ disabled: !events.volume.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="events.volume.enabled">
                💰 交易量阈值
              </el-checkbox>
            </div>
          </template>
          <div v-if="events.volume.enabled" class="event-config">
            
            <el-form-item label="交易量阈值" label-position="top" class="event-field">
              <el-input-number
                v-model="events.volume.threshold"
                :min="0"
                :step="1000"
                :precision="0"
                style="width: 100%"
                placeholder="5000"
              >
                <template #suffix>USD</template>
              </el-input-number>
            </el-form-item>
            
            <div class="event-tip">💡 交易量超过阈值时触发通知</div>
          </div>
        </el-card>

        <!-- 前十持仓过滤 -->
        <el-form-item label="前十持仓过滤" style="margin-top: 16px;">
          <el-input-number
            v-model="form.topHoldersThreshold"
            :min="0"
            :max="100"
            :precision="1"
            style="width: 200px"
            placeholder="可选"
          >
            <template #suffix>%</template>
          </el-input-number>
          <div class="form-tip">💡 前十持仓超过该百分比不播报（可选）</div>
        </el-form-item>
        
        <!-- 触发与通知 -->
        <el-divider content-position="left">
          <span style="font-weight: 600;">🔔 触发设置</span>
        </el-divider>
        
        <el-form-item label="触发逻辑" prop="triggerLogic">
          <el-radio-group v-model="form.triggerLogic">
            <el-radio label="any">
              任一条件满足即触发
              <span style="color: #909399; font-size: 12px;">（OR逻辑）</span>
            </el-radio>
            <el-radio label="all">
              需同时满足所有已勾选条件
              <span style="color: #909399; font-size: 12px;">（AND逻辑）</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="通知方式" prop="notifyMethods">
          <el-checkbox-group v-model="form.notifyMethods">
            <el-checkbox label="telegram">📱 Telegram</el-checkbox>
            <el-checkbox label="wechat">💬 微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="配置描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <!-- 当前监控条件预览 -->
      <el-alert 
        v-if="configConditionsSummary"
        :title="configConditionsSummary" 
        type="info" 
        :closable="false"
        class="monitor-preview"
      >
        <template #title>
          <div class="preview-title">📋 当前监控条件</div>
          <div class="preview-content">{{ configConditionsSummary }}</div>
        </template>
      </el-alert>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定创建
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 管理配置列表弹窗 -->
    <el-dialog
      v-model="manageDialogVisible"
      title="配置列表"
      width="900px"
      append-to-body
    >
      <el-table
        v-loading="manageLoading"
        :data="configList"
        stripe
        style="width: 100%"
        max-height="500px"
      >
        <el-table-column label="配置名称" prop="configName" width="200" show-overflow-tooltip />
        <el-table-column label="链类型" prop="chainType" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.chainType === 'sol' ? 'success' : 'warning'" size="small">
              {{ scope.row.chainType.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="市场类型" prop="marketType" width="90" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.marketType === 'external'" type="primary" size="small">外盘</el-tag>
            <el-tag v-else-if="scope.row.marketType === 'internal'" type="info" size="small">内盘</el-tag>
            <span v-else style="color: #909399;">不限</span>
          </template>
        </el-table-column>
        <el-table-column label="时间周期" prop="timeInterval" width="90" align="center" />
        <el-table-column label="触发逻辑" prop="triggerLogic" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.triggerLogic === 'any' ? 'warning' : 'success'" size="small">
              {{ scope.row.triggerLogic === 'any' ? '任一条件' : '所有条件' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="通知方式" prop="notifyMethods" width="120">
          <template #default="scope">
            <el-tag v-for="method in scope.row.notifyMethods?.split(',')" :key="method" size="small" style="margin-right: 4px;">
              {{ method === 'telegram' ? 'TG' : method === 'wechat' ? '微信' : method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-hasPermi="['crypto:monitor-v2:config:query']"
              text 
              type="primary" 
              size="small" 
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:config:edit']"
              text 
              type="primary" 
              size="small" 
              @click="handleEdit(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:config:remove']"
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
      
      <template #footer>
        <el-button @click="manageDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 配置详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="配置详情"
      width="800px"
      append-to-body
    >
      <el-descriptions v-if="configDetail" :column="2" border>
        <el-descriptions-item label="配置ID">{{ configDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="配置名称">{{ configDetail.configName }}</el-descriptions-item>
        <el-descriptions-item label="链类型">
          <el-tag size="small">{{ configDetail.chainType?.toUpperCase() }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间周期">{{ configDetail.timeInterval }}</el-descriptions-item>
        <el-descriptions-item label="市场类型">
          <el-tag v-if="configDetail.marketType === 'external'" size="small">🌍 外盘</el-tag>
          <el-tag v-else-if="configDetail.marketType === 'internal'" size="small">🏠 内盘</el-tag>
          <el-tag v-else size="small" type="info">不限</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="configDetail.status === 1 ? 'success' : 'info'" size="small">
            {{ configDetail.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ configDetail.createTime }}</el-descriptions-item>
        <el-descriptions-item label="更新时间" :span="2">{{ configDetail.updateTime }}</el-descriptions-item>
        <el-descriptions-item label="监控规则" :span="2">
          <div v-if="configDetail.eventsConfig">
            <el-tag 
              v-for="(event, index) in parseEventsConfig(configDetail.eventsConfig)" 
              :key="index"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ event }}
            </el-tag>
          </div>
          <span v-else style="color: #909399;">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ configDetail.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-hasPermi="['crypto:monitor-v2:config:edit']"
          type="primary" 
          @click="handleEdit(configDetail)"
        >
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, getCurrentInstance } from 'vue'
import { addConfig, listConfig, delConfig, updateConfig } from '@/api/crypto/monitor-v2'
import { 
  Star, Coin, TrendCharts, UserFilled, DataAnalysis, Bell 
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

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
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 管理配置列表弹窗
const manageDialogVisible = ref(false)
const manageLoading = ref(false)
const configList = ref([])

const form = reactive({
  configName: '',
  chainType: 'sol',
  marketType: null,
  timeInterval: '5m',
  // 交易量监控字段
  minTransactionUsd: null,
  cumulativeMinAmountUsd: null,
  // 持仓过滤
  topHoldersThreshold: null,
  // 触发与通知
  triggerLogic: 'any',
  notifyMethods: ['telegram'],
  description: ''
})

// 事件配置（独立的响应式对象）
const events = reactive({
  priceChange: {
    enabled: false,
    risePercent: null,
    fallPercent: null
  },
  holders: {
    enabled: false,
    increasePercent: null,
    decreasePercent: null
  },
  volume: {
    enabled: false,
    threshold: null
  }
})

const rules = {
  configName: [
    { required: true, message: '请输入配置名称', trigger: 'blur' }
  ],
  chainType: [
    { required: true, message: '请选择链类型', trigger: 'change' }
  ],
  timeInterval: [
    { required: true, message: '请选择时间周期', trigger: 'change' }
  ],
  triggerLogic: [
    { required: true, message: '请选择触发逻辑', trigger: 'change' }
  ],
  notifyMethods: [
    { type: 'array', required: true, message: '请至少选择一种通知方式', trigger: 'change' }
  ]
}

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

// 监控条件摘要（实时计算）
const configConditionsSummary = computed(() => {
  const conditions = []
  const { priceChange, holders, volume } = events
  const triggerLogicText = form.triggerLogic === 'any' ? '任一条件' : '所有条件'
  
  if (priceChange.enabled) {
    const parts = []
    if (priceChange.risePercent) parts.push(`涨幅≥${priceChange.risePercent}%`)
    if (priceChange.fallPercent) parts.push(`跌幅≥${priceChange.fallPercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  if (holders.enabled) {
    const parts = []
    if (holders.increasePercent) parts.push(`持币人数增长≥${holders.increasePercent}%`)
    if (holders.decreasePercent) parts.push(`持币人数减少≥${holders.decreasePercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  if (volume.enabled) {
    if (volume.threshold) conditions.push(`交易量≥$${volume.threshold}`)
  }
  
  if (conditions.length === 0) return ''
  
  return `${triggerLogicText}：${conditions.join(form.triggerLogic === 'any' ? ' 或 ' : ' 且 ')}`
})

// 监听事件启用，自动填充默认值
watch(() => events.priceChange.enabled, (newVal) => {
  if (newVal && !events.priceChange.risePercent && !events.priceChange.fallPercent) {
    events.priceChange.risePercent = 50
    events.priceChange.fallPercent = 30
  }
})

watch(() => events.holders.enabled, (newVal) => {
  if (newVal && !events.holders.increasePercent && !events.holders.decreasePercent) {
    events.holders.increasePercent = 100
    events.holders.decreasePercent = 50
  }
})

watch(() => events.volume.enabled, (newVal) => {
  if (newVal && !events.volume.threshold) {
    events.volume.threshold = 5000
  }
})

const openCreateDialog = () => {
  dialogVisible.value = true
}

const openManageDialog = async () => {
  console.log('打开管理配置弹窗')
  manageDialogVisible.value = true
  manageLoading.value = true
  try {
    const response = await listConfig({ pageNum: 1, pageSize: 100 })
    console.log('配置列表响应:', response)
    configList.value = response.rows || []
    console.log('配置列表数据:', configList.value)
  } catch (error) {
    console.error('加载配置列表失败:', error)
    
    // 临时：如果后端接口未实现(404)，使用Mock数据
    if (error.code === 'ERR_BAD_REQUEST' && error.response?.status === 404) {
      console.warn('⚠️ 后端接口未实现，使用Mock数据')
      configList.value = [
        {
          id: 1,
          configName: 'SOL链价格暴涨预警',
          chainType: 'sol',
          marketType: 'external',
          timeInterval: '5m',
          topHoldersThreshold: 50,
          eventsConfig: '{"priceChange":{"enabled":true,"risePercent":50,"fallPercent":30}}',
          triggerLogic: 'any',
          notifyMethods: 'telegram,wechat',
          status: 1,
          description: '监控SOL链外盘价格异常波动',
          createTime: '2025-11-09 10:00:00'
        },
        {
          id: 2,
          configName: 'BSC链大额交易监控',
          chainType: 'bsc',
          marketType: 'internal',
          timeInterval: '1h',
          topHoldersThreshold: null,
          eventsConfig: '{"volume":{"enabled":true,"threshold":10000}}',
          triggerLogic: 'all',
          notifyMethods: 'telegram',
          status: 1,
          description: '监控BSC链内盘大额交易',
          createTime: '2025-11-09 11:30:00'
        },
        {
          id: 3,
          configName: 'ETH链持仓变化预警',
          chainType: 'eth',
          marketType: null,
          timeInterval: '15m',
          topHoldersThreshold: 40,
          eventsConfig: '{"holders":{"enabled":true,"increasePercent":100,"decreasePercent":50}}',
          triggerLogic: 'any',
          notifyMethods: 'wechat',
          status: 0,
          description: '监控前十持仓变化',
          createTime: '2025-11-08 15:20:00'
        }
      ]
      proxy.$modal.msgWarning('后端接口未实现，显示Mock数据')
    } else {
      proxy.$modal.msgError('加载配置列表失败: ' + (error.message || ''))
    }
  } finally {
    manageLoading.value = false
  }
}

// 详情弹窗
const detailDialogVisible = ref(false)
const configDetail = ref(null)

// 查看详情
const handleDetail = (row) => {
  console.log('查看配置详情:', row)
  configDetail.value = row
  detailDialogVisible.value = true
}

// 解析监控规则为可读文本
const parseEventsConfig = (eventsConfigStr) => {
  try {
    const events = JSON.parse(eventsConfigStr)
    const rules = []
    
    if (events.priceChange?.enabled) {
      if(events.priceChange.risePercent){
        rules.push(`价格监控: 涨幅${events.priceChange.risePercent}%`)
      }
      if(events.priceChange.fallPercent){
        rules.push(`价格监控: 跌幅${events.priceChange.fallPercent}%`)
      }
    }
    if (events.holders?.enabled) {
      if(events.holders.decreasePercent){
        rules.push(`持仓人数跌幅: ${events.holders.decreasePercent}%`)
      }
      if(events.holders.increasePercent){
        rules.push(`持仓人数涨幅: ${events.holders.increasePercent}%`)
      }
    }
    if (events.volume?.enabled) {
      rules.push(`交易量监控: ${events.volume.threshold}`)
    }
    
    return rules.length > 0 ? rules : ['无监控规则']
  } catch (e) {
    return ['解析失败']
  }
}

const handleEdit = async (row) => {
  try {
    console.log('编辑配置:', row)
    
    // 解析eventsConfig JSON
    let eventsData = {}
    if (row.eventsConfig) {
      try {
        eventsData = JSON.parse(row.eventsConfig)
      } catch (e) {
        console.error('解析eventsConfig失败:', e)
      }
    }
    
    // 填充表单数据
    Object.assign(form, {
      id: row.id,
      configName: row.configName,
      chainType: row.chainType,
      marketType: row.marketType,
      timeInterval: row.timeInterval,
      topHoldersThreshold: row.topHoldersThreshold,
      triggerLogic: row.triggerLogic,
      notifyMethods: row.notifyMethods ? row.notifyMethods.split(',') : [],
      description: row.description
    })
    
    // 填充事件配置
    if (eventsData.priceChange.enabled) {
      events.priceChange = {
        enabled: true,
        risePercent: eventsData.priceChange.risePercent || null,
        fallPercent: eventsData.priceChange.fallPercent || null
      }
    } else {
      events.priceChange = { enabled: false, risePercent: null, fallPercent: null }
    }
    
    if (eventsData.holders.enabled) {
      events.holders = {
        enabled: true,
        increasePercent: eventsData.holders.increasePercent || null,
        decreasePercent: eventsData.holders.decreasePercent || null
      }
    } else {
      events.holders = { enabled: false, increasePercent: null, decreasePercent: null }
    }
    
    if (eventsData.volume.enabled) {
      events.volume = {
        enabled: true,
        threshold: eventsData.volume.threshold || null
      }
    } else {
      events.volume = { enabled: false, threshold: null }
    }
    
    // 关闭管理弹窗，打开编辑弹窗
    manageDialogVisible.value = false
    dialogVisible.value = true
  } catch (error) {
    console.error('加载配置失败:', error)
    proxy.$modal.msgError('加载配置失败')
  }
}

const handleDelete = (row) => {
  proxy.$modal.confirm(`确定删除配置"${row.configName}"吗？`).then(async () => {
    try {
      await delConfig(row.id)
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

const handleSubmit = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      // 验证至少启用一个监控事件
      const hasEnabledEvent = Object.values(events).some(e => e.enabled)
      if (!hasEnabledEvent) {
        proxy.$modal.msgWarning('请至少启用一个监控事件')
        return
      }
      
      // 验证每个启用的事件至少有一个阈值
      for (const [key, event] of Object.entries(events)) {
        if (event.enabled) {
          const hasThreshold = Object.values(event)
            .filter(v => typeof v === 'number')
            .some(v => v !== null && v !== undefined)
          
          if (!hasThreshold) {
            const eventNames = {
              priceChange: '涨跌幅变化',
              holders: '持币人数变化',
              volume: '交易量变化'
            }
            proxy.$modal.msgWarning(`${eventNames[key]}至少需要设置一个阈值`)
            return
          }
        }
      }
      
      submitting.value = true
      
      // 组装请求数据
      const data = {
        configName: form.configName,
        chainType: form.chainType,
        marketType: form.marketType,
        timeInterval: form.timeInterval,
        minTransactionUsd: form.minTransactionUsd,
        cumulativeMinAmountUsd: form.cumulativeMinAmountUsd,
        topHoldersThreshold: form.topHoldersThreshold,
        eventsConfig: JSON.stringify(events),
        triggerLogic: form.triggerLogic,
        notifyMethods: form.notifyMethods.join(','),
        description: form.description,
        status: 1
      }
      
      // 判断是新增还是编辑
      const isEdit = !!form.id
      if (isEdit) {
        data.id = form.id
      }
      
      const apiCall = isEdit ? updateConfig(data) : addConfig(data)
      
      apiCall.then(response => {
        proxy.$modal.msgSuccess(isEdit ? '更新成功' : '创建成功')
        dialogVisible.value = false
        emit('refresh')
        // 如果是从管理弹窗编辑的，重新加载管理列表
        if (isEdit) {
          openManageDialog()
        }
      }).finally(() => {
        submitting.value = false
      })
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: null,
    configName: '',
    chainType: 'sol',
    marketType: null,
    timeInterval: '5m',
    minTransactionUsd: null,
    cumulativeMinAmountUsd: null,
    topHoldersThreshold: null,
    triggerLogic: 'any',
    notifyMethods: ['telegram'],
    description: ''
  })
  
  // 重置事件配置
  Object.assign(events, {
    priceChange: { enabled: false, risePercent: null, fallPercent: null },
    holders: { enabled: false, increasePercent: null, decreasePercent: null },
    volume: { enabled: false, threshold: null }
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
  color: #409EFF;
  line-height: 1;
}

.count-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
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
  background: var(--el-fill-color-light);
  border-radius: 6px;
}

.stat-item .label {
  display: block;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 4px;
}

.stat-item .value {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.last-update {
  font-size: 12px;
  color: var(--el-text-color-secondary);
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

/* 事件卡片样式 */
.event-card {
  margin-bottom: 16px;
  border: 1px solid #DCDFE6;
  transition: all 0.3s ease;
}

.event-card:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
}

.event-card.disabled {
  opacity: 0.6;
  background: var(--el-fill-color-light);
}

.event-card :deep(.el-card__header) {
  padding: 12px 16px;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.event-title {
  font-weight: 500;
  font-size: 14px;
}

.event-config {
  padding-top: 8px;
}

.event-field {
  margin-bottom: 16px;
}

.event-field:last-child {
  margin-bottom: 0;
}

.event-tip {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  line-height: 1.5;
}

/* 监控条件预览样式 */
.monitor-preview {
  margin-top: 20px;
}

.preview-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #409EFF;
  font-size: 14px;
}

.preview-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}
</style>

