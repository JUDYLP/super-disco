import { computed, ref } from 'vue'
import { messages } from './messages'

const language = ref(localStorage.getItem('pf_language') || 'zh')

const setLanguage = (nextLanguage) => {
  language.value = nextLanguage === 'en' ? 'en' : 'zh'
  localStorage.setItem('pf_language', language.value)
}

const useI18n = () => {
  const t = computed(() => messages[language.value] || messages.zh)
  return {
    language,
    t,
    setLanguage
  }
}

export {
  language,
  setLanguage,
  useI18n
}
