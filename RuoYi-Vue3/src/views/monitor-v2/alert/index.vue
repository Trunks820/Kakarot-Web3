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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

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

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    // TODO: 调用告警列表API
    // const response = await listAlert(queryParams.value)
    // alertList.value = response.rows
    // total.value = response.total
    
    // 模拟数据
    alertList.value = []
    total.value = 0
    ElMessage.info('告警日志功能待开发')
  } catch (error) {
    ElMessage.error('查询失败：' + error.message)
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
const handleDetail = (row) => {
  ElMessage.info('查看详情功能待实现')
  console.log('查看告警详情:', row)
}

// 标记处理
const handleMark = (row) => {
  ElMessage.info('标记处理功能待实现')
  console.log('标记处理:', row)
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

