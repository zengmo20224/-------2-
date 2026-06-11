<template>
  <div class="pc-data-table-shell">
    <el-table
      v-loading="loading"
      :data="data"
      border
      style="width: 100%"
      :empty-text="emptyText"
    >
      <slot />
    </el-table>
    <div class="pc-data-table-shell__pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="currentSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleChange"
        @current-change="handleChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  data: unknown[]
  total: number
  page?: number
  size?: number
  loading?: boolean
  emptyText?: string
}>(), {
  page: 1,
  size: 10,
  loading: false,
  emptyText: '暂无数据',
})

const emit = defineEmits<{
  'page-change': [page: number, size: number]
}>()

const currentPage = ref(props.page)
const currentSize = ref(props.size)

watch(() => props.page, (val) => { currentPage.value = val })
watch(() => props.size, (val) => { currentSize.value = val })

const handleChange = () => {
  emit('page-change', currentPage.value, currentSize.value)
}
</script>

<style scoped>
.pc-data-table-shell__pagination {
  margin-top: var(--pc-spacing-md);
  display: flex;
  justify-content: flex-end;
}
</style>
