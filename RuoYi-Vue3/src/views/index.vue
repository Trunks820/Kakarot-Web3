<template>
  <div class="app-container home">
    <!-- 欢迎头部 -->
    <WelcomeHeader @config-click="showConfigDialog = true" />

    <!-- Widget展示区域 - 3列网格布局 -->
    <el-row :gutter="16" class="mt20 widget-row" align="stretch">
      <!-- Widget 1: 链监控配置 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
        <GlobalMonitorConfig />
      </el-col>
      
      <!-- Widget 2: Token监控 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
        <TokenQuickMonitorConfig />
      </el-col>
      
      <!-- Widget 3: 微信机器人（占位） -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" class="widget-col">
        <el-card class="widget-card placeholder-widget" shadow="hover">
          <template #header>
            <div class="widget-header">
              <div class="widget-title">
                <el-icon class="widget-icon"><ChatDotRound /></el-icon>
                <span>微信机器人</span>
              </div>
              <el-tag type="info" size="small" effect="plain">开发中</el-tag>
            </div>
          </template>
          
          <div class="placeholder-body">
            <el-empty description="功能开发中">
              <template #image>
                <el-icon :size="80" color="#909399"><Setting /></el-icon>
              </template>
              <el-button type="primary" plain disabled>即将上线</el-button>
            </el-empty>
          </div>
          
          <template #footer>
            <div class="widget-footer">
              <el-button size="small" disabled style="width: 100%;">
                <el-icon><Setting /></el-icon>
                <span>敬请期待</span>
              </el-button>
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 通知中心 -->
    <el-row :gutter="16" class="mt20">
      <el-col :span="24">
        <NotificationCenter />
      </el-col>
    </el-row>

    <!-- Widget配置对话框 -->
    <WidgetConfigDialog 
      v-model="showConfigDialog"
      @refresh="handleConfigRefresh"
    />
  </div>
</template>

<script setup name="Index">
import { ref, onMounted } from 'vue'
import { ChatDotRound, Setting } from '@element-plus/icons-vue'
import WelcomeHeader from '@/views/components/WelcomeHeader.vue'
import GlobalMonitorConfig from '@/views/components/GlobalMonitorConfig.vue'
import TokenQuickMonitorConfig from '@/views/components/TokenQuickMonitorConfig.vue'
import NotificationCenter from '@/views/components/NotificationCenter.vue'
import WidgetConfigDialog from '@/views/components/WidgetConfigDialog.vue'

// Widget配置对话框
const showConfigDialog = ref(false)

// 配置刷新
const handleConfigRefresh = () => {
  console.log('配置已更新，刷新页面Widget')
  // TODO: 根据配置重新渲染Widget
}

// 首页初始化
onMounted(() => {
  console.log('首页加载完成 - Widget模式')
})
</script>

<style scoped lang="scss">
.home {
  .mt20 {
    margin-top: 20px;
  }

  .mb20 {
    margin-bottom: 20px;
  }

  .ml20 {
    margin-left: 20px;
  }
  
  // Widget行样式
  .widget-row {
    .widget-col {
      margin-bottom: 20px;
      display: flex;
      flex-direction: column;
    }
  }
  
  // Widget卡片通用样式
  .widget-card {
    height: 100%;
    min-height: var(--widget-card-min-height); // 使用全局变量统一管理
    max-height: var(--widget-card-max-height); // 使用全局变量统一管理
    display: flex;
    flex-direction: column;
    transition: all 0.3s;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
    
    :deep(.el-card__header) {
      padding: 16px 20px;
      border-bottom: 1px solid #EBEEF5;
      background: #FAFAFA;
    }
    
    :deep(.el-card__body) {
      flex: 1;
      min-height: 0; // 关键：允许flex子元素正确收缩
      padding: 20px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
    }
    
    :deep(.el-card__footer) {
      padding: 12px 20px;
      border-top: 1px solid #EBEEF5;
    }
    
    // Widget Header样式
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
        }
      }
    }
    
    // 占位Widget样式
    &.placeholder-widget {
      .placeholder-body {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        
        :deep(.el-empty) {
          padding: 40px 0;
        }
      }
    }
    
    // Widget Footer样式
    .widget-footer {
      display: flex;
      justify-content: space-between;
      gap: 8px;
      
      .el-button {
        flex: 1;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .home {
    .el-row .el-col {
      margin-bottom: 16px;
    }
  }
}

/* 移动端适配 */
@media (max-width: 768px) {
  .home {
    padding: 10px;
    
    .mt20 {
      margin-top: 16px;
    }
    
    .widget-row {
      .widget-col {
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
    }
    
    .widget-card {
      min-height: 280px;
      
      &:hover {
        transform: none;
      }
    }
  }
}

/* 超小屏幕优化 */
@media (max-width: 480px) {
  .home {
    padding: 8px;
    
    .mt20 {
      margin-top: 12px;
    }
    
    .widget-card {
      min-height: 250px;
    }
  }
}
</style>

