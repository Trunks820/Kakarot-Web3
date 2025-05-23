<template>
  <div>
    <!-- TG热门CA列表 -->
    <el-card class="ca-card mb12" v-loading="hotTelegramLoading">
      <template #header>
        <div class="card-header">
          <span>热门CA</span>
          <div>
            <el-button
                type="text"
                @click="fetchHotCaByTelegramData"
                :loading="hotTelegramLoading"
                class="refresh-btn"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-tag type="primary" size="small" class="tag-primary">TG播报</el-tag>
          </div>
        </div>
      </template>
      <div class="card-body">
        <div v-for="(ca, index) in tgPopularCAs" :key="ca.address" class="ca-item">
          <div class="ca-header">
            <div class="left">
              <span class="ca-rank">{{ index + 1 }}</span>
              <span class="token-name">{{ ca.symbol }}</span>
            </div>
            <span class="ca-count">{{ ca.queryCount }}次</span>
          </div>
          <div class="ca-address" @click="copyToClipboard(ca.address)">
            {{ ca.address }}
            <i class="el-icon-document-copy copy-icon"></i>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 微信热门CA列表 -->
    <el-card class="ca-card wx-card" v-loading="hotWechatLoading">
      <template #header>
        <div class="card-header wx-header">
          <span>热门CA</span>
          <div>
            <el-button
                type="text"
                @click="fetchHotCaByWechatData"
                :loading="hotWechatLoading"
                class="refresh-btn"
            >
              <el-icon><Refresh /></el-icon>
            </el-button>
            <el-tag type="primary" size="small" class="tag-primary">微信查询</el-tag>
          </div>
        </div>
      </template>

      <div class="card-body">
        <div v-for="(ca, index) in wxPopularCAs" :key="ca.address" class="ca-item wx-item">
          <div class="ca-header">
            <div class="left">
              <span class="ca-rank wx-rank">{{ index + 1 }}</span>
              <span class="token-name">{{ ca.symbol }}</span>
            </div>
            <span class="ca-count">{{ ca.queryCount }}次</span>
          </div>
          <div class="ca-address" @click="copyToClipboard(ca.address)">
            {{ ca.address }}
            <i class="el-icon-document-copy copy-icon"></i>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getHotCaByWechat } from '@/api/crypto/index'

const hotWechatLoading = ref(false)
const hotTelegramLoading = ref(false)

// 热门CA数据
const tgPopularCAs = ref([])

// 微信热门CA数据
const wxPopularCAs = ref([])

// 添加热门Ca微信查询数据
const fetchHotCaByWechatData = async () => {
  hotWechatLoading.value = true
  try {
    // 添加模拟延迟，使行为与其他卡片一致
    const res = await new Promise((resolve) => {
      setTimeout(async () => {
        try {
          const apiRes = await getHotCaByWechat()
          resolve(apiRes)
        } catch (err) {
          resolve({ code: 500, msg: err.message })
        }
      }, 600)
    })

    if (res.code === 200 && res.data) {
      wxPopularCAs.value = res.data;
      hotWechatLoading.value = false;
    } else {
      ElMessage.warning('获取平台数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取平台数据异常，请检查网络连接或后端服务')
  } finally {
    hotWechatLoading.value = false
  }
}

const fetchHotCaByTelegramData = async () => {
  hotTelegramLoading.value = true
  try {
    // 添加模拟延迟，使行为与其他卡片一致
    const res = await new Promise((resolve) => {
      setTimeout(async () => {
        try {
          const apiRes = await getHotCaByWechat()
          resolve(apiRes)
        } catch (err) {
          resolve({ code: 500, msg: err.message })
        }
      }, 600)
    })

    if (res.code === 200 && res.data) {
      tgPopularCAs.value = res.data;
      hotTelegramLoading.value = false;
    } else {
      ElMessage.warning('获取平台数据失败: ' + (res.msg || '未知错误'))
    }
  } catch (error) {
    ElMessage.error('获取平台数据异常，请检查网络连接或后端服务')
  } finally {
    hotTelegramLoading.value = false
  }
}

// 复制到剪贴板
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage({
      message: '地址已复制到剪贴板',
      type: 'success'
    })
  }).catch(() => {
    ElMessage({
      message: '复制失败，请手动复制',
      type: 'error'
    })
  })
}
</script>

<style scoped lang="scss">
.ca-card {
  height: 321px;

  .card-body {
    padding: 6px;
    height: calc(100% - 50px);  // 减去header的高度
    overflow-y: auto;

    .ca-item {
      border-radius: 4px;
      padding: 6px;
      margin-bottom: 6px;
      transition: all 0.3s ease-in-out;
      background-color: #f5f7fa;

      &:last-child {
        margin-bottom: 0;
      }

      .ca-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 4px;

        .left {
          display: flex;
          align-items: center;
          gap: 4px;
          flex: 1;
          min-width: 0;

          .ca-rank {
            min-width: 18px;
            height: 18px;
            background: var(--el-color-primary);
            color: white;
            border-radius: 3px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 11px;
            flex-shrink: 0;
          }

          .token-name {
            font-size: 12px;
            font-weight: bold;
            color: #303133;
            margin-right: 4px;
            flex-shrink: 0;
          }
        }

        .ca-count {
          color: var(--el-color-success);
          font-weight: bold;
          white-space: nowrap;
          font-size: 11px;
          padding-left: 4px;
          flex-shrink: 0;
        }
      }

      .ca-address {
        font-family: 'Courier New', monospace;
        background-color: #ffffff;
        padding: 3px 5px;
        border-radius: 3px;
        color: #606266;
        cursor: pointer;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 11px;
        letter-spacing: -0.3px;
        line-height: 1.1;
        width: 100%;
        box-sizing: border-box;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        transition: all 0.3s ease-in-out;

        &:hover {
          background-color: #e9ecf2;
        }

        .copy-icon {
          color: var(--el-color-primary);
          margin-left: 3px;
          flex-shrink: 0;
          font-size: 11px;
        }
      }
    }
  }
}

/* 微信查询卡片样式增强 */
.wx-card {
  height: 321px;
  .wx-header {
    border-bottom-color: rgba(7, 193, 96, 0.2) !important;

    span {
      color: #333333 !important;
    }

    .wx-tag {
      display: flex;
      align-items: center;
      background-color: #07C160;
      color: white;
      font-weight: bold;
      padding: 3px 8px;
      border-radius: 3px;
      font-size: 11px;
      box-shadow: 0 2px 4px rgba(7, 193, 96, 0.2);

      .wx-icon {
        display: inline-block;
        width: 14px;
        height: 14px;
        background-color: white;
        margin-right: 4px;
        border-radius: 2px;
        position: relative;

        &:before {
          content: "";
          position: absolute;
          width: 10px;
          height: 10px;
          background-color: #07C160;
          top: 2px;
          left: 2px;
          border-radius: 1px;
        }
      }
    }
  }

  .wx-item {
    .wx-rank {
      background-color: #07C160 !important;
    }
    
    .wx-count {
      color: #07C160 !important;
    }
  }
}

/* Dark theme styles */
:global(html.dark) {
  .ca-card .card-body .ca-item {
    background-color: #1d1e1f !important;
    
    .token-name {
      color: #ffffff !important;
    }
    
    .ca-address {
      background-color: #141414 !important;
      color: #d0d0d0 !important;
      
      &:hover {
        background-color: #2d2d2d !important;
      }
    }
  }
  
  /* 微信查询暗黑模式 */
  .wx-card {
    border-color: #07C160 !important;
    
    .wx-header span {
      color: #ffffff !important;
    }
    
    .wx-item {
      border-left: 2px solid #07C160;
      background-color: #1d1e1f !important;
    }
  }
}

.tag-primary {
  font-weight: bold;
  box-shadow: 0 2px 4px rgba(64, 158, 255, 0.2);
}

.mb12 {
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  height: 16px;
}
</style> 