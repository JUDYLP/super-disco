<template>
  <div class="metric-card" :class="[`tone-${metric.tone}`, { divided, pulsing: isPulsing, bouncing: isBouncing }]" @animationend="onAnimEnd">
    <span class="metric-label">{{ metric.label }}</span>
    <strong class="metric-value">
      <template v-for="part in metric._displayParts" :key="part.type">
        <span v-if="part.type === 'currency' || part.type === 'minusSign'" class="metric-prefix">{{ part.value }}</span>
        <template v-else>{{ part.value }}</template>
      </template>
    </strong>
    <span class="metric-hint">{{ metric.hint }}</span>
    <Transition name="delta">
      <span v-if="deltaText" :class="['metric-delta', deltaDir]">{{ deltaText }}</span>
    </Transition>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
const props = defineProps({ metric: { type: Object, required: true }, divided: { type: Boolean, default: false } })
const isPulsing = ref(false), isBouncing = ref(false), deltaText = ref(''), deltaDir = ref('')
let pt, bt, dt

watch(() => props.metric.value, (nv, ov) => {
  if (ov === undefined || nv === ov) return; const diff = nv - ov; if (diff === 0) return
  isPulsing.value = false; void document.body?.offsetWidth; isPulsing.value = true; clearTimeout(pt); pt = setTimeout(() => { isPulsing.value = false }, 500)
  isBouncing.value = false; void document.body?.offsetWidth; isBouncing.value = true; clearTimeout(bt); bt = setTimeout(() => { isBouncing.value = false }, 350)
  const sign = diff > 0 ? '+' : ''; deltaText.value = sign + Math.abs(diff).toLocaleString(); deltaDir.value = diff > 0 ? 'up' : 'down'
  clearTimeout(dt); dt = setTimeout(() => { deltaText.value = '' }, 2000)
})

function onAnimEnd(e) { if (e.animationName?.includes('pulse')) isPulsing.value = false; if (e.animationName?.includes('bounce')) isBouncing.value = false }
</script>

<style scoped>
.metric-card {
  flex: 1; min-width: 0; padding: var(--s-4) var(--s-5);
  display: flex; flex-direction: column; gap: var(--s-1);
  position: relative; transition: background var(--dur-fast);
  background: var(--bg-raised);
}
.metric-card:hover { background: var(--bg-hover); }
.metric-card.divided { border-right: 1px solid var(--border-light); }

.metric-label { font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-tertiary); text-transform: uppercase; letter-spacing: var(--ls-caps); }
.metric-value { font-size: var(--fs-2xl); font-weight: var(--fw-bold); line-height: 1.2; font-variant-numeric: tabular-nums; letter-spacing: var(--ls-tight); }
.metric-prefix { font-weight: var(--fw-normal); opacity: 0.5; font-size: 0.65em; }

.tone-success .metric-value { color: var(--success); }
.tone-danger .metric-value { color: var(--danger); }
.tone-accent .metric-value { color: var(--accent); }
.tone-neutral .metric-value { color: var(--text-primary); }

.metric-hint { font-size: var(--fs-xs); color: var(--text-tertiary); }

@keyframes bounce { 0%{transform:scale(1)} 30%{transform:scale(1.05)} 60%{transform:scale(0.98)} 100%{transform:scale(1)} }
@keyframes pulse { 0%{box-shadow:none} 50%{box-shadow:inset 0 0 0 2px var(--accent-soft)} 100%{box-shadow:none} }
.metric-card.pulsing { animation: pulse 500ms var(--ease-out); }
.metric-value.bouncing { animation: bounce 350ms var(--ease-out); }

.metric-delta { position: absolute; top: var(--s-2); right: var(--s-3); font-size: 10px; font-weight: var(--fw-semibold); padding: 1px 6px; border-radius: var(--r-pill); }
.metric-delta.up { color: var(--success); background: var(--success-soft); }
.metric-delta.down { color: var(--danger); background: var(--danger-soft); }

.delta-enter-active { transition: all 0.25s var(--ease-out); }
.delta-leave-active { transition: all 0.35s var(--ease-out); }
.delta-enter-from { opacity: 0; transform: translateY(-4px) scale(0.8); }
.delta-leave-to { opacity: 0; transform: translateY(-2px) scale(0.9); }

@media (max-width: 720px) {
  .metric-card.divided { border-right: 0; border-bottom: 1px solid var(--border-light); }
}
</style>
