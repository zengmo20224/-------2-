<template>
  <view class="pc-page product-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="商品不存在"
      @retry="loadDetail"
    >
      <template v-if="product">
        <view v-if="product.coverUrl" class="product-detail__image-wrap">
          <image class="product-detail__image" :src="product.coverUrl" mode="aspectFill" />
        </view>
        <view class="product-detail__info">
          <text class="product-detail__name">{{ product.name }}</text>
          <text class="product-detail__price">{{ priceText }}</text>
          <text v-if="product.description" class="product-detail__desc">
            {{ product.description }}
          </text>
        </view>
        <view class="product-detail__action">
          <PcPrimaryButton text="加入购物车" @tap="addToCart" />
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { getProductDetail } from '@/api/product'
import { addCartItem } from '@/api/cart'
import { useUserStore } from '@/store/user'
import type { ProductDetail } from '@/types/product'
import { formatYuan } from '@/utils/format'

const userStore = useUserStore()
const product = ref<ProductDetail | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const adding = ref(false)

const priceText = computed(() => formatYuan(product.value?.price ?? 0))

async function loadDetail() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const id = currentPage?.options?.id

  if (!id) {
    pageStatus.value = 'empty'
    return
  }

  pageStatus.value = 'loading'
  const res = await getProductDetail(String(id))

  if (!res.success || !res.data) {
    pageStatus.value = 'error'
    return
  }

  product.value = res.data
  pageStatus.value = 'success'
}

async function addToCart() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!product.value) return

  adding.value = true
  const res = await addCartItem(product.value.id, 1)
  adding.value = false

  if (res.success) {
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  }
}

loadDetail()
</script>

<style scoped>
.product-detail {
  padding: var(--pc-page-padding);
}

.product-detail__image-wrap {
  width: 100%;
  height: 240px;
  border-radius: var(--pc-radius-card);
  overflow: hidden;
  margin-bottom: 16px;
}

.product-detail__image {
  width: 100%;
  height: 100%;
}

.product-detail__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.product-detail__name {
  font-size: var(--pc-font-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}

.product-detail__price {
  font-size: 20px;
  color: var(--pc-user-accent);
  font-weight: 700;
}

.product-detail__desc {
  font-size: var(--pc-font-body);
  color: var(--pc-user-muted);
  line-height: 1.6;
}

.product-detail__action {
  margin-top: 16px;
}
</style>
