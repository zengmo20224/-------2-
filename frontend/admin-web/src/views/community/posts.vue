<template>
  <div class="community-posts-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>帖子管理</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 140px">
            <el-option v-for="(v, k) in POST_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getPostList, approvePost, rejectPost, hidePost, deletePost } from '../../api/community'
import type { Post } from '../../api/community'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../store/user'
import { POST_STATUS } from '../../types/status'
import type { PostStatus as PostStatusType } from '../../types/status'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<Post[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

const fetchData = async () => {
  loading.value = true
  try { const res = await getPostList(queryParams); if (res.data) { tableData.value = res.data.items; total.value = res.data.total } }
  catch { /* handled */ } finally { loading.value = false }
}

const handleApprove = (id: number) => {
  ElMessageBox.confirm('确定通过此帖子吗？', '审核').then(async () => {
    try { await approvePost(id); ElMessage.success('已通过'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}
const handleReject = (id: number) => {
  ElMessageBox.confirm('确定拒绝此帖子吗？', '审核').then(async () => {
    try { await rejectPost(id); ElMessage.success('已拒绝'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}
const handleHide = (id: number) => {
  ElMessageBox.confirm('确定隐藏此帖子吗？', '操作').then(async () => {
    try { await hidePost(id); ElMessage.success('已隐藏'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}
const handleDelete = (id: number) => {
  ElMessageBox.confirm('确定删除此帖子吗？此操作不可恢复。', '警告', { type: 'warning' }).then(async () => {
    try { await deletePost(id); ElMessage.success('已删除'); await fetchData() } catch { /* handled */ }
  }).catch(() => {})
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.community-posts-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
