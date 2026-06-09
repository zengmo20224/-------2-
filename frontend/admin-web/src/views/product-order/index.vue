<template>
  <div class="product-order-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>自提订单管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
            <el-option v-for="(v, k) in PRODUCT_ORDER_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
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
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row.id)">详情</el-button>
            <el-button size="small" type="success" v-if="getProductOrderActions(row.status).includes('confirm')" @click="handleAction(row.id, 'confirm')" :disabled="!userStore.hasPermission('product:order:confirm')">确认</el-button>
            <el-button size="small" v-if="getProductOrderActions(row.status).includes('confirm-payment')" @click="handleAction(row.id, 'confirm-payment')" :disabled="!userStore.hasPermission('product:order:confirm-payment')">确认支付</el-button>
            <el-button size="small" type="primary" v-if="getProductOrderActions(row.status).includes('ready')" @click="handleAction(row.id, 'ready')" :disabled="!userStore.hasPermission('product:order:ready')">备货完成</el-button>
            <el-button size="small" type="success" v-if="getProductOrderActions(row.status).includes('complete')" @click="handleAction(row.id, 'complete')" :disabled="!userStore.hasPermission('product:order:complete')">完成</el-button>
            <el-button size="small" type="danger" v-if="getProductOrderActions(row.status).includes('cancel')" @click="handleAction(row.id, 'cancel')" :disabled="!userStore.hasPermission('product:order:cancel')">取消</el-button>
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
    <el-dialog title="订单详情" v-model="detailVisible" width="600px">
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
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProductOrderList, getProductOrderDetail, confirmProductOrder, readyProductOrder, confirmPaymentOrder, completeProductOrder, cancelProductOrder } from '../../api/product-order'
import type { ProductOrder, ProductOrderDetail } from '../../api/product-order'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store/user'
import { PRODUCT_ORDER_STATUS, PAYMENT_STATUS, getProductOrderActions } from '../../types/status'
import type { ProductOrderStatus as ProductOrderStatusType, PaymentStatus } from '../../types/status'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<ProductOrder[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

const detailVisible = ref(false)
const detailData = ref<ProductOrderDetail | null>(null)

const viewDetail = async (id: number) => {
  try { const res = await getProductOrderDetail(id); if (res.data) { detailData.value = res.data; detailVisible.value = true } } catch { /* handled */ }
}

const actionMap: Record<string, (id: number) => Promise<unknown>> = {
  confirm: confirmProductOrder,
  'confirm-payment': confirmPaymentOrder,
  ready: readyProductOrder,
  complete: completeProductOrder,
  cancel: cancelProductOrder,
}
const actionLabels: Record<string, string> = { confirm: '确认', 'confirm-payment': '确认支付', ready: '备货完成', complete: '完成', cancel: '取消' }

const handleAction = (id: number, action: string) => {
  const label = actionLabels[action] || action
  ElMessageBox.confirm(`确定执行「${label}」操作吗？`, action === 'cancel' ? '警告' : '确认', { type: action === 'cancel' ? 'warning' : undefined })
    .then(async () => {
      const fn = actionMap[action]
      if (!fn) return
      try { await fn(id); ElMessage.success(`${label}成功`); await fetchData() } catch { /* handled */ }
    }).catch(() => {})
}

const fetchData = async () => {
  loading.value = true
  try { const res = await getProductOrderList(queryParams); if (res.data) { tableData.value = res.data.items; total.value = res.data.total } }
  catch { /* handled */ } finally { loading.value = false }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.product-order-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
