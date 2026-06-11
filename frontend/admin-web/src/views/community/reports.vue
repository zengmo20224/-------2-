<template>
  <div class="reports-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>举报处理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 160px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="PROCESSED" />
            <el-option label="已忽略" value="IGNORED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="postId" label="帖子ID" width="90" />
        <el-table-column prop="reason" label="举报原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待处理</el-tag>
            <el-tag v-else-if="row.status === 'PROCESSED'" type="success">已处理</el-tag>
            <el-tag v-else-if="row.status === 'IGNORED'" type="info">已忽略</el-tag>
            <el-tag v-else type="info">{{ row.status }}</el-tag>
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
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

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
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { REPORT_HANDLE_RESULT } from '../../types/status'
import type { ReportHandleResult as ReportHandleResultType } from '../../types/status'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<PostReport[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

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
      await handleReport(handleTargetId.value, { handleResult: handleForm.handleResult, hidePost: handleForm.hidePost, handleRemark: handleForm.handleRemark })
      ElMessage.success('处理成功')
      handleDialogVisible.value = false
      await fetchData()
    } catch { /* handled */ } finally { handleLoading.value = false }
  })
}

const fetchData = async () => {
  loading.value = true
  try { const res = await getReportList(queryParams); if (res.data) { tableData.value = res.data.items; total.value = res.data.total } }
  catch { /* handled */ } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.reports-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
