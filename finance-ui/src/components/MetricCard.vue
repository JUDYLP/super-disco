<template>
  <div
    class="metric-card"
    :class="{
      'metric-card--divided': divided,
      [`metric-card--${metric.tone}`]: true,
      'metric-card--pulse': isPulsing,
      'metric-card--bounce': isBouncing
    }"
    @animationend="onAnimationEnd"
  >
    <div class="metric-glow"></div>
    <div class="metric-ripple-layer"></div>
    <span class="metric-label">{{ metric.label }}</span>
    <strong :class="['metric-value', `metric-value--${metric.tone}`]">
      <template v-for="part in metric._displayParts" :key="part.type">
        <span v-if="part.type === 'currency' || part.type === 'minusSign'" class="metric-currency">{{ part.value }}</span>
        <template v-else>{{ part.value }}</template>
      </template>
    </strong>
    <span class="metric-hint">{{ metric.hint }}</span>
    <Transition name="delta-fade">
      <span v-if="deltaText" :class="['metric-delta', deltaDirection]">{{ deltaText }}</span>
    </Transition>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  metric: { type: Object, required: true },
  divided: { type: Boolean, default: false },
  previousValue: { type: Number, default: undefined }
})

const isPulsing = ref(false)
const isBouncing = ref(false)
const deltaText = ref('')
const deltaDirection = ref('')

let pulseTimer = null
let bounceTimer = null
let deltaTimer = null

watch(() => props.metric.value, (newVal, oldVal) => {
  if (oldVal === undefined || newVal === oldVal) return

  const diff = newVal - oldVal
  if (diff === 0) return

  // Pulse — re-trigger by removing and re-adding class
  isPulsing.value = false
  void document.body?.offsetWidth // force reflow
  isPulsing.value = true
  clearTimeout(pulseTimer)
  pulseTimer = setTimeout(() => { isPulsing.value = false }, 600)

  // Bounce
  isBouncing.value = false
  void document.body?.offsetWidth
  isBouncing.value = true
  clearTimeout(bounceTimer)
  bounceTimer = setTimeout(() => { isBouncing.value = false }, 400)

  // Delta indicator
  const sign = diff > 0 ? '+' : ''
  const locale = navigator.language || 'en-US'
  deltaText.value = sign + Math.abs(diff).toLocaleString(locale, { maximumFractionDigits: 0 })
  deltaDirection.value = diff > 0 ? 'delta-up' : 'delta-down'

  clearTimeout(deltaTimer)
  deltaTimer = setTimeout(() => { deltaText.value = '' }, 2000)
})

function onAnimationEnd(e) {
  if (e.animationName?.includes('cardGlowPulse')) {
    isPulsing.value = false
  }
  if (e.animationName?.includes('metricBounce')) {
    isBouncing.value = false
  }
}
</script>

<style scoped>
.metric-card {
  flex: 1;
  min-width: 0;
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  position: relative;
  overflow: hidden;
  transition: background var(--motion-base);
}

.metric-card:hover {
  background: rgba(255, 255, 255, 0.02);
}

.metric-glow {
  position: absolute;
  top: -20px;
  right: -20px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  filter: blur(30px);
  opacity: 0;
  transition: opacity var(--motion-slow);
  pointer-events: none;
}

.metric-card:hover .metric-glow {
  opacity: 1;
}

.metric-card--income .metric-glow { background: rgba(0, 232, 157, 0.12); }
.metric-card--expense .metric-glow { background: rgba(255, 77, 106, 0.12); }
.metric-card--balance .metric-glow { background: rgba(108, 124, 255, 0.12); }
.metric-card--savings .metric-glow { background: rgba(139, 92, 246, 0.12); }

.metric-card--divided {
  border-right: 1px solid var(--border-hairline);
}

/* Ripple layer — always present, animated via CSS */
.metric-ripple-layer {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  opacity: 0;
}

.metric-card--pulse .metric-ripple-layer {
  animation: rippleExpand 600ms ease-out forwards;
}

@keyframes rippleExpand {
  0% { width: 20px; height: 20px; opacity: 0.4; }
  100% { width: 120px; height: 120px; opacity: 0; }
}

.metric-card--income .metric-ripple-layer { background: rgba(0, 232, 157, 0.12); }
.metric-card--expense .metric-ripple-layer { background: rgba(255, 77, 106, 0.12); }
.metric-card--balance .metric-ripple-layer { background: rgba(108, 124, 255, 0.12); }
.metric-card--savings .metric-ripple-layer { background: rgba(139, 92, 246, 0.12); }

/* Bounce animation */
@keyframes metricBounce {
  0% { transform: scale(1); }
  30% { transform: scale(1.06); }
  60% { transform: scale(0.98); }
  100% { transform: scale(1); }
}

.metric-card--bounce .metric-value {
  animation: metricBounce 400ms cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* Card glow pulse */
@keyframes cardGlowPulse {
  0% { box-shadow: none; }
  50% { box-shadow: inset 0 0 24px rgba(108, 124, 255, 0.06); }
  100% { box-shadow: none; }
}

@keyframes cardGlowPulseIncome {
  0% { box-shadow: none; }
  50% { box-shadow: inset 0 0 24px rgba(0, 232, 157, 0.06); }
  100% { box-shadow: none; }
}

@keyframes cardGlowPulseExpense {
  0% { box-shadow: none; }
  50% { box-shadow: inset 0 0 24px rgba(255, 77, 106, 0.06); }
  100% { box-shadow: none; }
}

.metric-card--pulse {
  animation: cardGlowPulse 600ms ease-out;
}

.metric-card--income.metric-card--pulse { animation-name: cardGlowPulseIncome; }
.metric-card--expense.metric-card--pulse { animation-name: cardGlowPulseExpense; }

.metric-label {
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.metric-value {
  font-size: 32px;
  font-weight: var(--weight-semibold);
  line-height: 1.15;
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
  transition: color var(--motion-base);
  position: relative;
}

.metric-value--income  { color: var(--income); }
.metric-value--expense { color: var(--expense); }
.metric-value--balance { color: var(--text-primary); }
.metric-value--savings { color: var(--accent); }

.metric-currency {
  font-weight: var(--weight-normal);
  opacity: 0.5;
  font-size: 0.65em;
}

.metric-hint {
  font-size: var(--text-xs);
  color: var(--text-muted);
  font-weight: var(--weight-medium);
}

/* Delta indicator with Vue Transition */
.metric-delta {
  position: absolute;
  top: var(--space-3);
  right: var(--space-4);
  font-size: 10px;
  font-weight: var(--weight-semibold);
  font-family: var(--font-mono);
  padding: 2px 6px;
  border-radius: var(--radius-pill);
}

.delta-fade-enter-active {
  transition: all 300ms cubic-bezier(0.34, 1.56, 0.64, 1);
}
.delta-fade-leave-active {
  transition: all 400ms cubic-bezier(0, 0, 0.2, 1);
}
.delta-fade-enter-from {
  opacity: 0;
  transform: translateY(-6px) scale(0.8);
}
.delta-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.9);
}

.delta-up {
  color: var(--income);
  background: var(--income-bg);
  border: 1px solid rgba(0, 232, 157, 0.15);
}

.delta-down {
  color: var(--expense);
  background: var(--expense-bg);
  border: 1px solid rgba(255, 77, 106, 0.15);
}

@media (max-width: 720px) {
  .metric-card--divided {
    border-right: 0;
    border-bottom: 1px solid var(--border-hairline);
  }

  .metric-value {
    font-size: 24px;
  }
}
</style>
