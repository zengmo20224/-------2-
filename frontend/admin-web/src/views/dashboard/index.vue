<template>
  <div class="dashboard-container">
    <!-- Welcome card -->
    <el-card shadow="never" class="welcome-card">
      <div class="welcome">
        <div class="welcome__text">
          <h2 class="welcome__title">{{ greeting }}，{{ displayName }}</h2>
          <p class="welcome__sub">角色：{{ userStore.userInfo?.role || '—' }} · 拥有 {{ permCount }} 项权限</p>
        </div>
        <div class="welcome__badge">{{ userStore.userInfo?.role || 'ADMIN' }}</div>
      </div>
    </el-card>

    <!-- Stat cards (each gated by its own permission) -->
    <div class="stat-grid">
      <el-card v-if="userStore.hasPermission('product:item:read')" shadow="hover" class="stat-card" @click="router.push('/products')">
        <div class="stat-card__body">
          <div class="stat-card__icon stat-card__icon--product">📦</div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.productCount ?? '—' }}</div>
            <div class="stat-card__label">在售商品</div>
          </div>
        </div>
        <div class="stat-card__hint">点击管理商品 →</div>
      </el-card>

      <el-card v-if="userStore.hasPermission('product:order:read')" shadow="hover" class="stat-card" @click="router.push('/product-orders')">
        <div class="stat-card__body">
          <div class="stat-card__icon stat-card__icon--order">🛍️</div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.orderCount ?? '—' }}</div>
            <div class="stat-card__label">商品订单</div>
          </div>
        </div>
        <div class="stat-card__hint">点击处理订单 →</div>
      </el-card>

      <el-card v-if="userStore.hasPermission('booking:booking:read')" shadow="hover" class="stat-card" @click="router.push('/bookings')">
        <div class="stat-card__body">
          <div class="stat-card__icon stat-card__icon--booking">📅</div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.bookingCount ?? '—' }}</div>
            <div class="stat-card__label">服务预约</div>
          </div>
        </div>
        <div class="stat-card__hint">点击查看预约 →</div>
      </el-card>

      <el-card v-if="userStore.hasPermission('community:post:read')" shadow="hover" class="stat-card" @click="router.push('/community/posts')">
        <div class="stat-card__body">
          <div class="stat-card__icon stat-card__icon--community">💬</div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.postCount ?? '—' }}</div>
            <div class="stat-card__label">社区帖子</div>
          </div>
        </div>
        <div class="stat-card__hint">点击审核帖子 →</div>
      </el-card>
    </div>

    <!-- Quick entries -->
    <el-card shadow="never" class="quick-card">
      <template #header><span class="quick-card__title">快捷入口</span></template>
      <div class="quick-grid">
        <el-button v-if="userStore.hasPermission('store:info:update')" @click="router.push('/store/info')">门店信息</el-button>
        <el-button v-if="userStore.hasPermission('service:item:read')" @click="router.push('/services')">服务项管理</el-button>
        <el-button v-if="userStore.hasPermission('staff:profile:read')" @click="router.push('/staff')">员工管理</el-button>
        <el-button v-if="userStore.hasPermission('community:report:handle')" @click="router.push('/community/reports')">举报处理</el-button>
        <el-button v-if="userStore.hasPermission('community:sensitive-word:manage')" @click="router.push('/moderation/sensitive-words')">敏感词</el-button>
        <el-button v-if="userStore.hasPermission('admin:operation-log:read')" @click="router.push('/operation-logs')">操作日志</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store/user'
import { getProductList } from '../../api/product'
import { getProductOrderList } from '../../api/product-order'
import { getBookingList } from '../../api/booking'
import { getPostList } from '../../api/community'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive<{
  productCount: number | null
  orderCount: number | null
  bookingCount: number | null
  postCount: number | null
}>({
  productCount: null,
  orderCount: null,
  bookingCount: null,
  postCount: null,
})

const displayName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '管理员')
const permCount = computed(() => userStore.userInfo?.permissions?.length ?? 0)

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

/** Fetch total counts from each list endpoint (size=1 to minimize payload). */
async function loadStats() {
  const tasks: Promise<void>[] = []
  if (userStore.hasPermission('product:item:read')) {
    tasks.push(
      getProductList({ page: 1, size: 1 })
        .then(res => { stats.productCount = res.data?.total ?? 0 })
        .catch(() => { stats.productCount = 0 })
    )
  }
  if (userStore.hasPermission('product:order:read')) {
    tasks.push(
      getProductOrderList({ page: 1, size: 1 })
        .then(res => { stats.orderCount = res.data?.total ?? 0 })
        .catch(() => { stats.orderCount = 0 })
    )
  }
  if (userStore.hasPermission('booking:booking:read')) {
    tasks.push(
      getBookingList({ page: 1, size: 1 })
        .then(res => { stats.bookingCount = res.data?.total ?? 0 })
        .catch(() => { stats.bookingCount = 0 })
    )
  }
  if (userStore.hasPermission('community:post:read')) {
    tasks.push(
      getPostList({ page: 1, size: 1 })
        .then(res => { stats.postCount = res.data?.total ?? 0 })
        .catch(() => { stats.postCount = 0 })
    )
  }
  await Promise.all(tasks)
}

onMounted(loadStats)
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Welcome */
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.welcome__title {
  margin: 0 0 6px 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--pc-text-primary, #1f2d3d);
}

.welcome__sub {
  margin: 0;
  font-size: 13px;
  color: var(--pc-text-secondary, #909399);
}

.welcome__badge {
  padding: 6px 16px;
  border-radius: 20px;
  background: var(--pc-primary, #11796F);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

/* Stat cards */
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card__body {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-card__icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-card__icon--product { background: rgba(17, 121, 111, 0.12); }
.stat-card__icon--order { background: rgba(245, 166, 35, 0.15); }
.stat-card__icon--booking { background: rgba(64, 158, 255, 0.12); }
.stat-card__icon--community { background: rgba(245, 106, 106, 0.12); }

.stat-card__value {
  font-size: 26px;
  font-weight: 700;
  color: var(--pc-text-primary, #1f2d3d);
  line-height: 1.2;
}

.stat-card__label {
  font-size: 13px;
  color: var(--pc-text-secondary, #909399);
  margin-top: 2px;
}

.stat-card__hint {
  margin-top: 12px;
  font-size: 12px;
  color: var(--pc-primary, #11796F);
}

/* Quick entries */
.quick-card__title {
  font-weight: 600;
}

.quick-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
