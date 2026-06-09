<template>
  <div class="store-info-container">
    <el-card class="box-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>Store Information</span>
          <el-button type="primary" @click="handleUpdate" :disabled="!hasPermission('store:info:update')">Save Changes</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="Store Name" prop="storeName">
          <el-input v-model="form.storeName" placeholder="Enter store name" />
        </el-form-item>
        
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="form.phone" placeholder="Enter contact phone" />
        </el-form-item>

        <el-form-item label="Business Hours" prop="businessHours">
          <el-input v-model="form.businessHours" placeholder="e.g. 09:00 - 21:00" />
        </el-form-item>
        
        <el-form-item label="Status" prop="status">
          <el-select v-model="form.status" placeholder="Select Status" style="width: 100%">
            <el-option label="Open" value="open" />
            <el-option label="Closed" value="closed" />
            <el-option label="Preparation" value="preparation" />
          </el-select>
        </el-form-item>

        <el-form-item label="Address" prop="address">
          <el-input v-model="form.address" type="textarea" placeholder="Enter detailed address" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="Longitude" prop="longitude">
              <el-input-number v-model="form.longitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Latitude" prop="latitude">
              <el-input-number v-model="form.latitude" :precision="6" :step="0.000001" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Description" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="Store description" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getStoreInfo, updateStoreInfo, StoreInfo } from '../../api/store';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();
const hasPermission = (perm: string) => {
  return userStore.userInfo?.permissions?.includes(perm) || false;
};

const formRef = ref<FormInstance>();
const loading = ref(false);

const form = ref<Partial<StoreInfo>>({
  storeName: '',
  phone: '',
  address: '',
  businessHours: '',
  status: 'open',
  longitude: 0,
  latitude: 0,
  description: ''
});

const rules: FormRules = {
  storeName: [{ required: true, message: 'Store Name is required', trigger: 'blur' }],
  phone: [{ required: true, message: 'Phone is required', trigger: 'blur' }],
  address: [{ required: true, message: 'Address is required', trigger: 'blur' }],
  status: [{ required: true, message: 'Status is required', trigger: 'change' }]
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getStoreInfo();
    if (res.data) {
      form.value = { ...res.data };
    }
  } catch (error) {
    // Error handled by interceptor
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
        await updateStoreInfo(form.value);
        ElMessage.success('Store info updated successfully');
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
.store-info-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>