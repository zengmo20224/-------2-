<template>
  <div class="pc-filter-bar">
    <el-form :inline="true" class="pc-filter-bar__form">
      <slot />
      <el-form-item>
        <el-button type="primary" @click="$emit('search')">查询</el-button>
        <el-button @click="$emit('reset')">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
defineEmits<{
  search: []
  reset: []
}>()
</script>

<style scoped>
.pc-filter-bar {
  margin-bottom: var(--pc-spacing-md);
}

.pc-filter-bar__form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
</style>
