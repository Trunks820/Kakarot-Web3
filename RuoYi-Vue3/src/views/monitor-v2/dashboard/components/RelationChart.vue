<!-- 配置-任务-批次应用链路（简化版） -->
<template>
  <div class="relation-simple-wrapper">
    <el-empty 
      v-if="!hasData"
      description="暂无数据，请先创建配置、任务和批次"
      :image-size="120"
    />
    <div v-else class="simple-content">
      <!-- 统计信息 -->
      <div class="summary-bar">
        <el-tag type="success">配置: {{ configs.length }}</el-tag>
        <el-tag type="warning">任务: {{ tasks.length }}</el-tag>
        <el-tag type="danger">批次: {{ batches.length }}</el-tag>
      </div>
      
      <!-- 链路列表 -->
      <div class="chain-list">
        <div v-for="chain in relationChains" :key="chain.config.id" class="chain-row">
          <!-- 配置 -->
          <div class="chain-config">
            <el-icon class="node-icon config-icon"><Setting /></el-icon>
            <span class="node-name">{{ chain.config.name }}</span>
            <el-tag size="small" type="success">{{ chain.tasks.length }}个任务</el-tag>
          </div>
          
          <!-- 任务列表 -->
          <div v-if="chain.tasks.length > 0" class="chain-tasks">
            <div v-for="taskItem in chain.tasks" :key="taskItem.task.id" class="task-row">
              <div class="arrow">└─</div>
              <el-icon class="node-icon task-icon"><List /></el-icon>
              <span class="node-name">{{ taskItem.task.name }}</span>
              <el-tag v-if="taskItem.batches.length > 0" size="small" type="warning">
                {{ taskItem.batches.length }}个批次
              </el-tag>
            </div>
          </div>
          <div v-else class="no-data">暂无任务</div>
        </div>
        
        <!-- 孤立任务 -->
        <div v-if="orphanTasks.length > 0" class="orphan-tasks">
          <div class="orphan-title">
            <el-icon><Warning /></el-icon>
            <span>未关联配置的任务 ({{ orphanTasks.length }})</span>
          </div>
          <div v-for="taskItem in orphanTasks" :key="taskItem.task.id" class="task-row orphan">
            <el-icon class="node-icon task-icon"><List /></el-icon>
            <span class="node-name">{{ taskItem.task.name }}</span>
            <el-tag v-if="taskItem.batches.length > 0" size="small" type="info">
              {{ taskItem.batches.length }}个批次
            </el-tag>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, watch, onMounted } from 'vue'
import { Setting, List, Warning } from '@element-plus/icons-vue'

const props = defineProps({
  data: {
    type: Object,
    default: () => ({
      nodes: [],
      links: []
    })
  }
})

// 性能监控
let renderStartTime = 0
onMounted(() => {
  console.log('    ├─ RelationChart组件挂载完成')
})

watch(() => props.data, (newData) => {
  renderStartTime = performance.now()
  console.log('    ├─ RelationChart开始处理数据...')
  
  // 等待DOM真正渲染完成（使用requestAnimationFrame）
  requestAnimationFrame(() => {
    requestAnimationFrame(() => {
      const renderTime = (performance.now() - renderStartTime).toFixed(0)
      console.log(`    └─ RelationChart真实渲染完成: ${renderTime}ms ${renderTime > 1000 ? '⚠️ 慢!' : ''}`)
    })
  })
}, { immediate: true })

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

// 构建配置 -> 任务 -> 批次的链路关系
const relationChains = computed(() => {
  const computeStart = performance.now()
  
  if (!props.data || !props.data.links) return []
  
  const chains = []
  const links = props.data.links || []
  
  // 为每个配置构建链路
  configs.value.forEach(config => {
    const chain = {
      config: config,
      tasks: [],
      totalBatches: 0
    }
    
    // 查找关联的任务
    const configTaskLinks = links.filter(link => 
      link.source === config.name && tasks.value.find(t => t.name === link.target)
    )
    
    configTaskLinks.forEach(link => {
      const task = tasks.value.find(t => t.name === link.target)
      if (task) {
        // 查找任务产生的批次
        const taskBatchLinks = links.filter(l => 
          l.source === task.name && batches.value.find(b => b.name === l.target)
        )
        
        const taskBatches = taskBatchLinks
          .map(l => batches.value.find(b => b.name === l.target))
          .filter(b => b)
        
        chain.tasks.push({
          task: task,
          batches: taskBatches
        })
        
        chain.totalBatches += taskBatches.length
      }
    })
    
    chains.push(chain)
  })
  
  const computeTime = (performance.now() - computeStart).toFixed(0)
  if (computeTime > 100) {
    console.warn(`    ⚠️ relationChains计算: ${computeTime}ms`)
  }
  
  return chains
})

// 孤立的任务（没有配置关联）
const orphanTasks = computed(() => {
  const computeStart = performance.now()
  
  if (!props.data || !props.data.links) return []
  
  const links = props.data.links || []
  const linkedTaskNames = new Set(
    links
      .filter(link => configs.value.find(c => c.name === link.source))
      .map(link => link.target)
  )
  
  const result = tasks.value
    .filter(task => !linkedTaskNames.has(task.name))
    .map(task => {
      // 查找孤立任务产生的批次
      const taskBatchLinks = links.filter(l => 
        l.source === task.name && batches.value.find(b => b.name === l.target)
      )
      
      const taskBatches = taskBatchLinks
        .map(l => batches.value.find(b => b.name === l.target))
        .filter(b => b)
      
      return {
        task: task,
        batches: taskBatches
      }
    })
  
  const computeTime = (performance.now() - computeStart).toFixed(0)
  if (computeTime > 100) {
    console.warn(`    ⚠️ orphanTasks计算: ${computeTime}ms`)
  }
  
  return result
})
</script>

<style scoped>
.relation-simple-wrapper {
  width: 100%;
  min-height: 300px;
  padding: 20px;
}

.simple-content {
  max-height: 600px;
  overflow-y: auto;
}

/* 统计栏 */
.summary-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
}

/* 链路列表 */
.chain-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 每条链路 */
.chain-row {
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
}

/* 配置行 */
.chain-config {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  background: var(--el-bg-color);
  border-radius: 6px;
  margin-bottom: 8px;
}

.node-icon {
  font-size: 18px;
}

.config-icon {
  color: #67C23A;
}

.task-icon {
  color: #E6A23C;
}

.node-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

/* 任务列表 */
.chain-tasks {
  margin-left: 20px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: var(--el-bg-color);
  border-radius: 4px;
}

.arrow {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  font-family: monospace;
}

.no-data {
  margin-left: 30px;
  padding: 8px;
  color: var(--el-text-color-placeholder);
  font-size: 12px;
}

/* 孤立任务 */
.orphan-tasks {
  margin-top: 20px;
  padding: 12px;
  background: var(--el-color-warning-light-9);
  border-radius: 8px;
  border: 1px dashed var(--el-color-warning);
}

.orphan-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-color-warning-dark-2);
}

.task-row.orphan {
  background: var(--el-bg-color);
}

/* 滚动条 */
.simple-content::-webkit-scrollbar {
  width: 6px;
}

.simple-content::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
}

.simple-content::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-darker);
}
</style>

