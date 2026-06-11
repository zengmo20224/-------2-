<template>
  <div class="pc-product">
    <div class="pc-product__header">
      <h2 class="pc-product__title">商品管理</h2>
      <el-button type="primary" @click="openCreateDialog" :disabled="!userStore.hasPermission('product:item:create')">新增商品</el-button>
    </div>

    <FilterBar @search="fetchData" @reset="handleReset">
      <el-form-item label="状态">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option v-for="(v, k) in PRODUCT_STATUS" :key="k" :label="v.label" :value="k" />
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
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="salesCount" label="销量" width="80" />
      <el-table-column prop="pickupOnly" label="仅自提" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="row.pickupOnly ? 'warning' : 'success'">{{ row.pickupOnly ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="PRODUCT_STATUS[row.status as ProductStatusType]?.color || 'info'">
            {{ PRODUCT_STATUS[row.status as ProductStatusType]?.label || row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEditDialog(row)" :disabled="!userStore.hasPermission('product:item:update')">编辑</el-button>
          <el-button size="small" @click="openStockDialog(row)" :disabled="!userStore.hasPermission('product:stock:update')">库存</el-button>
          <el-button
            size="small" type="danger"
            v-if="isProductOnSale(row.status)"
            @click="handleDisable(row.id)"
            :disabled="!userStore.hasPermission('product:item:disable')"
          >下架</el-button>
        </template>
      </el-table-column>
    </DataTableShell>

    <!-- Create/Edit Dialog -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类ID" prop="categoryId">
          <el-input-number v-model="form.categoryId" :min="1" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0" />
        </el-form-item>
        <el-form-item label="仅自提" prop="pickupOnly">
          <el-switch v-model="form.pickupOnly" />
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

    <!-- Stock Dialog -->
    <el-dialog title="调整库存" v-model="stockDialogVisible" width="350px">
      <el-form :model="stockForm" ref="stockFormRef" label-width="60px">
        <el-form-item label="库存" prop="stock" :rules="[{ required: true, message: '请输入', trigger: 'blur' }]">
          <el-input-number v-model="stockForm.stock" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock" :loading="stockLoading">确认</el-button>
      </template>
    </el-dialog>

    <!-- Disable Confirm Dialog -->
    <ActionConfirmDialog
      :visible="disableDialogVisible"
      title="下架商品"
      message="确定要下架此商品吗？下架后将不再展示给用户。"
      :danger="true"
      @confirm="executeDisable"
      @cancel="disableDialogVisible = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getProductList, createProduct, updateProduct, disableProduct, updateProductStock } from '../../api/product'
import type { Product, ProductCreateParams } from '../../api/product'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { showSuccess, showError } from '../../utils/feedback'
import { PRODUCT_STATUS, isProductOnSale } from '../../types/status'
import type { ProductStatus as ProductStatusType } from '../../types/status'
import FilterBar from '../../components/FilterBar.vue'
import DataTableShell from '../../components/DataTableShell.vue'
import ActionConfirmDialog from '../../components/ActionConfirmDialog.vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref<Product[]>([])
const total = ref(0)
const queryParams = reactive({ page: 1, size: 10, status: '' })

// ─── Dialog ───
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const isEdit = ref(false)
const currentId = ref<number>()

const defaultForm: ProductCreateParams = { categoryId: 1, name: '', price: 0, pickupOnly: true, sort: 0, description: '' }
const form = ref<ProductCreateParams>({ ...defaultForm })
const rules: FormRules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请输入分类ID', trigger: 'blur' }],
  pickupOnly: [{ required: true, message: '请选择', trigger: 'change' }],
}

// ─── Stock ───
const stockDialogVisible = ref(false)
const stockLoading = ref(false)
const stockFormRef = ref<FormInstance>()
const stockTargetId = ref(0)
const stockForm = reactive({ stock: 0 })

const openStockDialog = (row: Product) => {
  stockTargetId.value = row.id
  stockForm.stock = row.stock ?? 0
  stockDialogVisible.value = true
}

const submitStock = async () => {
  if (!stockFormRef.value) return
  await stockFormRef.value.validate(async (valid) => {
    if (!valid) return
    stockLoading.value = true
    try {
      await updateProductStock(stockTargetId.value, { stock: stockForm.stock })
      showSuccess('库存已更新')
      stockDialogVisible.value = false
      await fetchData()
    } catch (error) {
      showError(error instanceof Error ? error.message : '操作失败')
    } finally {
      stockLoading.value = false
    }
  })
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
    await disableProduct(disableTargetId.value)
    showSuccess('商品已下架')
    await fetchData()
  } catch (error) {
    showError(error instanceof Error ? error.message : '操作失败')
  }
}

// ─── Data Loading ───
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getProductList(queryParams)
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

// ─── CRUD ───
const openCreateDialog = () => {
  isEdit.value = false
  dialogTitle.value = '新增商品'
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

const openEditDialog = (row: Product) => {
  isEdit.value = true
  currentId.value = row.id
  dialogTitle.value = '编辑商品'
  form.value = {
    categoryId: row.categoryId,
    name: row.name,
    coverUrl: row.coverUrl ?? undefined,
    price: row.price,
    description: row.description ?? undefined,
    pickupOnly: row.pickupOnly,
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
        await updateProduct(currentId.value, form.value)
        showSuccess('商品已更新')
      } else {
        await createProduct(form.value)
        showSuccess('商品已创建')
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

const resetForm = () => { formRef.value?.resetFields() }

onMounted(() => { fetchData() })
</script>

<style scoped>
.pc-product {
  padding: 0;
}

.pc-product__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pc-spacing-lg);
}

.pc-product__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}
</style>
