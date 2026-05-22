<template>
  <div class="kpi-strip">
    <MetricCard
      v-for="(metric, index) in enrichedMetrics"
      :key="metric.key"
      :metric="metric"
      :divided="index < enrichedMetrics.length - 1"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, reactive, watch } from 'vue'
import { useI18n } from '../i18n/locale'
import MetricCard from './MetricCard.vue'

const props = defineProps({
  metrics: { type: Array, required: true }
})

const { language } = useI18n()
const animated = reactive({})
const frameIds = {}

const getDisplayParts = (value, isPercentage) => {
  if (isPercentage) {
    return [{ type: 'integer', value: String(Math.round(value)) }, { type: 'literal', value: '%' }]
  }
  const locale = language.value === 'zh' ? 'zh-CN' : 'en-US'
  const currency = language.value === 'zh' ? 'CNY' : 'USD'
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
    maximumFractionDigits: 2
  }).formatToParts(value)
}

const animateTo = (key, targetValue) => {
  const nextTarget = Number(targetValue) || 0
  if (!(key in animated)) {
    animated[key] = nextTarget
    return
  }
  const startValue = animated[key]
  const startTime = performance.now()
  const duration = 600

  if (frameIds[key]) cancelAnimationFrame(frameIds[key])

  const tick = (timestamp) => {
    const progress = Math.min((timestamp - startTime) / duration, 1)
    const eased = 1 - (1 - progress) * (1 - progress)
    animated[key] = startValue + (nextTarget - startValue) * eased
    if (progress < 1) {
      frameIds[key] = requestAnimationFrame(tick)
    } else {
      animated[key] = nextTarget
    }
  }

  frameIds[key] = requestAnimationFrame(tick)
}

watch(
  () => props.metrics,
  (newMetrics) => {
    newMetrics.forEach((m) => {
      if (!(m.key in animated)) animated[m.key] = Number(m.value) || 0
      animateTo(m.key, m.value)
    })
  },
  { immediate: true, deep: true }
)

watch(
  () => language.value,
  () => { /* trigger reactivity */ }
)

const enrichedMetrics = computed(() =>
  props.metrics.map((m) => ({
    ...m,
    _displayParts: getDisplayParts(animated[m.key] ?? Number(m.value) ?? 0, m.isPercentage)
  }))
)

onBeforeUnmount(() => {
  Object.values(frameIds).forEach((id) => cancelAnimationFrame(id))
})
</script>

<style scoped>
.kpi-strip {
  display: flex;
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-xl);
  overflow: hidden;
  position: relative;
}

.kpi-strip::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(108, 124, 255, 0.2), transparent);
  pointer-events: none;
}

@media (max-width: 720px) {
  .kpi-strip {
    flex-direction: column;
  }
}
</style>
