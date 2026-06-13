<template>
  <view class="pc-page booking-list">
    <PcPageHeader title="我的预约" />

    <PcStatePanel
      v-if="!isLoggedIn"
      status="unauthorized"
    />

    <PcStatePanel
      v-else
      :status="listStatus"
      empty-text="暂无预约记录"
      @retry="loadBookings"
    >
      <view class="booking-list__items">
        <PcBookingCard
          v-for="item in bookings"
          :key="item.id"
          :service-name="`预约 #${item.bookingNo}`"
          :status="item.status"
          :status-label="statusLabels[item.status] || item.status"
          :booking-date="item.bookingDate"
          :time-slot="`${item.startTime}-${item.endTime}`"
          @tap="goDetail(item.id)"
        />
      </view>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcBookingCard from '@/components/PcBookingCard.vue'
import { getMyBookings } from '@/api/booking'
import { useUserStore } from '@/store/user'
import type { BookingItem } from '@/types/booking'

const statusLabels: Record<string, string> = {
  PENDING_CONFIRM: '待确认',
  CONFIRMED: '已确认',
  IN_SERVICE: '服务中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
  REJECTED: '已拒绝',
}

const userStore = useUserStore()
const isLoggedIn = computed(() => userStore.isLoggedIn)

const listStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')
const bookings = ref<BookingItem[]>([])

async function loadBookings() {
  listStatus.value = 'loading'

  const res = await getMyBookings({ size: 20 })

  if (!res.success || !res.data) {
    listStatus.value = 'error'
    return
  }

  bookings.value = res.data.items
  listStatus.value = bookings.value.length > 0 ? 'success' : 'empty'
}

function goDetail(id: string) {
  uni.navigateTo({ url: `/pages/booking/detail?id=${id}` })
}

onMounted(() => {
  if (isLoggedIn.value) {
    loadBookings()
  }
})
</script>

<style scoped>
.booking-list {
  padding: var(--pc-page-padding);
}

.booking-list__items {
  display: flex;
  flex-direction: column;
  gap: var(--pc-card-gap);
}
</style>
