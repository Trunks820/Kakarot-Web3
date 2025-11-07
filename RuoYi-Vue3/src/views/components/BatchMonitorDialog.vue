<template>
  <el-dialog 
    title="📝 批量添加监控" 
    v-model="dialogVisible" 
    width="900px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
      <!-- Token地址输入 -->
      <el-form-item label="Token地址" prop="addresses">
        <el-input
          type="textarea"
          v-model="form.addresses"
          :rows="6"
          placeholder="支持SOL和BSC地址混合输入&#10;每行一个或用英文逗号分隔&#10;单次最多99个"
        />
        
        <!-- 解析结果统计 -->
        <div class="address-stats" v-if="form.addresses">
          <el-space wrap style="margin-top: 12px;">
            <el-tag v-if="parsedStats.sol" type="primary" size="large">
              <el-icon><Coin /></el-icon>
              SOL: {{ parsedStats.sol }}个
            </el-tag>
            <el-tag v-if="parsedStats.bsc" type="success" size="large">
              <el-icon><Coin /></el-icon>
              BSC: {{ parsedStats.bsc }}个
            </el-tag>
            <el-tag v-if="parsedStats.invalid" type="danger" size="large">
              <el-icon><WarningFilled /></el-icon>
              无效: {{ parsedStats.invalid }}个
            </el-tag>
            <el-tag type="info" size="large">
              <el-icon><DataLine /></el-icon>
              总计: {{ parsedStats.total }}个
            </el-tag>
          </el-space>
          
          <!-- 超出限制提示 -->
          <el-alert 
            v-if="parsedStats.total > 99" 
            type="warning" 
            :closable="false"
            style="margin-top: 12px;"
            show-icon
          >
            <template #title>
              已超出限制！单次最多支持99个Token，已自动截取前99个
            </template>
          </el-alert>
        </div>
      </el-form-item>

      <el-divider>监控配置</el-divider>

      <!-- 时间周期 -->
      <el-form-item label="时间周期" prop="timeInterval">
        <el-radio-group v-model="form.timeInterval">
          <el-radio-button value="1m">1分钟</el-radio-button>
          <el-radio-button value="5m">5分钟</el-radio-button>
          <el-radio-button value="1h">1小时</el-radio-button>
        </el-radio-group>
        <div class="form-tip">数据统计的时间窗口</div>
      </el-form-item>

      <!-- 前十持仓过滤 -->
      <el-form-item label="前十持仓过滤" prop="topHoldersThreshold">
        <el-input-number 
          v-model="form.topHoldersThreshold" 
          :min="0" 
          :max="100"
          :step="5"
        />
        <span style="margin-left: 8px;">% 及以下</span>
        <div class="form-tip">过滤掉前十大持仓占比超过此值的Token</div>
      </el-form-item>

      <!-- 监控事件 -->
      <el-form-item label="监控事件">
        <div class="events-config">
          <!-- 涨跌幅 -->
          <div class="event-item">
            <el-checkbox v-model="form.events.priceChange.enabled">
              <span style="font-weight: 500;">📈 涨跌幅</span>
            </el-checkbox>
            <div v-if="form.events.priceChange.enabled" class="event-values">
              <span>涨</span>
              <el-input-number 
                v-model="form.events.priceChange.risePercent" 
                :min="1" 
                :max="1000"
                :step="5"
                size="small"
                style="width: 120px; margin: 0 8px;"
              />
              <span>% / 跌</span>
              <el-input-number 
                v-model="form.events.priceChange.fallPercent" 
                :min="1" 
                :max="100"
                :step="5"
                size="small"
                style="width: 120px; margin: 0 8px;"
              />
              <span>%</span>
            </div>
          </div>

          <!-- 持币人数 -->
          <div class="event-item">
            <el-checkbox v-model="form.events.holders.enabled">
              <span style="font-weight: 500;">👥 持币人数</span>
            </el-checkbox>
            <div v-if="form.events.holders.enabled" class="event-values">
              <span>增</span>
              <el-input-number 
                v-model="form.events.holders.increasePercent" 
                :min="1" 
                :max="1000"
                :step="5"
                size="small"
                style="width: 120px; margin: 0 8px;"
              />
              <span>% / 减</span>
              <el-input-number 
                v-model="form.events.holders.decreasePercent" 
                :min="1" 
                :max="100"
                :step="5"
                size="small"
                style="width: 120px; margin: 0 8px;"
              />
              <span>%</span>
            </div>
          </div>

          <!-- 交易量 -->
          <div class="event-item">
            <el-checkbox v-model="form.events.volume.enabled">
              <span style="font-weight: 500;">💰 交易量</span>
            </el-checkbox>
            <div v-if="form.events.volume.enabled" class="event-values">
              <span>≥</span>
              <el-input-number 
                v-model="form.events.volume.threshold" 
                :min="100" 
                :max="1000000"
                :step="1000"
                size="small"
                style="width: 140px; margin: 0 8px;"
              />
              <span>USD</span>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 触发逻辑 -->
      <el-form-item label="触发逻辑" prop="triggerLogic">
        <el-radio-group v-model="form.triggerLogic">
          <el-radio value="any">
            <span style="font-weight: 500;">任一条件满足即触发</span>
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">(OR逻辑)</span>
          </el-radio>
          <el-radio value="all">
            <span style="font-weight: 500;">所有条件同时满足</span>
            <span style="color: #909399; font-size: 12px; margin-left: 8px;">(AND逻辑)</span>
          </el-radio>
        </el-radio-group>
        <div class="form-tip">
          OR逻辑：触发更频繁，适合捕捉各类信号 | AND逻辑：触发更精准，减少噪音
        </div>
      </el-form-item>

      <!-- 通知方式 -->
      <el-form-item label="通知方式" prop="notifyMethods">
        <el-checkbox-group v-model="form.notifyMethodsArray">
          <el-checkbox value="telegram">
            <el-icon><ChatDotRound /></el-icon>
            Telegram
          </el-checkbox>
          <el-checkbox value="wechat">
            <el-icon><ChatLineSquare /></el-icon>
            微信
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit" 
          :loading="saving"
          :disabled="parsedStats.total === 0"
        >
          <el-icon v-if="!saving"><Check /></el-icon>
          保存监控（{{ Math.min(parsedStats.total, 99) }}个）
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Coin, 
  WarningFilled, 
  DataLine, 
  ChatDotRound, 
  ChatLineSquare,
  Check 
} from '@element-plus/icons-vue'
import { smartBatchAdd } from '@/api/crypto/batchMonitor'

// Props & Emits
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

// 响应式变量
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const formRef = ref()
const saving = ref(false)

// 表单数据
const form = ref({
  addresses: '',
  timeInterval: '5m',
  topHoldersThreshold: 50,
  events: {
    priceChange: { enabled: true, risePercent: 30, fallPercent: 20 },
    holders: { enabled: true, increasePercent: 20, decreasePercent: 15 },
    volume: { enabled: true, threshold: 5000 }
  },
  triggerLogic: 'any',
  notifyMethodsArray: ['telegram']
})

// 表单验证规则
const rules = {
  addresses: [
    { required: true, message: '请输入Token地址', trigger: 'blur' }
  ],
  timeInterval: [
    { required: true, message: '请选择时间周期', trigger: 'change' }
  ],
  topHoldersThreshold: [
    { required: true, message: '请输入前十持仓阈值', trigger: 'blur' }
  ],
  triggerLogic: [
    { required: true, message: '请选择触发逻辑', trigger: 'change' }
  ],
  notifyMethods: [
    { 
      validator: (rule, value, callback) => {
        if (form.value.notifyMethodsArray.length === 0) {
          callback(new Error('请至少选择一种通知方式'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 解析地址统计
const parsedStats = computed(() => {
  if (!form.value.addresses) {
    return { sol: 0, bsc: 0, invalid: 0, total: 0, valid: [] }
  }

  const addresses = form.value.addresses
    .split(/[,\n]/)
    .map(addr => addr.trim())
    .filter(addr => addr.length > 0)

  let sol = 0
  let bsc = 0
  let invalid = 0
  const valid = []

  addresses.forEach(addr => {
    // BSC: 0x + 40位十六进制
    if (/^0x[a-fA-F0-9]{40}$/.test(addr)) {
      bsc++
      valid.push(addr)
    }
    // SOL: Base58, 32-44位
    else if (/^[1-9A-HJ-NP-Za-km-z]{32,44}$/.test(addr)) {
      sol++
      valid.push(addr)
    }
    else {
      invalid++
    }
  })

  return {
    sol,
    bsc,
    invalid,
    total: sol + bsc,
    valid
  }
})

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()

    if (parsedStats.value.total === 0) {
      ElMessage.warning('请输入有效的Token地址')
      return
    }

    saving.value = true

    // 取前99个有效地址
    const addresses = parsedStats.value.valid.slice(0, 99)

    const requestData = {
      addresses: addresses,
      config: {
        timeInterval: form.value.timeInterval,
        topHoldersThreshold: form.value.topHoldersThreshold,
        events: form.value.events,
        triggerLogic: form.value.triggerLogic,
        notifyMethods: form.value.notifyMethodsArray.join(',')
      }
    }

    const response = await smartBatchAdd(requestData)

    if (response.code === 200) {
      const results = response.data.results || []
      
      // 构建成功消息
      const summary = results.map(r => {
        const allocations = r.allocations.map(a => 
          `批次${a.batchId}(${a.addedCount}个)`
        ).join(' + ')
        return `${r.chainType.toUpperCase()}: ${allocations}`
      }).join('\n')

      ElMessage.success({
        message: `✅ 批量监控已创建！\n${summary}`,
        duration: 5000,
        showClose: true
      })

      emit('success')
      handleClose()
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  formRef.value?.resetFields()
  form.value.addresses = ''
  dialogVisible.value = false
}

// 监听对话框打开，可以加载上次配置
watch(dialogVisible, (newVal) => {
  if (newVal) {
    // TODO: 可以从localStorage或后端加载上次配置
    console.log('对话框已打开')
  }
})
</script>

<style scoped lang="scss">
.address-stats {
  margin-top: 12px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
}

.events-config {
  width: 100%;
  
  .event-item {
    padding: 12px 16px;
    background: #f5f7fa;
    border-radius: 8px;
    margin-bottom: 12px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .event-values {
      margin-top: 12px;
      margin-left: 24px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 4px;
      
      span {
        font-size: 14px;
        color: #606266;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-dialog__body) {
  max-height: 600px;
  overflow-y: auto;
}
</style>

