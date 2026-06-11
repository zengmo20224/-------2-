<template>
  <div class="staff-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>员工管理</span>
          <el-button type="primary" @click="openCreateDialog" :disabled="!userStore.hasPermission('staff:profile:create')">新增员工</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 130px">
            <el-option v-for="(v, k) in STAFF_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="STAFF_ROLE[row.role as StaffRole]?.color || 'info'">
              {{ STAFF_ROLE[row.role as StaffRole]?.label || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="STAFF_STATUS[row.status as StaffStatusType]?.color || 'info'">
              {{ STAFF_STATUS[row.status as StaffStatusType]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)" :disabled="!userStore.hasPermission('staff:profile:update')">编辑</el-button>
            <el-button size="small" @click="openScheduleDialog(row)" :disabled="!userStore.hasPermission('staff:schedule:read')">排班</el-button>
            <el-button
              size="small" type="danger"
              v-if="canDisableStaff(row.status)"
              @click="handleDisable(row.id)"
              :disabled="!userStore.hasPermission('staff:profile:disable')"
            >禁用</el-button>
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

    <!-- Create/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="员工姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="(v, k) in STAFF_ROLE" :key="k" :label="v.label" :value="k" />
          </el-select>
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

    <!-- Schedule Dialog -->
    <el-dialog :title="`排班管理 - ${scheduleStaffName}`" v-model="scheduleDialogVisible" width="700px">
      <el-button type="primary" size="small" @click="openScheduleForm" :disabled="!userStore.hasPermission('staff:schedule:manage')" style="margin-bottom: 12px">新增排班</el-button>
      <el-table :data="scheduleData" border v-loading="scheduleLoading" size="small">
        <el-table-column prop="workDate" label="日期" width="120" />
        <el-table-column prop="startTime" label="开始" width="80" />
        <el-table-column prop="endTime" label="结束" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="SCHEDULE_STATUS[row.status as ScheduleStatus]?.color || 'info'" size="small">
              {{ SCHEDULE_STATUS[row.status as ScheduleStatus]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      </el-table>
      <!-- Inline schedule form -->
      <el-form v-if="showScheduleForm" :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="80px" style="margin-top: 16px; border-top: 1px solid #eee; padding-top: 16px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="日期" prop="workDate">
              <el-input v-model="scheduleForm.workDate" placeholder="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="开始" prop="startTime">
              <el-input v-model="scheduleForm.startTime" placeholder="HH:mm" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="结束" prop="endTime">
              <el-input v-model="scheduleForm.endTime" placeholder="HH:mm" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-select v-model="scheduleForm.status" style="width: 200px">
            <el-option v-for="(v, k) in SCHEDULE_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitSchedule" :loading="scheduleSubmitting">保存</el-button>
          <el-button @click="showScheduleForm = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getStaffList, createStaff, updateStaff, disableStaff, getStaffSchedules, createStaffSchedule } from '../../api/staff'
import type { StaffMember, StaffCreateParams, StaffSchedule, StaffScheduleCreateParams } from '../../api/staff'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { STAFF_ROLE, STAFF_STATUS, SCHEDULE_STATUS, canDisableStaff } from '../../types/status'
import type { StaffRole, StaffStatus as StaffStatusType, ScheduleStatus } from '../../types/status'

const userStore = useUserStore()
const STORE_ID = 1

const loading = ref(false)
const tableData = ref<StaffMember[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

// ─── Staff Form ───
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const currentId = ref<number | undefined>()

const defaultStaffForm: StaffCreateParams = {
  storeId: STORE_ID,
  name: '',
  phone: '',
  role: 'GROOMER',
  description: '',
}
const form = ref<StaffCreateParams>({ ...defaultStaffForm })

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  storeId: [{ required: true, message: '门店ID必填', trigger: 'blur' }],
}

// ─── Schedule Dialog ───
const scheduleDialogVisible = ref(false)
const scheduleStaffId = ref(0)
const scheduleStaffName = ref('')
const scheduleData = ref<StaffSchedule[]>([])
const scheduleLoading = ref(false)
const showScheduleForm = ref(false)
const scheduleFormRef = ref<FormInstance>()
const scheduleSubmitting = ref(false)
const scheduleForm = ref<StaffScheduleCreateParams>({
  storeId: STORE_ID,
  workDate: '',
  startTime: '',
  endTime: '',
  status: 'AVAILABLE',
  remark: '',
})

const scheduleRules: FormRules = {
  workDate: [{ required: true, message: '请输入日期', trigger: 'blur' }],
  startTime: [{ required: true, message: '请输入开始时间', trigger: 'blur' }],
  endTime: [{ required: true, message: '请输入结束时间', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  storeId: [{ required: true, message: '门店ID必填', trigger: 'blur' }],
}

// ─── Data Loading ───
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getStaffList(queryParams)
    if (res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    }
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

// ─── Staff CRUD ───
const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增员工'
  form.value = { ...defaultStaffForm }
  dialogVisible.value = true
}

const openEditDialog = (row: StaffMember) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑员工'
  form.value = { storeId: row.storeId, name: row.name, phone: row.phone ?? undefined, role: row.role, description: row.description ?? undefined }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value && currentId.value) {
        await updateStaff(currentId.value, form.value)
        ElMessage.success('更新成功')
      } else {
        await createStaff(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await fetchData()
    } catch { /* handled */ } finally {
      submitLoading.value = false
    }
  })
}

const handleDisable = (id: number) => {
  ElMessageBox.confirm('确定要禁用此员工吗？', '警告', { type: 'warning' })
    .then(async () => {
      try { await disableStaff(id); ElMessage.success('已禁用'); await fetchData() } catch { /* handled */ }
    }).catch(() => {})
}

const resetForm = () => { formRef.value?.resetFields() }

// ─── Schedule ───
const openScheduleDialog = async (row: StaffMember) => {
  scheduleStaffId.value = row.id
  scheduleStaffName.value = row.name
  showScheduleForm.value = false
  scheduleDialogVisible.value = true
  await loadSchedules()
}

const loadSchedules = async () => {
  scheduleLoading.value = true
  try {
    const res = await getStaffSchedules(scheduleStaffId.value, { page: 1, size: 50 })
    if (res.data) { scheduleData.value = res.data.items }
  } catch { /* handled */ } finally { scheduleLoading.value = false }
}

const openScheduleForm = () => {
  scheduleForm.value = { storeId: STORE_ID, workDate: '', startTime: '', endTime: '', status: 'AVAILABLE', remark: '' }
  showScheduleForm.value = true
}

const submitSchedule = async () => {
  if (!scheduleFormRef.value) return
  await scheduleFormRef.value.validate(async (valid) => {
    if (!valid) return
    scheduleSubmitting.value = true
    try {
      await createStaffSchedule(scheduleStaffId.value, scheduleForm.value)
      ElMessage.success('排班已添加')
      showScheduleForm.value = false
      await loadSchedules()
    } catch { /* handled */ } finally { scheduleSubmitting.value = false }
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.staff-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
