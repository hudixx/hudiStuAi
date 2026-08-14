<template>
  <div class="api-panel">
    <div class="api-header">
      <div>
        <h3>{{ api.name }}</h3>
        <div class="meta">
          <el-tag size="small" type="success">{{ api.method }}</el-tag>
          <code>{{ api.path }}</code>
          <el-tag size="small" type="info">{{ api.produces }}</el-tag>
        </div>
        <p class="desc">{{ api.description }}</p>
      </div>
    </div>

    <el-table :data="api.params" size="small" border class="param-table">
      <el-table-column prop="name" label="参数" width="120" />
      <el-table-column prop="type" label="类型" width="120" />
      <el-table-column label="必填" width="70">
        <template #default="{ row }">{{ row.required ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column prop="description" label="说明" />
    </el-table>

    <el-form label-width="90px" class="call-form" @submit.prevent>
      <el-form-item
        v-for="param in editableParams"
        :key="param.name"
        :label="param.name"
      >
        <el-select
          v-if="param.type === 'select'"
          v-model="form[param.name]"
          style="width: 100%"
        >
          <el-option
            v-for="opt in param.options"
            :key="opt"
            :label="opt"
            :value="opt"
          />
        </el-select>
        <el-input
          v-else
          v-model="form[param.name]"
          type="textarea"
          :rows="param.name === 'message' ? 3 : 1"
          :placeholder="param.description"
        />
      </el-form-item>

      <el-form-item label="操作">
        <el-button type="primary" :loading="loading" @click="onSend">发送</el-button>
        <el-button :disabled="!loading" @click="onStop">停止生成</el-button>
        <el-button @click="onClear">清空会话</el-button>
        <el-radio-group v-if="api.stream" v-model="viewMode" class="view-mode">
          <el-radio-button value="chat">聊天气泡</el-radio-button>
          <el-radio-button value="raw">原始流</el-radio-button>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <div v-if="error" class="error-box">{{ error }}</div>

    <div v-if="viewMode === 'chat'" class="chat-box">
      <div v-if="!messages.length" class="empty">暂无消息，填写参数后点击发送。</div>
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        class="chat-row"
        :class="msg.role"
      >
        <div class="bubble">
          <div class="role">{{ msg.role === 'user' ? '用户' : '助手' }}</div>
          <pre>{{ msg.content || (msg.pending ? '…' : '') }}</pre>
        </div>
      </div>
    </div>

    <div v-else class="raw-box">
      <div class="raw-title">原始流 / 响应文本</div>
      <pre>{{ rawText || '（空）' }}</pre>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { requestStream, requestText } from '../api/http'

const props = defineProps({
  api: { type: Object, required: true },
  conversationId: { type: String, required: true }
})

const form = reactive({})
const messages = ref([])
const rawText = ref('')
const error = ref('')
const loading = ref(false)
const viewMode = ref(props.api.stream ? 'chat' : 'chat')
let abortController = null

const editableParams = computed(() =>
  props.api.params.filter((p) => p.type !== 'conversationId')
)

function initForm() {
  props.api.params.forEach((p) => {
    if (p.type === 'conversationId') return
    form[p.name] = p.defaultValue ?? ''
  })
}

initForm()

function buildParams() {
  return { ...form, id: props.conversationId }
}

async function onSend() {
  if (loading.value) return
  const params = buildParams()
  if (!params.message || !String(params.message).trim()) {
    ElMessage.warning('请输入 message')
    return
  }

  error.value = ''
  loading.value = true
  abortController = new AbortController()

  const userText = String(params.message)
  messages.value.push({ role: 'user', content: userText })
  // 使用 reactive，保证流式过程中直接改 content 能触发视图刷新
  const assistant = reactive({ role: 'assistant', content: '', pending: true })
  messages.value.push(assistant)
  rawText.value = ''

  try {
    if (props.api.stream) {
      await requestStream(props.api.path, params, {
        signal: abortController.signal,
        onChunk: (_chunk, full) => {
          assistant.content = full
          rawText.value = full
        }
      })
      assistant.pending = false
    } else {
      const text = await requestText(props.api.path, params, {
        signal: abortController.signal
      })
      assistant.content = text
      assistant.pending = false
      rawText.value = text
    }
  } catch (e) {
    if (e?.name === 'AbortError') {
      assistant.pending = false
      if (!assistant.content) assistant.content = '（已停止）'
      ElMessage.info('已停止生成')
    } else {
      const msg = e?.message || String(e)
      error.value = msg
      assistant.pending = false
      if (!assistant.content) assistant.content = '错误：' + msg
      ElMessage.error(msg)
    }
  } finally {
    loading.value = false
    abortController = null
  }
}

function onStop() {
  if (abortController) {
    abortController.abort()
  }
}

function onClear() {
  onStop()
  messages.value = []
  rawText.value = ''
  error.value = ''
}
</script>

<style scoped>
.api-panel {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
  margin-bottom: 16px;
}

.api-header h3 {
  margin: 0 0 8px;
  font-size: 16px;
}

.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.meta code {
  background: #f3f4f6;
  padding: 2px 8px;
  border-radius: 6px;
}

.desc {
  margin: 0 0 12px;
  color: #6b7280;
}

.param-table {
  margin-bottom: 16px;
}

.call-form {
  margin-top: 8px;
}

.view-mode {
  margin-left: 12px;
}

.error-box {
  margin: 8px 0;
  padding: 10px 12px;
  border-radius: 8px;
  background: #fef2f2;
  color: #b91c1c;
  white-space: pre-wrap;
}

.chat-box {
  margin-top: 8px;
  max-height: 420px;
  overflow: auto;
  padding: 12px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.empty {
  color: #9ca3af;
  text-align: center;
  padding: 24px 0;
}

.chat-row {
  display: flex;
  margin-bottom: 10px;
}

.chat-row.user {
  justify-content: flex-end;
}

.chat-row.assistant {
  justify-content: flex-start;
}

.bubble {
  max-width: min(720px, 90%);
  border-radius: 12px;
  padding: 10px 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.chat-row.user .bubble {
  background: #ecf5ff;
  border-color: #d9ecff;
}

.role {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.bubble pre,
.raw-box pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
}

.raw-box {
  margin-top: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 10px;
  background: #0b1220;
  color: #e5e7eb;
  padding: 12px;
  max-height: 420px;
  overflow: auto;
}

.raw-title {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 8px;
}
</style>
