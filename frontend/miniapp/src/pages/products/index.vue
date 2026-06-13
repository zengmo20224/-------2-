<template>
  <view class="pc-page products-page">
    <PcPageHeader title="商品" />

    <PcStatePanel
      :status="listStatus"
      empty-text="暂无商品"
      @retry="loadProducts"
    >
      <view class="products-grid">
        <PcProductCard
          v-for="item in products"
          :key="item.id"
          :name="item.name"
          :price="item.price"
          :image-url="item.coverUrl || undefined"
          @tap="goDetail(item.id)"
        />
      </view>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcProductCard from '@/components/PcProductCard.vue'
import { getProducts } from '@/api/product'
import type { ProductItem } from '@/types/product'

const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const products = ref<ProductItem[]>([])

async function loadProducts() {
  listStatus.value = 'loading'

  const res = await getProducts({ size: 50 })

  if (!res.success || !res.data) {
    listStatus.value = 'error'
    return
  }

  products.value = res.data.items
  listStatus.value = products.value.length > 0 ? 'success' : 'empty'
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/products/detail?id=${id}` })
}

loadProducts()
</script>

<style scoped>
.products-page {
  padding: var(--pc-page-padding);
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--pc-card-gap);
}
</style>
