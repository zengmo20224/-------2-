<template>
  <div class="store-config-container">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>门店配置</span>
          <el-button type="primary" @click="handleUpdate" :disabled="!userStore.hasPermission('store:config:update')" :loading="submitting">
            保存配置
          </el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="200px">
        <el-form-item label="上门服务半径 (km)" prop="homeServiceRadiusKm">
          <el-input-number v-model="form.homeServiceRadiusKm" :min="0.1" :precision="1" :step="0.5" />
        </el-form-item>

        <el-form-item label="预约可提前天数" prop="bookingAdvanceDays">
          <el-input-number v-model="form.bookingAdvanceDays" :min="1" :max="365" :step="1" />
        </el-form-item>

        <el-form-item label="预约取消时限 (小时)" prop="bookingCancelHours">
          <el-input-number v-model="form.bookingCancelHours" :min="0" :max="168" :step="1" />
        </el-form-item>

        <el-form-item label="时间段长度 (分钟)" prop="timeSlotMinutes">
          <el-select v-model="form.timeSlotMinutes" placeholder="选择时间段">
            <el-option label="15 分钟" :value="15" />
            <el-option label="30 分钟" :value="30" />
            <el-option label="60 分钟" :value="60" />
          </el-select>
        </el-form-item>

        <el-form-item label="预约自动确认" prop="autoConfirmBooking">
          <el-switch v-model="form.autoConfirmBooking" />
        </el-form-item>

        <el-form-item label="内容自动发布" prop="contentAutoPublish">
          <el-switch v-model="form.contentAutoPublish" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStoreConfig, updateStoreConfig } from '../../api/store'
import type { StoreConfigUpdateParams } from '../../api/store'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '../../store/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const submitting = ref(false)

const form = ref<StoreConfigUpdateParams>({
  homeServiceRadiusKm: 5.0,
  bookingAdvanceDays: 7,
  bookingCancelHours: 24,
  timeSlotMinutes: 30,
  autoConfirmBooking: false,
  contentAutoPublish: false,
})

const rules: FormRules = {
  homeServiceRadiusKm: [{ required: true, message: '请输入服务半径', trigger: 'blur' }],
  bookingAdvanceDays: [{ required: true, message: '请输入提前天数', trigger: 'blur' }],
  bookingCancelHours: [{ required: true, message: '请输入取消时限', trigger: 'blur' }],
  timeSlotMinutes: [{ required: true, message: '请选择时间段长度', trigger: 'change' }],
  autoConfirmBooking: [{ required: true, message: '请选择', trigger: 'change' }],
  contentAutoPublish: [{ required: true, message: '请选择', trigger: 'change' }],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStoreConfig()
    if (res.data) {
      form.value = {
        homeServiceRadiusKm: res.data.homeServiceRadiusKm,
        bookingAdvanceDays: res.data.bookingAdvanceDays,
        bookingCancelHours: res.data.bookingCancelHours,
        timeSlotMinutes: res.data.timeSlotMinutes,
        autoConfirmBooking: res.data.autoConfirmBooking,
        contentAutoPublish: res.data.contentAutoPublish,
      }
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
        await updateStoreConfig(form.value)
        ElMessage.success('门店配置已更新')
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
.store-config-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
