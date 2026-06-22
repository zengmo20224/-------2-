<template>
  <div class="pc-user">
    <div class="pc-user__header">
      <h2 class="pc-user__title">用户管理</h2>
    </div>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option v-for="(v, k) in USER_STATUS" :key="k" :label="v.label" :value="k" />
        </el-select>
      </el-form-item>
      <el-form-item label="搜索">
        <el-input v-model="queryParams.keyword" placeholder="手机号/昵称" clearable style="width: 180px" />
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
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="nickname" label="昵称" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="170">
        <template #default="{ row }">
          <el-tag :type="USER_STATUS[row.status as UserStatus]?.color || 'info'">
            {{ USER_STATUS[row.status as UserStatus]?.label || row.status }}
          </el-tag>
          <span v-if="row.status === 'BANNED' && row.banDescription" class="ban-desc">{{ row.banDescription }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="realName" label="实名" width="100" show-overflow-tooltip>
        <template #default="{ row }">{{ row.realName || '-' }}</template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最近登录" width="170">
        <template #default="{ row }">{{ row.lastLoginTime || '从未登录' }}</template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDetail(row)">详情</el-button>
          <el-button
            size="small" type="danger"
            v-if="canBanUser(row.status)"
            @click="openBanDialog(row.id)"
            :disabled="!userStore.hasPermission('user:profile:ban')"
          >封禁</el-button>
          <el-button
            size="small" type="success"
            v-if="canUnbanUser(row.status)"
            @click="handleUnban(row.id)"
            :disabled="!userStore.hasPermission('user:profile:ban')"
          >解封</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <!-- Ban reason dialog -->
    <el-dialog title="封禁用户" v-model="banDialogVisible" width="450px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 16px">
        封禁时长按历史次数自动递进：第1次1天 → 3天 → 7天 → 30天 → 365天 → 永久
      </el-alert>
      <el-form label-width="80px">
        <el-form-item label="封禁原因">
          <el-input v-model="banReason" type="textarea" :rows="3" placeholder="选填，记录封禁原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="banDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="executeBan" :loading="banLoading">确认封禁</el-button>
      </template>
    </el-dialog>

    <!-- User detail drawer (bookings + orders) -->
    <el-drawer v-model="detailVisible" title="用户详情" size="600px">
      <div v-if="detailUser" class="detail-head">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="手机号">{{ detailUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detailUser.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="USER_STATUS[detailUser.status as UserStatus]?.color || 'info'" size="small">
              {{ USER_STATUS[detailUser.status as UserStatus]?.label || detailUser.status }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="封禁">{{ detailUser.banDescription || '-' }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-tabs v-model="detailTab" style="margin-top: 16px">
        <el-tab-pane :label="`服务预约 (${bookingsTotal})`" name="bookings">
          <el-table :data="bookings" size="small" v-loading="historyLoading" empty-text="暂无预约记录">
            <el-table-column prop="serviceItemName" label="服务项目" min-width="120" show-overflow-tooltip />
            <el-table-column prop="bookingDate" label="日期" width="110" />
            <el-table-column label="时间" width="120">
              <template #default="{ row }">{{ row.startTime }}-{{ row.endTime }}</template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="80">
              <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="BOOKING_STATUS[row.status as BookingStatusType]?.color || 'info'">
                  {{ BOOKING_STATUS[row.status as BookingStatusType]?.label || row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane :label="`商品订单 (${ordersTotal})`" name="orders">
          <el-table :data="orders" size="small" v-loading="historyLoading" empty-text="暂无订单记录">
            <el-table-column prop="orderNo" label="订单号" width="170" show-overflow-tooltip />
            <el-table-column label="金额" width="90">
              <template #default="{ row }">{{ Number(row.totalAmount).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column label="配送" width="80">
              <template #default="{ row }">{{ row.deliveryMethod === 'EXPRESS' ? '快递' : '自提' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag size="small" :type="PRODUCT_ORDER_STATUS[row.status as OrderStatusType]?.color || 'info'">
                  {{ PRODUCT_ORDER_STATUS[row.status as OrderStatusType]?.label || row.status }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" width="160" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import {
  getUserList, banUser, unbanUser, getUserBookings, getUserOrders,
} from '../../api/user'
import type { UserView, UserQueryParams, UserBooking, UserOrder } from '../../api/user'
import { useUserStore } from '../../store/user'
import { showConfirm, showSuccess, showError } from '../../utils/feedback'
import {
  USER_STATUS, canBanUser, canUnbanUser, BOOKING_STATUS, PRODUCT_ORDER_STATUS,
} from '../../types/status'
import type { UserStatus, BookingStatus, ProductOrderStatus } from '../../types/status'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'

type BookingStatusType = BookingStatus
type OrderStatusType = ProductOrderStatus

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<UserView[]>([])
const total = ref(0)
const queryParams = reactive<UserQueryParams>({ page: 1, size: 10, status: '', keyword: '' })

const banDialogVisible = ref(false)
const banTargetId = ref(0)
const banReason = ref('')
const banLoading = ref(false)

const detailVisible = ref(false)
const detailUser = ref<UserView | null>(null)
const detailTab = ref('bookings')
const historyLoading = ref(false)
const bookings = ref<UserBooking[]>([])
const bookingsTotal = ref(0)
const orders = ref<UserOrder[]>([])
const ordersTotal = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const params: UserQueryParams = { page: queryParams.page, size: queryParams.size }
    if (queryParams.status) params.status = queryParams.status
    if (queryParams.keyword) params.keyword = queryParams.keyword
    const res = await getUserList(params)
    if (res.data) {
      tableData.value = res.data.items
      total.value = res.data.total
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handlePageChange(page: number, size: number) {
  queryParams.page = page
  queryParams.size = size
  fetchData()
}

function handleReset() {
  queryParams.page = 1
  queryParams.status = ''
  queryParams.keyword = ''
  fetchData()
}

function openBanDialog(id: number) {
  banTargetId.value = id
  banReason.value = ''
  banDialogVisible.value = true
}

async function executeBan() {
  banDialogVisible.value = false
  banLoading.value = true
  try {
    const res = await banUser(banTargetId.value, { reason: banReason.value || undefined })
    const msg = (res.data?.banDays === null || res.data?.banDays === undefined)
      ? `已永久封禁（第${res.data?.banLevel}次）`
      : `已封禁 ${res.data?.banDays} 天（第${res.data?.banLevel}次）`
    showSuccess(msg)
    await fetchData()
  } catch (error) {
    showError(error instanceof Error ? error.message : '操作失败')
  } finally {
    banLoading.value = false
  }
}

function handleUnban(id: number) {
  showConfirm('确定要解封此用户吗？解封后该手机号可重新注册和登录。', '提示', {
    confirmButtonText: '解封',
  })
    .then(async () => {
      try {
        await unbanUser(id)
        showSuccess('用户已解封')
        await fetchData()
      } catch (error) {
        showError(error instanceof Error ? error.message : '操作失败')
      }
    })
    .catch(() => {})
}

async function openDetail(row: UserView) {
  detailUser.value = row
  detailVisible.value = true
  detailTab.value = 'bookings'
  historyLoading.value = true
  try {
    const [bk, od] = await Promise.all([
      getUserBookings(row.id, { page: 1, size: 20 }),
      getUserOrders(row.id, { page: 1, size: 20 }),
    ])
    bookings.value = bk.data?.items ?? []
    bookingsTotal.value = bk.data?.total ?? 0
    orders.value = od.data?.items ?? []
    ordersTotal.value = od.data?.total ?? 0
  } catch {
    // handled by interceptor
  } finally {
    historyLoading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.pc-user__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pc-user__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.ban-desc {
  margin-left: 6px;
  font-size: 12px;
  color: var(--pc-danger, #d94a4a);
}

.detail-head {
  margin-bottom: 8px;
}
</style>
