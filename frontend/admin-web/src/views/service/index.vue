<template>
  <div class="service-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务项目管理</span>
          <el-button type="primary" @click="openCreateDialog" :disabled="!userStore.hasPermission('service:item:create')">
            新增服务
          </el-button>
        </div>
      </template>

      <!-- Search -->
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="名称">
          <el-input v-model="queryParams.name" placeholder="服务名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 130px">
            <el-option label="启用" value="ON_SALE" />
            <el-option label="已禁用" value="OFF_SALE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- Table -->
      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
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
              size="small"
              type="danger"
              v-if="isServiceOnSale(row.status)"
              @click="handleDisable(row.id)"
              :disabled="!userStore.hasPermission('service:item:disable')"
            >
              禁用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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
            <el-option label="到店" value="STORE" />
            <el-option label="上门" value="HOME" />
            <el-option label="到店/上门" value="BOTH" />
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
            <el-option label="犬类" value="DOG" />
            <el-option label="猫类" value="CAT" />
            <el-option label="不限" value="ALL" />
          </el-select>
        </el-form-item>
        <el-form-item label="宠物体型" prop="petSize">
          <el-select v-model="form.petSize" clearable style="width: 100%">
            <el-option label="小型" value="SMALL" />
            <el-option label="中型" value="MEDIUM" />
            <el-option label="大型" value="LARGE" />
            <el-option label="不限" value="ALL" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getServiceItems, createServiceItem, updateServiceItem, disableServiceItem } from '../../api/service'
import type { ServiceItem, ServiceItemCreateParams } from '../../api/service'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { SERVICE_MODE, SERVICE_STATUS, PET_TYPE, isServiceOnSale } from '../../types/status'
import type { ServiceMode, ServiceStatus, PetType } from '../../types/status'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<ServiceItem[]>([])
const total = ref(0)
const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  status: '',
})

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

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getServiceItems(queryParams)
    if (res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    }
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

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
    petType: row.petType,
    petSize: row.petSize,
    needAddress: row.needAddress,
    needPet: row.needPet,
    description: row.description,
    coverUrl: row.coverUrl,
    sort: row.sort,
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value && currentId.value) {
          await updateServiceItem(currentId.value, form.value)
          ElMessage.success('更新成功')
        } else {
          await createServiceItem(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        await fetchData()
      } catch {
        // Error handled by interceptor
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleDisable = (id: number) => {
  ElMessageBox.confirm('确定要禁用此服务项目吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await disableServiceItem(id)
      ElMessage.success('已禁用')
      await fetchData()
    } catch {
      // Error handled by interceptor
    }
  }).catch(() => {})
}

const resetDialog = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.service-list-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
}
</style>
