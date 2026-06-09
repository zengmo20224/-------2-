<template>
  <div class="store-info-container">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>门店信息</span>
          <el-button type="primary" @click="handleUpdate" :disabled="!userStore.hasPermission('store:info:update')" :loading="submitting">
            保存修改
          </el-button>
        </div>
      </template>

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
            <el-option label="营业中" value="OPEN" />
            <el-option label="已休息" value="CLOSED" />
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStoreInfo, updateStoreInfo } from '../../api/store'
import type { StoreUpdateParams } from '../../api/store'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'

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
      form.value = { ...res.data }
    }
  } catch {
    // Error handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleUpdate = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await updateStoreInfo(form.value)
        ElMessage.success('门店信息已更新')
        await loadData()
      } catch {
        // Error handled by interceptor
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.store-info-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
