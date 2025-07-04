<template>
  <div class="app-container">
            <!-- 搜索栏 -->
            <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
              <el-form-item label="代币地址" prop="coinAddress">
                <el-input 
                  v-model="queryParams.coinAddress" 
                  placeholder="请输入代币地址" 
                  clearable 
                  style="width: 200px" 
                  @keyup.enter="handleQuery" 
                />
              </el-form-item>
              <el-form-item label="监控模式" prop="alertMode">
                <el-select v-model="queryParams.alertMode" placeholder="监控模式" clearable style="width: 120px">
                  <el-option label="定时提醒" value="timer"></el-option>
                  <el-option label="价格触发" value="condition"></el-option>
                  <el-option label="事件监控" value="event"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="通知方式" prop="notifyMethods">
                <el-select v-model="queryParams.notifyMethods" placeholder="通知方式" clearable style="width: 120px">
                  <el-option label="微信" value="wechat"></el-option>
                  <el-option label="Telegram" value="telegram"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 100px">
                  <el-option label="启用" value="1"></el-option>
                  <el-option label="停用" value="0"></el-option>
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
                <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['crypto:monitor:add']">新增监控</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['crypto:monitor:edit']">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['crypto:monitor:remove']">删除</el-button>
              </el-col>

              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
            </el-row>

            <!-- 监控配置列表表格 -->
            <el-table v-loading="loading" :data="monitorList" @selection-change="handleSelectionChange" ref="monitorTable">
              <el-table-column type="selection" width="50" align="center" />
              
              <!-- 代币信息 -->
              <el-table-column label="代币信息" align="left" width="180" v-if="columns[0].visible">
                <template #default="scope">
                  <div class="token-info">
                    <div class="token-name">
                      {{ scope.row.tokenName || '未知代币' }}
                    </div>
                  </div>
                </template>
              </el-table-column>

              <!-- 监控模式 -->
              <el-table-column label="监控模式" align="center" width="120" v-if="columns[1].visible">
                <template #default="scope">
                  <el-tag :type="getAlertModeType(scope.row.alertMode)">
                    {{ getAlertModeText(scope.row.alertMode) }}
                  </el-tag>
                </template>
              </el-table-column>

              <!-- 监控条件 -->
              <el-table-column label="监控条件" align="center" width="180" v-if="columns[2].visible">
                <template #default="scope">
                  <div v-if="scope.row.alertMode === 'timer'">
                    <span class="condition-text">每 {{ scope.row.timerInterval }} 分钟</span>
                  </div>
                  <div v-else-if="scope.row.alertMode === 'condition'" class="condition-detail">
                    <div class="condition-type">{{ getConditionTypeText(scope.row.conditionType) }}</div>
                    <div class="condition-value">{{ formatConditionValue(scope.row.conditionValue, scope.row.conditionType) }}</div>
                  </div>
                  <div v-else-if="scope.row.alertMode === 'event'" class="event-detail">
                    <div class="event-type">{{ getEventTypeText(scope.row.eventType) }}</div>
                    <div class="event-config">{{ formatEventConfig(scope.row.eventConfig, scope.row.eventType) }}</div>
                  </div>
                </template>
              </el-table-column>

              <!-- 通知方式 -->
              <el-table-column label="通知方式" align="center" width="150" v-if="columns[3].visible">
                <template #default="scope">
                  <div class="notify-methods">
                    <el-tag 
                      v-for="method in getNotifyMethods(scope.row.notifyMethods)" 
                      :key="method" 
                      size="small" 
                      class="method-tag"
                      :type="method === 'telegram' ? 'primary' : 'success'"
                    >
                      {{ method === 'telegram' ? 'TG' : '微信' }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>

              <!-- 通知对象 -->
              <el-table-column label="通知对象" align="center" width="120" v-if="columns[4].visible">
                <template #default="scope">
                  <div class="notify-target">
                    <div v-if="scope.row.telegramName" class="target-item">
                      <el-icon><ChatLineSquare /></el-icon>
                      {{ scope.row.telegramName }}
                    </div>
                    <div v-if="scope.row.wechatName" class="target-item">
                      <el-icon><Message /></el-icon>
                      {{ scope.row.wechatName }}
                    </div>
                  </div>
                </template>
              </el-table-column>

              <!-- 最后通知 -->
              <el-table-column label="最后通知" align="center" width="140" v-if="columns[5].visible">
                <template #default="scope">
                  <div v-if="scope.row.lastNotificationTime">
                    {{ formatTime(scope.row.lastNotificationTime) }}
                  </div>
                  <span v-else class="text-gray-400">从未通知</span>
                </template>
              </el-table-column>

              <!-- 状态 -->
              <el-table-column label="状态" align="center" width="80" v-if="columns[6].visible">
                <template #default="scope">
                  <el-switch
                    v-model="scope.row.status"
                    active-value="1"
                    inactive-value="0"
                    @change="handleStatusChange(scope.row)"
                  ></el-switch>
                </template>
              </el-table-column>

              <!-- 创建者 -->
              <el-table-column label="创建者" align="center" width="100" v-if="columns[7].visible">
                <template #default="scope">
                  {{ scope.row.createBy || '-' }}
                </template>
              </el-table-column>

              <!-- 创建时间 -->
              <el-table-column label="创建时间" align="center" width="140" v-if="columns[8].visible">
                <template #default="scope">
                  {{ formatTime(scope.row.createTime) }}
                </template>
              </el-table-column>

              <!-- 操作 -->
              <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-tooltip content="查看详情" placement="top">
                    <el-button link type="primary" icon="View" @click="handleDetail(scope.row)"></el-button>
                  </el-tooltip>
                  <el-tooltip content="修改" placement="top">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['crypto:monitor:edit']"></el-button>
                  </el-tooltip>
                  <el-tooltip content="测试通知" placement="top">
                    <el-button link type="warning" icon="Bell" @click="handleTestNotify(scope.row)" v-hasPermi="['crypto:monitor:test']"></el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['crypto:monitor:remove']"></el-button>
                  </el-tooltip>
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

    <!-- 添加或修改监控配置对话框 -->
    <el-dialog :title="title" v-model="open" width="800px" append-to-body :close-on-click-modal="true">
      <el-form :model="form" :rules="rules" ref="monitorRef" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="代币地址" prop="coinAddress">
              <el-input 
                v-model="form.coinAddress" 
                placeholder="请输入代币地址" 
                maxlength="255" 
                :disabled="form.id && title.includes('修改')" 
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="监控模式" prop="alertMode">
              <el-radio-group v-model="form.alertMode">
                <el-radio value="timer">定时提醒</el-radio>
                <el-radio value="condition">价格触发</el-radio>
                <el-radio value="event">事件监控</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 定时提醒配置 -->
        <el-row v-if="form.alertMode === 'timer'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="提醒间隔" prop="timerInterval">
              <el-input-number 
                v-model="form.timerInterval" 
                :min="1" 
                :max="1440" 
                placeholder="分钟"
                style="width: 100%"
              />
              <div class="form-tip">建议设置 5-60 分钟</div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 价格触发配置 -->
        <el-row v-if="form.alertMode === 'condition'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="触发条件" prop="conditionType">
              <el-select v-model="form.conditionType" placeholder="请选择触发条件">
                <el-option label="价格高于" value="priceAbove"></el-option>
                <el-option label="价格低于" value="priceBelow"></el-option>
                <el-option label="市值低于" value="marketCapBelow"></el-option>
                <el-option label="涨跌幅超过" value="changeExceeds"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阈值" prop="conditionValue">
              <!-- 市值条件使用数字+单位选择器 -->
              <div v-if="form.conditionType === 'marketCapBelow'" style="display: flex; gap: 8px;">
                <el-input-number 
                  v-model="marketCapValue" 
                  :min="0" 
                  :precision="2"
                  placeholder="请输入数值" 
                  style="flex: 1;"
                  @change="updateMarketCapConditionValue"
                />
                <el-select 
                  v-model="marketCapUnit" 
                  placeholder="单位" 
                  style="width: 80px;"
                  @change="updateMarketCapConditionValue"
                >
                  <el-option label="K" value="K" />
                  <el-option label="M" value="M" />
                  <el-option label="B" value="B" />
                </el-select>
              </div>
              <!-- 其他条件使用普通数字输入框 -->
              <el-input-number 
                v-else
                v-model="form.conditionValue" 
                :min="0" 
                :precision="getValuePrecision()"
                placeholder="请输入阈值"
                style="width: 100%"
              />
              <div class="form-tip" :class="{ 'tip-red': shouldShowRedTip() }">{{ getConditionTip() }}</div>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 事件监控配置 -->
        <el-row v-if="form.alertMode === 'event'" :gutter="16">
          <el-col :span="12">
            <el-form-item label="事件类型" prop="eventType">
              <el-select v-model="form.eventType" placeholder="请选择事件类型" @change="handleEventTypeChange">
                <el-option label="大额交易监控" value="largeTransaction"></el-option>
                <el-option label="持仓异动监控" value="holdingChange"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 大额交易监控配置 -->
        <div v-if="form.alertMode === 'event' && form.eventType === 'largeTransaction'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="金额阈值" prop="eventConfig.amountThreshold">
                <el-input-number 
                  v-model="form.eventConfig.amountThreshold" 
                  :min="0" 
                  :precision="2"
                  placeholder="请输入金额阈值"
                  style="width: 100%"
                />
                <div class="form-tip">单位：SOL，触发通知的最小交易金额</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="交易类型" prop="eventConfig.transactionType">
                <el-select v-model="form.eventConfig.transactionType" placeholder="请选择交易类型">
                  <el-option label="买入" value="buy"></el-option>
                  <el-option label="卖出" value="sell"></el-option>
                  <el-option label="买入和卖出" value="both"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- 持仓异动监控配置 -->
        <div v-if="form.alertMode === 'event' && form.eventType === 'holdingChange'">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="变化阈值" prop="eventConfig.changeThreshold">
                <el-input-number 
                  v-model="form.eventConfig.changeThreshold" 
                  :min="0" 
                  :max="100"
                  :precision="2"
                  placeholder="请输入变化阈值"
                  style="width: 100%"
                />
                <div class="form-tip">单位：%，前10大持仓变化超过此百分比时触发</div>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="异动类型" prop="eventConfig.changeType">
                <el-select v-model="form.eventConfig.changeType" placeholder="请选择异动类型">
                  <el-option label="增加" value="increase"></el-option>
                  <el-option label="减少" value="decrease"></el-option>
                  <el-option label="双向" value="both"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="检查间隔" prop="eventConfig.checkInterval">
                <el-input-number 
                  v-model="form.eventConfig.checkInterval" 
                  :min="5" 
                  :max="1440"
                  placeholder="请输入检查间隔"
                  style="width: 100%"
                />
                <div class="form-tip">单位：分钟，检查持仓变化的间隔</div>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="通知方式" prop="notifyMethods">
              <el-checkbox-group v-model="form.notifyMethodsArray">
                <el-checkbox value="wechat">微信</el-checkbox>
                <el-checkbox value="telegram">Telegram</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.notifyMethodsArray.includes('wechat')" :gutter="16">
          <el-col :span="12">
            <el-form-item label="微信名称" prop="wechatName">
              <el-input v-model="form.wechatName" placeholder="请输入微信名称" maxlength="100" />
              <div class="form-tip">支持发送给个人或群聊（群聊请填写群名称）</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.notifyMethodsArray.includes('telegram')" :gutter="16">
          <el-col :span="12">
            <el-form-item label="Telegram用户或群聊ID" prop="telegramName">
              <el-input v-model="form.telegramName" placeholder="请输入Telegram用户ID或群聊ID（纯数字）" maxlength="100" />
              <div class="form-tip">支持发送给个人或群聊，需要填写数字ID而非用户名</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" maxlength="500"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="submitLoading" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 监控详情对话框 -->
    <el-dialog title="监控详情" v-model="detailVisible" width="600px" append-to-body :close-on-click-modal="true">
      <div v-if="currentMonitor" class="monitor-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="代币信息" :span="2">
            <div class="token-detail">
              <div class="token-name">{{ currentMonitor.tokenName || '未知代币' }}</div>
              <div class="token-address">{{ currentMonitor.coinAddress }}</div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="监控模式">
            <el-tag :type="getAlertModeType(currentMonitor.alertMode)">
              {{ getAlertModeText(currentMonitor.alertMode) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="链类型">
            <el-tag :type="getChainTagType(currentMonitor.coinAddress)">
              {{ getChainType(currentMonitor.coinAddress) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="监控条件" :span="2">
            <div v-if="currentMonitor.alertMode === 'timer'">
              每 {{ currentMonitor.timerInterval }} 分钟提醒一次
            </div>
            <div v-else-if="currentMonitor.alertMode === 'condition'" class="condition-detail">
              <div class="condition-type">{{ getConditionTypeText(currentMonitor.conditionType) }}</div>
              <div class="condition-value">{{ formatConditionValue(currentMonitor.conditionValue, currentMonitor.conditionType) }}</div>
            </div>
            <div v-else-if="currentMonitor.alertMode === 'event'" class="event-detail">
              <div class="event-type">{{ getEventTypeText(currentMonitor.eventType) }}</div>
              <div class="event-config">{{ formatEventConfig(currentMonitor.eventConfig, currentMonitor.eventType) }}</div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="通知方式">
            <div class="notify-methods">
              <el-tag 
                v-for="method in getNotifyMethods(currentMonitor.notifyMethods)" 
                :key="method" 
                size="small" 
                class="method-tag"
                :type="method === 'telegram' ? 'primary' : 'success'"
              >
                {{ method === 'telegram' ? 'Telegram' : '微信' }}
              </el-tag>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="通知对象">
            <div class="notify-target">
              <div v-if="currentMonitor.telegramName">TG: {{ currentMonitor.telegramName }}</div>
              <div v-if="currentMonitor.wechatName">微信: {{ currentMonitor.wechatName }}</div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentMonitor.status === '1' ? 'success' : 'danger'">
              {{ currentMonitor.status === '1' ? '启用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="最后通知">
            {{ currentMonitor.lastNotificationTime ? formatTime(currentMonitor.lastNotificationTime) : '从未通知' }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(currentMonitor.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="创建者">
            {{ currentMonitor.createBy || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentMonitor.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CryptoMonitor">
import { ref, reactive, toRefs, getCurrentInstance, onMounted } from 'vue'
import { parseTime } from "@/utils/ruoyi"
import { 
  listCryptoMonitorConfig, 
  getCryptoMonitorConfig, 
  addCryptoMonitorConfig, 
  updateCryptoMonitorConfig, 
  delCryptoMonitorConfig,
  changeCryptoMonitorStatus,
  testCryptoMonitorNotify
} from "@/api/crypto/monitor"
const { proxy } = getCurrentInstance()

const monitorList = ref([])
const open = ref(false)
const detailVisible = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const currentMonitor = ref({})

// 市值输入相关
const marketCapValue = ref(null)
const marketCapUnit = ref('M')

// 表单提交loading状态
const submitLoading = ref(false)

// 列显隐信息
const columns = ref([
  { key: 0, label: `代币信息`, visible: true },
  { key: 1, label: `监控模式`, visible: true },
  { key: 2, label: `监控条件`, visible: true },
  { key: 3, label: `通知方式`, visible: true },
  { key: 4, label: `通知对象`, visible: true },
  { key: 5, label: `最后通知`, visible: true },
  { key: 6, label: `状态`, visible: true },
  { key: 7, label: `创建者`, visible: true },
  { key: 8, label: `创建时间`, visible: true }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    coinAddress: undefined,
    alertMode: undefined,
    notifyMethods: undefined,
    status: undefined
  },
  rules: {
    coinAddress: [
      { required: true, message: "代币地址不能为空", trigger: "blur" },
      { min: 32, message: "代币地址长度不能少于32个字符", trigger: "blur" }
    ],
    alertMode: [
      { required: true, message: "请选择监控模式", trigger: "change" }
    ],
    notifyMethodsArray: [
      { type: 'array', required: true, message: "请选择至少一种通知方式", trigger: "change" }
    ],
    timerInterval: [
      { required: true, message: "请输入提醒间隔", trigger: "blur", validator: (rule, value, callback) => {
        if (form.alertMode === 'timer' && (!value || value < 1)) {
          callback(new Error('提醒间隔必须大于0分钟'))
        } else {
          callback()
        }
      }}
    ],
    conditionType: [
      { required: true, message: "请选择触发条件", trigger: "change", validator: (rule, value, callback) => {
        if (form.alertMode === 'condition' && !value) {
          callback(new Error('请选择触发条件'))
        } else {
          callback()
        }
      }}
    ],
    conditionValue: [
      { required: true, message: "请输入阈值", trigger: "blur", validator: (rule, value, callback) => {
        if (form.alertMode === 'condition' && (!value || value <= 0)) {
          callback(new Error('阈值必须大于0'))
        } else {
          callback()
        }
      }}
    ],
    eventType: [
      { required: true, message: "请选择事件类型", trigger: "change", validator: (rule, value, callback) => {
        if (form.alertMode === 'event' && !value) {
          callback(new Error('请选择事件类型'))
        } else {
          callback()
        }
      }}
    ],
    'eventConfig.amountThreshold': [
      { required: true, message: "请输入金额阈值", trigger: "blur", validator: (rule, value, callback) => {
        if (form.alertMode === 'event' && form.eventType === 'largeTransaction' && (!value || value <= 0)) {
          callback(new Error('金额阈值必须大于0'))
        } else {
          callback()
        }
      }}
    ],
    'eventConfig.transactionType': [
      { required: true, message: "请选择交易类型", trigger: "change", validator: (rule, value, callback) => {
        if (form.alertMode === 'event' && form.eventType === 'largeTransaction' && !value) {
          callback(new Error('请选择交易类型'))
        } else {
          callback()
        }
      }}
    ],

    'eventConfig.changeThreshold': [
      { required: true, message: "请输入变化阈值", trigger: "blur", validator: (rule, value, callback) => {
        if (form.alertMode === 'event' && form.eventType === 'holdingChange' && (!value || value <= 0)) {
          callback(new Error('变化阈值必须大于0'))
        } else {
          callback()
        }
      }}
    ],
    'eventConfig.changeType': [
      { required: true, message: "请选择异动类型", trigger: "change", validator: (rule, value, callback) => {
        if (form.alertMode === 'event' && form.eventType === 'holdingChange' && !value) {
          callback(new Error('请选择异动类型'))
        } else {
          callback()
        }
      }}
    ],
    'eventConfig.checkInterval': [
      { required: true, message: "请输入检查间隔", trigger: "blur", validator: (rule, value, callback) => {
        if (form.alertMode === 'event' && form.eventType === 'holdingChange' && (!value || value < 5)) {
          callback(new Error('检查间隔必须大于等于5分钟'))
        } else {
          callback()
        }
      }}
    ],
    wechatName: [
      { required: true, message: "请输入微信名称", trigger: "blur", validator: (rule, value, callback) => {
        if (form.notifyMethodsArray && form.notifyMethodsArray.includes('wechat') && !value) {
          callback(new Error('请输入微信名称'))
        } else {
          callback()
        }
      }}
    ],
    telegramName: [
      { required: true, message: "请输入Telegram用户或群聊ID", trigger: "blur", validator: (rule, value, callback) => {
        if (form.notifyMethodsArray && form.notifyMethodsArray.includes('telegram') && !value) {
          callback(new Error('请输入Telegram用户或群聊ID'))
        } else {
          callback()
        }
      }}
    ]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询监控配置列表 */
function getList() {
  loading.value = true
  listCryptoMonitorConfig(queryParams.value).then(response => {
    monitorList.value = response.rows
    total.value = response.total
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
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加监控配置"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getCryptoMonitorConfig(id).then(response => {
    form.value = response.data
    // 处理通知方式数组
    if (form.value.notifyMethods) {
      form.value.notifyMethodsArray = form.value.notifyMethods.split(',')
    } else {
      form.value.notifyMethodsArray = []
    }
    // 处理事件配置
    if (form.value.alertMode === 'event' && form.value.eventConfig) {
      if (typeof form.value.eventConfig === 'string') {
        try {
          form.value.eventConfig = JSON.parse(form.value.eventConfig)
        } catch (e) {
          console.error('解析事件配置失败:', e)
          form.value.eventConfig = {}
        }
      }
    }
    // 处理市值条件值解析
    if (form.value.alertMode === 'condition' && form.value.conditionType === 'marketCapBelow' && form.value.conditionValue) {
      parseMarketCapValue(form.value.conditionValue)
    }
    if (!form.value.eventConfig) {
      form.value.eventConfig = {
        amountThreshold: 20,
        transactionType: 'both',
        changeThreshold: null,
        changeType: 'both',
        checkInterval: 30
      }
    }
    open.value = true
    title.value = "修改监控配置"
  })
}

/** 查看详情 */
function handleDetail(row) {
  currentMonitor.value = row
  detailVisible.value = true
}

/** 测试通知 */
function handleTestNotify(row) {
  const tokenDisplay = row.tokenName || formatAddress(row.coinAddress)
  proxy.$modal.confirm(`确认要为代币"${tokenDisplay}"发送测试通知吗？`).then(() => {
    return testCryptoMonitorNotify(row.id)
  }).then(() => {
    proxy.$modal.msgSuccess("测试通知发送成功")
  }).catch((error) => {
    proxy.$modal.msgError(error.msg || "测试通知发送失败")
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["monitorRef"].validate(valid => {
    if (valid) {
      submitLoading.value = true
      
      // 处理通知方式
      form.value.notifyMethods = form.value.notifyMethodsArray.join(',')
      
      // 处理事件配置 - 如果是事件监控，将eventConfig转换为JSON字符串
      if (form.value.alertMode === 'event' && form.value.eventConfig) {
        // 创建一个拷贝以避免修改原始对象
        const submitData = { ...form.value }
        submitData.eventConfig = JSON.stringify(form.value.eventConfig)
        
        if (submitData.id != null) {
          updateCryptoMonitorConfig(submitData).then(response => {
            proxy.$modal.msgSuccess("修改成功")
            open.value = false
            getList()
          }).catch(() => {
            // 请求失败时也要关闭loading
          }).finally(() => {
            submitLoading.value = false
          })
        } else {
          addCryptoMonitorConfig(submitData).then(response => {
            proxy.$modal.msgSuccess("新增成功")
            open.value = false
            getList()
          }).catch(() => {
            // 请求失败时也要关闭loading
          }).finally(() => {
            submitLoading.value = false
          })
        }
      } else {
        if (form.value.id != null) {
          updateCryptoMonitorConfig(form.value).then(response => {
            proxy.$modal.msgSuccess("修改成功")
            open.value = false
            getList()
          }).catch(() => {
            // 请求失败时也要关闭loading
          }).finally(() => {
            submitLoading.value = false
          })
        } else {
          addCryptoMonitorConfig(form.value).then(response => {
            proxy.$modal.msgSuccess("新增成功")
            open.value = false
            getList()
          }).catch(() => {
            // 请求失败时也要关闭loading
          }).finally(() => {
            submitLoading.value = false
          })
        }
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const configIds = row.id || ids.value
  proxy.$modal.confirm('是否确认删除监控配置编号为"' + configIds + '"的数据项？').then(function() {
    return delCryptoMonitorConfig(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}



/** 状态修改 */
function handleStatusChange(row) {
  let text = row.status === "1" ? "启用" : "停用"
  proxy.$modal.confirm('确认要"' + text + '"该监控配置吗？').then(() => {
    return changeCryptoMonitorStatus(row.id, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(() => {
    row.status = row.status === "1" ? "0" : "1"
  })
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  // 重置市值输入
  marketCapValue.value = null
  marketCapUnit.value = 'M'
  // 重置提交loading状态
  submitLoading.value = false
  
  form.value = {
    id: null,
    coinAddress: null,
    tokenName: null,
    alertMode: 'timer',
    timerInterval: 15,
    conditionType: null,
    conditionValue: null,
    eventType: null,
    eventConfig: {
      // 大额交易监控配置
      amountThreshold: 20,
      transactionType: 'both',
      // 持仓异动监控配置
      changeThreshold: null,
      changeType: 'both',
      checkInterval: 30
    },
    notifyMethods: null,
    notifyMethodsArray: [],
    wechatName: null,
    telegramName: null,
    remark: null,
    status: '1'
  }
  proxy.resetForm("monitorRef")
}

/** 格式化地址 */
function formatAddress(address) {
  if (!address) return ''
  return address.length > 16 ? `${address.substring(0, 8)}...${address.substring(address.length - 8)}` : address
}

/** 获取链类型 */
function getChainType(address) {
  if (!address) return 'UNKNOWN'
  
  // ETH/BSC/BASE 地址格式: 0x + 40位十六进制
  if (address.startsWith('0x') && address.length === 42) {
    // 这里可以根据实际需求进一步区分ETH/BSC/BASE
    return 'EVM'
  }
  // SOL 地址格式: 32-44位 base58编码
  else if (address.length >= 32 && address.length <= 44 && !address.startsWith('0x')) {
    return 'SOL'
  }
  
  return 'UNKNOWN'
}

/** 获取链标签类型 */
function getChainTagType(address) {
  const chainType = getChainType(address)
  const types = {
    'SOL': 'primary',
    'EVM': 'success',
    'UNKNOWN': 'warning'
  }
  return types[chainType] || 'info'
}

/** 格式化时间 */
function formatTime(time) {
  if (!time) return ''
  return parseTime(time, '{y}-{m}-{d} {h}:{i}')
}

/** 获取通知方式数组 */
function getNotifyMethods(methods) {
  if (!methods) return []
  return methods.split(',').filter(method => method.trim())
}

/** 获取条件类型文本 */
function getConditionTypeText(type) {
  const texts = {
    'priceAbove': '价格高于',
    'priceBelow': '价格低于',
    'marketCapBelow': '市值低于',
    'changeExceeds': '涨跌幅超过'
  }
  return texts[type] || type
}

/** 格式化条件值 */
function formatConditionValue(value, type) {
  if (!value) return ''
  
  if (type === 'changeExceeds') {
    // 涨跌幅超过 - 百分比单位
    return `${value}%`
  } else if (type === 'marketCapBelow') {
    // 市值低于 - K/M/B单位
    return formatMarketCap(value)
  } else {
    // 价格高于/低于 - 美元单位，支持小数
    return formatPrice(value)
  }
}

/** 格式化价格 */
function formatPrice(value) {
  if (value >= 1) {
    return `$${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
  } else {
    return `$${value.toFixed(6)}`
  }
}

/** 格式化市值 */
function formatMarketCap(value) {
  if (value >= 1000000000) {
    return `$${(value / 1000000000).toFixed(1)}B`
  } else if (value >= 1000000) {
    return `$${(value / 1000000).toFixed(1)}M`
  } else if (value >= 1000) {
    return `$${(value / 1000).toFixed(1)}K`
  } else {
    return `$${value.toLocaleString()}`
  }
}

/** 获取输入精度 */
function getValuePrecision() {
  if (form.value.conditionType === 'changeExceeds') {
    return 2
  } else if (form.value.conditionType === 'marketCapBelow') {
    return 0
  } else {
    return 8
  }
}

/** 获取条件提示 */
function getConditionTip() {
  const tips = {
    'priceAbove': '单位：美元(USD)，当价格高于此值时触发通知',
    'priceBelow': '单位：美元(USD)，当价格低于此值时触发通知',
    'marketCapBelow': '单位：K千/M百万/B十亿，当市值等于此值时触发通知',
    'changeExceeds': '单位：百分比(%)，当涨跌幅超过此值时触发通知'
  }
  return tips[form.value.conditionType] || ''
}

/** 是否显示红色提示 */
function shouldShowRedTip() {
  return ['priceAbove', 'priceBelow', 'marketCapBelow', 'changeExceeds'].includes(form.value.conditionType)
}

/** 更新市值条件值 */
function updateMarketCapConditionValue() {
  if (!marketCapValue.value || !marketCapUnit.value) {
    form.value.conditionValue = null
    return
  }
  
  const multipliers = {
    'K': 1000,
    'M': 1000000,
    'B': 1000000000
  }
  
  form.value.conditionValue = marketCapValue.value * multipliers[marketCapUnit.value]
}

/** 从条件值解析市值输入 */
function parseMarketCapValue(value) {
  if (!value) {
    marketCapValue.value = null
    marketCapUnit.value = 'M'
    return
  }
  
  if (value >= 1000000000) {
    marketCapValue.value = Number((value / 1000000000).toFixed(2))
    marketCapUnit.value = 'B'
  } else if (value >= 1000000) {
    marketCapValue.value = Number((value / 1000000).toFixed(2))
    marketCapUnit.value = 'M'
  } else if (value >= 1000) {
    marketCapValue.value = Number((value / 1000).toFixed(2))
    marketCapUnit.value = 'K'
  } else {
    marketCapValue.value = value
    marketCapUnit.value = 'K'
  }
}

/** 获取监控模式类型 */
function getAlertModeType(mode) {
  const types = {
    'timer': 'primary',
    'condition': 'success',
    'event': 'warning'
  }
  return types[mode] || 'info'
}

/** 获取监控模式文本 */
function getAlertModeText(mode) {
  const texts = {
    'timer': '定时提醒',
    'condition': '价格触发',
    'event': '事件监控'
  }
  return texts[mode] || mode
}

/** 获取事件类型文本 */
function getEventTypeText(type) {
  const texts = {
    'largeTransaction': '大额交易监控',
    'holdingChange': '持仓异动监控'
  }
  return texts[type] || type
}

/** 格式化事件配置 */
function formatEventConfig(config, type) {
  if (!config) return ''
  
  // 如果config是字符串，尝试解析为JSON
  if (typeof config === 'string') {
    try {
      config = JSON.parse(config)
    } catch (e) {
      return '配置解析失败'
    }
  }
  
  if (type === 'largeTransaction') {
    const transactionTypeText = {
      'buy': '买入',
      'sell': '卖出', 
      'both': '买入和卖出'
    }[config.transactionType] || config.transactionType
    
    return `≥${config.amountThreshold} SOL (${transactionTypeText})`
  } else if (type === 'holdingChange') {
    const changeTypeText = {
      'increase': '增加',
      'decrease': '减少',
      'both': '双向'
    }[config.changeType] || config.changeType
    
    return `前10大持仓 ${changeTypeText} ≥${config.changeThreshold}% (每${config.checkInterval}分钟)`
  }
  return ''
}

/** 处理事件类型变化 */
function handleEventTypeChange() {
  // 重置事件配置
  if (form.value.eventType === 'largeTransaction') {
    form.value.eventConfig = {
      amountThreshold: 20,
      transactionType: 'both'
    }
  } else if (form.value.eventType === 'holdingChange') {
    form.value.eventConfig = {
      changeThreshold: 10,
      changeType: 'both',
      checkInterval: 30
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.token-info {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
}

.token-name {
  font-size: 14px;
  color: #333;
  font-weight: 600;
}

.token-address {
  font-size: 11px;
  color: #999;
  font-family: monospace;
}

.chain-badge {
  margin-top: 2px;
}

.condition-detail {
  text-align: center;
}

.condition-type {
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
}

.condition-value {
  font-weight: bold;
  font-size: 13px;
}

.condition-text {
  font-size: 13px;
  color: #409EFF;
}

.notify-methods {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: center;
}

.method-tag {
  margin: 1px;
}

.notify-target {
  font-size: 12px;
}

.target-item {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 2px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.form-tip.tip-red {
  color: #f56c6c;
  font-weight: 500;
}

.monitor-detail .token-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.monitor-detail .token-detail .token-name {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}

.monitor-detail .token-detail .token-address {
  font-family: monospace;
  word-break: break-all;
  font-size: 12px;
  color: #999;
}

.text-gray-400 {
  color: #9CA3AF;
}

.event-detail {
  text-align: center;
}

.event-type {
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
  font-weight: bold;
}

.event-config {
  font-size: 11px;
  color: #E6A23C;
  line-height: 1.3;
  word-break: break-all;
}

/* 事件监控表单样式 */
.el-form-item .form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.2;
}

/* 事件监控配置区域样式 */
.el-form .el-row {
  margin-bottom: 16px;
}

/* 大额交易监控特殊样式 */
.event-detail .event-config {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 持仓异动监控样式 */
.event-detail[data-event-type="holdingChange"] .event-config {
  white-space: normal;
  word-break: break-word;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .event-config {
    font-size: 10px;
  }
}
</style>
