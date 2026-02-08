import { defineConfig } from 'vite'

export default defineConfig({
  server: {
    port: 3000,
  },
  resolve: {
    alias: {
      '@scalajs': '/target/scala-2.13'
    }
  }
})
