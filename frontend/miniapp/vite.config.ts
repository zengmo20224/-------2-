import { defineConfig } from 'vite'
import UniPlugin from '@dcloudio/vite-plugin-uni'

// Handle ESM/CJS interop for the uni plugin
const uni = (UniPlugin as any).default || UniPlugin

export default defineConfig({
  plugins: [uni()],
})
