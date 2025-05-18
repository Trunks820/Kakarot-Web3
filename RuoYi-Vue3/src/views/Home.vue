<template>
  <div class="app-container home">
    <!-- 顶部数据卡片 -->
    <DataSummary />

    <!-- 主要内容区域 -->
    <el-row :gutter="12" class="mt20">
      <!-- 左侧K线图面板 -->
      <el-col :span="17">
        <MarketChart />
      </el-col>

      <!-- 右侧热门CA列表 -->
      <el-col :span="7">
        <PopularCA />
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Home">
import DataSummary from './components/DataSummary.vue'
import MarketChart from './components/MarketChart.vue'
import PopularCA from './components/PopularCA.vue'
</script>

<style scoped lang="scss">
.home {
  .mt20 {
    margin-top: 20px;
  }
}
</style> 