<template>
  <div class="store-config-container">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>Store Configuration</span>
          <el-button type="primary" @click="handleUpdate" :disabled="!hasPermission('store:config:update')">Save Config</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="200px">
        <el-form-item label="Home Service Radius (km)" prop="homeServiceRadiusKm">
          <el-input-number v-model="form.homeServiceRadiusKm" :min="0" :precision="1" :step="0.5" />
        </el-form-item>
        
        <el-form-item label="Booking Advance Days" prop="bookingAdvanceDays">
          <el-input-number v-model="form.bookingAdvanceDays" :min="0" :max="30" :step="1" />
        </el-form-item>

        <el-form-item label="Booking Cancel Hours" prop="bookingCancelHours">
          <el-input-number v-model="form.bookingCancelHours" :min="0" :max="72" :step="1" />
        </el-form-item>

        <el-form-item label="Time Slot (Minutes)" prop="timeSlotMinutes">
          <el-select v-model="form.timeSlotMinutes" placeholder="Select Slot Duration">
            <el-option label="15 Minutes" :value="15" />
            <el-option label="30 Minutes" :value="30" />
            <el-option label="60 Minutes" :value="60" />
          </el-select>
        </el-form-item>

        <el-form-item label="Auto Confirm Booking" prop="autoConfirmBooking">
          <el-switch
            v-model="form.autoConfirmBooking"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>

        <el-form-item label="Auto Publish Content" prop="contentAutoPublish">
          <el-switch
            v-model="form.contentAutoPublish"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getStoreConfig, updateStoreConfig, StoreConfig } from '../../api/store';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const hasPermission = (perm: string) => {
  return userStore.userInfo?.permissions?.includes(perm) || false;
};

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = ref<Partial<StoreConfig>>({
  homeServiceRadiusKm: 5.0,
  bookingAdvanceDays: 7,
  bookingCancelHours: 24,
  timeSlotMinutes: 30,
  autoConfirmBooking: 1,
  contentAutoPublish: 1
});

const rules: FormRules = {
  homeServiceRadiusKm: [{ required: true, message: 'Radius is required', trigger: 'blur' }],
  bookingAdvanceDays: [{ required: true, message: 'Advance Days is required', trigger: 'blur' }],
  bookingCancelHours: [{ required: true, message: 'Cancel Hours is required', trigger: 'blur' }],
  timeSlotMinutes: [{ required: true, message: 'Time Slot is required', trigger: 'change' }]
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getStoreConfig();
    if (res.data) {
      form.value = { ...res.data };
    }
  } catch (error) {
    // Error handled
  } finally {
    loading.value = false;
  }
};

const handleUpdate = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await updateStoreConfig(form.value);
        ElMessage.success('Store config updated successfully');
        await loadData();
      } catch (error) {
        // Error handled
      } finally {
        loading.value = false;
      }
    }
  });
};

onMounted(() => {
  loadData();
});
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