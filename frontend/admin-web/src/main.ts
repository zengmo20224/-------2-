import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// Global styles + design tokens (--pc-* variables). Must come AFTER element-plus
// css so our tokens can override defaults. Without this import the whole backoffice
// UI collapses (no sidebar color, no spacing/shadow variables).
import './style.css'
import router from './router'
import App from './App.vue'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')
