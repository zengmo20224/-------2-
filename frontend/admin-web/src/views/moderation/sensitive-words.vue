<template>
  <div class="sensitive-words-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>敏感词管理</span>
          <el-button type="primary" @click="openCreateDialog" :disabled="!userStore.hasPermission('community:sensitive-word:manage')">新增敏感词</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams">
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 130px">
            <el-option v-for="(v, k) in SENSITIVE_WORD_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="word" label="敏感词" width="180" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="row.level === 3 ? 'danger' : row.level === 2 ? 'warning' : 'info'" size="small">L{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="SENSITIVE_WORD_STATUS[row.status as SensitiveWordStatusType]?.color || 'info'">
              {{ SENSITIVE_WORD_STATUS[row.status as SensitiveWordStatusType]?.label || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)" :disabled="!userStore.hasPermission('community:sensitive-word:manage')">编辑</el-button>
            <el-button size="small" type="danger" v-if="row.status === 'ACTIVE'" @click="handleDisable(row.id)" :disabled="!userStore.hasPermission('community:sensitive-word:manage')">禁用</el-button>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="400px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="form.word" placeholder="请输入敏感词" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-input v-model="form.category" placeholder="例如：政治、色情、广告" />
        </el-form-item>
        <el-form-item label="级别" prop="level">
          <el-select v-model="form.level" style="width: 100%">
            <el-option label="L1 - 提醒" :value="1" />
            <el-option label="L2 - 替换" :value="2" />
            <el-option label="L3 - 拦截" :value="3" />
          </el-select>
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
import { getSensitiveWords, createSensitiveWord, updateSensitiveWord, disableSensitiveWord } from '../../api/moderation'
import type { SensitiveWord, SensitiveWordCreateParams, SensitiveWordUpdateParams } from '../../api/moderation'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { SENSITIVE_WORD_STATUS } from '../../types/status'
import type { SensitiveWordStatus as SensitiveWordStatusType } from '../../types/status'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<SensitiveWord[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const currentId = ref<number>()
const form = ref<SensitiveWordCreateParams & { level: number }>({ word: '', category: '', level: 1 })

const rules: FormRules = {
  word: [{ required: true, message: '请输入敏感词', trigger: 'blur' }],
  level: [{ required: true, message: '请选择级别', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try { const res = await getSensitiveWords(queryParams); if (res.data) { tableData.value = res.data.items; total.value = res.data.total } }
  catch { /* handled */ } finally { loading.value = false }
}

const openCreateDialog = () => { isEdit.value = false; dialogTitle.value = '新增敏感词'; form.value = { word: '', category: '', level: 1 }; dialogVisible.value = true }
const openEditDialog = (row: SensitiveWord) => { isEdit.value = true; currentId.value = row.id; dialogTitle.value = '编辑敏感词'; form.value = { word: row.word, category: row.category ?? undefined, level: row.level }; dialogVisible.value = true }

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      if (isEdit.value && currentId.value) {
        const updateData: SensitiveWordUpdateParams = { word: form.value.word, category: form.value.category, level: form.value.level }
        await updateSensitiveWord(currentId.value, updateData)
        ElMessage.success('更新成功')
      } else {
        await createSensitiveWord(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false; await fetchData()
    } catch { /* handled */ } finally { submitLoading.value = false }
  })
}

const handleDisable = (id: number) => {
  ElMessageBox.confirm('确定禁用此敏感词吗？', '警告', { type: 'warning' })
    .then(async () => { try { await disableSensitiveWord(id); ElMessage.success('已禁用'); await fetchData() } catch { /* handled */ } }).catch(() => {})
}

const resetForm = () => { formRef.value?.resetFields() }
onMounted(() => { fetchData() })
</script>

<style scoped>
.sensitive-words-container { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>
