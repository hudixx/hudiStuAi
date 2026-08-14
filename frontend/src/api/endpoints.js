/** 接口元数据（硬编码，对应 more-platform-and-model controllers） */
export const controllers = [
  {
    key: 'platform',
    label: '多平台对话',
    className: 'MorPlatformAndModelController',
    basePath: '/client',
    description: '多模型平台切换 + ChatMemory 会话记忆。支持同步 chat 与流式 stream。',
    apis: [
      {
        key: 'client-chat',
        name: '同步对话',
        method: 'GET',
        path: '/client/chat',
        produces: 'text/plain;charset=UTF-8',
        stream: false,
        description: '一次性返回完整回复内容。',
        params: [
          { name: 'message', type: 'string', required: true, defaultValue: '你好，你是谁', description: '用户输入' },
          {
            name: 'platform',
            type: 'select',
            required: true,
            defaultValue: 'dashscope',
            options: ['dashscope', 'deepseek', 'ollama', 'anthropic'],
            description: '模型平台'
          },
          { name: 'id', type: 'conversationId', required: true, description: '会话 ID（ChatMemory）' }
        ]
      },
      {
        key: 'client-stream',
        name: '流式对话',
        method: 'GET',
        path: '/client/stream',
        produces: 'text/stream;charset=UTF-8',
        stream: true,
        description: 'Flux 流式输出助手回复。',
        params: [
          { name: 'message', type: 'string', required: true, defaultValue: '你好，你是谁', description: '用户输入' },
          {
            name: 'platform',
            type: 'select',
            required: true,
            defaultValue: 'dashscope',
            options: ['dashscope', 'deepseek', 'ollama', 'anthropic'],
            description: '模型平台'
          },
          { name: 'id', type: 'conversationId', required: true, description: '会话 ID（ChatMemory）' }
        ]
      }
    ]
  },
  {
    key: 'clients',
    label: '规划客户端',
    className: 'MoreClientController',
    basePath: '/clients',
    description: '先用 planningChatClient 结构化解析任务（取消/查询/其他），再走业务或 bot 流式回复。',
    apis: [
      {
        key: 'clients-stream',
        name: '规划流式',
        method: 'GET',
        path: '/clients/stream',
        produces: 'text/stream;charset=UTF-8',
        stream: true,
        description: '返回 text stream，可能先输出规划提示，再输出业务/机器人内容。',
        params: [
          { name: 'message', type: 'string', required: true, defaultValue: '你好，你是谁', description: '用户输入' },
          { name: 'id', type: 'conversationId', required: true, description: '会话 ID（ChatMemory）' }
        ]
      }
    ]
  },
  {
    key: 'tools',
    label: '工具调用',
    className: 'ToolsClientController',
    basePath: '/tools',
    description: 'ChatClient + 动态 toolCallbacks，演示 tools 能力。',
    apis: [
      {
        key: 'tools-stream',
        name: '工具流式',
        method: 'GET',
        path: '/tools/stream',
        produces: 'text/stream;charset=UTF-8',
        stream: true,
        description: '带工具回调的流式输出。',
        params: [
          { name: 'message', type: 'string', required: true, defaultValue: '你好，你是谁', description: '用户输入' },
          { name: 'id', type: 'conversationId', required: true, description: '会话 ID（ChatMemory）' }
        ]
      }
    ]
  }
]
