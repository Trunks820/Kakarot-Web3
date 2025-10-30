<template>
  <el-card class="widget-card monitor-widget" shadow="hover">
    <!-- Header -->
    <template #header>
      <div class="widget-header">
        <div class="widget-title">
          <el-icon class="widget-icon"><MagicStick /></el-icon>
          <span>Token监控</span>
        </div>
        <div class="header-actions">
          <el-tag 
            :type="hasConfig ? 'success' : 'info'" 
            size="small"
            effect="plain"
          >
            {{ hasConfig ? `${configs.length}个配置段` : '未配置' }}
          </el-tag>
          <el-button 
            size="small" 
            text 
            :icon="Refresh" 
            :loading="loading"
            @click="loadConfigs"
            title="刷新数据"
          />
        </div>
      </div>
    </template>

    <!-- Body -->
    <el-skeleton :loading="loading" animated>
      <template #template>
        <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 12px" />
        <el-skeleton-item variant="text" style="width: 100%; height: 40px; margin-bottom: 12px" />
      </template>
      
      <template #default>
        <div class="config-content">
          <!-- 空状态 -->
          <div v-if="configs.length === 0" class="empty-state">
            <el-icon :size="48" color="#909399"><Setting /></el-icon>
            <div class="empty-text">还未设置智能监控策略</div>
            <div class="empty-desc">根据Token市值自动匹配监控规则</div>
          </div>
          
          <!-- 配置列表区域 -->
          <div v-else class="config-overview">
            
            <!-- 配置列表 -->
            <TransitionGroup name="config-list" tag="div" class="config-list">
              <div v-for="config in sortedConfigs" :key="config.id" class="config-item">
                <div class="config-info">
                  <div class="config-header-row">
                    <span class="config-label">≥ {{ formatMarketCap(config.minMarketCap) }}</span>
                    <div class="config-tags">
                      <el-tag size="small" type="success" effect="plain">
                        {{ config.tokenCount || 0 }} CA
                      </el-tag>
                      <el-tag size="small" type="info" effect="plain">
                        {{ getEnabledEventsCount(config) }}个事件
                      </el-tag>
                    </div>
                  </div>
                  <span class="config-desc">{{ getConfigDesc(config) }}</span>
                  <!-- 覆盖率进度条 -->
                  <el-progress 
                    :percentage="getConfigCoverage(config)"
                    :stroke-width="4"
                    :show-text="false"
                    :color="getProgressColor(config.tokenCount)"
                    style="margin-top: 6px;"
                  />
                </div>
              </div>
            </TransitionGroup>
            
            <!-- 最后更新时间 -->
            <div class="last-update" v-if="lastUpdateTime">
              <el-text size="small" type="info">
                <el-icon><Clock /></el-icon>
                最后更新: {{ lastUpdateTime }}
              </el-text>
            </div>
          </div>
        </div>
      </template>
    </el-skeleton>

    <!-- Footer -->
    <template #footer>
      <div class="widget-footer">
        <el-button size="small" @click="openConfigDialog" style="width: 100%;">
          <el-icon><Setting /></el-icon>
          <span>配置管理</span>
        </el-button>
      </div>
    </template>
  </el-card>

  <!-- 配置管理弹窗 -->
  <el-dialog
    v-model="dialogVisible"
    title="Token智能监控配置"
    :width="'min(800px, 90vw)'"
    @close="handleDialogClose"
  >
    <div class="dialog-content">
      <el-alert 
        title="💡 根据Token历史最高市值自动匹配监控策略" 
        type="info" 
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <div style="font-size: 13px; line-height: 1.6;">
            • 从高到低匹配：系统会选择第一个满足条件的配置<br>
            • 最低门槛 300K：低于此市值的Token不会被监控<br>
            • 应用时覆盖：会覆盖已有的监控配置
          </div>
        </template>
      </el-alert>

      <!-- 快速配置模板 -->
      <div class="config-templates" style="margin-bottom: 20px;">
        <el-alert type="success" :closable="false">
          <template #title>
            <span style="font-weight: 600;">🚀 快速开始</span>
          </template>
          <div style="display: flex; gap: 8px; margin-top: 12px; flex-wrap: wrap;">
            <el-button size="small" @click="applyTemplate('conservative')">
              <el-icon><TrendCharts /></el-icon>
              保守策略（低频通知）
            </el-button>
            <el-button size="small" type="primary" @click="applyTemplate('balanced')">
              <el-icon><DataLine /></el-icon>
              均衡策略（推荐）
            </el-button>
            <el-button size="small" type="warning" @click="applyTemplate('aggressive')">
              <el-icon><Lightning /></el-icon>
              激进策略（高频通知）
            </el-button>
          </div>
        </el-alert>
      </div>

      <!-- 配置列表 -->
      <div class="config-manager">
        <div class="config-header">
          <h4>配置列表（从高到低匹配）</h4>
          <div class="header-actions-group">
            <!-- 批量操作 -->
            <el-dropdown v-if="editConfigs.length > 0" size="small" style="margin-right: 8px;">
              <el-button size="small">
                批量操作<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="toggleAllEvents('priceChange', true)">
                    全部启用涨跌幅
                  </el-dropdown-item>
                  <el-dropdown-item @click="toggleAllEvents('holders', true)">
                    全部启用持币人
                  </el-dropdown-item>
                  <el-dropdown-item @click="toggleAllEvents('volume', true)">
                    全部启用交易量
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="toggleAllEvents('all', false)">
                    全部禁用
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="primary" size="small" icon="Plus" @click="addConfig">
              添加配置段
            </el-button>
          </div>
        </div>

        <div v-if="editConfigs.length === 0" class="empty-configs">
          <el-empty description="暂无配置，点击上方按钮添加" :image-size="80" />
        </div>

        <TransitionGroup name="config-list" tag="div" class="configs-list">
          <el-card 
            v-for="(config, index) in editConfigs" 
            :key="config.id"
            class="config-card"
            shadow="never"
          >
            <div class="config-card-header">
              <div class="config-number">{{ index + 1 }}</div>
              <div class="config-title">
                <el-input-number
                  v-model="config.minMarketCap"
                  :min="300000"
                  :step="100000"
                  :precision="0"
                  controls-position="right"
                  style="width: 180px;"
                  @change="handleConfigChange"
                />
                <span style="margin: 0 8px;">美元</span>
                <el-tag size="small" type="info">
                  {{ formatMarketCap(config.minMarketCap) }}
                </el-tag>
                <!-- 实时预览匹配数量 -->
                <el-popover placement="top" :width="220" trigger="hover">
                  <template #reference>
                    <el-tag size="small" type="warning" effect="plain" style="margin-left: 8px;">
                      <el-icon><View /></el-icon> 预计 {{ predictMatchCount(config) }} 个
                    </el-tag>
                  </template>
                  <div style="font-size: 12px; line-height: 1.6;">
                    根据当前市值门槛 <strong>{{ formatMarketCap(config.minMarketCap) }}</strong>，
                    预计会匹配到 <strong style="color: #E6A23C;">{{ predictMatchCount(config) }}</strong> 个Token
                  </div>
                </el-popover>
              </div>
              <el-button type="danger" text icon="Delete" @click="removeConfig(index)">
                删除
              </el-button>
            </div>

            <el-divider style="margin: 12px 0" />

            <!-- 监控事件配置 -->
            <div class="config-events">
              <el-row :gutter="16">
                <el-col :span="8">
                  <div class="event-item">
                    <el-checkbox v-model="config.events.priceChange.enabled">
                      <span style="font-weight: 500;">📈 涨跌幅</span>
                    </el-checkbox>
                    <div v-if="config.events.priceChange.enabled" style="margin-top: 8px;">
                      <el-input-number
                        v-model="config.events.priceChange.risePercent"
                        :min="0"
                        :max="1000"
                        placeholder="涨幅%"
                        size="small"
                        style="width: 100%;"
                      />
                      <el-input-number
                        v-model="config.events.priceChange.fallPercent"
                        :min="0"
                        :max="100"
                        placeholder="跌幅%"
                        size="small"
                        style="width: 100%; margin-top: 4px;"
                      />
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="event-item">
                    <el-checkbox v-model="config.events.holders.enabled">
                      <span style="font-weight: 500;">👥 持币人</span>
                    </el-checkbox>
                    <div v-if="config.events.holders.enabled" style="margin-top: 8px;">
                      <el-input-number
                        v-model="config.events.holders.increasePercent"
                        :min="0"
                        :max="1000"
                        placeholder="增加%"
                        size="small"
                        style="width: 100%;"
                      />
                      <el-input-number
                        v-model="config.events.holders.decreasePercent"
                        :min="0"
                        :max="100"
                        placeholder="减少%"
                        size="small"
                        style="width: 100%; margin-top: 4px;"
                      />
                    </div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="event-item">
                    <el-checkbox v-model="config.events.volume.enabled">
                      <span style="font-weight: 500;">📊 交易量</span>
                    </el-checkbox>
                    <div v-if="config.events.volume.enabled" style="margin-top: 8px;">
                      <el-input-number
                        v-model="config.events.volume.threshold"
                        :min="0"
                        :step="1000"
                        placeholder="阈值USD"
                        size="small"
                        style="width: 100%;"
                      />
                    </div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </el-card>
        </TransitionGroup>
      </div>

      <!-- 配置验证提示 -->
      <el-alert 
        v-if="validationIssues.length > 0" 
        type="warning" 
        :closable="false"
        style="margin-top: 16px;"
      >
        <template #title>
          <span style="font-weight: 600;">⚠️ 配置建议</span>
        </template>
        <div style="font-size: 13px; line-height: 1.8;">
          <div v-for="(issue, idx) in validationIssues" :key="idx">
            • {{ issue }}
          </div>
        </div>
      </el-alert>

      <!-- 通知方式 -->
      <div style="margin-top: 20px; padding: 16px; background: var(--notify-bg, #f5f7fa); border-radius: 8px;">
        <div style="font-weight: 500; margin-bottom: 12px;">📢 通知方式（所有配置共用，默认Web通知）</div>
        <el-checkbox-group v-model="notifyMethods">
          <el-checkbox label="telegram">📱 Telegram</el-checkbox>
          <el-checkbox label="wechat">💬 微信</el-checkbox>
        </el-checkbox-group>
        <div style="margin-top: 8px; font-size: 12px; color: #909399;">
          💡 Web通知始终启用，这里只需勾选额外的推送方式
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveConfigs" :loading="saving">
        保存配置
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  MagicStick, Setting, Plus, Delete, Refresh, Clock, View,
  TrendCharts, DataLine, Lightning, ArrowDown, Document, Coin, Check
} from '@element-plus/icons-vue'
import { getQuickMonitorStats, batchSaveQuickMonitor, predictTokenCounts } from '@/api/crypto/quickMonitor'

// 当前链类型（默认SOL）
const currentChain = ref('sol')

// 状态
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)

// 配置数据
const configs = ref([])
const editConfigs = ref([])
const notifyMethods = ref([])
const lastUpdateTime = ref('')

// Token数量预测结果缓存
const tokenCountPredictions = ref(new Map())
const validationIssues = ref([])

// 计算属性
const hasConfig = computed(() => configs.value.length > 0)

const sortedConfigs = computed(() => {
  return [...configs.value].sort((a, b) => b.minMarketCap - a.minMarketCap)
})

// 统计数据
const totalTokenCount = computed(() => {
  return configs.value.reduce((sum, config) => sum + (config.tokenCount || 0), 0)
})

const coverageRate = computed(() => {
  if (totalTokenCount.value === 0) return 0
  // 假设覆盖率基于配置的完整度
  const totalPossible = configs.value.length * 100
  return Math.min(Math.round((totalTokenCount.value / totalPossible) * 100), 100)
})

const highestThreshold = computed(() => {
  if (configs.value.length === 0) return 0
  return Math.max(...configs.value.map(c => c.minMarketCap))
})

// 格式化市值
const formatMarketCap = (value) => {
  if (value >= 1000000) return `${(value / 1000000).toFixed(0)}M`
  if (value >= 1000) return `${(value / 1000).toFixed(0)}K`
  return value.toString()
}

// 获取配置描述
const getConfigDesc = (config) => {
  const parts = []
  if (config.events.priceChange.enabled) {
    parts.push(`涨跌±${config.events.priceChange.risePercent || config.events.priceChange.fallPercent}%`)
  }
  if (config.events.holders.enabled) {
    parts.push(`持币人±${config.events.holders.increasePercent || config.events.holders.decreasePercent}%`)
  }
  if (config.events.volume.enabled) {
    parts.push(`交易量${(config.events.volume.threshold / 1000).toFixed(0)}K`)
  }
  return parts.join(' | ') || '未配置'
}

// 获取启用的事件数量
const getEnabledEventsCount = (config) => {
  if (!config.events) return 0
  let count = 0
  if (config.events.priceChange?.enabled) count++
  if (config.events.holders?.enabled) count++
  if (config.events.volume?.enabled) count++
  return count
}

// 获取配置覆盖率（基于Token数量）
const getConfigCoverage = (config) => {
  if (!config.tokenCount || totalTokenCount.value === 0) return 0
  return Math.round((config.tokenCount / totalTokenCount.value) * 100)
}

// 获取进度条颜色
const getProgressColor = (tokenCount) => {
  if (tokenCount > 50) return '#67C23A'
  if (tokenCount > 20) return '#E6A23C'
  return '#909399'
}

// 预测匹配Token数量（从API获取实时数据）
const predictMatchCount = (config) => {
  const prediction = tokenCountPredictions.value.get(config.minMarketCap)
  return prediction ? prediction.tokenCount : 0
}

// 更新Token数量预测
const updateTokenPredictions = async () => {
  if (editConfigs.value.length === 0) {
    tokenCountPredictions.value.clear()
    return
  }
  
  try {
    // 提取所有市值门槛
    const marketCapList = editConfigs.value.map(config => config.minMarketCap)
    
    // 调用API获取预测结果
    const response = await predictTokenCounts(marketCapList)
    
    if (response && response.code === 200 && response.data) {
      // 更新预测缓存
      const newPredictions = new Map()
      response.data.forEach(item => {
        newPredictions.set(item.minMarketCap, {
          tokenCount: item.tokenCount,
          maxMarketCap: item.maxMarketCap
        })
      })
      tokenCountPredictions.value = newPredictions
    }
  } catch (error) {
    console.error('获取Token数量预测失败:', error)
  }
}

// 加载配置
const loadConfigs = async () => {
  loading.value = true
  try {
    console.log('开始加载配置统计，链类型:', currentChain.value)
    const response = await getQuickMonitorStats(currentChain.value)
    console.log('📊 统计接口响应:', response)
    
    if (response && response.code === 200 && response.data) {
      configs.value = response.data.map(item => ({
        id: item.id,
        minMarketCap: parseFloat(item.minMarketCap),
        events: JSON.parse(item.eventsConfig || '{}'),
        tokenCount: item.tokenCount || 0
      }))
      
      // 获取通知方式（从第一个配置）
      if (response.data.length > 0 && response.data[0].notifyMethods) {
        const methods = response.data[0].notifyMethods.trim()
        notifyMethods.value = methods ? methods.split(',') : []
      } else {
        notifyMethods.value = []
      }
      
      console.log('✅ 加载完成，配置数量:', configs.value.length)
      console.log('✅ 配置内容:', configs.value)
      
      // 更新最后更新时间
      lastUpdateTime.value = new Date().toLocaleString('zh-CN', { 
        month: '2-digit', 
        day: '2-digit', 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    } else {
      console.warn('⚠️ 响应数据为空')
      configs.value = []
    }
  } catch (error) {
    console.error('❌ 加载配置异常:', error)
    ElMessage.error('加载配置失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 打开配置弹窗
const openConfigDialog = () => {
  editConfigs.value = JSON.parse(JSON.stringify(configs.value))
  dialogVisible.value = true
  // 注意：不需要手动调用 updateTokenPredictions()，watch 会自动触发
}

// 添加配置
const addConfig = () => {
  const newConfig = {
    id: Date.now(),
    minMarketCap: 300000,
    events: {
      priceChange: { enabled: true, risePercent: 50, fallPercent: 30 },
      holders: { enabled: true, increasePercent: 30, decreasePercent: 20 },
      volume: { enabled: true, threshold: 5000 }
    },
    tokenCount: 0
  }
  editConfigs.value.push(newConfig)
  // 注意：不需要手动调用 updateTokenPredictions()，watch 会自动触发
}

// 删除配置
const removeConfig = (index) => {
  editConfigs.value.splice(index, 1)
  // 触发验证
  validateConfigs()
  // 注意：不需要手动调用 updateTokenPredictions()，watch 会自动触发
}

// 配置模板
const configTemplates = {
  conservative: [
    {
      minMarketCap: 10000000,
      events: {
        priceChange: { enabled: true, risePercent: 100, fallPercent: 50 },
        holders: { enabled: true, increasePercent: 50, decreasePercent: 30 },
        volume: { enabled: false, threshold: 10000 }
      }
    },
    {
      minMarketCap: 5000000,
      events: {
        priceChange: { enabled: true, risePercent: 80, fallPercent: 40 },
        holders: { enabled: true, increasePercent: 40, decreasePercent: 25 },
        volume: { enabled: false, threshold: 8000 }
      }
    },
    {
      minMarketCap: 1000000,
      events: {
        priceChange: { enabled: true, risePercent: 60, fallPercent: 30 },
        holders: { enabled: false, increasePercent: 30, decreasePercent: 20 },
        volume: { enabled: false, threshold: 5000 }
      }
    }
  ],
  balanced: [
    {
      minMarketCap: 10000000,
      events: {
        priceChange: { enabled: true, risePercent: 50, fallPercent: 30 },
        holders: { enabled: true, increasePercent: 30, decreasePercent: 20 },
        volume: { enabled: true, threshold: 10000 }
      }
    },
    {
      minMarketCap: 5000000,
      events: {
        priceChange: { enabled: true, risePercent: 40, fallPercent: 25 },
        holders: { enabled: true, increasePercent: 25, decreasePercent: 15 },
        volume: { enabled: true, threshold: 7000 }
      }
    },
    {
      minMarketCap: 1000000,
      events: {
        priceChange: { enabled: true, risePercent: 30, fallPercent: 20 },
        holders: { enabled: true, increasePercent: 20, decreasePercent: 10 },
        volume: { enabled: true, threshold: 5000 }
      }
    },
    {
      minMarketCap: 300000,
      events: {
        priceChange: { enabled: true, risePercent: 50, fallPercent: 30 },
        holders: { enabled: false, increasePercent: 30, decreasePercent: 20 },
        volume: { enabled: false, threshold: 3000 }
      }
    }
  ],
  aggressive: [
    {
      minMarketCap: 10000000,
      events: {
        priceChange: { enabled: true, risePercent: 20, fallPercent: 15 },
        holders: { enabled: true, increasePercent: 15, decreasePercent: 10 },
        volume: { enabled: true, threshold: 5000 }
      }
    },
    {
      minMarketCap: 5000000,
      events: {
        priceChange: { enabled: true, risePercent: 15, fallPercent: 10 },
        holders: { enabled: true, increasePercent: 10, decreasePercent: 8 },
        volume: { enabled: true, threshold: 3000 }
      }
    },
    {
      minMarketCap: 1000000,
      events: {
        priceChange: { enabled: true, risePercent: 10, fallPercent: 8 },
        holders: { enabled: true, increasePercent: 8, decreasePercent: 5 },
        volume: { enabled: true, threshold: 2000 }
      }
    },
    {
      minMarketCap: 300000,
      events: {
        priceChange: { enabled: true, risePercent: 30, fallPercent: 20 },
        holders: { enabled: true, increasePercent: 20, decreasePercent: 10 },
        volume: { enabled: true, threshold: 1000 }
      }
    }
  ]
}

// 应用配置模板
const applyTemplate = (templateName) => {
  const template = configTemplates[templateName]
  if (!template) return
  
  editConfigs.value = template.map((config, index) => ({
    id: Date.now() + index,
    minMarketCap: config.minMarketCap,
    events: JSON.parse(JSON.stringify(config.events)),
    tokenCount: 0
  }))
  
  // 触发验证
  validateConfigs()
  
  ElMessage.success(`已应用${templateName === 'conservative' ? '保守' : templateName === 'balanced' ? '均衡' : '激进'}策略模板`)
}

// 批量操作事件
const toggleAllEvents = (eventType, enabled) => {
  if (eventType === 'all') {
    editConfigs.value.forEach(config => {
      config.events.priceChange.enabled = enabled
      config.events.holders.enabled = enabled
      config.events.volume.enabled = enabled
    })
  } else {
    editConfigs.value.forEach(config => {
      if (config.events[eventType]) {
        config.events[eventType].enabled = enabled
      }
    })
  }
  
  // 触发验证
  validateConfigs()
  
  ElMessage.success(enabled ? '已全部启用' : '已全部禁用')
}

// 配置变更处理
const handleConfigChange = () => {
  // 触发验证
  validateConfigs()
}

// 智能验证配置
const validateConfigs = () => {
  const issues = []
  
  if (editConfigs.value.length === 0) {
    validationIssues.value = []
    return
  }
  
  // 检查市值区间是否有重叠或间隔太小
  const sorted = [...editConfigs.value].sort((a, b) => b.minMarketCap - a.minMarketCap)
  for (let i = 0; i < sorted.length - 1; i++) {
    const gap = sorted[i].minMarketCap - sorted[i + 1].minMarketCap
    if (gap < 100000) {
      issues.push(`配置${i + 1}和配置${i + 2}的市值区间过小（${formatMarketCap(gap)}），建议至少相差100K`)
    }
  }
  
  // 检查事件阈值是否合理
  editConfigs.value.forEach((config, i) => {
    // 检查涨跌幅
    if (config.events.priceChange.enabled) {
      if (config.events.priceChange.risePercent > 500) {
        issues.push(`配置${i + 1}：涨幅阈值过高（${config.events.priceChange.risePercent}%），可能错过重要信号`)
      }
      if (config.events.priceChange.risePercent < 5 || config.events.priceChange.fallPercent < 5) {
        issues.push(`配置${i + 1}：涨跌幅阈值过低（涨${config.events.priceChange.risePercent}% / 跌${config.events.priceChange.fallPercent}%），可能产生大量通知`)
      }
    }
    
    // 检查持币人变化
    if (config.events.holders.enabled) {
      if (config.events.holders.increasePercent < 5 || config.events.holders.decreasePercent < 5) {
        issues.push(`配置${i + 1}：持币人变化阈值过低，可能产生大量通知`)
      }
    }
    
    // 检查是否至少启用一个事件
    const hasEvent = Object.values(config.events).some(e => e.enabled)
    if (!hasEvent) {
      issues.push(`配置${i + 1}：未启用任何监控事件`)
    }
  })
  
  validationIssues.value = issues
}

// 监听编辑配置变化
watch(editConfigs, () => {
  if (dialogVisible.value) {
    validateConfigs()
  }
}, { deep: true })

// 保存配置
const saveConfigs = async () => {
  // 验证
  if (editConfigs.value.length === 0) {
    ElMessage.warning('请至少添加一个配置段')
    return
  }
  
  for (const config of editConfigs.value) {
    if (config.minMarketCap < 300000) {
      ElMessage.warning('最低市值不能低于 300K')
      return
    }
    
    const hasEvent = Object.values(config.events).some(e => e.enabled)
    if (!hasEvent) {
      ElMessage.warning('每个配置段至少要启用一个监控事件')
      return
    }
  }
  
  // 如果有验证问题，询问是否继续
  if (validationIssues.value.length > 0) {
    try {
      await ElMessageBox.confirm(
        `<div style="line-height: 1.8; max-height: 300px; overflow-y: auto;">
          ${validationIssues.value.map(issue => `⚠️ ${issue}`).join('<br>')}
          <br><br><strong>是否仍要继续保存？</strong>
        </div>`,
        '配置建议',
        {
          confirmButtonText: '继续保存',
          cancelButtonText: '返回修改',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      )
    } catch {
      return
    }
  }
  
  // 通知方式可以为空，Web通知是默认的
  
  saving.value = true
  
  try {
    // 转换为后端格式
    const templates = editConfigs.value.map((config, index) => ({
      chainType: currentChain.value,
      minMarketCap: config.minMarketCap,
      configName: `配置${index + 1}`,
      eventsConfig: JSON.stringify(config.events),
      notifyMethods: notifyMethods.value.length > 0 ? notifyMethods.value.join(',') : '',
      triggerLogic: 'any',
      sortOrder: editConfigs.value.length - index,
      status: '1'
    }))
    
    // 批量保存
    await batchSaveQuickMonitor(currentChain.value, templates)
    
    ElMessage.success('配置保存成功')
    dialogVisible.value = false
    
    // 重新加载
    await loadConfigs()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  editConfigs.value = []
}

// 初始化
// 监听editConfigs的变化，自动更新Token预测（使用防抖避免频繁调用）
let updateTimer = null
watch(() => editConfigs.value.map(c => c.minMarketCap).join(','), () => {
  if (dialogVisible.value) {
    // 防抖：300ms内只执行最后一次（后端限制200ms，留100ms缓冲）
    if (updateTimer) clearTimeout(updateTimer)
    updateTimer = setTimeout(() => {
      updateTokenPredictions()
    }, 300)
  }
})

onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.monitor-widget {
  // 高度由父容器统一控制，限制最大高度让内容可滚动
  height: 100%;
  min-height: var(--widget-card-min-height); // 使用全局变量统一管理
  max-height: var(--widget-card-max-height); // 使用全局变量统一管理
  display: flex;
  flex-direction: column;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 20px;
    flex: 1;
    min-height: 0; // 关键：允许flex子元素正确收缩
    overflow-y: auto;
    display: flex;
    flex-direction: column;
  }

  :deep(.el-card__footer) {
    padding: 12px 20px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

.widget-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .widget-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);

    .widget-icon {
      font-size: 18px;
      color: #409EFF;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.config-content {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;

  .empty-text {
    margin-top: 16px;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }

  .empty-desc {
    margin-top: 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

// 配置概览
.config-overview {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
  min-height: 0; // 关键：允许flex子元素正确收缩
  overflow: hidden; // 防止内容溢出
}

// 简洁的统计信息
.quick-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  flex-wrap: wrap;

  .stat-badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 10px;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color);
    border-radius: 4px;
    font-size: 13px;
    color: var(--el-text-color-regular);
    transition: all 0.3s ease;

    &:hover {
      border-color: #409EFF;
      color: #409EFF;
      transform: translateY(-1px);
    }

    &.success {
      color: #67C23A;
      border-color: #67C23A;
      
      &:hover {
        background: rgba(103, 194, 58, 0.1);
      }
    }

    .el-icon {
      font-size: 14px;
    }
  }
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0; // 关键：允许flex子元素正确收缩
  overflow-y: auto;
  overflow-x: hidden;
}

.config-item {
  padding: 10px 14px;
  background: var(--config-item-bg, #f5f7fa);
  border-radius: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;

  &:hover {
    background: var(--config-item-hover-bg, #ebeef5);
    border-color: #409EFF;
    transform: translateX(4px);
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.15);
  }

  .config-info {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;

    .config-header-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;
    }

    .config-label {
      font-size: 14px;
      font-weight: 600;
      color: #409EFF;
    }

    .config-tags {
      display: flex;
      gap: 6px;
    }

    .config-desc {
      font-size: 12px;
      color: var(--el-text-color-secondary);
      margin-bottom: 4px;
    }
  }
}

.last-update {
  padding: 8px 0;
  text-align: center;
  border-top: 1px solid var(--el-border-color-lighter);
  margin-top: auto;
  
  :deep(.el-text) {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
}

.widget-footer {
  display: flex;
  justify-content: space-between;
  gap: 8px;

  .el-button {
    flex: 1;
  }
}

// 弹窗样式
.dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

.config-manager {
  .config-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h4 {
      margin: 0;
      font-size: 15px;
      color: var(--el-text-color-primary);
    }

    .header-actions-group {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.empty-configs {
  padding: 40px 0;
}

.configs-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.config-card {
  border: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
  transition: all 0.3s ease;

  &:hover {
    border-color: #409EFF;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
  }

  .config-card-header {
    display: flex;
    align-items: center;
    gap: 12px;

    .config-number {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 50%;
      font-weight: 600;
      font-size: 14px;
      flex-shrink: 0;
      box-shadow: 0 2px 6px rgba(102, 126, 234, 0.4);
    }

    .config-title {
      flex: 1;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
    }
  }

  .config-events {
    .event-item {
      padding: 12px;
      background: var(--event-item-bg, #f5f7fa);
      border-radius: 6px;
      min-height: 120px;
      transition: background 0.3s ease;

      &:hover {
        background: var(--event-item-hover-bg, #ebeef5);
      }
    }
  }
}

// 动画效果
.config-list-move,
.config-list-enter-active,
.config-list-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.config-list-enter-from {
  opacity: 0;
  transform: translateY(-20px);
}

.config-list-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.config-list-leave-active {
  position: absolute;
  width: 100%;
}

// 暗黑模式适配
:root[class~="dark"] {
  .config-item {
    --config-item-bg: rgba(255, 255, 255, 0.05);
    --config-item-hover-bg: rgba(255, 255, 255, 0.1);
  }

  .config-card {
    .config-events {
      .event-item {
        --event-item-bg: rgba(255, 255, 255, 0.05);
        --event-item-hover-bg: rgba(255, 255, 255, 0.08);
      }
    }
  }
}

// 响应式优化
@media (max-width: 768px) {
  .quick-stats {
    gap: 6px;
    padding: 8px;

    .stat-badge {
      font-size: 12px;
      padding: 3px 8px;
    }
  }

  .config-item {
    .config-info {
      .config-header-row {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }
    }
  }

  .config-card {
    .config-card-header {
      .config-title {
        font-size: 13px;
      }
    }
  }
}
</style>

