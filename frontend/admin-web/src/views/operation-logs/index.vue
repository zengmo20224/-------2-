<template>
  <div class="pc-operation-logs">
    <h2 class="pc-operation-logs__title">操作日志</h2>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="模块">
        <el-input v-model="queryParams.module" placeholder="模块名称" clearable style="width: 150px" />
      </el-form-item>
    </FilterBar>

    <DataTableShell
      :data="tableData"
      :total="total"
      :page="queryParams.page"
      :size="queryParams.size"
      :loading="loading"
      @page-change="handlePageChange"
    >
      <el-table-column prop="module" label="模块" width="130" />
      <el-table-column prop="operation" label="操作" width="160" show-overflow-tooltip />
      <el-table-column prop="requestMethod" label="方法" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="methodTagType(row.requestMethod)">{{ row.requestMethod }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="requestUrl" label="URL" show-overflow-tooltip />
      <el-table-column prop="result" label="结果" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="row.result === 'SUCCESS' ? 'success' : 'danger'">{{ row.result }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip>
        <template #default="{ row }">{{ row.errorMessage || '-' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="时间" width="170" />
    </DataTableShell>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOperationLogs } from '../../api/operation-log'
import type { OperationLog } from '../../api/operation-log'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 20, module: '' })

const methodTagType = (method: string) => {
  switch (method) {
    case 'GET': return 'success'
    case 'POST': return 'primary'
    case 'PUT': return 'warning'
    case 'PATCH': return 'warning'
    case 'DELETE': return 'danger'
    default: return 'info'
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getOperationLogs(queryParams)
    if (res.data) { tableData.value = res.data.items; total.value = res.data.total }
  } catch { /* handled */ } finally { loading.value = false }
}

const handlePageChange = (page: number, size: number) => {
  queryParams.page = page
  queryParams.size = size
  fetchData()
}

const handleReset = () => {
  queryParams.module = ''
  queryParams.page = 1
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.pc-operation-logs {
  padding: 0;
}

.pc-operation-logs__title {
  margin: 0 0 var(--pc-spacing-lg) 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}
</style>
