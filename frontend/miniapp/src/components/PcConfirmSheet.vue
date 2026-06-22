<template>
  <view v-if="visible" class="pc-confirm-sheet" @tap="$emit('cancel')">
    <view class="pc-confirm-sheet__panel" @tap.stop>
      <text class="pc-confirm-sheet__title">{{ title }}</text>
      <text class="pc-confirm-sheet__message">{{ message }}</text>
      <view class="pc-confirm-sheet__actions">
        <view class="pc-confirm-sheet__btn pc-confirm-sheet__btn--cancel" @tap="$emit('cancel')">
          <text>取消</text>
        </view>
        <view class="pc-confirm-sheet__btn pc-confirm-sheet__btn--confirm" @tap="$emit('confirm')">
          <text>{{ confirmText }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  visible: boolean
  title: string
  message: string
  confirmText?: string
}>(), {
  confirmText: '确定',
})

defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()
</script>

<style scoped>
.pc-confirm-sheet {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: flex-end;
  z-index: 999;
}

.pc-confirm-sheet__panel {
  width: 100%;
  background: #fff;
  border-radius: 20px 20px 0 0;
  padding: 24px 20px;
  padding-bottom: 34px;
}

.pc-confirm-sheet__title {
  font-size: 16px;
  font-weight: 700;
  color: #19322E;
  margin-bottom: 8px;
}

.pc-confirm-sheet__message {
  font-size: 14px;
  color: #71817D;
  margin-bottom: 20px;
}

.pc-confirm-sheet__actions {
  display: flex;
  gap: 12px;
}

.pc-confirm-sheet__btn {
  flex: 1;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
}

.pc-confirm-sheet__btn--cancel {
  background: #FAF8F3;
  color: #71817D;
}

.pc-confirm-sheet__btn--confirm {
  background: #11796F;
  color: #fff;
}
</style>
