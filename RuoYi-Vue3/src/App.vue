<template>
  <router-view />
</template>

<script setup>
import useSettingsStore from '@/store/modules/settings'
import { handleThemeStyle } from '@/utils/theme'

onMounted(() => {
  nextTick(() => {
    // 初始化主题样式
    handleThemeStyle(useSettingsStore().theme)
  })
})
</script>

<style>
/* 全局样式覆盖 - 亮模式 */
:root {
  --el-card-bg-color: #ffffff;
  --el-bg-color: #ffffff;
  --el-bg-color-overlay: #ffffff;
  --el-text-color-primary: #303133;
  --el-text-color-regular: #606266;
  --el-border-color: #dcdfe6;
  --el-border-color-light: #e4e7ed;
}

/* 全局样式覆盖 - 暗黑模式 */
html.dark {
  --el-card-bg-color: #1d1e1f !important;
  --el-bg-color: #141414 !important;
  --el-bg-color-overlay: #1d1e1f !important;
  --el-text-color-primary: #ffffff !important;
  --el-text-color-regular: #d0d0d0 !important;
  --el-border-color: #434343 !important;
  --el-border-color-light: #434343 !important;
}

/* 强制卡片样式 - 亮模式 */
.el-card {
  background-color: #ffffff;
  color: #303133;
  border-color: #e4e7ed;
}

.el-card .el-card__header {
  background-color: #f5f7fa;
  color: #303133;
  border-color: #e4e7ed;
}

/* 强制卡片样式 - 暗黑模式 */
html.dark .el-card {
  background-color: #1d1e1f !important;
  color: #ffffff !important; 
  border-color: #434343 !important;
}

html.dark .el-card .el-card__header {
  background-color: #141414 !important;
  color: #ffffff !important;
  border-color: #434343 !important;
}

/* 数据卡片组件覆盖 */
html.dark .data-card {
  background-color: #1d1e1f !important;
  border-color: #434343 !important;
}

html.dark .data-card .card-header {
  background-color: #141414 !important;
  border-color: #434343 !important;
}

html.dark .data-card .card-header span {
  color: #ffffff !important;
}

html.dark .data-card .card-body {
  background-color: #1d1e1f !important;
}

html.dark .data-card .stat-item h3 {
  color: #ffffff !important;
}

html.dark .data-card .stat-item .label {
  color: #d0d0d0 !important;
}

/* 市场图表组件覆盖 */
html.dark .market-chart {
  background-color: #1d1e1f !important;
  border-color: #434343 !important;
}

html.dark .market-chart .el-card__header {
  background-color: #141414 !important;
  border-color: #434343 !important;
}

html.dark .market-chart .card-header span {
  color: #ffffff !important;
}

html.dark .market-chart .chart-container {
  background-color: #1d1e1f !important;
}
</style>
