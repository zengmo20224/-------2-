<template>
  <div class="pc-service">
    <div class="pc-service__header">
      <h2 class="pc-service__title">服务项目管理</h2>
      <el-button type="primary" @click="openCreateDialog" :disabled="!userStore.hasPermission('service:item:create')">新增服务</el-button>
    </div>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option v-for="(v, k) in SERVICE_STATUS" :key="k" :label="v.label" :value="k" />
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
      <el-table-column prop="name" label="名称" width="160" />
      <el-table-column prop="serviceMode" label="服务模式" width="120">
        <template #default="{ row }">
          <el-tag :type="SERVICE_MODE[row.serviceMode as ServiceMode]?.color || 'info'">
            {{ SERVICE_MODE[row.serviceMode as ServiceMode]?.label || row.serviceMode }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格 (元)" width="100">
        <template #default="{ row }">
          {{ Number(row.price).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column prop="durationMinutes" label="时长 (分钟)" width="110" />
      <el-table-column prop="petType" label="宠物类型" width="90">
        <template #default="{ row }">
          {{ PET_TYPE[row.petType as PetType] || row.petType || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="SERVICE_STATUS[row.status as ServiceStatus]?.color || 'info'">
            {{ SERVICE_STATUS[row.status as ServiceStatus]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)" :disabled="!userStore.hasPermission('service:item:update')">编辑</el-button>
          <el-button
            size="small" type="danger"
            v-if="isServiceOnSale(row.status)"
            @click="handleDisable(row.id)"
            :disabled="!userStore.hasPermission('service:item:disable')"
          >禁用</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <!-- Create/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetDialog">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="服务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入服务名称" />
        </el-form-item>
        <el-form-item label="分类 ID" prop="categoryId">
          <el-input-number v-model="form.categoryId" :min="1" />
        </el-form-item>
        <el-form-item label="服务模式" prop="serviceMode">
          <el-select v-model="form.serviceMode" style="width: 100%">
            <el-option v-for="(v, k) in SERVICE_MODE" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格 (元)" prop="price">
          <el-input-number v-model="form.price" :precision="2" :step="1" :min="0" />
        </el-form-item>
        <el-form-item label="时长 (分钟)" prop="durationMinutes">
          <el-input-number v-model="form.durationMinutes" :step="15" :min="1" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="需要地址" prop="needAddress">
              <el-switch v-model="form.needAddress" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="需要宠物" prop="needPet">
              <el-switch v-model="form.needPet" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="宠物类型" prop="petType">
          <el-select v-model="form.petType" clearable style="width: 100%">
            <el-option v-for="(v, k) in PET_TYPE" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物体型" prop="petSize">
          <el-select v-model="form.petSize" clearable style="width: 100%">
            <el-option v-for="(v, k) in PET_SIZE" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确认</el-button>
      </template>
    </el-dialog>

    <!-- Disable Confirm Dialog -->
    <ActionConfirmDialog
      :visible="disableDialogVisible"
      title="禁用服务项目"
      message="确定要禁用此服务项目吗？禁用后将不再展示给用户。"
      :danger="true"
      @confirm="executeDisable"
      @cancel="disableDialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getServiceItems, createServiceItem, updateServiceItem, disableServiceItem } from '../../api/service'
import type { ServiceItem, ServiceItemCreateParams } from '../../api/service'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { showSuccess, showError } from '../../utils/feedback'
import { SERVICE_MODE, SERVICE_STATUS, PET_TYPE, PET_SIZE, isServiceOnSale } from '../../types/status'
import type { ServiceMode, ServiceStatus, PetType } from '../../types/status'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'
import ActionConfirmDialog from '../../components/ActionConfirmDialog.vue'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<ServiceItem[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

// ─── Dialog ───
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const currentId = ref<number | undefined>(undefined)

const defaultForm: ServiceItemCreateParams = {
  name: '',
  categoryId: 1,
  serviceMode: 'STORE',
  price: 0,
  durationMinutes: 30,
  needAddress: false,
  needPet: true,
  petType: 'ALL',
  petSize: 'ALL',
  description: '',
  sort: 0,
}

const form = ref<ServiceItemCreateParams>({ ...defaultForm })

const rules: FormRules = {
  name: [{ required: true, message: '请输入服务名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  durationMinutes: [{ required: true, message: '请输入时长', trigger: 'blur' }],
  serviceMode: [{ required: true, message: '请选择服务模式', trigger: 'change' }],
  categoryId: [{ required: true, message: '请输入分类ID', trigger: 'blur' }],
  needAddress: [{ required: true, message: '请选择', trigger: 'change' }],
  needPet: [{ required: true, message: '请选择', trigger: 'change' }],
}

// ─── Disable Confirm Dialog ───
const disableDialogVisible = ref(false)
const disableTargetId = ref(0)

const handleDisable = (id: number) => {
  disableTargetId.value = id
  disableDialogVisible.value = true
}

const executeDisable = async () => {
  disableDialogVisible.value = false
  try {
    await disableServiceItem(disableTargetId.value)
    showSuccess('服务项目已禁用')
    await fetchData()
  } catch (error) {
    showError(error instanceof Error ? error.message : '操作失败')
  }
}

// ─── Data Loading ───
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getServiceItems(queryParams)
    if (res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    }
  } catch { /* handled */ } finally {
    loading.value = false
  }
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

// ─── CRUD ───
const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增服务项目'
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

const openEditDialog = (row: ServiceItem) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑服务项目'
  form.value = {
    categoryId: row.categoryId,
    name: row.name,
    serviceMode: row.serviceMode,
    price: row.price,
    durationMinutes: row.durationMinutes,
    petType: row.petType ?? undefined,
    petSize: row.petSize ?? undefined,
    needAddress: row.needAddress,
    needPet: row.needPet,
    description: row.description ?? undefined,
    coverUrl: row.coverUrl ?? undefined,
    sort: row.sort ?? undefined,
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value && currentId.value) {
        await updateServiceItem(currentId.value, form.value)
        showSuccess('服务项目已更新')
      } else {
        await createServiceItem(form.value)
        showSuccess('服务项目已创建')
      }
      dialogVisible.value = false
      await fetchData()
    } catch (error) {
      showError(error instanceof Error ? error.message : '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const resetDialog = () => { formRef.value?.resetFields() }

onMounted(() => { fetchData() })
</script>

<style scoped>
.pc-service {
  padding: 0;
}

.pc-service__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pc-spacing-lg);
}

.pc-service__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}
</style>
