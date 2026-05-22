<template>
  <Transition name="modal">
    <div v-if="visible" class="confirm-overlay" @click.self="$emit('cancel')">
      <div class="confirm-dialog">
        <div class="confirm-icon-wrap">
          <svg class="confirm-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="13" />
            <circle cx="12" cy="16" r="0.5" fill="currentColor" />
          </svg>
        </div>
        <p class="confirm-message">{{ message }}</p>
        <div class="confirm-actions">
          <button class="confirm-btn confirm-btn--ghost" @click="$emit('cancel')">{{ cancelText }}</button>
          <button class="confirm-btn confirm-btn--danger" @click="$emit('confirm')">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  message: { type: String, default: '' },
  confirmText: { type: String, default: '' },
  cancelText: { type: String, default: '' }
})

defineEmits(['confirm', 'cancel'])
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: grid;
  place-items: center;
  padding: var(--space-6);
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.confirm-dialog {
  width: min(400px, 100%);
  padding: var(--space-8) var(--space-6) var(--space-6);
  border-radius: var(--radius-2xl);
  display: grid;
  gap: var(--space-5);
  justify-items: center;
  text-align: center;
  background: var(--surface-2);
  border: 1px solid var(--border-subtle);
  box-shadow:
    var(--shadow-lg),
    0 0 60px -10px rgba(255, 77, 106, 0.08);
  position: relative;
  overflow: hidden;
}

.confirm-dialog::before {
  content: '';
  position: absolute;
  top: 0;
  left: var(--space-6);
  right: var(--space-6);
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 77, 106, 0.2), transparent);
  pointer-events: none;
}

.confirm-icon-wrap {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  background: var(--expense-bg);
  border: 1px solid rgba(255, 77, 106, 0.18);
  box-shadow: 0 0 20px rgba(255, 77, 106, 0.1);
}

.confirm-icon {
  width: 26px;
  height: 26px;
  color: var(--expense);
}

.confirm-message {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--text-md);
  line-height: 1.6;
}

.confirm-actions {
  display: flex;
  gap: var(--space-2);
  width: 100%;
}

.confirm-btn {
  flex: 1;
  min-height: 42px;
  border: 0;
  border-radius: var(--radius-pill);
  cursor: pointer;
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast);
}

.confirm-btn--ghost {
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid var(--border-subtle);
}

.confirm-btn--ghost:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: var(--border-default);
}

.confirm-btn--danger {
  color: #fff;
  background: linear-gradient(135deg, var(--expense), #e8385a);
  box-shadow: 0 8px 20px rgba(255, 77, 106, 0.25);
  position: relative;
  overflow: hidden;
}

.confirm-btn--danger::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.1), transparent 60%);
  pointer-events: none;
}

.confirm-btn--danger:hover {
  box-shadow: 0 12px 28px rgba(255, 77, 106, 0.4);
  transform: translateY(-1px);
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

.modal-enter-from .confirm-dialog {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-leave-to .confirm-dialog {
  transform: scale(0.96) translateY(8px);
  opacity: 0;
}
</style>
