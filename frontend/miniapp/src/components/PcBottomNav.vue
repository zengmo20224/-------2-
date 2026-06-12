<template>
  <view class="pc-bottom-nav">
    <view
      v-for="tab in tabs"
      :key="tab.pagePath"
      class="pc-bottom-nav__item"
      :class="{ 'pc-bottom-nav__item--active': currentPath === tab.pagePath }"
      @tap="onTabTap(tab.pagePath)"
    >
      <image
        class="pc-bottom-nav__icon"
        :src="currentPath === tab.pagePath ? tab.selectedIconPath : tab.iconPath"
        mode="aspectFit"
      />
      <text class="pc-bottom-nav__label">{{ tab.text }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
interface TabItem {
  pagePath: string
  text: string
  iconPath: string
  selectedIconPath: string
}

defineProps<{
  tabs: TabItem[]
  currentPath: string
}>()

const emit = defineEmits<{
  (e: 'switch', path: string): void
}>()

function onTabTap(path: string) {
  emit('switch', path)
  uni.switchTab({ url: `/${path}` })
}
</script>

<style scoped>
.pc-bottom-nav {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: var(--pc-tab-bar-height);
  background: #fff;
  border-top: 1px solid var(--pc-user-line);
  padding-bottom: env(safe-area-inset-bottom);
}

.pc-bottom-nav__item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  gap: 2px;
  padding: 6px 0;
}

.pc-bottom-nav__item--active .pc-bottom-nav__label {
  color: var(--pc-user-primary);
  font-weight: 700;
}

.pc-bottom-nav__icon {
  width: 24px;
  height: 24px;
}

.pc-bottom-nav__label {
  font-size: var(--pc-font-caption);
  color: var(--pc-user-muted);
}
</style>
