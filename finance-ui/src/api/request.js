import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

let isRedirecting = false

request.interceptors.request.use((config) => {
  try {
    const stored = localStorage.getItem('pf_user')
    if (stored) {
      const user = JSON.parse(stored)
      if (user?.token) {
        config.headers.Authorization = `Bearer ${user.token}`
      }
    }
  } catch (e) {
    // ignore parse errors
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isRedirecting) {
      isRedirecting = true
      localStorage.removeItem('pf_user')
      window.location.href = window.location.origin + window.location.pathname
    }
    return Promise.reject(error)
  }
)

export default request
