<template>
  <div class="pc-community-posts">
    <h2 class="pc-community-posts__title">帖子管理</h2>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(v, k) in POST_STATUS" :key="k" :label="v.label" :value="k" />
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
      <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
      <el-table-column prop="content" label="内容" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="POST_STATUS[row.status as PostStatusType]?.color || 'info'">
            {{ POST_STATUS[row.status as PostStatusType]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="70" />
      <el-table-column prop="likeCount" label="点赞" width="70" />
      <el-table-column prop="commentCount" label="评论" width="70" />
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" v-if="row.status === 'PENDING_REVIEW'" type="success" @click="handleApprove(row.id)" :disabled="!userStore.hasPermission('community:post:approve')">通过</el-button>
          <el-button size="small" v-if="row.status === 'PENDING_REVIEW'" type="danger" @click="handleReject(row.id)" :disabled="!userStore.hasPermission('community:post:reject')">拒绝</el-button>
          <el-button size="small" v-if="['PUBLISHED','PENDING_REVIEW'].includes(row.status)" @click="handleHide(row.id)" :disabled="!userStore.hasPermission('community:post:hide')">隐藏</el-button>
          <el-button size="small" type="danger" v-if="row.status !== 'DELETED'" @click="handleDelete(row.id)" :disabled="!userStore.hasPermission('community:post:delete')">删除</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

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
import { getPostList, approvePost, rejectPost, hidePost, deletePost } from '../../api/community'
import type { Post } from '../../api/community'
import { useUserStore } from '../../store/user'
import { showSuccess, showError } from '../../utils/feedback'
import { POST_STATUS } from '../../types/status'
import type { PostStatus as PostStatusType } from '../../types/status'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'
import ActionConfirmDialog from '../../components/ActionConfirmDialog.vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<Post[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

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
  } catch (error) {
    showError(error instanceof Error ? error.message : '操作失败')
  }
}

// ─── Actions ───
const handleApprove = (id: number) => {
  openConfirmDialog('审核通过', '确定通过此帖子吗？', false, async () => {
    await approvePost(id)
    showSuccess('帖子已通过')
    await fetchData()
  })
}

const handleReject = (id: number) => {
  openConfirmDialog('拒绝帖子', '确定拒绝此帖子吗？', true, async () => {
    await rejectPost(id)
    showSuccess('帖子已拒绝')
    await fetchData()
  })
}

const handleHide = (id: number) => {
  openConfirmDialog('隐藏帖子', '确定隐藏此帖子吗？', false, async () => {
    await hidePost(id)
    showSuccess('帖子已隐藏')
    await fetchData()
  })
}

const handleDelete = (id: number) => {
  openConfirmDialog('删除帖子', '确定删除此帖子吗？此操作不可恢复。', true, async () => {
    await deletePost(id)
    showSuccess('帖子已删除')
    await fetchData()
  })
}

// ─── Data Fetching ───
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getPostList(queryParams)
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
.pc-community-posts {
  padding: 0;
}

.pc-community-posts__title {
  margin: 0 0 var(--pc-spacing-lg) 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}
</style>
