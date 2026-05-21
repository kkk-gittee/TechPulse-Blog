<template>
  <div class="min-h-[calc(100vh-64px)] flex items-center justify-center p-8">
    <div class="text-center max-w-md">
      <div class="text-[80px] sm:text-[120px] font-display font-black leading-none text-on-surface/5 select-none mb-4">
        404
      </div>
      <h1 class="font-display font-bold text-2xl text-on-surface mb-3">页面不存在</h1>
      <p class="text-sm text-on-surface-variant leading-relaxed mb-8">
        你访问的页面可能已被删除、移动或从未存在过。
      </p>
      <div class="flex items-center justify-center gap-3">
        <button
          class="px-6 py-3 bg-surface-subtle text-on-surface-variant rounded-xl font-medium text-sm hover:bg-surface-card hover:text-on-surface transition-colors border border-outline/15"
          @click="$router.back()"
        >
          返回上页
        </button>
        <router-link
          to="/"
          class="px-6 py-3 bg-primary text-white rounded-xl font-bold text-sm hover:opacity-90 active:scale-95 transition-all inline-block"
        >
          回到首页
        </router-link>
      </div>
    </div>
  </div>
</template>
