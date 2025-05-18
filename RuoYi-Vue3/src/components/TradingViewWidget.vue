<!-- TradingViewWidget.vue -->
<template>
  <div class="tradingview-widget-container" ref="container" style="height: 100%; width: 100%">
    <div class="tradingview-widget-container__widget" style="height: calc(100% - 32px); width: 100%"></div>
    <div class="tradingview-widget-copyright">
      <a href="https://www.tradingview.com/" rel="noopener nofollow" target="_blank">
        <span class="blue-text">Track all markets on TradingView</span>
      </a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue'
import useSettingsStore from '@/store/modules/settings'

const props = defineProps({
  symbol: {
    type: String,
    default: 'BINANCE:BTCUSDT'
  },
  interval: {
    type: String,
    default: '60'
  }
})

const container = ref(null)
const settingsStore = useSettingsStore()

const initWidget = () => {
  if (container.value) {
    // Clear existing widget
    container.value.innerHTML = `
      <div class="tradingview-widget-container__widget" style="height: calc(100% - 32px); width: 100%"></div>
      <div class="tradingview-widget-copyright">
        <a href="https://www.tradingview.com/" rel="noopener nofollow" target="_blank">
          <span class="blue-text">Track all markets on TradingView</span>
        </a>
      </div>
    `

    const script = document.createElement("script")
    script.src = "https://s3.tradingview.com/external-embedding/embed-widget-advanced-chart.js"
    script.type = "text/javascript"
    script.async = true
    
    // Force dark mode
    const isDarkMode = document.documentElement.classList.contains('dark') || settingsStore.isDark
    
    script.innerHTML = JSON.stringify({
      autosize: true,
      symbol: props.symbol,
      interval: props.interval,
      timezone: "Etc/UTC",
      theme: isDarkMode ? 'dark' : 'light',
      style: "1",
      locale: "en",
      allow_symbol_change: true,
      support_host: "https://www.tradingview.com"
    })
    
    container.value.appendChild(script)
  }
}

// Watch for symbol changes
watch(() => props.symbol, () => {
  initWidget()
})

// Watch for theme changes
watch(() => settingsStore.isDark, () => {
  initWidget()
})

// Also watch for class changes on html element
const observer = new MutationObserver(() => {
  if (document.documentElement.classList.contains('dark') !== settingsStore.isDark) {
    initWidget()
  }
})

onMounted(() => {
  initWidget()
  
  // Observe class changes on html element
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class']
  })
})

onUnmounted(() => {
  observer.disconnect()
})
</script>

<style scoped>
.tradingview-widget-container {
  background-color: var(--el-bg-color) !important;
  transition: background-color 0.3s ease-in-out;
}

.blue-text {
  color: #2962FF;
}

:deep(.tradingview-widget-copyright) {
  background-color: var(--el-bg-color) !important;
  transition: background-color 0.3s ease-in-out;
}

html.dark .tradingview-widget-container,
html.dark :deep(.tradingview-widget-copyright) {
  background-color: #1d1e1f !important;
}
</style> 