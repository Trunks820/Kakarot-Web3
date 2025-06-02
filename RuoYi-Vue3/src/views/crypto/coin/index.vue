<template>
  <div class="crypto-scanner">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-card class="search-card">
        <div class="search-container">
          <!-- 公链指示器 -->
          <div class="chain-indicator">
            <span class="chain-label">当前公链:</span>
            <el-tag :type="getChainTagType(currentChain)" size="small">
              {{ getChainDisplayName(currentChain) }}
            </el-tag>
          </div>
          
          <div class="search-input-group">
            <el-input
              v-model="searchCA"
              placeholder="输入CA地址查询代币信息..."
              class="search-input"
              @keyup.enter="searchToken"
              clearable
            >
              <template slot="prepend">
                <i class="el-icon-search"></i>
              </template>
              <template slot="append">
                <el-button @click="searchToken" type="primary" :loading="searching">
                  查询
                </el-button>
              </template>
            </el-input>
          </div>
          
          <!-- 测试CA地址快捷按钮 -->
          <div class="test-addresses">
            <div class="test-addresses-label">快速测试:</div>
            <div class="test-buttons">
              <el-button 
                size="mini" 
                @click="fillTestAddress('So11111111111111111111111111111111111111112')"
                type="success"
              >
                SOL测试
              </el-button>
              <el-button 
                size="mini" 
                @click="fillTestAddress('0xdAC17F958D2ee523a2206206994597C13D831ec7')"
                type="info"
              >
                USDT(ETH)测试
              </el-button>
              <el-button 
                size="mini" 
                @click="debugTokenData"
                type="warning"
              >
                调试数据
              </el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content" v-if="tokenData">
      <el-row :gutter="20">
        <!-- 左侧K线图区域 -->
        <el-col :span="16">
          <el-card class="chart-card">
            <div slot="header" class="chart-header">
              <div class="token-title">
                <img :src="tokenData.logoUrl" class="token-logo" v-if="tokenData.logoUrl">
                <div class="token-info">
                  <h3>{{ tokenData.symbol }}</h3>
                  <div class="price-info">
                    <span class="current-price">${{ formatPrice(tokenData.price) }}</span>
                    <span :class="['price-change', tokenData.change24h >= 0 ? 'positive' : 'negative']">
                      {{ tokenData.change24h >= 0 ? '+' : '' }}{{ tokenData.change24h }}%
                    </span>
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
          </el-card>
        </el-col>

        <!-- 右侧信息操作区域 -->
        <el-col :span="8">
          <!-- 代币信息 - GMGN风格 -->
          <el-card class="token-info-card" style="margin-bottom: 16px;">
            <div slot="header">
              
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
                <span class="info-value">{{ formatNumber(tokenData.holderCount) || '--' }}</span>
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

            <!-- 安全信息行 -->
            <div class="safety-row">
              <div class="safety-item">
                <i class="el-icon-shield" :class="tokenData.isVerified ? 'verified' : 'unverified'"></i>
                <span>{{ tokenData.isVerified ? '已验证' : '未验证' }}</span>
              </div>
              <div class="safety-item">
                <span>安全评分: </span>
                <el-tag :type="getSafetyType(tokenData.safetyScore)" size="mini">
                  {{ tokenData.safetyScore }}/100
                </el-tag>
              </div>
            </div>

            <!-- 官方社媒链接 -->
            <div class="social-links" v-if="tokenData.socialLinks && tokenData.socialLinks.length > 0">
              <div class="social-links-title">官方链接</div>
              <div class="social-buttons">
                <el-button 
                  v-for="link in tokenData.socialLinks" 
                  :key="link.type"
                  size="mini" 
                  @click="openSocialLink(link.url)"
                  :type="getSocialButtonType(link.type)"
                  circle
                  :title="link.type"
                >
                  <img 
                    v-if="getSocialIcon(link.type).includes('.png')" 
                    :src="getSocialIcon(link.type)" 
                    alt="Social Icon"
                    class="social-icon-img"
                  >
                  <i 
                    v-else
                    :class="getSocialIcon(link.type)"
                  ></i>
                </el-button>
              </div>
            </div>
            
            <!-- 无社媒链接提示 -->
            <div class="social-links" v-else>
              <div class="social-links-title">官方链接</div>
              <div class="no-social-links">
                <span class="no-links-text">暂无官方链接信息</span>
              </div>
            </div>
          </el-card>

          <!-- 快捷操作 -->
          <el-card class="action-card">
            <div class="action-buttons">
              <!-- 主要操作组 -->
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
          </el-card>

          <!-- 查询统计 -->
          <el-card class="stats-card">
            <div slot="header">查询统计</div>
            <div class="query-stats">
              <div class="stat-item">
                <span class="label">总查询次数</span>
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
          </el-card>
        </el-col>
      </el-row>
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

<script>
export default {
  name: "CryptoScanner",
  data() {
    return {
      searchCA: '',
      searching: false,
      tokenData: null,
      timeframe: '1h',
      monitoring: false,
      favoriting: false,
      settingAlert: false,
      alertDialogVisible: false,
      alertForm: {
        targetPrice: '',
        alertType: 'above',
        notifyMethods: ['telegram']
      },
      selectedTimeframe: 'm5',
      timeframes: [
        { value: 'm5', label: '1m' },
        { value: 'h1', label: '1h' },
        { value: 'h6', label: '6h' },
        { value: 'h24', label: '24h' }
      ],
      // 监控状态管理
      isMonitored: false,
      monitorStatus: 'not_monitored', // not_monitored, monitoring, monitored
      priceAlertSet: false,
      // 公链状态
      currentChain: 'sol' // 当前检测到的公链
    }
  },
  mounted() {
    // 示例：自动加载一个代币作为演示
    this.loadDemoToken()
  },
  methods: {
    // 搜索代币
    async searchToken() {
      if (!this.searchCA) {
        this.$message.warning('请输入CA地址')
        return
      }
      
      // 验证地址格式是否有效
      if (!this.validateAddressFormat(this.searchCA)) {
        this.$message.warning('地址格式无效，请检查地址是否正确')
        return
      }
      
      // 检测公链
      const detectedChain = this.detectChainFromCA(this.searchCA)
      this.currentChain = detectedChain
      
      // 显示检测结果
      if (detectedChain === 'sol') {
        this.$message.info(`✅ 检测到Solana地址，正在查询...`)
      } else if (detectedChain === 'ethereum') {
        this.$message.info(`✅ 检测到EVM地址 (默认Ethereum)，正在查询...`)
      } else if (detectedChain === 'unknown') {
        this.$message.warning(`⚠️ 地址格式不被识别，可能不是有效的加密货币地址`)
        return
      } else {
        this.$message.warning(`⚠️ 地址格式可能不正确，尝试以${detectedChain.toUpperCase()}公链查询...`)
      }
      
      this.searching = true
      
      // 重置状态
      this.isMonitored = false
      this.monitorStatus = 'not_monitored'
      this.priceAlertSet = false
      
      try {
        await this.loadTokenDataFromAPI()
        if (!this.tokenData) {
          this.$message.warning('未找到该代币信息，请检查CA地址是否正确')
        } else {
          this.$message.success(`🎉 成功加载${this.tokenData.symbol}代币信息`)
        }
      } catch (error) {
        this.$message.error('查询失败: ' + error.message)
      } finally {
        this.searching = false
      }
    },

    // 加载演示数据
    async loadDemoToken() {
      this.searchCA = "So11111111111111111111111111111111111111112"
      await this.loadTokenDataFromAPI()
    },

    // 从DexScreener API加载真实代币数据
    async loadTokenDataFromAPI() {
      if (!this.searchCA) return
      
      try {
        // 使用新的latest API接口，信息更全面
        const response = await fetch(`https://api.dexscreener.com/latest/dex/tokens/${this.searchCA}`)
        const data = await response.json()
        
        if (data && data.pairs && data.pairs.length > 0) {
          // 选择流动性最高的交易对作为主要数据源
          const tokenPair = this.selectBestPair(data.pairs)
          const baseToken = tokenPair.baseToken
          
          // 调试输出API数据结构
          console.log('DexScreener API Response:', data)
          console.log('Selected Token Pair:', tokenPair)
          console.log('Txns Data:', tokenPair.txns)
          console.log('Volume Data:', tokenPair.volume)
          console.log('Price Change Data:', tokenPair.priceChange)
          console.log('Social Links Info:', tokenPair.info)
          console.log('Websites:', tokenPair.info?.websites)
          console.log('Socials:', tokenPair.info?.socials)
          console.log('Extracted Social Links:', this.extractSocialLinks(tokenPair.info))
          
          this.tokenData = {
            name: baseToken.name,
            symbol: baseToken.symbol,
            address: baseToken.address,
            logoUrl: this.getChainLogo(this.currentChain),
            price: parseFloat(tokenPair.priceUsd) || 0,
            change24h: tokenPair.priceChange?.h24 || 0,
            marketCap: tokenPair.marketCap || tokenPair.fdv || 0,
            volume24h: tokenPair.volume?.h24 || 0,
            high24h: this.calculateHigh24h(tokenPair),
            low24h: this.calculateLow24h(tokenPair),
            holderCount: 0, // DexScreener不提供这个数据
            liquidity: tokenPair.liquidity?.usd || 0,
            safetyScore: this.calculateSafetyScore(tokenPair),
            isVerified: !!tokenPair.info?.websites?.length,
            hasRenounced: false, // 需要其他API获取
            queryCount: Math.floor(Math.random() * 1000) + 100, // 模拟数据
            todayQueries: Math.floor(Math.random() * 100) + 10, // 模拟数据
            monitorCount: Math.floor(Math.random() * 50) + 5, // 模拟数据
            // 新增：交易对信息
            pairInfo: {
              dexId: tokenPair.dexId,
              pairAddress: tokenPair.pairAddress,
              url: tokenPair.url,
              labels: tokenPair.labels || [],
              pairCreatedAt: tokenPair.pairCreatedAt
            },
            // 新增：实时交易数据
            realtimeData: this.processRealtimeData(tokenPair),
            // 新增：官方社媒链接
            socialLinks: this.extractSocialLinks(tokenPair.info)
          }
        } else {
          this.loadMockTokenData() // 如果API没有数据，使用备用数据
        }
      } catch (error) {
        console.error('Failed to load token data from DexScreener:', error)
        this.loadMockTokenData() // API失败时使用备用数据
      }
    },

    // 选择最佳交易对（流动性最高的）
    selectBestPair(pairs) {
      if (pairs.length === 1) return pairs[0]
      
      // 按流动性排序，选择流动性最高的交易对
      return pairs.reduce((best, current) => {
        const bestLiquidity = best.liquidity?.usd || 0
        const currentLiquidity = current.liquidity?.usd || 0
        return currentLiquidity > bestLiquidity ? current : best
      })
    },

    // 计算24小时最高价
    calculateHigh24h(tokenPair) {
      const currentPrice = parseFloat(tokenPair.priceUsd) || 0
      const change24h = tokenPair.priceChange?.h24 || 0
      
      if (change24h >= 0) {
        // 如果是正涨幅，当前价就是最高价
        return currentPrice
      } else {
        // 如果是负涨幅，计算24小时前的价格作为最高价
        return currentPrice / (1 + change24h / 100)
      }
    },

    // 计算24小时最低价
    calculateLow24h(tokenPair) {
      const currentPrice = parseFloat(tokenPair.priceUsd) || 0
      const change24h = tokenPair.priceChange?.h24 || 0
      
      if (change24h <= 0) {
        // 如果是负涨幅，当前价就是最低价
        return currentPrice
      } else {
        // 如果是正涨幅，计算24小时前的价格作为最低价
        return currentPrice / (1 + change24h / 100)
      }
    },

    // 根据公链类型获取logo
    getChainLogo(chainId) {
      const chainLogos = {
        'sol': '/src/assets/crypto-icons/SOL.png',
        'solana': '/src/assets/crypto-icons/SOL.png',
        'ethereum': '/src/assets/crypto-icons/ETH.png', 
        'bsc': '/src/assets/crypto-icons/BNB.png',
        'base': '/src/assets/crypto-icons/BASE.png'
      }
      return chainLogos[chainId] || chainLogos['sol'] // 默认使用Solana logo
    },

    // 计算安全评分
    calculateSafetyScore(tokenPair) {
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
    },

    // 操作功能
    async addToMonitor() {
      this.monitoring = true
      this.monitorStatus = 'monitoring'
      try {
        // 调用添加监控API
        await this.simulateApiCall()
        this.monitorStatus = 'monitored'
        this.isMonitored = true
        this.$message.success('添加监控成功！')
      } catch (error) {
        this.monitorStatus = 'not_monitored'
        this.$message.error('添加监控失败')
      } finally {
        this.monitoring = false
      }
    },

    async addToFavorite() {
      this.favoriting = true
      try {
        await this.simulateApiCall()
        this.$message.success('收藏成功！')
      } catch (error) {
        this.$message.error('收藏失败')
      } finally {
        this.favoriting = false
      }
    },

    copyCA() {
      if (this.tokenData) {
        navigator.clipboard.writeText(this.tokenData.address)
        this.$message.success('CA地址已复制到剪贴板')
      }
    },

    setPriceAlert() {
      this.alertDialogVisible = true
    },

    async confirmPriceAlert() {
      this.settingAlert = true
      try {
        await this.simulateApiCall()
        this.priceAlertSet = true
        this.$message.success('价格提醒设置成功！')
        this.alertDialogVisible = false
      } catch (error) {
        this.$message.error('设置失败')
      } finally {
        this.settingAlert = false
      }
    },

    viewHolders() {
      this.$message.info('查看持有者功能开发中...')
    },

    openInExplorer() {
      if (this.tokenData) {
        window.open(`https://solscan.io/token/${this.tokenData.address}`, '_blank')
      }
    },

    refreshTokenData() {
      this.searchToken()
    },

    // 验证地址格式是否有效
    validateAddressFormat(address) {
      if (!address) return false
      
      // 检查是否为有效的Solana地址
      const solAddress = this.findSolanaAddress(address)
      if (solAddress) return true
      
      // 检查是否为有效的ETH地址
      const ethAddress = this.findEthAddress(address)
      if (ethAddress) return true
      
      return false
    },

    // 检测CA地址属于哪个公链
    detectChainFromCA(address) {
      if (!address) return 'sol' // 默认sol
      
      // 先检测是否为Solana地址
      const solAddress = this.findSolanaAddress(address)
      if (solAddress) {
        return 'sol'
      }
      
      // 再检测是否为ETH系地址
      const ethAddress = this.findEthAddress(address)
      if (ethAddress) {
        // ETH系地址，默认返回ethereum
        // 后续可以添加公链选择器让用户手动选择
        return 'ethereum'
      }
      
      return 'unknown' // 无法识别的地址格式
    },

    // 检测文字中的Solana地址
    findSolanaAddress(text) {
      if (!text) return null
      // Base58字符集模式
      const base58Pattern = /\b[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]{40,44}\b/g
      // 查找所有匹配的地址
      const addresses = text.match(base58Pattern)
      if (addresses && addresses.length > 0) {
        return addresses[0] // 返回第一个匹配的地址
      }
      return null
    },

    // 检测文字中的ETH地址
    findEthAddress(text) {
      if (!text) return null
      // 正则表达式匹配以 0x 开头的地址（长度 42 个字符）
      const ethPattern = /\b0x[a-fA-F0-9]{40}\b/g
      // 查找所有匹配的地址
      const ethAddresses = text.match(ethPattern)
      if (ethAddresses && ethAddresses.length > 0) {
        return ethAddresses[0] // 返回第一个匹配的地址
      }
      return null
    },

    // 生成gmgn K线图URL，根据公链动态调整
    getKlineUrl() {
      if (!this.tokenData || !this.tokenData.address) {
        return 'about:blank'
      }
      
      const chain = this.detectChainFromCA(this.tokenData.address)
      const chainMap = {
        'sol': 'sol',
        'solana': 'sol', 
        'ethereum': 'eth',
        'bsc': 'bsc',
        'base': 'base'
      }
      
      const gmgnChain = chainMap[chain] || 'sol'
      return `https://www.gmgn.cc/kline/${gmgnChain}/${this.tokenData.address}`
    },

    // 工具函数
    formatPrice(price) {
      if (price >= 1) {
        return price.toFixed(4)
      } else {
        return price.toFixed(8)
      }
    },

    formatNumber(num) {
      if (num >= 1e9) {
        return (num / 1e9).toFixed(2) + 'B'
      } else if (num >= 1e6) {
        return (num / 1e6).toFixed(2) + 'M'
      } else if (num >= 1e3) {
        return (num / 1e3).toFixed(2) + 'K'
      }
      return num?.toString() || '0'
    },

    getSafetyType(score) {
      if (score >= 80) return 'success'
      if (score >= 60) return 'warning'
      return 'danger'
    },

    // 格式化价格变化
    formatChange(change) {
      if (change === null || change === undefined) return '--'
      const sign = change >= 0 ? '+' : ''
      return `${sign}${change.toFixed(2)}%`
    },

    // 获取价格变化的样式类
    getChangeClass(change) {
      if (change === null || change === undefined) return 'neutral'
      if (change > 0) return 'positive'
      if (change < 0) return 'negative'
      return 'neutral'
    },

    simulateApiCall() {
      return new Promise(resolve => setTimeout(resolve, 1000))
    },

    // 模拟代币数据（备用）
    loadMockTokenData() {
      this.tokenData = {
        name: "Solana",
        symbol: "SOL", 
        address: "So11111111111111111111111111111111111111112",
        logoUrl: this.getChainLogo(this.currentChain),
        price: 98.76,
        change24h: 5.24,
        marketCap: 45678900000,
        volume24h: 1234567890,
        high24h: 102.45,
        low24h: 94.12,
        holderCount: 1234567,
        liquidity: 87654321,
        safetyScore: 85,
        isVerified: true,
        hasRenounced: false,
        queryCount: 1256,
        todayQueries: 89,
        monitorCount: 234,
        // 新增：实时交易数据
        realtimeData: {
          txns: {
            m5: { buys: 8, sells: 16 },
            h1: { buys: 122, sells: 89 },
            h6: { buys: 567, sells: 432 },
            h24: { buys: 1234, sells: 987 }
          },
          priceChange: {
            m5: -19.54,
            h1: 0.84,
            h6: -44.98,
            h24: 454.2
          },
          volume: {
            m5: 1194.06,
            h1: 45678,
            h6: 234567,
            h24: 1234567
          }
        },
        // 新增：官方社媒链接
        socialLinks: [
          { type: 'website', url: 'https://solana.com', label: '官网' },
          { type: 'twitter', url: 'https://twitter.com/solana', label: 'Twitter' },
          { type: 'telegram', url: 'https://t.me/solana', label: 'Telegram' },
          { type: 'discord', url: 'https://discord.gg/solana', label: 'Discord' }
        ]
      }
    },

    getSelectedPriceChange() {
      if (this.tokenData.realtimeData) {
        const priceChange = this.tokenData.realtimeData.priceChange
        if (this.selectedTimeframe === 'h1') return priceChange?.h1
        if (this.selectedTimeframe === 'h6') return priceChange?.h6
        if (this.selectedTimeframe === 'h24') return priceChange?.h24
        if (this.selectedTimeframe === 'm5') return priceChange?.m5
      }
      return null
    },

    getSelectedVolume() {
      if (this.tokenData.realtimeData && this.tokenData.realtimeData.volume) {
        const volume = this.tokenData.realtimeData.volume
        if (this.selectedTimeframe === 'h1') return volume?.h1 || 0
        if (this.selectedTimeframe === 'h6') return volume?.h6 || 0
        if (this.selectedTimeframe === 'h24') return volume?.h24 || 0
        if (this.selectedTimeframe === 'm5') return volume?.m5 || 0
      }
      return 0
    },

    getSelectedBuys() {
      if (this.tokenData.realtimeData && this.tokenData.realtimeData.txns) {
        const txns = this.tokenData.realtimeData.txns
        if (this.selectedTimeframe === 'h1') return txns?.h1?.buys || 0
        if (this.selectedTimeframe === 'h6') return txns?.h6?.buys || 0
        if (this.selectedTimeframe === 'h24') return txns?.h24?.buys || 0
        if (this.selectedTimeframe === 'm5') return txns?.m5?.buys || 0
      }
      return 0
    },

    getSelectedSells() {
      if (this.tokenData.realtimeData && this.tokenData.realtimeData.txns) {
        const txns = this.tokenData.realtimeData.txns
        if (this.selectedTimeframe === 'h1') return txns?.h1?.sells || 0
        if (this.selectedTimeframe === 'h6') return txns?.h6?.sells || 0
        if (this.selectedTimeframe === 'h24') return txns?.h24?.sells || 0
        if (this.selectedTimeframe === 'm5') return txns?.m5?.sells || 0
      }
      return 0
    },

    getNetBuys() {
      if (this.tokenData.realtimeData) {
        const buys = this.getSelectedBuys()
        const sells = this.getSelectedSells()
        return buys - sells
      }
      return 0
    },

    getNetBuyClass() {
      if (this.tokenData.realtimeData) {
        const netBuys = this.getNetBuys()
        if (netBuys > 0) return 'positive'
        if (netBuys < 0) return 'negative'
      }
      return 'neutral'
    },

    getPriceChangeByTimeframe(timeframe) {
      if (this.tokenData.realtimeData && this.tokenData.realtimeData.priceChange) {
        const priceChange = this.tokenData.realtimeData.priceChange
        if (timeframe === 'm5') return priceChange?.m5
        if (timeframe === 'h1') return priceChange?.h1
        if (timeframe === 'h6') return priceChange?.h6
        if (timeframe === 'h24') return priceChange?.h24
      }
      return null
    },

    getSelectedBuyVolume() {
      if (this.tokenData.realtimeData) {
        const buys = this.getSelectedBuys()
        const volume = this.getSelectedVolume()
        // 假设买入占总交易量的一半作为示例
        return volume ? volume * 0.6 : 0
      }
      return 0
    },

    getSelectedSellVolume() {
      if (this.tokenData.realtimeData) {
        const sells = this.getSelectedSells()
        const volume = this.getSelectedVolume()
        // 假设卖出占总交易量的一半作为示例
        return volume ? volume * 0.4 : 0
      }
      return 0
    },

    getNetBuysFormatted() {
      if (this.tokenData.realtimeData) {
        const buyVolume = this.getSelectedBuyVolume()
        const sellVolume = this.getSelectedSellVolume()
        const netVolume = buyVolume - sellVolume
        
        if (netVolume > 0) {
          return `+$${this.formatNumber(netVolume)}`
        } else if (netVolume < 0) {
          return `-$${this.formatNumber(Math.abs(netVolume))}`
        } else {
          return '$0'
        }
      }
      return '-$948'
    },

    getMonitorButtonType() {
      return this.monitorStatus === 'monitored' ? 'info' : 'primary'
    },

    getMonitorButtonIcon() {
      return this.monitorStatus === 'monitored' ? 'el-icon-info' : 'el-icon-monitor'
    },

    getMonitorButtonText() {
      return this.monitorStatus === 'monitored' ? '已监控' : '添加监控'
    },

    toggleMonitor() {
      if (this.monitorStatus === 'not_monitored') {
        this.addToMonitor()
      } else {
        // 实现取消监控的逻辑
        this.$message.info('取消监控功能开发中...')
      }
    },

    // 社媒链接相关方法
    openSocialLink(url) {
      window.open(url, '_blank')
    },

    getSocialIcon(type) {
      const iconMap = {
        'website': '/src/assets/crypto-icons/web.png',
        'twitter': '/src/assets/crypto-icons/twitter.png',
        'telegram': '/src/assets/crypto-icons/telegram.png',
        'discord': '/src/assets/crypto-icons/discord.png',
        'github': 'el-icon-document',
        'medium': 'el-icon-edit-outline',
        'reddit': 'el-icon-chat-line-round'
      }
      return iconMap[type.toLowerCase()] || 'el-icon-link'
    },

    getSocialButtonType(type) {
      // 使用图片图标后，统一使用默认白色按钮样式
      return ''
    },

    extractSocialLinks(info) {
      console.log('extractSocialLinks 输入参数:', info)
      const socialLinks = []
      
      if (!info) {
        console.log('info 为空，返回空数组')
        return socialLinks
      }
      
      // 打印所有可用的字段
      console.log('info 的所有字段:', Object.keys(info))
      
      // 官网链接 - 尝试多种可能的字段名
      const websiteFields = ['websites', 'website', 'links', 'urls']
      let websites = null
      
      for (const field of websiteFields) {
        if (info[field] && Array.isArray(info[field]) && info[field].length > 0) {
          websites = info[field]
          console.log(`在字段 ${field} 中找到官网链接:`, websites)
          break
        } else if (info[field] && typeof info[field] === 'string') {
          websites = [{ url: info[field] }]
          console.log(`在字段 ${field} 中找到单个官网链接:`, info[field])
          break
        }
      }
      
      if (websites) {
        websites.forEach(website => {
          console.log('处理官网:', website)
          const url = website.url || website
          if (url) {
            socialLinks.push({
              type: 'website',
              url: url,
              label: '官网'
            })
          }
        })
      } else {
        console.log('没有找到官网链接')
      }
      
      // 社交媒体链接 - 尝试多种可能的字段名
      const socialFields = ['socials', 'social', 'socialLinks', 'links']
      let socials = null
      
      for (const field of socialFields) {
        if (info[field] && Array.isArray(info[field]) && info[field].length > 0) {
          socials = info[field]
          console.log(`在字段 ${field} 中找到社交媒体链接:`, socials)
          break
        }
      }
      
      if (socials) {
        socials.forEach(social => {
          console.log('处理社交媒体:', social)
          const url = social.url || social
          if (url) {
            const socialType = this.detectSocialType(url)
            socialLinks.push({
              type: socialType,
              url: url,
              label: socialType
            })
          }
        })
      } else {
        console.log('没有找到社交媒体链接')
      }
      
      console.log('最终提取的社媒链接:', socialLinks)
      return socialLinks
    },

    detectSocialType(url) {
      if (url.includes('twitter.com') || url.includes('x.com')) return 'twitter'
      if (url.includes('t.me') || url.includes('telegram')) return 'telegram'
      if (url.includes('discord')) return 'discord'
      if (url.includes('github')) return 'github'
      if (url.includes('medium')) return 'medium'
      if (url.includes('reddit')) return 'reddit'
      return 'website'
    },

    getChainTagType(chain) {
      const chainTypes = {
        'sol': 'success',
        'solana': 'success',
        'ethereum': 'info',
        'bsc': 'warning',
        'base': 'info',
        'unknown': 'danger'
      }
      return chainTypes[chain] || 'info'
    },

    getChainDisplayName(chain) {
      const chainNames = {
        'sol': 'Solana',
        'solana': 'Solana',
        'ethereum': 'Ethereum',
        'bsc': 'Binance Smart Chain',
        'base': 'Base',
        'unknown': '未知公链'
      }
      return chainNames[chain] || 'Unknown'
    },

    // 添加测试地址
    fillTestAddress(address) {
      this.searchCA = address
      this.searchToken()
    },

    // 处理交易数据，确保数据完整性
    processRealtimeData(tokenPair) {
      // 如果API没有交易数据，生成基于价格变化的模拟数据
      const txns = tokenPair.txns || this.generateMockTxnsFromPriceChange(tokenPair.priceChange)
      const priceChange = tokenPair.priceChange || {}
      const volume = tokenPair.volume || {}
      
      return {
        txns,
        priceChange,
        volume
      }
    },

    // 基于价格变化生成模拟交易数据
    generateMockTxnsFromPriceChange(priceChange) {
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
    },

    debugTokenData() {
      console.log('当前代币数据:', this.tokenData)
      console.log('实时交易数据:', this.tokenData?.realtimeData)
      console.log('社媒链接:', this.tokenData?.socialLinks)
      console.log('选中时间周期:', this.selectedTimeframe)
      console.log('当前交易数据:', {
        buys: this.getSelectedBuys(),
        sells: this.getSelectedSells(),
        volume: this.getSelectedVolume(),
        priceChange: this.getPriceChangeByTimeframe(this.selectedTimeframe)
      })
      
      this.$message.info('调试信息已输出到控制台，请按F12查看')
    }
  }
}
</script>

<style scoped>
.crypto-scanner {
  padding: 20px;
  background: #f5f5f5;
  min-height: calc(100vh - 100px);
}

.search-section {
  margin-bottom: 20px;
}

.search-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 20px 0 rgba(102, 126, 234, 0.15);
}

.search-card >>> .el-card__body {
  padding: 30px;
}

.search-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chain-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.chain-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  font-weight: 500;
}

.search-input-group {
  display: flex;
  justify-content: center;
}

.test-addresses {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.test-addresses-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

.test-buttons {
  display: flex;
  gap: 8px;
}

.search-input {
  max-width: 600px;
  width: 100%;
}

.search-input >>> .el-input__inner {
  height: 50px;
  font-size: 16px;
  border-radius: 25px;
}

.search-input >>> .el-input-group__prepend {
  background: #fff;
  border-radius: 25px 0 0 25px;
}

.search-input >>> .el-input-group__append {
  background: transparent;
  border: none;
  border-radius: 0 25px 25px 0;
}

.quick-actions {
  display: flex;
  justify-content: center;
}

.main-content {
  margin-top: 20px;
}

.chart-card {
  height: 500px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: none;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.token-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.token-logo {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}

.token-info h3 {
  margin: 0;
  color: #333;
}

.price-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 5px;
}

.current-price {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.price-change {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: bold;
}

.price-change.positive {
  background: #f0f9ff;
  color: #10b981;
}

.price-change.negative {
  background: #fef2f2;
  color: #ef4444;
}

.chart-container {
  height: 400px;
  margin-top: 20px;
  border-radius: 4px;
  overflow: hidden;
}

.kline-iframe {
  width: 100%;
  height: 100%;
  border: none;
  border-radius: 4px;
}

.info-card, .action-card, .stats-card {
  margin-bottom: 16px;
}

.token-info-card {
  margin-bottom: 16px;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border: none;
}

/* GMGN风格基础信息行 */
.basic-info-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* 代币价格区域 */
.token-price-section {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #eee;
}

.token-pair {
  margin-bottom: 8px;
}

.token-symbol {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.pair-divider {
  margin: 0 4px;
  color: #666;
}

.quote-symbol {
  font-size: 14px;
  color: #666;
}

.price-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.usd-price {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.price-change-24h {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: bold;
}

.price-change-24h.positive {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
}

.price-change-24h.negative {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.1);
}

/* 时间选择器 */
.timeframe-selector {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
}

.timeframe-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.timeframe-btn {
  padding: 12px 8px;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  text-align: center;
  background: var(--el-bg-color);
  transition: all 0.2s;
  color: var(--el-text-color-regular);
}

.timeframe-btn:hover {
  border-color: var(--el-border-color-hover);
  background: var(--el-fill-color-light);
}

.timeframe-btn.active {
  background: var(--el-fill-color);
  border-color: var(--el-border-color-darker);
  color: var(--el-text-color-primary);
}

.timeframe-label {
  font-size: 12px;
  font-weight: 500;
  color: inherit;
  margin-bottom: 4px;
}

.timeframe-change {
  font-size: 11px;
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

/* 交易统计行 */
.trading-stats {
  margin-bottom: 16px;
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
}

.trading-stats .stat-label {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.trading-stats .stat-value {
  font-size: 13px;
  color: #333;
  font-weight: 500;
}

.trading-stats .stat-value.buy-color {
  color: #10b981;
}

.trading-stats .stat-value.sell-color {
  color: #ef4444;
}

.trading-stats .stat-value.positive {
  color: #10b981;
}

.trading-stats .stat-value.negative {
  color: #ef4444;
}

.trading-stats .stat-value.neutral {
  color: #666;
}

/* 安全信息行 */
.safety-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.safety-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.safety-item i.verified {
  color: #10b981;
}

.safety-item i.unverified {
  color: #6b7280;
}

.trading-data-card {
  margin-bottom: 16px;
}

.token-stats, .query-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-row, .stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-row:last-child, .stat-item:last-child {
  border-bottom: none;
}

.stat-label, .label {
  color: #666;
  font-size: 14px;
}

.stat-value, .value {
  font-weight: bold;
  color: #333;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.primary-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.secondary-actions {
  display: flex;
  justify-content: center;
}

.action-buttons .el-button {
  margin: 0;
}

/* 社媒链接样式 */
.social-links {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-light);
}

.social-links-title {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  font-weight: 500;
}

.social-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.social-buttons .el-button {
  margin: 0;
  width: 28px;
  height: 28px;
  padding: 0;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  padding: 8px;
}

.no-links-text {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
  font-style: italic;
}

/* 自定义社媒图标样式 */
.social-buttons .el-button i {
  font-size: 14px;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
}

@media (max-width: 768px) {
  .main-content .el-col {
    margin-bottom: 20px;
  }
  
  .search-container {
    gap: 15px;
  }
  
  .search-input {
    max-width: 100%;
  }
  
  .chart-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}
</style>
