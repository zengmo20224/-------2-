<template>
  <view class="pc-page products-page">
    <PcPageHeader title="商品" />

    <view class="products-intro">
      <text class="products-intro__kicker">PET SELECT</text>
      <text class="products-intro__title">把日常照护，变成它喜欢的事</text>
      <text class="products-intro__desc">门店精选主粮、零食与生活用品，到店自提更安心。</text>
    </view>

    <view class="products-search">
      <input
        v-model="searchKeyword"
        class="products-search__input"
        type="text"
        confirm-type="search"
        placeholder="搜索商品"
        @confirm="handleSearch"
      />
      <view v-if="searchKeyword" class="products-search__clear" @tap="clearSearch">
        <text>清空</text>
      </view>
      <view class="products-search__button" @tap="handleSearch">
        <text>搜索</text>
      </view>
    </view>

    <!-- Category Tabs (horizontal scroll) -->
    <scroll-view class="products-tabs" scroll-x>
      <view class="products-tabs__track">
        <view
          class="products-tab"
          :class="{ 'products-tab--active': activeCategoryId === '' }"
          @tap="switchCategory('')"
        >
          <text>全部</text>
        </view>
        <view
          v-for="cat in categories"
          :key="cat.id"
          class="products-tab"
          :class="{ 'products-tab--active': activeCategoryId === cat.id }"
          @tap="switchCategory(cat.id)"
        >
          <text>{{ cat.name }}</text>
        </view>
      </view>
    </scroll-view>

    <PcStatePanel
      :status="listStatus"
      empty-text="暂无商品"
      @retry="loadProducts"
    >
      <view class="products-grid">
        <PcProductCard
          v-for="item in products"
          :key="item.id"
          :product-id="item.id"
          :name="item.name"
          :price="item.price"
          :cover-url="item.coverUrl"
          :sales-count="item.salesCount"
          :badge="item.salesCount >= 100 ? '人气好物' : undefined"
          @tap="goDetail(item.id)"
        />
      </view>
    </PcStatePanel>
    <view class="products-cart-fab" aria-label="购物车" @tap="goCart">
      <text class="products-cart-fab__icon">🛒</text>
      <view v-if="cartCount > 0" class="products-cart-fab__badge">
        <text class="products-cart-fab__badge-text">{{ cartCount > 99 ? '99+' : cartCount }}</text>
      </view>
    </view>
    <PcBottomNav current-path="pages/products/index" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcProductCard from '@/components/PcProductCard.vue'
import PcBottomNav from '@/components/PcBottomNav.vue'
import { getProducts, getProductCategories } from '@/api/product'
import { getCartItems } from '@/api/cart'
import { useUserStore } from '@/store/user'
import type { ProductItem, ProductCategory } from '@/types/product'

const userStore = useUserStore()
const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const products = ref<ProductItem[]>([])
const categories = ref<ProductCategory[]>([])
const activeCategoryId = ref('')
const searchKeyword = ref('')
const cartCount = ref(0)

async function loadCategories() {
  try {
    const res = await getProductCategories()
    if (res.success && res.data) {
      categories.value = res.data
    }
  } catch {
    categories.value = []
  }
}

async function loadProducts() {
  listStatus.value = 'loading'

  const params: { size: number; categoryId?: string; keyword?: string } = { size: 50 }
  if (activeCategoryId.value) params.categoryId = activeCategoryId.value
  const keyword = searchKeyword.value.trim()
  if (keyword) params.keyword = keyword

  try {
    const res = await getProducts(params)
    if (!res.success || !res.data) {
      listStatus.value = 'error'
      return
    }

    products.value = res.data.items
    listStatus.value = products.value.length > 0 ? 'success' : 'empty'
  } catch {
    listStatus.value = 'error'
  }
}

function switchCategory(categoryId: string) {
  if (activeCategoryId.value === categoryId) return
  activeCategoryId.value = categoryId
  loadProducts()
}

function handleSearch() {
  loadProducts()
}

function clearSearch() {
  if (!searchKeyword.value) return
  searchKeyword.value = ''
  loadProducts()
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/products/detail?id=${id}` })
}

function goCart() {
  uni.navigateTo({ url: '/pages/order/cart' })
}

/** Reload cart item count for the floating cart badge. Only for logged-in users. */
async function loadCartCount() {
  if (!userStore.isLoggedIn) {
    cartCount.value = 0
    return
  }
  try {
    const res = await getCartItems()
    if (res.success && res.data) {
      cartCount.value = res.data.reduce((sum, item) => sum + item.quantity, 0)
    }
  } catch {
    cartCount.value = 0
  }
}

onLoad(() => {
  loadCategories()
  loadProducts()
  loadCartCount()
})

// Refresh cart count when returning from cart/detail pages (after add-to-cart)
onShow(() => {
  loadCartCount()
})
</script>

<style scoped>
.products-page {
  min-height: 100vh;
  padding: 20px 20px 96px;
  background: #FAF8F3;
}

.products-cart-fab {
  position: fixed;
  right: 20px;
  bottom: 96px;
  z-index: 880;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  background: #11796F;
  background: linear-gradient(145deg, #16877c, #0C4D48);
  border: 3px solid rgba(255, 255, 255, 0.92);
  border-radius: 50%;
  box-shadow: 0 12px 28px rgba(12, 77, 72, 0.3);
}

.products-cart-fab:active {
  transform: scale(0.93);
}

.products-cart-fab__icon {
  color: #fff;
  font-size: 25px;
  font-weight: 700;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.products-cart-fab__badge {
  position: absolute;
  top: -2px;
  right: -2px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 10px;
  background: #E97951;
  border: 2px solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.products-cart-fab__badge-text {
  font-size: 11px;
  color: #fff;
  font-weight: 700;
  line-height: 1;
}

.products-intro {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 18px;
  min-height: 118px;
  padding: 21px 20px;
  border: 1px solid rgba(17, 121, 111, 0.16);
  border-radius: 22px;
  background:
    radial-gradient(circle at 92% 18%, rgba(255, 218, 138, 0.82) 0 38px, transparent 39px),
    linear-gradient(135deg, #F5FFFC 0%, #FFFFFF 46%, #E7F6F1 100%);
  background:
    radial-gradient(circle at 92% 18%, rgba(245, 166, 35, 0.3) 0 38px, transparent 39px),
    linear-gradient(135deg, #fff, #DFF2ED);
  box-shadow: 0 12px 30px rgba(25, 50, 46, 0.08);
}

.products-intro__kicker {
  font-size: 9px;
  font-weight: 800;
  letter-spacing: 1.4px;
  color: #E97951;
  color: #E97951;
}

.products-intro__title {
  max-width: 270px;
  font-size: 20px;
  line-height: 1.35;
  font-weight: 800;
  color: #19322E;
  color: #0C4D48;
}

.products-intro__desc {
  max-width: 290px;
  font-size: 11px;
  color: #5F746F;
  color: #71817D;
}

.products-search {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  padding: 8px;
  border: 1px solid #E2E9E6;
  border: 1px solid #E2E9E6;
  border-radius: 16px;
  background: #fff;
}

.products-search__input {
  flex: 1;
  min-width: 0;
  height: 34px;
  padding: 0 4px;
  font-size: 14px;
  color: #0C4D48;
}

.products-search__clear,
.products-search__button {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
}

.products-search__clear {
  background: #F3F7F5;
  background: #F3F7F5;
}

.products-search__clear text {
  font-size: 12px;
  color: #71817D;
}

.products-search__button {
  background: #11796F;
  background: #11796F;
}

.products-search__button text {
  font-size: 13px;
  font-weight: 700;
  color: #fff;
}

/* Category Tabs */
.products-tabs {
  margin-bottom: 16px;
  white-space: nowrap;
  width: 100%;
}

.products-tabs__track {
  display: inline-flex;
  gap: 8px;
  min-width: 100%;
  padding-right: 20px;
  box-sizing: border-box;
}

.products-tab {
  display: flex;
  align-items: center;
  padding: 7px 17px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #E2E9E6;
  border: 1px solid #E2E9E6;
  flex-shrink: 0;
}

.products-tab--active {
  background: #11796F;
  background: #11796F;
  border-color: #11796F;
  border-color: #11796F;
}

.products-tab--active text {
  color: #fff;
}

.products-tab text {
  font-size: 14px;
  color: #5F746F;
  color: #71817D;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
</style>
