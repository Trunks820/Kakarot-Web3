<!-- 配置表单弹窗组件（统一的新建/编辑配置弹窗） -->
<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="700px"
    append-to-body
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <!-- 基础信息 -->
      <el-divider content-position="left">
        <span style="font-weight: 600;">📋 基础信息</span>
      </el-divider>
      
      <el-form-item label="配置名称" prop="configName">
        <el-input 
          v-model="formData.configName" 
          placeholder="如：SOL链价格暴涨预警" 
          maxlength="100"
        />
      </el-form-item>
      
      <el-form-item label="链类型" prop="chainType">
        <el-radio-group v-model="formData.chainType">
          <el-radio-button label="sol">Solana</el-radio-button>
          <el-radio-button label="bsc">BSC</el-radio-button>
          <el-radio-button label="eth">Ethereum</el-radio-button>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="市场类型">
        <el-radio-group v-model="formData.marketType">
          <el-radio-button label="external">🌍 外盘</el-radio-button>
          <el-radio-button label="internal">🏠 内盘</el-radio-button>
          <el-radio-button label="all">不限</el-radio-button>
        </el-radio-group>
        <div class="form-tip">💡 限制只监控特定市场类型的Token</div>
      </el-form-item>
      
      <el-form-item label="时间周期" prop="timeInterval">
        <el-radio-group v-model="formData.timeInterval">
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
          v-model="formData.topHoldersThreshold"
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
        <el-radio-group v-model="formData.triggerLogic">
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
        <el-checkbox-group v-model="formData.notifyMethods">
          <el-checkbox label="telegram">📱 Telegram</el-checkbox>
          <el-checkbox label="wechat">💬 微信</el-checkbox>
        </el-checkbox-group>
      </el-form-item>
      
      <el-form-item label="配置描述">
        <el-input
          v-model="formData.description"
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
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '确认修改' : '确定创建' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  configData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

// 弹窗可见性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = ref({
  id: null,
  configName: '',
  chainType: 'sol',
  marketType: null,
  timeInterval: '5m',
  topHoldersThreshold: null,
  triggerLogic: 'any',
  notifyMethods: ['telegram'],
  description: '',
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

// 是否编辑模式
const isEdit = computed(() => !!formData.value.id)

// 弹窗标题
const dialogTitle = computed(() => {
  return isEdit.value ? '编辑监控配置' : '新建监控配置'
})

// 监控条件摘要（实时计算）
const configConditionsSummary = computed(() => {
  const conditions = []
  
  if (events.priceChange.enabled) {
    const parts = []
    if (events.priceChange.risePercent) parts.push(`涨幅>${events.priceChange.risePercent}%`)
    if (events.priceChange.fallPercent) parts.push(`跌幅>${events.priceChange.fallPercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  if (events.holders.enabled) {
    const parts = []
    if (events.holders.increasePercent) parts.push(`持币人增长>${events.holders.increasePercent}%`)
    if (events.holders.decreasePercent) parts.push(`持币人减少>${events.holders.decreasePercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  if (events.volume.enabled && events.volume.threshold) {
    conditions.push(`交易量>${events.volume.threshold} USD`)
  }
  
  if (conditions.length === 0) return null
  
  const logic = formData.value.triggerLogic === 'any' ? ' 或 ' : ' 且 '
  return conditions.join(logic)
})

// 表单验证规则
const formRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  chainType: [{ required: true, message: '请选择链类型', trigger: 'change' }],
  timeInterval: [{ required: true, message: '请选择时间周期', trigger: 'change' }],
  triggerLogic: [{ required: true, message: '请选择触发逻辑', trigger: 'change' }],
  notifyMethods: [
    { type: 'array', required: true, message: '请至少选择一种通知方式', trigger: 'change' }
  ]
}

// 监听configData变化，用于编辑模式
watch(() => props.configData, (newData) => {

  if (newData) {
    formData.value = {
      ...formData.value,
      ...newData
    }

    // ===== 解析 notifyMethods（兼容数组或逗号字符串） =====
    const rawNotify = newData.notifyMethods
    let parsedNotify = []
    if (Array.isArray(rawNotify)) {
      parsedNotify = rawNotify
    } else if (typeof rawNotify === 'string') {
      parsedNotify = rawNotify
          .trim()
          .split(',')
          .map(s => s.trim())
          .filter(Boolean)
    }
    // 直接赋给内部 form 对应字段（假设 el-checkbox-group v-model="formData.notifyMethods"）
    formData.value.notifyMethods = parsedNotify

    // 解析 raw（如果是字符串，尝试 JSON.parse；否则直接用）
    let parsed = {}
    const raw = newData.eventsConfig
    if (raw) {
      if (typeof raw === 'string') {
        try {
          parsed = JSON.parse(raw)
        } catch (e) {
          console.error('解析 eventsConfig JSON 失败：', e, raw)
          parsed = {}
        }
      } else if (typeof raw === 'object') {
        parsed = raw
      } else {
        parsed = {}
      }
    }

    // 解析事件配置
    const pc = parsed.priceChange || {}
    events.priceChange.enabled = !!pc.enabled
    events.priceChange.risePercent = pc.risePercent !== undefined ? pc.risePercent : null
    events.priceChange.fallPercent = pc.fallPercent !== undefined ? pc.fallPercent : null

    const ho = parsed.holders || {}
    events.holders.enabled = !!ho.enabled
    events.holders.increasePercent = ho.increasePercent !== undefined ? ho.increasePercent : null
    events.holders.decreasePercent = ho.decreasePercent !== undefined ? ho.decreasePercent : null

    const vo = parsed.volume || {}
    events.volume.enabled = !!vo.enabled
    events.volume.threshold = vo.threshold !== undefined ? vo.threshold : null
  }
}, { immediate: true })

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    configName: '',
    chainType: 'sol',
    marketType: null,
    timeInterval: '5m',
    topHoldersThreshold: null,
    triggerLogic: 'any',
    notifyMethods: ['telegram'],
    description: ''
  }
  
  events.priceChange = { enabled: false, risePercent: null, fallPercent: null }
  events.holders = { enabled: false, increasePercent: null, decreasePercent: null }
  events.volume = { enabled: false, threshold: null }
  
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    // 检查至少启用一个事件
    const hasEvent = events.priceChange.enabled || events.holders.enabled || events.volume.enabled
    if (!hasEvent) {
      ElMessage.warning('请至少启用一个监控事件')
      return
    }
    
    submitting.value = true
    
    // 构建提交数据
    const submitData = {
      ...formData.value,
      eventsConfig: JSON.stringify({
        priceChange: events.priceChange,
        holders: events.holders,
        volume: events.volume
      })
    }
    
    emit('submit', submitData)
    
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.warning('请检查表单填写是否完整')
  } finally {
    submitting.value = false
  }
}

// 暴露方法给父组件
defineExpose({
  resetForm,
  setFormData: (data) => {
    formData.value = { ...formData.value, ...data }
  }
})
</script>

<style scoped>
.form-tip {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.event-card {
  margin-bottom: 16px;
}

.event-card.disabled {
  opacity: 0.6;
}

.event-title {
  display: flex;
  align-items: center;
}

.event-config {
  padding: 8px 0;
}

.event-field {
  margin-bottom: 0;
}

.event-tip {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.monitor-preview {
  margin-top: 16px;
}

.preview-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.preview-content {
  font-size: 13px;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-card__header) {
  padding: 12px 16px;
}

:deep(.el-card__body) {
  padding: 16px;
}

:deep(.el-divider__text) {
  background-color: var(--el-bg-color);
}
</style>

