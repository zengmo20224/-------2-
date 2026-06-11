<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    :width="width"
    @close="handleCancel"
  >
    <p class="pc-action-confirm-dialog__message">{{ message }}</p>
    <template v-if="$slots.default">
      <slot />
    </template>
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button
        :type="danger ? 'danger' : 'primary'"
        :loading="confirming"
        @click="handleConfirm"
      >
        确认
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  message?: string
  danger?: boolean
  width?: string
  loading?: boolean
}>(), {
  title: '确认操作',
  message: '确定执行此操作吗？',
  danger: false,
  width: '420px',
  loading: false,
})

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const confirming = ref(false)

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.pc-action-confirm-dialog__message {
  color: var(--pc-ink);
  font-size: var(--pc-font-size-base);
  margin: 0 0 var(--pc-spacing-md) 0;
  line-height: 1.6;
}
</style>
