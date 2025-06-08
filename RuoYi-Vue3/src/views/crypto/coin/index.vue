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
                <!-- 主流币加载骨架屏 -->
                <template v-if="loading.mainCoins">
                  <div 
                    v-for="i in 4" 
                    :key="`skeleton-${i}`"
                    class="coin-item-compact skeleton-coin"
                  >
                    <el-skeleton-item variant="circle" style="width: 16px; height: 16px;" />
                    <div class="coin-info">
                      <el-skeleton-item variant="text" style="width: 60px; height: 12px;" />
                      <el-skeleton-item variant="text" style="width: 40px; height: 10px;" />
                    </div>
                  </div>
                </template>
                
                <!-- 实际主流币数据 -->
                <template v-else>
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
                </template>
              </div>
            </div>

            <!-- 代币信息区域骨架屏 -->
            <div v-if="loading.tokenData" class="token-header-skeleton">
              <div class="token-skeleton-row">
                <el-skeleton-item variant="circle" style="width: 50px; height: 50px;" />
                <div class="token-skeleton-text">
                  <el-skeleton-item variant="h3" style="width: 200px;" />
                  <div class="price-skeleton-row">
                    <el-skeleton-item variant="text" style="width: 100px; height: 24px;" />
                    <el-skeleton-item variant="button" style="width: 60px; height: 24px;" />
                  </div>
                </div>
                <div class="action-skeleton">
                  <el-skeleton-item variant="button" style="width: 80px; height: 34px;" />
                  <el-skeleton-item variant="button" style="width: 80px; height: 34px;" />
                </div>
                <div class="mini-stats-skeleton">
                  <div v-for="i in 3" :key="i" class="mini-skeleton">
                    <el-skeleton-item variant="text" style="width: 30px; height: 14px;" />
                    <el-skeleton-item variant="text" style="width: 40px; height: 10px;" />
                  </div>
                </div>
              </div>
            </div>

            <!-- 代币信息区域 - 仅在有数据时显示 -->
            <div v-else-if="tokenData" class="token-header-info">
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
            
            <!-- K线图骨架屏 -->
            <div v-if="loading.tokenData || loading.chartLoading" class="chart-skeleton">
              <el-skeleton-item variant="image" style="width: 100%; height: 400px;" />
            </div>
            
            <!-- K线图 - 仅在有数据时显示 -->
            <div v-else-if="tokenData" class="chart-container">
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

      <!-- 右侧信息区域骨架屏 -->
      <el-col v-if="loading.tokenData" :span="8">
        <!-- 基础数据骨架屏 -->
        <div class="data-section">
          <div class="data-cards-row">
            <div v-for="i in 4" :key="i" class="data-card-skeleton">
              <el-skeleton-item variant="text" style="width: 40px; height: 10px;" />
              <el-skeleton-item variant="text" style="width: 60px; height: 14px;" />
            </div>
          </div>
        </div>

        <!-- 安全分析骨架屏 -->
        <div class="data-section">
          <h4 class="section-title">🔒 安全分析</h4>
          <div class="security-skeleton">
            <!-- 风险等级行骨架 -->
            <div class="risk-skeleton-row">
              <el-skeleton-item variant="button" style="width: 80px; height: 32px;" />
              <el-skeleton-item variant="text" style="flex: 1; height: 32px;" />
            </div>
            
            <!-- 安全指标行骨架 -->
            <div class="security-skeleton-row">
              <div v-for="i in 4" :key="i" class="security-card-skeleton">
                <el-skeleton-item variant="text" style="width: 30px; height: 10px;" />
                <el-skeleton-item variant="text" style="width: 40px; height: 12px;" />
              </div>
            </div>
            
            <!-- 权限状态行骨架 -->
            <div class="permissions-skeleton-row">
              <div v-for="i in 4" :key="i" class="permission-card-skeleton">
                <el-skeleton-item variant="text" style="width: 50px; height: 12px;" />
              </div>
            </div>
          </div>
        </div>

        <!-- 时间周期选择器骨架屏 -->
        <div class="data-section">
          <div class="timeframe-skeleton-row">
            <div v-for="i in 4" :key="i" class="timeframe-card-skeleton">
              <el-skeleton-item variant="text" style="width: 20px; height: 12px;" />
              <el-skeleton-item variant="text" style="width: 30px; height: 10px;" />
            </div>
          </div>
        </div>

        <!-- 交易统计骨架屏 -->
        <div class="data-section">
          <div class="trading-skeleton-row">
            <div v-for="i in 4" :key="i" class="trading-card-skeleton">
              <el-skeleton-item variant="text" style="width: 30px; height: 10px;" />
              <el-skeleton-item variant="text" style="width: 50px; height: 12px;" />
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧信息区域 - 仅在有数据时显示 -->
      <el-col v-else-if="tokenData" :span="8">
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
          
          <!-- 安全数据加载骨架屏 -->
          <div v-if="loading.securityData" class="security-skeleton">
            <!-- 风险等级行骨架 -->
            <div class="risk-skeleton-row">
              <el-skeleton-item variant="button" style="width: 80px; height: 32px;" />
              <el-skeleton-item variant="text" style="flex: 1; height: 32px;" />
            </div>
            
            <!-- 安全指标行骨架 -->
            <div class="security-skeleton-row">
              <div v-for="i in 4" :key="i" class="security-card-skeleton">
                <el-skeleton-item variant="text" style="width: 30px; height: 10px;" />
                <el-skeleton-item variant="text" style="width: 40px; height: 12px;" />
              </div>
            </div>
            
            <!-- 权限状态行骨架 -->
            <div class="permissions-skeleton-row">
              <div v-for="i in 4" :key="i" class="permission-card-skeleton">
                <el-skeleton-item variant="text" style="width: 50px; height: 12px;" />
              </div>
            </div>
          </div>
          
          <!-- 实际安全数据 -->
          <div v-else-if="securityData">
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
          
          <!-- 无安全数据时的紧凑提示 -->
          <div v-else-if="!loading.securityData" class="no-security-data">
            <div class="no-data-text">暂无安全数据</div>
          </div>
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

    <!-- 监控配置弹窗 -->
    <el-dialog v-model="monitorDialogVisible" title="设置代币监控" width="600px">
      <el-form :model="monitorForm" :rules="monitorRules" ref="monitorFormRef" label-width="120px">
        <!-- 基础信息 -->
        <el-form-item label="代币地址" prop="contractAddress">
          <el-input 
            v-model="monitorForm.contractAddress" 
            placeholder="请输入代币合约地址"
            clearable
          >
            <template #suffix>
              <el-button 
                @click="useCurrentToken"
                size="small"
                text
                type="primary"
                :disabled="!tokenData"
              >
                使用当前
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <!-- 提醒模式选择 -->
        <el-form-item label="提醒模式" prop="alertMode">
          <el-radio-group v-model="monitorForm.alertMode">
            <el-radio value="timer">定时提醒</el-radio>
            <el-radio value="condition">条件触发</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 定时提醒配置 -->
        <div v-if="monitorForm.alertMode === 'timer'">
          <el-form-item label="提醒间隔" prop="timerInterval">
            <el-select v-model="monitorForm.timerInterval" placeholder="选择提醒间隔">
              <el-option label="每5分钟" value="5"></el-option>
              <el-option label="每10分钟" value="10"></el-option>
              <el-option label="每15分钟" value="15"></el-option>
              <el-option label="每30分钟" value="30"></el-option>
              <el-option label="每1小时" value="60"></el-option>
              <el-option label="每2小时" value="120"></el-option>
              <el-option label="每6小时" value="360"></el-option>
            </el-select>
          </el-form-item>
        </div>

        <!-- 条件触发配置 -->
        <div v-if="monitorForm.alertMode === 'condition'">
          <el-form-item label="触发条件" prop="conditionType">
            <el-select v-model="monitorForm.conditionType" placeholder="选择触发条件">
              <el-option label="价格高于" value="priceAbove"></el-option>
              <el-option label="价格低于" value="priceBelow"></el-option>
              <el-option label="市值低于" value="marketCapBelow"></el-option>
              <el-option label="涨跌幅超过" value="changeExceeds"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item 
            :label="getConditionLabel()" 
            prop="conditionValue"
            v-if="monitorForm.conditionType"
          >
            <el-input 
              v-model="monitorForm.conditionValue" 
              :placeholder="getConditionPlaceholder()"
              type="number"
              step="any"
            >
              <template #suffix>
                <span class="input-suffix">{{ getConditionSuffix() }}</span>
              </template>
            </el-input>
          </el-form-item>
        </div>

        <!-- 提醒方式 -->
        <el-form-item label="提醒方式" prop="notifyMethods">
          <el-checkbox-group v-model="monitorForm.notifyMethods">
            <el-checkbox value="wechat">微信</el-checkbox>
            <el-checkbox value="telegram">Telegram</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 微信配置 -->
        <el-form-item 
          v-if="monitorForm.notifyMethods.includes('wechat')"
          label="微信名称" 
          prop="wechatName"
        >
          <el-input 
            v-model="monitorForm.wechatName" 
            placeholder="请输入微信名称或备注"
            clearable
          />
        </el-form-item>

        <!-- Telegram配置 -->
        <el-form-item 
          v-if="monitorForm.notifyMethods.includes('telegram')"
          label="Telegram名称" 
          prop="telegramName"
        >
          <el-input 
            v-model="monitorForm.telegramName" 
            placeholder="请输入Telegram用户名"
            clearable
          >
            <template #prefix>@</template>
          </el-input>
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注" prop="remark">
          <el-input 
            v-model="monitorForm.remark" 
            type="textarea" 
            :rows="2"
            placeholder="选填：为这个监控添加备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="monitorDialogVisible = false">取 消</el-button>
          <el-button 
            type="primary" 
            @click="submitMonitorConfig"
            :loading="monitorSubmitting"
          >
            确认监控
          </el-button>
        </div>
      </template>
    </el-dialog>
    </div>
  </template>

<script setup name="CryptoScanner">
import { ref, reactive, getCurrentInstance, onMounted, onUnmounted, watch } from 'vue'
import { Search, Link, DocumentCopy, ArrowDown, Delete } from '@element-plus/icons-vue'
import { tokenInfo, securityInfo, getTopCoin, saveCryptoMonitorConfig, checkTokenMonitored} from "@/api/crypto/index"
const { proxy } = getCurrentInstance()
let securityTimer = null

// 响应式数据定义
const searchCA = ref('')
const tokenData = ref(null)
const monitoring = ref(false)
const settingAlert = ref(false)
const klineIframe = ref(null)
const securityData = ref(null)

// 监控弹窗相关状态
const monitorDialogVisible = ref(false)
const monitorFormRef = ref()
const monitorSubmitting = ref(false)

// 监控表单数据
const monitorForm = reactive({
  contractAddress: '',
  alertMode: 'timer', // timer 或 condition
  timerInterval: '',
  conditionType: '',
  conditionValue: '',
  notifyMethods: [],
  wechatName: '',
  telegramName: '',
  remark: ''
})

// 监控表单验证规则
const monitorRules = reactive({
  contractAddress: [
    { required: true, message: '请输入代币合约地址', trigger: 'blur' }
  ],
  alertMode: [
    { required: true, message: '请选择提醒模式', trigger: 'change' }
  ],
  timerInterval: [
    { required: true, message: '请选择提醒间隔', trigger: 'change', validator: validateTimerInterval }
  ],
  conditionType: [
    { required: true, message: '请选择触发条件', trigger: 'change', validator: validateConditionType }
  ],
  conditionValue: [
    { required: true, message: '请输入条件值', trigger: 'blur', validator: validateConditionValue }
  ],
  notifyMethods: [
    { required: true, message: '请选择至少一种提醒方式', trigger: 'change' }
  ],
  wechatName: [
    { required: true, message: '请输入微信名称', trigger: 'blur', validator: validateWechatName }
  ],
  telegramName: [
    { required: true, message: '请输入Telegram名称', trigger: 'blur', validator: validateTelegramName }
  ]
})

// 自定义验证方法
function validateTimerInterval(rule, value, callback) {
  if (monitorForm.alertMode === 'timer' && !value) {
    callback(new Error('请选择提醒间隔'))
  } else {
    callback()
  }
}

function validateConditionType(rule, value, callback) {
  if (monitorForm.alertMode === 'condition' && !value) {
    callback(new Error('请选择触发条件'))
  } else {
    callback()
  }
}

function validateConditionValue(rule, value, callback) {
  if (monitorForm.alertMode === 'condition' && !value) {
    callback(new Error('请输入条件值'))
  } else if (monitorForm.alertMode === 'condition' && value && isNaN(value)) {
    callback(new Error('条件值必须是数字'))
  } else if (monitorForm.alertMode === 'condition' && value && parseFloat(value) <= 0) {
    callback(new Error('条件值必须大于0'))
  } else {
    callback()
  }
}

function validateWechatName(rule, value, callback) {
  if (monitorForm.notifyMethods.includes('wechat') && !value) {
    callback(new Error('请输入微信名称'))
  } else {
    callback()
  }
}

function validateTelegramName(rule, value, callback) {
  if (monitorForm.notifyMethods.includes('telegram') && !value) {
    callback(new Error('请输入Telegram名称'))
  } else {
    callback()
  }
}

// 骨架屏加载状态
const loading = reactive({
  tokenData: false,       // 主要代币数据加载状态
  securityData: false,    // 安全数据加载状态
  mainCoins: true,        // 主流币价格加载状态
  chartLoading: false     // K线图加载状态
})

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

// 监控弹窗相关方法
const toggleMonitor = () => {
  if (monitorStatus.value === 'not_monitored') {
    openMonitorDialog()
  } else {
    // 实现取消监控的逻辑
    proxy.$modal.info('取消监控功能开发中...')
  }
}

const openMonitorDialog = async () => {
  // 重置表单
  resetMonitorForm()
  
  // 如果当前有代币数据，自动填入地址并检查是否已被监控
  if (tokenData.value && tokenData.value.address) {
    monitorForm.contractAddress = tokenData.value.address
    
    // 检查是否已被监控
    try {
      const response = await checkTokenMonitored(tokenData.value.address)
      if (response && response.code === 200 && response.data && response.data.monitored) {
        proxy.$modal.confirm(
          `代币 ${tokenData.value.symbol} 已存在监控配置，是否要新增另一个监控规则？`,
          '监控提示',
          {
            confirmButtonText: '新增监控',
            cancelButtonText: '取消',
            type: 'warning'
          }
        ).then(() => {
          monitorDialogVisible.value = true
        }).catch(() => {
          // 用户取消，不做任何操作
        })
        return
      }
    } catch (error) {
      // 检查失败，继续正常流程
      console.warn('检查监控状态失败:', error)
    }
  }
  
  monitorDialogVisible.value = true
}

const resetMonitorForm = () => {
  Object.assign(monitorForm, {
    contractAddress: '',
    alertMode: 'timer',
    timerInterval: '',
    conditionType: '',
    conditionValue: '',
    notifyMethods: [],
    wechatName: '',
    telegramName: '',
    remark: ''
  })
  
  // 清除表单验证状态
  if (monitorFormRef.value) {
    monitorFormRef.value.clearValidate()
  }
}

const useCurrentToken = () => {
  if (tokenData.value && tokenData.value.address) {
    monitorForm.contractAddress = tokenData.value.address
  }
}

const getConditionLabel = () => {
  const labels = {
    priceAbove: '目标价格',
    priceBelow: '目标价格',
    marketCapBelow: '市值阈值',
    changeExceeds: '变动幅度'
  }
  return labels[monitorForm.conditionType] || '条件值'
}

const getConditionPlaceholder = () => {
  const placeholders = {
    priceAbove: '请输入价格，触发时机：当前价格 > 目标价格',
    priceBelow: '请输入价格，触发时机：当前价格 < 目标价格',
    marketCapBelow: '请输入市值（美元），触发时机：当前市值 < 阈值',
    changeExceeds: '请输入百分比（如：10），触发时机：|涨跌幅| > 变动幅度'
  }
  return placeholders[monitorForm.conditionType] || '请输入数值'
}

const getConditionSuffix = () => {
  const suffixes = {
    priceAbove: 'USD',
    priceBelow: 'USD',
    marketCapBelow: 'USD',
    changeExceeds: '%'
  }
  return suffixes[monitorForm.conditionType] || ''
}

const submitMonitorConfig = async () => {
  try {
    // 表单验证
    await monitorFormRef.value.validate()
    
    monitorSubmitting.value = true
    
    // 构建提交数据
    const submitData = {
      contractAddress: monitorForm.contractAddress,
      alertMode: monitorForm.alertMode,
      timerInterval: monitorForm.alertMode === 'timer' ? parseInt(monitorForm.timerInterval) : null,
      conditionType: monitorForm.alertMode === 'condition' ? monitorForm.conditionType : null,
      conditionValue: monitorForm.alertMode === 'condition' ? parseFloat(monitorForm.conditionValue) : null,
      notifyMethods: monitorForm.notifyMethods.join(','),
      wechatName: monitorForm.notifyMethods.includes('wechat') ? monitorForm.wechatName : '',
      telegramName: monitorForm.notifyMethods.includes('telegram') ? monitorForm.telegramName : '',
      remark: monitorForm.remark,
      tokenSymbol: tokenData.value?.symbol || '',
      tokenName: tokenData.value?.name || '',
      createTime: new Date(),
      status: '1' // 启用状态
    }
    
    // 调用真实的API保存到crypto_monitor_config表
    const response = await saveCryptoMonitorConfig(submitData)
    
    if (response && response.code === 200) {
      monitorStatus.value = 'monitored'
      monitorDialogVisible.value = false
      proxy.$modal.msgSuccess('监控配置已保存！')
    } else {
      proxy.$modal.msgError('保存失败：' + (response.msg || '未知错误'))
    }
    
  } catch (error) {
    if (error.message !== 'validation failed') {
      proxy.$modal.msgError('保存失败：' + (error.message || '未知错误'))
    }
  } finally {
    monitorSubmitting.value = false
  }
}

// 其他原有方法保持不变
const getMonitorButtonType = () => {
  return monitorStatus.value === 'monitored' ? 'info' : 'primary'
}

const getMonitorButtonText = () => {
  return monitorStatus.value === 'monitored' ? '已监控' : '监控'
}

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
  loading.tokenData = true
  loading.securityData = true
  
  // 调用API获取数据
  tokenInfo(searchCA.value).then(response => {
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
        holderCount: tokenPair.holderCount,
        liquidity: tokenPair.liquidity || 0,
        hasRenounced: false,
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
      
      // 主要数据加载完成
      loading.tokenData = false
      
      // 自动获取安全数据
      getTokenSecurity(tokenData.value.address, tokenPair)
      
      // 保存到搜索历史
      saveToHistory(tokenData.value)
    } else {
      loading.tokenData = false
      loading.securityData = false
      proxy.$modal.msgError('未找到该代币信息，请检查CA地址是否正确')
    }
  }).catch(error => {
    loading.tokenData = false
    loading.securityData = false
    proxy.$modal.msgError('查询失败: ' + (error.message || '网络错误'))
    
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

  getTokenInfo()
}

// 其他工具函数和方法...
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
  return parseFloat(Number(n).toFixed(2)).toString()
}

const openInExplorer = () => {
  if (tokenData.value) {
    window.open(`https://solscan.io/token/${tokenData.value.address}`, '_blank')
  }
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

// 页面初始化
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
      }
    ]
    localStorage.setItem('crypto_search_history', JSON.stringify(searchHistory.value))
  }
  
  // 启动主流币价格更新
  startPriceUpdates()
  updateMainCoinPrices()
})

onUnmounted(() => {
  stopPriceUpdates()
})

// 其他必要的函数（简化版本）...
const calculateHigh24h = (tokenPair) => {
  const currentPrice = parseFloat(tokenPair.priceUsd) || 0
  const change24h = tokenPair.priceChange?.h24 || 0
  return change24h >= 0 ? currentPrice : currentPrice / (1 + change24h / 100)
}

const calculateLow24h = (tokenPair) => {
  const currentPrice = parseFloat(tokenPair.priceUsd) || 0
  const change24h = tokenPair.priceChange?.h24 || 0
  return change24h <= 0 ? currentPrice : currentPrice / (1 + change24h / 100)
}

const getChainLogo = (chainId) => {
  const chainLogos = {
    'sol': '/src/assets/crypto-icons/SOL.png',
    'solana': '/src/assets/crypto-icons/SOL.png',
    'ethereum': '/src/assets/crypto-icons/ETH.png',
    'bsc': '/src/assets/crypto-icons/BNB.png',
    'base': '/src/assets/crypto-icons/BASE.png'
  }
  return chainLogos[chainId] || chainLogos['sol']
}

const processRealtimeData = (tokenPair) => {
  const realtimeData = tokenPair.realtimeData;
  const txns = realtimeData?.txns || generateMockTxnsFromPriceChange(realtimeData?.txns)
  const priceChange = realtimeData?.priceChange || {}
  const volume = realtimeData?.volume || {}

  return { txns, priceChange, volume }
}

const generateMockTxnsFromPriceChange = (priceChange) => {
  if (!priceChange) return null

  const baseTxns = {
    m5: { buys: 0, sells: 0 },
    h1: { buys: 0, sells: 0 },
    h6: { buys: 0, sells: 0 },
    h24: { buys: 0, sells: 0 }
  }

  Object.keys(priceChange).forEach(timeframe => {
    const change = priceChange[timeframe]
    if (change !== null && change !== undefined) {
      const activity = Math.abs(change) * 10
      const buys = Math.floor(activity * (change > 0 ? 1.2 : 0.8))
      const sells = Math.floor(activity * (change > 0 ? 0.8 : 1.2))

      if (baseTxns[timeframe]) {
        baseTxns[timeframe] = { buys, sells }
      }
    }
  })

  return baseTxns
}

const extractSocialLinks = (info) => {
  return []  // 简化版本，可以后续扩展
}

const openSocialLink = (url) => {
  window.open(url, '_blank')
}

const getTokenSecurity = async (address, tokenPair) => {
  if (!address) {
    loading.securityData = false
    return
  }
  
  try {
    const response = await securityInfo(address)
    if (response && response.code === 200) {
      const data = response.data
      
      const extractValue = (value) => {
        if (Array.isArray(value)) {
          return value.length > 0 ? value[0] : null
        }
        return value
      }
      
      const toBool = (value) => {
        const extracted = extractValue(value)
        return extracted === "1" || extracted === true
      }
      
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
      
      loading.securityData = false
    } else {
      loadDemoSecurityData()
      loading.securityData = false
      proxy.$modal.msgWarning('获取安全数据失败，使用演示数据')
    }
  } catch (error) {
    loadDemoSecurityData()
    loading.securityData = false
    proxy.$modal.msgWarning('网络异常，使用演示数据')
  }
}

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

const getRiskLevelText = (level) => {
  const texts = {
    'LOW': '低风险',
    'MEDIUM': '中风险',
    'HIGH': '高风险'
  }
  return texts[level] || '未知'
}

const getConcentrationRiskClass = (top10Percent) => {
  if (top10Percent < 0.10) return 'safe'
  if (top10Percent < 0.20) return 'warning'
  return 'danger'
}

const getFeeRiskClass = (feeRate) => {
  if (feeRate < 0.05) return 'success'
  if (feeRate < 0.10) return 'warning'
  return 'danger'
}

const formatPercent = (value) => {
  if (value === null || value === undefined) return '--'
  return (value * 100).toFixed(2) + '%'
}

const copyAddress = (address) => {
  navigator.clipboard.writeText(address).then(() => {
    proxy.$modal.msgSuccess('开发者地址已复制到剪贴板')
  }).catch(err => {
    proxy.$modal.msgError('复制地址失败: ' + err.message)
  })
}

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
    
    const priceResults = await Promise.all(pricePromises)
    
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
    
    loading.mainCoins = false
    
  } catch (error) {
    loading.mainCoins = false
  }
}

const startPriceUpdates = () => {
  updateMainCoinPrices()
  priceUpdateTimer = setInterval(updateMainCoinPrices, 10000)
}

const stopPriceUpdates = () => {
  if (priceUpdateTimer) {
    clearInterval(priceUpdateTimer)
    priceUpdateTimer = null
  }
}

// 交易相关方法
const formatChange = (change) => {
  if (change === null || change === undefined) return '--'
  const sign = change >= 0 ? '+' : ''
  return `${sign}${change.toFixed(2)}%`
}

const getChangeClass = (change) => {
  if (change === null || change === undefined) return 'neutral'
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return 'neutral'
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

const getSelectedBuyVolume = () => {
  if (tokenData.value?.realtimeData) {
    const volume = getSelectedVolume()
    return volume ? volume * 0.6 : 0
  }
  return 0
}

const getSelectedSellVolume = () => {
  if (tokenData.value?.realtimeData) {
    const volume = getSelectedVolume()
    return volume ? volume * 0.4 : 0
  }
  return 0
}

const getNetBuyClass = () => {
  if (tokenData.value?.realtimeData) {
    const buyVolume = getSelectedBuyVolume()
    const sellVolume = getSelectedSellVolume()
    const netVolume = buyVolume - sellVolume
    
    if (netVolume > 0) return 'positive'
    if (netVolume < 0) return 'negative'
  }
  return 'neutral'
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
  color: var(--el-color-success-dark-2);
}

.risk-card.medium {
  color: var(--el-color-warning-dark-2);
}

.risk-card.high {
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

/* 安全等级颜色 - 更直观的语义化配色 */
.security-card.safe {
  border: 1px solid var(--el-color-success-dark-1);
}

.security-card.warning {
  border: 1px solid var(--el-color-warning-dark-1);
}

.security-card.danger {
  border: 1px solid var(--el-color-danger-dark-1);
}

.security-card.neutral {
  background: linear-gradient(135deg, var(--el-fill-color-light), var(--el-fill-color));
  border: 1px solid var(--el-border-color-light);
}

/* 安全等级文本颜色 */
.security-card.safe .security-value {
  color: var(--el-color-success-dark-2);
  font-weight: 700;
}

.security-card.warning .security-value {
  color: var(--el-color-warning-dark-2);
  font-weight: 700;
}

.security-card.danger .security-value {
  color: var(--el-color-danger-dark-2);
  font-weight: 700;
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

/* 权限状态行 - 安全语义化配色 */
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

/* 权限安全状态：绿色=安全，红色=危险 */
.permission-card.safe {
  border: 1px solid var(--el-color-success-dark-2);
  color: var(--el-color-success-dark-2);
}

.permission-card.danger {
  color: var(--el-color-danger-dark-2);
  border: 1px solid var(--el-color-danger-dark-2);
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

/* 交易统计卡片 - 买入绿色，卖出红色 */
.trading-cards-row {
  display: flex;
  gap: 8px;
}

.trading-card {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-bg-color);
  box-shadow: 0 2px 4px var(--el-box-shadow-light);
  transition: all 0.3s ease;
  text-align: center;
  border: 1px solid var(--el-border-color-light);
}

.trading-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px var(--el-box-shadow);
}

.trading-value {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 4px;
}

/* 交易数值颜色 */
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

/* K线图骨架屏 */
.chart-skeleton {
  height: 400px;
  background: var(--el-fill-color-blank);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: inset 0 2px 4px var(--el-box-shadow-light);
}

/* 骨架屏样式 */
.skeleton-coin {
  opacity: 0.8;
}

.token-header-skeleton {
  padding: 16px 0;
  border-bottom: 1px solid var(--el-border-color-light);
}

.token-skeleton-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.token-skeleton-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.price-skeleton-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.action-skeleton {
  display: flex;
  gap: 8px;
}

.mini-stats-skeleton {
  display: flex;
  gap: 8px;
}

.mini-skeleton {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
}

/* 右侧骨架屏 */
.data-card-skeleton {
  flex: 1;
  padding: 12px 8px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  text-align: center;
}

.security-skeleton {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.risk-skeleton-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.security-skeleton-row {
  display: flex;
  gap: 8px;
}

.security-card-skeleton {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.permissions-skeleton-row {
  display: flex;
  gap: 8px;
}

.permission-card-skeleton {
  flex: 1;
  padding: 18px 10px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.timeframe-skeleton-row {
  display: flex;
  gap: 8px;
}

.timeframe-card-skeleton {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.trading-skeleton-row {
  display: flex;
  gap: 8px;
}

.trading-card-skeleton {
  flex: 1;
  padding: 10px 8px;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  text-align: center;
}

.no-security-data {
  padding: 20px;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.no-data-text {
  color: var(--el-text-color-secondary);
}

/* 卖出交易 - 红色主题 */
.trading-card.sell {
  background: linear-gradient(135deg, var(--el-color-danger-light-9), var(--el-color-danger-light-8));
  border: 1px solid var(--el-color-danger-light-6);
}

.trading-value {
  font-size: 12px;
  font-weight: 700;
  margin-bottom: 4px;
}

/* 交易数值颜色 */
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

/* 监控弹窗样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.input-suffix {
  color: var(--el-text-color-placeholder);
  font-size: 12px;
  padding-right: 8px;
}

/* 监控弹窗表单样式优化 */
.el-dialog {
  border-radius: 12px;
}

.el-dialog__header {
  border-bottom: 1px solid var(--el-border-color-light);
  padding: 20px 24px 16px;
}

.el-dialog__body {
  padding: 24px;
}

.el-dialog__footer {
  border-top: 1px solid var(--el-border-color-light);
  padding: 16px 24px 20px;
}

/* 表单项样式优化 */
.el-form-item {
  margin-bottom: 20px;
}

.el-form-item__label {
  font-weight: 500;
  color: var(--el-text-color-primary) !important;
}

/* 单选按钮组样式 */
.el-radio-group {
  display: flex;
  gap: 16px;
}

.el-radio {
  margin-right: 0 !important;
}

.el-radio__label {
  padding-left: 8px;
}

/* 复选框组样式 */
.el-checkbox-group {
  display: flex;
  gap: 16px;
}

.el-checkbox {
  margin-right: 0 !important;
}

.el-checkbox__label {
  padding-left: 8px;
}

/* 条件配置区域样式 */
.el-form-item:has(.el-select) .el-input {
  border-radius: 6px;
}

.el-form-item:has(.el-input[type="number"]) .el-input {
  border-radius: 6px;
}

/* 输入框焦点状态优化 */
.el-input__wrapper:focus-within {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

.el-select:focus-within .el-input__wrapper {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

/* 按钮样式优化 */
.dialog-footer .el-button {
  min-width: 80px;
  border-radius: 8px;
  font-weight: 500;
}

.dialog-footer .el-button--primary {
  background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-dark-2));
  border: none;
  box-shadow: 0 4px 12px var(--el-color-primary-light-5);
}

.dialog-footer .el-button--primary:hover {
  background: linear-gradient(135deg, var(--el-color-primary-light-3), var(--el-color-primary));
  transform: translateY(-1px);
  box-shadow: 0 6px 16px var(--el-color-primary-light-4);
}

/* 加载状态优化 */
.el-button.is-loading {
  opacity: 0.8;
}

/* 表单验证错误样式优化 */
.el-form-item.is-error .el-input__wrapper {
  border-color: var(--el-color-danger);
  box-shadow: 0 0 0 1px var(--el-color-danger-light-7) inset;
}

.el-form-item__error {
  color: var(--el-color-danger);
  font-size: 12px;
  margin-top: 4px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .el-dialog {
    width: 95% !important;
    margin: 5vh auto 50px;
  }
  
  .el-dialog__body {
    padding: 16px;
  }
  
  .el-form-item {
    margin-bottom: 16px;
  }
  
  .el-radio-group,
  .el-checkbox-group {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
