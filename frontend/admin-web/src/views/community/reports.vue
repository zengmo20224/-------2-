<template>
  <div class="pc-community-reports">
    <h2 class="pc-community-reports__title">举报处理</h2>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 160px">
          <el-option v-for="(v, k) in REPORT_STATUS" :key="k" :label="v.label" :value="k" />
        </el-select>
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
      <el-table-column prop="postId" label="帖子ID" width="90" />
      <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="REPORT_STATUS[row.status as ReportStatusType]?.color || 'info'">
            {{ REPORT_STATUS[row.status as ReportStatusType]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleResult" label="处理结果" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.handleResult" :type="REPORT_HANDLE_RESULT[row.handleResult as ReportHandleResultType]?.color || 'info'" size="small">
            {{ REPORT_HANDLE_RESULT[row.handleResult as ReportHandleResultType]?.label || row.handleResult }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="举报时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" v-if="row.status === 'PENDING'" @click="openHandleDialog(row)" :disabled="!userStore.hasPermission('community:report:handle')">处理</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <!-- Handle Dialog -->
    <el-dialog title="处理举报" v-model="handleDialogVisible" width="450px">
      <el-form :model="handleForm" :rules="handleRules" ref="handleFormRef" label-width="100px">
        <el-form-item label="处理结果" prop="handleResult">
          <el-select v-model="handleForm.handleResult" style="width: 100%">
            <el-option v-for="(v, k) in REPORT_HANDLE_RESULT" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="同时隐藏帖子">
          <el-switch v-model="handleForm.hidePost" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle" :loading="handleLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getReportList, handleReport } from '../../api/community'
import type { PostReport } from '../../api/community'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { showSuccess, showError } from '../../utils/feedback'
import { REPORT_HANDLE_RESULT, REPORT_STATUS } from '../../types/status'
import type { ReportHandleResult as ReportHandleResultType, ReportStatus as ReportStatusType } from '../../types/status'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<PostReport[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

// ─── Handle Dialog ───
const handleDialogVisible = ref(false)
const handleLoading = ref(false)
const handleFormRef = ref<FormInstance>()
const handleTargetId = ref(0)
const handleForm = reactive({ handleResult: 'PROCESSED', hidePost: false, handleRemark: '' })
const handleRules: FormRules = { handleResult: [{ required: true, message: '请选择处理结果', trigger: 'change' }] }

const openHandleDialog = (row: PostReport) => {
  handleTargetId.value = row.id
  handleForm.handleResult = 'PROCESSED'
  handleForm.hidePost = false
  handleForm.handleRemark = ''
  handleDialogVisible.value = true
}

const submitHandle = async () => {
  if (!handleFormRef.value) return
  await handleFormRef.value.validate(async (valid) => {
    if (!valid) return
    handleLoading.value = true
    try {
      await handleReport(handleTargetId.value, {
        handleResult: handleForm.handleResult,
        hidePost: handleForm.hidePost,
        handleRemark: handleForm.handleRemark,
      })
      showSuccess('举报已处理')
      handleDialogVisible.value = false
      await fetchData()
    } catch (error) {
      showError(error instanceof Error ? error.message : '操作失败')
    } finally {
      handleLoading.value = false
    }
  })
}

// ─── Data Fetching ───
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getReportList(queryParams)
    if (res.data) { tableData.value = res.data.items; total.value = res.data.total }
  } catch { /* handled */ } finally { loading.value = false }
}

const handlePageChange = (page: number, size: number) => {
  queryParams.page = page
  queryParams.size = size
  fetchData()
}

const handleReset = () => {
  queryParams.status = ''
  queryParams.page = 1
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.pc-community-reports {
  padding: 0;
}

.pc-community-reports__title {
  margin: 0 0 var(--pc-spacing-lg) 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}
</style>
