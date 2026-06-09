import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '../store/user';

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: () => import('../layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: 'Dashboard', requiresAuth: true }
      },
      {
        path: 'store/info',
        name: 'StoreInfo',
        component: () => import('../views/store/info.vue'),
        meta: { title: 'Store Info', requiresAuth: true }
      },
      {
        path: 'store/config',
        name: 'StoreConfig',
        component: () => import('../views/store/config.vue'),
        meta: { title: 'Store Config', requiresAuth: true }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/error/403.vue'),
    meta: { hidden: true }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('../views/error/404.vue'),
    meta: { hidden: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore();
  const token = userStore.token;

  if (to.path === '/login') {
    if (token) {
      next({ path: '/' });
    } else {
      next();
    }
  } else {
    if (token) {
      if (!userStore.userInfo) {
        try {
          await userStore.getInfoAction();
          next();
        } catch (error) {
          userStore.logoutAction();
          next(`/login?redirect=${to.path}`);
        }
      } else {
        next();
      }
    } else {
      if (to.meta.requiresAuth) {
        next(`/login?redirect=${to.path}`);
      } else {
        next();
      }
    }
  }
});

export default router;