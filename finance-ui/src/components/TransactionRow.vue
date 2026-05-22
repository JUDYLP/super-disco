<template>
  <tr :class="['transaction-row', bill.type, { 'row-removing': removing }]">
    <td class="cell-desc">
      <span :class="['cat-icon', bill.type === 'income' ? 'cat-icon--income' : 'cat-icon--expense']">
        {{ initial }}
      </span>
      <div class="desc-text">
        <span class="desc-name">{{ bill.name }}</span>
        <span v-if="bill.remark" class="desc-memo">{{ bill.remark }}</span>
      </div>
    </td>
    <td class="cell-cat">
      <span :class="['cat-dot', bill.type === 'income' ? 'income' : 'expense']"></span>
      {{ categoryName }}
    </td>
    <td class="cell-type">
      <span :class="['type-badge', bill.type]">{{ typeLabel }}</span>
    </td>
    <td class="cell-date">{{ dateDisplay }}</td>
    <td :class="['cell-amount', bill.type]">{{ amountDisplay }}</td>
    <td class="cell-actions">
      <button class="action-btn" type="button" @click="$emit('edit', bill)" :aria-label="t.edit">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <circle cx="4" cy="4" r="1.5" fill="currentColor" /><circle cx="10" cy="4" r="1.5" fill="currentColor" />
          <circle cx="4" cy="10" r="1.5" fill="currentColor" /><circle cx="10" cy="10" r="1.5" fill="currentColor" />
        </svg>
      </button>
    </td>
  </tr>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  bill: { type: Object, required: true },
  initial: { type: String, default: '?' },
  dateDisplay: { type: String, default: '-' },
  categoryName: { type: String, default: '' },
  typeLabel: { type: String, default: '' },
  amountDisplay: { type: String, default: '' },
  t: { type: Object, required: true }
})

defineEmits(['edit', 'delete'])

const removing = ref(false)

defineExpose({ removing })
</script>

<style scoped>
.transaction-row {
  transition: all var(--motion-fast);
  position: relative;
}

.transaction-row::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, var(--border-hairline), transparent);
}

.transaction-row:hover {
  background: rgba(108, 124, 255, 0.03);
}

.transaction-row.income:hover {
  background: rgba(0, 232, 157, 0.03);
}

.transaction-row.expense:hover {
  background: rgba(255, 77, 106, 0.03);
}

.transaction-row:hover .action-btn {
  opacity: 1;
}

.transaction-row:hover .cat-icon {
  transform: scale(1.08);
}

/* Remove animation */
.row-removing {
  animation: rowRemove 400ms cubic-bezier(0, 0, 0.2, 1) forwards;
}

@keyframes rowRemove {
  0% {
    opacity: 1;
    transform: translateX(0) scaleY(1);
    max-height: 48px;
  }
  40% {
    opacity: 0.6;
    transform: translateX(20px) scaleY(1);
  }
  100% {
    opacity: 0;
    transform: translateX(40px) scaleY(0);
    max-height: 0;
    padding: 0;
    margin: 0;
    overflow: hidden;
  }
}

td {
  height: 48px;
  padding: 0 var(--space-3);
  font-size: var(--text-base);
  color: var(--text-secondary);
  border-bottom: none;
  vertical-align: middle;
  white-space: nowrap;
}

.cell-desc {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  min-width: 0;
}

.cat-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: #fff;
  flex-shrink: 0;
  transition: all var(--motion-fast);
}

.cat-icon--income {
  background: rgba(0, 232, 157, 0.12);
  color: var(--income);
  box-shadow: 0 0 0 0 rgba(0, 232, 157, 0);
}

.transaction-row.income:hover .cat-icon--income {
  box-shadow: 0 0 12px rgba(0, 232, 157, 0.2);
}

.cat-icon--expense {
  background: rgba(255, 77, 106, 0.12);
  color: var(--expense);
  box-shadow: 0 0 0 0 rgba(255, 77, 106, 0);
}

.transaction-row.expense:hover .cat-icon--expense {
  box-shadow: 0 0 12px rgba(255, 77, 106, 0.2);
}

.desc-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 1px;
}

.desc-name {
  font-weight: var(--weight-medium);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
}

.desc-memo {
  font-size: var(--text-xs);
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-cat {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.cat-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-dot.income { background: var(--income); box-shadow: 0 0 4px rgba(0, 232, 157, 0.4); }
.cat-dot.expense { background: var(--expense); box-shadow: 0 0 4px rgba(255, 77, 106, 0.4); }

.type-badge {
  display: inline-flex;
  align-items: center;
  height: 22px;
  padding: 0 var(--space-2);
  border-radius: var(--radius-pill);
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
}

.type-badge.income {
  color: var(--income);
  background: var(--income-bg);
  border: 1px solid rgba(0, 232, 157, 0.1);
}

.type-badge.expense {
  color: var(--expense);
  background: var(--expense-bg);
  border: 1px solid rgba(255, 77, 106, 0.1);
}

.cell-date {
  font-variant-numeric: tabular-nums;
}

.cell-amount {
  font-weight: var(--weight-semibold);
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  text-align: right;
}

.cell-amount.income  { color: var(--income); text-shadow: 0 0 12px rgba(0, 232, 157, 0.1); }
.cell-amount.expense { color: var(--expense); text-shadow: 0 0 12px rgba(255, 77, 106, 0.1); }

.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  opacity: 0;
  transition: all var(--motion-fast);
}

.action-btn:hover {
  color: var(--accent);
  background: var(--accent-bg);
}
</style>
