<template>
  <Transition name="modal">
    <div v-if="visible" class="overlay" @click.self="$emit('close')">
      <div class="sheet">
        <div class="sheet-head">
          <h2>{{ editing ? t.editTransaction : t.newTransaction }}</h2>
          <button class="close-btn" @click="$emit('close')">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>
          </button>
        </div>
        <form class="sheet-body" @submit.prevent="submitForm">
          <div class="type-row">
            <button :class="{active:form.type==='expense'}" type="button" class="type-btn tb-ex" @click="form.type='expense'">{{ t.expenses }}</button>
            <button :class="{active:form.type==='income'}" type="button" class="type-btn tb-in" @click="form.type='income'">{{ t.income }}</button>
          </div>
          <div class="amt-field">
            <span class="amt-prefix">¥</span>
            <input v-model="amountDisplay" type="text" inputmode="decimal" class="amt-input" :class="[form.type,{flash:amtFlash}]" placeholder="0.00" @input="onAmtInput" @blur="onAmtBlur"/>
          </div>
          <div class="field"><label>{{ t.name }}</label><input v-model.trim="form.name" type="text" :placeholder="t.namePlaceholder"/></div>
          <div class="field">
            <label>{{ t.category }}</label>
            <div class="cat-grid">
              <button v-for="cat in filteredCats" :key="cat.id" type="button" :class="['cat-tile',{sel:form.category_id===cat.id}]" @click="form.category_id=cat.id">{{ cat.name }}</button>
            </div>
          </div>
          <div class="field-row">
            <div class="field" style="flex:1"><label>{{ t.date }}</label><input v-model="form.consume_date" type="date"/></div>
            <div class="field" style="flex:1"><label>{{ t.note }}</label><input v-model.trim="form.remark" type="text" :placeholder="t.notePlaceholder"/></div>
          </div>
          <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
          <button class="submit-btn" :disabled="submitting" type="submit">{{ submitting ? t.saving : submitLabel }}</button>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from '../i18n/locale'
import { playCoinDrop, playWhoosh, playTick, playError } from '../utils/sounds'
const props = defineProps({ visible:{type:Boolean,default:false}, categories:{type:Array,default:()=>[]}, editingBill:{type:Object,default:null}, submitting:{type:Boolean,default:false} })
const emit = defineEmits(['submit','cancel','close'])
const { t } = useI18n()
const today = new Date().toISOString().slice(0,10)
const empty = () => ({ id:null, name:'', type:'expense', category_id:'', amount:'', consume_date:today, remark:'' })
const form = reactive(empty())
const errorMsg = ref(''), amountDisplay = ref(''), amtFlash = ref(false)
const editing = computed(()=>!!props.editingBill?.id)
const submitLabel = computed(()=>editing.value?t.value.saveChanges:t.value.addTransaction)
const filteredCats = computed(()=>props.categories.filter(c=>c.type===form.type))

const onAmtInput = (e) => { const r=e.target.value.replace(/[^\d.]/g,''); const p=r.split('.'); if(p.length>2)return; if(p[1]&&p[1].length>2)return; amountDisplay.value=r; form.amount=r; if(r.length>0)playTick() }
const onAmtBlur = () => { const n=parseFloat(amountDisplay.value); if(!isNaN(n))amountDisplay.value=n.toLocaleString('en-US',{maximumFractionDigits:2}) }
const reset = () => { Object.assign(form,empty()); amountDisplay.value=''; errorMsg.value=''; amtFlash.value=false }

const validate = () => { if(!form.name)return t.value.validationName; if(!form.category_id)return t.value.validationCategory; if(form.amount===''||Number(form.amount)<=0)return t.value.validationAmount; if(!form.consume_date)return t.value.validationDate; return'' }

const submitForm = () => { const m=validate(); if(m){errorMsg.value=m;playError();return} errorMsg.value=''; if(form.type==='income')playCoinDrop();else playWhoosh(); amtFlash.value=true; setTimeout(()=>{amtFlash.value=false},400); emit('submit',{id:form.id,name:form.name,type:form.type,category_id:form.category_id,amount:Number(form.amount),consume_date:form.consume_date,remark:form.remark}); if(!editing.value)reset() }

watch(()=>props.editingBill,(bill)=>{if(!bill){reset();return}Object.assign(form,{id:bill.id,name:bill.name,type:bill.type,category_id:bill.category_id,amount:bill.amount,consume_date:bill.consume_date,remark:bill.remark||''});amountDisplay.value=Number(bill.amount).toLocaleString('en-US',{maximumFractionDigits:2})},{immediate:true})
watch(()=>form.type,()=>{const v=filteredCats.value.some(c=>c.id===form.category_id); if(!v)form.category_id='';errorMsg.value=''})
watch(()=>props.visible,(v)=>{if(!v)reset()})
</script>

<style scoped>
.overlay { position:fixed; inset:0; z-index:100; display:grid; place-items:center; background:rgba(0,0,0,0.55); backdrop-filter:blur(8px); }
.sheet { width:440px; max-height:90vh; overflow-y:auto; background:var(--bg-raised); border:1px solid var(--border-light); border-radius:var(--r-xl); box-shadow:var(--shadow-xl); padding:var(--s-6); }
.sheet-head { display:flex; align-items:center; justify-content:space-between; margin-bottom:var(--s-5); }
.sheet-head h2 { font-size:var(--fs-lg); font-weight:var(--fw-semibold); }
.close-btn { width:32px; height:32px; border-radius:var(--r-sm); color:var(--text-tertiary); display:grid; place-items:center; transition:all var(--dur-fast); }
.close-btn:hover { background:var(--bg-hover); color:var(--text-primary); }
.sheet-body { display:flex; flex-direction:column; gap:var(--s-4); }

.type-row { display:flex; gap:var(--s-2); }
.type-btn { flex:1; height:38px; border-radius:var(--r-md); border:1px solid var(--border-default); font-size:var(--fs-sm); font-weight:var(--fw-medium); color:var(--text-tertiary); transition:all var(--dur-fast); }
.type-btn:hover { border-color:var(--border-strong); }
.tb-ex.active { background:var(--danger-soft); border-color:var(--danger); color:var(--danger); }
.tb-in.active { background:var(--success-soft); border-color:var(--success); color:var(--success); }

.amt-field { display:flex; align-items:center; justify-content:center; gap:var(--s-1); padding:var(--s-3) 0; border-bottom:2px solid var(--border-light); transition:border-color var(--dur-fast); }
.amt-field:focus-within { border-bottom-color:var(--accent); }
.amt-prefix { font-size:28px; color:var(--text-tertiary); }
.amt-input { width:180px; border:0; background:transparent; font-size:36px; font-weight:var(--fw-semibold); text-align:center; font-variant-numeric:tabular-nums; outline:none; color:var(--text-primary); }
.amt-input.expense { color:var(--danger); }
.amt-input.income { color:var(--success); }
.amt-input::placeholder { color:var(--text-tertiary); opacity:0.4; }

@keyframes flash { 0%{transform:scale(1)} 30%{transform:scale(1.06)} 60%{transform:scale(0.98)} 100%{transform:scale(1)} }
.amt-input.flash { animation:flash 0.4s var(--ease-out); }

.field { display:flex; flex-direction:column; gap:var(--s-1); }
.field label { font-size:var(--fs-xs); font-weight:var(--fw-medium); color:var(--text-secondary); text-transform:uppercase; letter-spacing:var(--ls-caps); }
.field input { height:38px; border:1px solid var(--border-default); border-radius:var(--r-md); padding:0 var(--s-3); font-size:var(--fs-sm); background:var(--bg-inset); transition:all var(--dur-fast); }
.field input:focus { border-color:var(--accent); box-shadow:var(--shadow-input); outline:none; }
.field-row { display:flex; gap:var(--s-3); }

.cat-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:var(--s-2); }
.cat-tile { height:36px; border:1px solid var(--border-default); border-radius:var(--r-md); font-size:var(--fs-xs); font-weight:var(--fw-medium); color:var(--text-tertiary); transition:all var(--dur-fast); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.cat-tile:hover { border-color:var(--border-strong); color:var(--text-secondary); }
.cat-tile.sel { background:var(--accent-soft); border-color:var(--accent); color:var(--accent); font-weight:var(--fw-semibold); }

.err { padding:var(--s-2) var(--s-3); border-radius:var(--r-md); background:var(--danger-soft); color:var(--danger); font-size:var(--fs-sm); font-weight:var(--fw-medium); }

.submit-btn { width:100%; height:42px; border-radius:var(--r-md); background:var(--accent); color:#fff; font-size:var(--fs-sm); font-weight:var(--fw-semibold); transition:all var(--dur-fast); }
.submit-btn:hover { background:var(--accent-hover); }
.submit-btn:disabled { opacity:0.5; cursor:not-allowed; }

.modal-enter-active { transition:all 0.2s var(--ease-out); }
.modal-leave-active { transition:all 0.15s var(--ease-out); }
.modal-enter-from { opacity:0; }
.modal-enter-from .sheet { transform:scale(0.96) translateY(8px); opacity:0; }
.modal-leave-to { opacity:0; }
.modal-leave-to .sheet { transform:scale(0.96) translateY(8px); opacity:0; }

@media (max-width:480px) { .overlay { align-items:flex-end; } .sheet { width:100%; border-radius:var(--r-xl) var(--r-xl) 0 0; } }
</style>
