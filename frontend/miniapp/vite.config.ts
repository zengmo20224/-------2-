import { defineConfig } from 'vite'
import UniPlugin from '@dcloudio/vite-plugin-uni'

// Handle ESM/CJS interop for the uni plugin
const uni = (UniPlugin as any).default || UniPlugin

export default defineConfig({
  plugins: [uni()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // Uploaded images are served by the backend at /uploads/**.
      // Without this proxy, the dev server returns SPA index.html (HTML)
      // for these paths, so <image> tags render blank.
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
