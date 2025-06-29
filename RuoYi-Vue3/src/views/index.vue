<template>
  <div class="app-container home">
    <!-- 顶部扩展数据卡片 -->
    <EnhancedDataSummary />

    <!-- 中间内容区域 - 三栏布局 -->
    <el-row :gutter="16" class="mt20 main-content-row">
      <!-- 预警中心 -->
      <el-col :span="8" :xs="24" class="content-col">
        <AlertPanel />
      </el-col>
      <!-- 监控代币 -->
      <el-col :span="8" :xs="24" class="content-col">
        <MonitorTokens />
      </el-col>
      <!-- 热门CA -->
      <el-col :span="8" :xs="24" class="content-col">
        <PopularCA />
      </el-col>
    </el-row>

    <!-- 底部OKX信号区域 -->
    <el-row :gutter="16" class="mt20">
      <el-col :span="24">
        <OkxSignalPreview />
      </el-col>
    </el-row>

  </div>
</template>

<script setup name="Index">
import { ref, onMounted, nextTick } from 'vue'
import EnhancedDataSummary from '@/views/components/EnhancedDataSummary.vue'
import AlertPanel from '@/views/components/AlertPanel.vue'
import PopularCA from "./components/PopularCA.vue"
import MonitorTokens from '@/views/components/MonitorTokens.vue'
import OkxSignalPreview from '@/views/components/OkxSignalPreview.vue'

// 首页初始化
onMounted(() => {
  console.log('首页加载完成')
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
    
    .main-content-row {
      .content-col {
        margin-bottom: 16px;
        
        &:last-child {
          margin-bottom: 0;
        }
      }
      
      // 移动端减少间距
      .el-row {
        --el-row-gutter: 8px;
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
    
    .main-content-row {
      .content-col {
        margin-bottom: 12px;
      }
    }
  }
}
</style>

