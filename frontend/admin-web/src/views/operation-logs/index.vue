<template>
  <div class="operation-logs-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>操作日志</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="模块">
          <el-input v-model="queryParams.module" placeholder="模块名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
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
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getOperationLogs } from '../../api/operation-log'
import type { OperationLog } from '../../api/operation-log'
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
  try { const res = await getOperationLogs(queryParams); if (res.data) { tableData.value = res.data.items; total.value = res.data.total } }
  catch { /* handled */ } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.operation-logs-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
