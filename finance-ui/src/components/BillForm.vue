<template>
  <form class="bill-form surface-panel" @submit.prevent="submitForm">
    <div class="form-header">
      <h2 class="form-title">{{ editing ? t.updateBill : t.addBill }}</h2>
    </div>

    <div class="type-switch">
      <button :class="{ active: form.type === 'expense' }" type="button" @click="form.type = 'expense'">{{ t.expense }}</button>
      <button :class="{ active: form.type === 'income' }" type="button" @click="form.type = 'income'">{{ t.income }}</button>
    </div>

    <div class="form-grid">
      <label class="field">
        <span>{{ t.name }}</span>
        <input v-model.trim="form.name" type="text" :placeholder="t.billNamePlaceholder" />
      </label>

      <label class="field">
        <span>{{ t.category }}</span>
        <select v-model.number="form.category_id">
          <option value="" disabled>{{ t.selectCategory }}</option>
          <option v-for="category in filteredCategories" :key="category.id" :value="category.id">
            {{ category.name }}
          </option>
        </select>
      </label>

      <label class="field">
        <span>{{ t.amount }}</span>
        <input v-model.number="form.amount" min="0" step="0.01" type="number" :placeholder="t.amountPlaceholder" />
      </label>

      <label class="field">
        <span>{{ t.date }}</span>
        <input v-model="form.consume_date" type="date" />
      </label>

      <label class="field field--wide">
        <span>{{ t.remark }}</span>
        <input v-model.trim="form.remark" type="text" :placeholder="t.remarkPlaceholder" />
      </label>
    </div>

    <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>

    <div class="form-actions">
      <button class="btn btn-primary" :disabled="submitting" type="submit">
        {{ submitting ? t.saving : submitText }}
      </button>
      <button v-if="editing" class="btn btn-ghost" type="button" @click="cancelEdit">{{ t.cancel }}</button>
    </div>
  </form>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useI18n } from '../i18n/locale'

const props = defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  editingBill: {
    type: Object,
    default: null
  },
  submitting: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['submit', 'cancel'])
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
const editing = computed(() => Boolean(props.editingBill?.id))
const submitText = computed(() => (editing.value ? t.value.updateBill : t.value.addBill))

const filteredCategories = computed(() => {
  return props.categories.filter((category) => category.type === form.type)
})

const resetForm = () => {
  Object.assign(form, createEmptyForm())
  errorMessage.value = ''
}

const validateForm = () => {
  if (!form.name) return t.value.validationBillName
  if (!form.category_id) return t.value.validationBillCategory
  if (form.amount === '' || Number(form.amount) <= 0) return t.value.validationBillAmount
  if (!form.consume_date) return t.value.validationBillDate
  return ''
}

const submitForm = () => {
  const validationMessage = validateForm()
  if (validationMessage) {
    errorMessage.value = validationMessage
    return
  }

  errorMessage.value = ''
  emit('submit', {
    id: form.id,
    name: form.name,
    type: form.type,
    category_id: form.category_id,
    amount: Number(form.amount),
    consume_date: form.consume_date,
    remark: form.remark
  })

  if (!editing.value) {
    resetForm()
  }
}

const cancelEdit = () => {
  resetForm()
  emit('cancel')
}

watch(
  () => props.editingBill,
  (bill) => {
    if (!bill) {
      resetForm()
      return
    }

    Object.assign(form, {
      id: bill.id,
      name: bill.name,
      type: bill.type,
      category_id: bill.category_id,
      amount: bill.amount,
      consume_date: bill.consume_date,
      remark: bill.remark || ''
    })
  },
  { immediate: true }
)

watch(
  () => form.type,
  () => {
    const stillValid = filteredCategories.value.some((category) => category.id === form.category_id)
    if (!stillValid) {
      form.category_id = ''
    }
    errorMessage.value = ''
  }
)
</script>

<style scoped>
.bill-form {
  padding: 20px 0;
  border-bottom: 1px solid var(--divider-subtle);
  display: grid;
  gap: 16px;
}

.form-title {
  margin: 0;
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

/* ── Type Switch ── */
.type-switch {
  display: inline-flex;
  gap: 2px;
  padding: 2px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
}

.type-switch button {
  height: 34px;
  min-width: 90px;
  border: 0;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  background: transparent;
  cursor: pointer;
  font-weight: var(--font-weight-semibold);
  font-size: var(--font-size-md);
  transition: all 180ms cubic-bezier(0.4, 0, 0.2, 1);
}

.type-switch button.active {
  color: var(--text-primary);
}

.type-switch button.active:first-child {
  background: var(--expense);
}

.type-switch button.active:last-child {
  background: var(--income);
}

/* ── Form Grid ── */
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.field {
  display: grid;
  gap: 4px;
}

.field span {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.field--wide {
  grid-column: span 2;
}

input,
select {
  width: 100%;
  height: var(--input-height);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 0 10px;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.03);
  font-size: var(--font-size-md);
  transition: all 180ms cubic-bezier(0.4, 0, 0.2, 1);
}

input:focus,
select:focus {
  border-color: var(--accent);
  outline: none;
  box-shadow: 0 0 0 2px var(--accent-muted);
}

select option {
  color: var(--text-primary);
  background: var(--surface-3);
}

/* ── Error ── */
.form-error {
  color: var(--expense);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  margin: 0;
}

/* ── Actions ── */
.form-actions {
  display: flex;
  gap: 8px;
}

.btn {
  display: inline-flex;
  align-items: center;
  height: var(--button-height);
  padding: 0 16px;
  border: 0;
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-medium);
  cursor: pointer;
  transition: all 180ms cubic-bezier(0.4, 0, 0.2, 1);
}

.btn:hover {
  transform: translateY(-1px);
}

.btn-primary {
  color: #fff;
  background: var(--accent);
}

.btn-primary:hover {
  background: #2563EB;
}

.btn-primary:disabled {
  cursor: not-allowed;
  opacity: 0.5;
  transform: none;
}

.btn-ghost {
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
}

.btn-ghost:hover {
  background: rgba(255, 255, 255, 0.06);
}

@media (max-width: 900px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .field--wide {
    grid-column: span 1;
  }
}
</style>
