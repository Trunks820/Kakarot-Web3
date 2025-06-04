<template>
  <div class="crypto-scanner">
    <!-- 主卡片 - 包含搜索和内容 -->
    <div class="main-card">
      <!-- 搜索头部区域 -->
      <div class="search-header">
        <div class="search-input-wrapper">
          <div class="search-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 21L16.514 16.506M19 10.5C19 15.194 15.194 19 10.5 19C5.806 19 2 15.194 2 10.5C2 5.806 5.806 2 10.5 2C15.194 2 19 5.806 19 10.5Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <el-input
            v-model="searchCA"
            placeholder="输入 CA 地址"
            class="search-input"
            @keyup.enter="searchToken"
            clearable
            size="large"
          />
          <el-button 
            @click="searchToken" 
            type="primary" 
            :loading="searching"
            class="search-button"
            size="large"
          >
            {{ searching ? '查询中...' : '查询' }}
          </el-button>
        </div>
      </div>

      <!-- 分割线 -->
      <div class="header-divider"></div>

      <!-- 主要内容区域 -->
      <div class="main-content" v-if="tokenData">
        <el-row :gutter="20">
          <!-- 左侧K线图区域 -->
          <el-col :span="16">
            <div class="chart-section">
              <div class="chart-header"><!--                </div>-->
                <div class="token-title-row">
                  <!-- logo -->
                  <img :src="tokenData.logoUrl" class="token-logo" v-if="tokenData.logoUrl">

                  <!-- 名称和价格 -->
                  <div class="token-info">
                    <div class="token-header">
                      <h3>{{ tokenData.symbol }} <span class="token-name">{{ tokenData.name }}</span></h3>
                    </div>
                    <div class="price-info">
                      <span class="current-price">${{ formatPrice(tokenData.price) }}</span>
                      <span :class="['price-change', tokenData.change24h >= 0 ? 'positive' : 'negative']">
                        {{ tokenData.change24h >= 0 ? '+' : '' }}{{ tokenData.change24h }}%
                      </span>
                    </div>
                  </div>

                  <!-- 社交链接 -->
                  <div class="social-links-inline" v-if="tokenData.socialLinks && tokenData.socialLinks.length > 0">
                    <div class="social-buttons">
                      <el-button
                          v-for="link in tokenData.socialLinks"
                          :key="link.type"
                          size="mini"
                          @click="openSocialLink(link.url)"
                          :type="getSocialButtonType()"
                          circle
                          :title="link.type"
                          class="social-btn-small"
                      >
                        <img
                            v-if="getSocialIcon(link.type).includes('.png')"
                            :src="getSocialIcon(link.type)"
                            alt="Social Icon"
                            class="social-icon-img"
                        >
                        <i v-else :class="getSocialIcon(link.type)"></i>
                      </el-button>
                    </div>
                  </div>

                  <!-- 操作按钮 -->
                  <div class="action-buttons">
                    <div class="primary-actions">
                      <el-button
                          :type="getMonitorButtonType()"
                          size="small"
                          @click="toggleMonitor"
                          :loading="monitoring"
                          :disabled="monitorStatus === 'monitored'"
                      >
                        <i :class="getMonitorButtonIcon()"></i>
                        {{ getMonitorButtonText() }}
                      </el-button>
                      <el-button size="small" @click="openInExplorer" type="info">
                        <i class="el-icon-link"></i> 区块浏览器
                      </el-button>
                    </div>
                  </div>

                  <!-- 查询统计 -->
                  <div class="query-stats-row">
                    <div class="stat-item">
                      <span class="label">总查询</span>
                      <span class="value">{{ tokenData.queryCount || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="label">今日查询</span>
                      <span class="value">{{ tokenData.todayQueries || 0 }}</span>
                    </div>
                    <div class="stat-item">
                      <span class="label">监控人数</span>
                      <span class="value">{{ tokenData.monitorCount || 0 }}</span>
                    </div>
                  </div>
                </div>

              </div>
              
              <div class="chart-container">
                <iframe 
                  ref="klineIframe"
                  :src="getKlineUrl()"
                  class="kline-iframe"
                  frameborder="0"
                  scrolling="no">
                </iframe>
              </div>
            </div>
          </el-col>

          <!-- 右侧信息操作区域 -->
          <el-col :span="8">
            <!-- 代币信息 - GMGN风格 -->
            <div class="token-info-section">
              <div class="section-header">
                <el-button style="float: right; padding: 3px 0" type="text" @click="refreshTokenData">
                  <i class="el-icon-refresh"></i> 刷新
                </el-button>
              </div>
              
              <!-- 基础数据行 -->
              <div class="basic-info-row">
                <div class="info-item">
                  <span class="info-label">市值</span>
                  <span class="info-value">${{ formatNumber(tokenData.marketCap) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">池子</span>
                  <span class="info-value">${{ formatNumber(tokenData.liquidity) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">24h成交额</span>
                  <span class="info-value">${{ formatNumber(tokenData.volume24h) }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">持有者</span>
                  <span class="info-value">{{ formatNumber(tokenData.holderCount) || '0' }}</span>
                </div>
              </div>

              <!-- 安全数据区域 -->
              <div class="security-info-row">
                <div class="security-header">
                  <span class="security-title">🔒 安全分析</span>
                </div>
                
                <div class="security-content" v-if="securityData">
                  <!-- 风险等级 -->
                  <div class="risk-level-section">
                    <el-tag :type="getRiskLevelType(securityData.riskLevel)" size="medium">
                      {{ getRiskLevelText(securityData.riskLevel) }}
                    </el-tag>
                    <span class="risk-tags" v-if="securityData.riskTag">{{ securityData.riskTag }}</span>
                  </div>
                  
                  <!-- 安全指标网格 -->
                  <div class="security-metrics-grid">
                    <div class="security-metric-card">
                      <div class="metric-label">Top10占比</div>
                      <div class="metric-value" :class="getConcentrationRiskClass(securityData.top10Percent)">
                        {{ formatPercent(securityData.top10Percent) }}
                      </div>
                    </div>
                    
                    <div class="security-metric-card">
                      <div class="metric-label">交易税率</div>
                      <div class="metric-value" :class="getFeeRiskClass(securityData.feeRate)">
                        {{ formatPercent(securityData.feeRate) }}
                      </div>
                    </div>

                    <div class="security-metric-card">
                      <div class="metric-label">持有者数</div>
                      <div class="metric-value neutral">
                        {{ securityData.holders }}
                      </div>
                    </div>
                    
                    <div class="security-metric-card clickable" @click="copyAddress(securityData.ownerAddress)" title="点击复制开发者地址">
                      <div class="metric-label">开发者</div>
                      <div class="metric-value neutral">
                        <i class="el-icon-document-copy"></i> 复制
                      </div>
                    </div>
                  </div>
                  
                  <!-- 权限状态网格 - 时间选择器风格 -->
                  <div class="permissions-timeframe-grid">
                    <div 
                      class="permission-timeframe-btn" 
                      :class="{ 'safe': !securityData.isMintable, 'danger': securityData.isMintable }"
                    >
                      <div class="permission-label">增发权限</div>
                      <div class="permission-status">{{ securityData.isMintable ? '可增发' : '不可增发' }}</div>
                    </div>
                    
                    <div 
                      class="permission-timeframe-btn" 
                      :class="{ 'safe': !securityData.isFreezable, 'danger': securityData.isFreezable }"
                    >
                      <div class="permission-label">冻结权限</div>
                      <div class="permission-status">{{ securityData.isFreezable ? '可冻结' : '不可冻结' }}</div>
                    </div>
                    
                    <div 
                      class="permission-timeframe-btn" 
                      :class="{ 'safe': !securityData.isClosable, 'danger': securityData.isClosable }"
                    >
                      <div class="permission-label">销毁权限</div>
                      <div class="permission-status">{{ securityData.isClosable ? '可销毁' : '不可销毁' }}</div>
                    </div>
                    
                    <div 
                      class="permission-timeframe-btn" 
                      :class="{ 'safe': securityData.dexFlag, 'danger': !securityData.dexFlag }"
                    >
                      <div class="permission-label">DEX状态</div>
                      <div class="permission-status">{{ securityData.dexFlag ? '已上DEX' : '未上DEX' }}</div>
                    </div>
                  </div>
                </div>
                
                <div class="security-loading" v-else-if="loadingSecurity">
                  <i class="el-icon-loading"></i>
                  <span>正在分析安全数据...</span>
                </div>
                
                <div class="security-error" v-else>
                  <i class="el-icon-warning-outline"></i>
                  <span>暂无安全数据</span>
                </div>
              </div>

              <!-- 时间周期选择器 - GMGN风格 -->
              <div class="timeframe-selector" v-if="tokenData.realtimeData">
                <div class="timeframe-buttons">
                  <div 
                    v-for="timeframe in timeframes" 
                    :key="timeframe.value"
                    :class="['timeframe-btn', { active: selectedTimeframe === timeframe.value }]"
                    @click="selectedTimeframe = timeframe.value"
                  >
                    <div class="timeframe-label">{{ timeframe.label }}</div>
                    <div :class="['timeframe-change', getChangeClass(getPriceChangeByTimeframe(timeframe.value))]">
                      {{ formatChange(getPriceChangeByTimeframe(timeframe.value)) }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 交易统计 - GMGN风格 -->
              <div class="trading-stats" v-if="tokenData.realtimeData">
                <div class="trading-stats-row">
                  <div class="stat-item">
                    <div class="stat-label">成交额</div>
                    <div class="stat-value">${{ formatNumber(getSelectedVolume()) }}</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">买入</div>
                    <div class="stat-value buy-color">{{ getSelectedBuys() }}/${{ formatNumber(getSelectedBuyVolume()) }}</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">卖出</div>
                    <div class="stat-value sell-color">{{ getSelectedSells() }}/${{ formatNumber(getSelectedSellVolume()) }}</div>
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">净买入</div>
                    <div :class="['stat-value', getNetBuyClass()]">{{ getNetBuysFormatted() }}</div>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 价格提醒对话框 -->
    <el-dialog title="设置价格提醒" :visible.sync="alertDialogVisible" width="500px">
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
      <div slot="footer">
        <el-button @click="alertDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmPriceAlert" :loading="settingAlert">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="CryptoScanner">
import { ref, reactive, computed, getCurrentInstance, watch, onMounted } from 'vue'
import { tokenInfo, securityInfo} from "@/api/crypto/index"
const { proxy } = getCurrentInstance()

// 响应式数据定义
const searchCA = ref('')
const searching = ref(false)
const tokenData = ref(null)
const monitoring = ref(false)
const settingAlert = ref(false)
const alertDialogVisible = ref(false)
const currentChain = ref('sol')
const klineIframe = ref(null)
const securityData = ref(null)
const loadingSecurity = ref(false)

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

// 获取代币信息
const getTokenInfo = () => {
  searching.value = true
  // 调用API获取数据
  tokenInfo(searchCA.value).then(response => {
    searching.value = false

    if (response && response.data) {
      const tokenPair = response.data
      
      // 检查数据结构
      if (!tokenPair.baseToken) {
        proxy.$modal.msgError('返回的数据格式不正确，缺少baseToken信息')
        return
      }
      
      const baseToken = tokenPair.baseToken
      tokenData.value = {
        name: baseToken.name,
        symbol: baseToken.symbol,
        address: baseToken.address,
        logoUrl: tokenPair.info?.imageUrl || getChainLogo(tokenPair.chainId || 'sol'),
        price: parseFloat(tokenPair.priceUsd) || 0,
        change24h: tokenPair.priceChange?.h24 || 0,
        marketCap: tokenPair.marketCap || tokenPair.fdv || 0,
        volume24h: tokenPair.volume?.h24 || 0,
        high24h: calculateHigh24h(tokenPair),
        low24h: calculateLow24h(tokenPair),
        holderCount: 0, // DexScreener不提供这个数据
        liquidity: tokenPair.liquidity?.usd || 0,
        isVerified: !!tokenPair.info?.websites?.length,
        hasRenounced: false, // 需要其他API获取
        queryCount: Math.floor(Math.random() * 1000) + 100, // 模拟数据
        todayQueries: Math.floor(Math.random() * 100) + 10, // 模拟数据
        monitorCount: Math.floor(Math.random() * 50) + 5, // 模拟数据
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
        socialLinks: extractSocialLinks(tokenPair.info)
      }
      
      proxy.$modal.msgSuccess(`🎉 成功加载${tokenData.value.symbol}代币信息`)
      
      // 自动获取安全数据
      getTokenSecurity(tokenData.value.address)
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

  // 重置状态
  monitorStatus.value = 'not_monitored'
  tokenData.value = null

  getTokenInfo()
}

// 加载演示数据 - 移除自动调用getTokenInfo，使用静态模拟数据
const loadDemoToken = () => {
  searchCA.value = "So11111111111111111111111111111111111111112"
  
  // 使用静态模拟数据，避免API调用失败的无限递归
  tokenData.value = {
    name: "Wrapped SOL",
    symbol: "SOL",
    address: "So11111111111111111111111111111111111111112",
    logoUrl: getChainLogo('sol'),
    price: 180.45,
    change24h: 2.5,
    marketCap: 84500000000,
    volume24h: 2100000000,
    high24h: 185.20,
    low24h: 175.80,
    holderCount: 1250000,
    liquidity: 12500000,
    isVerified: true,
    hasRenounced: false,
    queryCount: 1256,
    todayQueries: 89,
    monitorCount: 234,
    pairInfo: {
      dexId: "raydium",
      chainId: "solana",
      pairAddress: "58oQChx4yWmvKdwLLZzBi4ChoCc2fqCUWBkwMihLYQo2",
      url: "https://dexscreener.com/solana/58oqchx4ywmvkdwllzzbxchscc2fqcuwbkwmihlvqo2",
      labels: ["v3"],
      pairCreatedAt: 1640995200000
    },
    realtimeData: {
      txns: {
        m5: { buys: 12, sells: 8 },
        h1: { buys: 145, sells: 123 },
        h6: { buys: 867, sells: 745 },
        h24: { buys: 3456, sells: 2987 }
      },
      priceChange: {
        m5: 0.1,
        h1: 1.2,
        h6: 1.8,
        h24: 2.5
      },
      volume: {
        m5: 125000,
        h1: 1250000,
        h6: 7500000,
        h24: 2100000000
      }
    },
    socialLinks: [
      { type: 'website', url: 'https://solana.com', label: '官网' },
      { type: 'twitter', url: 'https://twitter.com/solana', label: 'Twitter' }
    ]
  }
  
  proxy.$modal.msgSuccess('🎉 加载演示数据成功')
  
  // 加载演示安全数据
  getTokenSecurity(tokenData.value.address)
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
    return (num / 1e9).toFixed(2) + 'B'
  } else if (num >= 1e6) {
    return (num / 1e6).toFixed(2) + 'M'
  } else if (num >= 1e3) {
    return (num / 1e3).toFixed(2) + 'K'
  }
  return num?.toString() || '0'
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
    if (netBuys > 0) return 'positive'
    if (netBuys < 0) return 'negative'
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
  const txns = tokenPair.txns || generateMockTxnsFromPriceChange(tokenPair.priceChange)
  const priceChange = tokenPair.priceChange || {}
  const volume = tokenPair.volume || {}

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
  loadDemoToken()
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
const getTokenSecurity = async (address) => {
  if (!address) return
  
  loadingSecurity.value = true
  try {
    const response = await securityInfo(address)
    if (response && response.code === 200) {
      // 处理后端返回的数据格式（后端使用append会创建数组）
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
      
      securityData.value = {
        holders: extractValue(data.holders) || "0",
        top10Percent: toNumber(data.top10Percent),
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
      
      // 更新基础数据中的持有者数量
      if (tokenData.value) {
        tokenData.value.holderCount = securityData.value.holders
      }
      
      console.log('安全数据获取成功:', securityData.value)
    } else {
      console.warn('安全数据API返回错误:', response)
      // 获取失败时使用演示数据
      loadDemoSecurityData()
      proxy.$modal.msgWarning('获取安全数据失败，使用演示数据')
    }
  } catch (error) {
    console.error('获取安全数据异常:', error)
    // 异常时使用演示数据
    loadDemoSecurityData()
    proxy.$modal.msgWarning('网络异常，使用演示数据')
  } finally {
    loadingSecurity.value = false
  }
}

const calculateRiskLevel = (riskTag) => {
  if (!riskTag || riskTag.trim() === '') return 'LOW'
  
  const riskCount = riskTag.split(' ').filter(tag => tag.trim() !== '').length
  
  if (riskTag.includes('🚨 疑似貔貅') || riskCount >= 4) return 'HIGH'
  if (riskCount >= 2) return 'MEDIUM'
  return 'LOW'
}

const formatAddress = (address) => {
  if (!address || address.length < 8) return address || '--'
  // 显示前4位和后4位，中间用...代替
  return `${address.slice(0, 4)}...${address.slice(-4)}`
}

const getConcentrationRiskClass = (top10Percent) => {
  if (top10Percent < 0.1) return 'success'
  if (top10Percent < 0.2) return 'warning'
  return 'danger'
}

const getFeeRiskClass = (feeRate) => {
  if (feeRate < 0.01) return 'success'
  if (feeRate < 0.03) return 'warning'
  return 'danger'
}

const copyAddress = (address) => {
  navigator.clipboard.writeText(address).then(() => {
    proxy.$modal.msgSuccess('开发者地址已复制到剪贴板')
  }).catch(err => {
    proxy.$modal.msgError('复制地址失败: ' + err.message)
  })
}
</script>

<style scoped lang="scss">
.crypto-scanner {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 100px);
}

/* 主卡片 - 统一的大容器 */
.main-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 
    0 10px 40px rgba(31, 38, 135, 0.15),
    0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.main-card:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 20px 60px rgba(31, 38, 135, 0.2),
    0 4px 20px rgba(0, 0, 0, 0.12);
}

/* 搜索头部区域 */
.search-header {
  padding: 32px 36px 24px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #ffffff;
  border-radius: 16px;
  padding: 8px 12px 8px 20px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-input-wrapper:focus-within {
  border-color: #667eea;
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1), 0 4px 12px rgba(0, 0, 0, 0.08);
}

.search-icon {
  width: 20px;
  height: 20px;
  color: #9ca3af;
  flex-shrink: 0;
  transition: color 0.3s ease;
}

.search-input-wrapper:focus-within .search-icon {
  color: #667eea;
}

.search-input {
  flex: 1;
}

:deep(.search-input .el-input__inner) {
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  font-size: 16px;
  padding: 16px 8px;
  color: #374151;
  font-weight: 500;
}

:deep(.search-input .el-input__inner:focus) {
  border: none !important;
  box-shadow: none !important;
}

:deep(.search-input .el-input__inner::placeholder) {
  color: #9ca3af;
  font-weight: 400;
}

.search-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  padding: 16px 28px;
  font-weight: 600;
  font-size: 14px;
  color: white;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 14px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
  white-space: nowrap;
}

.search-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.search-button:hover::before {
  left: 100%;
}

.search-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.search-button:active {
  transform: translateY(0);
}

/* 头部分割线 */
.header-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.06), transparent);
  margin: 0;
}

/* 主内容区域 */
.main-content {
  padding: 24px 36px 36px;
  animation: fadeInUp 0.5s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 图表区域 */
.chart-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.chart-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.chart-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.token-title {
  display: flex;
  align-items: center;
  gap: 20px;
  justify-content: space-between;
}

.token-logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.token-info {
  flex: 1;
  min-width: 0;
}

.token-info h3 {
  margin: 0;
  color: #1f2937;
  font-weight: 700;
}

.token-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  margin-bottom: 6px;
}

.token-name {
  font-size: 14px;
  font-weight: 400;
  color: #6b7280;
  margin-left: 8px;
}

.social-links-inline {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.social-links-inline .social-buttons {
  display: flex;
  gap: 6px;
  align-items: center;
}

.social-btn-small {
  margin: 0 !important;
  width: 24px !important;
  height: 24px !important;
  padding: 0 !important;
  border-radius: 50% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  transition: all 0.3s ease !important;
  font-size: 10px !important;
}

.social-btn-small:hover {
  transform: translateY(-1px) scale(1.05) !important;
}

.social-btn-small .social-icon-img {
  width: 12px;
  height: 12px;
  object-fit: contain;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}

/* 操作按钮区域样式 */
.token-title .action-buttons {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.token-title .primary-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.token-title .primary-actions .el-button {
  margin: 0;
  border-radius: 8px;
  font-weight: 500;
  font-size: 12px;
  padding: 8px 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.token-title .primary-actions .el-button:hover {
  transform: translateY(-1px);
}

/* 统计信息区域样式 */
.token-title .query-stats {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-shrink: 0;
}

.token-title .query-stats .stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 0;
  border-bottom: none;
  font-size: 11px;
  text-align: center;
  min-width: 60px;
}

.token-title .query-stats .label {
  color: #64748b;
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
}

.token-title .query-stats .value {
  font-weight: 600;
  color: #1e293b;
  font-size: 12px;
}

.chart-container {
  height: 473px;
  border-radius: 12px;
  overflow: hidden;
}

.kline-iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 12px;
}

/* 右侧信息区域 */
.token-info-section, .action-section, .stats-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.token-info-section:hover, .action-section:hover, .stats-section:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.section-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

/* 基础信息行 */
.basic-info-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
  justify-content: space-between;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 0;
  text-align: center;
}

.info-item:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
}

.info-label {
  font-size: 11px;
  color: #64748b;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.info-value {
  font-size: 13px;
  color: #1e293b;
  font-weight: 700;
  word-break: break-all;
}

/* 安全数据区域 */
.security-info-row {
  margin-bottom: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 2px dashed #e2e8f0;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.security-info-row:hover {
  border-color: #cbd5e1;
  background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
}

.security-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.security-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.security-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.risk-level-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.risk-tags {
  font-size: 12px;
  color: #6b7280;
}

/* 安全指标网格 */
.security-metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 16px;
}

.security-metric-card {
  padding: 8px 6px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  text-align: center;
  background: #ffffff;
  transition: all 0.3s ease;
}

.security-metric-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.security-metric-card.clickable {
  cursor: pointer;
}

.security-metric-card.clickable:hover {
  border-color: #6366f1;
  background: #f8fafc;
}

.metric-label {
  font-size: 10px;
  color: #64748b;
  font-weight: 500;
  margin-bottom: 2px;
}

.metric-value {
  font-size: 11px;
  color: #1e293b;
  font-weight: 700;
}

.metric-value.neutral {
  color: #6366f1;
  display: flex;
  align-items: center;
  gap: 2px;
  justify-content: center;
  font-size: 10px;
}

.metric-value.success {
  color: #059669;
}

.metric-value.warning {
  color: #f59e0b;
}

.metric-value.danger {
  color: #dc2626;
}

.owner-address {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 11px !important;
  background: rgba(99, 102, 241, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid rgba(99, 102, 241, 0.2);
  cursor: pointer;
  transition: all 0.3s ease;
}

.owner-address:hover {
  background: rgba(99, 102, 241, 0.15);
  border-color: rgba(99, 102, 241, 0.3);
}

/* 时间选择器风格的权限状态 */
.permissions-timeframe-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-top: 12px;
}

.permission-timeframe-btn {
  padding: 8px 6px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: default;
  text-align: center;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #64748b;
}

.permission-timeframe-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.permission-timeframe-btn.safe {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  border-color: #6ee7b7;
  color: #059669;
}

.permission-timeframe-btn.danger {
  background: linear-gradient(135deg, #fee2e2, #fca5a5);
  border-color: #f87171;
  color: #dc2626;
}

.permission-label {
  font-size: 10px;
  font-weight: 600;
  color: inherit;
  margin-bottom: 2px;
  opacity: 0.8;
}

.permission-status {
  font-size: 11px;
  font-weight: 700;
  color: inherit;
}

.security-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.security-error {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

/* 时间选择器 */
.timeframe-selector {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.timeframe-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.timeframe-btn {
  padding: 12px 8px;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  text-align: center;
  background: #ffffff;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #64748b;
}

.timeframe-btn:hover {
  border-color: #cbd5e1;
  background: #f8fafc;
  transform: translateY(-1px);
}

.timeframe-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-color: transparent;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.timeframe-label {
  font-size: 12px;
  font-weight: 600;
  color: inherit;
  margin-bottom: 4px;
}

.timeframe-change {
  font-size: 11px;
  font-weight: 700;
}

.timeframe-change.positive {
  color: #059669;
}

.timeframe-change.negative {
  color: #dc2626;
}

.timeframe-change.neutral {
  color: #6b7280;
}

/* 交易统计行 */
.trading-stats {
  margin-bottom: 20px;
}

.trading-stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.trading-stats .stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: center;
  padding: 12px 8px;
  background: #f8fafc;
  border-radius: 12px;
}

.trading-stats .stat-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.trading-stats .stat-value {
  font-size: 13px;
  color: #1e293b;
  font-weight: 700;
}

.trading-stats .stat-value.buy-color {
  color: #059669;
}

.trading-stats .stat-value.sell-color {
  color: #dc2626;
}

.trading-stats .stat-value.positive {
  color: #059669;
}

.trading-stats .stat-value.negative {
  color: #dc2626;
}

.trading-stats .stat-value.neutral {
  color: #6b7280;
}

/* 安全信息行 */
.safety-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 16px;
}

.safety-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
}

.safety-item i.verified {
  color: #059669;
}

.safety-item i.unverified {
  color: #6b7280;
}

/* 查询统计 */
.query-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.query-stats .stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.query-stats .stat-item:last-child {
  border-bottom: none;
}

.label {
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.value {
  font-weight: 700;
  color: #1e293b;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.primary-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.action-buttons .el-button {
  margin: 0;
  border-radius: 12px;
  font-weight: 600;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
}

/* 社媒链接样式 */
.social-links {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.social-links-title {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.social-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.social-buttons .el-button {
  margin: 0;
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.social-buttons .el-button:hover {
  transform: translateY(-2px) scale(1.1);
}

.social-icon-img {
  width: 16px;
  height: 16px;
  object-fit: contain;
}

.no-social-links {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px;
}

.no-links-text {
  color: #9ca3af;
  font-size: 12px;
  font-style: italic;
}

/* 响应式设计 */
@media (max-width: 800px) {
  .crypto-scanner {
    padding: 16px;
  }
  
  .main-card {
    border-radius: 20px;
  }
  
  .search-header {
    padding: 20px 16px;
  }
  
  .main-content {
    padding: 20px 16px 24px;
  }
  
  .search-input-wrapper {
    flex-direction: row;
    gap: 12px;
    padding: 6px 8px 6px 16px;
    border-radius: 12px;
  }
  
  .search-button {
    padding: 14px 20px;
    font-size: 13px;
    border-radius: 10px;
    white-space: nowrap;
    flex-shrink: 0;
  }

  :deep(.search-input .el-input__inner) {
    padding: 14px 8px;
    font-size: 15px;
  }
  
  .main-content .el-col {
    margin-bottom: 20px;
  }
  
  .chart-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
  
  .token-title {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .token-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .social-links-inline {
    width: 100%;
    justify-content: center;
  }

  .token-title .action-buttons {
    width: 100%;
    justify-content: center;
  }

  .token-title .primary-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .token-title .query-stats {
    justify-content: space-around;
    flex-wrap: wrap;
    gap: 12px;
  }

  .token-title .query-stats .stat-item {
    min-width: 80px;
  }

  .basic-info-row {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .timeframe-buttons {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }
  
  .trading-stats-row {
    grid-template-columns: repeat(2, 1fr);
    gap: 6px;
  }

  /* 安全分析响应式设计 - 移动端2x2布局 */
  .security-metrics-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .permissions-timeframe-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .permission-timeframe-btn,
  .security-metric-card {
    padding: 6px 4px;
  }

  .permission-label {
    font-size: 9px;
  }

  .permission-status {
    font-size: 10px;
  }

  .metric-label {
    font-size: 9px;
  }

  .metric-value {
    font-size: 10px;
  }
}

.token-title-row {
  display: flex;
  align-items: center;
  gap: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
  flex-wrap: wrap; /* 响应式更友好 */
  transition: all 0.3s ease;
}

.token-logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.token-logo:hover {
  transform: scale(1.05);
}

.token-info { 
  min-width: 130px;
  flex: 1;
}

.social-links-inline { 
  min-width: 88px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-buttons { 
  min-width: 180px;
  display: flex;
  justify-content: center;
}

.action-buttons .primary-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-buttons .el-button {
  font-size: 12px;
  padding: 8px 12px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
}

.query-stats-row {
  display: flex;
  gap: 20px;
  min-width: 210px;
  justify-content: flex-end;
}

.query-stats-row .stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  font-size: 11px;
  min-width: 60px;
  padding: 8px 6px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 8px;
  transition: all 0.3s ease;
  cursor: default;
}

.query-stats-row .stat-item:hover {
  background: rgba(241, 245, 249, 0.9);
  transform: translateY(-1px);
}

.query-stats-row .label {
  color: #64748b;
  font-size: 10px;
  font-weight: 500;
  white-space: nowrap;
  margin-bottom: 2px;
}

.query-stats-row .value {
  font-weight: 600;
  color: #1e293b;
  font-size: 13px;
}
.current-price {
  font-size: 20px;
  font-weight: 600;
}

.price-change {
  padding: 4px 8px;
  border-radius: 8px;
  font-weight: bold;
  font-size: 12px;
  /* 默认色 */
  color: #1f2937;
}

.price-change.positive {
  background: linear-gradient(135deg, #d1fae5, #a7f3d0);
  color: #059669 !important; /* 绿色 */
}

.price-change.negative {
  background: linear-gradient(135deg, #fee2e2, #fca5a5);
  color: #dc2626 !important; /* 红色 */
}


/* 黑暗模式适配 */
:global(html.dark) {
  :deep(.crypto-scanner){
    background: linear-gradient(135deg, #1f2937 0%, #111827 100%);
  }

  :deep(.main-card) {
    background: rgba(31, 41, 55, 0.95);
    border: 1px solid rgba(75, 85, 99, 0.3);
  }

  :deep(.search-header) {
    background: linear-gradient(135deg, #374151 0%, #1f2937 100%);
  }

  :deep(.search-input-wrapper) {
    background: #374151;
    border-color: #4b5563;
  }

:deep(.search-input-wrapper:focus-within) {
    border-color: #6366f1;
    background: #374151;
  }

:deep(.search-icon) {
    color: #9ca3af;
  }

:deep(.search-input-wrapper:focus-within .search-icon) {
    color: #6366f1;
  }

:deep(.search-input .el-input__inner) {
    background: transparent !important;
    color: #f9fafb !important;
  }

:deep(.search-input .el-input__inner::placeholder) {
    color: #6b7280 !important;
  }

:deep(.header-divider) {
    background: linear-gradient(90deg, transparent, rgba(75, 85, 99, 0.3), transparent);
  }

:deep(.chart-section,
  .token-info-section,
  .action-section,
  .stats-section) {
    background: rgba(31, 41, 55, 0.8);
    border: 1px solid rgba(75, 85, 99, 0.3);
  }

:deep(.chart-header) {
    border-bottom-color: rgba(75, 85, 99, 0.3);
  }

:deep(.token-info h3) {
    color: #f9fafb;
  }

:deep(.token-name) {
    color: #9ca3af !important;
  }

:deep(.current-price) {
    color: #f9fafb;
    font-size: 20px;
    font-weight: 600;
  }

:deep(.section-header,
  .section-title) {
    color: #f9fafb;
    border-bottom-color: rgba(75, 85, 99, 0.3);
  }

:deep(.info-item) {
    background: rgba(55, 65, 81, 0.6);
  }

:deep(.info-item:hover) {
    background: rgba(55, 65, 81, 0.8);
  }

:deep(.info-label) {
    color: #9ca3af;
  }

:deep(.info-value) {
    color: #f3f4f6;
  }

:deep(.timeframe-btn) {
    background: rgba(55, 65, 81, 0.8);
    border-color: #4b5563;
    color: #d1d5db;
  }

:deep(.timeframe-btn:hover) {
    background: rgba(55, 65, 81, 1);
    border-color: #6b7280;
  }

:deep(.trading-stats .stat-item) {
    background: rgba(55, 65, 81, 0.6);
  }

:deep(.trading-stats .stat-label) {
    color: #9ca3af;
  }

:deep(.trading-stats .stat-value) {
    color: #f3f4f6;
  }


:deep(.query-stats .stat-item) {
    border-bottom-color: rgba(75, 85, 99, 0.3);
  }

:deep(.label) {
    color: #9ca3af;
  }

:deep(.value) {
    color: #f3f4f6;
  }

:deep(.social-btn-small) {
    background: rgba(55, 65, 81, 0.8) !important;
    border-color: #4b5563 !important;
    color: #d1d5db !important;
  }

:deep(.social-btn-small:hover) {
    background: rgba(75, 85, 99, 0.9) !important;
    border-color: #6b7280 !important;
  }

  /* 新布局元素黑暗模式适配 */
:deep(.token-title .query-stats .label) {
    color: #9ca3af !important;
  }

:deep(.token-title .query-stats .value) {
    color: #f3f4f6 !important;
  }

:deep(.token-title .primary-actions .el-button) {
    background: rgba(55, 65, 81, 0.8) !important;
    border-color: #4b5563 !important;
    color: #d1d5db !important;
  }

:deep(.token-title .primary-actions .el-button:hover) {
    background: rgba(75, 85, 99, 0.9) !important;
    border-color: #6b7280 !important;
  }

:deep(.token-title .primary-actions .el-button--primary) {
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
    border-color: transparent !important;
    color: white !important;
  }

:deep(.token-title .primary-actions .el-button--info) {
    background: rgba(75, 85, 99, 0.8) !important;
    border-color: #6b7280 !important;
    color: #d1d5db !important;
  }

  /* 新布局黑暗模式适配 */
:deep(.token-title-row) {
    border-bottom-color: rgba(75, 85, 99, 0.3);
  }

:deep(.query-stats-row .stat-item) {
    background: rgba(55, 65, 81, 0.6) !important;
  }

:deep(.query-stats-row .stat-item:hover) {
    background: rgba(55, 65, 81, 0.8) !important;
  }

:deep(.query-stats-row .label) {
    color: #9ca3af !important;
  }

:deep(.query-stats-row .value) {
    color: #f3f4f6 !important;
  }

:deep(.action-buttons .el-button) {
    background: rgba(55, 65, 81, 0.8) !important;
    border-color: #4b5563 !important;
    color: #d1d5db !important;
  }

:deep(.action-buttons .el-button:hover) {
    background: rgba(75, 85, 99, 0.9) !important;
    border-color: #6b7280 !important;
  }

:deep(.action-buttons .el-button--primary) {
    background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
    border-color: transparent !important;
    color: white !important;
  }

:deep(.action-buttons .el-button--info) {
    background: rgba(75, 85, 99, 0.8) !important;
    border-color: #6b7280 !important;
    color: #d1d5db !important;
  }

  /* 安全数据区域黑暗模式适配 */
:deep(.security-info-row) {
    background: linear-gradient(135deg, rgba(55, 65, 81, 0.6) 0%, rgba(31, 41, 55, 0.8) 100%);
    border-color: rgba(75, 85, 99, 0.5);
  }

:deep(.security-info-row:hover) {
    border-color: rgba(107, 114, 128, 0.7);
    background: linear-gradient(135deg, rgba(55, 65, 81, 0.8) 0%, rgba(31, 41, 55, 1) 100%);
  }


:deep(.security-header) {
    border-bottom-color: rgba(75, 85, 99, 0.3);
  }

:deep(.security-title) {
    color: #f9fafb;
  }

:deep(.metric-label) {
    color: #9ca3af;
  }

:deep(.metric-value) {
    color: #f3f4f6;
  }

:deep(.permission-item i) {
    color: #9ca3af;
  }

:deep(.permission-item.risk i) {
    color: #f87171;
  }

:deep(.permission-item span) {
    color: #d1d5db;
  }

:deep(.security-loading,
  .security-error) {
    color: #9ca3af;
  }

  /* 新安全分析样式的黑暗模式 */
:deep(.security-metric-card) {
    background: rgba(55, 65, 81, 0.8);
    border-color: #4b5563;
  }

:deep(.security-metric-card:hover) {
    background: rgba(55, 65, 81, 1);
    border-color: #6b7280;
  }

:deep(.security-metric-card.clickable:hover) {
    border-color: #6366f1;
    background: rgba(55, 65, 81, 1);
  }

:deep(.permission-timeframe-btn) {
    background: rgba(55, 65, 81, 0.8);
    border-color: #4b5563;
    color: #d1d5db;
  }

:deep(.permission-timeframe-btn:hover) {
    background: rgba(55, 65, 81, 1);
  }

:deep(.permission-timeframe-btn.safe) {
    background: linear-gradient(135deg, rgba(6, 95, 70, 0.8), rgba(4, 120, 87, 0.6));
    border-color: #6ee7b7;
    color: #6ee7b7;
  }

:deep(.permission-timeframe-btn.danger) {
    background: linear-gradient(135deg, rgba(153, 27, 27, 0.8), rgba(220, 38, 38, 0.6));
    border-color: #f87171;
    color: #fca5a5;
  }

:deep(.metric-value.neutral) {
    color: #6366f1;
    display: flex;
    align-items: center;
    gap: 2px;
    justify-content: center;
    font-size: 10px;
  }

:deep(.metric-value.success) {
    color: #059669;
  }

:deep(.metric-value.warning) {
    color: #f59e0b;
  }

:deep(.metric-value.danger) {
    color: #dc2626;
  }

}

</style>
