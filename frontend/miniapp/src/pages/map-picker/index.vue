<template>
  <view class="map-picker">
    <view v-if="!mapKey" class="map-picker__notice">
      <text class="map-picker__notice-title">未配置地图 Key</text>
      <text class="map-picker__notice-hint">
        请在 .env.development 中设置 VITE_TENCENT_MAP_KEY。
      </text>
      <text class="map-picker__notice-link" @tap="openLbs">前往腾讯位置服务</text>
    </view>

    <iframe
      v-else
      :src="pickerUrl"
      frameborder="0"
      class="map-picker__iframe"
    />
  </view>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, computed } from 'vue'

const mapKey = import.meta.env.VITE_TENCENT_MAP_KEY || ''

const pickerUrl = computed(() => {
  return `https://apis.map.qq.com/tools/locpicker?type=1&key=${mapKey}&referer=petcare`
})

function openLbs() {
  window.open('https://lbs.qq.com/', '_blank')
}

function handleMessage(event: MessageEvent) {
  const loc = event.data
  if (!loc || loc.module !== 'locationPicker') return

  // Emit a global event so the address edit page can receive it
  uni.$emit('map-picked', {
    latitude: loc.lat,
    longitude: loc.lng,
    poiAddress: loc.poiaddress || '',
    poiName: loc.poiname || '',
  })

  uni.navigateBack()
}

onMounted(() => {
  window.addEventListener('message', handleMessage)
})

onUnmounted(() => {
  window.removeEventListener('message', handleMessage)
})
</script>

<style scoped>
.map-picker {
  width: 100%;
  height: 100vh;
}

.map-picker__iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.map-picker__notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 32px;
  gap: 12px;
}

.map-picker__notice-title {
  font-size: 18px;
  font-weight: 700;
  color: #333;
}

.map-picker__notice-hint {
  font-size: 14px;
  color: #999;
  text-align: center;
  line-height: 1.6;
}

.map-picker__notice-link {
  font-size: 14px;
  color: #11796F;
  margin-top: 12px;
}
</style>
