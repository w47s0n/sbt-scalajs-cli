import { defineConfig } from 'vite'

export default defineConfig(({ mode }) => ({
  server: {
    port: 3000,
  },
  resolve: {
    alias: {
      '@scalajs': '/target/scala-2.13'
    }
  },
  define: {
    '__SCALAJS_BUILD__': JSON.stringify(mode === 'production' ? 'opt' : 'fastopt')
  }
}))
