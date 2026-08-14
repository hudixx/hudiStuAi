<template>
  <div class="page">
    <header class="hero">
      <div>
        <h1>more-platform-and-model 接口演示</h1>
        <p>
          Vue 3 + Element Plus。按 Controller 分 Tab，每个接口提供说明、参数表单、同步/流式调用结果。
        </p>
      </div>
      <div class="hero-meta">
        <el-tag>开发代理 → localhost:8080</el-tag>
        <el-tag type="warning">会话 id 按 Tab 隔离</el-tag>
      </div>
    </header>

    <el-card shadow="never" class="main-card">
      <el-tabs v-model="active">
        <el-tab-pane
          v-for="item in controllers"
          :key="item.key"
          :label="item.label"
          :name="item.key"
        >
          <ControllerTab :controller="item" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { controllers } from './api/endpoints'
import ControllerTab from './components/ControllerTab.vue'

const active = ref(controllers[0]?.key || 'platform')
</script>

<style scoped>
.page {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px 16px 40px;
}

.hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.hero h1 {
  margin: 0 0 8px;
  font-size: 24px;
}

.hero p {
  margin: 0;
  color: #6b7280;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.main-card {
  border-radius: 14px;
}
</style>
