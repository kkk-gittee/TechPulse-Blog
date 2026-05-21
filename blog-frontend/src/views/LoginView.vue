<template>
  <div class="min-h-screen relative overflow-hidden flex items-center justify-center p-6">
    <!-- Mesh Background Decorations -->
    <div class="absolute top-[-20%] left-[-20%] w-[80vw] h-[80vw] rounded-full bg-primary/5 blur-[120px] pointer-events-none" />
    <div class="absolute bottom-[-20%] right-[-20%] w-[90vw] h-[90vw] rounded-full bg-primary-light/10 blur-[150px] pointer-events-none" />

    <main class="relative z-10 w-full max-w-[440px]">
      <!-- Glassmorphism Card -->
      <div class="bg-surface-card/60 backdrop-blur-2xl border border-white/60 dark:border-outline/15 rounded-[32px] shadow-[0_32px_64px_-16px_rgba(0,0,0,0.1)] p-6 sm:p-10 md:p-12">
        <!-- Brand + Header -->
        <div class="text-center mb-10">
          <!-- Brand Chip -->
          <div class="inline-flex items-center gap-2.5 px-4 py-2 rounded-full bg-primary-light/40 border border-primary/10 mb-6">
            <div class="w-5 h-5 rounded-md bg-primary flex items-center justify-center">
              <span class="font-display font-black text-[10px] text-white leading-none">T</span>
            </div>
            <span class="font-display font-bold text-xs text-primary tracking-wide">技术脉动 · TechPulse</span>
          </div>

          <h1 class="font-display font-bold text-3xl text-on-surface tracking-tight">欢迎回来</h1>
          <p class="font-sans text-on-surface-variant mt-2 text-sm leading-relaxed max-w-xs mx-auto">
            登录你的技术脉动账号，继续探索前沿架构与 AI 技术。
          </p>
        </div>

        <form class="space-y-6" @submit.prevent="handleLogin">
          <!-- Server Error Banner -->
          <div v-if="serverError" class="flex items-center gap-2 px-4 py-3 bg-red-50 border border-red-100 dark:bg-red-900/20 dark:border-red-900/30 dark:text-red-400 rounded-2xl text-sm text-red-600">
            <AlertCircle :size="16" class="flex-shrink-0" />
            <span>{{ serverError }}</span>
          </div>

          <!-- Username Field -->
          <div class="space-y-2">
            <label class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-widest pl-1" for="username">
              用户名
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                <User :size="18" />
              </div>
              <input
                id="username"
                v-model="form.username"
                class="w-full bg-surface-subtle/50 border rounded-2xl py-4 pl-12 pr-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all"
                :class="errors.username ? 'border-red-300' : 'border-outline/10'"
                placeholder="请输入用户名"
                type="text"
                @input="errors.username = ''; serverError = ''"
              />
            </div>
            <p v-if="errors.username" class="text-xs text-red-500 pl-1 flex items-center gap-1">
              <span class="w-1 h-1 rounded-full bg-red-400" />{{ errors.username }}
            </p>
          </div>

          <!-- Password Field -->
          <div class="space-y-2">
            <div class="flex items-center justify-between pl-1 pr-1">
              <label class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-widest" for="password">
                密码
              </label>
            </div>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                <Lock :size="18" />
              </div>
              <input
                id="password"
                v-model="form.password"
                class="w-full bg-surface-subtle/50 border rounded-2xl py-4 pl-12 pr-12 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all"
                :class="errors.password ? 'border-red-300' : 'border-outline/10'"
                placeholder="请输入密码"
                :type="showPass ? 'text' : 'password'"
                @input="errors.password = ''; serverError = ''"
              />
              <button
                type="button"
                class="absolute inset-y-0 right-0 pr-4 flex items-center text-on-surface-variant hover:text-on-surface transition-colors"
                @click="showPass = !showPass"
              >
                <EyeOff v-if="!showPass" :size="18" />
                <Eye v-else :size="18" />
              </button>
            </div>
            <p v-if="errors.password" class="text-xs text-red-500 pl-1 flex items-center gap-1">
              <span class="w-1 h-1 rounded-full bg-red-400" />{{ errors.password }}
            </p>
          </div>

          <!-- Submit Button -->
          <div class="pt-4">
            <button
              type="submit"
              class="w-full flex justify-center items-center gap-2 bg-slate-900 text-white font-display font-bold rounded-2xl py-4 hover:bg-black hover:shadow-xl hover:-translate-y-0.5 active:scale-95 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
              :disabled="loading"
            >
              <span v-if="loading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              <span v-else>登录</span>
              <ArrowRight v-if="!loading" :size="18" />
            </button>
          </div>
        </form>

        <!-- Footer -->
        <div class="mt-12 pt-6 border-t border-outline/10 text-center">
          <p class="font-sans text-on-surface-variant text-sm leading-relaxed">
            还没有账号？
            <router-link to="/register" class="text-primary font-bold hover:underline ml-1.5">立即注册</router-link>
          </p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { User, Lock, EyeOff, Eye, ArrowRight, AlertCircle } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { toast } from '@/composables/useToast'
import { z } from '@/utils/validate'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const showPass = ref(false)
const serverError = ref('')

// ─── Validation Schema ───────────────────────────────────
const loginSchema = z.object({
  username: z.string().min(3, '用户名至少 3 个字符').max(20, '用户名最多 20 个字符').noSpecial('用户名不允许特殊字符'),
  password: z.string().min(6, '密码至少 6 个字符').max(32, '密码最多 32 个字符'),
})

const form = reactive({ username: '', password: '' })
const errors = reactive({ username: '', password: '' })

function validate() {
  errors.username = ''
  errors.password = ''
  serverError.value = ''

  const result = loginSchema.safeParse({
    username: form.username.trim(),
    password: form.password
  })

  if (!result.success) {
    Object.assign(errors, result.errors)
    return false
  }
  return true
}

async function handleLogin() {
  if (!validate()) return
  loading.value = true
  serverError.value = ''
  try {
    await userStore.login(form.username, form.password)
    toast.success('登录成功')
    router.push(route.query.redirect || '/')
  } catch (e) {
    const msg = e?.message || '登录失败，请稍后重试'
    if (e?.code === 401) {
      // 用户名或密码错误 — 光标聚焦用户名
      serverError.value = msg
    } else {
      // 网络错误等其他异常
      serverError.value = msg
    }
  } finally {
    loading.value = false
  }
}
</script>
