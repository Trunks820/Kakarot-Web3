<template>
  <div class="app-container">
    <!-- 始终显示的主要内容区域 -->
    <el-row :gutter="20">
      <!-- 左侧K线图区域 -->
      <el-col :span="16">
        <el-card shadow="never">
          <!-- 代币基本信息 -->
          <template #header>
            <!-- 顶部搜索区域：左侧搜索框 + 右侧主流币 -->
            <div class="top-search-area">
              <!-- 左侧：搜索框 -->
              <div class="search-compact">
                <el-autocomplete
                  v-model="searchCA"
                  placeholder="输入合约地址..."
                  class="search-input-compact"
                  size="default"
                  clearable
                  :fetch-suggestions="fetchSuggestions"
                  @select="handleSelect"
                  @keyup.enter="searchToken"
                  @paste="handlePaste"
                  :trigger-on-focus="true"
                  popper-class="search-history-popper"
                  :debounce="300"
                >
                  <template #prefix>
                    <el-icon class="search-icon-compact"><Search /></el-icon>
                  </template>
                  <template #suffix>
                    <el-button 
                      @click="pasteFromClipboard"
                      size="small"
                      text
                      class="paste-btn-compact"
                      title="粘贴"
                    >
                      <el-icon><DocumentCopy /></el-icon>
                    </el-button>
                  </template>
                  <template #default="{ item }">
                    <div class="history-suggestion">
                      <div class="suggestion-main">
                        <span class="suggestion-symbol">{{ item.symbol }}</span>
                        <span class="suggestion-name">{{ item.name }}</span>
                      </div>
                      <div class="suggestion-address">{{ formatAddress(item.address) }}</div>
                    </div>
                  </template>
                </el-autocomplete>
                <el-button 
                  @click="searchToken" 
                  type="primary" 
                  class="search-btn-compact"
                  size="default"
                >
                  查询
                </el-button>
              </div>

              <!-- 右侧：主流币价格 -->
              <div class="main-coins-compact">
                <div 
                  v-for="coin in mainCoins" 
                  :key="coin.symbol"
                  class="coin-item-compact"
                >
                  <img :src="coin.icon" :alt="coin.symbol" class="coin-logo" />
                  <div class="coin-info">
                    <div class="coin-price-compact">
                      ${{ formatCoinPrice(coin.price) }}
                    </div>
                    <div 
                      :class="['coin-change-compact', coin.change24h >= 0 ? 'positive' : 'negative']"
                    >
                      {{ coin.change24h >= 0 ? '+' : '' }}{{ coin.change24h.toFixed(1) }}%
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 代币信息区域 - 仅在有数据时显示 -->
            <div v-if="tokenData" class="token-header-info">
              <!-- 代币基本信息 + 操作按钮 -->
              <div class="token-main-row">
                <div class="token-basic">
                  <el-avatar 
                    v-if="tokenData.logoUrl" 
                    :src="tokenData.logoUrl" 
                    :size="50"
                    class="token-avatar"
                  />
                  <div class="token-text">
                    <h3>{{ tokenData.symbol }} <span class="token-sub">{{ tokenData.name }}</span></h3>
                    <div class="price-social-row">
                      <div class="price-row">
                        <span class="price-main">${{ formatPrice(tokenData.price) }}</span>
                        <el-tag 
                          :type="tokenData.change24h >= 0 ? 'success' : 'danger'"
                          effect="plain"
                          size="small"
                          class="price-change-tag"
                        >
                          {{ tokenData.change24h >= 0 ? '+' : '' }}{{ tokenData.change24h }}%
                        </el-tag>
                      </div>
                      
                      <!-- 社交链接图标 -->
                      <div v-if="tokenData.socialLinks && tokenData.socialLinks.length > 0" class="social-icons">
                        <el-button
                          v-for="link in tokenData.socialLinks"
                          :key="link.type"
                          size="small"
                          @click="openSocialLink(link.url)"
                          circle
                          :title="link.type"
                          class="social-icon-btn"
                        >
                          <el-icon><Link /></el-icon>
                        </el-button>
                      </div>
                    </div>
                  </div>

                  <div class="action-buttons">
                    <el-button
                        class="action-btn small"
                        :type="getMonitorButtonType()"
                        size="small"
                        @click="toggleMonitor"
                        :disabled="monitorStatus === 'monitored'"
                        round
                        icon="Monitor"
                    >
                      {{ getMonitorButtonText() }}
                    </el-button>
                    <el-button
                        class="action-btn small"
                        size="small"
                        @click="openInExplorer"
                        round
                        icon="Link"
                    >
                      区块链
                    </el-button>
                  </div>

                  <!-- 统计小卡片 -->
                  <div class="mini-stats">
                    <div class="mini-card">
                      <div class="mini-value">{{ tokenData.queryCount || 0 }}</div>
                      <div class="mini-label">总查询</div>
                    </div>
                    <div class="mini-card">
                      <div class="mini-value">{{ tokenData.todayQueries || 0 }}</div>
                      <div class="mini-label">今日查询</div>
                    </div>
                    <div class="mini-card">
                      <div class="mini-value">{{ tokenData.monitorCount || 0 }}</div>
                      <div class="mini-label">监控人数</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- K线图 - 仅在有数据时显示 -->
            <div v-if="tokenData" class="chart-container">
              <iframe 
                ref="klineIframe"
                :src="getKlineUrl()"
                class="kline-iframe"
                frameborder="0"
                scrolling="no">
              </iframe>
            </div>

            <!-- 没有数据时的占位内容 -->
            <div v-else class="no-data-placeholder">
              <el-empty description="请输入代币地址开始分析" />
            </div>
          </template>
        </el-card>
      </el-col>

      <!-- 右侧信息区域 - 仅在有数据时显示 -->
      <el-col v-if="tokenData" :span="8">
        <!-- 基础数据 - 一行卡片 -->
        <div class="data-section">
          <div class="data-cards-row">
            <div 
              class="data-card"
              :class="{ 'data-updated': animationStates.dataCards }"
            >
              <div class="data-label">市值</div>
              <div 
                class="data-value"
                :class="{ 'value-updated': animationStates.dataValues }"
              >
                ${{ formatNumber(tokenData.marketCap) }}
              </div>
            </div>
            <div 
              class="data-card"
              :class="{ 'data-updated': animationStates.dataCards }"
            >
              <div class="data-label">池子</div>
              <div 
                class="data-value"
                :class="{ 'value-updated': animationStates.dataValues }"
              >
                ${{ formatNumber(tokenData.liquidity) }}
              </div>
            </div>
            <div 
              class="data-card"
              :class="{ 'data-updated': animationStates.dataCards }"
            >
              <div class="data-label">24h成交额</div>
              <div 
                class="data-value"
                :class="{ 'value-updated': animationStates.dataValues }"
              >
                ${{ formatNumber(tokenData.realtimeData.volume.h24) }}
              </div>
            </div>
            <div 
              class="data-card"
              :class="{ 'data-updated': animationStates.dataCards }"
            >
              <div class="data-label">持有者</div>
              <div 
                class="data-value"
                :class="{ 'value-updated': animationStates.dataValues }"
              >
                {{ formatNumber(tokenData.holderCount || 0) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 安全分析 -->
        <div class="data-section">
          <h4 class="section-title">🔒 安全分析</h4>
          <div v-if="securityData">
            <!-- 第一行：风险等级 + 风险提示 -->
            <div class="risk-level-row">
              <div 
                class="risk-card" 
                :class="[
                  getRiskLevelClass(securityData.riskLevel),
                  { 'risk-updated': animationStates.riskCard }
                ]"
              >
                {{ getRiskLevelText(securityData.riskLevel) }}
              </div>
              <div 
                v-if="securityData.riskTag" 
                class="risk-warning"
                :class="{ 'warning-updated': animationStates.riskWarning }"
              >
                {{ securityData.riskTag }}
              </div>
            </div>
                    
            <!-- 第二行：安全指标 -->
            <div class="security-metrics-row">
              <div 
                class="security-card" 
                :class="[
                  getConcentrationRiskClass(securityData.top10Percent),
                  { 'metric-updated': animationStates.securityMetrics }
                ]"
              >
                <div class="security-label">Top10</div>
                <div 
                  class="security-value"
                  :class="{ 'security-value-updated': animationStates.securityValues }"
                >
                  {{ formatPercent(securityData.top10Percent) }}
                </div>
              </div>
              <div 
                class="security-card" 
                :class="[
                  getFeeRiskClass(securityData.feeRate),
                  { 'metric-updated': animationStates.securityMetrics }
                ]"
              >
                <div class="security-label">交易税率</div>
                <div 
                  class="security-value"
                  :class="{ 'security-value-updated': animationStates.securityValues }"
                >
                  {{ formatPercent(securityData.feeRate) }}
                </div>
              </div>
              <div 
                class="security-card neutral"
                :class="{ 'metric-updated': animationStates.securityMetrics }"
              >
                <div class="security-label">持有数</div>
                <div 
                  class="security-value"
                  :class="{ 'security-value-updated': animationStates.securityValues }"
                >
                  {{ securityData.holders }}
                </div>
              </div>
              <div 
                class="security-card neutral"
                :class="{ 'metric-updated': animationStates.securityMetrics }"
              >
                <el-button 
                  size="small" 
                  @click="copyAddress(securityData.ownerAddress)"
                  :icon="DocumentCopy"
                  round
                >
                  开发者
                </el-button>
              </div>
            </div>
            
            <!-- 第三行：权限状态 -->
            <div class="permissions-row">
              <div 
                class="permission-card" 
                :class="[
                  !securityData.isMintable ? 'safe' : 'danger',
                  { 'permission-updated': animationStates.permissions }
                ]"
              >
                {{ securityData.isMintable ? '可增发' : '不可增发' }}
              </div>
              <div 
                class="permission-card" 
                :class="[
                  !securityData.isFreezable ? 'safe' : 'danger',
                  { 'permission-updated': animationStates.permissions }
                ]"
              >
                {{ securityData.isFreezable ? '可冻结' : '不可冻结' }}
              </div>
              <div 
                class="permission-card" 
                :class="[
                  !securityData.isClosable ? 'safe' : 'danger',
                  { 'permission-updated': animationStates.permissions }
                ]"
              >
                {{ securityData.isClosable ? '可销毁' : '不可销毁' }}
              </div>
              <div 
                class="permission-card" 
                :class="[
                  securityData.dexFlag ? 'safe' : 'danger',
                  { 'permission-updated': animationStates.permissions }
                ]"
              >
                {{ securityData.dexFlag ? '已上DEX' : '未上DEX' }}
              </div>
            </div>
          </div>
          
          <el-empty v-if="!securityData" description="暂无安全数据" />
        </div>

        <!-- 时间周期选择器 - 无标题，直接一行 -->
        <div class="data-section" v-if="tokenData.realtimeData">
          <div class="timeframe-cards-row">
            <div 
              v-for="timeframe in timeframes" 
              :key="timeframe.value"
              class="timeframe-card"
              :class="{ active: selectedTimeframe === timeframe.value }"
              @click="selectedTimeframe = timeframe.value"
            >
              <div class="timeframe-label">{{ timeframe.label }}</div>
              <div 
                :class="['timeframe-change', getChangeClass(getPriceChangeByTimeframe(timeframe.value))]"
              >
                {{ formatChange(getPriceChangeByTimeframe(timeframe.value)) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 交易统计 - 一行 -->
        <div class="data-section" v-if="tokenData.realtimeData">
          <div class="trading-cards-row">
            <div class="trading-card">
              <div class="trading-label">成交额</div>
              <div class="trading-value">${{ formatNumber(getSelectedVolume()) }}</div>
            </div>
            <div class="trading-card buy">
              <div class="trading-label">买入</div>
              <div class="trading-value">${{ formatNumber(getSelectedBuyVolume()) }}</div>
            </div>
            <div class="trading-card sell">
              <div class="trading-label">卖出</div>
              <div class="trading-value">${{ formatNumber(getSelectedSellVolume()) }}</div>
            </div>
            <div class="trading-card" :class="getNetBuyClass()">
              <div class="trading-label">净买入</div>
              <div class="trading-value">{{ getNetBuysFormatted() }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 价格提醒对话框 -->
    <el-dialog v-model="alertDialogVisible" title="设置价格提醒" width="500px">
      <el-form :model="alertForm" label-width="100px">
        <el-form-item label="提醒价格">
          <el-input v-model="alertForm.targetPrice" placeholder="请输入目标价格"></el-input>
        </el-form-item>
        <el-form-item label="提醒类型">
          <el-select v-model="alertForm.alertType" placeholder="请选择">
            <el-option label="价格上涨到" value="above"></el-option>
            <el-option label="价格下跌到" value="below"></el-option>
            <el-option label="涨跌幅超过" value="change"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="alertForm.notifyMethods">
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="alertDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmPriceAlert">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CryptoScanner">
import { ref, reactive, getCurrentInstance, onMounted, onUnmounted, watch } from 'vue'
import { Search, Link, DocumentCopy, ArrowDown, Delete } from '@element-plus/icons-vue'
import { tokenInfo, securityInfo, getTopCoin} from "@/api/crypto/index"
const { proxy } = getCurrentInstance()
let securityTimer = null

// onMounted(() => {
//   // 每隔15秒查一次
//   securityTimer = setInterval(() => {
//     getTokenInfo()
//   }, 15000)
// })
//
// onUnmounted(() => {
//   // 页面卸载时清除定时器，防止内存泄漏
//   if (securityTimer) {
//     clearInterval(securityTimer)
//     securityTimer = null
//   }
// })
// 响应式数据定义
const searchCA = ref('')
const searching = ref(false)
const tokenData = ref(null)
const monitoring = ref(false)
const settingAlert = ref(false)
const alertDialogVisible = ref(false)
const klineIframe = ref(null)
const securityData = ref(null)

// 动画控制状态
const animationStates = reactive({
  dataCards: false,
  dataValues: false,
  riskCard: false,
  riskWarning: false,
  securityMetrics: false,
  securityValues: false,
  permissions: false
})

const alertForm = reactive({
  targetPrice: '',
  alertType: 'above',
  notifyMethods: ['telegram']
})

const selectedTimeframe = ref('m5')
const timeframes = ref([
  { value: 'm5', label: '1m' },
  { value: 'h1', label: '1h' },
  { value: 'h6', label: '6h' },
  { value: 'h24', label: '24h' }
])

const monitorStatus = ref('not_monitored') // not_monitored, monitoring, monitored

// 搜索历史记录
const searchHistory = ref([])

// 主流币价格数据
const mainCoins = ref([
  { 
    symbol: 'BTC', 
    name: 'Bitcoin', 
    icon: '/src/assets/crypto-icons/BTC.png',
    price: 0, 
    change24h: 0, 
    volume24h: 0,
    high24h: 0,
    low24h: 0,
    coin: 'BTC_USDT'
  },
  { 
    symbol: 'ETH', 
    name: 'Ethereum', 
    icon: '/src/assets/crypto-icons/ETH.png',
    price: 0, 
    change24h: 0, 
    volume24h: 0,
    high24h: 0,
    low24h: 0,
    coin: 'ETH_USDT'
  },
  { 
    symbol: 'BNB', 
    name: 'BNB', 
    icon: '/src/assets/crypto-icons/BNB.png',
    price: 0, 
    change24h: 0, 
    volume24h: 0,
    high24h: 0,
    low24h: 0,
    coin: 'BNB_USDT'
  },
  { 
    symbol: 'SOL', 
    name: 'Solana', 
    icon: '/src/assets/crypto-icons/SOL.png',
    price: 0, 
    change24h: 0, 
    volume24h: 0,
    high24h: 0,
    low24h: 0,
    coin: 'SOL_USDT'
  }
])

let priceUpdateTimer = null

// 自动完成建议
const fetchSuggestions = (queryString, callback) => {
  // 如果没有输入内容，显示最近5条历史记录
  if (!queryString) {
    callback(searchHistory.value.slice(0, 5))
    return
  }
  
  // 如果没有历史记录，返回空数组
  if (searchHistory.value.length === 0) {
    callback([])
    return
  }
  
  // 过滤匹配的历史记录
  const suggestions = searchHistory.value.filter(item => {
    const query = queryString.toLowerCase()
    return (
      item.symbol.toLowerCase().includes(query) ||
      item.name.toLowerCase().includes(query) ||
      item.address.toLowerCase().includes(query)
    )
  })
  
  callback(suggestions.slice(0, 5)) // 最多显示5条
}

// 处理选择历史记录
const handleSelect = (item) => {
  searchCA.value = item.address
  searchToken()
}

// 填充示例地址（保留用于兼容）
const fillExample = (address) => {
  searchCA.value = address
  searchToken()
}

// 处理粘贴事件
const handlePaste = async (event) => {
  // 延迟一点处理，确保粘贴内容已经填入
  setTimeout(() => {
    // 自动检测并清理地址格式
    if (searchCA.value) {
      searchCA.value = searchCA.value.trim()
    }
  }, 100)
}

// 从剪贴板粘贴
const pasteFromClipboard = async () => {
  try {
    const text = await navigator.clipboard.readText()
    if (text) {
      searchCA.value = text.trim()
      proxy.$modal.msgSuccess('已粘贴地址')
    }
  } catch (err) {
    proxy.$modal.msgWarning('请手动粘贴地址')
  }
}

// 清空历史记录
const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('crypto_search_history')
  proxy.$modal.msgSuccess('历史记录已清空')
}

// 保存搜索历史
const saveToHistory = (tokenData) => {
  if (!tokenData || !tokenData.address) return
  
  const historyItem = {
    address: tokenData.address,
    symbol: tokenData.symbol,
    name: tokenData.name,
    timestamp: Date.now()
  }
  
  // 移除重复项
  searchHistory.value = searchHistory.value.filter(item => item.address !== tokenData.address)
  
  // 添加到开头
  searchHistory.value.unshift(historyItem)
  
  // 最多保存5条记录
  searchHistory.value = searchHistory.value.slice(0, 5)
  
  // 保存到本地存储
  localStorage.setItem('crypto_search_history', JSON.stringify(searchHistory.value))
}

// 加载历史记录
const loadHistory = () => {
  try {
    const saved = localStorage.getItem('crypto_search_history')
    if (saved) {
      searchHistory.value = JSON.parse(saved)
    }
  } catch (err) {
    console.warn('Failed to load search history:', err)
  }
}

// 格式化地址显示
const formatAddress = (address) => {
  if (!address || address.length < 8) return address || '--'
  return `${address.slice(0, 6)}...${address.slice(-4)}`
}

// 获取代币信息
const getTokenInfo = () => {
  searching.value = true
  // 调用API获取数据
  tokenInfo(searchCA.value).then(response => {
    searching.value = false

    if (response && response.data) {
      const tokenPair = response.data
      tokenData.value = {
        name: tokenPair.name,
        symbol: tokenPair.symbol,
        address: tokenPair.address,
        logoUrl: tokenPair.logoUrl || getChainLogo(tokenPair.chainId || 'sol'),
        price: parseFloat(tokenPair.price) || 0,
        change24h: tokenPair.priceChange?.h24 || 0,
        marketCap: tokenPair.marketCap || tokenPair.fdv || 0,
        volume24h: tokenPair.volume?.h24 || 0,
        high24h: calculateHigh24h(tokenPair),
        low24h: calculateLow24h(tokenPair),
        holderCount: tokenPair.holderCount, // DexScreener不提供这个数据
        liquidity: tokenPair.liquidity || 0,
        hasRenounced: false, // 需要其他API获取
        queryCount: tokenPair.queryCount || 0,
        todayQueries: tokenPair.todayQueries || 0,
        monitorCount: tokenPair.monitorCount || 0,
        // 新增：交易对信息
        pairInfo: {
          dexId: tokenPair.dexId,
          chainId: tokenPair.chainId,
          pairAddress: tokenPair.pairAddress,
          url: tokenPair.url,
          labels: tokenPair.labels || [],
          pairCreatedAt: tokenPair.pairCreatedAt
        },
        // 新增：实时交易数据
        realtimeData: processRealtimeData(tokenPair),
        // 新增：官方社媒链接
        socialLinks: extractSocialLinks(tokenPair)
      }
      // 自动获取安全数据
      getTokenSecurity(tokenData.value.address, tokenPair)
      
      // 保存到搜索历史
      saveToHistory(tokenData.value)
    } else {
      proxy.$modal.msgError('未找到该代币信息，请检查CA地址是否正确')
    }
  }).catch(error => {
    searching.value = false
    proxy.$modal.msgError('查询失败: ' + (error.message || '网络错误'))
    
    // 移除自动加载演示数据，避免无限递归
    tokenData.value = null
  })
}

const searchToken = () => {
  if (!searchCA.value) {
    proxy.$modal.msgWarning('请输入CA地址')
    return
  }

  // 重置状态，但保留tokenData避免布局切换
  monitorStatus.value = 'not_monitored'
  // tokenData.value = null  // 注释掉这行，避免布局切换

  getTokenInfo()
}

// 加载演示数据 - 移除自动调用getTokenInfo，使用静态模拟数据
const loadDemoToken = () => {
  searchCA.value = "So11111111111111111111111111111111111111112"

  getTokenInfo()
}

// 计算24小时最高价
const calculateHigh24h = (tokenPair) => {
  const currentPrice = parseFloat(tokenPair.priceUsd) || 0
  const change24h = tokenPair.priceChange?.h24 || 0

  if (change24h >= 0) {
    // 如果是正涨幅，当前价就是最高价
    return currentPrice
  } else {
    // 如果是负涨幅，计算24小时前的价格作为最高价
    return currentPrice / (1 + change24h / 100)
  }
}

// 计算24小时最低价
const calculateLow24h = (tokenPair) => {
  const currentPrice = parseFloat(tokenPair.priceUsd) || 0
  const change24h = tokenPair.priceChange?.h24 || 0

  if (change24h <= 0) {
    // 如果是负涨幅，当前价就是最低价
    return currentPrice
  } else {
    // 如果是正涨幅，计算24小时前的价格作为最低价
    return currentPrice / (1 + change24h / 100)
  }
}

// 根据公链类型获取logo
const getChainLogo = (chainId) => {
  const chainLogos = {
    'sol': '/src/assets/crypto-icons/SOL.png',
    'solana': '/src/assets/crypto-icons/SOL.png',
    'ethereum': '/src/assets/crypto-icons/ETH.png',
    'bsc': '/src/assets/crypto-icons/BNB.png',
    'base': '/src/assets/crypto-icons/BASE.png'
  }
  return chainLogos[chainId] || chainLogos['sol'] // 默认使用Solana logo
}

// 计算安全评分
const calculateSafetyScore = (tokenPair) => {
  let score = 60 // 基础分

  // 有网站 +10分
  if (tokenPair.info?.websites?.length) score += 10

  // 有社交媒体 +10分  
  if (tokenPair.info?.socials?.length) score += 10

  // 流动性充足 +15分
  if (tokenPair.liquidity?.usd > 100000) score += 15

  // 交易量活跃 +5分
  if (tokenPair.volume?.h24 > 10000) score += 5

  return Math.min(score, 100)
}

// 操作功能
const addToMonitor = () => {
  monitorStatus.value = 'monitoring'
  try {
    // 调用添加监控API
    simulateApiCall()
    monitorStatus.value = 'monitored'
    proxy.$modal.msgSuccess('添加监控成功！')
  } catch (error) {
    monitorStatus.value = 'not_monitored'
    proxy.$modal.error('添加监控失败')
  }
}

const confirmPriceAlert = () => {
  try {
    simulateApiCall()
    proxy.$modal.msgSuccess('价格提醒设置成功！')
  } catch (error) {
    proxy.$modal.error('设置失败')
  }
}

const openInExplorer = () => {
  if (tokenData.value) {
    window.open(`https://solscan.io/token/${tokenData.value.address}`, '_blank')
  }
}

const refreshTokenData = () => {
  searchToken()
}

// 生成gmgn K线图URL，根据公链动态调整
const getKlineUrl = () => {
  if (!tokenData.value || !tokenData.value.address) {
    return 'about:blank'
  }

  const chain = tokenData.value.pairInfo.chainId
  const chainMap = {
    'sol': 'sol',
    'solana': 'sol',
    'ethereum': 'eth',
    'bsc': 'bsc',
    'base': 'base'
  }

  const gmgnChain = chainMap[chain] || 'sol'
  return `https://www.gmgn.cc/kline/${gmgnChain}/${tokenData.value.address}`
}

// 工具函数
const formatPrice = (price) => {
  if (price >= 1) {
    return price.toFixed(4)
  } else {
    return price.toFixed(8)
  }
}

const formatNumber = (num) => {
  if (num >= 1e9) {
    return removeTrailingZero(num / 1e9) + 'B'
  } else if (num >= 1e6) {
    return removeTrailingZero(num / 1e6) + 'M'
  } else if (num >= 1e3) {
    return removeTrailingZero(num / 1e3) + 'K'
  }
  return removeTrailingZero(num)
}

function removeTrailingZero(n) {
  // 转成字符串，最多两位小数，然后去掉末尾多余的0和小数点
  return parseFloat(Number(n).toFixed(2)).toString()
}

// 格式化价格变化
const formatChange = (change) => {
  if (change === null || change === undefined) return '--'
  const sign = change >= 0 ? '+' : ''
  return `${sign}${change.toFixed(2)}%`
}

// 获取价格变化的样式类
const getChangeClass = (change) => {
  if (change === null || change === undefined) return 'neutral'
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
}

const simulateApiCall = () => {
  return new Promise(resolve => setTimeout(resolve, 1000))
}

const getSelectedVolume = () => {
  if (tokenData.value?.realtimeData && tokenData.value.realtimeData.volume) {
    const volume = tokenData.value.realtimeData.volume
    if (selectedTimeframe.value === 'h1') return volume?.h1 || 0
    if (selectedTimeframe.value === 'h6') return volume?.h6 || 0
    if (selectedTimeframe.value === 'h24') return volume?.h24 || 0
    if (selectedTimeframe.value === 'm5') return volume?.m5 || 0
  }
  return 0
}

const getSelectedBuys = () => {
  if (tokenData.value?.realtimeData && tokenData.value.realtimeData.txns) {
    const txns = tokenData.value.realtimeData.txns
    if (selectedTimeframe.value === 'h1') return txns?.h1?.buys || 0
    if (selectedTimeframe.value === 'h6') return txns?.h6?.buys || 0
    if (selectedTimeframe.value === 'h24') return txns?.h24?.buys || 0
    if (selectedTimeframe.value === 'm5') return txns?.m5?.buys || 0
  }
  return 0
}

const getSelectedSells = () => {
  if (tokenData.value?.realtimeData && tokenData.value.realtimeData.txns) {
    const txns = tokenData.value.realtimeData.txns
    if (selectedTimeframe.value === 'h1') return txns?.h1?.sells || 0
    if (selectedTimeframe.value === 'h6') return txns?.h6?.sells || 0
    if (selectedTimeframe.value === 'h24') return txns?.h24?.sells || 0
    if (selectedTimeframe.value === 'm5') return txns?.m5?.sells || 0
  }
  return 0
}

const getNetBuys = () => {
  if (tokenData.value?.realtimeData) {
    const buys = getSelectedBuys()
    const sells = getSelectedSells()
    return buys - sells
  }
  return 0
}

const getNetBuyClass = () => {
  if (tokenData.value?.realtimeData) {
    const netBuys = getNetBuys()
    if (netBuys < 0) return 'positive'
    if (netBuys > 0) return 'negative'
  }
  return 'neutral'
}

const getPriceChangeByTimeframe = (timeframe) => {
  if (tokenData.value?.realtimeData && tokenData.value.realtimeData.priceChange) {
    const priceChange = tokenData.value.realtimeData.priceChange
    if (timeframe === 'm5') return priceChange?.m5
    if (timeframe === 'h1') return priceChange?.h1
    if (timeframe === 'h6') return priceChange?.h6
    if (timeframe === 'h24') return priceChange?.h24
  }
  return null
}

const getSelectedBuyVolume = () => {
  if (tokenData.value?.realtimeData) {
    const volume = getSelectedVolume()
    // 假设买入占总交易量的一半作为示例
    return volume ? volume * 0.6 : 0
  }
  return 0
}

const getSelectedSellVolume = () => {
  if (tokenData.value?.realtimeData) {
    const volume = getSelectedVolume()
    // 假设卖出占总交易量的一半作为示例
    return volume ? volume * 0.4 : 0
  }
  return 0
}

const getNetBuysFormatted = () => {
  if (tokenData.value?.realtimeData) {
    const buyVolume = getSelectedBuyVolume()
    const sellVolume = getSelectedSellVolume()
    const netVolume = buyVolume - sellVolume

    if (netVolume > 0) {
      return `+$${formatNumber(netVolume)}`
    } else if (netVolume < 0) {
      return `-$${formatNumber(Math.abs(netVolume))}`
    } else {
      return '$0'
    }
  }
  return '-$948'
}

const getMonitorButtonType = () => {
  return monitorStatus.value === 'monitored' ? 'info' : 'primary'
}

const getMonitorButtonIcon = () => {
  return monitorStatus.value === 'monitored' ? 'el-icon-info' : 'el-icon-monitor'
}

const getMonitorButtonText = () => {
  return monitorStatus.value === 'monitored' ? '已监控' : '添加监控'
}

const toggleMonitor = () => {
  if (monitorStatus.value === 'not_monitored') {
    addToMonitor()
  } else {
    // 实现取消监控的逻辑
    proxy.$modal.info('取消监控功能开发中...')
  }
}

// 社媒链接相关方法
const openSocialLink = (url) => {
  window.open(url, '_blank')
}

const getSocialIcon = (type) => {
  const iconMap = {
    'website': '/src/assets/crypto-icons/web.png',
    'twitter': '/src/assets/crypto-icons/twitter.png',
    'telegram': '/src/assets/crypto-icons/telegram.png',
    'discord': '/src/assets/crypto-icons/discord.png',
    'github': 'el-icon-document',
    'medium': 'el-icon-edit-outline',
    'reddit': 'el-icon-chat-line-round',
    'docs': '/src/assets/crypto-icons/gitbook.png'
  }
  return iconMap[type.toLowerCase()] || 'el-icon-link'
}

const getSocialButtonType = () => {
  // 使用图片图标后，统一使用默认白色按钮样式
  return ''
}

const extractSocialLinks = (info) => {
  const socialLinks = []

  if (!info) {
    return socialLinks
  }

  // 官网链接 - 尝试多种可能的字段名
  const websiteFields = ['websites', 'website', 'links', 'urls']
  let websites = null

  for (const field of websiteFields) {
    if (info[field] && Array.isArray(info[field]) && info[field].length > 0) {
      websites = info[field]
      break
    } else if (info[field] && typeof info[field] === 'string') {
      websites = [{ url: info[field] }]
      break
    }
  }

  if (websites) {
    websites.forEach(website => {
      const url = website.url || website
      const label = website.label || website
      if (url) {
        socialLinks.push({
          type: label.toLowerCase(),
          url: url,
          label: label
        })
      }
    })
  }

  // 社交媒体链接 - 尝试多种可能的字段名
  const socialFields = ['socials', 'social', 'socialLinks', 'links']
  let socials = null

  for (const field of socialFields) {
    if (info[field] && Array.isArray(info[field]) && info[field].length > 0) {
      socials = info[field]
      break
    }
  }

  if (socials) {
    socials.forEach(social => {
      const url = social.url || social
      if (url) {
        const socialType = detectSocialType(url)
        socialLinks.push({
          type: socialType,
          url: url,
          label: socialType
        })
      }
    })
  }
  return socialLinks
}

const detectSocialType = (url) => {
  if (url.includes('twitter.com') || url.includes('x.com')) return 'twitter'
  if (url.includes('t.me') || url.includes('telegram')) return 'telegram'
  if (url.includes('discord')) return 'discord'
  if (url.includes('github')) return 'github'
  if (url.includes('medium')) return 'medium'
  if (url.includes('reddit')) return 'reddit'
  return 'website'
}

// 处理交易数据，确保数据完整性
const processRealtimeData = (tokenPair) => {
  // 如果API没有交易数据，生成基于价格变化的模拟数据
  const realtimeData = tokenPair.realtimeData;
  const txns = realtimeData.txns || generateMockTxnsFromPriceChange(realtimeData.txns)
  const priceChange = realtimeData.priceChange || {}
  const volume = realtimeData.volume || {}

  return {
    txns,
    priceChange,
    volume
  }
}

// 基于价格变化生成模拟交易数据
const generateMockTxnsFromPriceChange = (priceChange) => {
  if (!priceChange) return null

  const baseTxns = {
    m5: { buys: 0, sells: 0 },
    h1: { buys: 0, sells: 0 },
    h6: { buys: 0, sells: 0 },
    h24: { buys: 0, sells: 0 }
  }

  // 根据价格变化推算交易活跃度
  Object.keys(priceChange).forEach(timeframe => {
    const change = priceChange[timeframe]
    if (change !== null && change !== undefined) {
      const activity = Math.abs(change) * 10 // 价格变化越大，交易越活跃
      const buys = Math.floor(activity * (change > 0 ? 1.2 : 0.8)) // 涨的时候买入多一些
      const sells = Math.floor(activity * (change > 0 ? 0.8 : 1.2)) // 跌的时候卖出多一些

      if (baseTxns[timeframe]) {
        baseTxns[timeframe] = { buys, sells }
      }
    }
  })

  return baseTxns
}

// 页面加载时自动显示演示数据
onMounted(() => {
  loadHistory()
  
  // 如果没有历史记录，添加一些示例数据用于测试
  if (searchHistory.value.length === 0) {
    searchHistory.value = [
      {
        address: 'So11111111111111111111111111111111111111112',
        symbol: 'SOL',
        name: 'Solana',
        timestamp: Date.now() - 3600000
      },
      {
        address: 'EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v',
        symbol: 'USDC',
        name: 'USD Coin',
        timestamp: Date.now() - 7200000
      },
      {
        address: 'DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263',
        symbol: 'BONK',
        name: 'Bonk',
        timestamp: Date.now() - 10800000
      }
    ]
    // 保存示例数据到本地存储
    localStorage.setItem('crypto_search_history', JSON.stringify(searchHistory.value))
  }
  
  // 启动主流币价格更新
  startPriceUpdates()
  
  // 立即获取一次价格数据
  updateMainCoinPrices()
})

onUnmounted(() => {
  // 页面卸载时清除定时器，防止内存泄漏
  stopPriceUpdates()
})

const loadDemoSecurityData = () => {
  securityData.value = {
    holders: "1,234",
    top10Percent: 0.45,
    ownerAddress: "5Q544fKrFoe6tsEbD7S8EmxGTJYAKtTVhAW5Q5pge4j1",
    isMintable: false,
    isFreezable: true,
    isClosable: false,
    feeRate: 0.05,
    dexFlag: true,
    riskTag: "⚠️ 可冻结（黑名单）",
    isHoneypot: false,
    riskLevel: "MEDIUM"
  }
}

const getRiskLevelType = (level) => {
  const types = {
    'LOW': 'success',
    'MEDIUM': 'warning', 
    'HIGH': 'danger'
  }
  return types[level] || 'info'
}

const getRiskLevelText = (level) => {
  const texts = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险'
  }
  return texts[level] || '未知'
}

const formatPercent = (value) => {
  if (value === null || value === undefined) return '--'
  return (value * 100).toFixed(2) + '%'
}

// 安全数据相关方法
const getTokenSecurity = async (address, tokenPair) => {
  if (!address) return
  
  try {
    const response = await securityInfo(address)
    if (response && response.code === 200) {
      const data = response.data
      
      // 辅助函数：从数组或单值中提取数据
      const extractValue = (value) => {
        if (Array.isArray(value)) {
          return value.length > 0 ? value[0] : null
        }
        return value
      }
      
      // 辅助函数：转换为布尔值
      const toBool = (value) => {
        const extracted = extractValue(value)
        return extracted === "1" || extracted === true
      }
      
      // 辅助函数：转换为数字
      const toNumber = (value) => {
        const extracted = extractValue(value)
        return parseFloat(extracted) || 0
      }
      
      const riskTagValue = extractValue(data.riskTag) || ""
      const holderCount = tokenPair?.holderCount
      const fallbackHolders = data?.holders
      const top10Percent = tokenPair?.cryptoSecurityData?.top10Percent
      const fallbackTop10 = data?.top10Percent
      securityData.value = {
        holders: (holderCount && holderCount !== "0") ? holderCount : (fallbackHolders || "0"),
        top10Percent: (top10Percent && top10Percent !== 0) ? top10Percent : (fallbackTop10 || 0),
        ownerAddress: extractValue(data.ownerAddress) || "",
        isMintable: toBool(data.isMintable),
        isFreezable: toBool(data.isFreezable), 
        isClosable: toBool(data.isClosable),
        feeRate: toNumber(data.feeRate),
        dexFlag: extractValue(data.dexFlag) === true,
        riskTag: riskTagValue,
        isHoneypot: extractValue(data.isHoneypot) === true,
        riskLevel: calculateRiskLevel(riskTagValue)
      }

    } else {
      // 获取失败时使用演示数据
      loadDemoSecurityData()
      proxy.$modal.msgWarning('获取安全数据失败，使用演示数据')
    }
  } catch (error) {
    // 异常时使用演示数据
    loadDemoSecurityData()
    proxy.$modal.msgWarning('网络异常，使用演示数据')
  }
}

const calculateRiskLevel = (riskTag) => {
  if (!riskTag || riskTag.trim() === '') return 'LOW'
  
  const riskCount = riskTag.split(' ').filter(tag => tag.trim() !== '').length
  
  if (riskTag.includes('🚨 疑似貔貅') || riskCount >= 4) return 'HIGH'
  if (riskCount >= 2) return 'MEDIUM'
  return 'LOW'
}

const getRiskLevelClass = (level) => {
  const classes = {
    'LOW': 'low',
    'MEDIUM': 'medium', 
    'HIGH': 'high'
  }
  return classes[level] || 'medium'
}

const getConcentrationRiskClass = (top10Percent) => {
  if (top10Percent < 0.15) return 'success'
  if (top10Percent < 0.25) return 'warning'
  return 'danger'
}

const getFeeRiskClass = (feeRate) => {
  if (feeRate < 0.05) return 'success'
  if (feeRate < 0.10) return 'warning'
  return 'danger'
}

const copyAddress = (address) => {
  navigator.clipboard.writeText(address).then(() => {
    proxy.$modal.msgSuccess('开发者地址已复制到剪贴板')
  }).catch(err => {
    proxy.$modal.msgError('复制地址失败: ' + err.message)
  })
}

// 主流币相关方法
const formatCoinPrice = (price) => {
  if (!price || price === 0) return '0.00'
  
  if (price >= 1) {
    return price.toLocaleString('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    })
  } else if (price >= 0.01) {
    return price.toFixed(4)
  } else if (price >= 0.0001) {
    return price.toFixed(6)
  } else {
    return price.toExponential(2)
  }
}

const updateMainCoinPrices = async () => {
  try {
    // 并发请求所有币种的价格数据
    const pricePromises = mainCoins.value.map(async (coin) => {
      try {
        const response = await getTopCoin(coin.coin)
        if (response && response.code === 200) {
          return {
            symbol: coin.symbol,
            price: parseFloat(response.data.last),
            change24h: parseFloat(response.data.change_percentage),
            volume24h: parseFloat(response.data.quote_volume),
            high24h: parseFloat(response.data.high_24h),
            low24h: parseFloat(response.data.low_24h)
          }
        }
        return null
      } catch (error) {
        console.warn(`Failed to fetch ${coin.symbol} price:`, error)
        return null
      }
    })
    
    // 等待所有请求完成
    const priceResults = await Promise.all(pricePromises)
    
    // 更新价格数据
    mainCoins.value.forEach((coin, index) => {
      const priceData = priceResults[index]
      if (priceData) {
        coin.price = priceData.price
        coin.change24h = priceData.change24h
        coin.volume24h = priceData.volume24h
        coin.high24h = priceData.high24h
        coin.low24h = priceData.low24h
      }
    })
    
    console.log('Main coin prices updated successfully')
  } catch (error) {
    console.warn('Failed to update main coin prices:', error)
  }
}

const startPriceUpdates = () => {
  // 立即更新一次
  updateMainCoinPrices()
  
  // 每5秒更新一次价格
  priceUpdateTimer = setInterval(updateMainCoinPrices, 10000)
}

const stopPriceUpdates = () => {
  if (priceUpdateTimer) {
    clearInterval(priceUpdateTimer)
    priceUpdateTimer = null
  }
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 动画触发函数
const triggerDataCardAnimation = () => {
  animationStates.dataCards = true
  setTimeout(() => {
    animationStates.dataCards = false
  }, 600)
}

const triggerDataValueAnimation = () => {
  animationStates.dataValues = true
  setTimeout(() => {
    animationStates.dataValues = false
  }, 800)
}

const triggerRiskCardAnimation = () => {
  animationStates.riskCard = true
  setTimeout(() => {
    animationStates.riskCard = false
  }, 800)
}

const triggerRiskWarningAnimation = () => {
  animationStates.riskWarning = true
  setTimeout(() => {
    animationStates.riskWarning = false
  }, 600)
}

const triggerSecurityMetricsAnimation = () => {
  animationStates.securityMetrics = true
  setTimeout(() => {
    animationStates.securityMetrics = false
  }, 700)
}

const triggerSecurityValuesAnimation = () => {
  animationStates.securityValues = true
  setTimeout(() => {
    animationStates.securityValues = false
  }, 600)
}

const triggerPermissionsAnimation = () => {
  animationStates.permissions = true
  setTimeout(() => {
    animationStates.permissions = false
  }, 1000)
}

// 监听数据变化并触发动画
watch(
  () => tokenData.value,
  (newData, oldData) => {
    if (newData && oldData) {
      // 检查基础数据是否有变化
      const dataFields = ['marketCap', 'liquidity', 'volume24h', 'holderCount']
      const hasDataChange = dataFields.some(field => 
        newData[field] !== oldData[field]
      )
      
      if (hasDataChange) {
        triggerDataCardAnimation()
        setTimeout(() => {
          triggerDataValueAnimation()
        }, 200)
      }
    }
  },
  { deep: true }
)

watch(
  () => securityData.value,
  (newData, oldData) => {
    if (newData && oldData) {
      // 检查风险等级变化
      if (newData.riskLevel !== oldData.riskLevel) {
        triggerRiskCardAnimation()
      }
      
      // 检查风险提示变化
      if (newData.riskTag !== oldData.riskTag) {
        triggerRiskWarningAnimation()
      }
      
      // 检查安全指标变化
      const metricFields = ['top10Percent', 'feeRate', 'holders']
      const hasMetricChange = metricFields.some(field => 
        newData[field] !== oldData[field]
      )
      
      if (hasMetricChange) {
        triggerSecurityMetricsAnimation()
        setTimeout(() => {
          triggerSecurityValuesAnimation()
        }, 100)
      }
      
      // 检查权限状态变化
      const permissionFields = ['isMintable', 'isFreezable', 'isClosable', 'dexFlag']
      const hasPermissionChange = permissionFields.some(field => 
        newData[field] !== oldData[field]
      )
      
      if (hasPermissionChange) {
        triggerPermissionsAnimation()
      }
    }
  },
  { deep: true }
)
</script>

<style scoped>
/* 整体容器美化 */
.app-container {
  background: linear-gradient(135deg, var(--el-bg-color-page) 0%, var(--el-bg-color) 100%);
  min-height: calc(100vh - 200px);
  padding: 20px;
}

/* 主要卡片美化 */
.el-card {
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 
    0 4px 12px var(--el-box-shadow-light),
    0 2px 4px var(--el-box-shadow);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: var(--el-bg-color);
  position: relative;
  overflow: hidden;
}

.el-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, 
    var(--el-color-primary-light-8), 
    var(--el-color-primary-light-9), 
    var(--el-color-primary-light-8));
}

.el-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 20px var(--el-box-shadow-light),
    0 4px 8px var(--el-box-shadow);
  border-color: var(--el-color-primary-light-8);
}

/* 代币头部信息美化 */
.token-header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  padding: 4px 0;
}

.token-main-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.token-basic {
  display: flex;
  align-items: center;
  gap: 12px;
}

.token-avatar {
  flex-shrink: 0;
  box-shadow: 0 4px 8px var(--el-box-shadow-light);
  transition: all 0.3s ease;
}

.token-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 12px var(--el-box-shadow);
}

.token-text h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, var(--el-text-color-primary), var(--el-color-primary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.token-sub {
  color: var(--el-text-color-secondary);
  font-weight: 400;
  font-size: 14px;
  margin-left: 8px;
}

.price-social-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 6px;
}

.price-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-main {
  font-size: 24px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  text-shadow: 0 1px 2px var(--el-box-shadow-light);
}

.price-change-tag {
  border-radius: 8px;
  font-weight: 600;
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s ease;
}

.price-change-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
}

.social-icons {
  display: flex;
  gap: 8px;
}

.social-icon-btn {
  border-radius: 50%;
  box-shadow: 0 2px 6px var(--el-box-shadow-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.social-icon-btn:hover {
  transform: translateY(-2px) scale(1.1);
  box-shadow: 0 4px 12px var(--el-box-shadow);
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn.small {
  font-size: 13px;
  padding: 4px 8px;
  border-radius: 20px !important;
  height: 34px;
  min-width: 80px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 6px var(--el-box-shadow-light);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn.small:hover {
  background: linear-gradient(135deg, var(--el-color-primary-light-9), var(--el-color-primary-light-6));
  color: var(--el-color-primary-dark-2);
  box-shadow: 0 6px 14px var(--el-box-shadow-light);
}

/* 统计小卡片 */
.mini-stats {
  display: flex;
  gap: 8px;
}

.mini-card {
  padding: 8px 12px;
  border-radius: 8px;
  background: linear-gradient(135deg, 
    var(--el-fill-color-light) 0%, 
    var(--el-fill-color) 100%);
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s ease;
  text-align: center;
  min-width: 60px;
}

.mini-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
}

.mini-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary);
}

.mini-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

/* K线图美化 */
.chart-container {
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px var(--el-box-shadow-light);
  background: var(--el-fill-color-light);
}

.kline-iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 12px;
}

/* 数据区域 */
.data-section {
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 12px 0;
  padding-left: 8px;
  border-left: 3px solid var(--el-color-primary);
}

/* 基础数据卡片行 */
.data-cards-row {
  display: flex;
  gap: 8px;
}

.data-card {
  flex: 1;
  padding: 12px 8px;
  border-radius: 12px;
  background: linear-gradient(135deg, 
    var(--el-bg-color) 0%, 
    var(--el-bg-color-page) 100%);
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 4px 12px var(--el-box-shadow-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
}

.data-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px var(--el-box-shadow-light);
  border-color: var(--el-color-primary-light-8);
}

/* 数据变动动画 */
.data-card.data-updated {
  animation: dataUpdatePulse 0.6s ease-out;
}

@keyframes dataUpdatePulse {
  0% {
    transform: scale(1);
    box-shadow: 0 4px 12px var(--el-box-shadow-light);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 8px 20px var(--el-color-primary-light-8);
    border-color: var(--el-color-primary-light-6);
  }
  100% {
    transform: scale(1);
    box-shadow: 0 4px 12px var(--el-box-shadow-light);
  }
}

.data-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

/* 数值变动动画 */
.data-value.value-updated {
  animation: valueGlow 0.8s ease-out;
}

@keyframes valueGlow {
  0% {
    color: var(--el-text-color-primary);
    text-shadow: none;
  }
  50% {
    color: var(--el-color-primary);
    text-shadow: 0 0 8px var(--el-color-primary-light-6);
  }
  100% {
    color: var(--el-text-color-primary);
    text-shadow: none;
  }
}

.data-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* 风险等级行 */
.risk-level-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.risk-card {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 14px;
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s ease;
}

/* 风险等级变动动画 */
.risk-card.risk-updated {
  animation: riskLevelChange 0.8s ease-out;
}

@keyframes riskLevelChange {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  25% {
    transform: scale(0.95);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
    box-shadow: 0 6px 16px var(--el-box-shadow);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.risk-card.low {
  background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
  color: var(--el-color-success-dark-2);
}

.risk-card.medium {
  background: linear-gradient(135deg, var(--el-color-warning-light-9), var(--el-color-warning-light-8));
  color: var(--el-color-warning-dark-2);
}

.risk-card.high {
  background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
  color: var(--el-color-danger-dark-2);
}

.risk-warning {
  flex: 1;
  padding: 8px 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  font-style: italic;
  transition: all 0.3s ease;
}

/* 风险提示变动动画 */
.risk-warning.warning-updated {
  animation: warningFlash 0.6s ease-out;
}

@keyframes warningFlash {
  0% {
    background: var(--el-fill-color-light);
  }
  50% {
    background: var(--el-color-warning-light-9);
    color: var(--el-color-warning-dark-2);
  }
  100% {
    background: var(--el-fill-color-light);
  }
}

/* 安全指标行 */
.security-metrics-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.security-card {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s ease;
  text-align: center;
}

.security-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
}

/* 安全指标变动动画 */
.security-card.metric-updated {
  animation: securityMetricUpdate 0.7s ease-out;
}

@keyframes securityMetricUpdate {
  0% {
    transform: translateY(0) scale(1);
    box-shadow: 0 2px 4px var(--el-box-shadow-light);
  }
  40% {
    transform: translateY(-3px) scale(1.02);
    box-shadow: 0 8px 16px var(--el-box-shadow);
  }
  100% {
    transform: translateY(0) scale(1);
    box-shadow: 0 2px 4px var(--el-box-shadow-light);
  }
}

.security-card.success {
  background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
  border: 1px solid var(--el-color-success-light-6);
}

.security-card.warning {
  background: linear-gradient(135deg, var(--el-color-warning-light-9), var(--el-color-warning-light-8));
  border: 1px solid var(--el-color-warning-light-6);
}

.security-card.danger {
  background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
  border: 1px solid var(--el-color-danger-light-6);
}

.security-card.neutral {
  background: linear-gradient(135deg, var(--el-fill-color-light), var(--el-fill-color));
  border: 1px solid var(--el-border-color-light);
}

.security-card.success .security-value {
  color: var(--el-color-success-dark-2);
}

.security-card.warning .security-value {
  color: var(--el-color-warning-dark-2);
}

.security-card.danger .security-value {
  color: var(--el-color-danger-dark-2);
}

.security-card.neutral .security-value {
  color: var(--el-text-color-primary);
}

.security-value {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

/* 安全数值变动动画 */
.security-value.security-value-updated {
  animation: securityValuePulse 0.6s ease-out;
}

@keyframes securityValuePulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.15);
    opacity: 0.8;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.security-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* 权限状态行 */
.permissions-row {
  display: flex;
  gap: 8px;
}

.permission-card {
  flex: 1;
  padding: 18px 10px;
  border-radius: 8px;
  text-align: center;
  font-size: 12px;
  font-weight: 600;
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s ease;
}

.permission-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
}

/* 权限状态变动动画 */
.permission-card.permission-updated {
  animation: permissionStatusChange 0.8s ease-out;
}

@keyframes permissionStatusChange {
  0% {
    transform: scale(1) rotateY(0deg);
    opacity: 1;
  }
  25% {
    transform: scale(0.95) rotateY(5deg);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.05) rotateY(-5deg);
    opacity: 1;
    box-shadow: 0 6px 16px var(--el-box-shadow);
  }
  75% {
    transform: scale(1.02) rotateY(2deg);
    opacity: 1;
  }
  100% {
    transform: scale(1) rotateY(0deg);
    opacity: 1;
  }
}

.permission-card.safe {
  background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
  color: var(--el-color-success-dark-2);
  border: 1px solid var(--el-color-success-light-6);
}

.permission-card.danger {
  background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
  color: var(--el-color-danger-dark-2);
  border: 1px solid var(--el-color-danger-light-6);
}

/* 权限状态安全→危险 变化动画 */
.permission-card.danger.permission-updated {
  animation: permissionToDanger 1s ease-out;
}

@keyframes permissionToDanger {
  0% {
    background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
    color: var(--el-color-success-dark-2);
    border-color: var(--el-color-success-light-6);
  }
  50% {
    background: linear-gradient(135deg, var(--el-color-warning-light-9), var(--el-color-warning-light-8));
    color: var(--el-color-warning-dark-2);
    border-color: var(--el-color-warning-light-6);
    transform: scale(1.1);
    box-shadow: 0 8px 20px var(--el-color-warning-light-7);
  }
  100% {
    background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
    color: var(--el-color-danger-dark-2);
    border-color: var(--el-color-danger-light-6);
    transform: scale(1);
  }
}

/* 权限状态危险→安全 变化动画 */
.permission-card.safe.permission-updated {
  animation: permissionToSafe 1s ease-out;
}

@keyframes permissionToSafe {
  0% {
    background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
    color: var(--el-color-danger-dark-2);
    border-color: var(--el-color-danger-light-6);
  }
  50% {
    background: linear-gradient(135deg, var(--el-color-warning-light-9), var(--el-color-warning-light-8));
    color: var(--el-color-warning-dark-2);
    border-color: var(--el-color-warning-light-6);
    transform: scale(1.1);
    box-shadow: 0 8px 20px var(--el-color-warning-light-7);
  }
  100% {
    background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
    color: var(--el-color-success-dark-2);
    border-color: var(--el-color-success-light-6);
    transform: scale(1);
  }
}

/* 时间周期选择器 */
.timeframe-cards-row {
  display: flex;
  gap: 8px;
}

.timeframe-card {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-bg-color);
  border: 2px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  text-align: center;
  cursor: pointer;
}

.timeframe-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
  border-color: var(--el-color-primary-light-7);
}

.timeframe-card.active {
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-dark-2));
  border-color: var(--el-color-primary);
  box-shadow: 0 4px 12px var(--el-color-primary-light-7);
}

.timeframe-label {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 4px;
}

.timeframe-card.active .timeframe-label {
  color: white;
}

.timeframe-change {
  font-size: 10px;
  font-weight: 600;
}

.timeframe-change.positive {
  color: var(--el-color-success);
}

.timeframe-change.negative {
  color: var(--el-color-danger);
}

.timeframe-change.neutral {
  color: var(--el-text-color-secondary);
}

.timeframe-card.active .timeframe-change {
  color: rgba(255, 255, 255, 0.9);
}

/* 交易统计卡片 */
.trading-cards-row {
  display: flex;
  gap: 8px;
}

.trading-card {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-bg-color);
  transition: all 0.3s ease;
  text-align: center;
}

.trading-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
}

.trading-card.buy {
  background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
  border-color: var(--el-color-success-light-6);
}

.trading-card.sell {
  background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
  border-color: var(--el-color-danger-light-6);
}

.trading-card.positive {
  background: linear-gradient(135deg, var(--el-color-success-light-9), var(--el-color-success-light-8));
  border-color: var(--el-color-success-light-6);
}

.trading-card.negative {
  background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
  border-color: var(--el-color-danger-light-6);
}

.trading-value {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 4px;
}

.trading-card.buy .trading-value {
  color: var(--el-color-success-dark-2);
}

.trading-card.sell .trading-value {
  color: var(--el-color-danger-dark-2);
}

.trading-card.positive .trading-value {
  color: var(--el-color-success-dark-2);
}

.trading-card.negative .trading-value {
  color: var(--el-color-danger-dark-2);
}

.trading-label {
  font-size: 10px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

/* 顶部搜索区域样式 */
.top-search-area {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-light);
  
  /* 
  📝 搜索框宽度自定义变量
  修改下面的值来调整搜索框宽度：
  - 默认: 320px
  - 可以使用 px、%、rem 等单位
  - 例如: 400px, 25%, 20rem 等
  */
  --search-input-width: 320px;
}

.search-compact {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input-compact {
  width: var(--search-input-width) !important;
}

.search-input-compact .el-input__wrapper {
  border-radius: 8px;
  border: 1px solid var(--el-border-color);
  background: var(--el-bg-color);
}

.search-icon-compact {
  color: var(--el-text-color-placeholder);
}

.paste-btn-compact {
  color: var(--el-text-color-placeholder);
  padding: 0;
  height: auto;
}

.paste-btn-compact:hover {
  color: var(--el-color-primary);
}

.search-btn-compact {
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

/* 主流币紧凑布局 */
.main-coins-compact {
  display: flex;
  gap: 12px;
  align-items: center;
}

.coin-item-compact {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color-light);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.coin-item-compact:hover {
  border-color: var(--el-color-primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.coin-logo {
  width: 16px;
  height: 16px;
  object-fit: contain;
  border-radius: 50%;
  flex-shrink: 0;
  transition: all 0.2s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.coin-item-compact:hover .coin-logo {
  transform: scale(1.1);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.coin-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.coin-price-compact {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.2;
}

.coin-change-compact {
  font-size: 10px;
  font-weight: 500;
  line-height: 1.2;
}

.coin-change-compact.positive {
  color: #16a34a;
}

.coin-change-compact.negative {
  color: #dc2626;
}

/* 搜索历史下拉样式 */
.search-history-popper .el-popper {
  min-width: var(--search-input-width) !important;
}

.history-suggestion {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.suggestion-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.suggestion-symbol {
  font-weight: 600;
  color: var(--el-color-primary);
}

.suggestion-name {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.suggestion-address {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  font-family: monospace;
}

/* 响应式调整 */
@media (max-width: 1400px) {
  .main-coins-compact {
    gap: 8px;
  }
  
  .coin-item-compact {
    padding: 4px 8px;
    gap: 4px;
  }
  
  .coin-logo {
    width: 14px;
    height: 14px;
  }
  
  .coin-price-compact {
    font-size: 11px;
  }
  
  .coin-change-compact {
    font-size: 9px;
  }
}

@media (max-width: 1200px) {
  .top-search-area {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .search-compact {
    justify-content: center;
  }
  
  .main-coins-compact {
    justify-content: center;
  }
}

/* 隐藏右侧栏的主流币价格部分 - 因为已移到顶部 */
.data-section:has(.main-coins-row) {
  display: none;
}

.search-input-compact :deep(.el-input__wrapper) {
  border-radius: 8px !important;
  border: 1px solid var(--el-border-color) !important;
  background: var(--el-bg-color) !important;
  width: 100% !important;
}

.search-input-compact :deep(.el-input__inner) {
  width: 100% !important;
}

.search-compact .el-autocomplete {
  width: var(--search-input-width) !important;
}

/* 强制覆盖Element Plus的样式 */
.top-search-area .search-compact .el-autocomplete,
.top-search-area .search-compact .el-autocomplete .el-input,
.top-search-area .search-compact .search-input-compact {
  width: var(--search-input-width) !important;
  min-width: var(--search-input-width) !important;
  max-width: var(--search-input-width) !important;
}

.top-search-area .search-compact .el-autocomplete :deep(.el-input__wrapper),
.top-search-area .search-compact .search-input-compact :deep(.el-input__wrapper) {
  width: 100% !important;
}

/* 无数据时的占位样式 */
.no-data-placeholder {
  padding: 60px 20px;
  text-align: center;
  background: linear-gradient(135deg, var(--el-fill-color-light), var(--el-fill-color-blank));
  border-radius: 12px;
  border: 2px dashed var(--el-border-color-light);
}

.no-data-sidebar {
  padding: 40px 20px;
  text-align: center;
}

/* 主流币加载状态 */
.main-coins-compact.loading {
  opacity: 0.7;
}

.coin-loading {
  font-size: 10px;
  color: var(--el-color-primary);
}
</style>
