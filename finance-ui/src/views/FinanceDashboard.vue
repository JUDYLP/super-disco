<template>
  <div class="dashboard">
    <!-- Gradient intro overlay -->
    <div class="intro-gradient" ref="introRef"></div>

    <!-- Top bar -->
    <header class="topbar">
      <div>
        <h1 class="greeting">{{ greeting }}, {{ currentUser?.username || 'User' }}</h1>
        <p class="greeting-sub">{{ t.overviewFor }} {{ monthLabel }}</p>
      </div>
      <div class="topbar-actions">
        <button class="btn-ghost" @click="showAddModal = true">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
          {{ t.addTransaction }}
        </button>
      </div>
    </header>

    <!-- Smart Analysis -->
    <section class="ai-panel card">
      <div class="ai-header">
        <div class="ai-badge">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none"><circle cx="7" cy="7" r="5.5" stroke="currentColor" stroke-width="1.2"/><path d="M7 4v3.5M7 9.5V10" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/></svg>
          {{ t.aiInsights }}
        </div>
        <span class="ai-time">{{ t.updatedJustNow }}</span>
      </div>
      <div class="ai-insights">
        <div v-for="(insight, i) in aiInsights" :key="i" class="ai-insight">
          <span :class="['ai-dot', insight.tone]"></span>
          <p>{{ insight.text }}</p>
        </div>
      </div>
    </section>

    <!-- KPI cards -->
    <section class="kpi-grid">
      <div v-for="m in kpiMetrics" :key="m.key" class="kpi-card card">
        <span class="kpi-label">{{ m.label }}</span>
        <span :class="['kpi-value', m.tone]">{{ formatKPI(m) }}</span>
        <span class="kpi-hint">{{ m.hint }}</span>
      </div>
    </section>

    <!-- Charts row -->
    <section class="charts-row">
      <div class="chart-box card">
        <div class="chart-box-header"><h3>{{ t.incomeVsExpense }}</h3></div>
        <div ref="areaChartRef" class="chart-body"></div>
      </div>
      <div class="chart-box card">
        <div class="chart-box-header"><h3>{{ t.expenseByCategory }}</h3></div>
        <div class="chart-body chart-donut-wrap">
          <div ref="donutRef" class="chart-donut"></div>
          <div class="donut-center">
            <span class="donut-total">{{ fmt(dashboard.month_expense) }}</span>
            <span class="donut-label">{{ t.totalSpent }}</span>
          </div>
        </div>
        <div class="donut-legend">
          <div v-for="(item, i) in categoryStats.slice(0,5)" :key="i" class="legend-row">
            <span class="legend-dot" :style="{background: chartColors[i]}"></span>
            <span class="legend-name">{{ item.name }}</span>
            <span class="legend-val">{{ fmt(item.value) }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Savings rate bar -->
    <section class="savings-bar card">
      <div class="savings-header">
        <span>{{ t.savingsRateLabel }}</span>
        <span :class="['savings-pct', savingsRate >= 30 ? 'good' : savingsRate >= 10 ? 'warn' : 'bad']">{{ savingsRate }}%</span>
      </div>
      <div class="bar-track">
        <div class="bar-fill" :class="savingsRate >= 30 ? 'good' : savingsRate >= 10 ? 'warn' : 'bad'" :style="{width: Math.min(Math.abs(savingsRate), 100) + '%'}"></div>
      </div>
      <div class="savings-hint">{{ savingsHint }}</div>
    </section>

    <!-- Transactions + Side -->
    <section class="content-split">
      <div class="content-main">
        <div class="section-header">
          <h3>{{ t.recentTransactions }}</h3>
          <div class="filter-chips">
            <button :class="{active: filters.type === ''}" @click="setFilter('')">{{ t.all }}</button>
            <button :class="{active: filters.type === 'expense'}" @click="setFilter('expense')">{{ t.expenses }}</button>
            <button :class="{active: filters.type === 'income'}" @click="setFilter('income')">{{ t.income }}</button>
          </div>
        </div>
        <BillList :bills="recentBills" :categories="categories" :loading="loadingBills" @edit="startEdit" @delete="removeBill" @refresh="loadBills" />
        <p v-if="message" :class="['msg', messageType]">{{ message }}</p>
      </div>
      <aside class="content-side">
        <div class="card side-card">
          <h3 class="side-title">{{ t.upcomingBills }}</h3>
          <div class="upcoming-list">
            <div v-for="b in upcomingBills" :key="b.name" class="upcoming-row">
              <div class="upcoming-icon" :style="{background: b.color}"></div>
              <div class="upcoming-info">
                <span class="upcoming-name">{{ b.name }}</span>
                <span class="upcoming-date">{{ b.date }}</span>
              </div>
              <span class="upcoming-amount">{{ fmt(b.amount) }}</span>
            </div>
            <div v-if="upcomingBills.length === 0" class="empty-note">{{ t.noUpcomingBills }}</div>
          </div>
        </div>
        <div class="card side-card">
          <h3 class="side-title">{{ t.budgetProgress }}</h3>
          <div class="budget-list">
            <div v-for="b in budgetGoals" :key="b.name" class="budget-row">
              <div class="budget-info">
                <span class="budget-name">{{ b.name }}</span>
                <span class="budget-meta">{{ fmt(b.spent) }} / {{ fmt(b.total) }}</span>
              </div>
              <div class="bar-track" style="height:6px;">
                <div class="bar-fill" :class="b.pct > 90 ? 'bad' : b.pct > 70 ? 'warn' : 'good'" :style="{width: b.pct + '%'}"></div>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </section>

    <section class="receipt-section">
      <InteractiveReceipt :bill-data="bills" :dashboard="dashboard" :savings-rate="savingsRate" :category-stats="categoryStats" />
    </section>

    <AddTransactionModal :visible="showAddModal" :categories="categories" :editing-bill="editingBill" :submitting="submitting" @submit="saveBill" @cancel="closeAddModal" @close="closeAddModal" />
    <CoinParticles ref="coinParticles" />
    <ConfirmModal :visible="confirmVisible" :message="confirmMessage" :confirm-text="t.delete" :cancel-text="t.cancel" @confirm="onConfirm" @cancel="onCancel" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch, nextTick } from 'vue'
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

const props = defineProps({ currentUser: { type: Object, default: null } })
const emit = defineEmits(['logout', 'bills-change'])
const { language, t } = useI18n()

const chartColors = ['#6c83ff','#00e89d','#f5a623','#ff5c72','#06B6D4','#8B5CF6','#10B981','#F97316']

/* ── Intro gradient ── */
const introRef = ref(null)
onMounted(() => {
  nextTick(() => {
    if (introRef.value) {
      introRef.value.classList.add('intro-done')
      setTimeout(() => { if (introRef.value) introRef.value.style.display = 'none' }, 1200)
    }
  })
})

/* ── Greeting ── */
const greeting = computed(() => {
  const h = new Date().getHours()
  return h < 12 ? t.value.goodMorning : h < 18 ? t.value.goodAfternoon : t.value.goodEvening
})
const monthLabel = computed(() => {
  const locale = language.value === 'zh' ? 'zh-CN' : 'en-US'
  return new Date().toLocaleDateString(locale, { month: 'long', year: 'numeric' })
})

/* ── Smart Analysis ── */
const aiInsights = computed(() => {
  const insights = []
  const sr = savingsRate.value
  if (sr >= 30) insights.push({ text: t.value.aiSavingsGood.replace('{rate}', sr), tone: 'good' })
  else if (sr >= 10) insights.push({ text: t.value.aiSavingsWarn.replace('{rate}', sr), tone: 'warn' })
  else insights.push({ text: t.value.aiSavingsBad.replace('{rate}', sr), tone: 'bad' })

  const topCat = categoryStats.value[0]
  if (topCat && topCat.value > dashboard.month_expense * 0.3) {
    insights.push({ text: t.value.aiCategoryWarn.replace('{cat}', topCat.name).replace('{pct}', Math.round(topCat.value / dashboard.month_expense * 100)), tone: 'warn' })
  }
  if (dashboard.month_income > dashboard.month_expense * 1.5) {
    insights.push({ text: t.value.aiIncomeStrong, tone: 'good' })
  }
  insights.push({ text: t.value.aiForecast.replace('{amount}', fmt(Math.round(dashboard.month_expense * 0.12))), tone: 'info' })
  return insights.slice(0, 4)
})

/* ── Data ── */
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

const dashboard = reactive({ month_income: 0, month_expense: 0, month_balance: 0 })

const savingsRate = computed(() => dashboard.month_income === 0 ? 0 : Math.round((dashboard.month_balance / dashboard.month_income) * 100))
const savingsHint = computed(() => {
  if (savingsRate.value >= 50) return t.value.savingsExcellent
  if (savingsRate.value >= 30) return t.value.savingsSolid
  if (savingsRate.value >= 10) return t.value.savingsFair
  return t.value.savingsNeedsAttention
})

const kpiMetrics = computed(() => [
  { key: 'income',   label: t.value.kpiMonthlyIncome, value: dashboard.month_income, hint: t.value.thisMonth, tone: 'success' },
  { key: 'expense',  label: t.value.kpiMonthlyExpense, value: dashboard.month_expense, hint: t.value.thisMonth, tone: 'danger' },
  { key: 'balance',  label: t.value.kpiNetCashFlow,  value: dashboard.month_balance, hint: t.value.incomeMinusExpense, tone: 'neutral' },
  { key: 'savings',  label: t.value.kpiSavingsRate,  value: savingsRate.value, hint: t.value.incomePctSaved, tone: 'accent', isPct: true },
])

const upcomingBills = []

const budgetGoals = computed(() => [])

const recentBills = computed(() => bills.value.slice(0, 8))

const filters = reactive({ start_date: '', end_date: '', type: '', category_id: '' })
const confirmVisible = ref(false)
const confirmMessage = ref('')
let confirmResolve = null

/* ── Charts ── */
const areaChartRef = ref(null)
const donutRef = ref(null)
let areaChart = null, donutChart = null

const fmt = (v) => {
  const n = Number(v) || 0
  const locale = language.value === 'zh' ? 'zh-CN' : 'en-US'
  const currency = language.value === 'zh' ? 'CNY' : 'USD'
  const symbol = language.value === 'zh' ? '￥' : '$'
  return symbol + n.toLocaleString(locale, { maximumFractionDigits: n >= 10000 ? 1 : 0 })
}

const formatKPI = (m) => m.isPct ? Math.round(m.value) + '%' : fmt(m.value)

const initAreaChart = () => {
  if (!areaChartRef.value) return
  areaChart = echarts.init(areaChartRef.value)
  areaChart.setOption({
    grid: { top: 8, right: 16, bottom: 24, left: 48 },
    xAxis: { type: 'category', data: [], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#5a6088', fontSize: 11 }, boundaryGap: false },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }, axisLabel: { color: '#5a6088', fontSize: 11 } },
    series: [
      { name: 'Income', type: 'line', data: [], smooth: true, symbol: 'none', lineStyle: { width: 2, color: '#00e89d', shadowColor: 'rgba(0,232,157,0.2)', shadowBlur: 8 }, areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(0,232,157,0.12)'},{offset:1,color:'rgba(0,232,157,0)'}])} },
      { name: 'Expense', type: 'line', data: [], smooth: true, symbol: 'none', lineStyle: { width: 2, color: '#ff5c72', shadowColor: 'rgba(255,92,114,0.2)', shadowBlur: 8 }, areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(255,92,114,0.10)'},{offset:1,color:'rgba(255,92,114,0)'}])} }
    ],
    tooltip: { backgroundColor: 'rgba(16,18,28,0.96)', borderColor: 'rgba(255,255,255,0.06)', textStyle: { color: '#e8ecf4', fontSize: 13 }, trigger: 'axis', extraCssText: 'border-radius:10px;box-shadow:0 4px 24px rgba(0,0,0,0.5);' }
  })
}

const updateAreaChart = () => { if(!areaChart)return; const byDate={}; bills.value.forEach(b=>{const d=(b.consume_date||b.date||'').slice(0,10); if(!d)return; if(!byDate[d])byDate[d]={income:0,expense:0}; if(b.type==='income')byDate[d].income+=Number(b.amount)||0; else byDate[d].expense+=Number(b.amount)||0}); const sorted=Object.entries(byDate).sort((a,b)=>a[0].localeCompare(b[0])); areaChart.setOption({xAxis:{data:sorted.map(([k])=>k.slice(5))},series:[{data:sorted.map(([,v])=>v.income)},{data:sorted.map(([,v])=>v.expense)}]}) }

const initDonutChart = () => { if(!donutRef.value)return; donutChart=echarts.init(donutRef.value); donutChart.setOption({series:[{type:'pie',radius:['65%','84%'],center:['50%','50%'],data:[],padAngle:2,itemStyle:{borderColor:'#0f111a',borderRadius:3,borderWidth:2},color:chartColors,label:{show:false},emphasis:{scale:true,scaleSize:4,label:{show:false}}}],tooltip:{trigger:'item',backgroundColor:'rgba(16,18,28,0.96)',borderColor:'rgba(255,255,255,0.06)',textStyle:{color:'#e8ecf4',fontSize:13},formatter:'{b}: {c} ({d}%)',extraCssText:'border-radius:10px;box-shadow:0 4px 24px rgba(0,0,0,0.5);'}}) }
const updateDonutChart = () => { if(!donutChart)return; donutChart.setOption({series:[{data:categoryStats.value.map(c=>({name:c.name,value:Number(c.value)||0}))}]}) }

/* ── CRUD ── */
const setFilter = (type) => { filters.type = type; loadBills() }
const closeAddModal = () => { showAddModal.value = false; editingBill.value = null }

const showConfirm = (msg) => new Promise(r => { confirmMessage.value = msg; confirmVisible.value = true; confirmResolve = r })
const onConfirm = () => { confirmVisible.value = false; confirmResolve?.(true); confirmResolve = null }
const onCancel = () => { confirmVisible.value = false; confirmResolve?.(false); confirmResolve = null }

const showMessage = (text, type = 'success') => { message.value = text; messageType.value = type; setTimeout(() => { message.value = '' }, 2600) }

const unwrapResponse = (r) => { const d = r.data; if (!d.success) throw new Error(d.message||t.value.requestFailed); return d.data }

const buildQueryParams = () => { const p = {}; if(filters.start_date)p.start_date=filters.start_date; if(filters.end_date)p.end_date=filters.end_date; if(filters.type)p.type=filters.type; if(filters.category_id)p.category_id=filters.category_id; return p }

const loadCategories = async () => { const p = unwrapResponse(await fetchCategories()); categories.value = Array.isArray(p)?p:(p.records||[]) }
const loadBills = async () => { loadingBills.value = true; try { const p = unwrapResponse(await fetchBills(buildQueryParams())); bills.value = Array.isArray(p)?p:(p.records||[]); emit('bills-change', bills.value.length); updateAreaChart() } catch(e){showMessage(e.message,'error')} finally{loadingBills.value=false} }
const loadDashboard = async () => { Object.assign(dashboard, unwrapResponse(await fetchDashboard())) }
const loadCategoryStats = async () => { categoryStats.value = unwrapResponse(await fetchExpenseByCategory()); updateDonutChart() }

const loadAllData = async () => { try{await Promise.all([loadCategories(),loadBills(),loadDashboard(),loadCategoryStats()])}catch(e){showMessage(e.message,'error')} }

const saveBill = async (payload) => {
  submitting.value = true
  try {
    if(payload.id){ const d=unwrapResponse(await updateBill(payload.id,payload)); const i=bills.value.findIndex(b=>b.id===d.id); if(i>=0)bills.value[i]=d; editingBill.value=null; showMessage(t.value.updated); playSuccess() }
    else { const d=unwrapResponse(await createBill(payload)); bills.value.unshift(d); showMessage(t.value.added); if(payload.type==='income'){coinParticles.value?.spawnIncome(getRecommendedParticleCount());playKaChing()}else{coinParticles.value?.spawnExpense(Math.max(3,getRecommendedParticleCount()-1))} }
    await Promise.all([loadDashboard(),loadCategoryStats()]); emit('bills-change',bills.value.length); updateAreaChart(); showAddModal.value=false
  } catch(e){showMessage(e.message,'error')} finally{submitting.value=false}
}

const startEdit = (bill) => { editingBill.value = {...bill}; showAddModal.value = true }

const removeBill = async (bill) => {
  const ok = await showConfirm(`${t.value.confirmDeletePrefix} "${bill.name}"${t.value.confirmDeleteSuffix}`)
  if(!ok)return
  try{unwrapResponse(await deleteBill(bill.id));bills.value=bills.value.filter(b=>b.id!==bill.id);playDelete();await Promise.all([loadDashboard(),loadCategoryStats()]);emit('bills-change',bills.value.length);updateAreaChart();showMessage(t.value.deleted)}catch(e){showMessage(e.message,'error')}
}

onMounted(()=>{initAreaChart();initDonutChart();loadAllData()})
onBeforeUnmount(()=>{areaChart?.dispose();donutChart?.dispose()})
watch(language,()=>{updateAreaChart();updateDonutChart()})
</script>

<style scoped>
.dashboard { max-width: var(--mx); margin: 0 auto; padding: var(--s-8) var(--s-8) var(--s-16); position: relative; }

/* ── Intro gradient ── */
.intro-gradient {
  position: fixed; inset: 0; z-index: 9999; pointer-events: none;
  background: radial-gradient(ellipse 80% 60% at 50% 40%, rgba(108,131,255,0.16), rgba(0,232,157,0.06) 40%, transparent 70%);
  opacity: 0;
  animation: introIn 1.0s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}
.intro-gradient.intro-done {
  animation: introOut 0.5s cubic-bezier(0.4, 0, 0.2, 1) forwards;
}
@keyframes introIn {
  0% { opacity: 0; transform: scale(0.92); }
  100% { opacity: 1; transform: scale(1); }
}
@keyframes introOut {
  0% { opacity: 1; }
  100% { opacity: 0; }
}

/* ── Topbar ── */
.topbar { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: var(--s-6); }
.greeting { font-size: var(--fs-2xl); font-weight: var(--fw-bold); color: var(--text-primary); letter-spacing: var(--ls-tight); line-height: 1.2; }
.greeting-sub { margin-top: var(--s-1); font-size: var(--fs-base); color: var(--text-tertiary); }
.topbar-actions { display: flex; gap: var(--s-2); align-items: center; padding-top: var(--s-1); }

.btn-ghost {
  display: inline-flex; align-items: center; gap: var(--s-2);
  height: 36px; padding: 0 var(--s-4);
  border-radius: var(--r-md); border: 1px solid var(--border-default);
  color: var(--text-primary); font-size: var(--fs-sm); font-weight: var(--fw-medium);
  background: var(--bg-raised); transition: all var(--dur-fast) var(--ease-out);
}
.btn-ghost:hover { border-color: var(--border-strong); background: var(--bg-hover); }

/* ── Smart Analysis Panel ── */
.ai-panel { padding: var(--s-5) var(--s-6); margin-bottom: var(--s-6); }
.ai-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--s-4); }
.ai-badge { display: flex; align-items: center; gap: var(--s-2); font-size: var(--fs-sm); font-weight: var(--fw-semibold); color: var(--accent); }
.ai-time { font-size: var(--fs-xs); color: var(--text-tertiary); }
.ai-insights { display: flex; flex-direction: column; gap: var(--s-2); }
.ai-insight { display: flex; gap: var(--s-2); padding: var(--s-2) 0; }
.ai-dot { width: 6px; height: 6px; border-radius: 50%; margin-top: 6px; flex-shrink: 0; }
.ai-dot.good { background: var(--success); }
.ai-dot.warn { background: var(--warning); }
.ai-dot.bad { background: var(--danger); }
.ai-dot.info { background: var(--info); }
.ai-insight p { font-size: var(--fs-sm); color: var(--text-secondary); line-height: var(--lh-relaxed); margin: 0; }

/* ── KPI Grid ── */
.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--s-3); margin-bottom: var(--s-6); }
.kpi-card { padding: var(--s-4) var(--s-5); display: flex; flex-direction: column; gap: var(--s-1); transition: box-shadow var(--dur-fast); }
.kpi-card:hover { box-shadow: var(--shadow-md); }
.kpi-label { font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-tertiary); text-transform: uppercase; letter-spacing: var(--ls-caps); }
.kpi-value { font-size: var(--fs-2xl); font-weight: var(--fw-bold); letter-spacing: var(--ls-tight); line-height: 1.2; font-variant-numeric: tabular-nums; }
.kpi-value.success { color: var(--success); }
.kpi-value.danger { color: var(--danger); }
.kpi-value.accent { color: var(--accent); }
.kpi-value.neutral { color: var(--text-primary); }
.kpi-hint { font-size: var(--fs-xs); color: var(--text-tertiary); }

/* ── Charts ── */
.charts-row { display: grid; grid-template-columns: 1.5fr 1fr; gap: var(--s-4); margin-bottom: var(--s-6); }
.chart-box { padding: var(--s-4) var(--s-5) var(--s-5); }
.chart-box-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--s-2); }
.chart-box-header h3 { font-size: var(--fs-sm); font-weight: var(--fw-semibold); color: var(--text-primary); }
.chart-body { height: 240px; }
.chart-donut-wrap { position: relative; }
.chart-donut { width: 100%; height: 100%; }
.donut-center { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); text-align: center; pointer-events: none; }
.donut-total { display: block; font-size: var(--fs-xl); font-weight: var(--fw-bold); color: var(--text-primary); }
.donut-label { display: block; font-size: var(--fs-xs); color: var(--text-tertiary); margin-top: 2px; }
.donut-legend { margin-top: var(--s-2); display: flex; flex-direction: column; gap: 4px; }
.legend-row { display: flex; align-items: center; gap: var(--s-2); padding: 2px 0; }
.legend-dot { width: 6px; height: 6px; border-radius: 50%; flex-shrink: 0; }
.legend-name { flex: 1; font-size: var(--fs-xs); color: var(--text-secondary); }
.legend-val { font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-primary); font-variant-numeric: tabular-nums; }

/* ── Savings bar ── */
.savings-bar { padding: var(--s-4) var(--s-5); margin-bottom: var(--s-6); }
.savings-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--s-3); font-size: var(--fs-sm); font-weight: var(--fw-semibold); }
.savings-pct { font-size: var(--fs-xl); font-weight: var(--fw-bold); }
.savings-pct.good { color: var(--success); }
.savings-pct.warn { color: var(--warning); }
.savings-pct.bad { color: var(--danger); }
.bar-track { height: 8px; border-radius: 4px; background: var(--bg-inset); overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; transition: width 0.8s var(--ease-out); }
.bar-fill.good { background: var(--success); }
.bar-fill.warn { background: var(--warning); }
.bar-fill.bad { background: var(--danger); }
.savings-hint { margin-top: var(--s-2); font-size: var(--fs-xs); color: var(--text-tertiary); }

/* ── Content Split ── */
.content-split { display: grid; grid-template-columns: 1fr 320px; gap: var(--s-4); margin-bottom: var(--s-8); }
.content-main { min-width: 0; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: var(--s-4); }
.section-header h3 { font-size: var(--fs-md); font-weight: var(--fw-semibold); }
.filter-chips { display: flex; gap: var(--s-1); }
.filter-chips button {
  height: 28px; padding: 0 var(--s-3); border-radius: var(--r-pill);
  font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-tertiary);
  transition: all var(--dur-fast);
}
.filter-chips button:hover { color: var(--text-primary); background: var(--bg-hover); }
.filter-chips button.active { color: var(--accent); background: var(--accent-soft); font-weight: var(--fw-semibold); }

.content-side { display: flex; flex-direction: column; gap: var(--s-4); }
.side-card { padding: var(--s-4) var(--s-5); }
.side-title { font-size: var(--fs-sm); font-weight: var(--fw-semibold); margin-bottom: var(--s-3); }

.upcoming-list { display: flex; flex-direction: column; gap: var(--s-3); }
.upcoming-row { display: flex; align-items: center; gap: var(--s-2); }
.upcoming-icon { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.upcoming-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.upcoming-name { font-size: var(--fs-sm); font-weight: var(--fw-medium); }
.upcoming-date { font-size: var(--fs-xs); color: var(--text-tertiary); }
.upcoming-amount { font-size: var(--fs-sm); font-weight: var(--fw-semibold); color: var(--text-primary); font-variant-numeric: tabular-nums; }

.budget-list { display: flex; flex-direction: column; gap: var(--s-3); }
.budget-row { display: flex; flex-direction: column; gap: var(--s-1); }
.budget-info { display: flex; justify-content: space-between; }
.budget-name { font-size: var(--fs-xs); font-weight: var(--fw-medium); }
.budget-meta { font-size: var(--fs-xs); color: var(--text-tertiary); font-variant-numeric: tabular-nums; }

.empty-note { font-size: var(--fs-xs); color: var(--text-tertiary); text-align: center; padding: var(--s-4) 0; }

.msg { margin: var(--s-3) 0 0; padding: var(--s-2) var(--s-4); border-radius: var(--r-md); font-size: var(--fs-sm); font-weight: var(--fw-medium); }
.msg.success { color: var(--success); background: var(--success-soft); }
.msg.error { color: var(--danger); background: var(--danger-soft); }

.receipt-section { margin-bottom: var(--s-8); }

@media (max-width: 1200px) { .kpi-grid { grid-template-columns: repeat(2, 1fr); } .charts-row { grid-template-columns: 1fr; } }
@media (max-width: 900px) { .content-split { grid-template-columns: 1fr; } }
@media (max-width: 640px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .dashboard { padding: var(--s-4); }
  .topbar { flex-direction: column; gap: var(--s-3); }
}
</style>