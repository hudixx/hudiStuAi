# Frontend (Vue 3 + Element Plus)

演示 `more-platform-and-model` 下 Controller 接口的单页：

- 按 Controller 分 Tab
- 每个接口：说明 + 表单 + 结果（聊天气泡 / 原始流）
- 开发代理到 Spring Boot（默认 `http://localhost:8080`）

## 命令

```bash
cd frontend
npm install
npm run dev
```

环境变量：

- `VITE_API_BASE`：请求前缀；开发默认空（走 Vite proxy）
- `VITE_PROXY_TARGET`：代理目标，默认 `http://localhost:8080`
