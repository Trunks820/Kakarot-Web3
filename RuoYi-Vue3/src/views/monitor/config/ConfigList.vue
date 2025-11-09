<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="配置名称" prop="configName">
        <el-input
          v-model="queryParams.configName"
          placeholder="请输入配置名称"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="链类型" prop="chainType">
        <el-select v-model="queryParams.chainType" placeholder="请选择链类型" clearable style="width: 150px">
          <el-option label="Solana" value="sol" />
          <el-option label="BSC" value="bsc" />
          <el-option label="Ethereum" value="eth" />
        </el-select>
      </el-form-item>
      <el-form-item label="配置类型" prop="configType">
        <el-select v-model="queryParams.configType" placeholder="请选择配置类型" clearable style="width: 150px">
          <el-option label="价格监控" value="price" />
          <el-option label="持仓监控" value="holder" />
          <el-option label="成交量监控" value="volume" />
          <el-option label="综合监控" value="composite" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="配置状态" clearable style="width: 120px">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['crypto:config:add']"
        >新增配置</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['crypto:config:remove']"
        >批量删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['crypto:config:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 数据表格 -->
    <el-table 
      v-loading="loading" 
      :data="configList" 
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="配置名称" align="left" prop="configName" min-width="180" show-overflow-tooltip>
        <template #default="scope">
          <div class="config-name">
            <el-icon v-if="scope.row.createBy === 'system'" class="system-icon"><Lock /></el-icon>
            <span>{{ scope.row.configName }}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="链类型" align="center" prop="chainType" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.chainType === 'sol'" type="success" effect="plain">
            <el-icon><Star /></el-icon> SOL
          </el-tag>
          <el-tag v-else-if="scope.row.chainType === 'bsc'" type="warning" effect="plain">
            <el-icon><Coin /></el-icon> BSC
          </el-tag>
          <el-tag v-else type="info" effect="plain">
            {{ scope.row.chainType?.toUpperCase() }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="配置类型" align="center" prop="configType" width="120">
        <template #default="scope">
          <el-tag :type="getConfigTypeTag(scope.row.configType)" size="small">
            {{ getConfigTypeLabel(scope.row.configType) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="触发逻辑" align="center" prop="triggerLogic" width="100">
        <template #default="scope">
          <el-tag v-if="scope.row.triggerLogic === 'any'" type="info" size="small">任一满足</el-tag>
          <el-tag v-else type="warning" size="small">全部满足</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="通知方式" align="center" prop="notifyMethods" width="120">
        <template #default="scope">
          <div class="notify-methods">
            <el-tooltip v-if="scope.row.notifyMethods?.includes('telegram')" content="Telegram" placement="top">
              <el-icon color="#0088cc"><ChatDotRound /></el-icon>
            </el-tooltip>
            <el-tooltip v-if="scope.row.notifyMethods?.includes('wechat')" content="微信" placement="top">
              <el-icon color="#07c160"><ChatLineRound /></el-icon>
            </el-tooltip>
            <el-tooltip v-if="scope.row.notifyMethods?.includes('email')" content="邮件" placement="top">
              <el-icon color="#d93025"><Message /></el-icon>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="版本" align="center" prop="version" width="80">
        <template #default="scope">
          <el-tag size="small" type="info" effect="plain">v{{ scope.row.version || 1 }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['crypto:config:edit']"
          />
        </template>
      </el-table-column>
      
      <el-table-column label="更新时间" align="center" prop="updateTime" width="180" sortable="custom">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-tooltip content="查看详情" placement="top">
            <el-button
              link
              type="primary"
              icon="View"
              @click="handleView(scope.row)"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="修改" placement="top">
            <el-button
              link
              type="primary"
              icon="Edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['crypto:config:edit']"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="复制" placement="top">
            <el-button
              link
              type="warning"
              icon="DocumentCopy"
              @click="handleCopy(scope.row)"
              v-hasPermi="['crypto:config:add']"
            ></el-button>
          </el-tooltip>
          <el-tooltip content="删除" placement="top">
            <el-button
              link
              type="danger"
              icon="Delete"
              @click="handleDelete(scope.row)"
              v-hasPermi="['crypto:config:remove']"
            ></el-button>
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

    <!-- 配置详情弹窗 -->
    <el-dialog v-model="detailOpen" title="配置详情" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="配置名称" :span="2">
          {{ detailData.configName }}
        </el-descriptions-item>
        <el-descriptions-item label="链类型">
          <el-tag :type="detailData.chainType === 'sol' ? 'success' : 'warning'">
            {{ detailData.chainType?.toUpperCase() }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="配置类型">
          <el-tag :type="getConfigTypeTag(detailData.configType)">
            {{ getConfigTypeLabel(detailData.configType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="时间周期">
          {{ getTimeIntervalLabel(detailData.timeInterval) }}
        </el-descriptions-item>
        <el-descriptions-item label="触发逻辑">
          {{ detailData.triggerLogic === 'any' ? '任一满足' : '全部满足' }}
        </el-descriptions-item>
        <el-descriptions-item label="事件配置" :span="2">
          <el-card shadow="never" class="events-config-card">
            <pre>{{ formatEventsConfig(detailData.eventsConfig) }}</pre>
          </el-card>
        </el-descriptions-item>
        <el-descriptions-item label="配置描述" :span="2">
          {{ detailData.description || '暂无描述' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建者">
          {{ detailData.createBy }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ parseTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新者">
          {{ detailData.updateBy }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ parseTime(detailData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关闭</el-button>
          <el-button type="primary" @click="handleUpdateFromDetail">编辑</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MonitorConfigList">
import { ref, reactive, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { listConfig, delConfig, changeConfigStatus, copyConfig } from '@/api/crypto/monitor-v2'
import { 
  Lock, Star, Coin, ChatDotRound, ChatLineRound, Message 
} from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const router = useRouter()

const loading = ref(false)
const showSearch = ref(true)
const configList = ref([])
const total = ref(0)
const detailOpen = ref(false)
const detailData = ref({})
const multiple = ref(true)
const ids = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  configName: null,
  chainType: null,
  configType: null,
  status: null,
  orderByColumn: 'update_time',
  isAsc: 'desc'
})

/** 查询配置列表 */
function getList() {
  loading.value = true
  listConfig(queryParams).then(response => {
    configList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 排序触发事件 */
function handleSortChange(column) {
  queryParams.orderByColumn = column.prop
  queryParams.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  getList()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id)
  multiple.value = !selection.length
}

/** 新增按钮操作 */
function handleAdd() {
  router.push('/monitor/config/create')
}

/** 查看详情 */
function handleView(row) {
  detailData.value = { ...row }
  detailOpen.value = true
}

/** 从详情跳转到编辑 */
function handleUpdateFromDetail() {
  detailOpen.value = false
  router.push(`/monitor/config/edit/${detailData.value.id}`)
}

/** 修改按钮操作 */
function handleUpdate(row) {
  router.push(`/monitor/config/edit/${row.id}`)
}

/** 复制按钮操作 */
function handleCopy(row) {
  proxy.$modal.confirm(`是否确认复制配置 "${row.configName}"？`).then(() => {
    return copyConfig(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('复制成功')
  }).catch(() => {})
}

/** 删除按钮操作 */
function handleDelete(row) {
  const configIds = row.id || ids.value
  const configNames = row.configName || configList.value
    .filter(item => ids.value.includes(item.id))
    .map(item => item.configName)
    .join('、')
  
  proxy.$modal.confirm(`是否确认删除配置 "${configNames}"？`).then(() => {
    return delConfig(configIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 状态修改 */
function handleStatusChange(row) {
  const text = row.status === 1 ? '启用' : '停用'
  proxy.$modal.confirm(`确认要"${text}"配置 "${row.configName}" 吗？`).then(() => {
    return changeConfigStatus(row.id, row.status)
  }).then(() => {
    proxy.$modal.msgSuccess(`${text}成功`)
  }).catch(() => {
    row.status = row.status === 0 ? 1 : 0
  })
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('crypto/monitor-v2/config/export', {
    ...queryParams
  }, `monitor_config_${new Date().getTime()}.xlsx`)
}

/** 获取配置类型标签 */
function getConfigTypeTag(type) {
  const map = {
    'price': 'danger',
    'holder': 'warning',
    'volume': 'success',
    'composite': 'primary'
  }
  return map[type] || ''
}

/** 获取配置类型标签 */
function getConfigTypeLabel(type) {
  const map = {
    'price': '价格监控',
    'holder': '持仓监控',
    'volume': '成交量',
    'composite': '综合监控'
  }
  return map[type] || type
}

/** 获取时间周期标签 */
function getTimeIntervalLabel(interval) {
  const map = {
    '1m': '1分钟',
    '5m': '5分钟',
    '15m': '15分钟',
    '1h': '1小时',
    '4h': '4小时',
    '24h': '24小时'
  }
  return map[interval] || interval
}

/** 格式化事件配置 */
function formatEventsConfig(config) {
  try {
    const obj = typeof config === 'string' ? JSON.parse(config) : config
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return config
  }
}

// 初始化
getList()
</script>

<style scoped lang="scss">
.config-name {
  display: flex;
  align-items: center;
  gap: 6px;
  
  .system-icon {
    color: #909399;
    font-size: 14px;
  }
}

.notify-methods {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  
  .el-icon {
    font-size: 18px;
    cursor: pointer;
    transition: transform 0.2s;
    
    &:hover {
      transform: scale(1.2);
    }
  }
}

.events-config-card {
  background: #f5f7fa;
  
  pre {
    margin: 0;
    font-size: 12px;
    color: #606266;
    line-height: 1.6;
  }
}
</style>

