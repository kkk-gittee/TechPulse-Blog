import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './styles/global.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

// Apply theme before mount to avoid flash
import { useThemeStore } from './stores/theme'
useThemeStore()

app.mount('#app')
