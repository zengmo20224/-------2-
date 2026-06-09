<template>
  <div class="error-container">
    <h2>404 Not Found</h2>
    <p>The page you are looking for does not exist.</p>
    <el-button type="primary" @click="$router.push('/')">Go Home</el-button>
  </div>
</template>

<style scoped>
.error-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>