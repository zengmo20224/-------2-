<template>
  <view class="pc-page order-cart">
    <PcPageHeader title="购物车" />

    <PcStatePanel v-if="!isLoggedIn" status="unauthorized" />

    <PcStatePanel
      v-else
      :status="listStatus"
      empty-text="购物车是空的"
      @retry="loadCart"
    >
      <view class="cart-list">
        <view v-for="item in cartItems" :key="item.id" class="cart-item">
          <view class="cart-item__info">
            <text class="cart-item__name">{{ item.productName }}</text>
            <text class="cart-item__price">¥{{ item.productPrice }}</text>
          </view>
          <view class="cart-item__controls">
            <view class="cart-qty">
              <text class="cart-qty__btn" @tap="changeQty(item, -1)">-</text>
              <text class="cart-qty__val">{{ item.quantity }}</text>
              <text class="cart-qty__btn" @tap="changeQty(item, 1)">+</text>
            </view>
            <text class="cart-item__remove" @tap="removeItem(item.id)">删除</text>
          </view>
        </view>
      </view>

      <view v-if="cartItems.length > 0" class="cart-footer">
        <text class="cart-footer__total">合计：¥{{ totalAmount }}</text>
        <PcPrimaryButton text="去结算" @tap="goCheckout" />
      </view>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import { getCartItems, updateCartItem, deleteCartItem } from '@/api/cart'
import { useUserStore } from '@/store/user'
import type { CartItem } from '@/types/product'

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const cartItems = ref<CartItem[]>([])

const totalAmount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.subtotal, 0)
)

async function loadCart() {
  listStatus.value = 'loading'
  const res = await getCartItems()
  if (!res.success || !res.data) {
    listStatus.value = 'error'
    return
  }
  cartItems.value = res.data
  listStatus.value = cartItems.value.length > 0 ? 'success' : 'empty'
}

async function changeQty(item: CartItem, delta: number) {
  const newQty = item.quantity + delta
  if (newQty < 1) return
  const res = await updateCartItem(item.id, newQty)
  if (res.success) {
    await loadCart()
  }
}

async function removeItem(id: string) {
  const res = await deleteCartItem(id)
  if (res.success) {
    await loadCart()
  }
}

function goCheckout() {
  uni.navigateTo({ url: '/pages/order/confirm' })
}

onMounted(() => {
  if (isLoggedIn.value) loadCart()
})
</script>

<style scoped>
.order-cart {
  padding: var(--pc-page-padding);
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: var(--pc-card-gap);
}

.cart-item {
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 16px;
  box-shadow: 0 2px 8px rgba(25, 50, 46, 0.06);
}

.cart-item__info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.cart-item__name {
  font-size: var(--pc-font-body);
  font-weight: 600;
  color: var(--pc-user-ink);
}

.cart-item__price {
  font-size: var(--pc-font-body);
  color: var(--pc-user-accent);
  font-weight: 600;
}

.cart-item__controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-qty {
  display: flex;
  align-items: center;
  gap: 12px;
}

.cart-qty__btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--pc-user-soft);
  text-align: center;
  line-height: 28px;
  font-size: var(--pc-font-body);
  color: var(--pc-user-primary);
}

.cart-qty__val {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
  min-width: 24px;
  text-align: center;
}

.cart-item__remove {
  font-size: var(--pc-font-caption);
  color: #e05050;
}

.cart-footer {
  margin-top: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.cart-footer__total {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-accent);
}
</style>
