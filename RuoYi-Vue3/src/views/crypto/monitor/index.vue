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
              <el-form-item label="代币符号" prop="tokenSymbol">
                <el-input 
                  v-model="queryParams.tokenSymbol" 
                  placeholder="请输入代币符号" 
                  clearable 
                  style="width: 120px" 
                  @keyup.enter="handleQuery" 
                />
              </el-form-item>
              <el-form-item label="监控模式" prop="alertMode">
                <el-select v-model="queryParams.alertMode" placeholder="监控模式" clearable style="width: 120px">
                  <el-option label="定时提醒" value="timer"></el-option>
                  <el-option label="条件触发" value="condition"></el-option>
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
              <el-col :span="1.5">
                <el-button type="warning" plain icon="VideoPlay" :disabled="multiple" @click="handleBatchEnable" v-hasPermi="['crypto:monitor:edit']">批量启用</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="info" plain icon="VideoPause" :disabled="multiple" @click="handleBatchDisable" v-hasPermi="['crypto:monitor:edit']">批量停用</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
            </el-row>

            <!-- 监控配置列表表格 -->
            <el-table v-loading="loading" :data="monitorList" @selection-change="handleSelectionChange" ref="monitorTable">
              <el-table-column type="selection" width="50" align="center" />
              
              <!-- 代币信息 -->
              <el-table-column label="代币信息" align="left" width="200" v-if="columns[0].visible">
                <template #default="scope">
                  <div class="token-info">
                    <div class="token-main">
                      <span class="token-symbol">{{ scope.row.tokenSymbol || 'N/A' }}</span>
                    </div>
                  </div>
                </template>
              </el-table-column>

              <!-- 监控模式 -->
              <el-table-column label="监控模式" align="center" width="120" v-if="columns[1].visible">
                <template #default="scope">
                  <el-tag :type="scope.row.alertMode === 'timer' ? 'primary' : 'success'">
                    {{ scope.row.alertMode === 'timer' ? '定时提醒' : '条件触发' }}
                  </el-tag>
                </template>
              </el-table-column>

              <!-- 监控条件 -->
              <el-table-column label="监控条件" align="center" width="180" v-if="columns[2].visible">
                <template #default="scope">
                  <div v-if="scope.row.alertMode === 'timer'">
                    <span class="condition-text">每 {{ scope.row.timerInterval }} 分钟</span>
                  </div>
                  <div v-else class="condition-detail">
                    <div class="condition-type">{{ getConditionTypeText(scope.row.conditionType) }}</div>
                    <div class="condition-value">{{ formatConditionValue(scope.row.conditionValue, scope.row.conditionType) }}</div>
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

              <!-- 创建时间 -->
              <el-table-column label="创建时间" align="center" width="140" v-if="columns[7].visible">
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
    <el-dialog :title="title" v-model="open" width="800px" append-to-body :close-on-click-modal="false">
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
          <el-col :span="12">
            <el-form-item label="代币符号" prop="tokenSymbol">
              <el-input v-model="form.tokenSymbol" placeholder="如：SOL" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="代币名称" prop="tokenName">
              <el-input v-model="form.tokenName" placeholder="如：Solana" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="监控模式" prop="alertMode">
              <el-radio-group v-model="form.alertMode">
                <el-radio value="timer">定时提醒</el-radio>
                <el-radio value="condition">条件触发</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="链类型" prop="chainType">
              <el-select v-model="form.chainType" placeholder="请选择链类型">
                <el-option label="SOL" value="SOL"></el-option>
                <el-option label="ETH" value="ETH"></el-option>
                <el-option label="BSC" value="BSC"></el-option>
                <el-option label="BASE" value="BASE"></el-option>
              </el-select>
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

        <!-- 条件触发配置 -->
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
              <el-input-number 
                v-model="form.conditionValue" 
                :min="0" 
                :precision="getValuePrecision()"
                placeholder="请输入阈值"
                style="width: 100%"
              />
              <div class="form-tip">{{ getConditionTip() }}</div>
            </el-form-item>
          </el-col>
        </el-row>

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
            </el-form-item>
          </el-col>
        </el-row>

        <el-row v-if="form.notifyMethodsArray.includes('telegram')" :gutter="16">
          <el-col :span="12">
            <el-form-item label="Telegram名称" prop="telegramName">
              <el-input v-model="form.telegramName" placeholder="请输入Telegram用户名" maxlength="100" />
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
          <el-button type="primary" @click="submitForm">确 定</el-button>
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
              <span class="token-symbol">{{ currentMonitor.tokenSymbol || 'N/A' }}</span>
              <span class="token-name">{{ currentMonitor.tokenName || '' }}</span>
              <div class="token-address">{{ currentMonitor.coinAddress }}</div>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="监控模式">
            <el-tag :type="currentMonitor.alertMode === 'timer' ? 'primary' : 'success'">
              {{ currentMonitor.alertMode === 'timer' ? '定时提醒' : '条件触发' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="链类型">
            <el-tag>{{ currentMonitor.chainType || 'SOL' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="监控条件" :span="2">
            <div v-if="currentMonitor.alertMode === 'timer'">
              每 {{ currentMonitor.timerInterval }} 分钟提醒一次
            </div>
            <div v-else>
              {{ getConditionTypeText(currentMonitor.conditionType) }}：
              {{ formatConditionValue(currentMonitor.conditionValue, currentMonitor.conditionType) }}
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
  batchEnableCryptoMonitor,
  batchDisableCryptoMonitor,
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

// 列显隐信息
const columns = ref([
  { key: 0, label: `代币信息`, visible: true },
  { key: 1, label: `监控模式`, visible: true },
  { key: 2, label: `监控条件`, visible: true },
  { key: 3, label: `通知方式`, visible: true },
  { key: 4, label: `通知对象`, visible: true },
  { key: 5, label: `最后通知`, visible: true },
  { key: 6, label: `状态`, visible: true },
  { key: 7, label: `创建时间`, visible: true }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    coinAddress: undefined,
    tokenSymbol: undefined,
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
      { required: true, message: "请输入Telegram名称", trigger: "blur", validator: (rule, value, callback) => {
        if (form.notifyMethodsArray && form.notifyMethodsArray.includes('telegram') && !value) {
          callback(new Error('请输入Telegram名称'))
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
  const tokenName = row.tokenSymbol || row.tokenName || row.coinAddress?.substring(0, 8)
  proxy.$modal.confirm(`确认要为"${tokenName}"发送测试通知吗？`).then(() => {
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
      // 处理通知方式
      form.value.notifyMethods = form.value.notifyMethodsArray.join(',')
      
      if (form.value.id != null) {
        updateCryptoMonitorConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addCryptoMonitorConfig(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
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

/** 批量启用 */
function handleBatchEnable() {
  if (ids.value.length === 0) {
    proxy.$modal.msgError("请选择要启用的监控配置")
    return
  }
  proxy.$modal.confirm(`确认要启用选中的 ${ids.value.length} 个监控配置吗？`).then(() => {
    return batchEnableCryptoMonitor(ids.value)
  }).then(() => {
    proxy.$modal.msgSuccess("批量启用成功")
    // 清空选择
    ids.value = []
    single.value = true
    multiple.value = true
    // 清空表格选择状态
    proxy.$refs.monitorTable?.clearSelection()
    getList()
  }).catch(() => {
    proxy.$modal.msgError("批量启用失败，请重试")
  })
}

/** 批量停用 */
function handleBatchDisable() {
  if (ids.value.length === 0) {
    proxy.$modal.msgError("请选择要停用的监控配置")
    return
  }
  proxy.$modal.confirm(`确认要停用选中的 ${ids.value.length} 个监控配置吗？`).then(() => {
    return batchDisableCryptoMonitor(ids.value)
  }).then(() => {
    proxy.$modal.msgSuccess("批量停用成功")
    // 清空选择
    ids.value = []
    single.value = true
    multiple.value = true
    // 清空表格选择状态
    proxy.$refs.monitorTable?.clearSelection()
    getList()
  }).catch(() => {
    proxy.$modal.msgError("批量停用失败，请重试")
  })
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
  form.value = {
    id: null,
    coinAddress: null,
    tokenSymbol: null,
    tokenName: null,
    alertMode: 'timer',
    timerInterval: 15,
    conditionType: null,
    conditionValue: null,
    notifyMethods: null,
    notifyMethodsArray: [],
    wechatName: null,
    telegramName: null,
    remark: null,
    chainType: 'SOL',
    status: '1'
  }
  proxy.resetForm("monitorRef")
}

/** 格式化地址 */
function formatAddress(address) {
  if (!address) return ''
  return address.length > 16 ? `${address.substring(0, 8)}...${address.substring(address.length - 8)}` : address
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
    return `${value}%`
  } else if (type === 'marketCapBelow') {
    return `$${value.toLocaleString()}`
  } else {
    return `$${value}`
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
    'priceAbove': '当价格高于此值时触发',
    'priceBelow': '当价格低于此值时触发',
    'marketCapBelow': '当市值低于此值时触发',
    'changeExceeds': '当涨跌幅超过此百分比时触发'
  }
  return tips[form.value.conditionType] || ''
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
}

.token-main {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.token-symbol {
  font-weight: bold;
  font-size: 14px;
  color: #409EFF;
}

.token-name {
  font-size: 12px;
  color: #666;
}

.token-address {
  font-size: 11px;
  color: #999;
  font-family: monospace;
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

.monitor-detail .token-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.monitor-detail .token-detail .token-address {
  font-family: monospace;
  word-break: break-all;
  font-size: 12px;
  color: #666;
}

.text-gray-400 {
  color: #9CA3AF;
}
</style>
