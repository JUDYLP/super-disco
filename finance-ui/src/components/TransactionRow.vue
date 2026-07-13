<template>
  <tr :class="['txn-row', bill.type, {removing}]">
    <td>
      <div class="desc-cell">
        <span :class="['avatar', bill.type==='income'?'av-in':'av-ex']">{{ initial }}</span>
        <div class="desc-text"><span class="desc-name">{{ bill.name }}</span><span v-if="bill.remark" class="desc-memo">{{ bill.remark }}</span></div>
      </div>
    </td>
    <td class="cell-sec">{{ categoryName }}</td>
    <td><span :class="['badge', bill.type]">{{ typeLabel }}</span></td>
    <td class="cell-sec">{{ dateDisplay }}</td>
    <td :class="['cell-amt', bill.type]">{{ amountDisplay }}</td>
    <td>
      <button class="act-btn" @click="$emit('edit',bill)" title="Edit">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><circle cx="3" cy="3" r="1.5" fill="currentColor"/><circle cx="11" cy="3" r="1.5" fill="currentColor"/><circle cx="3" cy="11" r="1.5" fill="currentColor"/><circle cx="11" cy="11" r="1.5" fill="currentColor"/></svg>
      </button>
    </td>
  </tr>
</template>

<script setup>
import { ref } from 'vue'
defineProps({ bill:{type:Object,required:true}, initial:{type:String,default:'?'}, dateDisplay:{type:String,default:'-'}, categoryName:{type:String,default:''}, typeLabel:{type:String,default:''}, amountDisplay:{type:String,default:''} })
defineEmits(['edit','delete'])
const removing = ref(false)
defineExpose({ removing })
</script>

<style scoped>
.txn-row { transition: background var(--dur-fast); }
.txn-row:hover { background: var(--bg-hover); }

@keyframes rowOut { 0%{opacity:1;transform:translateX(0)} 100%{opacity:0;transform:translateX(24px)} }
.removing { animation: rowOut 0.3s var(--ease-out) forwards; }

td { height: 52px; padding: 0 var(--s-3); font-size: var(--fs-sm); color: var(--text-secondary); border-bottom: 1px solid var(--border-light); white-space: nowrap; }
.cell-sec { color: var(--text-tertiary); }

.desc-cell { display: flex; align-items: center; gap: var(--s-2); }
.avatar { width: 32px; height: 32px; border-radius: var(--r-sm); display: grid; place-items: center; font-size: var(--fs-xs); font-weight: var(--fw-semibold); flex-shrink: 0; }
.av-in { background: var(--success-soft); color: var(--success); }
.av-ex { background: var(--danger-soft); color: var(--danger); }
.desc-text { display: flex; flex-direction: column; min-width: 0; }
.desc-name { font-weight: var(--fw-medium); color: var(--text-primary); }
.desc-memo { font-size: var(--fs-xs); color: var(--text-tertiary); }

.badge { display: inline-flex; align-items: center; height: 22px; padding: 0 var(--s-2); border-radius: var(--r-pill); font-size: var(--fs-xs); font-weight: var(--fw-medium); }
.badge.income { color: var(--success); background: var(--success-soft); }
.badge.expense { color: var(--danger); background: var(--danger-soft); }

.cell-amt { font-weight: var(--fw-semibold); font-variant-numeric: tabular-nums; text-align: right; }
.cell-amt.income { color: var(--success); }
.cell-amt.expense { color: var(--danger); }

.act-btn { width: 28px; height: 28px; border-radius: var(--r-sm); color: var(--text-tertiary); display: grid; place-items: center; opacity: 0; transition: all var(--dur-fast); }
.txn-row:hover .act-btn { opacity: 1; }
.act-btn:hover { color: var(--accent); background: var(--accent-soft); }
</style>
