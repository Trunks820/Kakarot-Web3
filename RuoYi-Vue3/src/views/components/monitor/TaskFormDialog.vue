<!-- 任务表单弹窗组件（统一的新建/编辑任务弹窗） -->
<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="800px"
    append-to-body
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
      <el-form-item label="任务名称" prop="taskName">
        <el-input v-model="formData.taskName" placeholder="请输入任务名称" />
      </el-form-item>
      
      <el-form-item label="任务类型" prop="taskType">
        <el-radio-group v-model="formData.taskType" :disabled="isEdit">
          <el-radio label="smart">
            <el-icon><MagicStick /></el-icon> 智能监控
          </el-radio>
          <el-radio label="batch">
            <el-icon><List /></el-icon> 批量监控
          </el-radio>
          <el-radio label="block">
            <el-icon><Histogram /></el-icon> 区块监控
          </el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="链类型" prop="chainType">
        <el-radio-group v-model="formData.chainType">
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
      <template v-if="formData.taskType === 'smart'">
        <el-form-item label="市值范围" prop="marketCapRange">
          <el-row :gutter="10">
            <el-col :span="11">
              <el-input v-model.number="formData.minMarketCap" placeholder="最小市值">
                <template #append>USD</template>
              </el-input>
            </el-col>
            <el-col :span="2" style="text-align: center">-</el-col>
            <el-col :span="11">
              <el-input v-model.number="formData.maxMarketCap" placeholder="最大市值(可选)">
                <template #append>USD</template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>
        
        <el-form-item label="Twitter筛选">
          <el-radio-group v-model="formData.hasTwitter">
            <el-radio :label="null">不限</el-radio>
            <el-radio :label="1">有Twitter</el-radio>
            <el-radio :label="0">无Twitter</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="自动同步">
          <el-switch v-model="formData.autoSyncTargets" :active-value="1" :inactive-value="0" />
          <span class="form-tip">开启后将自动同步符合条件的Token</span>
        </el-form-item>
        
        <el-form-item label="同步间隔" v-if="formData.autoSyncTargets === 1">
          <el-input-number v-model="formData.syncIntervalMinutes" :min="5" :max="1440" />
          <span class="form-tip">分钟</span>
        </el-form-item>
      </template>
      
      <!-- 批量监控专属字段 -->
      <template v-if="formData.taskType === 'batch'">
        <el-form-item label="Token地址" prop="caList">
          <el-input
            v-model="formData.caList"
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
      <template v-if="formData.taskType === 'block'">
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
          v-model="formData.configId"
          placeholder="请选择监控配置"
          style="width: 100%"
          @change="handleConfigChange"
        >
          <el-option
            v-for="config in availableConfigs"
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
          v-model="formData.description"
          type="textarea"
          :rows="2"
          placeholder="请输入任务描述（可选）"
        />
      </el-form-item>
      
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '确认修改' : (formData.taskType === 'smart' ? '创建并同步目标' : '创建任务') }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, MagicStick, List, Histogram, Star, Coin } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { // v-model双向绑定弹窗显示状态
    type: Boolean,
    default: false
  },
  taskData: { // 编辑时传入的任务数据
    type: Object,
    default: null
  },
  configList: { // 可用的配置列表
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

// 弹窗可见性
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

// 表单ref
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = ref({
  id: null,
  taskName: '',
  taskType: 'smart',
  chainType: 'sol',
  minMarketCap: 300000,
  maxMarketCap: null,
  hasTwitter: null,
  autoSyncTargets: 0,
  syncIntervalMinutes: 30,
  caList: '',
  configId: null,
  description: '',
  status: 1
})

// 是否编辑模式
const isEdit = computed(() => !!formData.value.id)

// 弹窗标题
const dialogTitle = computed(() => {
  if (isEdit.value) {
    return formData.value.taskType === 'smart' ? '编辑智能监控任务' : 
           formData.value.taskType === 'batch' ? '编辑批量监控任务' :
           '编辑区块监控任务'
  } else {
    return formData.value.taskType === 'smart' ? '新建智能监控任务' : 
           formData.value.taskType === 'batch' ? '新建批量监控任务' :
           '新建区块监控任务'
  }
})

// 可用配置列表（根据链类型筛选）
const availableConfigs = computed(() => {
  if (!formData.value.chainType) return []
  return props.configList.filter(c => c.chainType === formData.value.chainType)
})

// CA地址数量统计
const caCount = computed(() => {
  if (!formData.value.caList) return 0
  return formData.value.caList.trim().split('\n').filter(line => line.trim()).length
})

// 表单验证规则
const formRules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  taskType: [{ required: true, message: '请选择任务类型', trigger: 'change' }],
  chainType: [{ required: true, message: '请选择链类型', trigger: 'change' }],
  configId: [{ required: true, message: '请选择监控配置', trigger: 'change' }],
  caList: [
    { required: true, message: '请输入至少一个Token地址', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (formData.value.taskType === 'batch' && (!value || value.trim() === '')) {
          callback(new Error('请输入至少一个Token地址'))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ]
}

// 监听taskData变化，用于编辑模式
watch(() => props.taskData, (newData) => {
  if (newData) {
    formData.value = {
      ...formData.value,
      ...newData,
      autoSyncTargets: newData.autoSyncTargets || 0,
      status: newData.status !== undefined ? newData.status : 1
    }
  }
}, { immediate: true })

// 配置变更处理
const handleConfigChange = () => {
  // 可以在这里添加配置变更后的逻辑
}

// 关闭弹窗
const handleClose = () => {
  visible.value = false
  resetForm()
}

// 重置表单
const resetForm = () => {
  formData.value = {
    id: null,
    taskName: '',
    taskType: 'smart',
    chainType: 'sol',
    minMarketCap: 300000,
    maxMarketCap: null,
    hasTwitter: null,
    autoSyncTargets: 0,
    syncIntervalMinutes: 30,
    caList: '',
    configId: null,
    description: '',
    status: 1
  }
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    // 触发submit事件，父组件处理具体的提交逻辑
    emit('submit', { ...formData.value })
    
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
  margin-left: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-radio) {
  margin-right: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>

