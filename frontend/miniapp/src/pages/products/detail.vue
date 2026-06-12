<template>
  <view class="pc-page product-detail">
    <PcStatePanel
      :status="pageStatus"
      empty-text="商品不存在"
      @retry="loadDetail"
    >
      <template v-if="product">
        <view v-if="product.imageUrl" class="product-detail__image-wrap">
          <image class="product-detail__image" :src="product.imageUrl" mode="aspectFill" />
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
import type { ProductItem } from '@/types/product'
import { formatYuan } from '@/utils/format'

const product = ref<ProductItem | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('empty')

const priceText = computed(() => formatYuan(product.value?.price))

function loadDetail() {
  pageStatus.value = 'empty'
}

function addToCart() {
  uni.showToast({ title: '请先登录', icon: 'none' })
}
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
