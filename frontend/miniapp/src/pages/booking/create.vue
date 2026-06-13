<template>
  <view class="pc-page booking-create">
    <PcPageHeader title="创建预约" />

    <PcStatePanel :status="pageStatus" empty-text="请从服务页面进入预约">
      <template v-if="serviceItem">
        <!-- Service Summary -->
        <view class="booking-service">
          <text class="booking-service__name">{{ serviceItem.name }}</text>
          <text class="booking-service__price">¥{{ serviceItem.price }}</text>
        </view>

        <!-- Date Selection -->
        <view class="booking-section">
          <text class="booking-label">选择日期</text>
          <view class="booking-dates">
            <view
              v-for="d in dateOptions"
              :key="d.value"
              class="booking-date"
              :class="{ 'booking-date--active': selectedDate === d.value }"
              @tap="selectDate(d.value)"
            >
              <text>{{ d.label }}</text>
            </view>
          </view>
        </view>

        <!-- Time Slots -->
        <view class="booking-section">
          <text class="booking-label">可预约时段</text>
          <PcStatePanel :status="slotsStatus" empty-text="当天暂无可预约时段">
            <view class="booking-slots">
              <view
                v-for="slot in slots"
                :key="slot.startTime"
                class="booking-slot"
                :class="{ 'booking-slot--active': selectedSlot === slot.startTime }"
                @tap="selectedSlot = slot.startTime"
              >
                <text>{{ slot.startTime }}</text>
                <text class="booking-slot__count">{{ slot.availableStaffCount }}人可选</text>
              </view>
            </view>
          </PcStatePanel>
        </view>

        <!-- Contact Info -->
        <view class="booking-section">
          <text class="booking-label">联系人信息</text>
          <PcFormField label="姓名" placeholder="联系人姓名" v-model="contactName" />
          <PcFormField label="电话" placeholder="联系电话" v-model="contactPhone" />
        </view>

        <!-- Submit -->
        <view class="booking-action">
          <PcPrimaryButton text="提交预约" :loading="submitting" @tap="handleSubmit" />
        </view>
      </template>
    </PcStatePanel>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PcPageHeader from '@/components/PcPageHeader.vue'
import PcStatePanel from '@/components/PcStatePanel.vue'
import PcPrimaryButton from '@/components/PcPrimaryButton.vue'
import PcFormField from '@/components/PcFormField.vue'
import { getServiceDetail } from '@/api/service'
import { getAvailability, createBooking } from '@/api/booking'
import { useUserStore } from '@/store/user'
import type { ServiceItem } from '@/types/service'
import type { BookingSlot } from '@/types/booking'

const STORE_ID = '1001'

const userStore = useUserStore()
const serviceItem = ref<ServiceItem | null>(null)
const pageStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

const dateOptions = ref<{label: string; value: string}[]>([])
const selectedDate = ref('')
const selectedSlot = ref('')

const slots = ref<BookingSlot[]>([])
const slotsStatus = ref<'loading' | 'empty' | 'success' | 'error'>('loading')

const contactName = ref('')
const contactPhone = ref('')
const submitting = ref(false)

function initDateOptions() {
  const today = new Date()
  const opts: {label: string; value: string}[] = []
  for (let i = 0; i < 7; i++) {
    const d = new Date(today)
    d.setDate(d.getDate() + i)
    const value = d.toISOString().slice(0, 10)
    const label = i === 0 ? '今天' : i === 1 ? '明天' : `${d.getMonth()+1}/${d.getDate()}`
    opts.push({ label, value })
  }
  dateOptions.value = opts
}

async function loadService(id: string) {
  pageStatus.value = 'loading'
  const res = await getServiceDetail(id)
  if (!res.success || !res.data) {
    pageStatus.value = 'error'
    return
  }
  serviceItem.value = res.data
  pageStatus.value = 'success'

  if (dateOptions.value.length > 0) {
    selectDate(dateOptions.value[0].value)
  }
}

async function selectDate(date: string) {
  selectedDate.value = date
  selectedSlot.value = ''
  await loadSlots()
}

async function loadSlots() {
  if (!serviceItem.value || !selectedDate.value) return

  slotsStatus.value = 'loading'
  const res = await getAvailability({
    storeId: STORE_ID,
    serviceItemId: serviceItem.value.id,
    bookingDate: selectedDate.value,
    serviceMode: serviceItem.value.serviceMode === 'HOME' ? 'HOME' : 'STORE',
  })

  if (!res.success || !res.data) {
    slotsStatus.value = 'error'
    return
  }

  slots.value = res.data.slots
  slotsStatus.value = slots.value.length > 0 ? 'success' : 'empty'
}

async function handleSubmit() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (!serviceItem.value || !selectedDate.value || !selectedSlot.value) {
    uni.showToast({ title: '请选择预约时间', icon: 'none' })
    return
  }
  if (!contactName.value || !contactPhone.value) {
    uni.showToast({ title: '请填写联系人', icon: 'none' })
    return
  }

  submitting.value = true
  const res = await createBooking({
    storeId: STORE_ID,
    serviceItemId: serviceItem.value.id,
    serviceMode: serviceItem.value.serviceMode === 'HOME' ? 'HOME' : 'STORE',
    bookingDate: selectedDate.value,
    startTime: selectedSlot.value,
    contactName: contactName.value,
    contactPhone: contactPhone.value,
    paymentMethod: 'OFFLINE_STORE',
  })
  submitting.value = false

  if (res.success) {
    uni.redirectTo({ url: '/pages/booking/success' })
  }
}

onMounted(() => {
  initDateOptions()
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1] as any
  const serviceId = currentPage?.options?.serviceId

  if (!serviceId) {
    pageStatus.value = 'empty'
    return
  }

  loadService(String(serviceId))
})
</script>

<style scoped>
.booking-create {
  padding: var(--pc-page-padding);
}

.booking-service {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-radius: var(--pc-radius-card);
  padding: 16px;
  margin-bottom: 16px;
}

.booking-service__name {
  font-size: var(--pc-font-card-title);
  font-weight: 700;
  color: var(--pc-user-ink);
}

.booking-service__price {
  font-size: var(--pc-font-body);
  color: var(--pc-user-accent);
  font-weight: 700;
}

.booking-section {
  margin-bottom: 20px;
}

.booking-label {
  font-size: var(--pc-font-body);
  font-weight: 600;
  color: var(--pc-user-ink);
  margin-bottom: 8px;
}

.booking-dates {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.booking-date {
  padding: 8px 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--pc-user-line);
  white-space: nowrap;
}

.booking-date--active {
  background: var(--pc-user-primary);
  border-color: var(--pc-user-primary);
}

.booking-date--active text {
  color: #fff;
}

.booking-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.booking-slot {
  padding: 8px 14px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid var(--pc-user-line);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.booking-slot--active {
  background: var(--pc-user-soft);
  border-color: var(--pc-user-primary);
}

.booking-slot__count {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}

.booking-action {
  margin-top: 24px;
}
</style>
