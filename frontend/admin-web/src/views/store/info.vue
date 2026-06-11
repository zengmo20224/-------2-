<template>
  <div class="pc-store-info">
    <div class="pc-store-info__header">
      <h2 class="pc-store-info__title">门店信息</h2>
      <el-button type="primary" @click="handleUpdate" :disabled="!userStore.hasPermission('store:info:update')" :loading="submitting">保存修改</el-button>
    </div>

    <div class="pc-store-info__form" v-loading="loading">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="门店名称" prop="storeName">
          <el-input v-model="form.storeName" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="营业时间" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="例如：09:00 - 21:00" />
        </el-form-item>
        <el-form-item label="门店状态" prop="status">
          <el-select v-model="form.status" placeholder="选择状态" style="width: 100%">
            <el-option v-for="(v, k) in STORE_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="请输入详细地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="经度" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="门店描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="门店描述" />
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStoreInfo, updateStoreInfo } from '../../api/store'
import type { StoreUpdateParams } from '../../api/store'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'
import { showSuccess, showError } from '../../utils/feedback'
import { STORE_STATUS } from '../../types/status'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const form = ref<StoreUpdateParams>({
  storeName: '',
  phone: '',
  address: '',
  businessHours: '',
  status: 'OPEN',
  longitude: 0,
  latitude: 0,
  description: '',
})

const rules: FormRules = {
  storeName: [{ required: true, message: '请输入门店名称', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入门店地址', trigger: 'blur' }],
  status: [{ required: true, message: '请选择门店状态', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStoreInfo()
    if (res.data) {
      form.value = {
        storeName: res.data.storeName,
        phone: res.data.phone ?? undefined,
        address: res.data.address ?? undefined,
        longitude: res.data.longitude ?? undefined,
        latitude: res.data.latitude ?? undefined,
        businessHours: res.data.businessHours ?? undefined,
        status: res.data.status,
        description: res.data.description ?? undefined,
      }
    }
  } catch { /* handled */ } finally {
    loading.value = false
  }
}

const handleUpdate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await updateStoreInfo(form.value)
      showSuccess('门店信息已更新')
      await loadData()
    } catch (error) {
      showError(error instanceof Error ? error.message : '操作失败')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.pc-store-info {
  padding: 0;
}

.pc-store-info__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--pc-spacing-lg);
}

.pc-store-info__title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--pc-ink);
}

.pc-store-info__form {
  background: #fff;
  border-radius: var(--pc-radius);
  padding: var(--pc-spacing-xl);
  box-shadow: var(--pc-shadow-sm);
}
</style>
