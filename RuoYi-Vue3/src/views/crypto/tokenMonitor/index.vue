<template>
  <div class="app-container">
    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="数据来源" prop="source">
        <el-select v-model="queryParams.source" placeholder="请选择" clearable style="width: 120px">
          <el-option label="全部" value="all" />
          <el-option label="Pump" value="pump" />
          <el-option label="BONK" value="bonk" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="时间范围" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="-"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 360px"
        />
      </el-form-item>
      
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="名称/符号/合约地址"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      
      <el-form-item label="监控状态" prop="monitorStatus">
        <el-select v-model="queryParams.monitorStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="已监控" value="monitored" />
          <el-option label="未监控" value="unmonitored" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="Twitter" prop="hasTwitter">
        <el-select v-model="queryParams.hasTwitter" placeholder="全部" clearable style="width: 130px">
          <el-option label="全部" value="" />
          <el-option label="推特主页" value="profile" />
          <el-option label="推文" value="tweet" />
          <el-option label="社区" value="community" />
          <el-option label="无推特" value="none" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="市值筛选" prop="minMarketCap">
        <el-select v-model="queryParams.minMarketCap" placeholder="全部" clearable style="width: 150px">
          <el-option label="全部" value="" />
          <el-option label="≥ 30万 USD" :value="300000" />
          <el-option label="≥ 50万 USD" :value="500000" />
          <el-option label="≥ 100万 USD" :value="1000000" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="关注状态" prop="isFollowing">
        <el-select v-model="queryParams.isFollowing" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="已关注" value="1" />
          <el-option label="未关注" value="0" />
        </el-select>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button 
          type="primary" 
          plain 
          icon="Plus" 
          @click="handleBatchFollow"
          :disabled="!hasTwitterSelected"
        >
          批量关注
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger" 
          plain 
          icon="Remove" 
          @click="handleBatchUnfollow"
          :disabled="!hasTwitterSelected"
        >
          批量取消关注
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="primary" 
          plain 
          icon="Monitor" 
          @click="handleBatchMonitor"
          :disabled="multiple"
        >
          批量监控
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button 
          type="danger" 
          plain 
          icon="RemoveFilled" 
          @click="handleBatchCancelMonitor"
          :disabled="multiple"
        >
          批量取消监控
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="Refresh" @click="refreshData">刷新数据</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="tokenList" 
      @selection-change="handleSelectionChange"
      ref="tokenTable"
    >
      <el-table-column type="selection" width="50" align="center" />
      
      <!-- Token信息 -->
      <el-table-column label="Token信息" align="left" min-width="220" v-if="columns[0].visible">
        <template #default="scope">
          <div class="token-info-cell">
            <div class="token-main-info">
              <span class="token-symbol">{{ scope.row.tokenSymbol || 'Unknown' }}</span>
              <span class="token-name">{{ scope.row.tokenName || '-' }}</span>
              <el-tag 
                v-if="scope.row.source === 'pump'" 
                type="primary" 
                size="small"
                class="source-tag"
              >
                Pump
              </el-tag>
              <el-tag 
                v-else-if="scope.row.source === 'bonk'" 
                type="success" 
                size="small"
                class="source-tag"
              >
                BONK
              </el-tag>
            </div>
            <div class="token-address-row">
              <el-tooltip :content="scope.row.ca" placement="top">
                <el-link 
                  type="primary" 
                  @click="copyText(scope.row.ca)" 
                  :underline="false" 
                  class="address-link"
                >
                  {{ scope.row.ca }}
                  <el-icon><DocumentCopy /></el-icon>
                </el-link>
              </el-tooltip>
            </div>
          </div>
        </template>
      </el-table-column>
      
      <!-- 发射时间 -->
      <el-table-column label="发射时间" align="center" width="150" sortable prop="launchTime" v-if="columns[1].visible">
        <template #default="scope">
          <div class="time-cell">
            <div>{{ parseTime(scope.row.launchTime, '{y}-{m}-{d}') }}</div>
            <div class="time-sub">{{ parseTime(scope.row.launchTime, '{h}:{i}:{s}') }}</div>
          </div>
        </template>
      </el-table-column>


      <!-- 市值 -->
      <el-table-column label="历史最高市值" align="right" width="130" v-if="columns[2].visible">
        <template #default="scope">
          <span class="market-cap">{{ formatMarketCap(scope.row.highestMarketCap) }}</span>
        </template>
      </el-table-column>
      
      <!-- Twitter操作 -->
      <el-table-column label="Twitter" align="center" width="280" v-if="columns[3].visible">
        <template #default="scope">
          <div v-if="scope.row.twitterUrl" class="twitter-actions">
            <!-- Twitter类型标签 -->
            <el-tag 
              :type="getTwitterTypeTag(scope.row.twitterUrl).type" 
              size="small"
              class="twitter-type-tag"
            >
              {{ getTwitterTypeTag(scope.row.twitterUrl).label }}
            </el-tag>
            
            <el-button 
              link
              type="primary"
              @click="openLink(scope.row.twitterUrl)"
              size="small"
              title="查看Twitter"
            >
              <el-icon><Link /></el-icon>
            </el-button>
            
            <!-- 只有推特主页才显示推送配置和关注按钮 -->
            <template v-if="isTwitterProfile(scope.row.twitterUrl)">
              <el-divider direction="vertical" />
              
              <el-tooltip content="Twitter推送配置" placement="top">
                <el-button 
                  link
                  type="success"
                  @click="handleTwitterPush(scope.row)"
                  size="small"
                >
                  <el-icon><BellFilled /></el-icon>
                </el-button>
              </el-tooltip>
              
              <el-divider direction="vertical" />
              
              <el-tooltip :content="scope.row.isFollowing ? '取消关注' : '关注'" placement="top">
                <el-button 
                  link
                  :type="scope.row.isFollowing ? 'warning' : 'primary'"
                  @click="handleToggleFollow(scope.row)"
                  size="small"
                  :loading="scope.row.followLoading"
                >
                  <el-icon><Star v-if="!scope.row.isFollowing" /><StarFilled v-else /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
          </div>
          <span v-else class="text-gray-400">-</span>
        </template>
      </el-table-column>
      
      <!-- 监控状态 -->
      <el-table-column label="监控状态" align="center" width="180" v-if="columns[4].visible">
        <template #default="scope">
          <div class="monitor-status-cell">
            <el-tag 
              v-if="scope.row.monitorConfig && scope.row.monitorConfig.status === '1'" 
              type="success"
              size="small"
            >
              监控中
            </el-tag>
            <el-tag 
              v-else-if="scope.row.monitorConfig && scope.row.monitorConfig.status === '0'" 
              type="info"
              size="small"
            >
              已停用
            </el-tag>
            <el-tag v-else type="info" size="small">未监控</el-tag>
            
            <el-button 
              link
              type="primary"
              @click="handleMonitorConfig(scope.row)"
              size="small"
              style="margin-left: 8px"
            >
              <el-icon><Setting /></el-icon>
            </el-button>
          </div>
        </template>
      </el-table-column>
      
      <!-- 入库时间 -->
      <el-table-column label="入库时间" align="center" width="150" v-if="columns[5].visible">
        <template #default="scope">
          <div class="time-cell">
            <div>{{ parseTime(scope.row.createdAt, '{y}-{m}-{d}') }}</div>
            <div class="time-sub">{{ parseTime(scope.row.createdAt, '{h}:{i}:{s}') }}</div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Twitter推送配置对话框 -->
    <el-dialog 
      v-model="twitterPushDialog.visible" 
      title="Twitter推送配置" 
      width="600px"
      @close="handleTwitterPushClose"
    >
      <el-form :model="twitterPushDialog.form" label-width="120px">
        <el-form-item label="Token信息">
          <div class="dialog-token-info">
            <span class="token-symbol">{{ twitterPushDialog.tokenInfo.symbol }}</span>
            <span class="token-name">{{ twitterPushDialog.tokenInfo.name }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="Twitter账号">
          <el-link :href="twitterPushDialog.tokenInfo.twitterUrl" target="_blank" type="primary">
            {{ twitterPushDialog.tokenInfo.twitterUrl }}
          </el-link>
        </el-form-item>
        
        <el-divider>推送配置</el-divider>
        
        <el-form-item label="关注推送">
          <el-switch v-model="twitterPushDialog.form.followPush" />
          <span class="form-tip">监控该账号的关注动态</span>
        </el-form-item>
        
        <el-form-item label="推文推送">
          <el-switch v-model="twitterPushDialog.form.tweetPush" />
          <span class="form-tip">推送该账号发布的新推文</span>
        </el-form-item>
        
        <el-form-item label="转发推送">
          <el-switch v-model="twitterPushDialog.form.retweetPush" />
          <span class="form-tip">推送该账号的转发动态</span>
        </el-form-item>
        
        <el-form-item label="回复推送">
          <el-switch v-model="twitterPushDialog.form.replyPush" />
          <span class="form-tip">推送该账号的回复内容</span>
        </el-form-item>
        
        <el-form-item label="头像更换推送">
          <el-switch v-model="twitterPushDialog.form.avatarChangePush" />
          <span class="form-tip">监控账号头像更换</span>
        </el-form-item>
        
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="twitterPushDialog.form.notifyMethods">
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="推送状态">
          <el-switch 
            v-model="twitterPushDialog.form.enabled" 
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="twitterPushDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleTwitterPushSave" :loading="twitterPushDialog.loading">
          保存配置
        </el-button>
      </template>
    </el-dialog>

    <!-- 监控配置对话框 -->
    <el-dialog 
      v-model="monitorDialog.visible" 
      title="监控配置" 
      width="700px"
      @close="handleMonitorDialogClose"
    >
      <el-form :model="monitorDialog.form" label-width="120px" ref="monitorFormRef">
        <el-form-item label="Token信息">
          <div class="dialog-token-info">
            <span class="token-symbol">{{ monitorDialog.tokenInfo.symbol }}</span>
            <span class="token-name">{{ monitorDialog.tokenInfo.name }}</span>
          </div>
        </el-form-item>
        
        <el-form-item label="合约地址">
          <el-input v-model="monitorDialog.form.coinAddress" disabled />
        </el-form-item>
        
        <el-form-item label="监控模式" prop="alertMode">
          <el-radio-group v-model="monitorDialog.form.alertMode">
            <el-radio label="timer">定时提醒</el-radio>
            <el-radio label="condition">价格触发</el-radio>
            <el-radio label="event">事件监控</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 定时提醒配置 -->
        <el-form-item 
          v-if="monitorDialog.form.alertMode === 'timer'" 
          label="提醒间隔" 
          prop="timerInterval"
        >
          <el-input-number 
            v-model="monitorDialog.form.timerInterval" 
            :min="1" 
            :max="1440"
            placeholder="分钟"
          />
          <span class="form-tip">分钟（1-1440）</span>
        </el-form-item>
        
        <!-- 价格触发配置 -->
        <template v-if="monitorDialog.form.alertMode === 'condition'">
          <el-form-item label="条件类型" prop="conditionType">
            <el-select v-model="monitorDialog.form.conditionType" placeholder="请选择">
              <el-option label="价格高于" value="priceAbove" />
              <el-option label="价格低于" value="priceBelow" />
              <el-option label="市值低于" value="marketCapBelow" />
              <el-option label="涨跌幅超过" value="changeExceeds" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="阈值" prop="conditionValue">
            <el-input-number 
              v-model="monitorDialog.form.conditionValue" 
              :min="0"
              :precision="8"
              placeholder="请输入阈值"
            />
            <span class="form-tip">
              {{ getConditionValueTip(monitorDialog.form.conditionType) }}
            </span>
          </el-form-item>
        </template>
        
        <!-- 事件监控配置 -->
        <template v-if="monitorDialog.form.alertMode === 'event'">
          <el-form-item label="事件类型" prop="eventType">
            <el-select v-model="monitorDialog.form.eventType" placeholder="请选择">
              <el-option label="大额交易监控" value="largeTransaction" />
              <el-option label="持仓异动监控" value="holdingChange" />
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item label="通知方式" prop="notifyMethods">
          <el-checkbox-group v-model="monitorDialog.notifyMethodsArray">
            <el-checkbox label="telegram">Telegram</el-checkbox>
            <el-checkbox label="wechat">微信</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="监控状态">
          <el-switch 
            v-model="monitorDialog.form.status" 
            active-value="1"
            inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
        
        <el-form-item label="备注">
          <el-input 
            v-model="monitorDialog.form.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="monitorDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleMonitorSave" :loading="monitorDialog.loading">
          保存配置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="TokenMonitor">
import { ref, reactive, computed, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { 
  listToken, 
  followTwitter, 
  unfollowTwitter, 
  batchFollowTwitter, 
  batchUnfollowTwitter,
  getTwitterAccounts,
  getPushConfig,
  updatePushConfig
} from '@/api/crypto/token'
import { 
  DocumentCopy, 
  Link, 
  BellFilled, 
  Star, 
  StarFilled, 
  Setting 
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  source: 'all',
  keyword: null,
  monitorStatus: '',
  hasTwitter: '',
  minMarketCap: '',
  isFollowing: ''
})

// 数据
const showSearch = ref(true)
const tokenList = ref([])
const total = ref(0)
const loading = ref(false)
const dateRange = ref([])
const selectedRows = ref([])
const single = ref(true)
const multiple = ref(true)

// 列配置
const columns = ref([
  { key: 0, label: 'Token信息', visible: true },
  { key: 1, label: '发射时间', visible: true },
  { key: 2, label: '市值', visible: true },
  { key: 3, label: 'Twitter', visible: true },
  { key: 4, label: '监控状态', visible: true },
  { key: 5, label: '入库时间', visible: true }
])

// Twitter推送配置对话框
const twitterPushDialog = reactive({
  visible: false,
  loading: false,
  tokenInfo: {
    ca: '',
    symbol: '',
    name: '',
    twitterUrl: ''
  },
  form: {
    followPush: false,
    tweetPush: false,
    retweetPush: false,
    replyPush: false,
    avatarChangePush: false,
    notifyMethods: [],
    enabled: true
  }
})

// 监控配置对话框
const monitorDialog = reactive({
  visible: false,
  loading: false,
  tokenInfo: {
    ca: '',
    symbol: '',
    name: ''
  },
  form: {
    id: null,
    coinAddress: '',
    tokenName: '',
    alertMode: 'timer',
    timerInterval: 60,
    conditionType: '',
    conditionValue: null,
    eventType: '',
    notifyMethods: '',
    status: '1',
    remark: ''
  },
  notifyMethodsArray: []
})

// 定时刷新
let refreshInterval = null

// 判断是否是推特主页（提前定义，供computed使用）
const isTwitterProfile = (twitterUrl) => {
  if (!twitterUrl) return false
  // 推特主页：不包含 /status/、/communities/ 和 /search
  return !twitterUrl.includes('/status/') && 
         !twitterUrl.includes('/communities/') && 
         !twitterUrl.includes('/search')
}

// 计算是否有选中带推特主页的行
const hasTwitterSelected = computed(() => {
  return selectedRows.value.some(row => row.twitterUrl && isTwitterProfile(row.twitterUrl))
})

// 初始化今天的时间范围
const initTodayDateRange = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59)
  
  dateRange.value = [
    proxy.parseTime(today, '{y}-{m}-{d} {h}:{i}:{s}'),
    proxy.parseTime(endOfDay, '{y}-{m}-{d} {h}:{i}:{s}')
  ]
}

// 查询列表
const getList = () => {
  loading.value = true
  const params = { ...queryParams }
  
  // 添加时间范围
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  
  // 如果source是all，则不传递该参数
  if (params.source === 'all') {
    delete params.source
  }
  
  // 如果hasTwitter为空，则不传递
  if (params.hasTwitter === '') {
    delete params.hasTwitter
  }
  
  // 如果minMarketCap为空，则不传递
  if (params.minMarketCap === '') {
    delete params.minMarketCap
  }
  
  console.log('查询参数:', params) // 调试用
  
  listToken(params).then(response => {
    tokenList.value = response.rows.map(row => ({
      ...row,
      followLoading: false,
      isFollowing: false
    }))
    total.value = response.total
    
    // 加载Twitter账号状态
    loadTwitterAccountStatus()
  }).catch(error => {
    proxy.$modal.msgError('查询失败：' + (error.message || '未知错误'))
  }).finally(() => {
    loading.value = false
  })
}

// 加载Twitter账号状态
const loadTwitterAccountStatus = () => {
  // 提取所有有效的Twitter URL
  const twitterUrls = tokenList.value
    .filter(token => token.twitterUrl && isTwitterProfile(token.twitterUrl))
    .map(token => token.twitterUrl)
  
  if (twitterUrls.length === 0) {
    return
  }
  
  // 批量获取Twitter账号信息
  getTwitterAccounts(twitterUrls).then(response => {
    const accountMap = response.data || {}
    
    // 更新tokenList中的关注状态
    tokenList.value = tokenList.value.map(token => {
      if (token.twitterUrl && accountMap[token.twitterUrl]) {
        const account = accountMap[token.twitterUrl]
        return {
          ...token,
          isFollowing: account.isFollowing === 1
        }
      }
      return token
    })
  }).catch(error => {
    console.error('加载Twitter账号状态失败:', error)
  })
}

// 查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  dateRange.value = []
  proxy.resetForm('queryRef')
  queryParams.source = 'all'
  queryParams.monitorStatus = ''
  queryParams.hasTwitter = ''
  queryParams.minMarketCap = ''
  queryParams.isFollowing = ''
  initTodayDateRange()
  handleQuery()
}

// 刷新数据
const refreshData = () => {
  proxy.$modal.msgSuccess('数据已刷新')
  getList()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 批量关注
const handleBatchFollow = () => {
  // 只筛选推特主页
  const twitterTokens = selectedRows.value.filter(row => 
    row.twitterUrl && isTwitterProfile(row.twitterUrl)
  )
  
  if (twitterTokens.length === 0) {
    proxy.$modal.msgWarning('请选择带有推特主页的Token（推文和社区不支持关注）')
    return
  }
  
  proxy.$modal.confirm(`确认关注选中的 ${twitterTokens.length} 个推特主页？`).then(() => {
    const twitterUrls = twitterTokens.map(token => token.twitterUrl)
    batchFollowTwitter(twitterUrls).then(response => {
      proxy.$modal.msgSuccess(response.msg || '批量关注成功')
      getList()
    }).catch(() => {
      proxy.$modal.msgError('批量关注失败')
    })
  }).catch(() => {})
}

// 批量取消关注
const handleBatchUnfollow = () => {
  // 只筛选推特主页
  const twitterTokens = selectedRows.value.filter(row => 
    row.twitterUrl && isTwitterProfile(row.twitterUrl)
  )
  
  if (twitterTokens.length === 0) {
    proxy.$modal.msgWarning('请选择带有推特主页的Token（推文和社区不支持关注）')
    return
  }
  
  proxy.$modal.confirm(`确认取消关注选中的 ${twitterTokens.length} 个推特主页？`).then(() => {
    const twitterUrls = twitterTokens.map(token => token.twitterUrl)
    batchUnfollowTwitter(twitterUrls).then(response => {
      proxy.$modal.msgSuccess(response.msg || '批量取消关注成功')
      getList()
    }).catch(() => {
      proxy.$modal.msgError('批量取消关注失败')
    })
  }).catch(() => {})
}

// 批量监控
const handleBatchMonitor = () => {
  if (selectedRows.value.length === 0) {
    proxy.$modal.msgWarning('请选择要监控的Token')
    return
  }
  
  proxy.$modal.confirm(`确认对选中的 ${selectedRows.value.length} 个Token启用监控？`).then(() => {
    // TODO: 调用批量启用监控API
    proxy.$modal.msgSuccess('批量监控启用成功')
    getList()
  })
}

// 批量取消监控
const handleBatchCancelMonitor = () => {
  if (selectedRows.value.length === 0) {
    proxy.$modal.msgWarning('请选择要取消监控的Token')
    return
  }
  
  proxy.$modal.confirm(`确认取消选中的 ${selectedRows.value.length} 个Token的监控？`).then(() => {
    // TODO: 调用批量取消监控API
    proxy.$modal.msgSuccess('批量取消监控成功')
    getList()
  })
}

// 格式化地址
const formatAddress = (address) => {
  if (!address) return '-'
  if (address.length <= 14) return address
  return `${address.substring(0, 8)}...${address.substring(address.length - 6)}`
}

// 格式化市值
const formatMarketCap = (value) => {
  if (!value || value === 0) return '-'
  if (value >= 1000000) {
    return `$${(value / 1000000).toFixed(2)}M`
  } else if (value >= 1000) {
    return `$${(value / 1000).toFixed(2)}K`
  }
  return '$' + value.toLocaleString()
}

// 复制文本
const copyText = (text) => {
  if (!navigator.clipboard) {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      proxy.$modal.msgSuccess('已复制')
    } catch (err) {
      proxy.$modal.msgError('复制失败')
    }
    document.body.removeChild(textarea)
    return
  }
  
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess('已复制')
  }).catch(() => {
    proxy.$modal.msgError('复制失败')
  })
}

// 打开链接
const openLink = (url) => {
  window.open(url, '_blank')
}

// 获取Twitter类型标签
const getTwitterTypeTag = (twitterUrl) => {
  if (!twitterUrl) {
    return { label: '-', type: 'info' }
  }
  
  if (twitterUrl.includes('/status/')) {
    return { label: '推文', type: 'warning' }
  } else if (twitterUrl.includes('/communities/')) {
    return { label: '社区', type: 'success' }
  } else {
    return { label: '主页', type: 'primary' }
  }
}

// 切换关注状态
const handleToggleFollow = (row) => {
  row.followLoading = true
  
  const apiCall = row.isFollowing ? unfollowTwitter(row.twitterUrl) : followTwitter(row.twitterUrl)
  
  apiCall.then(response => {
    row.isFollowing = !row.isFollowing
    proxy.$modal.msgSuccess(row.isFollowing ? '关注成功' : '取消关注成功')
  }).catch(error => {
    proxy.$modal.msgError(row.isFollowing ? '取消关注失败' : '关注失败')
  }).finally(() => {
    row.followLoading = false
  })
}

// 打开Twitter推送配置
const handleTwitterPush = (row) => {
  twitterPushDialog.tokenInfo = {
    ca: row.ca,
    symbol: row.tokenSymbol,
    name: row.tokenName,
    twitterUrl: row.twitterUrl
  }
  
  twitterPushDialog.loading = true
  twitterPushDialog.visible = true
  
  // 从后端获取现有配置
  getPushConfig(row.twitterUrl).then(response => {
    const config = response.data || {}
    twitterPushDialog.form = {
      followPush: config.enableFollowPush === 1,
      tweetPush: config.enableTweetPush === 1,
      retweetPush: config.enableRetweetPush === 1,
      replyPush: config.enableReplyPush === 1,
      avatarChangePush: config.enableAvatarPush === 1,
      notifyMethods: config.notifyMethods ? config.notifyMethods.split(',') : [],
      enabled: config.pushStatus === '1'
    }
  }).catch(error => {
    console.error('获取推送配置失败:', error)
    // 使用默认配置
    twitterPushDialog.form = {
      followPush: false,
      tweetPush: false,
      retweetPush: false,
      replyPush: false,
      avatarChangePush: false,
      notifyMethods: [],
      enabled: true
    }
  }).finally(() => {
    twitterPushDialog.loading = false
  })
}

// 关闭Twitter推送配置对话框
const handleTwitterPushClose = () => {
  twitterPushDialog.form = {
    followPush: false,
    tweetPush: false,
    retweetPush: false,
    replyPush: false,
    avatarChangePush: false,
    notifyMethods: [],
    enabled: true
  }
}

// 保存Twitter推送配置
const handleTwitterPushSave = () => {
  if (twitterPushDialog.form.notifyMethods.length === 0) {
    proxy.$modal.msgWarning('请选择至少一种通知方式')
    return
  }
  
  twitterPushDialog.loading = true
  
  const data = {
    twitterUrl: twitterPushDialog.tokenInfo.twitterUrl,
    enableFollowPush: twitterPushDialog.form.followPush ? 1 : 0,
    enableTweetPush: twitterPushDialog.form.tweetPush ? 1 : 0,
    enableRetweetPush: twitterPushDialog.form.retweetPush ? 1 : 0,
    enableReplyPush: twitterPushDialog.form.replyPush ? 1 : 0,
    enableAvatarPush: twitterPushDialog.form.avatarChangePush ? 1 : 0,
    notifyMethods: twitterPushDialog.form.notifyMethods.join(','),
    pushStatus: twitterPushDialog.form.enabled ? '1' : '0'
  }
  
  updatePushConfig(data).then(response => {
    proxy.$modal.msgSuccess('推送配置保存成功')
    twitterPushDialog.visible = false
  }).catch(error => {
    proxy.$modal.msgError('推送配置保存失败')
  }).finally(() => {
    twitterPushDialog.loading = false
  })
}

// 打开监控配置
const handleMonitorConfig = (row) => {
  monitorDialog.tokenInfo = {
    ca: row.ca,
    symbol: row.tokenSymbol,
    name: row.tokenName
  }
  
  if (row.monitorConfig) {
    // 编辑现有配置
    monitorDialog.form = { ...row.monitorConfig }
    monitorDialog.notifyMethodsArray = row.monitorConfig.notifyMethods 
      ? row.monitorConfig.notifyMethods.split(',') 
      : []
  } else {
    // 新建配置
    monitorDialog.form = {
      id: null,
      coinAddress: row.ca,
      tokenName: row.tokenName,
      alertMode: 'timer',
      timerInterval: 60,
      conditionType: '',
      conditionValue: null,
      eventType: '',
      notifyMethods: '',
      status: '1',
      remark: ''
    }
    monitorDialog.notifyMethodsArray = []
  }
  
  monitorDialog.visible = true
}

// 关闭监控配置对话框
const handleMonitorDialogClose = () => {
  monitorDialog.form = {
    id: null,
    coinAddress: '',
    tokenName: '',
    alertMode: 'timer',
    timerInterval: 60,
    conditionType: '',
    conditionValue: null,
    eventType: '',
    notifyMethods: '',
    status: '1',
    remark: ''
  }
  monitorDialog.notifyMethodsArray = []
}

// 获取条件值提示
const getConditionValueTip = (conditionType) => {
  const tips = {
    priceAbove: 'USD',
    priceBelow: 'USD',
    marketCapBelow: 'USD',
    changeExceeds: '%'
  }
  return tips[conditionType] || ''
}

// 保存监控配置
const handleMonitorSave = () => {
  if (monitorDialog.notifyMethodsArray.length === 0) {
    proxy.$modal.msgWarning('请选择至少一种通知方式')
    return
  }
  
  monitorDialog.form.notifyMethods = monitorDialog.notifyMethodsArray.join(',')
  
  monitorDialog.loading = true
  
  // TODO: 调用保存监控配置API
  setTimeout(() => {
    monitorDialog.loading = false
    monitorDialog.visible = false
    proxy.$modal.msgSuccess('保存成功')
    getList()
  }, 500)
}

// 自动刷新数据（每60秒）
const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    getList()
  }, 60000)
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

// 初始化
onMounted(() => {
  initTodayDateRange()
  getList()
  startAutoRefresh()
})

// 组件卸载时清理定时器
onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
// Token信息单元格
.token-info-cell {
  .token-main-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;
    
    .token-symbol {
      font-weight: 600;
      font-size: 15px;
      color: #303133;
    }
    
    .token-name {
      font-size: 13px;
      color: #606266;
      flex: 1;
    }
    
    .source-tag {
      flex-shrink: 0;
    }
  }
  
  .token-address-row {
    .address-link {
      font-size: 12px;
      font-family: 'Courier New', monospace;
      color: #409EFF;
      
      &:hover {
        color: #66b1ff;
      }
      
      .el-icon {
        margin-left: 4px;
        font-size: 12px;
      }
    }
  }
}

// 时间单元格
.time-cell {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  
  .time-sub {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
  }
}

// 市值
.market-cap {
  font-weight: 500;
  color: #303133;
  font-size: 14px;
}

// Twitter操作区域
.twitter-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  
  .twitter-type-tag {
    font-size: 12px;
  }
}

// 监控状态单元格
.monitor-status-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

// 灰色文本
.text-gray-400 {
  color: #909399;
  font-size: 13px;
}

// 对话框Token信息
.dialog-token-info {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .token-symbol {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
  }
  
  .token-name {
    font-size: 14px;
    color: #606266;
  }
}

// 表单提示
.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

// 表格行高优化
:deep(.el-table__row) {
  td {
    padding: 12px 0;
  }
}

// 按钮组紧凑布局
:deep(.el-button-group) {
  .el-button {
    padding: 5px 10px;
  }
}
</style>
