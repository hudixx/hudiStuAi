<template>
  <div class="controller-tab">
    <el-alert
      :title="controller.className"
      type="info"
      :closable="false"
      show-icon
      class="summary"
    >
      <template #default>
        <div>前缀：<code>{{ controller.basePath }}</code></div>
        <div>{{ controller.description }}</div>
      </template>
    </el-alert>

    <div class="session-bar">
      <span>会话 ID：</span>
      <el-input v-model="conversationId" style="max-width: 360px" />
      <el-button type="primary" plain @click="newSession">新建会话</el-button>
      <el-text type="info" size="small">该 Tab 内接口共用此 id，与其它 Tab 隔离。</el-text>
    </div>

    <ApiPanel
      v-for="api in controller.apis"
      :key="api.key"
      :api="api"
      :conversation-id="conversationId"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import ApiPanel from './ApiPanel.vue'

const props = defineProps({
  controller: { type: Object, required: true }
})

function createId() {
  if (crypto?.randomUUID) return crypto.randomUUID()
  return `sess-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`
}

const conversationId = ref(createId())

function newSession() {
  conversationId.value = createId()
}
</script>

<style scoped>
.controller-tab {
  padding: 4px 0 12px;
}

.summary {
  margin-bottom: 14px;
}

.session-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  margin-bottom: 16px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
}

code {
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
}
</style>
