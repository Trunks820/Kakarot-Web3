<!-- 配置管理完整页面 -->
<template>
  <div class="config-manage">
    <el-card>
      <template #header>
        <div class="page-header">
          <span class="page-title">配置管理</span>
          <el-button 
            v-hasPermi="['crypto:monitor-v2:config:add']"
            type="primary" 
            icon="Plus"
            @click="handleAdd"
          >
            新建配置
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="配置名称">
          <el-input
            v-model="queryParams.configName"
            placeholder="请输入配置名称"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="链类型">
          <el-select v-model="queryParams.chainType" placeholder="请选择链类型" clearable>
            <el-option label="Solana" value="sol" />
            <el-option label="BSC" value="bsc" />
            <el-option label="Ethereum" value="eth" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
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
        :data="configList"
        stripe
        style="width: 100%"
      >
        <el-table-column label="ID" prop="id" width="80" />
        <el-table-column label="配置名称" prop="configName" min-width="150" show-overflow-tooltip />
        <el-table-column label="链类型" prop="chainType" width="100">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.chainType?.toUpperCase() }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间周期" prop="timeInterval" width="100" />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="关联任务数" prop="taskCount" width="120" />
        <el-table-column label="创建时间" prop="createTime" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="200" align="center" fixed="right">
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
      
      <!-- 分页 -->
      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      append-to-body
      @close="handleDialogClose"
    >
      <el-form
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        label-width="120px"
      >
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        
        <el-form-item label="链类型" prop="chainType">
          <el-select v-model="configForm.chainType" placeholder="请选择链类型">
            <el-option label="Solana" value="sol" />
            <el-option label="BSC" value="bsc" />
            <el-option label="Ethereum" value="eth" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="配置类型" prop="configType">
          <el-select v-model="configForm.configType" placeholder="请选择配置类型">
            <el-option label="系统配置" value="system" />
            <el-option label="自定义配置" value="custom" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间周期(分钟)" prop="timeInterval">
          <el-input-number v-model="configForm.timeInterval" :min="1" :max="1440" />
        </el-form-item>
        
        <el-form-item label="监控事件">
          <el-checkbox-group v-model="selectedEvents">
            <el-checkbox label="price">价格监控</el-checkbox>
            <el-checkbox label="holder">持仓监控</el-checkbox>
            <el-checkbox label="volume">交易量监控</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <!-- 价格监控配置 -->
        <el-form-item v-if="selectedEvents.includes('price')" label="价格变化阈值(%)">
          <el-input-number v-model="eventConfig.price.threshold" :min="0" :max="100" />
        </el-form-item>
        
        <!-- 持仓监控配置 -->
        <template v-if="selectedEvents.includes('holder')">
          <el-form-item label="监控前N名">
            <el-input-number v-model="eventConfig.holder.topCount" :min="1" :max="100" />
          </el-form-item>
          <el-form-item label="持仓占比阈值(%)">
            <el-input-number v-model="eventConfig.holder.threshold" :min="0" :max="100" />
          </el-form-item>
        </template>
        
        <!-- 交易量监控配置 -->
        <el-form-item v-if="selectedEvents.includes('volume')" label="交易量阈值">
          <el-input-number v-model="eventConfig.volume.threshold" :min="0" />
        </el-form-item>
        
        <el-form-item label="触发逻辑" prop="triggerLogic">
          <el-radio-group v-model="configForm.triggerLogic">
            <el-radio label="AND">所有条件都满足</el-radio>
            <el-radio label="OR">任意条件满足</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="通知方式" prop="notifyMethods">
          <el-checkbox-group v-model="notifyMethods">
            <el-checkbox label="email">邮件</el-checkbox>
            <el-checkbox label="sms">短信</el-checkbox>
            <el-checkbox label="webhook">Webhook</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="描述">
          <el-input
            v-model="configForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="configForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="配置详情"
      width="800px"
      append-to-body
    >
      <el-descriptions v-if="detailData" :column="2" border>
        <el-descriptions-item label="配置ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="配置名称">{{ detailData.configName }}</el-descriptions-item>
        <el-descriptions-item label="链类型">
          <el-tag size="small">{{ detailData.chainType?.toUpperCase() }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="配置类型">
          <el-tag :type="detailData.configType === 'system' ? 'success' : 'info'" size="small">
            {{ detailData.configType === 'system' ? '系统配置' : '自定义配置' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间周期">{{ detailData.timeInterval }}分钟</el-descriptions-item>
        <el-descriptions-item label="触发逻辑">
          {{ detailData.triggerLogic === 'AND' ? '所有条件都满足' : '任意条件满足' }}
        </el-descriptions-item>
        <el-descriptions-item label="通知方式" :span="2">
          <el-tag v-for="method in parseNotifyMethods(detailData.notifyMethods)" :key="method" size="small" style="margin-right: 8px;">
            {{ method }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="监控规则" :span="2">
          <div v-if="detailData.eventsConfig">
            <el-tag 
              v-for="(event, index) in parseEventsConfig(detailData.eventsConfig)" 
              :key="index"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ event }}
            </el-tag>
          </div>
          <span v-else style="color: var(--el-text-color-secondary);">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'" size="small">
            {{ detailData.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ detailData.version || 1 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">
          {{ detailData.description || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button 
          v-hasPermi="['crypto:monitor-v2:config:edit']"
          type="primary" 
          @click="handleEditFromDetail"
        >
          编辑
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { listConfig, getConfig, addConfig, updateConfig, delConfig } from '@/api/crypto/monitor-v2'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const configList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  configName: undefined,
  chainType: undefined,
  status: undefined
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = computed(() => configForm.value.id ? '编辑配置' : '新建配置')
const submitLoading = ref(false)
const configFormRef = ref(null)

// 表单数据
const configForm = ref({
  id: undefined,
  configName: '',
  chainType: '',
  configType: 'custom',
  timeInterval: 5,
  eventsConfig: '',
  triggerLogic: 'OR',
  notifyMethods: '',
  description: '',
  status: 1
})

// 监控事件选择
const selectedEvents = ref([])
const eventConfig = ref({
  price: { enabled: false, threshold: 10 },
  holder: { enabled: false, topCount: 10, threshold: 70 },
  volume: { enabled: false, threshold: 1000000 }
})

// 通知方式选择
const notifyMethods = ref([])

// 表单验证规则
const configRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  chainType: [{ required: true, message: '请选择链类型', trigger: 'change' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  timeInterval: [{ required: true, message: '请输入时间周期', trigger: 'blur' }],
  triggerLogic: [{ required: true, message: '请选择触发逻辑', trigger: 'change' }]
}

// 详情对话框
const detailDialogVisible = ref(false)
const detailData = ref(null)

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const response = await listConfig(queryParams.value)
    configList.value = response.rows
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
    configName: undefined,
    chainType: undefined,
    status: undefined
  }
  getList()
}

// 新增
const handleAdd = () => {
  resetForm()
  dialogVisible.value = true
}

// 详情
const handleDetail = async (row) => {
  try {
    const response = await getConfig(row.id)
    detailData.value = response.data
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取详情失败：' + error.message)
  }
}

// 编辑
const handleEdit = async (row) => {
  resetForm()
  try {
    const response = await getConfig(row.id)
    const config = response.data
    
    configForm.value = {
      id: config.id,
      configName: config.configName,
      chainType: config.chainType,
      configType: config.configType || 'custom',
      timeInterval: config.timeInterval,
      triggerLogic: config.triggerLogic || 'OR',
      description: config.description,
      status: config.status
    }
    
    // 解析事件配置
    if (config.eventsConfig) {
      try {
        const events = JSON.parse(config.eventsConfig)
        selectedEvents.value = []
        if (events.price?.enabled) {
          selectedEvents.value.push('price')
          eventConfig.value.price = events.price
        }
        if (events.holder?.enabled) {
          selectedEvents.value.push('holder')
          eventConfig.value.holder = events.holder
        }
        if (events.volume?.enabled) {
          selectedEvents.value.push('volume')
          eventConfig.value.volume = events.volume
        }
      } catch (e) {
        console.error('解析事件配置失败:', e)
      }
    }
    
    // 解析通知方式
    if (config.notifyMethods) {
      notifyMethods.value = config.notifyMethods.split(',')
    }
    
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取配置失败：' + error.message)
  }
}

// 从详情对话框跳转到编辑
const handleEditFromDetail = () => {
  detailDialogVisible.value = false
  handleEdit(detailData.value)
}

// 提交表单
const submitForm = async () => {
  if (!configFormRef.value) return
  
  await configFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      // 构建事件配置
      const events = {}
      if (selectedEvents.value.includes('price')) {
        events.price = { ...eventConfig.value.price, enabled: true }
      }
      if (selectedEvents.value.includes('holder')) {
        events.holder = { ...eventConfig.value.holder, enabled: true }
      }
      if (selectedEvents.value.includes('volume')) {
        events.volume = { ...eventConfig.value.volume, enabled: true }
      }
      
      const submitData = {
        ...configForm.value,
        eventsConfig: JSON.stringify(events),
        notifyMethods: notifyMethods.value.join(',')
      }
      
      if (submitData.id) {
        await updateConfig(submitData)
        ElMessage.success('更新成功')
      } else {
        await addConfig(submitData)
        ElMessage.success('创建成功')
      }
      
      dialogVisible.value = false
      getList()
    } catch (error) {
      ElMessage.error('操作失败：' + error.message)
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  configForm.value = {
    id: undefined,
    configName: '',
    chainType: '',
    configType: 'custom',
    timeInterval: 5,
    eventsConfig: '',
    triggerLogic: 'OR',
    notifyMethods: '',
    description: '',
    status: 1
  }
  selectedEvents.value = []
  eventConfig.value = {
    price: { enabled: false, threshold: 10 },
    holder: { enabled: false, topCount: 10, threshold: 70 },
    volume: { enabled: false, threshold: 1000000 }
  }
  notifyMethods.value = []
  configFormRef.value?.resetFields()
}

// 对话框关闭
const handleDialogClose = () => {
  resetForm()
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除配置"${row.configName}"吗？`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await delConfig(row.id)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 解析监控规则为可读文本
const parseEventsConfig = (eventsConfigStr) => {
  try {
    const events = JSON.parse(eventsConfigStr)
    const rules = []
    if (events.price?.enabled) { rules.push(`价格监控: ${events.price.threshold}%`) }
    if (events.holder?.enabled) { rules.push(`持仓监控: 前${events.holder.topCount}名 > ${events.holder.threshold}%`) }
    if (events.volume?.enabled) { rules.push(`交易量监控: ${events.volume.threshold}`) }
    return rules.length > 0 ? rules : ['无监控规则']
  } catch (e) {
    return ['解析失败']
  }
}

// 解析通知方式
const parseNotifyMethods = (methodsStr) => {
  if (!methodsStr) return []
  const methodMap = {
    email: '邮件',
    sms: '短信',
    webhook: 'Webhook'
  }
  return methodsStr.split(',').map(m => methodMap[m] || m)
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.config-manage {
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

