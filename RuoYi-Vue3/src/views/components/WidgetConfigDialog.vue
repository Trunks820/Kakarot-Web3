<template>
  <el-dialog
    v-model="visible"
    title="配置工作台"
    width="500px"
    @close="handleClose"
  >
    <div v-loading="loading" class="config-content">
      <div class="config-section">
        <div class="section-title">
          <el-icon><Grid /></el-icon>
          <span>Widget管理</span>
        </div>
        <div class="section-desc">选择要在首页显示的Widget模块</div>
      </div>

      <div class="widget-list">
        <div
          v-for="widget in widgets"
          :key="widget.id"
          class="widget-item"
          :class="{ disabled: widget.comingSoon }"
        >
          <el-checkbox
            v-model="widget.enabled"
            :disabled="widget.comingSoon"
            @change="handleWidgetChange(widget)"
          >
            <div class="widget-info">
              <div class="widget-header-row">
                <span class="widget-icon">{{ widget.icon }}</span>
                <span class="widget-name">{{ widget.name }}</span>
                <el-tag
                  v-if="widget.comingSoon"
                  type="info"
                  size="small"
                  effect="plain"
                >
                  即将上线
                </el-tag>
              </div>
              <div class="widget-desc">{{ widget.description }}</div>
            </div>
          </el-checkbox>
        </div>
      </div>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="config-tip"
      >
        <template #title>
          <span>💡 提示：配置将立即生效，你可以随时调整显示的Widget</span>
        </template>
      </el-alert>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleReset">
          <el-icon><RefreshLeft /></el-icon>
          <span>重置为默认</span>
        </el-button>
        <div class="right-buttons">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="handleSave" :loading="saving">
            保存配置
          </el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Grid, RefreshLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

// 对话框显示状态
const visible = ref(false)
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadWidgetConfig()
  }
})
watch(visible, (val) => {
  emit('update:modelValue', val)
})

// Widget列表
const widgets = ref([
  {
    id: 'global-monitor',
    name: '链监控配置',
    icon: '🔔',
    description: '区块链全局监控配置，基于交易金额触发预警',
    enabled: true,
    sortOrder: 1,
    comingSoon: false
  },
  {
    id: 'wechat-bot',
    name: '微信机器人',
    icon: '💬',
    description: '微信群管理和自动回复功能',
    enabled: true,
    sortOrder: 2,
    comingSoon: true
  },
  {
    id: 'twitter-push',
    name: 'Twitter推送',
    icon: '🐦',
    description: 'Twitter推送状态监控',
    enabled: true,
    sortOrder: 3,
    comingSoon: true
  }
])

// 默认配置
const defaultWidgets = JSON.parse(JSON.stringify(widgets.value))

// 加载状态
const loading = ref(false)
const saving = ref(false)

// 加载配置
const loadWidgetConfig = async () => {
  loading.value = true
  try {
    // TODO: 调用实际API
    // const response = await getUserDashboardConfig()
    // if (response.data && response.data.length > 0) {
    //   response.data.forEach(config => {
    //     const widget = widgets.value.find(w => w.id === config.widgetId)
    //     if (widget) {
    //       widget.enabled = config.enabled
    //       widget.sortOrder = config.sortOrder
    //     }
    //   })
    // }
    
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 300))
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

// Widget启用/禁用变化
const handleWidgetChange = (widget) => {
  console.log(`Widget ${widget.name} ${widget.enabled ? '启用' : '禁用'}`)
}

// 重置为默认
const handleReset = () => {
  ElMessageBox.confirm(
    '确定要重置为默认配置吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    widgets.value = JSON.parse(JSON.stringify(defaultWidgets))
    ElMessage.success('已重置为默认配置')
  }).catch(() => {})
}

// 保存配置
const handleSave = async () => {
  // 检查是否至少启用一个Widget
  const enabledCount = widgets.value.filter(w => w.enabled && !w.comingSoon).length
  if (enabledCount === 0) {
    ElMessage.warning('请至少启用一个Widget')
    return
  }
  
  saving.value = true
  try {
    // TODO: 调用实际API
    // const configs = widgets.value.map(w => ({
    //   widgetId: w.id,
    //   enabled: w.enabled,
    //   sortOrder: w.sortOrder
    // }))
    // await batchUpdateDashboardConfig(configs)
    
    // 模拟延迟
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('保存成功')
    visible.value = false
    emit('refresh')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}
</script>

<style scoped lang="scss">
.config-content {
  .config-section {
    margin-bottom: 20px;
    
    .section-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 15px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      
      .el-icon {
        font-size: 18px;
        color: #409EFF;
      }
    }
    
    .section-desc {
      font-size: 13px;
      color: #909399;
      padding-left: 26px;
    }
  }
  
  .widget-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    margin-bottom: 20px;
    
    .widget-item {
      padding: 16px;
      border: 1px solid #DCDFE6;
      border-radius: 8px;
      transition: all 0.3s;
      
      &:hover:not(.disabled) {
        border-color: #409EFF;
        background: #F0F9FF;
      }
      
      &.disabled {
        opacity: 0.6;
        background: #FAFAFA;
      }
      
      :deep(.el-checkbox) {
        width: 100%;
        
        .el-checkbox__label {
          width: 100%;
          padding-left: 8px;
        }
      }
      
      .widget-info {
        .widget-header-row {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 6px;
          
          .widget-icon {
            font-size: 20px;
          }
          
          .widget-name {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
          }
          
          .el-tag {
            margin-left: auto;
          }
        }
        
        .widget-desc {
          font-size: 12px;
          color: #909399;
          line-height: 1.5;
          padding-left: 28px;
        }
      }
    }
  }
  
  .config-tip {
    :deep(.el-alert__content) {
      font-size: 13px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .right-buttons {
    display: flex;
    gap: 8px;
  }
}
</style>

