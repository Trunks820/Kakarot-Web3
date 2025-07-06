<template>
  <el-card class="popular-ca-panel">
    <template #header>
      <div class="panel-header">
        <div class="header-left">
          <el-icon><TrendCharts /></el-icon>
          <span>热门CA</span>
        </div>
        <div class="header-right">
          <el-tag type="success" size="small">5秒自动更新</el-tag>
        </div>
      </div>
    </template>

    <div class="ca-content">
      <!-- CA列表 -->
      <div class="ca-list" v-loading="loading">
        <div v-for="(ca, index) in tgPopularCAs" :key="ca.address" 
             class="ca-item" @click="handleCaClick(ca)">
          <div class="ca-icon">
            <div class="rank-circle" :class="getRankClass(index)">
              <span class="rank-number">{{ index + 1 }}</span>
            </div>
          </div>
          
          <div class="ca-info">
            <div class="ca-main">
              <span class="token-symbol">{{ ca.symbol }}</span>
              <span class="query-count">{{ ca.queryCount }}次</span>
            </div>
            <div class="ca-meta">
              <span class="ca-address-display">{{ formatAddress(ca.address) }}</span>
              <span class="ca-time">热度排名</span>
            </div>
          </div>
          
          <div class="ca-status">
            <div class="ca-actions">
              <el-button size="small" type="text" @click.stop="copyToClipboard(ca.address)">
                复制
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && tgPopularCAs.length === 0" class="empty-state">
        <el-icon><DataBoard /></el-icon>
        <p>暂无热门CA数据</p>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotCaByTelegram } from '@/api/crypto/caRecord'
import { 
  TrendCharts, DataBoard
} from '@element-plus/icons-vue'

const loading = ref(false)
const tgPopularCAs = ref([])
let updateTimer = null

// 获取TG热门CA数据
const fetchHotCaByTelegramData = async () => {
  loading.value = true
  try {
    const res = await getHotCaByTelegram()
    
    if (res.code === 200 && res.data) {
      tgPopularCAs.value = res.data
    } else {
      console.warn('获取TG数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    console.error('获取TG数据异常:', error)
    ElMessage.error('获取TG数据异常，请检查网络连接或后端服务')
  } finally {
    loading.value = false
  }
}

// 格式化地址显示
const formatAddress = (address) => {
  if (!address) return ''
  if (address.length <= 20) return address
  return `${address.slice(0, 10)}...${address.slice(-8)}`
}

// 复制到剪贴板 - 兼容HTTPS和HTTP环境
const copyToClipboard = (text) => {
  // 尝试使用现代API (仅HTTPS/localhost)
  if (navigator.clipboard && window.isSecureContext) {
    navigator.clipboard.writeText(text).then(() => {
      ElMessage.success('地址已复制到剪贴板')
    }).catch(() => {
      fallbackCopyTextToClipboard(text)
    })
  } else {
    // 回退到传统方案
    fallbackCopyTextToClipboard(text)
  }
}

// 兼容性复制方案
const fallbackCopyTextToClipboard = (text) => {
  const textArea = document.createElement('textarea')
  textArea.value = text
  
  // 避免滚动到底部
  textArea.style.top = '0'
  textArea.style.left = '0'
  textArea.style.position = 'fixed'
  textArea.style.opacity = '0'
  textArea.style.pointerEvents = 'none'
  textArea.style.zIndex = '-1'
  
  document.body.appendChild(textArea)
  textArea.focus()
  textArea.select()
  
  try {
    const successful = document.execCommand('copy')
    if (successful) {
      ElMessage.success('地址已复制到剪贴板')
    } else {
      ElMessage.error('复制失败，请手动复制')
    }
  } catch (err) {
    console.error('复制失败:', err)
    ElMessage.error('复制失败，请手动复制')
  }
  
  document.body.removeChild(textArea)
}

// 获取排名样式
const getRankClass = (index) => {
  if (index === 0) return 'rank-first'
  if (index === 1) return 'rank-second'
  if (index === 2) return 'rank-third'
  return 'rank-normal'
}

// 处理CA点击
const handleCaClick = (ca) => {
  ElMessage.info(`查看 ${ca.symbol} 详细信息`)
}

// 启动定时更新
const startAutoUpdate = () => {
  if (updateTimer) {
    clearInterval(updateTimer)
  }
  
  updateTimer = setInterval(() => {
    fetchHotCaByTelegramData()
  }, 5000)
}

// 组件挂载时自动获取数据并启动定时更新
onMounted(() => {
  fetchHotCaByTelegramData()
  startAutoUpdate()
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (updateTimer) {
    clearInterval(updateTimer)
  }
})
</script>

<style scoped lang="scss">
.popular-ca-panel {
  height: 400px;
  
  :deep(.el-card__header) {
    padding: 16px 20px;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 600;
      color: var(--el-text-color-primary);

      .el-icon {
        color: var(--el-color-success);
      }
    }
  }

  .ca-content {
    padding: 0;
    height: calc(100% - 60px);
    display: flex;
    flex-direction: column;

    .ca-list {
      flex: 1;
      overflow-y: auto;
      padding: 0 20px;

      .ca-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 16px 0;
        border-bottom: 1px solid var(--el-border-color-lighter);
        cursor: pointer;
        transition: background-color 0.2s ease;

        &:hover {
          background: var(--el-bg-color-page);
        }

        &:last-child {
          border-bottom: none;
        }

        .ca-icon {
          width: 28px;
          height: 28px;
          flex-shrink: 0;

          .rank-circle {
            width: 100%;
            height: 100%;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;

            .rank-number {
              font-size: 12px;
              font-weight: 600;
            }

            &.rank-first {
              background: linear-gradient(135deg, #FFD700, #FFA500);
            }

            &.rank-second {
              background: linear-gradient(135deg, #C0C0C0, #A0A0A0);
            }

            &.rank-third {
              background: linear-gradient(135deg, #CD7F32, #B8860B);
            }

            &.rank-normal {
              background: var(--el-color-success);
            }
          }
        }

        .ca-info {
          flex: 1;
          min-width: 0;

          .ca-main {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 2px;

            .token-symbol {
              font-weight: 600;
              color: var(--el-text-color-primary);
              font-size: 13px;
            }

            .query-count {
              color: var(--el-color-success);
              font-size: 12px;
              font-weight: 500;
              font-family: 'Courier New', monospace;
            }
          }

          .ca-meta {
            display: flex;
            justify-content: space-between;
            align-items: center;

            .ca-address-display {
              color: var(--el-text-color-secondary);
              font-size: 11px;
              font-family: 'Courier New', monospace;
              background: var(--el-fill-color-light);
              padding: 2px 6px;
              border-radius: 3px;
            }

            .ca-time {
              color: var(--el-text-color-secondary);
              font-size: 10px;
            }
          }
        }

        .ca-status {
          display: flex;
          align-items: center;
          flex-shrink: 0;

          .ca-actions {
            .el-button {
              font-size: 10px;
              padding: 2px 4px;
            }
          }
        }
      }
    }

    .empty-state {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--el-text-color-secondary);
      padding: 0 20px;

      .el-icon {
        font-size: 48px;
        color: var(--el-color-success);
        margin-bottom: 12px;
      }

      p {
        margin: 0;
        font-size: 14px;
      }
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .popular-ca-panel {
    height: auto;
    min-height: 300px;
    
    :deep(.el-card__header) {
      padding: 12px 16px;
    }
    
    .panel-header {
      .header-left {
        gap: 6px;
        font-size: 14px;
        
        .el-icon {
          font-size: 16px;
        }
      }
      
      .el-tag {
        font-size: 10px;
        height: 16px;
        line-height: 16px;
        padding: 0 4px;
      }
    }
    
    .ca-content {
      .ca-list {
        padding: 0 16px;
        
        .ca-item {
          gap: 8px;
          padding: 8px 0;
          min-height: 50px;
          
          .ca-icon {
            .rank-circle {
              width: 24px;
              height: 24px;
              
              .rank-number {
                font-size: 10px;
              }
            }
          }
          
          .ca-info {
            .ca-main {
              margin-bottom: 2px;
              
              .token-symbol {
                font-size: 12px;
              }
              
              .query-count {
                font-size: 11px;
              }
            }
            
            .ca-meta {
              .ca-address-display {
                font-size: 10px;
                padding: 1px 4px;
              }
              
              .ca-time {
                font-size: 9px;
              }
            }
          }
          
          .ca-status {
            .ca-actions {
              .el-button {
                font-size: 9px;
                padding: 1px 3px;
                height: 18px;
              }
            }
          }
        }
      }
      
      .empty-state {
        padding: 0 16px;
        
        .el-icon {
          font-size: 36px;
          margin-bottom: 8px;
        }
        
        p {
          font-size: 13px;
        }
      }
    }
  }
}

/* 小屏幕设备优化 */
@media (max-width: 480px) {
  .popular-ca-panel {
    min-height: 250px;
    
    :deep(.el-card__header) {
      padding: 10px 12px;
    }
    
    .panel-header {
      .header-left {
        font-size: 13px;
        
        .el-icon {
          font-size: 14px;
        }
      }
      
      .el-tag {
        font-size: 9px;
        height: 14px;
        line-height: 14px;
        padding: 0 3px;
      }
    }
    
    .ca-content {
      .ca-list {
        padding: 0 12px;
        
        .ca-item {
          gap: 6px;
          padding: 6px 0;
          min-height: 45px;
          
          .ca-icon {
            .rank-circle {
              width: 20px;
              height: 20px;
              
              .rank-number {
                font-size: 9px;
              }
            }
          }
          
          .ca-info {
            .ca-main {
              .token-symbol {
                font-size: 11px;
              }
              
              .query-count {
                font-size: 10px;
              }
            }
            
            .ca-meta {
              .ca-address-display {
                font-size: 9px;
                padding: 1px 3px;
              }
              
              .ca-time {
                font-size: 8px;
              }
            }
          }
          
          .ca-status {
            .ca-actions {
              .el-button {
                font-size: 8px;
                padding: 1px 2px;
                height: 16px;
              }
            }
          }
        }
      }
      
      .empty-state {
        padding: 0 12px;
        
        .el-icon {
          font-size: 32px;
          margin-bottom: 6px;
        }
        
        p {
          font-size: 12px;
        }
      }
    }
  }
}
</style> 