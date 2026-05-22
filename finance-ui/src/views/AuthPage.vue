<template>
  <main class="auth-page">
    <BackgroundParticles />
    <div class="aurora aurora-one"></div>
    <div class="aurora aurora-two"></div>
    <div class="aurora aurora-three"></div>
    <div class="grid-overlay"></div>

    <section class="auth-shell">
      <section class="brand-panel glass-panel">
        <div class="brand-topline">
          <span class="chip">{{ t.appName }}</span>
          <div class="language-buttons">
            <button :class="{ active: language === 'zh' }" type="button" @click="handleLanguageChange('zh')">
              {{ messages.zh.languageName }}
            </button>
            <button :class="{ active: language === 'en' }" type="button" @click="handleLanguageChange('en')">
              {{ messages.en.languageName }}
            </button>
          </div>
        </div>

        <div class="brand-copy-block">
          <p class="eyebrow">{{ t.brandEyebrow }}</p>
          <h1 class="shimmer-text">{{ t.brandTitle }}</h1>
          <p class="brand-copy">{{ t.brandCopy }}</p>
        </div>

        <div class="floating-ledger">
          <div class="ledger-strip">
            <span>{{ t.income }}</span>
            <strong>+ 12,800</strong>
          </div>
          <div class="ledger-strip">
            <span>{{ t.expense }}</span>
            <strong>- 3,920</strong>
          </div>
          <div class="ledger-strip">
            <span>{{ t.balance }}</span>
            <strong>8,880</strong>
          </div>
        </div>

        <div class="orbit-ring">
          <div class="orbit-dot"></div>
        </div>
      </section>

      <section class="auth-card glass-panel">
        <div class="mode-tabs" role="tablist">
          <button
            :class="{ active: mode === 'login' }"
            type="button"
            @click="switchMode('login')"
          >
            {{ t.login }}
          </button>
          <button
            :class="{ active: mode === 'register' }"
            type="button"
            @click="switchMode('register')"
          >
            {{ t.register }}
          </button>
        </div>

        <div class="avatar-stage" :class="{ sleeping: passwordFocused && !passwordVisible, awake: passwordVisible }">
          <div class="avatar-core">
            <span class="eye left-eye"></span>
            <span class="eye right-eye"></span>
            <span class="mouth"></span>
          </div>
          <div class="avatar-ring"></div>
        </div>

        <div class="form-slider">
          <div class="slide-track" :class="{ 'slide-register': mode === 'register' }">
            <form class="auth-form slide-panel" @submit.prevent="submitAuth">
              <label>
                <span>{{ t.email }}</span>
                <input v-model.trim="form.email" type="email" autocomplete="email" :placeholder="t.emailPlaceholder" />
              </label>

              <label>
                <span>{{ t.password }}</span>
                <div class="password-field">
                  <input
                    v-model="form.password"
                    :type="passwordVisible ? 'text' : 'password'"
                    autocomplete="current-password"
                    :placeholder="t.passwordPlaceholder"
                    @focus="passwordFocused = true"
                    @blur="passwordFocused = false"
                  />
                  <button
                    class="eye-button"
                    type="button"
                    :aria-label="passwordVisible ? t.hidePassword : t.showPassword"
                    @click="passwordVisible = !passwordVisible"
                  >
                    {{ passwordVisible ? t.hide : t.open }}
                  </button>
                </div>
              </label>

              <p v-if="message" :class="['auth-message', messageType]">{{ message }}</p>

              <button class="submit-button" :disabled="submitting" type="submit">
                <span v-if="!submitting">{{ t.login }}</span>
                <span v-else class="btn-loading">
                  <span class="dot-pulse"></span>
                  {{ t.processing }}
                </span>
              </button>
            </form>

            <form class="auth-form slide-panel" @submit.prevent="submitAuth">
              <label>
                <span>{{ t.username }}</span>
                <input v-model.trim="form.username" type="text" autocomplete="username" :placeholder="t.usernamePlaceholder" />
              </label>

              <label>
                <span>{{ t.email }}</span>
                <input v-model.trim="form.email" type="email" autocomplete="email" :placeholder="t.emailPlaceholder" />
              </label>

              <label>
                <span>{{ t.password }}</span>
                <div class="password-field">
                  <input
                    v-model="form.password"
                    :type="passwordVisible ? 'text' : 'password'"
                    autocomplete="new-password"
                    :placeholder="t.passwordPlaceholder"
                    @focus="passwordFocused = true"
                    @blur="passwordFocused = false"
                  />
                  <button
                    class="eye-button"
                    type="button"
                    :aria-label="passwordVisible ? t.hidePassword : t.showPassword"
                    @click="passwordVisible = !passwordVisible"
                  >
                    {{ passwordVisible ? t.hide : t.open }}
                  </button>
                </div>
              </label>

              <div class="strength-box">
                <div class="strength-track">
                  <span :style="{ width: `${passwordScore * 25}%` }" :class="strengthClass"></span>
                </div>
                <div class="strength-meta">
                  <span>{{ strengthLabel }}</span>
                  <span>{{ form.password.length }}/6 {{ t.min }}</span>
                </div>
                <ul class="rule-list">
                  <li :class="{ passed: form.password.length >= 6 }">{{ t.ruleLength }}</li>
                  <li :class="{ passed: hasNumber }">{{ t.ruleNumber }}</li>
                  <li :class="{ passed: hasLetter }">{{ t.ruleLetter }}</li>
                </ul>
              </div>

              <p v-if="message" :class="['auth-message', messageType]">{{ message }}</p>

              <button class="submit-button" :disabled="submitting" type="submit">
                <span v-if="!submitting">{{ t.createAccount }}</span>
                <span v-else class="btn-loading">
                  <span class="dot-pulse"></span>
                  {{ t.processing }}
                </span>
              </button>
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
import BackgroundParticles from '../components/BackgroundParticles.vue'
import { messages } from '../i18n/messages'
import { useI18n } from '../i18n/locale'

const emit = defineEmits(['authenticated'])

const { language, t, setLanguage } = useI18n()
const mode = ref('login')
const submitting = ref(false)
const passwordVisible = ref(false)
const passwordFocused = ref(false)
const message = ref('')
const messageType = ref('success')

const form = reactive({
  username: '',
  email: '',
  password: ''
})

const hasNumber = computed(() => /\d/.test(form.password))
const hasLetter = computed(() => /[A-Za-z]/.test(form.password))
const hasMixedCase = computed(() => /[a-z]/.test(form.password) && /[A-Z]/.test(form.password))
const hasSymbol = computed(() => /[^A-Za-z0-9]/.test(form.password))

const passwordScore = computed(() => {
  let score = 0
  if (form.password.length >= 6) score += 1
  if (hasNumber.value) score += 1
  if (hasLetter.value) score += 1
  if (hasMixedCase.value || hasSymbol.value) score += 1
  return score
})

const strengthLabel = computed(() => {
  if (!form.password) return t.value.passwordStrength
  if (passwordScore.value <= 1) return t.value.weakPassword
  if (passwordScore.value <= 3) return t.value.mediumPassword
  return t.value.strongPassword
})

const strengthClass = computed(() => {
  if (passwordScore.value <= 1) return 'weak'
  if (passwordScore.value <= 3) return 'medium'
  return 'strong'
})

const handleLanguageChange = (nextLanguage) => {
  setLanguage(nextLanguage)
  message.value = ''
}

const switchMode = (nextMode) => {
  mode.value = nextMode
  message.value = ''
  passwordVisible.value = false
}

const showMessage = (text, type = 'error') => {
  message.value = text
  messageType.value = type
}

const validateForm = () => {
  if (mode.value === 'register' && !form.username) return t.value.validationUsername
  if (!form.email) return t.value.validationEmail
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return t.value.validationEmailFormat
  if (!form.password) return t.value.validationPassword
  if (mode.value === 'register' && form.password.length < 6) return t.value.validationPasswordLength
  if (mode.value === 'register' && !hasNumber.value) return t.value.validationPasswordNumber
  return ''
}

const unwrapResponse = (response) => {
  const result = response.data
  if (!result.success) {
    throw new Error(result.message || t.value.requestFailed)
  }
  return result.data
}

const submitAuth = async () => {
  const validationMessage = validateForm()
  if (validationMessage) {
    showMessage(validationMessage)
    return
  }

  submitting.value = true
  message.value = ''

  try {
    const payload = {
      email: form.email,
      password: form.password
    }

    if (mode.value === 'register') {
      payload.username = form.username
    }

    const response = mode.value === 'login' ? await loginUser(payload) : await registerUser(payload)
    const user = unwrapResponse(response)
    localStorage.setItem('pf_user', JSON.stringify(user))
    emit('authenticated', { user, language: language.value })
  } catch (error) {
    showMessage(error.message || t.value.authFailed)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.auth-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: center;
  overflow: hidden;
  padding: 28px;
  color: var(--color-text-primary);
  background:
    radial-gradient(ellipse at 15% 20%, rgba(108, 124, 255, 0.08), transparent 50%),
    radial-gradient(ellipse at 85% 18%, rgba(0, 232, 157, 0.05), transparent 45%),
    radial-gradient(ellipse at 50% 85%, rgba(255, 77, 106, 0.04), transparent 40%),
    var(--color-bg);
}

.aurora {
  position: absolute;
  border-radius: 999px;
  filter: blur(100px);
  pointer-events: none;
}

.aurora-one {
  width: 500px;
  height: 500px;
  top: -180px;
  left: -120px;
  background: rgba(108, 124, 255, 0.15);
  animation: auroraFloat 14s ease-in-out infinite;
}

.aurora-two {
  width: 450px;
  height: 450px;
  right: -120px;
  bottom: -150px;
  background: rgba(0, 232, 157, 0.10);
  animation: auroraFloat 14s ease-in-out infinite 5s;
}

.aurora-three {
  width: 350px;
  height: 350px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(255, 77, 106, 0.06);
  animation: auroraFloat 14s ease-in-out infinite 9s;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(108, 124, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(108, 124, 255, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
  mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
  -webkit-mask-image: radial-gradient(ellipse at center, black 30%, transparent 70%);
  pointer-events: none;
}

@keyframes auroraFloat {
  0%, 100% { transform: translate(0, 0) scale(1); opacity: 0.6; }
  33% { transform: translate(30px, -20px) scale(1.1); opacity: 0.8; }
  66% { transform: translate(-20px, 15px) scale(0.95); opacity: 0.5; }
}

.auth-shell {
  position: relative;
  z-index: 1;
  width: min(1180px, 100%);
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(360px, 440px);
  gap: 20px;
  animation: shellFadeIn 0.8s cubic-bezier(0, 0, 0.2, 1) both;
}

@keyframes shellFadeIn {
  from { opacity: 0; transform: translateY(20px) scale(0.98); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}

.brand-panel,
.auth-card {
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-card);
}

.brand-panel {
  padding: 38px 38px 34px;
}

.brand-panel::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -30%;
  width: 80%;
  height: 80%;
  background: radial-gradient(circle, rgba(108, 124, 255, 0.06), transparent 60%);
  pointer-events: none;
  animation: brandGlow 8s ease-in-out infinite;
}

@keyframes brandGlow {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.brand-topline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.chip {
  display: inline-flex;
  align-items: center;
  min-height: 34px;
  border-radius: var(--radius-pill);
  padding: 0 14px;
  color: var(--accent);
  background: var(--accent-bg);
  border: 1px solid rgba(108, 124, 255, 0.2);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  animation: chipGlow 3s ease-in-out infinite;
}

@keyframes chipGlow {
  0%, 100% { box-shadow: 0 0 8px rgba(108, 124, 255, 0.1); }
  50% { box-shadow: 0 0 20px rgba(108, 124, 255, 0.25); }
}

.language-buttons {
  display: inline-flex;
  gap: 4px;
  padding: 4px;
  border-radius: var(--radius-pill);
  background: rgba(108, 124, 255, 0.04);
  border: 1px solid var(--color-border);
}

.language-buttons button {
  min-width: 56px;
  min-height: 32px;
  border: 0;
  border-radius: var(--radius-pill);
  color: var(--color-text-muted);
  background: transparent;
  cursor: pointer;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.3s ease;
}

.language-buttons button.active {
  color: #fff;
  background: var(--accent);
  box-shadow: 0 6px 20px rgba(108, 124, 255, 0.35);
}

.brand-copy-block {
  position: relative;
  margin-top: 72px;
}

.eyebrow {
  margin: 0 0 14px;
  color: var(--accent);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.brand-panel h1 {
  margin: 0;
  max-width: 560px;
  font-size: clamp(38px, 4vw, 58px);
  font-weight: 700;
  line-height: 1.04;
  letter-spacing: -0.02em;
}

.brand-copy {
  max-width: 560px;
  margin: 20px 0 0;
  color: var(--color-text-secondary);
  font-size: 15px;
  line-height: 1.7;
}

.floating-ledger {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 44px;
}

.ledger-strip {
  padding: 18px;
  border-radius: 18px;
  background: rgba(108, 124, 255, 0.03);
  border: 1px solid var(--color-border);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
  transition: all 0.3s ease;
}

.ledger-strip:hover {
  border-color: rgba(108, 124, 255, 0.2);
  box-shadow: 0 0 24px rgba(108, 124, 255, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.04);
  transform: translateY(-2px);
}

.ledger-strip span {
  display: block;
  color: var(--color-text-muted);
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.ledger-strip strong {
  display: block;
  margin-top: 8px;
  font-size: 26px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.01em;
}

.ledger-strip:nth-child(1) strong { color: var(--income); }
.ledger-strip:nth-child(2) strong { color: var(--expense); }
.ledger-strip:nth-child(3) strong { color: var(--accent); }

.orbit-ring {
  position: absolute;
  bottom: 40px;
  right: 40px;
  width: 120px;
  height: 120px;
  border: 1px solid rgba(108, 124, 255, 0.1);
  border-radius: 50%;
  animation: orbitSpin 20s linear infinite;
}

.orbit-dot {
  position: absolute;
  top: -4px;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 12px rgba(108, 124, 255, 0.6);
}

@keyframes orbitSpin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.auth-card {
  padding: 28px 30px 32px;
  animation: cardSlideIn 0.6s cubic-bezier(0, 0, 0.2, 1) 0.15s both;
}

@keyframes cardSlideIn {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

.mode-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  padding: 5px;
  border-radius: var(--radius-pill);
  background: rgba(108, 124, 255, 0.04);
  border: 1px solid var(--color-border);
}

.mode-tabs button {
  min-height: 44px;
  border: 0;
  border-radius: var(--radius-pill);
  color: var(--color-text-muted);
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.mode-tabs button.active {
  color: #fff;
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  box-shadow: 0 8px 28px rgba(108, 124, 255, 0.4);
}

.avatar-stage {
  position: relative;
  display: grid;
  place-items: center;
  height: 140px;
  margin: 8px 0 2px;
}

.avatar-ring {
  position: absolute;
  width: 96px;
  height: 96px;
  border-radius: 999px;
  border: 1px solid rgba(108, 124, 255, 0.15);
  box-shadow:
    0 0 30px rgba(108, 124, 255, 0.15),
    inset 0 0 20px rgba(108, 124, 255, 0.08);
  animation: spin 14s linear infinite;
}

.avatar-core {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 24px;
  background: linear-gradient(160deg, #fde68a, #f59e0b);
  box-shadow:
    0 14px 28px rgba(245, 158, 11, 0.18),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.avatar-stage.sleeping .avatar-core {
  transform: scale(0.95);
}

.avatar-stage.awake .avatar-core {
  transform: scale(1.05);
}

.eye {
  position: absolute;
  top: 26px;
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: #422006;
  transition: all 0.25s ease;
}

.left-eye { left: 19px; }
.right-eye { right: 19px; }

.mouth {
  position: absolute;
  left: 25px;
  bottom: 16px;
  width: 22px;
  height: 8px;
  border-bottom: 2px solid #422006;
  border-radius: 0 0 999px 999px;
  transition: all 0.25s ease;
}

.sleeping .eye { height: 2px; transform: translateY(4px); }
.awake .eye { background: #0f172a; box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.2); }
.awake .mouth { border-bottom-width: 3px; width: 26px; left: 23px; }

.form-slider { overflow: hidden; margin-top: 2px; }

.slide-track {
  display: flex;
  width: 200%;
  transition: transform 0.45s cubic-bezier(0.4, 0, 0.2, 1);
  transform: translateX(0);
}

.slide-track.slide-register { transform: translateX(-50%); }

.slide-panel { width: 50%; flex-shrink: 0; padding: 0 2px; }

.auth-form { display: grid; gap: 14px; }

label {
  display: grid;
  gap: 6px;
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
}

input {
  width: 100%;
  min-height: 46px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-input);
  padding: 10px 14px;
  color: var(--color-text-primary);
  background: rgba(108, 124, 255, 0.03);
  font-size: 14px;
  transition: border-color 0.25s ease, box-shadow 0.25s ease, background 0.25s ease;
}

input::placeholder { color: var(--color-text-muted); }

input:focus {
  border-color: var(--accent);
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-bg), 0 0 20px rgba(108, 124, 255, 0.1);
  background: rgba(108, 124, 255, 0.05);
}

.password-field {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 56px;
  gap: 8px;
}

.eye-button,
.submit-button {
  border: 0;
  cursor: pointer;
  font-weight: 600;
}

.eye-button {
  min-height: 46px;
  border-radius: var(--radius-input);
  color: var(--color-text-muted);
  background: rgba(108, 124, 255, 0.04);
  font-size: 12px;
  letter-spacing: 0.04em;
  transition: all 0.2s ease;
}

.eye-button:hover {
  background: rgba(108, 124, 255, 0.10);
  color: var(--accent);
}

.strength-box {
  display: grid;
  gap: 8px;
  padding: 14px;
  border-radius: var(--radius-input);
  background: rgba(108, 124, 255, 0.02);
  border: 1px solid var(--color-border);
}

.strength-track {
  height: 6px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(108, 124, 255, 0.08);
}

.strength-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  transition: width 0.35s ease, background 0.35s ease;
}

.weak { background: var(--expense); box-shadow: 0 0 8px rgba(255, 77, 106, 0.3); }
.medium { background: var(--warning); box-shadow: 0 0 8px rgba(255, 179, 71, 0.3); }
.strong { background: var(--income); box-shadow: 0 0 8px rgba(0, 232, 157, 0.3); }

.strength-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.rule-list {
  display: grid;
  gap: 4px;
  margin: 0;
  padding-left: 16px;
  color: var(--color-text-muted);
  font-size: 12px;
}

.rule-list .passed { color: var(--income); }

.auth-message {
  margin: 0;
  border-radius: var(--radius-input);
  padding: 12px 14px;
  font-size: 13px;
  font-weight: 500;
  animation: fadeIn 0.3s ease;
}

.auth-message.error {
  color: var(--expense);
  background: var(--expense-bg);
  border: 1px solid rgba(255, 77, 106, 0.15);
}

.auth-message.success {
  color: var(--income);
  background: var(--income-bg);
  border: 1px solid rgba(0, 232, 157, 0.15);
}

.submit-button {
  min-height: 50px;
  border-radius: var(--radius-input);
  color: #fff;
  background: linear-gradient(135deg, var(--accent), #8b5cf6);
  box-shadow: 0 8px 28px rgba(108, 124, 255, 0.35);
  font-size: 15px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.submit-button::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.15), transparent 60%);
  pointer-events: none;
}

.submit-button:hover {
  box-shadow: 0 12px 36px rgba(108, 124, 255, 0.5);
  transform: translateY(-2px);
}

.submit-button:active { transform: translateY(0); }

.submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
  transform: none;
}

.btn-loading {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.dot-pulse {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: currentColor;
  animation: dotPulse 0.8s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.06); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: minmax(0, 1fr);
    max-width: 480px;
  }

  .brand-panel { display: none; }

  .orbit-ring { display: none; }
}

@media (max-width: 480px) {
  .auth-page { padding: 16px; }

  .auth-card { padding: 20px 18px 24px; }
}
</style>
