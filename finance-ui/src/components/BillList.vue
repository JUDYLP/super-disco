<template>
  <section class="bill-list">
    <!-- Skeleton loading -->
    <div v-if="loading" class="skeleton-list">
      <div v-for="n in 5" :key="n" class="skeleton-row">
        <span class="skeleton-block sk-circle"></span>
        <span class="skeleton-block sk-name"></span>
        <span class="skeleton-block sk-cat"></span>
        <span class="skeleton-block sk-date"></span>
        <span class="skeleton-block sk-amount"></span>
      </div>
    </div>

    <!-- Empty state -->
    <div v-else-if="bills.length === 0" class="empty-state">
      <svg width="40" height="40" viewBox="0 0 40 40" fill="none" class="empty-icon">
        <rect x="4" y="4" width="32" height="32" rx="8" stroke="currentColor" stroke-width="1.2" />
        <path d="M4 14h32M14 14v22" stroke="currentColor" stroke-width="1.2" />
      </svg>
      <p class="empty-text">{{ t.noBills }}</p>
      <button class="empty-cta" @click="$emit('refresh')">{{ t.addBill }}</button>
    </div>

    <!-- Table -->
    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th class="col-desc">{{ t.name }}</th>
            <th class="col-cat">{{ t.category }}</th>
            <th class="col-acc">{{ t.type }}</th>
            <th class="col-date">{{ t.date }}</th>
            <th class="col-amount">{{ t.amount }}</th>
            <th class="col-act"></th>
          </tr>
        </thead>
        <tbody>
          <TransactionRow
            v-for="bill in bills"
            :key="bill.id"
            :bill="bill"
            :initial="getInitial(bill.name)"
            :date-display="formatDate(bill.consume_date)"
            :category-name="getCategoryName(bill.category_id)"
            :type-label="bill.type === 'income' ? t.income : t.expense"
            :amount-display="formatCurrency(bill.amount)"
            :t="t"
            @edit="$emit('edit', bill)"
            @delete="$emit('delete', bill)"
          />
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { useI18n } from '../i18n/locale'
import TransactionRow from './TransactionRow.vue'

const props = defineProps({
  bills: { type: Array, default: () => [] },
  categories: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})

defineEmits(['edit', 'delete', 'refresh'])

const { language, t } = useI18n()

const getInitial = (name) => {
  if (!name) return '?'
  return name.trim().charAt(0).toUpperCase()
}

const formatCurrency = (amount) => {
  const numericAmount = Number(amount) || 0
  const locale = language.value === 'zh' ? 'zh-CN' : 'en-US'
  const currency = language.value === 'zh' ? 'CNY' : 'USD'
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency
  }).format(numericAmount)
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const parts = dateStr.split('-')
  if (parts.length === 3) {
    if (language.value === 'zh') return `${parts[1]}/${parts[2]}`
    return `${parts[1]}/${parts[2]}/${parts[0].slice(2)}`
  }
  return dateStr
}

const getCategoryName = (categoryId) => {
  const category = props.categories.find((item) => item.id === categoryId)
  return category ? category.name : t.value.unknown
}
</script>

<style scoped>
.bill-list {
  min-width: 0;
}

/* ── Empty state ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-16) var(--space-4);
  gap: var(--space-3);
}

.empty-icon {
  color: var(--text-muted);
  opacity: 0.4;
}

.empty-text {
  margin: 0;
  color: var(--text-muted);
  font-size: var(--text-md);
}

.empty-cta {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 var(--space-4);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: all var(--motion-fast);
}

.empty-cta:hover {
  border-color: var(--accent);
  color: var(--accent);
}

/* ── Skeletons ── */
@keyframes skeleton-shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.skeleton-list {
  display: flex;
  flex-direction: column;
}

.skeleton-row {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  height: 48px;
  padding: 0 var(--space-3);
  border-bottom: 1px solid var(--border-hairline);
}

.skeleton-block {
  height: 12px;
  border-radius: var(--radius-sm);
  background: linear-gradient(90deg, var(--surface-2) 25%, var(--surface-3) 50%, var(--surface-2) 75%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.5s infinite;
}

.sk-circle {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
}

.sk-name { flex: 1; max-width: 140px; }
.sk-cat { width: 80px; }
.sk-date { width: 60px; }
.sk-amount { width: 80px; margin-left: auto; }

/* ── Table ── */
.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 640px;
}

th {
  padding: 0 var(--space-3);
  text-align: left;
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  height: var(--table-header-height);
  border-bottom: 1px solid var(--border-subtle);
  vertical-align: middle;
  white-space: nowrap;
}

.col-desc { /* flex */ }
.col-cat { width: 100px; }
.col-acc { width: 90px; }
.col-date { width: 90px; }
.col-amount { width: 120px; text-align: right; }
.col-act { width: 40px; }
</style>
