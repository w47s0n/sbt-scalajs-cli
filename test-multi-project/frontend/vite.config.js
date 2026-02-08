import { defineConfig } from 'vite'

export default defineConfig(({ mode }) => ({
  server: {
    port: 3000
  },
  build: {
    outDir: 'dist'
  },
  define: {
    '__SCALAJS_BUILD__': JSON.stringify(mode === 'production' ? 'opt' : 'fastopt')
  }
}))
