<template>
  <el-drawer
    :model-value="visible"
    :title="title"
    :size="width"
    @close="handleClose"
  >
    <div v-loading="loading" class="pc-detail-drawer__content">
      <slot />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  visible: boolean
  title?: string
  width?: string
  loading?: boolean
}>(), {
  title: '详情',
  width: '500px',
  loading: false,
})

const emit = defineEmits<{
  close: []
}>()

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.pc-detail-drawer__content {
  padding: var(--pc-spacing-md);
  min-height: 100px;
}
</style>
