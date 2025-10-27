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
              <!-- BSC卡片 -->
              <div class="stat-card" :class="{ active: bscConfig.external.enabled || bscConfig.internal.enabled }">
                <div class="stat-header">
                  <el-tag type="warning" size="small">BSC</el-tag>
                  <div style="display: flex; gap: 4px;">
                    <el-tag v-if="bscConfig.external.enabled" type="success" size="small" effect="plain">外盘✓</el-tag>
                    <el-tag v-if="bscConfig.internal.enabled" type="success" size="small" effect="plain">内盘✓</el-tag>
                  </div>
                </div>
                <div class="stat-content">
                  <div class="stat-label">配置状态</div>
                  <div class="stat-value" style="font-size: 16px;">
                    <span v-if="bscConfig.external.data || bscConfig.internal.data">
                      {{ (bscConfig.external.data ? 1 : 0) + (bscConfig.internal.data ? 1 : 0) }}/2 已配置
                    </span>
                    <span v-else style="color: #909399;">未配置</span>
                  </div>
                </div>
                <div class="stat-footer">
                  <el-button 
                    text 
                    size="small" 
                    @click="handleConfigClick('bsc')"
                    :icon="Setting"
                  >
                    管理配置
                  </el-button>
                </div>
              </div>

              <!-- SOL卡片（开发中） -->
              <div class="stat-card disabled">
                <div class="stat-header">
                  <el-tag type="success" size="small">SOL</el-tag>
                  <el-switch
                    v-model="solConfig.external.enabled"
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
                <span class="value">{{ todayAlertCount }}</span>
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
      <!-- 市场类型选择 -->
      <div style="margin-bottom: 20px; padding: 16px; background: #F5F7FA; border-radius: 8px;">
        <div style="font-size: 14px; font-weight: 600; color: #303133; margin-bottom: 12px;">
          📊 选择市场类型
        </div>
        <el-radio-group v-model="configDialog.form.marketType" size="large">
          <el-radio-button label="external">
            <span style="display: flex; align-items: center; gap: 6px;">
              <span>🌍</span>
              <span>外盘</span>
            </span>
          </el-radio-button>
          <el-radio-button label="internal">
            <span style="display: flex; align-items: center; gap: 6px;">
              <span>🏠</span>
              <span>内盘</span>
            </span>
          </el-radio-button>
        </el-radio-group>
      </div>

      

      <el-form :model="configDialog.form" label-width="120px">
        <!-- 基础配置 -->
        <el-form-item label="配置名称">
          <el-input
            v-model="configDialog.form.configName"
            placeholder="请输入配置名称"
            maxlength="100"
          />
        </el-form-item>

        <el-form-item label="单笔最小金额">
          <el-input-number
            v-model="configDialog.form.minTransactionUsd"
            :min="0"
            :precision="2"
            style="width: 200px"
          >
            <template #suffix>USD</template>
          </el-input-number>
          <span style="margin-left: 12px; color: #909399; font-size: 13px;">
            💡 单笔交易金额需大于此值
          </span>
        </el-form-item>

        <el-form-item label="累计最小金额">
          <el-input-number
            v-model="configDialog.form.cumulativeMinAmountUsd"
            :min="0"
            :precision="2"
            style="width: 200px"
          >
            <template #suffix>USD</template>
          </el-input-number>
          <span style="margin-left: 12px; color: #909399; font-size: 13px;">
            💡 累计交易金额需大于此值（可选）
          </span>
        </el-form-item>

        <el-form-item label="时间周期">
          <el-radio-group v-model="configDialog.form.timeInterval">
            <el-radio-button label="1m">1分钟</el-radio-button>
            <el-radio-button label="5m">5分钟</el-radio-button>
            <el-radio-button label="1h">1小时</el-radio-button>
          </el-radio-group>
          <span style="margin-left: 12px; color: #909399; font-size: 13px;">
            💡 监控API选择该时间段的交易量和涨跌幅
          </span>
        </el-form-item>

        <el-form-item label="前十持仓过滤">
          <el-input-number
            v-model="configDialog.form.topHoldersThreshold"
            :min="0"
            :max="100"
            :precision="1"
            style="width: 200px"
          >
            <template #suffix>%</template>
          </el-input-number>
          <span style="margin-left: 12px; color: #909399; font-size: 13px;">
            💡 前十持仓超过该百分比不播报（可选）
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
                💰 交易量阈值
              </el-checkbox>
            </div>
          </template>
          <div v-if="configDialog.events.volume.enabled" class="event-config">
            <el-form-item label="交易量阈值" label-position="top" class="event-field">
              <el-input-number
                v-model="configDialog.events.volume.threshold"
                :min="0"
                :max="100000000"
                :step="1000"
                :precision="0"
                style="width: 100%"
                placeholder="5000"
              />
              <div class="event-tip">💡 单位：USD，触发通知的最小交易量</div>
            </el-form-item>
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
        <el-table-column label="时间" prop="updateTime" width="180" />
        <el-table-column label="链" prop="chainType" width="80">
          <template #default="scope">
            <el-tag 
              :type="scope.row.chainType === 'bsc' ? 'warning' : 'success'" 
              size="small"
            >
              {{ scope.row.chainType.toUpperCase() }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="市场" prop="marketType" width="80">
          <template #default="scope">
            <el-tag 
              :type="scope.row.marketType === 'external' ? 'primary' : 'info'" 
              size="small"
              effect="plain"
            >
              {{ scope.row.marketType === 'external' ? '外盘' : '内盘' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="配置名称" prop="configName" width="180" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="80">
          <template #default="scope">
            <el-tag 
              :type="scope.row.status === '1' ? 'success' : 'info'" 
              size="small"
            >
              {{ scope.row.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="updateBy" width="100" />
        <el-table-column label="备注" prop="remark" show-overflow-tooltip />
      </el-table>
      
      <div v-if="logsDialog.logs.length === 0 && !logsDialog.loading" style="text-align: center; padding: 40px; color: #909399;">
        <el-icon style="font-size: 48px; margin-bottom: 16px;"><Document /></el-icon>
        <div>暂无配置日志</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch, getCurrentInstance } from 'vue'
import { Bell, Setting, Check, Close, Document, Tools } from '@element-plus/icons-vue'
import { 
  getGlobalMonitorByChain, 
  saveOrUpdateGlobalMonitor,
  changeGlobalMonitorStatus,
  getTodayAlertCount,
  getGlobalMonitorLogs
} from '@/api/crypto/globalMonitor'

const { proxy } = getCurrentInstance()

// 加载状态
const loading = ref(true)

// 今日预警数量
const todayAlertCount = ref(0)

// 配置数据结构：支持内盘和外盘
const bscConfig = reactive({
  external: {
    enabled: false,
    switching: false,
    data: null
  },
  internal: {
    enabled: false,
    switching: false,
    data: null
  }
})

// SOL配置（预留）
const solConfig = reactive({
  external: {
    enabled: false,
    switching: false,
    data: null
  },
  internal: {
    enabled: false,
    switching: false,
    data: null
  }
})

// 活跃配置数量
const activeConfigCount = computed(() => {
  let count = 0
  if (bscConfig.external.enabled && bscConfig.external.data) count++
  if (bscConfig.internal.enabled && bscConfig.internal.data) count++
  if (solConfig.external.enabled && solConfig.external.data) count++
  if (solConfig.internal.enabled && solConfig.internal.data) count++
  return count
})

// 监控事件总数
const totalEvents = computed(() => {
  let count = 0
  
  const countEventsForConfig = (configData) => {
    if (configData && configData.eventsConfig) {
      try {
        const events = JSON.parse(configData.eventsConfig)
        let eventCount = 0
        if (events.priceChange?.enabled) eventCount++
        if (events.holders?.enabled) eventCount++
        if (events.volume?.enabled) eventCount++
        return eventCount
      } catch (e) {
        console.error('解析事件配置失败:', e)
      }
    }
    return 0
  }
  
  count += countEventsForConfig(bscConfig.external.data)
  count += countEventsForConfig(bscConfig.internal.data)
  
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
    marketType: 'external',
    source: 'all',
    minTransactionUsd: 400,
    cumulativeMinAmountUsd: null,
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
      threshold: null
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
    if (volume.threshold) conditions.push(`交易量≥$${volume.threshold}`)
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
  if (newVal && !configDialog.events.volume.threshold) {
    configDialog.events.volume.threshold = 5000
  }
})

// 监听市场类型切换，自动加载对应配置
watch(() => configDialog.form.marketType, (newMarketType, oldMarketType) => {
  if (!configDialog.visible || !configDialog.chainType || newMarketType === oldMarketType) {
    return
  }
  
  // 加载对应市场类型的配置
  const chainConfig = configDialog.chainType === 'bsc' ? bscConfig : solConfig
  const config = chainConfig[newMarketType]
  
  if (config && config.data) {
    // 有配置，加载它（编辑模式）
    configDialog.form = {
      id: config.data.id, // 保留ID用于编辑
      configName: config.data.configName,
      chainType: config.data.chainType,
      marketType: config.data.marketType || newMarketType,
      source: config.data.source || 'all',
      minTransactionUsd: config.data.minTransactionUsd || 400,
      cumulativeMinAmountUsd: config.data.cumulativeMinAmountUsd || null,
      triggerLogic: config.data.triggerLogic || 'any',
      status: config.data.status,
      remark: config.data.remark || ''
    }
    
    // 解析事件配置
    if (config.data.eventsConfig) {
      try {
        const parsedEvents = JSON.parse(config.data.eventsConfig)
        if (parsedEvents.volume) {
          const { enabled, threshold } = parsedEvents.volume
          parsedEvents.volume = { enabled, threshold }
        }
        configDialog.events = parsedEvents
      } catch (e) {
        console.error('解析事件配置失败:', e)
      }
    } else {
      // 重置事件配置
      configDialog.events = {
        priceChange: { enabled: false, risePercent: null, fallPercent: null },
        holders: { enabled: false, increasePercent: null, decreasePercent: null },
        volume: { enabled: false, threshold: null }
      }
    }
    
    // 解析通知方式
    if (config.data.notifyMethods) {
      configDialog.notifyMethodsArray = config.data.notifyMethods.split(',')
    } else {
      configDialog.notifyMethodsArray = []
    }
  } else {
    // 无配置，重置为新建模式（清空ID，保留 chainType 和 marketType）
    const chainType = configDialog.form.chainType
    resetConfigForm()
    configDialog.form.id = null // ⚠️ 关键：清空ID，确保是新增而不是更新
    configDialog.form.chainType = chainType
    configDialog.form.marketType = newMarketType
    configDialog.form.configName = `${chainType.toUpperCase()}链${newMarketType === 'internal' ? '内盘' : '外盘'}监控`
  }
})

// 加载配置
const loadConfigs = async () => {
  loading.value = true
  
  try {
    // 加载BSC外盘配置
    const bscExternalRes = await getGlobalMonitorByChain('bsc', 'external')
    if (bscExternalRes.data) {
      bscConfig.external.data = bscExternalRes.data
      bscConfig.external.enabled = bscExternalRes.data.status === '1'
    }
    
    // 加载BSC内盘配置
    const bscInternalRes = await getGlobalMonitorByChain('bsc', 'internal')
    if (bscInternalRes.data) {
      bscConfig.internal.data = bscInternalRes.data
      bscConfig.internal.enabled = bscInternalRes.data.status === '1'
    }
    
    // 加载SOL配置（预留）
    // const solExternalRes = await getGlobalMonitorByChain('sol', 'external')
    // if (solExternalRes.data) {
    //   solConfig.external.data = solExternalRes.data
    //   solConfig.external.enabled = solExternalRes.data.status === '1'
    // }
  } catch (error) {
    console.error('加载配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载今日预警数量
const loadTodayAlertCount = async () => {
  try {
    const res = await getTodayAlertCount()
    if (res.code === 200 && res.data) {
      // 获取BSC链的预警数量（可以根据需要调整）
      todayAlertCount.value = res.data.bsc || res.data.total || 0
    }
  } catch (error) {
    console.error('加载今日预警数量失败:', error)
  }
}

// 切换状态
const handleStatusChange = async (chainType, marketType) => {
  const chainConfig = chainType === 'bsc' ? bscConfig : solConfig
  const config = chainConfig[marketType]
  
  if (!config.data) {
    proxy.$modal.msgWarning('请先配置监控规则')
    config.enabled = false
    return
  }
  
  config.switching = true
  
  try {
    const newStatus = config.enabled ? '1' : '0'
    await changeGlobalMonitorStatus(config.data.id, newStatus)
    const marketLabel = marketType === 'internal' ? '内盘' : '外盘'
    proxy.$modal.msgSuccess(`${chainType.toUpperCase()} ${marketLabel}配置已${config.enabled ? '启用' : '停用'}`)
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
  
  const chainConfig = chainType === 'bsc' ? bscConfig : solConfig
  
  // 优先加载外盘配置，如果外盘没有就加载内盘
  let initialMarketType = 'external'
  if (!chainConfig.external.data && chainConfig.internal.data) {
    initialMarketType = 'internal'
  }
  
  const config = chainConfig[initialMarketType]
  
  if (config && config.data) {
    // 编辑现有配置
    configDialog.form = {
      id: config.data.id,
      configName: config.data.configName,
      chainType: config.data.chainType,
      marketType: config.data.marketType || initialMarketType,
      source: config.data.source || 'all',
      minTransactionUsd: config.data.minTransactionUsd || 400,
      cumulativeMinAmountUsd: config.data.cumulativeMinAmountUsd || null,
      timeInterval: config.data.timeInterval || '5m',
      topHoldersThreshold: config.data.topHoldersThreshold || null,
      triggerLogic: config.data.triggerLogic || 'any',
      status: config.data.status,
      remark: config.data.remark || ''
    }
    
    // 解析事件配置
    if (config.data.eventsConfig) {
      try {
        const parsedEvents = JSON.parse(config.data.eventsConfig)
        
        // 清理 volume 字段中的旧格式数据
        if (parsedEvents.volume) {
          const { enabled, threshold } = parsedEvents.volume
          parsedEvents.volume = { enabled, threshold }
        }
        
        configDialog.events = parsedEvents
      } catch (e) {
        console.error('解析事件配置失败:', e)
      }
    }
    
    // 解析通知方式
    if (config.data.notifyMethods) {
      configDialog.notifyMethodsArray = config.data.notifyMethods.split(',')
    }
  } else {
    // 新建配置 - 默认外盘
    resetConfigForm()
    configDialog.form.chainType = chainType
    configDialog.form.marketType = 'external'
    configDialog.form.configName = `${chainType.toUpperCase()}链外盘监控`
  }
}

// 重置表单
const resetConfigForm = () => {
  configDialog.form = {
    id: null,
    configName: '',
    chainType: '',
    marketType: 'external',
    source: 'all',
    minTransactionUsd: 400,
    cumulativeMinAmountUsd: null,
    timeInterval: '5m',
    topHoldersThreshold: null,
    triggerLogic: 'any',
    status: '1',
    remark: ''
  }
  
  configDialog.events = {
    priceChange: { enabled: false, risePercent: null, fallPercent: null },
    holders: { enabled: false, increasePercent: null, decreasePercent: null },
    volume: { enabled: false, threshold: null }
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
const handleViewLogs = async () => {
  logsDialog.visible = true
  logsDialog.loading = true
  
  try {
    const res = await getGlobalMonitorLogs(20)
    if (res.code === 200 && res.data) {
      logsDialog.logs = res.data
    } else {
      logsDialog.logs = []
    }
  } catch (error) {
    console.error('加载配置日志失败:', error)
    logsDialog.logs = []
    proxy.$modal.msgError('加载配置日志失败')
  } finally {
    logsDialog.loading = false
  }
}

// 快速配置
const handleQuickConfig = () => {
  // 快速配置BSC链
  const hasConfig = bscConfig.external.data || bscConfig.internal.data
  
  if (hasConfig) {
    // 已有配置，直接打开配置页面
    handleConfigClick('bsc')
  } else {
    // 未配置，询问是否立即配置
    proxy.$modal.confirm('检测到BSC链未配置，是否立即配置外盘监控？', '提示', {
      type: 'info'
    }).then(() => {
      handleConfigClick('bsc')
    }).catch(() => {})
  }
}

// 定时器
let refreshTimer = null

// 初始化
onMounted(() => {
  loadConfigs()
  loadTodayAlertCount()
  
  // 每10秒刷新今日预警数量
  refreshTimer = setInterval(() => {
    loadTodayAlertCount()
  }, 10000)
})

// 清理定时器
onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
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

