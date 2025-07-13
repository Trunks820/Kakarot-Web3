<template>
  <div class="app-container">
    <el-row :gutter="20">
      <splitpanes :horizontal="appStore.device === 'mobile'" class="default-theme">
        <pane size="84">
          <el-col>
            <!-- 搜索栏 -->
            <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
              <el-form-item label="钱包地址" prop="walletAddress">
                <el-input v-model="queryParams.walletAddress" placeholder="请输入钱包地址" clearable style="width: 240px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="钱包备注" prop="walletName">
                <el-input v-model="queryParams.walletName" placeholder="请输入钱包备注" clearable style="width: 240px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item label="监控状态" prop="monitorState">
                <el-select v-model="queryParams.monitorState" placeholder="监控状态" clearable style="width: 120px">
                  <el-option label="已监控" value="1"></el-option>
                  <el-option label="未监控" value="0"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="操作类型" prop="operationType">
                <el-input v-model="queryParams.operationType" placeholder="请输入操作类型" clearable style="width: 240px" @keyup.enter="handleQuery" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
                <el-button icon="Refresh" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>

            <!-- 操作按钮 -->
            <el-row :gutter="10" class="mb8">
              <el-col :span="1.5">
                <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['crypto:wallet:add']">新增</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['crypto:wallet:edit']">修改</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['crypto:wallet:remove']">删除</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="warning" plain icon="VideoPlay" :disabled="multiple" @click="handleBatchEnable" v-hasPermi="['crypto:wallet:edit']">一键启用</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="info" plain icon="VideoPause" :disabled="multiple" @click="handleBatchDisable" v-hasPermi="['crypto:wallet:edit']">一键禁用</el-button>
              </el-col>
              <el-col :span="1.5">
                <el-button type="info" plain icon="Upload" @click="handleImport" v-hasPermi="['crypto:wallet:import']">导入</el-button>
              </el-col>
              <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
            </el-row>

            <!-- 钱包列表表格 -->
            <el-table v-loading="loading" :data="walletList" @selection-change="handleSelectionChange" ref="walletTable">
              <el-table-column type="selection" width="50" align="center" />
              <el-table-column label="钱包地址" align="center" key="walletAddress" prop="walletAddress" v-if="columns[0].visible" :show-overflow-tooltip="true" width="400" />
              <el-table-column label="钱包备注" align="center" key="walletName" prop="walletName" v-if="columns[1].visible" :show-overflow-tooltip="true" />
              <el-table-column label="链类型" align="center" key="chainType" prop="chainType" v-if="columns[2].visible" width="80">
                <template #default="scope">
                  <el-tag :type="getChainTypeColor(scope.row.chainType)">{{ scope.row.chainType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作类型" align="center" key="operationType" prop="operationType" v-if="columns[3].visible" width="120" :show-overflow-tooltip="true" />
              <el-table-column label="监控状态" align="center" key="monitorState" v-if="columns[4].visible" width="100">
                <template #default="scope">
                  <el-switch
                      v-model="scope.row.monitorState"
                      :active-value="1"
                      :inactive-value="0"
                      @change="handleStatusChange(scope.row)"
                  ></el-switch>
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
                <template #default="scope">
                  <el-tooltip content="修改" placement="top">
                    <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['crypto:wallet:edit']"></el-button>
                  </el-tooltip>
                  <el-tooltip content="删除" placement="top">
                    <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['crypto:wallet:remove']"></el-button>
                  </el-tooltip>
                  <el-tooltip content="查看活动" placement="top">
                    <el-button link type="primary" icon="Money" @click="handleViewTransactions(scope.row)" v-hasPermi="['crypto:wallet:transaction']"></el-button>
                  </el-tooltip>
                </template>
              </el-table-column>
            </el-table>

            <!-- 分页 -->
            <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
          </el-col>
        </pane>
      </splitpanes>
    </el-row>

    <!-- 添加或修改钱包配置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body :close-on-click-modal="true">
      <el-form :model="form" :rules="rules" ref="walletRef" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="钱包地址" prop="walletAddress">
              <el-input v-model="form.walletAddress" placeholder="请输入钱包地址" maxlength="255" :disabled="form.walletAddress && title.includes('修改')" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="钱包备注" prop="walletName">
              <el-input v-model="form.walletName" placeholder="请输入钱包备注" maxlength="100" />
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
        <el-row>
          <el-col :span="12">
            <el-form-item label="监控状态" prop="monitorState">
              <el-radio-group v-model="form.monitorState">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="操作类型" prop="operationType">
              <el-input v-model="form.operationType" placeholder="请输入操作类型备注" maxlength="100" />
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



    <!-- 钱包导入对话框 -->
    <el-dialog :title="upload.title" v-model="upload.open" width="400px" append-to-body :close-on-click-modal="true">
      <el-upload ref="uploadRef" :limit="1" accept=".xlsx, .xls" :headers="upload.headers" :action="upload.url + '?updateSupport=' + upload.updateSupport" :disabled="upload.isUploading" :on-progress="handleFileUploadProgress" :on-success="handleFileSuccess" :auto-upload="false" drag>
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip text-center">
            <span>仅允许导入xls、xlsx格式文件。</span>
            <el-link type="primary" :underline="false" style="font-size: 12px; vertical-align: baseline" @click="importTemplate">下载模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="upload.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 钱包交易活动弹窗 -->
    <el-dialog 
      :title="`${currentWallet.walletName || currentWallet.walletAddress} - 交易活动`" 
      v-model="activityDialogVisible" 
      width="1200px" 
      append-to-body
      :close-on-click-modal="true"
    >
      <el-table v-loading="activityLoading" :data="activityList" max-height="600">
        <el-table-column label="时间" width="160">
          <template #default="scope">
            {{ formatTimestamp(scope.row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.event_type === 'buy' ? 'success' : 'danger'">
              {{ scope.row.event_type === 'buy' ? '买入' : '卖出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="代币" width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <img v-if="scope.row.token.logo" :src="scope.row.token.logo" 
                   style="width: 24px; height: 24px; margin-right: 8px; border-radius: 50%;" 
                   @error="$event.target.style.display='none'">
              <div>
                <div style="font-weight: bold;">{{ scope.row.token.symbol }}</div>
                <div style="font-size: 12px; color: #666;">{{ scope.row.token.address.substring(0, 8) }}...</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="数量" width="150" align="right">
          <template #default="scope">
            {{ formatNumber(scope.row.token_amount) }}
          </template>
        </el-table-column>
        <el-table-column label="价格 (USD)" width="120" align="right">
          <template #default="scope">
            ${{ formatPrice(scope.row.price_usd) }}
          </template>
        </el-table-column>
        <el-table-column label="金额 (USD)" width="150" align="right">
          <template #default="scope">
            <span :class="scope.row.event_type === 'buy' ? 'text-red-500' : 'text-green-500'">
              {{ scope.row.event_type === 'buy' ? '-' : '+' }}${{ formatNumber(scope.row.cost_usd) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="Gas费 (USD)" width="120" align="right">
          <template #default="scope">
            ${{ formatNumber(scope.row.gas_usd) }}
          </template>
        </el-table-column>
        <el-table-column label="交易哈希" width="150">
          <template #default="scope">
            <el-link type="primary" :href="getExplorerUrl(scope.row.tx_hash, scope.row.chain)" target="_blank">
              {{ scope.row.tx_hash.substring(0, 8) }}...
            </el-link>
          </template>
        </el-table-column>
      </el-table>
      
      <div v-if="!activityLoading && activityList.length === 0" 
           style="text-align: center; padding: 40px; color: #666;">
        暂无交易活动数据
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="activityDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="CryptoWallet">
import { getToken } from "@/utils/auth"
import useAppStore from '@/store/modules/app'
import { listWallets, getWallet, addWallet, updateWallet, delWallet, batchUpdateWalletStatus} from "@/api/crypto/wallet"
import { getWalletActivity } from "@/api/crypto/activity"
import { Splitpanes, Pane } from "splitpanes"
import "splitpanes/dist/splitpanes.css"

const router = useRouter()
const appStore = useAppStore()
const { proxy } = getCurrentInstance()

const walletList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const dateRange = ref([])

// 交易活动相关
const activityDialogVisible = ref(false)
const currentWallet = ref({})
const activityList = ref([])
const activityLoading = ref(false)

/*** 钱包导入参数 */
const upload = reactive({
  // 是否显示弹出层（钱包导入）
  open: false,
  // 弹出层标题（钱包导入）
  title: "",
  // 是否禁用上传
  isUploading: false,
  // 是否更新已经存在的钱包数据
  updateSupport: 0,
  // 设置上传的请求头部
  headers: { Authorization: "Bearer " + getToken() },
  // 上传的地址
  url: import.meta.env.VITE_APP_BASE_API + "/crypto/wallet/importData"
})

// 列显隐信息
const columns = ref([
  { key: 0, label: `钱包地址`, visible: true },
  { key: 1, label: `钱包备注`, visible: true },
  { key: 2, label: `链类型`, visible: true },
  { key: 3, label: `操作类型`, visible: true },
  { key: 4, label: `监控状态`, visible: true },
  { key: 5, label: `最后活跃时间`, visible: true },
  { key: 6, label: `创建时间`, visible: true }
])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    walletAddress: undefined,
    walletName: undefined,
    chainType: undefined,
    operationType: undefined,
    monitorState: undefined
  },
  rules: {
    walletAddress: [
      { required: true, message: "钱包地址不能为空", trigger: "blur" },
    ],
    walletName: [
      { max: 100, message: "钱包备注长度不能超过20个字符", trigger: "blur" }
    ],
    chainType: [
      { required: true, message: "请选择链类型", trigger: "change" }
    ],
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 获取链类型颜色 */
function getChainTypeColor(chainType) {
  const colorMap = {
    'SOL': 'success',
    'ETH': 'primary',
    'BSC': 'warning',
    'BASE': 'info'
  }
  return colorMap[chainType] || 'default'
}



/** 查询钱包列表 */
function getList() {
  loading.value = true
  listWallets(proxy.addDateRange(queryParams.value, dateRange.value)).then(res => {
    loading.value = false
    walletList.value = res.rows
    total.value = res.total
    // 数据刷新后清空选择状态
    ids.value = []
    single.value = true
    multiple.value = true
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
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download("crypto/wallet/export", {
    ...queryParams.value,
  },`wallet_${new Date().getTime()}.xlsx`)
}

/** 钱包监控状态修改  */
function handleStatusChange(row) {
  let text = row.monitorState === 1 ? "启用监控" : "禁用监控"
  proxy.$modal.confirm('确认要"' + text + '"钱包"' + (row.walletName || row.walletAddress) + '"吗?').then(function () {
    return updateWallet(row)
  }).then(() => {
    proxy.$modal.msgSuccess(text + "成功")
  }).catch(function () {
    // 如果操作失败，恢复原始状态
    row.monitorState = row.monitorState === 1 ? 0 : 1
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  let deleteIds
  let confirmText
  
  // 判断是否是钱包数据对象（有id和walletAddress属性）还是事件对象
  if (row && row.id && row.walletAddress) {
    // 单个删除
    deleteIds = row.id
    confirmText = row.walletName || row.walletAddress
  } else {
    // 批量删除
    if (!ids.value || ids.value.length === 0) {
      proxy.$modal.msgError("请先选择要删除的钱包")
      return
    }
    deleteIds = ids.value
    confirmText = `${ids.value.length}个钱包`
  }
  
  proxy.$modal.confirm('是否确认删除"' + confirmText + '"？').then(function () {
    return delWallet(deleteIds)
  }).then(() => {
    // 清空选择
    ids.value = []
    single.value = true
    multiple.value = true
    // 清空表格选择状态
    proxy.$refs.walletTable?.clearSelection()
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}



/** 查看钱包交易活动 */
function handleViewTransactions(row) {
  // 打开交易活动弹窗
  currentWallet.value = row
  activityDialogVisible.value = true
  getWalletActivityData(row.walletAddress, row.chainType)
}

/** 获取钱包交易活动数据 */
function getWalletActivityData(address, chainType) {
  activityLoading.value = true
  activityList.value = []
  
  getWalletActivity(address, chainType).then(response => {
    activityLoading.value = false
    if (response.code === 200 && response.data) {
      activityList.value = Array.isArray(response.data) ? response.data : [response.data]
    } else {
      activityList.value = []
      proxy.$modal.msgWarning("暂无交易活动数据")
    }
  }).catch(error => {
    activityLoading.value = false
    activityList.value = []
    proxy.$modal.msgError("获取交易活动失败")
  })
}

/** 选择条数  */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 导入按钮操作 */
function handleImport() {
  upload.title = "钱包导入"
  upload.open = true
}

/** 下载模板操作 */
function importTemplate() {
  proxy.download("crypto/wallet/importTemplate", {
  }, `wallet_template_${new Date().getTime()}.xlsx`)
}

/**文件上传中处理 */
const handleFileUploadProgress = (event, file, fileList) => {
  upload.isUploading = true
}

/** 文件上传成功处理 */
const handleFileSuccess = (response, file, fileList) => {
  upload.open = false
  upload.isUploading = false
  proxy.$refs["uploadRef"].handleRemove(file)
  proxy.$alert("<div style='overflow: auto;overflow-x: hidden;max-height: 70vh;padding: 10px 20px 0;'>" + response.msg + "</div>", "导入结果", { dangerouslyUseHTMLString: true })
  getList()
}

/** 提交上传文件 */
function submitFileForm() {
  proxy.$refs["uploadRef"].submit()
}

/** 重置操作表单 */
function reset() {
  form.value = {
    walletAddress: undefined,
    walletName: undefined,
    chainType: "SOL",
    operationType: undefined, // 操作类型备注
    monitorState: 0 // 默认不启用监控
  }
  proxy.resetForm("walletRef")
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加钱包"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id || ids.value[0]
  getWallet(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改钱包"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["walletRef"].validate(valid => {
    if (valid) {
      if (form.value.walletAddress && title.value.includes('修改')) {
        updateWallet(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addWallet(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 格式化时间戳 */
function formatTimestamp(timestamp) {
  const date = new Date(timestamp * 1000)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

/** 格式化数字 */
function formatNumber(num) {
  if (!num) return '0'
  const number = parseFloat(num)
  if (number >= 1000000) {
    return (number / 1000000).toFixed(2) + 'M'
  } else if (number >= 1000) {
    return (number / 1000).toFixed(2) + 'K'
  } else if (number < 0.01 && number > 0) {
    return number.toExponential(2)
  } else {
    return number.toFixed(2)
  }
}

/** 格式化价格 */
function formatPrice(price) {
  if (!price) return '0'
  const number = parseFloat(price)
  if (number < 0.000001 && number > 0) {
    return number.toExponential(4)
  } else if (number < 0.01 && number > 0) {
    return number.toFixed(6)
  } else {
    return number.toFixed(4)
  }
}

/** 获取区块链浏览器链接 */
function getExplorerUrl(txHash, chain) {
  const explorers = {
    'sol': `https://solscan.io/tx/${txHash}`,
    'eth': `https://etherscan.io/tx/${txHash}`,
    'bsc': `https://bscscan.com/tx/${txHash}`,
    'base': `https://basescan.org/tx/${txHash}`
  }
  return explorers[chain.toLowerCase()] || `https://solscan.io/tx/${txHash}`
}

/** 一键启用按钮操作 */
function handleBatchEnable() {
  if (!ids.value || ids.value.length === 0) {
    proxy.$modal.msgError("请先选择要启用的钱包")
    return
  }
  
  const selectedWallets = walletList.value.filter(wallet => ids.value.includes(wallet.id))
  const walletNames = selectedWallets.map(w => w.walletName || w.walletAddress).join('、')
  
  proxy.$modal.confirm(`确认要启用监控 ${selectedWallets.length} 个钱包（${walletNames}）吗？`).then(function () {
    // 调用批量更新API，一次请求完成所有更新
    batchUpdateWalletStatus(ids.value, 1).then(() => {
      proxy.$modal.msgSuccess(`成功启用 ${selectedWallets.length} 个钱包的监控`)
      // 清空选择
      ids.value = []
      single.value = true
      multiple.value = true
      // 清空表格选择状态
      proxy.$refs.walletTable?.clearSelection()
      getList()
    }).catch(() => {
      proxy.$modal.msgError("批量启用失败，请重试")
    })
  }).catch(() => {})
}

/** 一键禁用按钮操作 */
function handleBatchDisable() {
  if (!ids.value || ids.value.length === 0) {
    proxy.$modal.msgError("请先选择要禁用的钱包")
    return
  }
  
  const selectedWallets = walletList.value.filter(wallet => ids.value.includes(wallet.id))
  const walletNames = selectedWallets.map(w => w.walletName || w.walletAddress).join('、')
  
  proxy.$modal.confirm(`确认要禁用监控 ${selectedWallets.length} 个钱包（${walletNames}）吗？`).then(function () {
    // 调用批量更新API，一次请求完成所有更新
    batchUpdateWalletStatus(ids.value, 0).then(() => {
      proxy.$modal.msgSuccess(`成功禁用 ${selectedWallets.length} 个钱包的监控`)
      // 清空选择
      ids.value = []
      single.value = true
      multiple.value = true
      // 清空表格选择状态
      proxy.$refs.walletTable?.clearSelection()
      getList()
    }).catch(() => {
      proxy.$modal.msgError("批量禁用失败，请重试")
    })
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.el-table .el-table__cell {
  padding: 8px 0;
}

.el-descriptions {
  margin-top: 20px;
}

.el-descriptions :deep(.el-descriptions__label) {
  font-weight: bold;
}

.text-red-500 {
  color: #ef4444;
}

.text-green-500 {
  color: #22c55e;
}
</style>
