<template>
  <view class="pc-page order-confirm">
    <PcPageHeader title="确认订单" />

    <PcStatePanel :status="pageStatus" empty-text="购物车为空">
      <template v-if="cartItems.length > 0">
        <!-- Items Summary -->
        <view class="confirm-section">
          <text class="confirm-label">商品清单</text>
          <view class="confirm-items">
            <view v-for="item in cartItems" :key="item.id" class="confirm-item">
              <text class="confirm-item__name">{{ item.productName }} ×{{ item.quantity }}</text>
              <text class="confirm-item__price">¥{{ item.subtotal }}</text>
            </view>
          </view>
        </view>

        <!-- Contact Info -->
        <view class="confirm-section">
          <text class="confirm-label">联系人信息</text>
          <PcFormField label="姓名" placeholder="取货联系人" v-model="contactName" />
          <PcFormField label="电话" placeholder="联系电话" v-model="contactPhone" />
        </view>

        <!-- Remark -->
        <view class="confirm-section">
          <text class="confirm-label">备注</text>
          <PcFormField label="备注" placeholder="选填" v-model="remark" />
        </view>

        <!-- Total -->
        <view class="confirm-total">
          <text class="confirm-total__label">合计</text>
          <text class="confirm-total__amount">¥{{ totalAmount }}</text>
        </view>

        <view class="confirm-action">
          <PcPrimaryButton text="提交订单" :loading="submitting" @tap="handleSubmit" />
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import PcFormField from '@/components/PcFormField.vue'
import { getCartItems } from '@/api/cart'
import { createOrder } from '@/api/order'
import type { CartItem } from '@/types/product'

const STORE_ID = '1001'

const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const cartItems = ref<CartItem[]>([])
const contactName = ref('')
const contactPhone = ref('')
const remark = ref('')
const submitting = ref(false)

const totalAmount = computed(() =>
  cartItems.value.reduce((sum, item) => sum + item.subtotal, 0)
)

async function loadCart() {
  pageStatus.value = 'loading'
  const res = await getCartItems()
  if (!res.success || !res.data) {
    pageStatus.value = 'error'
    return
  }
  cartItems.value = res.data
  pageStatus.value = cartItems.value.length > 0 ? 'success' : 'empty'
}

async function handleSubmit() {
  if (!contactName.value || !contactPhone.value) {
    uni.showToast({ title: '请填写联系人', icon: 'none' })
    return
  }

  submitting.value = true
  const res = await createOrder({
    storeId: STORE_ID,
    contactName: contactName.value,
    contactPhone: contactPhone.value,
    remark: remark.value || undefined,
  })
  submitting.value = false

  if (res.success) {
    uni.showToast({ title: '下单成功', icon: 'success' })
    setTimeout(() => {
      uni.redirectTo({ url: `/pages/order/detail?id=${res.data!.id}` })
    }, 1000)
  }
}

onMounted(() => loadCart())
</script>

<style scoped>
.order-confirm {
  padding: var(--pc-page-padding);
}

.confirm-section {
  margin-bottom: 20px;
}

.confirm-label {
  font-size: var(--pc-font-body);
  font-weight: 600;
  color: var(--pc-user-ink);
  margin-bottom: 8px;
}

.confirm-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
}

.confirm-item__name {
  font-size: var(--pc-font-body);
  color: var(--pc-user-ink);
}

.confirm-item__price {
  font-size: var(--pc-font-body);
  color: var(--pc-user-accent);
  font-weight: 600;
}

.confirm-total {
  display: flex;
  justify-content: space-between;
  padding: 16px 0;
  border-top: 1px solid var(--pc-user-line);
}

.confirm-total__label {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}

.confirm-total__amount {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-accent);
}

.confirm-action {
  margin-top: 24px;
}
</style>
