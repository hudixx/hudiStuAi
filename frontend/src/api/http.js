function resolveBaseUrl() {
  const base = import.meta.env.VITE_API_BASE
  if (base === undefined || base === null) return ''
  return String(base).replace(/\/$/, '')
}

export function buildUrl(path, params = {}) {
  const base = resolveBaseUrl()
  const url = new URL(`${base}${path}`, window.location.origin)
  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') return
    url.searchParams.set(key, String(value))
  })
  // 开发期 base 为空时，返回相对路径以走 Vite proxy
  if (!base) {
    return `${url.pathname}${url.search}`
  }
  return url.toString()
}

export async function requestText(path, params = {}, options = {}) {
  const url = buildUrl(path, params)
  const response = await fetch(url, {
    method: 'GET',
    signal: options.signal
  })
  if (!response.ok) {
    const body = await response.text().catch(() => '')
    throw new Error(`HTTP ${response.status}: ${body || response.statusText}`)
  }
  return response.text()
}

/**
 * 读取 text stream，分片回调 onChunk(chunkText, fullText)
 */
export async function requestStream(path, params = {}, options = {}) {
  const url = buildUrl(path, params)
  const response = await fetch(url, {
    method: 'GET',
    signal: options.signal
  })
  if (!response.ok) {
    const body = await response.text().catch(() => '')
    throw new Error(`HTTP ${response.status}: ${body || response.statusText}`)
  }
  if (!response.body) {
    const text = await response.text()
    options.onChunk?.(text, text)
    return text
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let full = ''

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    const chunk = decoder.decode(value, { stream: true })
    if (!chunk) continue
    full += chunk
    options.onChunk?.(chunk, full)
  }
  const tail = decoder.decode()
  if (tail) {
    full += tail
    options.onChunk?.(tail, full)
  }
  return full
}
