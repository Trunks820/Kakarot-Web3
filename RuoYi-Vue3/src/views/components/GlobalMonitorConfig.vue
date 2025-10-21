<template>
  <div class="global-monitor-container">
    <el-card class="widget-card monitor-widget" shadow="hover">
      <!-- Header -->
      <template #header>
        <div class="widget-header">
          <div class="widget-title">
            <el-icon class="widget-icon"><Bell /></el-icon>
            <span>链监控配置</span>
          </div>
          <el-tag 
            :type="activeConfigCount > 0 ? 'success' : 'info'" 
            size="small"
            effect="plain"
          >
            {{ activeConfigCount > 0 ? '运行中' : '未启用' }}
          </el-tag>
        </div>
      </template>

      <!-- Body -->
      <el-skeleton :loading="loading" animated>
        <template #template>
          <el-skeleton-item variant="text" style="width: 100%; height: 60px; margin-bottom: 12px" />
          <el-skeleton-item variant="text" style="width: 100%; height: 60px; margin-bottom: 12px" />
          <el-skeleton-item variant="rect" style="width: 100%; height: 80px" />
        </template>
        
        <template #default>
          <div class="widget-body">
            <!-- 核心数据展示 -->
            <div class="config-stats">
              <div class="stat-card" :class="{ active: bscConfig.enabled }">
                <div class="stat-header">
                  <el-tag type="warning" size="small">BSC</el-tag>
                  <el-switch
                    v-model="bscConfig.enabled"
                    :loading="bscConfig.switching"
                    @change="handleStatusChange('bsc')"
                    size="small"
                  />
                </div>
                <div class="stat-content">
                  <div class="stat-label">交易阈值</div>
                  <div class="stat-value">
                    {{ bscConfig.data ? bscConfig.data.minTransactionUsd : 400 }} USD
                  </div>
                </div>
                <div class="stat-footer">
                  <el-button 
                    text 
                    size="small" 
                    @click="handleConfigClick('bsc')"
                    :icon="Setting"
                  >
                    {{ bscConfig.data ? '编辑配置' : '新建配置' }}
                  </el-button>
                </div>
              </div>

              <div class="stat-card disabled">
                <div class="stat-header">
                  <el-tag type="success" size="small">SOL</el-tag>
                  <el-switch
                    v-model="solConfig.enabled"
                    size="small"
                    disabled
                  />
                </div>
                <div class="stat-content">
                  <div class="stat-label">开发中</div>
                  <div class="stat-value">-</div>
                </div>
                <div class="stat-footer">
                  <el-tag size="small" type="info">即将上线</el-tag>
                </div>
              </div>
            </div>

            <!-- 统计摘要 -->
            <div class="summary-info">
              <div class="summary-item">
                <span class="label">活跃配置</span>
                <span class="value">{{ activeConfigCount }}</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <span class="label">今日预警</span>
                <span class="value">-</span>
              </div>
              <div class="summary-divider"></div>
              <div class="summary-item">
                <span class="label">监控事件</span>
                <span class="value">{{ totalEvents }}</span>
              </div>
            </div>
          </div>
        </template>
      </el-skeleton>

      <!-- Footer -->
      <template #footer>
        <div class="widget-footer">
          <el-button size="small" @click="handleViewLogs">
            <el-icon><Document /></el-icon>
            <span>配置日志</span>
          </el-button>
          <el-button size="small" type="primary" plain @click="handleQuickConfig">
            <el-icon><Tools /></el-icon>
            <span>快速配置</span>
          </el-button>
        </div>
      </template>
    </el-card>

    <!-- 配置弹窗（移到卡片外面） -->
    <el-dialog
      v-model="configDialog.visible"
      :title="`${configDialog.chainType.toUpperCase()}链全局监控配置`"
      :width="'min(720px, 90vw)'"
      @close="resetConfigForm"
    >
      <el-alert
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <template #title>
          <div style="display: flex; align-items: center; gap: 8px;">
            <span>🌐</span>
            <span>此配置将应用于 <strong>{{ configDialog.chainType.toUpperCase() }}链区块监听</strong></span>
          </div>
        </template>
      </el-alert>

      <el-form :model="configDialog.form" label-width="120px">
        <!-- 基础配置 -->
        <el-form-item label="配置名称">
          <el-input
            v-model="configDialog.form.configName"
            placeholder="请输入配置名称"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="最小交易金额">
          <el-input-number
            v-model="configDialog.form.minTransactionUsd"
            :min="0"
            :precision="2"
            style="width: 200px"
          >
            <template #suffix>USD</template>
          </el-input-number>
          <span style="margin-left: 12px; color: #909399; font-size: 13px;">
            💡 只监控大于此金额的交易
          </span>
        </el-form-item>

        <el-divider content-position="left">
          <span style="font-weight: 600;">监控事件</span>
        </el-divider>

        <!-- 涨跌幅变化 -->
        <el-card class="event-card" :class="{ disabled: !configDialog.events.priceChange.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="configDialog.events.priceChange.enabled">
                📈 涨跌幅变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="configDialog.events.priceChange.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="涨幅" label-position="top" class="event-field">
                  <el-input-number
                    v-model="configDialog.events.priceChange.risePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="跌幅" label-position="top" class="event-field">
                  <el-input-number
                    v-model="configDialog.events.priceChange.fallPercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>

        <!-- 持币人数变化 -->
        <el-card class="event-card" :class="{ disabled: !configDialog.events.holders.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="configDialog.events.holders.enabled">
                👥 持币人数变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="configDialog.events.holders.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="增长" label-position="top" class="event-field">
                  <el-input-number
                    v-model="configDialog.events.holders.increasePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="减少" label-position="top" class="event-field">
                  <el-input-number
                    v-model="configDialog.events.holders.decreasePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>

        <!-- 交易量变化 -->
        <el-card class="event-card" :class="{ disabled: !configDialog.events.volume.enabled }">
          <template #header>
            <div class="event-title">
              <el-checkbox v-model="configDialog.events.volume.enabled">
                💰 交易量变化
              </el-checkbox>
            </div>
          </template>
          <div v-if="configDialog.events.volume.enabled" class="event-config">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="增长" label-position="top" class="event-field">
                  <el-input-number
                    v-model="configDialog.events.volume.increasePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="减少" label-position="top" class="event-field">
                  <el-input-number
                    v-model="configDialog.events.volume.decreasePercent"
                    :min="0"
                    :max="1000"
                    :precision="1"
                    style="width: 100%"
                  >
                    <template #suffix>%</template>
                  </el-input-number>
                </el-form-item>
              </el-col>
            </el-row>
            <div class="event-tip">💡 留空表示不监控该方向</div>
          </div>
        </el-card>

        <el-divider content-position="left">
          <span style="font-weight: 600;">触发设置</span>
        </el-divider>

        <!-- 触发逻辑 -->
        <el-form-item label="触发逻辑">
          <el-radio-group v-model="configDialog.form.triggerLogic">
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

        <!-- 通知方式 -->
        <el-form-item>
          <template #label>
            <span class="required-mark">*</span>
            <span>通知方式</span>
          </template>
          <el-checkbox-group v-model="configDialog.notifyMethodsArray">
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
            v-model="configDialog.form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
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
        <el-button @click="configDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig" :loading="configDialog.saving">
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 配置日志对话框 -->
    <el-dialog
      v-model="logsDialog.visible"
      title="配置日志"
      width="800px"
    >
      <el-table
        v-loading="logsDialog.loading"
        :data="logsDialog.logs"
        stripe
        style="width: 100%"
      >
        <el-table-column label="时间" prop="time" width="180" />
        <el-table-column label="链" prop="chainType" width="80">
          <template #default="scope">
            <el-tag 
              :type="scope.row.chainType === 'BSC' ? 'warning' : 'success'" 
              size="small"
            >
              {{ scope.row.chainType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" prop="action" width="100" />
        <el-table-column label="操作人" prop="operator" width="100" />
        <el-table-column label="详情" prop="details" show-overflow-tooltip />
      </el-table>
      
      <div v-if="logsDialog.logs.length === 0 && !logsDialog.loading" style="text-align: center; padding: 40px; color: #909399;">
        <el-icon style="font-size: 48px; margin-bottom: 16px;"><Document /></el-icon>
        <div>暂无配置日志</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, getCurrentInstance } from 'vue'
import { Bell, Setting, Check, Close, Document, Tools } from '@element-plus/icons-vue'
import { 
  getGlobalMonitorByChain, 
  saveOrUpdateGlobalMonitor,
  changeGlobalMonitorStatus 
} from '@/api/crypto/globalMonitor'

const { proxy } = getCurrentInstance()

// 加载状态
const loading = ref(true)

// BSC配置
const bscConfig = reactive({
  enabled: false,
  switching: false,
  data: null
})

// SOL配置
const solConfig = reactive({
  enabled: false,
  switching: false,
  data: null
})

// 活跃配置数量
const activeConfigCount = computed(() => {
  let count = 0
  if (bscConfig.enabled && bscConfig.data) count++
  if (solConfig.enabled && solConfig.data) count++
  return count
})

// 监控事件总数
const totalEvents = computed(() => {
  let count = 0
  if (bscConfig.data && bscConfig.data.eventsConfig) {
    try {
      const events = JSON.parse(bscConfig.data.eventsConfig)
      if (events.priceChange?.enabled) count++
      if (events.holders?.enabled) count++
      if (events.volume?.enabled) count++
    } catch (e) {
      console.error('解析事件配置失败:', e)
    }
  }
  return count
})

// 配置弹窗
const configDialog = reactive({
  visible: false,
  chainType: '',
  saving: false,
  form: {
    id: null,
    configName: '',
    chainType: '',
    source: 'all',
    minTransactionUsd: 400,
    triggerLogic: 'any',
    status: '1',
    remark: ''
  },
  events: {
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
      increasePercent: null,
      decreasePercent: null
    }
  },
  notifyMethodsArray: []
})

// 监控条件摘要
const configConditionsSummary = computed(() => {
  const conditions = []
  const { priceChange, holders, volume } = configDialog.events
  const triggerLogicText = configDialog.form.triggerLogic === 'any' ? '任一条件' : '所有条件'
  
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
    const parts = []
    if (volume.increasePercent) parts.push(`交易量增长≥${volume.increasePercent}%`)
    if (volume.decreasePercent) parts.push(`交易量减少≥${volume.decreasePercent}%`)
    if (parts.length > 0) conditions.push(parts.join(' 或 '))
  }
  
  if (conditions.length === 0) return ''
  
  return `${triggerLogicText}：${conditions.join(configDialog.form.triggerLogic === 'any' ? ' 或 ' : ' 且 ')}`
})

// 监听事件启用，自动填充默认值
watch(() => configDialog.events.priceChange.enabled, (newVal) => {
  if (newVal && !configDialog.events.priceChange.risePercent && !configDialog.events.priceChange.fallPercent) {
    configDialog.events.priceChange.risePercent = 50
    configDialog.events.priceChange.fallPercent = 30
  }
})

watch(() => configDialog.events.holders.enabled, (newVal) => {
  if (newVal && !configDialog.events.holders.increasePercent && !configDialog.events.holders.decreasePercent) {
    configDialog.events.holders.increasePercent = 100
    configDialog.events.holders.decreasePercent = 50
  }
})

watch(() => configDialog.events.volume.enabled, (newVal) => {
  if (newVal && !configDialog.events.volume.increasePercent && !configDialog.events.volume.decreasePercent) {
    configDialog.events.volume.increasePercent = 200
    configDialog.events.volume.decreasePercent = 100
  }
})

// 加载配置
const loadConfigs = async () => {
  loading.value = true
  
  try {
    // 加载BSC配置
    const bscRes = await getGlobalMonitorByChain('bsc')
    if (bscRes.data) {
      bscConfig.data = bscRes.data
      bscConfig.enabled = bscRes.data.status === '1'
    }
    
    // 加载SOL配置（预留）
    // const solRes = await getGlobalMonitorByChain('sol')
    // if (solRes.data) {
    //   solConfig.data = solRes.data
    //   solConfig.enabled = solRes.data.status === '1'
    // }
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换状态
const handleStatusChange = async (chainType) => {
  const config = chainType === 'bsc' ? bscConfig : solConfig
  
  if (!config.data) {
    proxy.$modal.msgWarning('请先配置监控规则')
    config.enabled = false
    return
  }
  
  config.switching = true
  
  try {
    const newStatus = config.enabled ? '1' : '0'
    await changeGlobalMonitorStatus(config.data.id, newStatus)
    proxy.$modal.msgSuccess(config.enabled ? '已启用' : '已停用')
  } catch (error) {
    config.enabled = !config.enabled
    proxy.$modal.msgError('操作失败：' + (error.message || '未知错误'))
  } finally {
    config.switching = false
  }
}

// 打开配置弹窗
const handleConfigClick = (chainType) => {
  configDialog.chainType = chainType
  configDialog.visible = true
  
  const config = chainType === 'bsc' ? bscConfig : solConfig
  
  if (config.data) {
    // 编辑现有配置
    configDialog.form = {
      id: config.data.id,
      configName: config.data.configName,
      chainType: config.data.chainType,
      source: config.data.source || 'all',
      minTransactionUsd: config.data.minTransactionUsd || 400,
      triggerLogic: config.data.triggerLogic || 'any',
      status: config.data.status,
      remark: config.data.remark || ''
    }
    
    // 解析事件配置
    if (config.data.eventsConfig) {
      try {
        configDialog.events = JSON.parse(config.data.eventsConfig)
      } catch (e) {
        console.error('解析事件配置失败:', e)
      }
    }
    
    // 解析通知方式
    if (config.data.notifyMethods) {
      configDialog.notifyMethodsArray = config.data.notifyMethods.split(',')
    }
  } else {
    // 新建配置
    resetConfigForm()
    configDialog.form.chainType = chainType
    configDialog.form.configName = `${chainType.toUpperCase()}链全局监控`
  }
}

// 重置表单
const resetConfigForm = () => {
  configDialog.form = {
    id: null,
    configName: '',
    chainType: '',
    source: 'all',
    minTransactionUsd: 400,
    triggerLogic: 'any',
    status: '1',
    remark: ''
  }
  
  configDialog.events = {
    priceChange: { enabled: false, risePercent: null, fallPercent: null },
    holders: { enabled: false, increasePercent: null, decreasePercent: null },
    volume: { enabled: false, increasePercent: null, decreasePercent: null }
  }
  
  configDialog.notifyMethodsArray = []
}

// 保存配置
const saveConfig = async () => {
  // 验证
  const hasEnabledEvent = Object.values(configDialog.events).some(e => e.enabled)
  if (!hasEnabledEvent) {
    proxy.$modal.msgWarning('请至少选择一个监控事件')
    return
  }
  
  for (const [key, event] of Object.entries(configDialog.events)) {
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
  
  if (configDialog.notifyMethodsArray.length === 0) {
    proxy.$modal.msgWarning('请至少选择一个通知方式')
    return
  }
  
  configDialog.saving = true
  
  try {
    const data = {
      ...configDialog.form,
      eventsConfig: JSON.stringify(configDialog.events),
      notifyMethods: configDialog.notifyMethodsArray.join(',')
    }
    
    await saveOrUpdateGlobalMonitor(data)
    proxy.$modal.msgSuccess('保存成功')
    configDialog.visible = false
    
    // 重新加载配置
    await loadConfigs()
  } catch (error) {
    proxy.$modal.msgError('保存失败：' + (error.message || '未知错误'))
  } finally {
    configDialog.saving = false
  }
}

// 配置日志对话框
const logsDialog = reactive({
  visible: false,
  loading: false,
  logs: []
})

// 查看配置日志
const handleViewLogs = () => {
  logsDialog.visible = true
  logsDialog.loading = true
  
  // 模拟日志数据（后续可对接真实API）
  setTimeout(() => {
    logsDialog.logs = [
      {
        id: 1,
        chainType: 'BSC',
        action: '启用监控',
        operator: 'admin',
        time: '2025-10-21 14:30:00',
        details: '启用BSC链全局监控，交易阈值: 400 USD'
      },
      {
        id: 2,
        chainType: 'BSC',
        action: '修改配置',
        operator: 'admin',
        time: '2025-10-21 12:15:00',
        details: '调整涨幅阈值从5%提升至10%'
      }
    ]
    logsDialog.loading = false
  }, 500)
}

// 快速配置
const handleQuickConfig = () => {
  // 快速配置BSC链
  if (bscConfig.data) {
    handleConfigClick('bsc')
  } else {
    proxy.$modal.confirm('检测到BSC链未配置，是否立即配置？', '提示', {
      type: 'info'
    }).then(() => {
      handleConfigClick('bsc')
    }).catch(() => {})
  }
}

// 初始化
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
// 容器样式
.global-monitor-container {
  height: 100%;
}

// Widget卡片基础样式
.widget-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid #EBEEF5;
  }
  
  :deep(.el-card__body) {
    flex: 1;
    padding: 20px;
    overflow-y: auto;
  }
  
  :deep(.el-card__footer) {
    padding: 12px 20px;
    border-top: 1px solid #EBEEF5;
    background: #FAFAFA;
  }
}

// Widget Header
.widget-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  
  .widget-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    
    .widget-icon {
      font-size: 20px;
      color: #409EFF;
    }
  }
}

// Widget Body
.widget-body {
  .config-stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 20px;
    
    .stat-card {
      padding: 16px;
      background: #F5F7FA;
      border-radius: 8px;
      border: 2px solid transparent;
      transition: all 0.3s;
      
      &.active {
        background: #E7F4FF;
        border-color: #409EFF;
      }
      
      &.disabled {
        opacity: 0.6;
      }
      
      .stat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 12px;
      }
      
      .stat-content {
        margin-bottom: 12px;
        
        .stat-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }
        
        .stat-value {
          font-size: 20px;
          font-weight: 600;
          color: #303133;
        }
      }
      
      .stat-footer {
        display: flex;
        justify-content: center;
      }
    }
  }
  
  .summary-info {
    display: flex;
    align-items: center;
    justify-content: space-around;
    padding: 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    
    .summary-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
      
      .label {
        font-size: 12px;
        color: rgba(255, 255, 255, 0.8);
      }
      
      .value {
        font-size: 24px;
        font-weight: 700;
        color: #FFFFFF;
      }
    }
    
    .summary-divider {
      width: 1px;
      height: 40px;
      background: rgba(255, 255, 255, 0.3);
    }
  }
}

// Widget Footer
.widget-footer {
  display: flex;
  gap: 8px;
  
  .el-button {
    flex: 1;
  }
}

// 事件卡片样式
.event-card {
  margin-bottom: 16px;
  
  &.disabled {
    opacity: 0.6;
  }
  
  :deep(.el-card__header) {
    padding: 12px 16px;
    background: #F5F7FA;
  }
  
  .event-title {
    font-weight: 500;
  }
  
  .event-config {
    padding-top: 8px;
  }
  
  .event-field {
    margin-bottom: 0;
  }
  
  .event-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #909399;
  }
}

// 条件预览样式
.monitor-preview {
  margin-top: 20px;
  
  .preview-title {
    font-weight: 600;
    margin-bottom: 8px;
    color: #409EFF;
  }
  
  .preview-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.6;
  }
}

.required-mark {
  color: #F56C6C;
  margin-right: 4px;
}
</style>

