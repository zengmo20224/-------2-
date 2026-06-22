<template>
  <div class="pc-announcement">
    <div class="pc-announcement__header">
      <h2 class="pc-announcement__title">公告管理</h2>
      <el-button type="primary" @click="openCreateDialog" :disabled="!userStore.hasPermission('system:config')">发布公告</el-button>
    </div>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option v-for="(v, k) in ANNOUNCEMENT_STATUS" :key="k" :label="v.label" :value="k" />
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
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="ANNOUNCEMENT_STATUS[row.status as AnnouncementStatus]?.color || 'info'">
            {{ ANNOUNCEMENT_STATUS[row.status as AnnouncementStatus]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)" :disabled="!userStore.hasPermission('system:config')">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)" :disabled="!userStore.hasPermission('system:config')">删除</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <!-- Create/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="公告标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="支持纯文本，换行会保留" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option v-for="(v, k) in ANNOUNCEMENT_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
          <span class="form-hint">数字越小越靠前</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getAnnouncementList,
  createAnnouncement,
  updateAnnouncement,
  deleteAnnouncement,
} from '../../api/announcement'
import type { Announcement, AnnouncementCreateParams, AnnouncementQueryParams } from '../../api/announcement'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { showConfirm, showSuccess } from '../../utils/feedback'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'

// Announcement status dictionary
const ANNOUNCEMENT_STATUS = {
  PUBLISHED: { label: '已发布', color: 'success' },
  DRAFT: { label: '草稿', color: 'info' },
} as const
type AnnouncementStatus = keyof typeof ANNOUNCEMENT_STATUS

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<Announcement[]>([])
const total = ref(0)
const queryParams = reactive<AnnouncementQueryParams>({ page: 1, size: 10, status: '' })

// ─── Dialog ───
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const currentId = ref<number>()

const defaultForm: AnnouncementCreateParams = {
  title: '',
  content: '',
  status: 'PUBLISHED',
  sort: 0,
}
const form = ref<AnnouncementCreateParams>({ ...defaultForm })
const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: AnnouncementQueryParams = { page: queryParams.page, size: queryParams.size }
    if (queryParams.status) params.status = queryParams.status
    const res = await getAnnouncementList(params)
    if (res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    }
  } catch (e) {
    // error toast handled by interceptor
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number) {
  queryParams.page = page
  fetchData()
}

function handleReset() {
  queryParams.page = 1
  queryParams.status = ''
  fetchData()
}

function openCreateDialog() {
  isEdit.value = false
  dialogTitle.value = '发布公告'
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function openEditDialog(row: Announcement) {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑公告'
  form.value = {
    title: row.title,
    content: row.content,
    status: row.status,
    sort: row.sort,
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.value = { ...defaultForm }
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload: AnnouncementCreateParams = {
        title: form.value.title,
        content: form.value.content,
        status: form.value.status,
        sort: form.value.sort,
      }
      if (isEdit.value && currentId.value) {
        await updateAnnouncement(currentId.value, payload)
        showSuccess('公告已更新')
      } else {
        await createAnnouncement(payload)
        showSuccess('公告已发布')
      }
      dialogVisible.value = false
      fetchData()
    } catch (e) {
      // error toast handled by interceptor
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDelete(id: number) {
  showConfirm('确定要删除此公告吗？删除后用户端将不再显示。', '提示', {
    confirmButtonText: '删除',
  })
    .then(async () => {
      try {
        await deleteAnnouncement(id)
        showSuccess('公告已删除')
        fetchData()
      } catch (e) {
        // handled by interceptor
      }
    })
    .catch(() => {})
}

onMounted(fetchData)
</script>

<style scoped>
.pc-announcement__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pc-announcement__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.form-hint {
  margin-left: 10px;
  color: var(--pc-text-secondary, #909399);
  font-size: 12px;
}
</style>
