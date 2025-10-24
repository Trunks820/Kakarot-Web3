<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <el-card shadow="never" style="margin-bottom: 16px;">
      <div style="display: flex; align-items: center; gap: 12px;">
        <el-icon :size="24" color="#409EFF"><Bell /></el-icon>
        <div>
          <div style="font-size: 18px; font-weight: 600; color: #303133;">历史播报</div>
          <div style="font-size: 13px; color: #909399; margin-top: 4px;">查看所有监控预警历史记录</div>
        </div>
      </div>
    </el-card>

    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="链类型" prop="chainType">
        <el-select v-model="queryParams.chainType" placeholder="全部" clearable style="width: 120px">
          <el-option label="全部" value="" />
          <el-option label="Solana" value="sol" />
          <el-option label="BSC" value="bsc" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="Token名称" prop="tokenName">
        <el-input
          v-model="queryParams.tokenName"
          placeholder="请输入Token名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="合约地址" prop="ca">
        <el-input
          v-model="queryParams.ca"
          placeholder="请输入合约地址"
          clearable
          style="width: 300px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>

      <el-form-item label="触发时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['crypto:block:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="alertList" 
      @selection-change="handleSelectionChange"
      :default-sort="{ prop: 'triggerTime', order: 'descending' }"
    >
      <el-table-column type="selection" width="55" align="center" />
      
      <el-table-column label="链类型" align="center" prop="chainType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.chainType === 'sol'" type="success">Solana</el-tag>
          <el-tag v-else-if="scope.row.chainType === 'bsc'" type="warning">BSC</el-tag>
          <el-tag v-else type="info">{{ scope.row.chainType || 'sol' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="Token信息" align="left" min-width="280">
        <template #default="scope">
          <div class="token-info-card">
            <div class="token-content">
              <!-- 第一行：符号 -->
              <div class="token-title-row">
                <div class="token-title-left">
                  <span class="token-symbol">{{ scope.row.tokenSymbol || 'Unknown' }}</span>
                </div>
              </div>
              
              <!-- 第二行：Token名称 -->
              <div class="token-subtitle" :title="scope.row.tokenName">
                {{ scope.row.tokenName || '-' }}
              </div>
              
              <!-- 第三行：合约地址 -->
              <div class="token-address">
                <el-tooltip content="点击复制" placement="top">
                  <span 
                    class="token-ca" 
                    @click="handleCopyCA(scope.row.ca)" 
                    style="white-space: normal; word-break: break-all;"
                  >
                    {{ scope.row.ca }}
                  </span>
                </el-tooltip>
                <el-tooltip content="复制" placement="top">
                  <el-icon 
                    class="copy-icon" 
                    @click="handleCopyCA(scope.row.ca)"
                  >
                    <DocumentCopy />
                  </el-icon>
                </el-tooltip>
              </div>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="播报市值" align="center" width="120">
        <template #default="scope">
          <div v-if="scope.row.marketCap" style="font-size: 13px; font-weight: 600; color: #409EFF;">
            ${{ formatNumber(scope.row.marketCap) }}
          </div>
          <span v-else style="color: #C0C4CC; font-size: 12px;">-</span>
        </template>
      </el-table-column>

      <el-table-column label="触发事件" align="center" min-width="300">
        <template #default="scope">
          <div style="line-height: 1.8;">
            {{ getEventDescription(scope.row.triggerEvents) }}
          </div>
        </template>
      </el-table-column>

      <el-table-column label="通知方式" align="center" width="120">
        <template #default="scope">
          <div style="display: flex; gap: 8px; align-items: center; justify-content: center;">
            <el-tooltip 
              v-for="method in parseNotifyMethods(scope.row.notifyMethods)" 
              :key="method"
              :content="getNotifyMethodLabel(method)"
              placement="top"
            >
              <el-icon :size="20" :color="getNotifyMethodColor(method)">
                <component :is="getNotifyMethodIcon(method)" />
              </el-icon>
            </el-tooltip>
            <span v-if="!scope.row.notifyMethods || parseNotifyMethods(scope.row.notifyMethods).length === 0" style="color: #C0C4CC; font-size: 12px;">-</span>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="通知状态" align="center" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.notifyStatus === 'success'" type="success" size="small">成功</el-tag>
          <el-tag v-else-if="scope.row.notifyStatus === 'failed'" type="danger" size="small">失败</el-tag>
          <el-tag v-else-if="scope.row.notifyStatus === 'pending'" type="warning" size="small">待发送</el-tag>
          <el-tag v-else type="info" size="small">{{ scope.row.notifyStatus || '-' }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="触发时间" align="center" prop="triggerTime" width="180" sortable>
        <template #default="scope">
          <div style="font-size: 12px;">{{ parseTime(scope.row.triggerTime) }}</div>
        </template>
      </el-table-column>

      <el-table-column label="操作" align="center" width="140" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleDetail(scope.row)"
            v-hasPermi="['crypto:block:query']"
          >详情</el-button>
          <el-button
            link
            type="primary"
            icon="Link"
            @click="handleOpenGMGN(scope.row.ca, scope.row.chainType)"
          >GMGN</el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="播报详情" width="700px" append-to-body>
      <div v-if="currentDetail" style="padding: 10px;">
        <!-- Token 基础信息 -->
        <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 24px; padding: 16px; background: #F5F7FA; border-radius: 8px;">
          <img 
            v-if="getStatsDataValue(currentDetail, 'logo') || getStatsDataValue(currentDetail, 'avatar')" 
            :src="getStatsDataValue(currentDetail, 'logo') || getStatsDataValue(currentDetail, 'avatar')" 
            :alt="currentDetail.tokenName"
            style="width: 60px; height: 60px; border-radius: 50%; object-fit: cover; box-shadow: 0 2px 8px rgba(0,0,0,0.1);"
          />
          <div v-else style="width: 60px; height: 60px; border-radius: 50%; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); display: flex; align-items: center; justify-content: center; font-size: 24px; color: white; font-weight: bold; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            {{ currentDetail.tokenSymbol?.substring(0, 1) || '?' }}
          </div>
          
          <div style="flex: 1;">
            <div style="font-size: 20px; font-weight: 700; color: #303133; margin-bottom: 8px;">
              {{ currentDetail.tokenName }} ({{ currentDetail.tokenSymbol }})
            </div>
            <el-tag v-if="currentDetail.chainType === 'sol'" type="success">Solana</el-tag>
            <el-tag v-else-if="currentDetail.chainType === 'bsc'" type="warning">BSC</el-tag>
          </div>
        </div>

        <!-- 合约地址 -->
        <div style="margin-bottom: 20px;">
          <div style="font-size: 13px; color: #909399; margin-bottom: 8px;">合约地址 (CA)</div>
          <div style="display: flex; align-items: center; gap: 8px; padding: 12px; background: #F5F7FA; border-radius: 6px;">
            <span style="font-family: monospace; font-size: 13px; font-weight: 700; color: #303133; flex: 1; word-break: break-all;">
              {{ currentDetail.ca }}
            </span>
            <el-button size="small" @click="handleCopyCA(currentDetail.ca)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
            <el-button size="small" type="primary" @click="handleOpenGMGN(currentDetail.ca, currentDetail.chainType)">
              <el-icon><Link /></el-icon>
              GMGN
            </el-button>
          </div>
        </div>

        <!-- 触发事件 -->
        <div style="margin-bottom: 20px;">
          <div style="font-size: 13px; color: #909399; margin-bottom: 8px;">触发事件</div>
          <div style="padding: 12px; background: #FEF0F0; border-radius: 6px; color: #F56C6C; font-weight: 600;">
            {{ getEventDescription(currentDetail.triggerEvents) }}
          </div>
        </div>

        <!-- 关键数据 -->
        <div style="margin-bottom: 20px;">
          <div style="font-size: 13px; color: #909399; margin-bottom: 12px;">关键数据</div>
          <el-row :gutter="16">
            <el-col :span="12" v-if="getStatsDataValue(currentDetail, 'price')">
              <div style="padding: 16px; background: #F0F9FF; border-radius: 6px; text-align: center;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 4px;">💰 价格</div>
                <div style="font-size: 18px; font-weight: 700; color: #409EFF;">
                  ${{ formatPrice(getStatsDataValue(currentDetail, 'price')) }}
                </div>
              </div>
            </el-col>
            <el-col :span="12" v-if="currentDetail.marketCap">
              <div style="padding: 16px; background: #F0F9FF; border-radius: 6px; text-align: center;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 4px;">💎 市值</div>
                <div style="font-size: 18px; font-weight: 700; color: #409EFF;">
                  ${{ formatNumber(currentDetail.marketCap) }}
                </div>
              </div>
            </el-col>
            <el-col :span="12" v-if="getStatsDataValue(currentDetail, 'volume24h')">
              <div style="padding: 16px; background: #F0F9FF; border-radius: 6px; text-align: center; margin-top: 16px;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 4px;">📊 24h交易量</div>
                <div style="font-size: 18px; font-weight: 700; color: #409EFF;">
                  ${{ formatNumber(getStatsDataValue(currentDetail, 'volume24h')) }}
                </div>
              </div>
            </el-col>
            <el-col :span="12" v-if="getStatsDataValue(currentDetail, 'holders')">
              <div style="padding: 16px; background: #F0F9FF; border-radius: 6px; text-align: center; margin-top: 16px;">
                <div style="font-size: 12px; color: #909399; margin-bottom: 4px;">👥 持币人</div>
                <div style="font-size: 18px; font-weight: 700; color: #409EFF;">
                  {{ formatNumber(getStatsDataValue(currentDetail, 'holders')) }}
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 播报信息 -->
        <div style="display: flex; justify-content: space-between; padding: 12px; background: #F5F7FA; border-radius: 6px;">
          <div>
            <span style="font-size: 12px; color: #909399;">⏰ 触发时间:</span>
            <span style="font-size: 13px; font-weight: 600; margin-left: 8px;">{{ parseTime(currentDetail.triggerTime) }}</span>
          </div>
          <div v-if="getStatsDataValue(currentDetail, 'notificationCount')">
            <span style="font-size: 12px; color: #909399;">📢 播报次数:</span>
            <span style="font-size: 13px; font-weight: 600; margin-left: 8px;">第{{ getStatsDataValue(currentDetail, 'notificationCount') }}次</span>
          </div>
        </div>
      </div>
      
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="BlockMonitor">
import { ref, getCurrentInstance } from 'vue'
import { listAlertLog, delAlertLog } from '@/api/crypto/blockMonitor'
import { Bell, CopyDocument, Link, DocumentCopy, Connection, Monitor, Notification, ChatDotRound } from '@element-plus/icons-vue'
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()

const alertList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const currentDetail = ref(null)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  chainType: undefined,
  tokenName: undefined,
  ca: undefined,
  triggerEvents: undefined,
  notifyStatus: undefined
})

/** 查询列表 */
function getList() {
  loading.value = true
  console.log('🔍 [BlockMonitor] 开始查询列表，参数:', queryParams.value)
  const params = proxy.addDateRange(queryParams.value, dateRange.value)
  console.log('🔍 [BlockMonitor] 最终请求参数:', params)
  
  listAlertLog(params).then(response => {
    console.log('✅ [BlockMonitor] API响应成功:', response)
    console.log('✅ [BlockMonitor] 数据行数:', response.rows?.length)
    console.log('✅ [BlockMonitor] 总数:', response.total)
    alertList.value = response.rows
    total.value = response.total
    loading.value = false
  }).catch(error => {
    console.error('❌ [BlockMonitor] API调用失败:', error)
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 详情按钮操作 */
function handleDetail(row) {
  currentDetail.value = { ...row }
  detailVisible.value = true
}

/** 删除按钮操作 */
function handleDelete(row) {
  const alertIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除选中的历史播报记录？').then(() => {
    return delAlertLog(alertIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('crypto/block/export', {
    ...queryParams.value
  }, `alert_log_${new Date().getTime()}.xlsx`)
}

/** 复制CA地址 */
async function handleCopyCA(ca) {
  try {
    await navigator.clipboard.writeText(ca)
    proxy.$modal.msgSuccess('CA地址已复制')
  } catch (error) {
    console.error('复制失败:', error)
    proxy.$modal.msgError('复制失败，请手动复制')
  }
}

/** 打开GMGN */
function handleOpenGMGN(ca, chainType = 'sol') {
  const chain = chainType ? chainType.toLowerCase() : 'sol'
  const url = `https://gmgn.ai/${chain}/token/${ca}`
  window.open(url, '_blank')
}

/** 从 trigger_events 中提取 description */
function getEventDescription(triggerEvents) {
  if (!triggerEvents) return '-'
  
  try {
    const events = typeof triggerEvents === 'string' ? JSON.parse(triggerEvents) : triggerEvents
    if (Array.isArray(events) && events.length > 0 && events[0].description) {
      return events[0].description
    }
  } catch (e) {
    console.error('解析 trigger_events 失败:', e)
  }
  
  return '-'
}

/** 获取stats_data中的值 */
function getStatsDataValue(row, key) {
  const stats = parseStatsData(row.statsData)
  return stats[key]
}

/** 解析触发事件 */
function parseTriggerEvents(events) {
  if (!events) return []
  try {
    return typeof events === 'string' ? JSON.parse(events) : events
  } catch {
    return []
  }
}

/** 解析统计数据 */
function parseStatsData(data) {
  if (!data) return {}
  try {
    return typeof data === 'string' ? JSON.parse(data) : data
  } catch {
    return {}
  }
}

/** 解析通知方式 */
function parseNotifyMethods(methods) {
  if (!methods) {
    return []
  }
  
  try {
    // 如果是字符串，尝试两种格式
    if (typeof methods === 'string') {
      // 尝试 JSON 格式：["websocket", "browser"]
      if (methods.startsWith('[')) {
        return JSON.parse(methods)
      }
      // 逗号分隔格式：telegram,wechat
      return methods.split(',').map(m => m.trim()).filter(m => m)
    }
    // 如果已经是数组，直接返回
    return Array.isArray(methods) ? methods : []
  } catch (e) {
    console.error('❌ 解析 notify_methods 失败:', methods, e)
    return []
  }
}

/** 获取事件标签类型 */
function getEventTagType(event) {
  const typeMap = {
    price: 'danger',
    holders: 'success',
    volume: 'warning',
    marketCap: 'primary'
  }
  return typeMap[event] || 'info'
}

/** 获取事件标签文本 */
function getEventLabel(event) {
  const labelMap = {
    price: '价格异动',
    holders: '持币人增加',
    volume: '交易量变化',
    marketCap: '市值变化'
  }
  return labelMap[event] || event
}

/** 获取统计数据标签 */
function getStatsLabel(key) {
  const labelMap = {
    price: '价格',
    priceChange: '涨跌幅',
    holders: '持币人',
    holdersIncrease: '持币人增加',
    volume24h: '24h交易量',
    volumeChange: '交易量变化',
    marketCap: '市值',
    marketCapChange: '市值变化'
  }
  return labelMap[key] || key
}

/** 格式化统计数据值 */
function formatStatsValue(key, value) {
  if (value === null || value === undefined) return '-'
  
  if (key.includes('Change') || key.includes('Increase')) {
    return `${value > 0 ? '+' : ''}${value}%`
  }
  
  if (key === 'price') {
    return `$${parseFloat(value).toFixed(8).replace(/\.?0+$/, '')}`
  }
  
  if (key === 'holders') {
    return formatNumber(value)
  }
  
  if (key.includes('volume') || key.includes('marketCap')) {
    return `$${formatNumber(value)}`
  }
  
  return value
}

/** 格式化数字 */
function formatNumber(num) {
  if (!num) return '0'
  const value = Number(num)
  if (value >= 1e9) return (value / 1e9).toFixed(2) + 'B'
  if (value >= 1e6) return (value / 1e6).toFixed(2) + 'M'
  if (value >= 1e3) return (value / 1e3).toFixed(2) + 'K'
  return value.toFixed(2)
}

/** 格式化价格（保留5位小数） */
function formatPrice(price) {
  if (!price) return '0'
  const value = Number(price)
  return value.toFixed(5)
}

/** 获取通知方式标签 */
function getNotifyMethodLabel(method) {
  const labelMap = {
    websocket: 'WebSocket',
    browser: '浏览器通知',
    system: '系统通知',
    notification: '页面通知',
    telegram: 'Telegram',
    wechat: '微信'
  }
  return labelMap[method] || method
}

/** 获取通知方式图标 */
function getNotifyMethodIcon(method) {
  const iconMap = {
    websocket: 'Connection',
    browser: 'Monitor',
    system: 'Bell',
    notification: 'Notification',
    telegram: 'Notification',
    wechat: 'ChatDotRound'
  }
  return iconMap[method] || 'Bell'
}

/** 获取通知方式颜色 */
function getNotifyMethodColor(method) {
  const colorMap = {
    websocket: '#409EFF',
    browser: '#67C23A',
    system: '#E6A23C',
    notification: '#909399',
    telegram: '#0088CC',
    wechat: '#07C160'
  }
  return colorMap[method] || '#909399'
}

getList()
</script>

<style scoped>
.app-container {
  padding: 20px;
}

/* Token信息卡片样式 */
.token-info-card {
  padding: 8px 0;
}

.token-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.token-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.token-symbol {
  font-size: 15px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 0.3px;
}

.token-subtitle {
  font-size: 13px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.token-address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
}

.token-ca {
  font-family: 'JetBrains Mono', Monaco, Menlo, Consolas, 'Courier New', monospace;
  color: #5B8FF9;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.2s;
}

.token-ca:hover {
  color: #409EFF;
}

.copy-icon {
  cursor: pointer;
  color: #909399;
  transition: color 0.2s;
  font-size: 14px;
}

.copy-icon:hover {
  color: #409EFF;
}
</style>

