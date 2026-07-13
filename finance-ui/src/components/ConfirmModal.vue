<template>
  <Transition name="modal">
    <div v-if="visible" class="overlay" @click.self="$emit('cancel')">
      <div class="dialog">
        <div class="icon-box"><svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="13"/><circle cx="12" cy="16" r="0.5" fill="currentColor"/></svg></div>
        <p>{{ message }}</p>
        <div class="actions">
          <button class="btn-cancel" @click="$emit('cancel')">{{ cancelText }}</button>
          <button class="btn-confirm" @click="$emit('confirm')">{{ confirmText }}</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({ visible:{type:Boolean,default:false}, message:{type:String,default:''}, confirmText:{type:String,default:''}, cancelText:{type:String,default:''} })
defineEmits(['confirm','cancel'])
</script>

<style scoped>
.overlay { position:fixed; inset:0; z-index:10000; display:grid; place-items:center; background:rgba(0,0,0,0.55); backdrop-filter:blur(8px); padding:var(--s-6); }
.dialog { width:min(380px,100%); padding:var(--s-8) var(--s-6) var(--s-6); border-radius:var(--r-xl); background:var(--bg-raised); border:1px solid var(--border-light); box-shadow:var(--shadow-xl); text-align:center; display:flex; flex-direction:column; align-items:center; gap:var(--s-5); }
.icon-box { width:48px; height:48px; border-radius:50%; background:var(--danger-soft); color:var(--danger); display:grid; place-items:center; }
p { font-size:var(--fs-base); color:var(--text-secondary); line-height:1.6; }
.actions { display:flex; gap:var(--s-2); width:100%; }
.actions button { flex:1; height:42px; border-radius:var(--r-md); font-size:var(--fs-sm); font-weight:var(--fw-semibold); transition:all var(--dur-fast); }
.btn-cancel { background:var(--bg-inset); color:var(--text-secondary); }
.btn-cancel:hover { background:var(--bg-hover); }
.btn-confirm { background:var(--danger); color:#fff; }
.btn-confirm:hover { opacity:0.9; }

.modal-enter-active { transition:all 0.2s var(--ease-out); }
.modal-leave-active { transition:all 0.15s var(--ease-out); }
.modal-enter-from { opacity:0; }
.modal-enter-from .dialog { transform:scale(0.96) translateY(8px); opacity:0; }
.modal-leave-to { opacity:0; }
.modal-leave-to .dialog { transform:scale(0.96) translateY(8px); opacity:0; }
</style>
