<template>
  <div class="pc-product-order">
    <h2 class="pc-product-order__title">自提订单管理</h2>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(v, k) in PRODUCT_ORDER_STATUS" :key="k" :label="v.label" :value="k" />
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
      <el-table-column prop="orderNo" label="订单号" width="160" />
      <el-table-column prop="contactName" label="联系人" width="100" />
      <el-table-column prop="contactPhone" label="电话" width="130" />
      <el-table-column prop="totalAmount" label="金额" width="100">
        <template #default="{ row }">{{ Number(row.totalAmount).toFixed(2) }}</template>
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
          <el-tag :type="PRODUCT_ORDER_STATUS[row.status as ProductOrderStatusType]?.color || 'info'">
            {{ PRODUCT_ORDER_STATUS[row.status as ProductOrderStatusType]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
          <el-button size="small" type="success" v-if="getProductOrderActions({ status: row.status, paymentStatus: row.paymentStatus, pickupStatus: row.pickupStatus }).includes('confirm')" @click="handleAction(row.id, 'confirm')" :disabled="!userStore.hasPermission('product:order:confirm')">确认</el-button>
          <el-button size="small" v-if="getProductOrderActions({ status: row.status, paymentStatus: row.paymentStatus, pickupStatus: row.pickupStatus }).includes('confirm-payment')" @click="handleAction(row.id, 'confirm-payment')" :disabled="!userStore.hasPermission('product:order:confirm-payment')">确认支付</el-button>
          <el-button size="small" type="primary" v-if="getProductOrderActions({ status: row.status, paymentStatus: row.paymentStatus, pickupStatus: row.pickupStatus }).includes('ready')" @click="handleAction(row.id, 'ready')" :disabled="!userStore.hasPermission('product:order:ready')">备货完成</el-button>
          <el-button size="small" type="success" v-if="getProductOrderActions({ status: row.status, paymentStatus: row.paymentStatus, pickupStatus: row.pickupStatus }).includes('complete')" @click="handleAction(row.id, 'complete')" :disabled="!userStore.hasPermission('product:order:complete')">完成</el-button>
          <el-button size="small" type="danger" v-if="getProductOrderActions({ status: row.status, paymentStatus: row.paymentStatus, pickupStatus: row.pickupStatus }).includes('cancel')" @click="handleAction(row.id, 'cancel')" :disabled="!userStore.hasPermission('product:order:cancel')">取消</el-button>
          <el-button size="small" type="warning" v-if="getProductOrderActions({ status: row.status, paymentStatus: row.paymentStatus, pickupStatus: row.pickupStatus }).includes('out-of-stock')" @click="handleAction(row.id, 'out-of-stock')" :disabled="!userStore.hasPermission('product:order:cancel')">缺货</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <!-- Detail Drawer -->
    <DetailDrawer
      :visible="detailVisible"
      title="订单详情"
      width="600px"
      @close="handleDetailClose"
    >
      <template v-if="detailData">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ detailData.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ PRODUCT_ORDER_STATUS[detailData.status as ProductOrderStatusType]?.label || detailData.status }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ detailData.contactName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ detailData.contactPhone }}</el-descriptions-item>
          <el-descriptions-item label="总金额">{{ Number(detailData.totalAmount).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="支付状态">{{ PAYMENT_STATUS[detailData.paymentStatus as PaymentStatus]?.label || detailData.paymentStatus }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.remark || '-' }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top: 16px">商品明细</h4>
        <el-table :data="detailData.items" border size="small">
          <el-table-column prop="productName" label="名称" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="totalAmount" label="小计" width="100">
            <template #default="{ row }">{{ Number(row.totalAmount).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </template>
    </DetailDrawer>

    <!-- Confirm Action Dialog -->
    <ActionConfirmDialog
      :visible="confirmDialogVisible"
      :title="confirmDialogTitle"
      :message="confirmDialogMessage"
      :danger="confirmDialogDanger"
      @confirm="executeConfirmedAction"
      @cancel="confirmDialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProductOrderList, getProductOrderDetail, confirmProductOrder, readyProductOrder, confirmPaymentOrder, completeProductOrder, cancelProductOrder, outOfStockProductOrder } from '../../api/product-order'
import type { ProductOrder, ProductOrderDetail } from '../../api/product-order'
import { useUserStore } from '../../store/user'
import { showSuccess, showConflict, showError } from '../../utils/feedback'
import { PRODUCT_ORDER_STATUS, PAYMENT_STATUS, getProductOrderActions } from '../../types/status'
import type { ProductOrderStatus as ProductOrderStatusType, PaymentStatus } from '../../types/status'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'
import DetailDrawer from '../../components/DetailDrawer.vue'
import ActionConfirmDialog from '../../components/ActionConfirmDialog.vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<ProductOrder[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

// ─── Detail Drawer ───
const detailVisible = ref(false)
const detailData = ref<ProductOrderDetail | null>(null)

const viewDetail = async (id: number) => {
  try {
    const res = await getProductOrderDetail(id)
    if (res.data) { detailData.value = res.data; detailVisible.value = true }
  } catch { /* handled by interceptor */ }
}

const handleDetailClose = () => {
  detailVisible.value = false
  detailData.value = null
}

// ─── Confirm Action Dialog ───
const confirmDialogVisible = ref(false)
const confirmDialogTitle = ref('')
const confirmDialogMessage = ref('')
const confirmDialogDanger = ref(false)
const pendingAction = ref<(() => Promise<void>) | null>(null)

const openConfirmDialog = (title: string, message: string, danger: boolean, action: () => Promise<void>) => {
  confirmDialogTitle.value = title
  confirmDialogMessage.value = message
  confirmDialogDanger.value = danger
  pendingAction.value = action
  confirmDialogVisible.value = true
}

const executeConfirmedAction = async () => {
  if (!pendingAction.value) return
  confirmDialogVisible.value = false
  try {
    await pendingAction.value
  } catch (error: unknown) {
    if (error && typeof error === 'object' && 'response' in error) {
      const resp = (error as { response?: { status?: number } }).response
      if (resp?.status === 409) {
        showConflict()
        await fetchData()
        return
      }
    }
    showError(error instanceof Error ? error.message : '操作失败')
  }
}

// ─── Actions ───
const actionMap: Record<string, (id: number, data?: unknown) => Promise<unknown>> = {
  confirm: confirmProductOrder,
  'confirm-payment': confirmPaymentOrder,
  ready: readyProductOrder,
  complete: completeProductOrder,
  cancel: (id: number) => cancelProductOrder(id),
  'out-of-stock': (id: number) => outOfStockProductOrder(id),
}
const actionLabels: Record<string, string> = {
  confirm: '确认订单',
  'confirm-payment': '确认支付',
  ready: '备货完成',
  complete: '完成订单',
  cancel: '取消订单',
  'out-of-stock': '缺货取消',
}

const handleAction = (id: number, action: string) => {
  const label = actionLabels[action] || action
  const danger = action === 'cancel' || action === 'out-of-stock'
  openConfirmDialog(
    label,
    `确定执行「${label}」操作吗？${danger ? '此操作不可恢复。' : ''}`,
    danger,
    async () => {
      const fn = actionMap[action]
      if (!fn) return
      await fn(id)
      showSuccess(`${label}成功`)
      await fetchData()
    },
  )
}

// ─── Data Fetching ───
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductOrderList(queryParams)
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
.pc-product-order {
  padding: 0;
}

.pc-product-order__title {
  margin: 0 0 var(--pc-spacing-lg) 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}
</style>
