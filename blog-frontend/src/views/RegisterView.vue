<template>
  <div class="min-h-screen relative overflow-hidden flex items-center justify-center p-6">
    <!-- Mesh Background Decorations -->
    <div class="absolute top-[-20%] left-[-20%] w-[80vw] h-[80vw] rounded-full bg-primary/5 blur-[120px] pointer-events-none" />
    <div class="absolute bottom-[-20%] right-[-20%] w-[90vw] h-[90vw] rounded-full bg-primary-light/10 blur-[150px] pointer-events-none" />

    <main class="relative z-10 w-full max-w-[440px]">
      <!-- Glassmorphism Card -->
      <div class="bg-surface-card/60 backdrop-blur-2xl border border-white/60 dark:border-outline/15 rounded-[32px] shadow-[0_32px_64px_-16px_rgba(0,0,0,0.1)] p-6 sm:p-10 md:p-12">
        <!-- Brand Chip -->
        <div class="text-center mb-10">
          <div class="inline-flex items-center gap-2.5 px-4 py-2 rounded-full bg-primary-light/40 border border-primary/10 mb-6">
            <div class="w-5 h-5 rounded-md bg-primary flex items-center justify-center">
              <span class="font-display font-black text-[10px] text-white leading-none">T</span>
            </div>
            <span class="font-display font-bold text-xs text-primary tracking-wide">技术脉动 · TechPulse</span>
          </div>

          <h1 class="font-display font-bold text-3xl text-on-surface tracking-tight">创建账号</h1>
          <p class="font-sans text-on-surface-variant mt-2 text-sm leading-relaxed max-w-xs mx-auto">加入技术脉动，分享你的技术见解与代码实践。</p>
        </div>

        <form class="space-y-5" @submit.prevent="handleRegister">
          <!-- Username -->
          <div class="space-y-2">
            <label class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-widest pl-1">用户名</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                <User :size="18" />
              </div>
              <input
                v-model="form.username"
                class="w-full bg-surface-subtle/50 border border-outline/10 rounded-2xl py-3.5 pl-12 pr-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all"
                placeholder="3-20位字符"
                type="text"
              />
            </div>
            <p v-if="errors.username" class="text-xs text-red-500 pl-1">{{ errors.username }}</p>
          </div>

          <!-- Password -->
          <div class="space-y-2">
            <label class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-widest pl-1">密码</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                <Lock :size="18" />
              </div>
              <input
                v-model="form.password"
                class="w-full bg-surface-subtle/50 border border-outline/10 rounded-2xl py-3.5 pl-12 pr-12 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all"
                placeholder="6-20位字符"
                :type="showPass ? 'text' : 'password'"
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
            <p v-if="errors.password" class="text-xs text-red-500 pl-1">{{ errors.password }}</p>
          </div>

          <!-- Confirm Password -->
          <div class="space-y-2">
            <label class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-widest pl-1">确认密码</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                <ShieldCheck :size="18" />
              </div>
              <input
                v-model="form.confirmPassword"
                class="w-full bg-surface-subtle/50 border border-outline/10 rounded-2xl py-3.5 pl-12 pr-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all"
                placeholder="再次输入密码"
                type="password"
              />
            </div>
            <p v-if="errors.confirmPassword" class="text-xs text-red-500 pl-1">{{ errors.confirmPassword }}</p>
          </div>

          <!-- Nickname -->
          <div class="space-y-2">
            <label class="font-display font-bold text-[10px] text-on-surface-variant uppercase tracking-widest pl-1">
              昵称 <span class="font-normal normal-case">选填</span>
            </label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-on-surface-variant group-focus-within:text-primary transition-colors">
                <Smile :size="18" />
              </div>
              <input
                v-model="form.nickname"
                class="w-full bg-surface-subtle/50 border border-outline/10 rounded-2xl py-3.5 pl-12 pr-4 font-sans text-sm text-on-surface placeholder:text-on-surface-variant focus:ring-4 focus:ring-primary/5 focus:bg-surface-card focus:border-primary/20 outline-none transition-all"
                placeholder="你的显示名称"
                type="text"
              />
            </div>
          </div>

          <!-- Submit -->
          <div class="pt-2">
            <button
              type="submit"
              class="w-full flex justify-center items-center gap-2 bg-slate-900 text-white font-display font-bold rounded-2xl py-4 hover:bg-black hover:shadow-xl hover:-translate-y-0.5 active:scale-95 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed dark:bg-white dark:text-slate-900 dark:hover:bg-slate-200"
              :disabled="loading"
            >
              <span v-if="loading" class="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              <span v-else>注册</span>
              <ArrowRight v-if="!loading" :size="18" />
            </button>
          </div>
        </form>

        <!-- Footer -->
        <div class="mt-10 pt-6 border-t border-outline/10 text-center">
          <p class="font-sans text-on-surface-variant text-sm leading-relaxed">
            已有账号？
            <router-link to="/login" class="text-primary font-bold hover:underline ml-1.5">立即登录</router-link>
          </p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, EyeOff, Eye, ShieldCheck, Smile, ArrowRight } from 'lucide-vue-next'
import { useUserStore } from '@/stores/user'
import { toast } from '@/composables/useToast'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showPass = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

function validate() {
  let ok = true
  errors.username = ''
  errors.password = ''
  errors.confirmPassword = ''

  if (!form.username.trim()) {
    errors.username = '请输入用户名'
    ok = false
  } else if (form.username.length < 3 || form.username.length > 20) {
    errors.username = '用户名长度为3-20位'
    ok = false
  }

  if (!form.password) {
    errors.password = '请输入密码'
    ok = false
  } else if (form.password.length < 6 || form.password.length > 20) {
    errors.password = '密码长度为6-20位'
    ok = false
  }

  if (!form.confirmPassword) {
    errors.confirmPassword = '请确认密码'
    ok = false
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = '两次输入的密码不一致'
    ok = false
  }

  return ok
}

async function handleRegister() {
  if (!validate()) return
  loading.value = true
  try {
    const { confirmPassword, ...data } = form
    await userStore.register(data)
    toast.success('注册成功，请登录')
    router.push('/login')
  } catch (e) {
    toast.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>
