<template>
  <section class="chart-panel">
    <div class="chart-wrapper">
      <div ref="chartRef" class="chart-donut"></div>
      <div class="chart-center">
        <span class="center-value">{{ formatAmount(totalAmount) }}</span>
        <span class="center-label">{{ t.total }}</span>
      </div>
    </div>
    <div class="chart-legend">
      <div v-for="(item, i) in legendData" :key="i" class="legend-item">
        <span class="legend-dot" :style="{ background: item.color, boxShadow: `0 0 6px ${item.color}40` }"></span>
        <span class="legend-name">{{ item.name }}</span>
        <span class="legend-value">{{ formatAmount(item.value) }}</span>
      </div>
      <div v-if="legendData.length === 0" class="legend-empty">{{ t.noBills }}</div>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { useI18n } from '../i18n/locale'

const props = defineProps({
  chartData: { type: Array, default: () => [] }
})

const { language, t } = useI18n()
const chartRef = ref(null)
let chartInstance = null

const colors = ['#6c7cff', '#00e89d', '#ffb347', '#ff4d6a', '#06B6D4', '#8B5CF6', '#10B981', '#F97316']

const totalAmount = computed(() =>
  props.chartData.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
)

const legendData = computed(() =>
  props.chartData.map((item, i) => ({
    name: item.category_name || item.name,
    value: Number(item.amount) || 0,
    color: colors[i % colors.length]
  }))
)

const formatAmount = (value) => {
  const locale = language.value === 'zh' ? 'zh-CN' : 'en-US'
  const currency = language.value === 'zh' ? 'CNY' : 'USD'
  const symbol = language.value === 'zh' ? '￥' : '$'
  return symbol + Number(value).toLocaleString(locale, { maximumFractionDigits: 0 })
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)

  const seriesData = props.chartData.map((item) => ({
    name: item.category_name || item.name,
    value: Number(item.amount) || 0
  }))

  if (seriesData.length === 0) {
    chartInstance.setOption({ series: [{ type: 'pie', data: [] }] })
    return
  }

  chartInstance.setOption({
    series: [{
      type: 'pie',
      radius: ['70%', '88%'],
      center: ['50%', '50%'],
      data: seriesData,
      padAngle: 3,
      itemStyle: {
        borderColor: 'transparent',
        borderRadius: 4,
        borderWidth: 0
      },
      color: colors,
      animationDuration: 800,
      animationEasing: 'cubicOut',
      label: { show: false },
      emphasis: {
        scale: true,
        scaleSize: 6,
        label: { show: false },
        itemStyle: {
          shadowBlur: 20,
          shadowColor: 'rgba(108, 124, 255, 0.3)'
        }
      }
    }],
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(16, 21, 42, 0.92)',
      borderColor: 'rgba(108, 124, 255, 0.15)',
      borderWidth: 1,
      textStyle: { color: '#e8ecf4', fontSize: 12 },
      formatter: '{b}: {c} ({d}%)',
      extraCssText: 'backdrop-filter: blur(12px); border-radius: 8px; box-shadow: 0 8px 24px rgba(0,0,0,0.4);'
    }
  })
}

onMounted(() => {
  renderChart()
  window.addEventListener('resize', () => chartInstance?.resize())
})

onBeforeUnmount(() => {
  chartInstance?.dispose()
})

watch(
  [() => props.chartData, () => language.value],
  () => renderChart(),
  { deep: true }
)
</script>

<style scoped>
.chart-panel {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.chart-wrapper {
  position: relative;
  height: 220px;
}

.chart-donut {
  width: 100%;
  height: 100%;
}

.chart-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  pointer-events: none;
}

.center-value {
  font-size: 18px;
  font-weight: var(--weight-semibold);
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  color: var(--text-primary);
  line-height: 1.2;
  text-shadow: 0 0 16px rgba(108, 124, 255, 0.1);
}

.center-label {
  font-size: var(--text-xs);
  color: var(--text-muted);
  letter-spacing: 0.06em;
}

.chart-legend {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) 0;
  transition: all var(--motion-fast);
}

.legend-item:hover {
  padding-left: var(--space-1);
}

.legend-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.legend-name {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.legend-value {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.legend-empty {
  padding: var(--space-4) 0;
  text-align: center;
  color: var(--text-muted);
  font-size: var(--text-sm);
}
</style>
