import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/storage'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/article/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetailView.vue')
  },
  {
    path: '/user/:id',
    name: 'Profile',
    component: () => import('@/views/ProfileView.vue')
  },
  {
    path: '/write',
    name: 'WriteArticle',
    component: () => import('@/views/ArticleEditView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/edit/:id',
    name: 'EditArticle',
    component: () => import('@/views/ArticleEditView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: () => import('@/views/AiChatView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/NotificationView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/FavoriteView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/HistoryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/tag/:name',
    name: 'Tag',
    component: () => import('@/views/TagView.vue')
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: () => import('@/views/CategoryView.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { guestOnly: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  const token = getToken()

  if (to.meta.requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if (to.meta.guestOnly && token) {
    next('/')
  } else {
    next()
  }
})

export default router
