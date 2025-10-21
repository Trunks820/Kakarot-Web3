<template>
  <div class="welcome-header">
    <el-card shadow="never" class="welcome-card">
      <div class="welcome-content">
        <!-- 左侧：问候和日期 -->
        <div class="welcome-left">
          <div class="greeting">
            <span class="greeting-icon">{{ greetingIcon }}</span>
            <span class="greeting-text">{{ greetingText }}，{{ userName }}</span>
          </div>
          <div class="current-date">
            <el-icon><Calendar /></el-icon>
            <span>{{ currentDate }}</span>
          </div>
        </div>

        <!-- 右侧：系统状态和数据统计 -->
        <div class="welcome-right">
          <div class="stats-cards">
            <!-- 系统状态 -->
            <div class="stat-card">
              <div class="stat-icon" style="background: #F0F9FF; color: #409EFF;">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">系统状态</div>
                <div class="stat-value">运行正常</div>
              </div>
            </div>

            <!-- 今日新增Token -->
            <div class="stat-card clickable" @click="handleStatClick('newTokens')">
              <div class="stat-icon" style="background: #FDF6EC; color: #E6A23C;">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">今日新增</div>
                <div class="stat-value">{{ stats.newTokens }} <span class="stat-unit">个Token</span></div>
              </div>
            </div>

            <!-- 监控中Token -->
            <div class="stat-card clickable" @click="handleStatClick('monitoring')">
              <div class="stat-icon" style="background: #F0F9FF; color: #409EFF;">
                <el-icon><Monitor /></el-icon>
              </div>
              <div class="stat-content">
                <div class="stat-label">监控中</div>
                <div class="stat-value">{{ stats.monitoringCount }} <span class="stat-unit">个</span></div>
              </div>
            </div>

            <!-- 待处理事项 -->
            <div class="stat-card clickable alert" @click="handlePendingClick">
              <div class="stat-icon" style="background: #FEF0F0; color: #F56C6C;">
                <el-badge :value="pendingCount" :max="99" :hidden="pendingCount === 0" :offset="[8, -8]">
                  <el-icon><Bell /></el-icon>
                </el-badge>
              </div>
              <div class="stat-content">
                <div class="stat-label">待处理</div>
                <div class="stat-value">{{ pendingCount }} <span class="stat-unit">个事项</span></div>
              </div>
            </div>

            <!-- 配置按钮 -->
            <div class="stat-card config-button">
              <el-button 
                type="primary" 
                size="default"
                :icon="Setting"
                @click="handleConfigClick"
                style="width: 100%;"
              >
                配置工作台
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 待处理事项详情（可折叠） -->
      <el-collapse-transition>
        <div v-show="showPendingDetail" class="pending-detail">
          <el-divider />
          <div class="pending-list">
            <div 
              v-for="item in pendingItems" 
              :key="item.type"
              class="pending-item"
              :class="{ disabled: !item.route }"
              @click="handlePendingItemClick(item)"
            >
              <el-icon :color="item.color">
                <component :is="item.icon" />
              </el-icon>
              <span class="pending-label">{{ item.label }}:</span>
              <span class="pending-value">{{ item.count }} 个</span>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-collapse-transition>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getTodayNewTokens, getMonitoringCount } from '@/api/dashboard/stats'
import { 
  Calendar, 
  CircleCheck, 
  DataAnalysis, 
  Bell, 
  Setting,
  WarningFilled,
  Warning,
  ChatDotRound,
  ArrowRight,
  Monitor
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 发射事件
const emit = defineEmits(['config-click'])

// 用户名
const userName = computed(() => userStore.name || userStore.nickName || 'Admin')

// 问候语
const greetingIcon = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '🌙'
  if (hour < 12) return '🌅'
  if (hour < 18) return '☀️'
  return '🌆'
})

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const date = now.getDate()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[now.getDay()]
  return `${year}年${month}月${date}日 ${weekDay}`
})

// 统计数据
const stats = ref({
  newTokens: 0,
  monitoringCount: 0,
  systemStatus: 'normal'
})

// 待处理事项
const showPendingDetail = ref(false)
const pendingItems = ref([
  { 
    type: 'alert', 
    label: 'Token预警', 
    count: 0, 
    icon: WarningFilled, 
    color: '#F56C6C',
    route: { path: '/crypto/tokenMonitor', query: { chain: 'sol', monitorStatus: 'monitored' } }
  },
  { 
    type: 'push-failed', 
    label: '推送失败', 
    count: 0, 
    icon: Warning, 
    color: '#E6A23C',
    route: null // 暂不跳转
  },
  { 
    type: 'new-token', 
    label: '新增Token', 
    count: 0, 
    icon: DataAnalysis, 
    color: '#409EFF',
    route: { path: '/crypto/tokenMonitor', query: { chain: 'sol' } }
  },
  { 
    type: 'unread', 
    label: '未读通知', 
    count: 0, 
    icon: ChatDotRound, 
    color: '#909399',
    route: '#notifications'
  }
])

// 待处理总数
const pendingCount = computed(() => {
  return pendingItems.value.reduce((sum, item) => sum + item.count, 0)
})

// 加载统计数据
const loadStats = async () => {
  try {
    // 调用实际API获取今日新增Token数量
    const [newTokensRes, monitoringRes] = await Promise.all([
      getTodayNewTokens().catch(() => ({ data: 0 })),
      getMonitoringCount().catch(() => ({ data: 0 }))
    ])
    
    stats.value = {
      newTokens: newTokensRes.data || 0,
      monitoringCount: monitoringRes.data || 0,
      systemStatus: 'normal'
    }
    
    // TODO: 后续从API获取待处理事项统计
    pendingItems.value[0].count = stats.value.monitoringCount  // Token预警（使用监控中数量）
    pendingItems.value[1].count = 0  // 推送失败（暂时为0）
    pendingItems.value[2].count = stats.value.newTokens  // 新增Token（使用实际数量）
    pendingItems.value[3].count = 0  // 未读通知（暂时为0）
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 失败时使用默认值
    stats.value = {
      newTokens: 0,
      monitoringCount: 0,
      systemStatus: 'normal'
    }
  }
}

// 点击统计卡片
const handleStatClick = (type) => {
  if (type === 'newTokens') {
    // 今日新增 -> 跳转到tokenMonitor，SOL链
    router.push({
      path: '/crypto/tokenMonitor',
      query: { chain: 'sol' }
    })
  } else if (type === 'monitoring') {
    // 监控中 -> 跳转到tokenMonitor，SOL链，筛选已监控
    router.push({
      path: '/crypto/tokenMonitor',
      query: { 
        chain: 'sol',
        monitorStatus: 'monitored'
      }
    })
  }
}

// 点击待处理事项
const handlePendingClick = () => {
  showPendingDetail.value = !showPendingDetail.value
}

// 点击待处理详情
const handlePendingItemClick = (item) => {
  if (!item.route) {
    // 没有路由，不跳转
    return
  }
  
  if (item.route === '#notifications') {
    // 滚动到通知中心
    const notificationCenter = document.querySelector('.notification-center')
    if (notificationCenter) {
      notificationCenter.scrollIntoView({ behavior: 'smooth' })
    }
  } else if (typeof item.route === 'object') {
    // 路由对象，带query参数
    router.push(item.route)
  } else {
    // 字符串路由
    router.push(item.route)
  }
}

// 点击配置工作台
const handleConfigClick = () => {
  emit('config-click')
}

onMounted(() => {
  loadStats()
  
  // 每30秒刷新统计
  setInterval(loadStats, 30000)
})
</script>

<style scoped lang="scss">
.welcome-header {
  margin-bottom: 20px;
  
  .welcome-card {
    :deep(.el-card__body) {
      padding: 24px;
    }
  }
  
  .welcome-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 24px;
  }
  
  .welcome-left {
    flex: 1;
    
    .greeting {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      
      .greeting-icon {
        font-size: 28px;
      }
      
      .greeting-text {
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
    }
    
    .current-date {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #909399;
      
      .el-icon {
        font-size: 16px;
      }
    }
  }
  
  .welcome-right {
    .stats-cards {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 12px;
      
      .stat-card {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px;
        background: #FFFFFF;
        border: 1px solid #EBEEF5;
        border-radius: 8px;
        transition: all 0.3s;
        
        &.clickable {
          cursor: pointer;
          
          &:hover {
            border-color: #409EFF;
            box-shadow: 0 2px 12px rgba(64, 158, 255, 0.15);
            transform: translateY(-2px);
          }
        }
        
        &.config-button {
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 12px;
        }
        
        .stat-icon {
          width: 40px;
          height: 40px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          
          .el-icon {
            font-size: 20px;
          }
        }
        
        .stat-content {
          flex: 1;
          
          .stat-label {
            font-size: 12px;
            color: #909399;
            margin-bottom: 4px;
          }
          
          .stat-value {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            
            .stat-unit {
              font-size: 12px;
              font-weight: 400;
              color: #909399;
              margin-left: 2px;
            }
          }
        }
      }
    }
  }
  
  .pending-detail {
    .pending-list {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;
      
      .pending-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px;
        background: #F5F7FA;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #E6F7FF;
          transform: translateY(-2px);
        }
        
        &.disabled {
          opacity: 0.5;
          cursor: not-allowed;
          
          &:hover {
            background: #F5F7FA;
            transform: none;
          }
        }
        
        .el-icon {
          font-size: 20px;
        }
        
        .pending-label {
          font-size: 13px;
          color: #606266;
        }
        
        .pending-value {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
          margin-left: auto;
        }
        
        .arrow-icon {
          font-size: 14px;
          color: #909399;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1400px) {
  .welcome-right {
    .stats-cards {
      grid-template-columns: repeat(3, 1fr) !important;
    }
  }
}

@media (max-width: 1200px) {
  .welcome-content {
    flex-direction: column;
    align-items: flex-start !important;
  }
  
  .welcome-right {
    width: 100%;
    
    .stats-cards {
      grid-template-columns: repeat(2, 1fr) !important;
    }
  }
  
  .pending-list {
    grid-template-columns: repeat(2, 1fr) !important;
  }
}

@media (max-width: 768px) {
  .welcome-header {
    margin-bottom: 16px;
  }
  
  .welcome-card {
    :deep(.el-card__body) {
      padding: 16px !important;
    }
  }
  
  .welcome-left {
    .greeting {
      .greeting-icon {
        font-size: 24px;
      }
      
      .greeting-text {
        font-size: 20px;
      }
    }
  }
  
  .welcome-right {
    .stats-cards {
      grid-template-columns: 1fr !important;
      gap: 8px !important;
      
      .stat-card {
        padding: 12px;
      }
    }
  }
  
  .pending-list {
    grid-template-columns: 1fr !important;
  }
}
</style>

