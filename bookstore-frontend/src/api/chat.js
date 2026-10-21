/**
 * 智能客服 聊天 API 模块
 * 使用原生 fetch + ReadableStream 解析 SSE 流式响应
 * 不使用 Axios，避免与现有拦截器的 Result<T> 格式冲突
 */

const SESSION_KEY = 'chat_session_id'

/**
 * 创建新的聊天会话
 * @returns {Promise<string>} session_id
 */
export async function createSession() {
  const resp = await fetch('/api/chat/session')
  if (!resp.ok) throw new Error('创建会话失败')
  const data = await resp.json()
  localStorage.setItem(SESSION_KEY, data.session_id)
  return data.session_id
}

/**
 * 获取或创建会话 ID（优先复用 localStorage 中的）
 * @returns {Promise<string>} session_id
 */
export async function getOrCreateSession() {
  const existing = localStorage.getItem(SESSION_KEY)
  if (existing) return existing
  return await createSession()
}

/**
 * 清除本地会话（用户主动重置）
 */
export function clearSession() {
  localStorage.removeItem(SESSION_KEY)
}

/**
 * 流式聊天
 * @param {string} sessionId - 会话 ID
 * @param {string} message - 用户消息
 * @param {object} callbacks - 回调函数集合
 * @param {function} callbacks.onToken - 收到一个 token
 * @param {function} callbacks.onDone - 回复完成
 * @param {function} callbacks.onError - 发生错误
 * @param {function} callbacks.onStart - 连接建立
 * @param {function} callbacks.onFinish - 最终清理（无论成功/失败）
 * @returns {Promise<void>}
 */
export async function streamChat(sessionId, message, callbacks) {
  const { onToken, onDone, onError, onStart, onFinish, onToolStatus } = callbacks

  try {
    const resp = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session_id: sessionId, message }),
    })

    if (!resp.ok) {
      const errText = await resp.text()
      throw new Error(errText || '请求失败')
    }

    if (onStart) onStart()

    const reader = resp.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })

      // 按 SSE 事件分隔符 \n\n 拆分
      const parts = buffer.split('\n\n')
      // 最后一个可能不完整，保留到下次循环
      buffer = parts.pop() || ''

      for (const part of parts) {
        const trimmed = part.trim()
        if (!trimmed) continue

        // 匹配 "data: {json}"
        const match = trimmed.match(/^data:\s*(.+)$/)
        if (!match) continue

        try {
          const event = JSON.parse(match[1])
          if (event.token && onToken) {
            onToken(event.token)
          } else if (event.tool_status && onToolStatus) {
            onToolStatus(event.tool_status)
          } else if (event.done && onDone) {
            onDone()
          } else if (event.error && onError) {
            onError(event.error)
          }
        } catch {
          // 忽略无法解析的事件
        }
      }
    }

    // 处理缓冲区剩余内容
    if (buffer.trim()) {
      const match = buffer.trim().match(/^data:\s*(.+)$/)
      if (match) {
        try {
          const event = JSON.parse(match[1])
          if (event.done && onDone) onDone()
          if (event.error && onError) onError(event.error)
        } catch { /* ignore */ }
      }
    }
  } catch (err) {
    if (onError) onError(err.message || '网络连接失败')
  } finally {
    if (onFinish) onFinish()
  }
}

/**
 * 获取会话历史消息
 * @param {string} sessionId - 会话 ID
 * @returns {Promise<Array>} 消息列表
 */
export async function getSessionMessages(sessionId) {
  try {
    const resp = await fetch('/api/chat/messages', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ session_id: sessionId }),
    })
    if (!resp.ok) return []
    const data = await resp.json()
    return data.messages || []
  } catch {
    return []
  }
}
