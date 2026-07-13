<template>
  <aside class="sidebar">
    <div class="sidebar-brand">
      <div class="logo">
        <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
          <path d="M10 2l6 4.5v9L10 18l-6-4.5v-9L10 2z" stroke="#fff" stroke-width="1.6" stroke-linejoin="round"/>
          <path d="M10 2v16M4 6.5l12 9M16 6.5l-12 9" stroke="#fff" stroke-width="0.5" opacity="0.5"/>
        </svg>
      </div>
      <span class="brand-name">{{ t.appName }}</span>
    </div>

    <nav class="nav">
      <div class="nav-group">
        <span class="nav-label">{{ t.sidebarMain }}</span>
        <button class="nav-item active">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1" y="1" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="11" y="1" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="1" y="11" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/><rect x="11" y="11" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.5"/></svg>
          {{ t.navDashboard }}
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><rect x="1.5" y="1.5" width="15" height="15" rx="2" stroke="currentColor" stroke-width="1.5"/><path d="M1.5 6.5h15M6.5 6.5v10" stroke="currentColor" stroke-width="1.5"/></svg>
          {{ t.navTransactions }}
          <span v-if="billsCount" class="nav-badge">{{ billsCount }}</span>
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M2 16V7.5M6.5 16V2.5M11 16V9.5M15.5 16V5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          {{ t.navBudgets }}
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="6" cy="6" r="4" stroke="currentColor" stroke-width="1.5"/><path d="M9 9l7.5 7.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          {{ t.navAnalytics }}
        </button>
      </div>
      <div class="nav-group">
        <span class="nav-label">{{ t.sidebarMore }}</span>
        <button class="nav-item">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M3 9.5l3 3 9-9" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
          {{ t.navInvestments }}
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><path d="M9 1v3.5M9 13.5V17M2.5 9H6M12 9h3.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/><circle cx="9" cy="9" r="2.5" stroke="currentColor" stroke-width="1.5"/></svg>
          {{ t.navAI }}
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="18" height="18" viewBox="0 0 18 18" fill="none"><circle cx="9" cy="9" r="7" stroke="currentColor" stroke-width="1.5"/><path d="M9 5v4l2.5 2.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg>
          {{ t.navSettings }}
        </button>
      </div>
    </nav>

    <div class="sidebar-footer">
      <div class="lang-switch">
        <button :class="{ active: language === 'zh' }" @click="setLanguage('zh')">中文</button>
        <button :class="{ active: language === 'en' }" @click="setLanguage('en')">EN</button>
      </div>
      <div class="user-row">
        <div class="user-avatar">{{ (currentUser?.username || currentUser?.email || '?').charAt(0).toUpperCase() }}</div>
        <div class="user-meta">
          <span class="user-name">{{ currentUser?.username || currentUser?.email }}</span>
          <span class="user-status">{{ t.signedInAs }}</span>
        </div>
        <button class="logout-btn" @click="$emit('logout')" :title="t.logout">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none"><path d="M6 2H3a1 1 0 00-1 1v10a1 1 0 001 1h3M11 11l3.5-3.5L11 4M14.5 7.5H6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/></svg>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useI18n } from '../i18n/locale'
defineProps({ currentUser: { type: Object, default: null }, billsCount: { type: Number, default: 0 } })
defineEmits(['logout'])
const { language, t, setLanguage } = useI18n()
</script>

<style scoped>
.sidebar {
  position: fixed; top: 0; left: 0; bottom: 0;
  width: var(--sidebar-w);
  display: flex; flex-direction: column;
  background: var(--bg-raised);
  border-right: 1px solid var(--border-light);
  z-index: 10; user-select: none;
  padding: var(--s-4);
}

.sidebar-brand { display: flex; align-items: center; gap: var(--s-2); padding: 0 var(--s-2); margin-bottom: var(--s-6); height: 44px; }
.logo { width: 28px; height: 28px; border-radius: var(--r-sm); background: var(--accent); display: grid; place-items: center; }
.brand-name { font-size: var(--fs-md); font-weight: var(--fw-bold); color: var(--text-primary); letter-spacing: var(--ls-tight); }

.nav { flex: 1; overflow-y: auto; }
.nav-group { margin-bottom: var(--s-5); }
.nav-label { display: block; padding: 0 var(--s-2); margin-bottom: var(--s-1); font-size: var(--fs-caption); font-weight: var(--fw-semibold); color: var(--text-tertiary); text-transform: uppercase; letter-spacing: var(--ls-caps); }

.nav-item {
  display: flex; align-items: center; gap: var(--s-2);
  width: 100%; height: 36px; padding: 0 var(--s-2);
  border-radius: var(--r-md);
  color: var(--text-secondary); font-size: var(--fs-sm); font-weight: var(--fw-medium);
  transition: all var(--dur-fast) var(--ease-out);
}
.nav-item:hover { color: var(--text-primary); background: var(--bg-hover); }
.nav-item.active { color: var(--accent); background: var(--bg-active); font-weight: var(--fw-semibold); }

.nav-icon { flex-shrink: 0; opacity: 0.45; transition: opacity var(--dur-fast); }
.nav-item.active .nav-icon { opacity: 1; }

.nav-badge {
  margin-left: auto; min-width: 18px; height: 18px;
  display: inline-flex; align-items: center; justify-content: center;
  padding: 0 6px; border-radius: var(--r-pill);
  background: var(--accent-soft); color: var(--accent);
  font-size: 10px; font-weight: var(--fw-semibold);
}

.sidebar-footer { padding-top: var(--s-4); border-top: 1px solid var(--border-light); display: flex; flex-direction: column; gap: var(--s-3); }
.lang-switch { display: flex; gap: 2px; padding: 3px; border-radius: var(--r-md); background: var(--bg-inset); }
.lang-switch button { flex: 1; height: 28px; border-radius: var(--r-sm); color: var(--text-tertiary); font-size: var(--fs-xs); font-weight: var(--fw-medium); transition: all var(--dur-fast); }
.lang-switch button.active { color: var(--text-primary); background: var(--bg-raised); box-shadow: var(--shadow-xs); }

.user-row { display: flex; align-items: center; gap: var(--s-2); }
.user-avatar { width: 28px; height: 28px; border-radius: var(--r-sm); background: var(--accent); color: #fff; display: grid; place-items: center; font-size: var(--fs-xs); font-weight: var(--fw-semibold); flex-shrink: 0; }
.user-meta { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.user-name { font-size: var(--fs-sm); font-weight: var(--fw-medium); color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-status { font-size: var(--fs-caption); color: var(--text-tertiary); }
.logout-btn { width: 28px; height: 28px; border-radius: var(--r-sm); color: var(--text-tertiary); display: grid; place-items: center; transition: all var(--dur-fast); }
.logout-btn:hover { color: var(--danger); background: var(--danger-soft); }

@media (max-width: 768px) { .sidebar { display: none; } }
</style>
