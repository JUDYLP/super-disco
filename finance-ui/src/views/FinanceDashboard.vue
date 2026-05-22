<template>
  <div class="dashboard">
    <header class="page-header">
      <div class="header-left">
        <h1 class="page-title">{{ t.dashboardTitle }}</h1>
        <span class="header-status">
          <span class="status-dot success"></span>
          <span class="status-text">LIVE</span>
        </span>
      </div>
      <div class="page-header-actions">
        <button class="fab" type="button" @click="showAddModal = true" :aria-label="t.addBill">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M9 3v12M3 9h12" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
          </svg>
        </button>
      </div>
    </header>

    <section class="kpi-section">
      <KpiStrip :metrics="kpiMetrics" />
      <div class="savings-bar-wrapper">
        <div class="savings-bar-header">
          <span class="savings-bar-label">{{ t.savingsRate || '储蓄率' }}</span>
          <span :class="['savings-bar-value', savingsRate >= 0 ? 'positive' : 'negative']">
            {{ savingsRate }}%
          </span>
        </div>
        <div class="savings-bar-track">
          <div
            class="savings-bar-fill"
            :class="{ 'savings-bar-fill--negative': savingsRate < 0 }"
            :style="{ width: Math.min(Math.abs(savingsRate), 100) + '%' }"
          >
            <div class="savings-bar-shimmer"></div>
          </div>
          <div class="savings-bar-markers">
            <span class="marker" style="left: 25%">25%</span>
            <span class="marker" style="left: 50%">50%</span>
            <span class="marker" style="left: 75%">75%</span>
          </div>
        </div>
        <div class="savings-bar-hint">
          <span v-if="savingsRate >= 50">{{ t.savingsExcellent || '储蓄状况优秀' }}</span>
          <span v-else-if="savingsRate >= 30">{{ t.savingsGood || '储蓄状况良好' }}</span>
          <span v-else-if="savingsRate >= 10">{{ t.savingsFair || '储蓄状况一般' }}</span>
          <span v-else>{{ t.savingsWarning || '储蓄率偏低，注意控制支出' }}</span>
        </div>
      </div>
    </section>

    <div class="content-split">
      <div class="content-main">
        <div class="filter-bar">
          <div class="filter-chips">
            <button :class="{ active: filters.type === '' }" @click="setFilter('')">{{ t.allBills }}</button>
            <button :class="{ active: filters.type === 'expense' }" @click="setFilter('expense')">{{ t.expense }}</button>
            <button :class="{ active: filters.type === 'income' }" @click="setFilter('income')">{{ t.income }}</button>
          </div>
          <div class="filter-right">
            <select v-model="filters.category_id" class="filter-select">
              <option value="">{{ t.all }}</option>
              <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
            <button class="btn-apply" @click="loadBills">{{ t.apply }}</button>
          </div>
        </div>

        <BillList
          :bills="recentBills"
          :categories="categories"
          :loading="loadingBills"
          @edit="startEdit"
          @delete="removeBill"
          @refresh="loadBills"
        />

        <p v-if="message" :class="['message', messageType]">{{ message }}</p>
      </div>

      <aside class="content-side">
        <ChartPanel :chart-data="categoryStats" />

        <div class="category-list">
          <h3 class="side-title">{{ t.expenseByCategory }}</h3>
          <div v-if="categoryStats.length === 0" class="side-empty">{{ t.noBills }}</div>
          <div v-for="(item, i) in categoryStats.slice(0, 6)" :key="i" class="category-row">
            <span class="cat-dot" :style="{ background: chartColors[i % chartColors.length], boxShadow: `0 0 8px ${chartColors[i % chartColors.length]}40` }"></span>
            <span class="cat-name">{{ item.name }}</span>
            <span class="cat-amount">{{ formatCurrency(item.value) }}</span>
          </div>
        </div>

      </aside>
    </div>

    <section class="receipt-section">
      <InteractiveReceipt :bill-data="bills" :dashboard="dashboard" :savings-rate="savingsRate" :category-stats="categoryStats" />
    </section>

    <section class="cashflow-section glass-panel">
      <div class="cashflow-header">
        <h3 class="side-title">{{ t.liveProjection }}</h3>
        <span class="cashflow-badge">REAL-TIME</span>
      </div>
      <div ref="areaChartRef" class="area-chart"></div>
    </section>

    <AddTransactionModal
      :visible="showAddModal"
      :categories="categories"
      :editing-bill="editingBill"
      :submitting="submitting"
      @submit="saveBill"
      @cancel="closeAddModal"
      @close="closeAddModal"
    />

    <CoinParticles ref="coinParticles" />

    <ConfirmModal
      :visible="confirmVisible"
      :message="confirmMessage"
      :confirm-text="t.delete"
      :cancel-text="t.cancel"
      @confirm="onConfirm"
      @cancel="onCancel"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { createBill, deleteBill, fetchBills, updateBill } from '../api/bill'
import { fetchCategories } from '../api/category'
import { fetchDashboard, fetchExpenseByCategory } from '../api/statistics'
import BillList from '../components/BillList.vue'
import ChartPanel from '../components/ChartPanel.vue'
import KpiStrip from '../components/StatCard.vue'
import AddTransactionModal from '../components/AddTransactionModal.vue'
import ConfirmModal from '../components/ConfirmModal.vue'
import InteractiveReceipt from '../components/InteractiveReceipt.vue'
import CoinParticles from '../components/CoinParticles.vue'
import { useI18n } from '../i18n/locale'
import { playKaChing, playDelete, playSuccess, getRecommendedParticleCount } from '../utils/sounds'

const props = defineProps({
  currentUser: { type: Object, default: null }
})

const emit = defineEmits(['logout', 'bills-change'])

const { language, t } = useI18n()

const chartColors = [
  '#6c7cff', '#00e89d', '#ffb347', '#ff4d6a',
  '#06B6D4', '#8B5CF6', '#10B981', '#F97316'
]

const bills = ref([])
const categories = ref([])
const categoryStats = ref([])
const editingBill = ref(null)
const loadingBills = ref(false)
const submitting = ref(false)
const message = ref('')
const messageType = ref('success')
const showAddModal = ref(false)
const coinParticles = ref(null)

const dashboard = reactive({
  month_income: 0,
  month_expense: 0,
  month_balance: 0
})

const savingsRate = computed(() => {
  if (dashboard.month_income === 0) return 0
  return Math.round((dashboard.month_balance / dashboard.month_income) * 100)
})

const kpiMetrics = computed(() => [
  {
    key: 'income',
    label: t.value.monthIncome,
    value: dashboard.month_income,
    hint: t.value.incomeHint,
    tone: 'income'
  },
  {
    key: 'expense',
    label: t.value.monthExpense,
    value: dashboard.month_expense,
    hint: t.value.expenseHint,
    tone: 'expense'
  },
  {
    key: 'balance',
    label: t.value.monthBalance,
    value: dashboard.month_balance,
    hint: t.value.balanceHint,
    tone: 'balance'
  },
  {
    key: 'savings',
    label: t.value.savingsRate || '储蓄率',
    value: savingsRate.value,
    hint: '%',
    tone: 'savings',
    isPercentage: true
  }
])

const recentBills = computed(() => bills.value.slice(0, 8))

const filters = reactive({
  start_date: '',
  end_date: '',
  type: '',
  category_id: ''
})

const confirmVisible = ref(false)
const confirmMessage = ref('')
let confirmResolve = null

const areaChartRef = ref(null)
let areaChart = null

const initAreaChart = () => {
  if (!areaChartRef.value) return
  areaChart = echarts.init(areaChartRef.value)
  const option = {
    grid: { top: 16, right: 24, bottom: 24, left: 24 },
    xAxis: {
      type: 'category',
      data: [],
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#4a5068', fontSize: 11 },
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      splitLine: {
        lineStyle: { color: 'rgba(108, 124, 255, 0.06)', type: 'dashed' }
      },
      axisLabel: { color: '#4a5068', fontSize: 11 }
    },
    series: [
      {
        name: 'Income',
        type: 'line',
        data: [],
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#00e89d', shadowColor: 'rgba(0, 232, 157, 0.3)', shadowBlur: 8 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(0, 232, 157, 0.20)' },
            { offset: 1, color: 'rgba(0, 232, 157, 0)' }
          ])
        }
      },
      {
        name: 'Expense',
        type: 'line',
        data: [],
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#ff4d6a', shadowColor: 'rgba(255, 77, 106, 0.3)', shadowBlur: 8 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 77, 106, 0.15)' },
            { offset: 1, color: 'rgba(255, 77, 106, 0)' }
          ])
        }
      }
    ],
    tooltip: {
      backgroundColor: 'rgba(16, 21, 42, 0.9)',
      borderColor: 'rgba(108, 124, 255, 0.15)',
      textStyle: { color: '#e8ecf4', fontSize: 12 },
      trigger: 'axis'
    }
  }
  areaChart.setOption(option)
}

const updateAreaChart = () => {
  if (!areaChart) return
  const byDate = {}
  bills.value.forEach((b) => {
    const d = b.consume_date || b.date || ''
    if (!d) return
    const key = d.slice(0, 10)
    if (!byDate[key]) byDate[key] = { income: 0, expense: 0 }
    if (b.type === 'income') byDate[key].income += Number(b.amount) || 0
    else byDate[key].expense += Number(b.amount) || 0
  })
  const sorted = Object.entries(byDate).sort((a, b) => a[0].localeCompare(b[0]))
  areaChart.setOption({
    xAxis: { data: sorted.map(([k]) => k.slice(5)) },
    series: [
      { data: sorted.map(([, v]) => v.income) },
      { data: sorted.map(([, v]) => v.expense) }
    ]
  })
}

const formatCurrency = (value) => {
  const locale = language.value === 'zh' ? 'zh-CN' : 'en-US'
  const currency = language.value === 'zh' ? 'CNY' : 'USD'
  const symbol = language.value === 'zh' ? '￥' : '$'
  return symbol + Number(value).toLocaleString(locale, { maximumFractionDigits: 0 })
}

const setFilter = (type) => {
  filters.type = type
  loadBills()
}

const closeAddModal = () => {
  showAddModal.value = false
  editingBill.value = null
}

const showConfirm = (msg) => {
  return new Promise((resolve) => {
    confirmMessage.value = msg
    confirmVisible.value = true
    confirmResolve = resolve
  })
}

const onConfirm = () => {
  confirmVisible.value = false
  confirmResolve?.(true)
  confirmResolve = null
}

const onCancel = () => {
  confirmVisible.value = false
  confirmResolve?.(false)
  confirmResolve = null
}

const showMessage = (text, type = 'success') => {
  message.value = text
  messageType.value = type
  setTimeout(() => { message.value = '' }, 2600)
}

const unwrapResponse = (response) => {
  const result = response.data
  if (!result.success) throw new Error(result.message || t.value.requestFailed)
  return result.data
}

const buildQueryParams = () => {
  const params = {}
  if (filters.start_date) params.start_date = filters.start_date
  if (filters.end_date) params.end_date = filters.end_date
  if (filters.type) params.type = filters.type
  if (filters.category_id) params.category_id = filters.category_id
  return params
}

const loadCategories = async () => {
  const page = unwrapResponse(await fetchCategories())
  categories.value = Array.isArray(page) ? page : (page.records || [])
}

const loadBills = async () => {
  loadingBills.value = true
  try {
    const page = unwrapResponse(await fetchBills(buildQueryParams()))
    bills.value = Array.isArray(page) ? page : (page.records || [])
    emit('bills-change', bills.value.length)
    updateAreaChart()
  } catch (error) {
    showMessage(error.message, 'error')
  } finally {
    loadingBills.value = false
  }
}

const loadDashboard = async () => {
  Object.assign(dashboard, unwrapResponse(await fetchDashboard()))
}

const loadCategoryStats = async () => {
  categoryStats.value = unwrapResponse(await fetchExpenseByCategory())
}

const loadAllData = async () => {
  try {
    await Promise.all([loadCategories(), loadBills(), loadDashboard(), loadCategoryStats()])
  } catch (error) {
    showMessage(error.message, 'error')
  }
}

const saveBill = async (payload) => {
  submitting.value = true
  try {
    if (payload.id) {
      const data = unwrapResponse(await updateBill(payload.id, payload))
      const index = bills.value.findIndex((item) => item.id === data.id)
      if (index >= 0) bills.value[index] = data
      editingBill.value = null
      showMessage(t.value.updated)
      playSuccess()
    } else {
      const data = unwrapResponse(await createBill(payload))
      bills.value.unshift(data)
      showMessage(t.value.added)
      // Trigger coin particles and sound based on type
      if (payload.type === 'income') {
        coinParticles.value?.spawnIncome(getRecommendedParticleCount())
        playKaChing()
      } else {
        coinParticles.value?.spawnExpense(Math.max(3, getRecommendedParticleCount() - 1))
      }
    }
    await Promise.all([loadDashboard(), loadCategoryStats()])
    emit('bills-change', bills.value.length)
    updateAreaChart()
    showAddModal.value = false
  } catch (error) {
    showMessage(error.message, 'error')
  } finally {
    submitting.value = false
  }
}

const startEdit = (bill) => {
  editingBill.value = { ...bill }
  showAddModal.value = true
}

const removeBill = async (bill) => {
  const confirmed = await showConfirm(`${t.value.confirmDeletePrefix} "${bill.name}"${t.value.confirmDeleteSuffix}`)
  if (!confirmed) return
  try {
    unwrapResponse(await deleteBill(bill.id))
    bills.value = bills.value.filter((item) => item.id !== bill.id)
    playDelete()
    await Promise.all([loadDashboard(), loadCategoryStats()])
    emit('bills-change', bills.value.length)
    updateAreaChart()
    showMessage(t.value.deleted)
  } catch (error) {
    showMessage(error.message, 'error')
  }
}

const resetFilters = () => {
  Object.assign(filters, { start_date: '', end_date: '', type: '', category_id: '' })
  loadBills()
}

onMounted(() => {
  initAreaChart()
  loadAllData()
})

onBeforeUnmount(() => {
  areaChart?.dispose()
})

watch(language, () => {
  updateAreaChart()
})
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  color: var(--text-primary);
  background: var(--surface-0);
  position: relative;
}

.dashboard::before {
  content: '';
  position: fixed;
  top: 0;
  right: 0;
  width: 40%;
  height: 40%;
  background: radial-gradient(ellipse, rgba(108, 124, 255, 0.04), transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 var(--space-8);
  background: rgba(4, 6, 14, 0.85);
  backdrop-filter: blur(20px) saturate(1.2);
  -webkit-backdrop-filter: blur(20px) saturate(1.2);
  border-bottom: 1px solid var(--border-hairline);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.page-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-primary);
}

.header-status {
  display: flex;
  align-items: center;
  gap: var(--space-1);
}

.status-text {
  font-size: 10px;
  font-weight: var(--weight-bold);
  color: var(--income);
  letter-spacing: 0.1em;
}

.page-header-actions {
  display: flex;
  gap: var(--space-2);
}

.fab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: var(--radius-lg);
  color: #fff;
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  cursor: pointer;
  box-shadow: 0 4px 18px rgba(108, 124, 255, 0.4);
  transition: all var(--motion-fast);
  position: relative;
  overflow: hidden;
}

.fab::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15), transparent 60%);
  pointer-events: none;
}

.fab:hover {
  box-shadow: 0 6px 24px rgba(108, 124, 255, 0.6);
  transform: translateY(-2px);
}

.kpi-section {
  padding: var(--space-6) var(--space-8);
  position: relative;
  z-index: 1;
}

.savings-bar-wrapper {
  margin-top: var(--space-4);
  padding: var(--space-4) var(--space-5);
  background: var(--surface-1);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  position: relative;
  overflow: hidden;
}

.savings-bar-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.2), transparent);
  pointer-events: none;
}

.savings-bar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-2);
}

.savings-bar-label {
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.savings-bar-value {
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
}

.savings-bar-value.positive { color: var(--income); text-shadow: 0 0 12px rgba(0, 232, 157, 0.2); }
.savings-bar-value.negative { color: var(--expense); text-shadow: 0 0 12px rgba(255, 77, 106, 0.2); }

.savings-bar-track {
  height: 6px;
  background: rgba(255, 255, 255, 0.04);
  border-radius: 3px;
  position: relative;
  overflow: hidden;
}

.savings-bar-fill {
  height: 100%;
  border-radius: 3px;
  background: linear-gradient(90deg, #00e89d, #06B6D4);
  transition: width 800ms cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  overflow: hidden;
}

.savings-bar-fill--negative {
  background: linear-gradient(90deg, #ff4d6a, #F97316);
}

.savings-bar-shimmer {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    90deg,
    transparent 0%,
    rgba(255, 255, 255, 0.15) 50%,
    transparent 100%
  );
  animation: shimmer 2s ease-in-out infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.savings-bar-markers {
  position: relative;
  height: 16px;
  margin-top: 2px;
}

.marker {
  position: absolute;
  transform: translateX(-50%);
  font-size: 9px;
  color: var(--text-muted);
  opacity: 0.4;
  font-family: var(--font-mono);
}

.savings-bar-hint {
  margin-top: var(--space-2);
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.savings-bar-hint span {
  display: inline-flex;
  align-items: center;
  gap: var(--space-1);
}

.content-split {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 0;
  border-top: 1px solid var(--border-hairline);
  border-bottom: 1px solid var(--border-hairline);
  position: relative;
  z-index: 1;
}

.content-main {
  padding: var(--space-4) var(--space-8) var(--space-6);
  border-right: 1px solid var(--border-hairline);
  min-width: 0;
}

.content-side {
  padding: var(--space-5) var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
  background: rgba(10, 14, 26, 0.5);
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
}

.filter-chips {
  display: flex;
  gap: var(--space-1);
}

.filter-chips button {
  height: 28px;
  padding: 0 var(--space-3);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-pill);
  background: var(--surface-2);
  color: var(--text-muted);
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: all var(--motion-fast);
}

.filter-chips button.active {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: 0 0 12px rgba(108, 124, 255, 0.15);
}

.filter-right {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.filter-select {
  height: 32px;
  padding: 0 var(--space-3);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--surface-2);
  color: var(--text-secondary);
  font-size: var(--text-xs);
  cursor: pointer;
}

.filter-select:focus {
  border-color: var(--accent);
  outline: none;
  box-shadow: 0 0 0 2px var(--accent-bg);
}

.filter-select option {
  background: var(--surface-2);
  color: var(--text-primary);
}

.btn-apply {
  height: 32px;
  padding: 0 var(--space-4);
  border: 0;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  color: #fff;
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  cursor: pointer;
  transition: all var(--motion-fast);
  box-shadow: 0 4px 12px rgba(108, 124, 255, 0.25);
}

.btn-apply:hover {
  box-shadow: 0 6px 18px rgba(108, 124, 255, 0.4);
  transform: translateY(-1px);
}

.side-title {
  margin: 0 0 var(--space-3);
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--accent);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
}

.side-empty {
  padding: var(--space-8) 0;
  text-align: center;
  color: var(--text-muted);
  font-size: var(--text-sm);
}

.category-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) 0;
  transition: all var(--motion-fast);
}

.category-row:hover {
  padding-left: var(--space-1);
}

.cat-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.cat-name {
  flex: 1;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-amount {
  font-family: var(--font-mono);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.cashflow-section {
  padding: var(--space-5) var(--space-8) var(--space-8);
  margin: var(--space-4) var(--space-8) var(--space-8);
  border-radius: var(--radius-2xl);
  position: relative;
  z-index: 1;
}

.cashflow-header {
  margin-bottom: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cashflow-badge {
  font-size: 10px;
  font-weight: var(--weight-bold);
  color: var(--accent);
  letter-spacing: 0.1em;
  padding: 2px 8px;
  border-radius: var(--radius-pill);
  background: var(--accent-bg);
  border: 1px solid rgba(108, 124, 255, 0.15);
}

.area-chart {
  width: 100%;
  height: 260px;
}

.receipt-section {
  padding: var(--space-8) var(--space-8);
  position: relative;
  z-index: 1;
}

.message {
  margin: var(--space-3) 0 0;
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
}

.message.success {
  color: var(--income);
  background: var(--income-bg);
  border: 1px solid rgba(0, 232, 157, 0.12);
}

.message.error {
  color: var(--expense);
  background: var(--expense-bg);
  border: 1px solid rgba(255, 77, 106, 0.12);
}

@media (max-width: 1100px) {
  .content-split {
    grid-template-columns: 1fr;
  }

  .content-main {
    border-right: 0;
    border-bottom: 1px solid var(--border-hairline);
  }
}

@media (max-width: 720px) {
  .page-header {
    padding: 0 var(--space-5);
  }

  .kpi-section {
    padding: var(--space-4) var(--space-5);
  }

  .content-main {
    padding: var(--space-3) var(--space-5) var(--space-5);
  }

  .content-side {
    padding: var(--space-4) var(--space-5);
  }

  .cashflow-section {
    padding: var(--space-4) var(--space-5) var(--space-5);
    margin: var(--space-3) var(--space-5) var(--space-5);
  }
}
</style>
