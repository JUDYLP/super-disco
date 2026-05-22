<template>
  <Transition name="modal">
    <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-sheet">
        <div class="modal-drag-handle"></div>

        <div class="modal-header">
          <h2 class="modal-title">{{ editing ? t.updateBill : t.addBill }}</h2>
          <button class="modal-close" @click="$emit('close')" type="button">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M4 4l8 8M12 4l-8 8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </button>
        </div>

        <form class="modal-body" @submit.prevent="submitForm">
          <div class="type-toggle">
            <button
              :class="{ active: form.type === 'expense' }"
              type="button"
              class="type-btn type-btn--expense"
              @click="form.type = 'expense'"
            >{{ t.expense }}</button>
            <button
              :class="{ active: form.type === 'income' }"
              type="button"
              class="type-btn type-btn--income"
              @click="form.type = 'income'"
            >{{ t.income }}</button>
          </div>

          <div class="amount-field">
            <span class="amount-currency">¥</span>
            <input
              v-model="amountDisplay"
              type="text"
              inputmode="decimal"
              class="amount-input"
              :class="[form.type, { 'amount-flash': amountFlashing }]"
              placeholder="0.00"
              @input="onAmountInput"
              @blur="onAmountBlur"
            />
            <Transition name="coin-pop">
              <span v-if="showCoinIcon" class="amount-coin" :class="form.type">
                <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
                  <circle cx="9" cy="9" r="7" stroke="currentColor" stroke-width="1.2" />
                  <text x="9" y="12" text-anchor="middle" fill="currentColor" font-size="7" font-weight="600">$</text>
                </svg>
              </span>
            </Transition>
          </div>

          <label class="field">
            <span>{{ t.name }}</span>
            <input v-model.trim="form.name" type="text" :placeholder="t.billNamePlaceholder" />
          </label>

          <div class="field">
            <span>{{ t.category }}</span>
            <div class="category-grid">
              <button
                v-for="cat in filteredCategories"
                :key="cat.id"
                type="button"
                :class="['cat-tile', { selected: form.category_id === cat.id }]"
                @click="form.category_id = cat.id"
              >
                {{ cat.name }}
              </button>
            </div>
          </div>

          <div class="field-row">
            <label class="field field--half">
              <span>{{ t.date }}</span>
              <input v-model="form.consume_date" type="date" />
            </label>
            <label class="field field--half">
              <span>{{ t.remark }}</span>
              <input v-model.trim="form.remark" type="text" :placeholder="t.remarkPlaceholder" />
            </label>
          </div>

          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

          <button class="submit-btn" :disabled="submitting" type="submit">
            {{ submitting ? t.saving : submitText }}
          </button>
        </form>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from '../i18n/locale'
import { playCoinDrop, playWhoosh, playTick, playError } from '../utils/sounds'

const props = defineProps({
  visible: { type: Boolean, default: false },
  categories: { type: Array, default: () => [] },
  editingBill: { type: Object, default: null },
  submitting: { type: Boolean, default: false }
})

const emit = defineEmits(['submit', 'cancel', 'close'])
const { t } = useI18n()

const today = new Date().toISOString().slice(0, 10)

const createEmptyForm = () => ({
  id: null,
  name: '',
  type: 'expense',
  category_id: '',
  amount: '',
  consume_date: today,
  remark: ''
})

const form = reactive(createEmptyForm())
const errorMessage = ref('')
const amountDisplay = ref('')
const amountFlashing = ref(false)
const showCoinIcon = ref(false)
const editing = computed(() => Boolean(props.editingBill?.id))
const submitText = computed(() => (editing.value ? t.value.updateBill : t.value.addBill))

const filteredCategories = computed(() =>
  props.categories.filter((c) => c.type === form.type)
)

const onAmountInput = (e) => {
  const raw = e.target.value.replace(/[^\d.]/g, '')
  const parts = raw.split('.')
  if (parts.length > 2) return
  if (parts[1] && parts[1].length > 2) return
  amountDisplay.value = raw
  form.amount = raw

  // Tick sound on digit input
  if (raw.length > 0) {
    playTick()
    // Show coin icon briefly
    showCoinIcon.value = true
    setTimeout(() => { showCoinIcon.value = false }, 400)
  }
}

const onAmountBlur = () => {
  const num = parseFloat(amountDisplay.value)
  if (!isNaN(num)) {
    amountDisplay.value = num.toLocaleString('en-US', { maximumFractionDigits: 2 })
  }
}

const resetForm = () => {
  Object.assign(form, createEmptyForm())
  amountDisplay.value = ''
  errorMessage.value = ''
  amountFlashing.value = false
  showCoinIcon.value = false
}

const validateForm = () => {
  if (!form.name) return t.value.validationBillName
  if (!form.category_id) return t.value.validationBillCategory
  if (form.amount === '' || Number(form.amount) <= 0) return t.value.validationBillAmount
  if (!form.consume_date) return t.value.validationBillDate
  return ''
}

const submitForm = () => {
  const msg = validateForm()
  if (msg) {
    errorMessage.value = msg
    playError()
    return
  }
  errorMessage.value = ''

  // Play sound effect based on type
  if (form.type === 'income') {
    playCoinDrop()
  } else {
    playWhoosh()
  }

  // Flash the amount field
  amountFlashing.value = true
  setTimeout(() => { amountFlashing.value = false }, 400)

  emit('submit', {
    id: form.id,
    name: form.name,
    type: form.type,
    category_id: form.category_id,
    amount: Number(form.amount),
    consume_date: form.consume_date,
    remark: form.remark
  })
  if (!editing.value) resetForm()
}

watch(() => props.editingBill, (bill) => {
  if (!bill) { resetForm(); return }
  Object.assign(form, {
    id: bill.id,
    name: bill.name,
    type: bill.type,
    category_id: bill.category_id,
    amount: bill.amount,
    consume_date: bill.consume_date,
    remark: bill.remark || ''
  })
  amountDisplay.value = Number(bill.amount).toLocaleString('en-US', { maximumFractionDigits: 2 })
}, { immediate: true })

watch(() => form.type, () => {
  const valid = filteredCategories.value.some((c) => c.id === form.category_id)
  if (!valid) form.category_id = ''
  errorMessage.value = ''
})

watch(() => props.visible, (v) => {
  if (!v) resetForm()
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 100;
  display: grid;
  place-items: center;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.modal-sheet {
  width: 440px;
  max-height: 90vh;
  overflow-y: auto;
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-2xl);
  box-shadow:
    var(--shadow-xl),
    0 0 60px -10px rgba(108, 124, 255, 0.1);
  padding: var(--space-6);
  position: relative;
}

.modal-sheet::before {
  content: '';
  position: absolute;
  top: 0;
  left: var(--space-6);
  right: var(--space-6);
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(108, 124, 255, 0.2), transparent);
  pointer-events: none;
}

.modal-drag-handle {
  display: none;
  width: 32px;
  height: 4px;
  border-radius: 2px;
  background: var(--border-default);
  margin: 0 auto var(--space-4);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.modal-title {
  margin: 0;
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-primary);
}

.modal-close {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
  transition: all var(--motion-fast);
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.06);
  color: var(--text-primary);
}

.modal-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.type-toggle {
  display: flex;
  gap: var(--space-2);
}

.type-btn {
  flex: 1;
  height: 36px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-muted);
  font-size: var(--text-md);
  font-weight: var(--weight-semibold);
  cursor: pointer;
  transition: all var(--motion-fast);
}

.type-btn--expense.active {
  background: var(--expense-bg);
  border-color: var(--expense);
  color: var(--expense);
  box-shadow: 0 0 16px rgba(255, 77, 106, 0.15);
}

.type-btn--income.active {
  background: var(--income-bg);
  border-color: var(--income);
  color: var(--income);
  box-shadow: 0 0 16px rgba(0, 232, 157, 0.15);
}

.amount-field {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  padding: var(--space-4) 0;
  border-bottom: 2px solid var(--border-hairline);
  transition: border-color var(--motion-fast);
}

.amount-field:focus-within {
  border-bottom-color: var(--accent);
  box-shadow: 0 2px 8px rgba(108, 124, 255, 0.1);
}

.amount-currency {
  font-size: 28px;
  font-weight: var(--weight-normal);
  color: var(--text-muted);
  opacity: 0.55;
}

.amount-input {
  width: 200px;
  border: 0;
  background: transparent;
  color: var(--text-primary);
  font-size: 40px;
  font-family: var(--font-mono);
  font-weight: var(--weight-semibold);
  text-align: center;
  font-variant-numeric: tabular-nums;
  outline: none;
}

.amount-input.income { color: var(--income); text-shadow: 0 0 20px rgba(0, 232, 157, 0.15); }
.amount-input.expense { color: var(--expense); text-shadow: 0 0 20px rgba(255, 77, 106, 0.15); }

.amount-input::placeholder {
  color: var(--text-muted);
  opacity: 0.3;
}

/* Amount flash animation on submit */
@keyframes amountFlash {
  0% { transform: scale(1); }
  30% { transform: scale(1.06); }
  60% { transform: scale(0.98); }
  100% { transform: scale(1); }
}

.amount-flash {
  animation: amountFlash 400ms cubic-bezier(0.34, 1.56, 0.64, 1);
}

.amount-flash.income {
  text-shadow: 0 0 30px rgba(0, 232, 157, 0.4) !important;
}

.amount-flash.expense {
  text-shadow: 0 0 30px rgba(255, 77, 106, 0.4) !important;
}

/* Coin icon next to amount */
.amount-coin {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.amount-coin.income { color: var(--income); }
.amount-coin.expense { color: var(--expense); }

.coin-pop-enter-active {
  transition: all 300ms cubic-bezier(0.34, 1.56, 0.64, 1);
}
.coin-pop-leave-active {
  transition: all 200ms cubic-bezier(0, 0, 0.2, 1);
}
.coin-pop-enter-from {
  opacity: 0;
  transform: scale(0.3) rotate(-30deg);
}
.coin-pop-leave-to {
  opacity: 0;
  transform: scale(0.5) translateY(-8px);
}

.field {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.field span {
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.field input,
.field select {
  height: 36px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 0 var(--space-3);
  background: var(--surface-3);
  color: var(--text-primary);
  font-size: var(--text-md);
  transition: all var(--motion-fast);
}

.field input:focus {
  border-color: var(--accent);
  outline: none;
  box-shadow: 0 0 0 2px var(--accent-bg);
}

.field-row {
  display: flex;
  gap: var(--space-3);
}

.field--half {
  flex: 1;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-2);
}

.cat-tile {
  height: 36px;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  background: var(--surface-3);
  color: var(--text-muted);
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: all var(--motion-fast);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cat-tile:hover {
  border-color: var(--border-default);
  color: var(--text-secondary);
  background: rgba(108, 124, 255, 0.04);
}

.cat-tile.selected {
  background: var(--accent-bg);
  border-color: var(--accent);
  color: var(--accent);
  box-shadow: 0 0 12px rgba(108, 124, 255, 0.15);
}

.form-error {
  margin: 0;
  color: var(--expense);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  padding: var(--space-2) var(--space-3);
  background: var(--expense-bg);
  border-radius: var(--radius-md);
  border: 1px solid rgba(255, 77, 106, 0.12);
}

.submit-btn {
  width: 100%;
  height: 38px;
  border: 0;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  color: #fff;
  font-size: var(--text-md);
  font-weight: var(--weight-semibold);
  cursor: pointer;
  transition: all var(--motion-fast);
  box-shadow: 0 4px 16px rgba(108, 124, 255, 0.3);
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), transparent 60%);
  pointer-events: none;
}

.submit-btn:hover {
  box-shadow: 0 6px 24px rgba(108, 124, 255, 0.45);
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.modal-enter-active {
  transition: all 220ms cubic-bezier(0, 0, 0.2, 1);
}

.modal-leave-active {
  transition: all 160ms cubic-bezier(0, 0, 0.2, 1);
}

.modal-enter-from {
  opacity: 0;
}

.modal-enter-from .modal-sheet {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-leave-to .modal-sheet {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}

@media (max-width: 480px) {
  .modal-overlay {
    align-items: flex-end;
  }

  .modal-sheet {
    width: 100%;
    max-height: 85vh;
    border-radius: var(--radius-2xl) var(--radius-2xl) 0 0;
    padding: var(--space-4) var(--space-5) var(--space-6);
  }

  .modal-drag-handle {
    display: block;
  }
}
</style>
