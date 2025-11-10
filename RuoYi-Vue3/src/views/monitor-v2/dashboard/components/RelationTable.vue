<!-- 配置应用链路 - 极简表格版 -->
<template>
  <div class="relation-table-wrapper">
    <el-empty 
      v-if="!hasData"
      description="暂无数据，请先创建配置、任务和批次"
      :image-size="100"
    />
    
    <el-table 
      v-else
      :data="tableData" 
      style="width: 100%"
      :header-cell-style="{ background: 'var(--el-fill-color-light)' }"
    >
      <el-table-column prop="configName" label="配置名称" min-width="200">
        <template #default="{ row }">
          <el-tag type="success" effect="plain">
            <el-icon><Setting /></el-icon>
            {{ row.configName }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="chainType" label="链类型" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ row.chainType || '-' }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="taskCount" label="关联任务" width="120" align="center">
        <template #default="{ row }">
          <el-text v-if="row.taskCount > 0" type="warning">
            <el-icon><List /></el-icon>
            {{ row.taskCount }} 个
          </el-text>
          <el-text v-else type="info">-</el-text>
        </template>
      </el-table-column>
      
      <el-table-column prop="batchCount" label="产生批次" width="120" align="center">
        <template #default="{ row }">
          <el-text v-if="row.batchCount > 0" type="danger">
            <el-icon><Files /></el-icon>
            {{ row.batchCount }} 个
          </el-text>
          <el-text v-else type="info">-</el-text>
        </template>
      </el-table-column>
      
      <el-table-column label="任务详情" min-width="300">
        <template #default="{ row }">
          <div v-if="row.tasks && row.tasks.length > 0" class="task-list">
            <el-tag 
              v-for="task in row.tasks" 
              :key="task.name"
              size="small"
              effect="plain"
              style="margin: 2px;"
            >
              {{ task.name }}
              <span v-if="task.batchCount > 0" style="color: var(--el-color-danger); margin-left: 4px;">
                ({{ task.batchCount }}批次)
              </span>
            </el-tag>
          </div>
          <el-text v-else type="info" size="small">暂无任务</el-text>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 孤立任务提示 -->
    <el-alert
      v-if="orphanTaskCount > 0"
      :title="`发现 ${orphanTaskCount} 个未关联配置的任务`"
      type="warning"
      :closable="false"
      style="margin-top: 16px;"
    >
      <template #default>
        <div class="orphan-tasks">
          <el-tag 
            v-for="task in orphanTasks" 
            :key="task.name"
            size="small"
            type="warning"
            effect="plain"
            style="margin: 4px;"
          >
            <el-icon><Warning /></el-icon>
            {{ task.name }}
          </el-tag>
        </div>
      </template>
    </el-alert>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Setting, List, Files, Warning } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({
      nodes: [],
      links: []
    })
  }
})

// 判断是否有数据
const hasData = computed(() => {
  return props.data && props.data.nodes && props.data.nodes.length > 0
})

// 提取各类型节点
const configs = computed(() => {
  if (!props.data || !props.data.nodes) return []
  return props.data.nodes.filter(node => node.type === 'config')
})

const tasks = computed(() => {
  if (!props.data || !props.data.nodes) return []
  return props.data.nodes.filter(node => node.type === 'task')
})

const batches = computed(() => {
  if (!props.data || !props.data.nodes) return []
  return props.data.nodes.filter(node => node.type === 'batch')
})

// 构建表格数据（极简算法，性能优化）
const tableData = computed(() => {
  if (!props.data || !props.data.links) return []
  
  const links = props.data.links || []
  const result = []
  
  // 为每个配置构建一行数据
  configs.value.forEach(config => {
    const row = {
      configName: config.name,
      chainType: config.chainType,
      taskCount: 0,
      batchCount: 0,
      tasks: []
    }
    
    // 查找关联的任务
    links.forEach(link => {
      if (link.source === config.name) {
        const task = tasks.value.find(t => t.name === link.target)
        if (task) {
          row.taskCount++
          
          // 统计该任务产生的批次数量
          let taskBatchCount = 0
          links.forEach(batchLink => {
            if (batchLink.source === task.name) {
              const batch = batches.value.find(b => b.name === batchLink.target)
              if (batch) {
                taskBatchCount++
              }
            }
          })
          
          row.batchCount += taskBatchCount
          row.tasks.push({
            name: task.name,
            batchCount: taskBatchCount
          })
        }
      }
    })
    
    result.push(row)
  })
  
  return result
})

// 孤立任务（简化计算）
const orphanTasks = computed(() => {
  if (!props.data || !props.data.links) return []
  
  const links = props.data.links || []
  const linkedTaskNames = new Set()
  
  // 找出所有被配置关联的任务
  links.forEach(link => {
    const isFromConfig = configs.value.find(c => c.name === link.source)
    if (isFromConfig) {
      linkedTaskNames.add(link.target)
    }
  })
  
  // 返回未被关联的任务
  return tasks.value
    .filter(task => !linkedTaskNames.has(task.name))
    .map(task => ({ name: task.name }))
})

const orphanTaskCount = computed(() => orphanTasks.value.length)
</script>

<style scoped>
.relation-table-wrapper {
  padding: 10px;
}

.task-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.orphan-tasks {
  display: flex;
  flex-wrap: wrap;
  margin-top: 8px;
}
</style>

