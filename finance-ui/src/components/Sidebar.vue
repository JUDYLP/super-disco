<template>
  <aside class="sidebar">
    <div class="sidebar-glow"></div>

    <div class="sidebar-header">
      <div class="logo">
        <div class="logo-orb">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <path d="M9 2L14 6.5V13.5L9 16L4 13.5V6.5L9 2Z" stroke="#fff" stroke-width="1.2" stroke-linejoin="round" />
            <path d="M9 2V16M4 6.5L14 13.5M14 6.5L4 13.5" stroke="#fff" stroke-width="0.6" opacity="0.5" />
          </svg>
        </div>
        <span class="logo-text">{{ t.appName }}</span>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="nav-section">
        <span class="nav-section-label">{{ t.sidebarOverview }}</span>
        <button class="nav-item active">
          <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="1" y="1" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.2" />
            <rect x="9" y="1" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.2" />
            <rect x="1" y="9" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.2" />
            <rect x="9" y="9" width="6" height="6" rx="1.5" stroke="currentColor" stroke-width="1.2" />
          </svg>
          {{ t.navDashboard }}
        </button>
      </div>

      <div class="nav-section">
        <span class="nav-section-label">{{ t.sidebarData }}</span>
        <button class="nav-item">
          <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="1.5" y="1.5" width="13" height="13" rx="2" stroke="currentColor" stroke-width="1.2" />
            <path d="M1.5 5.5h13M5.5 5.5v9" stroke="currentColor" stroke-width="1.2" />
          </svg>
          {{ t.navBills }}
          <span v-if="billsCount" class="nav-badge">{{ billsCount }}</span>
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="5" cy="5" r="3.5" stroke="currentColor" stroke-width="1.2" />
            <path d="M7.5 7.5L14 14" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
          </svg>
          {{ t.navCategories }}
        </button>
        <button class="nav-item">
          <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M2 14V6M6 14V2M10 14V8M14 14V4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
          </svg>
          {{ t.navReports }}
        </button>
      </div>

      <div class="nav-section">
        <span class="nav-section-label">{{ t.sidebarTools }}</span>
        <button class="nav-item">
          <svg class="nav-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="8" r="6.5" stroke="currentColor" stroke-width="1.2" />
            <path d="M8 4v4l2.5 2.5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" />
          </svg>
          {{ t.navSettings }}
        </button>
      </div>
    </nav>

    <div class="sidebar-footer">
      <div class="footer-actions">
        <button class="footer-btn" @click="$emit('logout')">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M5 1H2.5A1.5 1.5 0 001 2.5v9A1.5 1.5 0 002.5 13H5M9.5 10L13 7l-3.5-3M13 7H5" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <span>{{ t.logout }}</span>
        </button>
      </div>

      <div class="language-switch">
        <button :class="{ active: language === 'zh' }" @click="setLanguage('zh')">中文</button>
        <button :class="{ active: language === 'en' }" @click="setLanguage('en')">EN</button>
      </div>

      <div class="footer-user">
        <span class="user-dot"></span>
        <div class="user-info">
          <span class="user-name">{{ currentUser?.username || currentUser?.email }}</span>
          <span class="user-status">{{ t.signedInAs }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useI18n } from '../i18n/locale'

defineProps({
  currentUser: { type: Object, default: null },
  billsCount: { type: Number, default: 0 }
})

defineEmits(['logout'])

const { language, t, setLanguage } = useI18n()
</script>

<style scoped>
.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  background: var(--surface-1);
  border-right: 1px solid var(--border-hairline);
  z-index: 10;
  user-select: none;
  overflow: hidden;
}

.sidebar-glow {
  position: absolute;
  top: -40%;
  left: -60%;
  width: 200%;
  height: 80%;
  background: radial-gradient(ellipse at center, rgba(108, 124, 255, 0.06), transparent 60%);
  pointer-events: none;
  animation: sidebarGlow 8s ease-in-out infinite alternate;
}

@keyframes sidebarGlow {
  0%   { transform: translate(0, 0); opacity: 0.5; }
  100% { transform: translate(10%, 20%); opacity: 0.8; }
}

.sidebar-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 var(--space-4);
  position: relative;
  z-index: 1;
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.logo-orb {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  box-shadow: 0 4px 16px rgba(108, 124, 255, 0.35);
  animation: logoPulse 4s ease-in-out infinite;
  position: relative;
}

.logo-orb::after {
  content: '';
  position: absolute;
  inset: -2px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(108, 124, 255, 0.4), rgba(139, 92, 246, 0.2));
  filter: blur(6px);
  z-index: -1;
  animation: logoGlow 4s ease-in-out infinite;
}

@keyframes logoPulse {
  0%, 100% { box-shadow: 0 4px 16px rgba(108, 124, 255, 0.35); }
  50%      { box-shadow: 0 4px 24px rgba(108, 124, 255, 0.55); }
}

@keyframes logoGlow {
  0%, 100% { opacity: 0.5; }
  50%      { opacity: 0.8; }
}

.logo-text {
  font-size: var(--text-md);
  font-weight: var(--weight-semibold);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: var(--space-2) var(--space-2);
  position: relative;
  z-index: 1;
}

.nav-section {
  margin-bottom: var(--space-4);
}

.nav-section-label {
  display: block;
  padding: 0 var(--space-3);
  margin-bottom: var(--space-1);
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
  line-height: 2;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  width: 100%;
  height: var(--nav-item-height);
  padding: 0 var(--space-3);
  border: 0;
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
  background: transparent;
  cursor: pointer;
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
  transition: all var(--motion-fast);
  position: relative;
  text-align: left;
  overflow: hidden;
}

.nav-item::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 2px;
  border-radius: 0 2px 2px 0;
  background: transparent;
  transition: all var(--motion-base);
}

.nav-item::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(90deg, rgba(108, 124, 255, 0.08), transparent);
  opacity: 0;
  transition: opacity var(--motion-fast);
  pointer-events: none;
}

.nav-item:hover {
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.04);
  transform: translateX(2px);
}

.nav-item:hover::after {
  opacity: 1;
}

.nav-item.active {
  color: var(--text-primary);
  background: rgba(108, 124, 255, 0.08);
}

.nav-item.active::before {
  background: var(--accent);
  box-shadow: 0 0 8px rgba(108, 124, 255, 0.4);
}

.nav-item.active::after {
  opacity: 1;
}

.nav-icon {
  flex-shrink: 0;
  opacity: 0.55;
  transition: all var(--motion-fast);
}

.nav-item:hover .nav-icon {
  opacity: 0.85;
}

.nav-item.active .nav-icon {
  opacity: 1;
  color: var(--accent);
  filter: drop-shadow(0 0 4px rgba(108, 124, 255, 0.4));
}

.nav-badge {
  margin-left: auto;
  min-width: 20px;
  height: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  border-radius: var(--radius-pill);
  background: var(--accent-bg);
  color: var(--accent);
  font-size: 10px;
  font-weight: var(--weight-semibold);
  border: 1px solid rgba(108, 124, 255, 0.15);
}

.sidebar-footer {
  flex-shrink: 0;
  padding: var(--space-3) var(--space-3);
  border-top: 1px solid var(--border-hairline);
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  position: relative;
  z-index: 1;
}

.footer-actions {
  display: flex;
  gap: var(--space-1);
}

.footer-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  height: 32px;
  border: 0;
  border-radius: var(--radius-md);
  color: var(--text-muted);
  background: transparent;
  cursor: pointer;
  font-size: var(--text-xs);
  font-weight: var(--weight-medium);
  transition: all var(--motion-fast);
}

.footer-btn:hover {
  color: var(--expense);
  background: var(--expense-bg);
}

.language-switch {
  display: flex;
  gap: 2px;
  padding: 3px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid var(--border-hairline);
}

.language-switch button {
  flex: 1;
  height: 28px;
  border: 0;
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  background: transparent;
  cursor: pointer;
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  transition: all var(--motion-fast);
}

.language-switch button.active {
  color: #fff;
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  box-shadow: 0 4px 12px rgba(108, 124, 255, 0.3);
}

.footer-user {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-1) 0;
}

.user-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--income);
  box-shadow: 0 0 8px rgba(0, 232, 157, 0.5);
  flex-shrink: 0;
  animation: userDotPulse 3s ease-in-out infinite;
}

@keyframes userDotPulse {
  0%, 100% { box-shadow: 0 0 6px rgba(0, 232, 157, 0.4); }
  50%      { box-shadow: 0 0 14px rgba(0, 232, 157, 0.7); }
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-status {
  font-size: 10px;
  color: var(--text-muted);
}

@media (max-width: 768px) {
  .sidebar {
    top: auto;
    bottom: 0;
    left: 0;
    right: 0;
    width: 100%;
    height: 64px;
    flex-direction: row;
    align-items: center;
    border-right: 0;
    border-top: 1px solid var(--border-hairline);
    padding: 0 var(--space-2);
  }

  .sidebar-glow { display: none; }
  .sidebar-header,
  .sidebar-nav,
  .footer-user,
  .footer-actions span,
  .language-switch {
    display: none;
  }

  .sidebar-footer {
    flex-direction: row;
    width: 100%;
    border-top: 0;
    padding: 0;
  }

  .footer-actions {
    width: 100%;
  }
}
</style>
