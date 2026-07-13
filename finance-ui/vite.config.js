import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import os from 'node:os'
import path from 'node:path'

// Windows 权限问题：将 Vite 缓存放到系统临时目录
const cacheDir = path.join(os.tmpdir(), 'vite-finance-cache')

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  cacheDir,
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
