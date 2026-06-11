<template>
  <div class="pc-forbidden">
    <h1 class="pc-forbidden__code">403</h1>
    <h2 class="pc-forbidden__title">无权访问</h2>
    <p class="pc-forbidden__message">您没有权限访问此页面，请联系管理员。</p>
    <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
  </div>
</template>

<style scoped>
.pc-forbidden {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  gap: var(--pc-spacing-md);
}

.pc-forbidden__code {
  margin: 0;
  font-size: 72px;
  font-weight: 700;
  color: var(--pc-primary);
  line-height: 1;
}

.pc-forbidden__title {
  margin: 0;
  font-size: var(--pc-font-size-lg);
  color: var(--pc-ink);
}

.pc-forbidden__message {
  margin: 0;
  color: var(--pc-muted);
  font-size: var(--pc-font-size-base);
}
</style>
