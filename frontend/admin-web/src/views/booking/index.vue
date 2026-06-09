<template>
  <div class="booking-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
            <el-option v-for="(v, k) in BOOKING_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期">
          <el-input v-model="queryParams.bookingDate" placeholder="YYYY-MM-DD" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="bookingNo" label="预约编号" width="150" />
        <el-table-column prop="serviceMode" label="模式" width="80">
          <template #default="{ row }">
            <el-tag size="small" :type="row.serviceMode === 'STORE' ? 'success' : 'warning'">
              {{ row.serviceMode === 'STORE' ? '到店' : '上门' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="bookingDate" label="日期" width="110" />
        <el-table-column label="时间" width="120">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column prop="contactName" label="联系人" width="90" />
        <el-table-column prop="price" label="价格" width="90">
          <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paymentStatus" label="支付" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="PAYMENT_STATUS[row.paymentStatus as PaymentStatus]?.color || 'info'">
              {{ PAYMENT_STATUS[row.paymentStatus as PaymentStatus]?.label || row.paymentStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="BOOKING_STATUS[row.status as BookingStatusType]?.color || 'info'">
              {{ BOOKING_STATUS[row.status as BookingStatusType]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button size="small" type="success" v-if="row.status === 'PENDING'" @click="handleConfirm(row.id)" :disabled="!userStore.hasPermission('booking:booking:confirm')">确认</el-button>
            <el-button size="small" type="danger" v-if="row.status === 'PENDING'" @click="openRejectDialog(row.id)" :disabled="!userStore.hasPermission('booking:booking:reject')">拒绝</el-button>
            <el-button size="small" type="primary" v-if="row.status === 'CONFIRMED'" @click="handleStart(row.id)" :disabled="!userStore.hasPermission('booking:booking:start')">开始</el-button>
            <el-button size="small" type="success" v-if="row.status === 'IN_PROGRESS'" @click="handleComplete(row.id)" :disabled="!userStore.hasPermission('booking:booking:complete')">完成</el-button>
            <el-button size="small" v-if="['PENDING', 'CONFIRMED'].includes(row.status)" @click="handleCancel(row.id)" :disabled="!userStore.hasPermission('booking:booking:cancel')">取消</el-button>
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

    <!-- Detail Dialog -->
    <el-dialog title="预约详情" v-model="detailVisible" width="550px">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="预约编号">{{ detailData.bookingNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ BOOKING_STATUS[detailData.status as BookingStatusType]?.label || detailData.status }}</el-descriptions-item>
        <el-descriptions-item label="服务模式">{{ detailData.serviceMode }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ detailData.bookingDate }}</el-descriptions-item>
        <el-descriptions-item label="时间段">{{ detailData.startTime }} - {{ detailData.endTime }}</el-descriptions-item>
        <el-descriptions-item label="价格">{{ Number(detailData.price).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="商家备注" :span="2">{{ detailData.merchantRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- Reject Dialog -->
    <el-dialog title="拒绝预约" v-model="rejectVisible" width="400px">
      <el-form ref="rejectFormRef" :model="rejectForm" :rules="rejectRules">
        <el-form-item label="拒绝原因" prop="reason">
          <el-input v-model="rejectForm.reason" type="textarea" :rows="3" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="submitReject" :loading="rejectLoading">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getBookingList, getBookingDetail, confirmBooking, rejectBooking, startBooking, completeBooking, cancelBooking } from '../../api/booking'
import type { Booking } from '../../api/booking'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { BOOKING_STATUS, PAYMENT_STATUS } from '../../types/status'
import type { BookingStatus as BookingStatusType, PaymentStatus } from '../../types/status'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<Booking[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '', bookingDate: '' })

// ─── Detail ───
const detailVisible = ref(false)
const detailData = ref<Booking | null>(null)
const viewDetail = async (id: number) => {
  try {
    const res = await getBookingDetail(id)
    if (res.data) { detailData.value = res.data; detailVisible.value = true }
  } catch { /* handled */ }
}

// ─── Actions ───
const handleConfirm = (id: number) => {
  ElMessageBox.confirm('确定确认此预约吗？', '确认').then(async () => {
    try { await confirmBooking(id); ElMessage.success('已确认'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}
const handleStart = (id: number) => {
  ElMessageBox.confirm('确定开始服务吗？', '确认').then(async () => {
    try { await startBooking(id); ElMessage.success('已开始'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}
const handleComplete = (id: number) => {
  ElMessageBox.confirm('确定完成此预约吗？', '确认').then(async () => {
    try { await completeBooking(id); ElMessage.success('已完成'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}
const handleCancel = (id: number) => {
  ElMessageBox.confirm('确定取消此预约吗？', '警告', { type: 'warning' }).then(async () => {
    try { await cancelBooking(id); ElMessage.success('已取消'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}

// ─── Reject ───
const rejectVisible = ref(false)
const rejectLoading = ref(false)
const rejectFormRef = ref<FormInstance>()
const rejectTargetId = ref(0)
const rejectForm = reactive({ reason: '' })
const rejectRules: FormRules = { reason: [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }] }

const openRejectDialog = (id: number) => { rejectTargetId.value = id; rejectForm.reason = ''; rejectVisible.value = true }
const submitReject = async () => {
  if (!rejectFormRef.value) return
  await rejectFormRef.value.validate(async (valid) => {
    if (!valid) return
    rejectLoading.value = true
    try { await rejectBooking(rejectTargetId.value, { reason: rejectForm.reason }); ElMessage.success('已拒绝'); rejectVisible.value = false; await fetchData() }
    catch { /* handled */ } finally { rejectLoading.value = false }
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getBookingList(queryParams)
    if (res.data) { tableData.value = res.data.items; total.value = res.data.total }
  } catch { /* handled */ } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.booking-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
