<template>
  <main class="auth-page">
    <section class="auth-shell">
      <section class="brand-panel">
        <span class="brand-chip">{{ t.authBrandEyebrow }}</span>
        <div class="brand-copy">
          <h1>{{ t.authBrandTitle }}</h1>
          <p>{{ t.authBrandCopy }}</p>
        </div>
        <div class="ledger-row">
          <div class="ledger-item"><span>{{ t.income }}</span><strong class="c-success">+ ¥12,800</strong></div>
          <div class="ledger-item"><span>{{ t.expenses }}</span><strong class="c-danger">− ¥3,920</strong></div>
          <div class="ledger-item"><span>{{ t.balance }}</span><strong>¥8,880</strong></div>
        </div>
      </section>

      <section class="auth-card card">
        <div class="lang-row">
          <button :class="{active: language === 'zh'}" @click="handleLang('zh')">中文</button>
          <button :class="{active: language === 'en'}" @click="handleLang('en')">English</button>
        </div>
        <div class="mode-tabs">
          <button :class="{active: mode === 'login'}" @click="switchMode('login')">{{ t.authSignIn }}</button>
          <button :class="{active: mode === 'register'}" @click="switchMode('register')">{{ t.authCreateAccount }}</button>
        </div>

        <div class="avatar-stage" :class="{sleeping: pwFocused && !pwVisible, awake: pwVisible}">
          <div class="avatar-core"><span class="eye left"></span><span class="eye right"></span><span class="mouth"></span></div>
        </div>

        <div class="form-slider">
          <div class="slide-track" :class="{'shift': mode === 'register'}">
            <form class="auth-form" @submit.prevent="submitAuth">
              <label><span>{{ t.authEmail }}</span><input v-model.trim="form.email" type="email" autocomplete="email" :placeholder="t.authEmailPlaceholder"/></label>
              <label><span>{{ t.authPassword }}</span>
                <div class="pw-field">
                  <input v-model="form.password" :type="pwVisible?'text':'password'" autocomplete="current-password" :placeholder="t.authPasswordPlaceholder" @focus="pwFocused=true" @blur="pwFocused=false"/>
                  <button type="button" class="eye-btn" @click="pwVisible=!pwVisible">{{ pwVisible ? t.authHide : t.authShow }}</button>
                </div>
              </label>
              <p v-if="message" :class="['auth-msg', messageType]">{{ message }}</p>
              <button class="submit-btn" :disabled="submitting" type="submit">{{ submitting ? t.authSigningIn : t.authSignIn }}</button>
            </form>
            <form class="auth-form" @submit.prevent="submitAuth">
              <label><span>{{ t.authUsername }}</span><input v-model.trim="form.username" type="text" autocomplete="username" :placeholder="t.authUsernamePlaceholder"/></label>
              <label><span>{{ t.authEmail }}</span><input v-model.trim="form.email" type="email" autocomplete="email" :placeholder="t.authEmailPlaceholder"/></label>
              <label><span>{{ t.authPassword }}</span>
                <div class="pw-field">
                  <input v-model="form.password" :type="pwVisible?'text':'password'" autocomplete="new-password" :placeholder="t.authPasswordPlaceholder" @focus="pwFocused=true" @blur="pwFocused=false"/>
                  <button type="button" class="eye-btn" @click="pwVisible=!pwVisible">{{ pwVisible ? t.authHide : t.authShow }}</button>
                </div>
              </label>
              <div class="strength-box">
                <div class="strength-track"><span :style="{width: pwScore*25+'%'}" :class="strengthClass"></span></div>
                <div class="strength-meta"><span>{{ strengthLabel }}</span><span>{{ form.password.length }}/6 {{ t.authMin }}</span></div>
                <ul class="rule-list"><li :class="{ok: form.password.length>=6}">{{ t.authRuleLength }}</li><li :class="{ok: hasNum}">{{ t.authRuleNumber }}</li><li :class="{ok: hasLet}">{{ t.authRuleLetter }}</li></ul>
              </div>
              <p v-if="message" :class="['auth-msg', messageType]">{{ message }}</p>
              <button class="submit-btn" :disabled="submitting" type="submit">{{ submitting ? t.authCreating : t.authCreateAccount }}</button>
            </form>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { loginUser, registerUser } from '../api/auth'
import { useI18n } from '../i18n/locale'

const emit = defineEmits(['authenticated'])
const { language, t, setLanguage } = useI18n()
const mode = ref('login'), submitting = ref(false), pwVisible = ref(false), pwFocused = ref(false), message = ref(''), messageType = ref('success')
const form = reactive({ username: '', email: '', password: '' })

const hasNum = computed(() => /\d/.test(form.password))
const hasLet = computed(() => /[A-Za-z]/.test(form.password))
const hasMixed = computed(() => /[a-z]/.test(form.password) && /[A-Z]/.test(form.password))
const hasSym = computed(() => /[^A-Za-z0-9]/.test(form.password))
const pwScore = computed(() => { let s = 0; if (form.password.length >= 6) s++; if (hasNum.value) s++; if (hasLet.value) s++; if (hasMixed.value || hasSym.value) s++; return s })
const strengthLabel = computed(() => !form.password ? t.value.authPasswordStrength : pwScore.value <= 1 ? t.value.authWeak : pwScore.value <= 3 ? t.value.authMedium : t.value.authStrong)
const strengthClass = computed(() => pwScore.value <= 1 ? 'wk' : pwScore.value <= 3 ? 'md' : 'st')

const handleLang = (l) => { setLanguage(l); message.value = '' }
const switchMode = (m) => { mode.value = m; message.value = ''; pwVisible.value = false }
const showMsg = (text, ty = 'error') => { message.value = text; messageType.value = ty }

const validate = () => {
  if (mode.value === 'register' && !form.username) return t.value.authValidationUsername
  if (!form.email) return t.value.authValidationEmail
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return t.value.authValidationEmailFormat
  if (!form.password) return t.value.authValidationPassword
  if (mode.value === 'register' && form.password.length < 6) return t.value.authValidationPasswordLength
  if (mode.value === 'register' && !hasNum.value) return t.value.authValidationPasswordNumber
  return ''
}

const unwrap = (r) => { const d = r.data; if (!d.success) throw new Error(d.message || t.value.requestFailed); return d.data }

const submitAuth = async () => {
  const msg = validate(); if (msg) { showMsg(msg); return }
  submitting.value = true; message.value = ''
  try {
    const payload = { email: form.email, password: form.password }
    if (mode.value === 'register') payload.username = form.username
    const data = unwrap(mode.value === 'login' ? await loginUser(payload) : await registerUser(payload))
    localStorage.setItem('pf_user', JSON.stringify(data))
    emit('authenticated', { user: data, language: language.value })
  } catch (e) { showMsg(e.message || t.value.authFailed) }
  finally { submitting.value = false }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: var(--s-8); background: var(--bg-app); }
.auth-shell { display: grid; grid-template-columns: 1fr 420px; gap: var(--s-8); max-width: 1080px; width: 100%; align-items: center; animation: fadeIn 0.6s var(--ease-out); }

.brand-panel { display: flex; flex-direction: column; gap: var(--s-8); }
.brand-chip { display: inline-flex; align-self: flex-start; height: 28px; padding: 0 var(--s-3); border-radius: var(--r-pill); background: var(--accent-soft); color: var(--accent); font-size: var(--fs-xs); font-weight: var(--fw-semibold); align-items: center; letter-spacing: var(--ls-caps); }
.brand-copy h1 { font-size: var(--fs-4xl); font-weight: var(--fw-bold); line-height: 1.1; letter-spacing: var(--ls-tight); color: var(--text-primary); margin-bottom: var(--s-4); }
.brand-copy p { font-size: var(--fs-md); color: var(--text-secondary); line-height: var(--lh-relaxed); max-width: 480px; }

.ledger-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--s-3); }
.ledger-item { padding: var(--s-4); border-radius: var(--r-lg); background: var(--bg-raised); border: 1px solid var(--border-light); }
.ledger-item span { display: block; font-size: var(--fs-xs); color: var(--text-tertiary); text-transform: uppercase; letter-spacing: var(--ls-caps); margin-bottom: var(--s-1); }
.ledger-item strong { font-size: var(--fs-xl); font-weight: var(--fw-bold); }
.c-success { color: var(--success); }
.c-danger { color: var(--danger); }

.auth-card { padding: var(--s-8); }
.lang-row { display: flex; gap: var(--s-1); margin-bottom: var(--s-4); justify-content: flex-end; }
.lang-row button { height: 28px; padding: 0 var(--s-3); border-radius: var(--r-sm); font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-tertiary); transition: all var(--dur-fast); }
.lang-row button:hover { color: var(--text-primary); }
.lang-row button.active { color: var(--accent); background: var(--accent-soft); font-weight: var(--fw-semibold); }

.mode-tabs { display: grid; grid-template-columns: 1fr 1fr; gap: var(--s-1); padding: 4px; border-radius: var(--r-md); background: var(--bg-inset); margin-bottom: var(--s-6); }
.mode-tabs button { height: 40px; border-radius: var(--r-sm); font-size: var(--fs-sm); font-weight: var(--fw-medium); color: var(--text-tertiary); transition: all var(--dur-fast); }
.mode-tabs button.active { color: var(--text-primary); background: var(--bg-raised); box-shadow: var(--shadow-xs); font-weight: var(--fw-semibold); }

.avatar-stage { display: grid; place-items: center; height: 100px; margin-bottom: var(--s-4); }
.avatar-core { position: relative; width: 56px; height: 56px; border-radius: 18px; background: linear-gradient(160deg, #fde68a, #f59e0b); box-shadow: 0 8px 20px rgba(245,158,11,0.12), inset 0 1px 0 rgba(255,255,255,0.4); transition: transform 0.3s var(--ease-out); }
.sleeping .avatar-core { transform: scale(0.94); }
.awake .avatar-core { transform: scale(1.06); }
.eye { position: absolute; top: 20px; width: 8px; height: 8px; border-radius: 50%; background: #422006; transition: all 0.2s; }
.left { left: 14px; } .right { right: 14px; }
.mouth { position: absolute; left: 19px; bottom: 12px; width: 18px; height: 6px; border-bottom: 2px solid #422006; border-radius: 0 0 999px 999px; transition: all 0.2s; }
.sleeping .eye { height: 2px; transform: translateY(3px); }
.awake .eye { background: #0f172a; box-shadow: 0 0 0 3px rgba(255,255,255,0.2); }
.awake .mouth { border-bottom-width: 3px; width: 20px; left: 18px; }

.form-slider { overflow: hidden; }
.slide-track { display: flex; width: 200%; transition: transform 0.4s var(--ease-out); }
.slide-track.shift { transform: translateX(-50%); }
.auth-form { width: 50%; flex-shrink: 0; display: flex; flex-direction: column; gap: var(--s-4); padding: 0 1px; }

label { display: flex; flex-direction: column; gap: var(--s-1); }
label span { font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-secondary); }
input { width: 100%; height: 42px; border: 1px solid var(--border-default); border-radius: var(--r-md); padding: 0 var(--s-3); font-size: var(--fs-sm); color: var(--text-primary); background: var(--bg-inset); transition: all var(--dur-fast); }
input::placeholder { color: var(--text-tertiary); }
input:focus { border-color: var(--accent); box-shadow: var(--shadow-input); outline: none; }

.pw-field { display: grid; grid-template-columns: 1fr 56px; gap: var(--s-2); }
.eye-btn { height: 42px; border-radius: var(--r-md); font-size: var(--fs-xs); font-weight: var(--fw-medium); color: var(--text-tertiary); background: var(--bg-inset); transition: all var(--dur-fast); }
.eye-btn:hover { color: var(--accent); background: var(--accent-soft); }

.strength-box { padding: var(--s-3); border-radius: var(--r-md); background: var(--bg-inset); display: flex; flex-direction: column; gap: var(--s-2); }
.strength-track { height: 4px; border-radius: 2px; background: var(--border-light); overflow: hidden; }
.strength-track span { display: block; height: 100%; border-radius: 2px; transition: width 0.3s; }
.wk { background: var(--danger); } .md { background: var(--warning); } .st { background: var(--success); }
.strength-meta { display: flex; justify-content: space-between; font-size: var(--fs-xs); color: var(--text-tertiary); }
.rule-list { font-size: var(--fs-xs); color: var(--text-tertiary); list-style: none; display: flex; flex-direction: column; gap: 2px; }
.rule-list .ok { color: var(--success); }

.auth-msg { padding: var(--s-2) var(--s-3); border-radius: var(--r-md); font-size: var(--fs-sm); font-weight: var(--fw-medium); }
.auth-msg.error { color: var(--danger); background: var(--danger-soft); }
.auth-msg.success { color: var(--success); background: var(--success-soft); }

.submit-btn { width: 100%; height: 44px; border-radius: var(--r-md); background: var(--accent); color: #fff; font-size: var(--fs-sm); font-weight: var(--fw-semibold); transition: all var(--dur-fast); }
.submit-btn:hover { background: var(--accent-hover); }
.submit-btn:active { background: var(--accent-pressed); }
.submit-btn:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 860px) { .auth-shell { grid-template-columns: 1fr; max-width: 440px; } .brand-panel { display: none; } }
@media (max-width: 480px) { .auth-page { padding: var(--s-4); } .auth-card { padding: var(--s-5); } }
</style>
