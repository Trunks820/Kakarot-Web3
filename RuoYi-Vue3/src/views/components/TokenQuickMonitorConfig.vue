<template>
  <el-card class="widget-card monitor-widget" shadow="hover">
    <!-- Header -->
    <template #header>
      <div class="widget-header">
        <div class="widget-title">
          <el-icon class="widget-icon"><MagicStick /></el-icon>
          <span>Token监控</span>
        </div>
        <el-tag 
          :type="hasConfig ? 'success' : 'info'" 
          size="small"
          effect="plain"
        >
          {{ hasConfig ? `${configs.length}个配置段` : '未配置' }}
        </el-tag>
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
          
          <!-- 配置列表 -->
          <div v-else class="config-list">
            <div v-for="config in sortedConfigs" :key="config.id" class="config-item">
              <div class="config-info">
                <span class="config-label">≥ {{ formatMarketCap(config.minMarketCap) }}</span>
                <span class="config-desc">{{ getConfigDesc(config) }}</span>
              </div>
              <el-tag size="small" type="success" effect="plain">
                {{ config.tokenCount || 0 }} CA
              </el-tag>
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

      <!-- 配置列表 -->
      <div class="config-manager">
        <div class="config-header">
          <h4>配置列表（从高到低匹配）</h4>
          <el-button type="primary" size="small" icon="Plus" @click="addConfig">
            添加配置段
          </el-button>
        </div>

        <div v-if="editConfigs.length === 0" class="empty-configs">
          <el-empty description="暂无配置，点击上方按钮添加" :image-size="80" />
        </div>

        <div v-else class="configs-list">
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
                />
                <span style="margin: 0 8px;">美元</span>
                <el-tag size="small" type="info">
                  {{ formatMarketCap(config.minMarketCap) }}
                </el-tag>
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
        </div>
      </div>

      <!-- 通知方式 -->
      <div style="margin-top: 20px; padding: 16px; background: #f5f7fa; border-radius: 8px;">
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, Setting, Plus, Delete } from '@element-plus/icons-vue'
import { getQuickMonitorStats, batchSaveQuickMonitor } from '@/api/crypto/quickMonitor'

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

// 计算属性
const hasConfig = computed(() => configs.value.length > 0)

const sortedConfigs = computed(() => {
  return [...configs.value].sort((a, b) => b.minMarketCap - a.minMarketCap)
})

// 格式化市值
const formatMarketCap = (value) => {
  if (value >= 10000000) return `${(value / 10000000).toFixed(0)}千万`
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
}

// 删除配置
const removeConfig = (index) => {
  editConfigs.value.splice(index, 1)
}

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
onMounted(() => {
  loadConfigs()
})
</script>

<style scoped lang="scss">
.monitor-widget {
  height: 400px;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  :deep(.el-card__body) {
    padding: 20px;
    height: calc(100% - 120px);
    overflow-y: auto;
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
    color: #303133;

    .widget-icon {
      font-size: 18px;
      color: #409EFF;
    }
  }
}

.config-content {
  height: 100%;
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
    color: #606266;
  }

  .empty-desc {
    margin-top: 8px;
    font-size: 13px;
    color: #909399;
  }
}

.config-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.config-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;

  &:hover {
    background: #ebeef5;
  }

  .config-info {
    display: flex;
    flex-direction: column;
    gap: 4px;

    .config-label {
      font-size: 14px;
      font-weight: 600;
      color: #409EFF;
    }

    .config-desc {
      font-size: 12px;
      color: #909399;
    }
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
      color: #303133;
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
  border: 1px solid #e4e7ed;

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
      background: #409EFF;
      color: white;
      border-radius: 50%;
      font-weight: 600;
      font-size: 14px;
      flex-shrink: 0;
    }

    .config-title {
      flex: 1;
      display: flex;
      align-items: center;
    }
  }

  .config-events {
    .event-item {
      padding: 12px;
      background: #f5f7fa;
      border-radius: 6px;
      min-height: 120px;
    }
  }
}
</style>

