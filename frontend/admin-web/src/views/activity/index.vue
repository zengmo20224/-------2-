<template>
  <div class="pc-activity">
    <div class="pc-activity__header">
      <h2 class="pc-activity__title">活动管理</h2>
      <el-button
        type="primary"
        :disabled="!canManage"
        @click="openCreateDialog"
      >
        新建活动
      </el-button>
    </div>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(item, key) in ACTIVITY_STATUS" :key="key" :label="item.label" :value="key" />
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
      <el-table-column label="封面" width="96">
        <template #default="{ row }">
          <el-image
            v-if="row.coverUrl"
            class="pc-activity__cover"
            :src="row.coverUrl"
            fit="cover"
          />
          <span v-else class="pc-activity__cover-empty">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="活动标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="activityType" label="类型" width="100">
        <template #default="{ row }">{{ ACTIVITY_TYPE[row.activityType as ActivityType] || row.activityType }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="ACTIVITY_STATUS[row.status as ActivityStatus]?.color || 'info'">
            {{ ACTIVITY_STATUS[row.status as ActivityStatus]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="活动时间" min-width="230">
        <template #default="{ row }">{{ formatRange(row.startTime, row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :disabled="!canManage" @click="openEditDialog(row)">编辑</el-button>
          <el-button
            size="small"
            type="success"
            :disabled="!canManage || row.status === 'ACTIVE'"
            @click="handleStatus(row.id, 'ACTIVE')"
          >
            上线
          </el-button>
          <el-button
            size="small"
            type="warning"
            :disabled="!canManage || row.status === 'ENDED'"
            @click="handleStatus(row.id, 'ENDED')"
          >
            结束
          </el-button>
          <el-button size="small" type="danger" :disabled="!canManage" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="720px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入活动标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="活动类型" prop="activityType">
          <el-select v-model="form.activityType" style="width: 100%">
            <el-option v-for="(label, key) in ACTIVITY_TYPE" :key="key" :label="label" :value="key" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图" prop="coverUrl">
          <div class="pc-activity__cover-row">
            <el-input v-model="form.coverUrl" placeholder="活动封面 URL，如 /uploads/images/activity.jpg" />
            <el-upload
              v-model:file-list="coverUploadFileList"
              action=""
              accept="image/jpeg,image/png,image/gif,image/webp"
              :show-file-list="false"
              :http-request="uploadCoverImage"
              :before-upload="beforeUploadCoverImage"
            >
              <el-button type="primary">导入封面图</el-button>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填表示立即开始"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="不填表示长期有效"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="关联商品" prop="productIds">
          <el-select v-model="form.productIds" multiple filterable clearable placeholder="选择参与活动的商品" style="width: 100%">
            <el-option v-for="item in productOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联服务" prop="serviceItemIds">
          <el-select v-model="form.serviceItemIds" multiple filterable clearable placeholder="选择参与活动的服务" style="width: 100%">
            <el-option v-for="item in serviceOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="展示给用户看的活动说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="!canManage" @click="submitForm">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import type { FormInstance, FormRules, UploadRawFile, UploadRequestOptions, UploadUserFile } from 'element-plus'
import {
  createActivity,
  deleteActivity,
  getActivityDetail,
  getActivityList,
  updateActivity,
  updateActivityStatus,
} from '../../api/activity'
import type { Activity, ActivityId, ActivityUpsertParams } from '../../api/activity'
import { getProductList } from '../../api/product'
import type { Product } from '../../api/product'
import { getServiceItems } from '../../api/service'
import type { ServiceItem } from '../../api/service'
import { uploadCatalogImage } from '../../api/upload'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'
import { useUserStore } from '../../store/user'
import { showConfirm, showError, showSuccess } from '../../utils/feedback'

const MAX_UPLOAD_SIZE = 10 * 1024 * 1024
const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']

const ACTIVITY_STATUS = {
  DRAFT: { label: '草稿', color: 'info' },
  ACTIVE: { label: '已上线', color: 'success' },
  ENDED: { label: '已结束', color: 'warning' },
  CANCELLED: { label: '已取消', color: 'danger' },
} as const
type ActivityStatus = keyof typeof ACTIVITY_STATUS

const ACTIVITY_TYPE = {
  SERVICE: '服务活动',
  PRODUCT: '商品活动',
  COMMUNITY: '社区活动',
  MIXED: '综合活动',
} as const
type ActivityType = keyof typeof ACTIVITY_TYPE

const userStore = useUserStore()
const canManage = computed(() => userStore.hasPermission('marketing:activity:manage'))
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<Activity[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })
const productOptions = ref<Product[]>([])
const serviceOptions = ref<ServiceItem[]>([])
const coverUploadFileList = ref<UploadUserFile[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const currentId = ref<ActivityId>()
const formRef = ref<FormInstance>()

const createDefaultForm = (): ActivityUpsertParams => ({
  title: '',
  activityType: 'MIXED',
  description: '',
  coverUrl: '',
  startTime: null,
  endTime: null,
  productIds: [],
  serviceItemIds: [],
})

const form = ref<ActivityUpsertParams>(createDefaultForm())
const rules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  activityType: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: queryParams.page, size: queryParams.size, status: queryParams.status || undefined }
    const res = await getActivityList(params)
    if (res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    }
  } finally {
    loading.value = false
  }
}

async function fetchSelectOptions() {
  const [productRes, serviceRes] = await Promise.all([
    getProductList({ page: 1, size: 100, status: 'ON_SALE' }),
    getServiceItems({ page: 1, size: 100, status: 'ON_SALE' }),
  ])
  productOptions.value = productRes.data?.items ?? []
  serviceOptions.value = serviceRes.data?.items ?? []
}

function handlePageChange(page: number, size: number) {
  queryParams.page = page
  queryParams.size = size
  fetchData()
}

function handleReset() {
  queryParams.page = 1
  queryParams.status = ''
  fetchData()
}

async function openCreateDialog() {
  if (!canManage.value) return
  isEdit.value = false
  currentId.value = undefined
  dialogTitle.value = '新建活动'
  form.value = createDefaultForm()
  await fetchSelectOptions()
  dialogVisible.value = true
}

async function openEditDialog(row: Activity) {
  if (!canManage.value) return
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑活动'
  await fetchSelectOptions()
  const res = await getActivityDetail(row.id)
  if (!res.data) return
  form.value = {
    title: res.data.title,
    activityType: res.data.activityType,
    description: res.data.description ?? '',
    coverUrl: res.data.coverUrl ?? '',
    startTime: res.data.startTime,
    endTime: res.data.endTime,
    productIds: [...res.data.productIds],
    serviceItemIds: [...res.data.serviceItemIds],
  }
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.resetFields()
  form.value = createDefaultForm()
  coverUploadFileList.value = []
}

async function submitForm() {
  if (!canManage.value) return
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const payload: ActivityUpsertParams = {
        ...form.value,
        productIds: [...form.value.productIds],
        serviceItemIds: [...form.value.serviceItemIds],
      }
      if (isEdit.value && currentId.value != null) {
        await updateActivity(currentId.value, payload)
        showSuccess('活动已更新')
      } else {
        await createActivity(payload)
        showSuccess('活动已创建，默认保存为草稿')
      }
      dialogVisible.value = false
      fetchData()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleStatus(id: ActivityId, status: ActivityStatus) {
  if (!canManage.value) return
  const label = ACTIVITY_STATUS[status].label
  showConfirm(`确定要将活动状态改为「${label}」吗？`, '调整活动状态')
    .then(async () => {
      await updateActivityStatus(id, status)
      showSuccess('活动状态已更新')
      fetchData()
    })
    .catch(() => {})
}

function handleDelete(id: ActivityId) {
  if (!canManage.value) return
  showConfirm('确定要删除这个活动吗？删除后用户端将不再显示。', '删除活动', {
    confirmButtonText: '删除',
  })
    .then(async () => {
      await deleteActivity(id)
      showSuccess('活动已删除')
      fetchData()
    })
    .catch(() => {})
}

function beforeUploadCoverImage(file: UploadRawFile) {
  if (!IMAGE_TYPES.includes(file.type)) {
    showError('只支持 JPG/PNG/GIF/WebP 格式')
    return false
  }
  if (file.size > MAX_UPLOAD_SIZE) {
    showError('单张图片不能超过 10MB')
    return false
  }
  return true
}

async function uploadCoverImage(options: UploadRequestOptions) {
  const res = await uploadCatalogImage(options.file)
  const url = res.data?.url
  if (url) {
    form.value = { ...form.value, coverUrl: url }
    showSuccess('封面图已导入')
  }
}

function formatRange(start: string | null, end: string | null) {
  const startText = formatDateTime(start) || '立即开始'
  const endText = formatDateTime(end) || '长期有效'
  return `${startText} 至 ${endText}`
}

function formatDateTime(value: string | null) {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(fetchData)
</script>

<style scoped>
.pc-activity__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.pc-activity__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--pc-ink);
}

.pc-activity__cover {
  width: 64px;
  height: 44px;
  border-radius: 8px;
  background: var(--pc-surface);
}

.pc-activity__cover-empty {
  color: var(--pc-muted);
  font-size: 12px;
}

.pc-activity__cover-row {
  display: flex;
  width: 100%;
  gap: 10px;
}

.pc-activity__cover-row .el-input {
  flex: 1;
}
</style>
