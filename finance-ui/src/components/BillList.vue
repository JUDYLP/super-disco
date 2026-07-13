<template>
  <section class="bill-list">
    <div v-if="loading" class="sk-list">
      <div v-for="n in 5" :key="n" class="sk-row">
        <span class="skeleton sk-circle"></span><span class="skeleton sk-name"></span><span class="skeleton sk-cat"></span><span class="skeleton sk-date"></span><span class="skeleton sk-amt"></span>
      </div>
    </div>
    <div v-else-if="bills.length === 0" class="empty-wrap">
      <p class="empty-text">{{ t.noTransactions }}</p>
      <button class="btn-ghost" @click="$emit('refresh')">{{ t.addFirstTransaction }}</button>
    </div>
    <div v-else class="table-wrap">
      <table>
        <thead><tr><th>{{ t.description }}</th><th>{{ t.category }}</th><th>{{ t.type }}</th><th>{{ t.date }}</th><th>{{ t.amount }}</th><th></th></tr></thead>
        <tbody>
          <TransactionRow v-for="bill in bills" :key="bill.id" :bill="bill" :initial="getInit(bill.name)" :date-display="fmtDate(bill.consume_date)" :category-name="getCat(bill.category_id)" :type-label="bill.type==='income'?t.income:t.expenses" :amount-display="fmtAmt(bill.amount)" @edit="$emit('edit',bill)" @delete="$emit('delete',bill)" />
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { useI18n } from '../i18n/locale'
import TransactionRow from './TransactionRow.vue'
const props = defineProps({ bills:{type:Array,default:()=>[]}, categories:{type:Array,default:()=>[]}, loading:{type:Boolean,default:false} })
defineEmits(['edit','delete','refresh'])
const { language, t } = useI18n()
const getInit = (n) => n ? n.trim().charAt(0).toUpperCase() : '?'
const fmtAmt = (a) => { const n=Number(a)||0; const l=language.value==='zh'?'zh-CN':'en-US'; const c=language.value==='zh'?'CNY':'USD'; return new Intl.NumberFormat(l,{style:'currency',currency:c}).format(n) }
const fmtDate = (d) => { if(!d)return'-'; const p=d.split('-'); if(p.length===3) return language.value==='zh'?`${p[1]}/${p[2]}`:`${p[1]}/${p[2]}/${p[0].slice(2)}`; return d }
const getCat = (id) => { const c=props.categories.find(i=>i.id===id); return c?c.name:'...' }
</script>

<style scoped>
.bill-list { min-width: 0; }
.empty-wrap { text-align: center; padding: var(--s-12) var(--s-4); display: flex; flex-direction: column; align-items: center; gap: var(--s-3); }
.empty-text { font-size: var(--fs-base); color: var(--text-tertiary); }
.btn-ghost { display: inline-flex; align-items: center; height: 34px; padding: 0 var(--s-4); border: 1px solid var(--border-default); border-radius: var(--r-md); font-size: var(--fs-sm); font-weight: var(--fw-medium); color: var(--accent); transition: all var(--dur-fast); }
.btn-ghost:hover { background: var(--accent-soft); }

.sk-list { display: flex; flex-direction: column; }
.sk-row { display: flex; align-items: center; gap: var(--s-3); height: 48px; padding: 0 var(--s-2); }
.skeleton { height: 12px; border-radius: var(--r-sm); background: linear-gradient(90deg,var(--bg-inset)25%,var(--bg-hover)50%,var(--bg-inset)75%); background-size:200% 100%; animation:skeleton-shimmer 1.5s infinite; }
.sk-circle { width: 32px; height: 32px; border-radius: 50%; }
.sk-name { flex:1; max-width:140px; }
.sk-cat { width:80px; }
.sk-date { width:60px; }
.sk-amt { width:80px; margin-left:auto; }

.table-wrap { overflow-x: auto; }
table { width:100%; border-collapse: collapse; min-width: 600px; }
th { text-align: left; padding: 0 var(--s-3); font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-tertiary); text-transform: uppercase; letter-spacing: var(--ls-caps); height: 36px; border-bottom: 1px solid var(--border-light); white-space: nowrap; }
</style>
