<!-- 告警日志完整页面 -->
<template>
  <div class="alert-manage">
    <el-card>
      <template #header>
        <div class="page-header">
          <span class="page-title">告警日志</span>
          <el-button type="primary" icon="Refresh" @click="getList">
            刷新
          </el-button>
        </div>
      </template>
      
      <!-- 搜索栏 -->
      <el-form :model="queryParams" :inline="true" class="search-form">
        <el-form-item label="Token">
          <el-input
            v-model="queryParams.tokenSymbol"
            placeholder="请输入Token符号"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="告警级别">
          <el-select v-model="queryParams.level" placeholder="请选择告警级别" clearable>
            <el-option label="高级别" value="high" />
            <el-option label="中级别" value="medium" />
            <el-option label="低级别" value="low" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
            <el-option label="已处理" value="handled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="alertList"
        stripe
        style="width: 100%"
      >
        <el-table-column label="Token" prop="tokenSymbol" min-width="120" show-overflow-tooltip />
        <el-table-column label="CA地址" prop="ca" min-width="200" show-overflow-tooltip />
        <el-table-column label="告警类型" prop="alertType" width="120">
          <template #default="scope">
            <el-tag size="small">{{ scope.row.alertType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警级别" prop="level" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.level === 'high'" type="danger" size="small">高级别</el-tag>
            <el-tag v-else-if="scope.row.level === 'medium'" type="warning" size="small">中级别</el-tag>
            <el-tag v-else type="info" size="small">低级别</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警内容" prop="message" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === 'unread'" type="danger" size="small">未读</el-tag>
            <el-tag v-else-if="scope.row.status === 'read'" type="warning" size="small">已读</el-tag>
            <el-tag v-else type="success" size="small">已处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="告警时间" prop="alertTime" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button 
              v-hasPermi="['crypto:monitor-v2:alert:query']"
              text 
              type="primary" 
              size="small"
              @click="handleDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button 
              v-hasPermi="['crypto:monitor-v2:alert:handle']"
              text 
              type="success" 
              size="small"
              @click="handleMark(scope.row)"
              :disabled="scope.row.status === 'handled'"
            >
              处理
            </el-button>
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
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="告警详情"
      width="800px"
      :close-on-click-modal="false"
      @close="closeDetailDialog"
    >
      <el-descriptions v-loading="detailDialog.loading" :column="2" border>
        <el-descriptions-item label="Token符号">
          {{ detailDialog.data.tokenSymbol }}
        </el-descriptions-item>
        <el-descriptions-item label="Token名称">
          {{ detailDialog.data.tokenName }}
        </el-descriptions-item>
        <el-descriptions-item label="CA地址" :span="2">
          {{ detailDialog.data.ca }}
        </el-descriptions-item>
        <el-descriptions-item label="告警类型">
          <el-tag size="small">{{ detailDialog.data.alertType }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警级别">
          <el-tag v-if="detailDialog.data.level === 'high'" type="danger" size="small">高级别</el-tag>
          <el-tag v-else-if="detailDialog.data.level === 'medium'" type="warning" size="small">中级别</el-tag>
          <el-tag v-else type="info" size="small">低级别</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警标题" :span="2">
          {{ detailDialog.data.alertTitle }}
        </el-descriptions-item>
        <el-descriptions-item label="触发值">
          {{ detailDialog.data.triggerValue }}
        </el-descriptions-item>
        <el-descriptions-item label="市值">
          {{ detailDialog.data.marketCap }}
        </el-descriptions-item>
        <el-descriptions-item label="通知状态">
          <el-tag :type="detailDialog.data.notifyStatus === 'sent' ? 'success' : 'info'" size="small">
            {{ detailDialog.data.notifyStatus }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="通知渠道">
          {{ detailDialog.data.notifyChannels }}
        </el-descriptions-item>
        <el-descriptions-item label="告警时间" :span="2">
          {{ detailDialog.data.createTime }}
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag v-if="detailDialog.data.status === 'unread'" type="danger" size="small">未读</el-tag>
          <el-tag v-else-if="detailDialog.data.status === 'read'" type="warning" size="small">已读</el-tag>
          <el-tag v-else type="success" size="small">已处理</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人" v-if="detailDialog.data.handleBy">
          {{ detailDialog.data.handleBy }}
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" v-if="detailDialog.data.handleTime" :span="2">
          {{ detailDialog.data.handleTime }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" v-if="detailDialog.data.handleRemark" :span="2">
          {{ detailDialog.data.handleRemark }}
        </el-descriptions-item>
        <el-descriptions-item label="告警内容" :span="2">
          <pre style="margin: 0; white-space: pre-wrap;">{{ detailDialog.data.alertContent }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <el-button @click="closeDetailDialog">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAlert, getAlert, markAlertHandled } from '@/api/crypto/monitor-v2'
import Pagination from '@/components/Pagination'

const loading = ref(false)
const alertList = ref([])
const total = ref(0)

const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  tokenSymbol: undefined,
  level: undefined,
  status: undefined
})

// 详情对话框
const detailDialog = ref({
  visible: false,
  loading: false,
  data: {}
})

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const response = await listAlert(queryParams.value)
    alertList.value = response.rows
    total.value = response.total
  } catch (error) {
    ElMessage.error('查询失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

// 重置
const resetQuery = () => {
  queryParams.value = {
    pageNum: 1,
    pageSize: 10,
    tokenSymbol: undefined,
    level: undefined,
    status: undefined
  }
  getList()
}

// 详情
const handleDetail = async (row) => {
  detailDialog.value.visible = true
  detailDialog.value.loading = true
  try {
    const response = await getAlert(row.id)
    detailDialog.value.data = response.data
  } catch (error) {
    ElMessage.error('获取详情失败：' + (error.message || '未知错误'))
    detailDialog.value.visible = false
  } finally {
    detailDialog.value.loading = false
  }
}

// 关闭详情对话框
const closeDetailDialog = () => {
  detailDialog.value.visible = false
  detailDialog.value.data = {}
}

// 标记处理
const handleMark = (row) => {
  ElMessageBox.prompt('请输入处理备注（可选）', '标记为已处理', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /.*/,
    inputType: 'textarea'
  }).then(async ({ value }) => {
    try {
      await markAlertHandled(row.id, { handleRemark: value || '' })
      ElMessage.success('标记成功')
      getList()
    } catch (error) {
      ElMessage.error('标记失败：' + (error.message || '未知错误'))
    }
  }).catch(() => {
    // 取消操作
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped lang="scss">
.alert-manage {
  padding: 20px;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .page-title {
      font-size: 18px;
      font-weight: 600;
    }
  }
  
  .search-form {
    margin-bottom: 20px;
  }
}
</style>

