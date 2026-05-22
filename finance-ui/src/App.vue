<template>
  <AuthPage v-if="!currentUser" @authenticated="handleAuthenticated" />
  <div v-else class="app-layout">
    <BackgroundParticles />
    <Sidebar
      :current-user="currentUser"
      :bills-count="billsCount"
      @logout="logout"
    />
    <main class="main-content">
      <FinanceDashboard
        :current-user="currentUser"
        @logout="logout"
        @bills-change="onBillsChange"
      />
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AuthPage from './views/AuthPage.vue'
import FinanceDashboard from './views/FinanceDashboard.vue'
import Sidebar from './components/Sidebar.vue'
import BackgroundParticles from './components/BackgroundParticles.vue'
import { setLanguage } from './i18n/locale'

const storedUser = localStorage.getItem('pf_user')
const devUser = { id: 1, username: 'demo', email: 'demo@test.com' }
const currentUser = ref(storedUser ? JSON.parse(storedUser) : (import.meta.env.DEV ? devUser : null))
const billsCount = ref(0)

const handleAuthenticated = (payload) => {
  currentUser.value = payload.user
  setLanguage(payload.language)
}

const logout = () => {
  localStorage.removeItem('pf_user')
  currentUser.value = null
  billsCount.value = 0
}

const onBillsChange = (count) => {
  billsCount.value = count
}
</script>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--surface-0);
  position: relative;
}

.main-content {
  flex: 1;
  margin-left: var(--sidebar-width);
  min-width: 0;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  z-index: 1;
}

@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
    padding-bottom: 64px;
  }
}
</style>
