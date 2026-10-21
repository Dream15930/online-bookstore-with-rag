<template>
  <div class="chat-widget">
    <!-- 折叠状态：浮动按钮 -->
    <Transition name="btn-fade">
      <button v-if="!visible" class="chat-toggle-btn" @click="openWidget" title="智能客服">
        <el-icon :size="26"><ChatDotRound /></el-icon>
        <span v-if="hasUnread" class="unread-dot"></span>
      </button>
    </Transition>

    <!-- 展开状态：聊天窗口 -->
    <Transition name="panel-slide">
      <div v-if="visible" class="chat-panel">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="chat-header-left">
            <el-avatar :size="36" icon="UserFilled" class="chat-avatar" />
            <div>
              <div class="chat-title">书虫小助手</div>
              <div class="chat-subtitle">智能客服 · 在线</div>
            </div>
          </div>
          <button class="chat-close-btn" @click="visible = false" title="收起">
            <el-icon :size="18"><Close /></el-icon>
          </button>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-bubble">
            <div class="welcome-icon">📚</div>
            <div class="welcome-text">
              您好！我是<strong>书虫小助手</strong>，很高兴为您服务。<br />
              您可以问我图书推荐、下单流程、网站使用等问题，请问有什么可以帮您的？
            </div>
          </div>

          <div
            v-for="(msg, index) in messages"
            :key="index"
            :class="['message-bubble', msg.role === 'user' ? 'message-user' : 'message-assistant']"
          >
            <div class="message-content">{{ msg.content }}</div>
          </div>

          <!-- 正在输入动画 -->
          <div v-if="isTyping" class="typing-block">
            <div v-if="toolStatus" class="tool-status">
              <el-icon :size="14"><Search /></el-icon>
              <span>{{ toolStatus }}</span>
            </div>
            <div v-else class="typing-indicator">
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
              <span class="typing-dot"></span>
            </div>
          </div>

          <!-- 错误提示 -->
          <div v-if="errorMsg" class="error-bubble">
            <el-icon :size="14"><WarningFilled /></el-icon>
            <span>{{ errorMsg }}</span>
          </div>
        </div>

        <!-- 输入区 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputText"
            placeholder="输入您的问题..."
            :disabled="isTyping"
            maxlength="500"
            @keyup.enter="sendMessage"
            class="chat-input"
          >
            <template #suffix>
              <button
                class="send-btn"
                :disabled="!inputText.trim() || isTyping"
                @click="sendMessage"
                title="发送"
              >
                <el-icon :size="18"><Promotion /></el-icon>
              </button>
            </template>
          </el-input>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { ChatDotRound, Close, Promotion, WarningFilled, Search } from '@element-plus/icons-vue'
import { getOrCreateSession, streamChat, clearSession, getSessionMessages } from '../api/chat'

// ---------- 状态 ----------
const visible = ref(false)
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const toolStatus = ref('')
const errorMsg = ref('')
const sessionId = ref(null)
const hasUnread = ref(false)
const messagesContainer = ref(null)

// ---------- 方法 ----------

/** 打开聊天窗口 */
async function openWidget() {
  visible.value = true
  hasUnread.value = false
  errorMsg.value = ''

  // 首次打开时创建会话并加载历史
  if (!sessionId.value) {
    try {
      sessionId.value = await getOrCreateSession()
      // 尝试恢复历史消息
      const history = await getSessionMessages(sessionId.value)
      if (history.length > 0) {
        messages.value = history
      }
    } catch {
      errorMsg.value = '连接客服服务失败，请稍后再试'
    }
  }

  await nextTick()
  scrollToBottom()
}

/** 发送消息 */
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  inputText.value = ''
  errorMsg.value = ''

  // 确保有会话
  if (!sessionId.value) {
    try {
      sessionId.value = await getOrCreateSession()
    } catch {
      errorMsg.value = '连接客服服务失败，请稍后再试'
      return
    }
  }

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  await nextTick()
  scrollToBottom()

  // 添加 AI 消息占位
  messages.value.push({ role: 'assistant', content: '' })
  const aiIndex = messages.value.length - 1
  isTyping.value = true
  toolStatus.value = ''

  // 流式获取回复
  await streamChat(sessionId.value, text, {
    onToken(token) {
      // 收到第一个 token 时清除 tool_status
      if (toolStatus.value) toolStatus.value = ''
      messages.value[aiIndex].content += token
      scrollToBottom()
    },
    onToolStatus(status) {
      toolStatus.value = status
      scrollToBottom()
    },
    onDone() {
      isTyping.value = false
      toolStatus.value = ''
    },
    onError(err) {
      isTyping.value = false
      toolStatus.value = ''
      // 如果 AI 消息为空，移除占位
      if (!messages.value[aiIndex].content) {
        messages.value.splice(aiIndex, 1)
      }
      errorMsg.value = err || '抱歉，客服助手暂时不可用，请稍后再试。'
      scrollToBottom()
    },
    onFinish() {
      isTyping.value = false
      toolStatus.value = ''
    },
  })
}

/** 滚动到底部 */
function scrollToBottom() {
  nextTick(() => {
    const el = messagesContainer.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

// 监听消息变化自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
/* ===== 浮动按钮 ===== */
.chat-toggle-btn {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  width: 56px;
  height: 56px;
  border: none;
  border-radius: 50%;
  background: var(--primary, #3b82f6);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.4);
  transition: transform var(--transition, 0.25s ease), box-shadow var(--transition, 0.25s ease);
}
.chat-toggle-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 24px rgba(59, 130, 246, 0.55);
}
.chat-toggle-btn:active {
  transform: scale(0.95);
}

.unread-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--danger, #ef4444);
  border: 2px solid #fff;
}

/* ===== 聊天面板 ===== */
.chat-panel {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  width: 380px;
  height: 520px;
  background: var(--white, #fff);
  border-radius: var(--radius, 12px);
  box-shadow: var(--shadow-lg, 0 10px 25px rgba(0,0,0,0.15));
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 头部 ===== */
.chat-header {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  flex-shrink: 0;
}
.chat-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.chat-avatar {
  background: rgba(255,255,255,0.25) !important;
  color: #fff !important;
}
.chat-title {
  font-size: 15px;
  font-weight: 600;
}
.chat-subtitle {
  font-size: 11px;
  opacity: 0.8;
  margin-top: 1px;
}
.chat-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  background: rgba(255,255,255,0.15);
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}
.chat-close-btn:hover {
  background: rgba(255,255,255,0.3);
}

/* ===== 消息区域 ===== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: var(--bg, #f8fafc);
}
.chat-messages::-webkit-scrollbar {
  width: 4px;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

/* ===== 欢迎气泡 ===== */
.welcome-bubble {
  text-align: center;
  padding: 20px 16px;
}
.welcome-icon {
  font-size: 40px;
  margin-bottom: 10px;
}
.welcome-text {
  font-size: 13px;
  color: var(--text-secondary, #6b7280);
  line-height: 1.7;
}
.welcome-text strong {
  color: var(--primary, #3b82f6);
}

/* ===== 消息气泡 ===== */
.message-bubble {
  display: flex;
  max-width: 85%;
}
.message-user {
  align-self: flex-end;
}
.message-assistant {
  align-self: flex-start;
}
.message-content {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13.5px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}
.message-user .message-content {
  background: var(--primary, #3b82f6);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.message-assistant .message-content {
  background: #fff;
  color: var(--text, #1f2937);
  border: 1px solid var(--border, #e5e7eb);
  border-bottom-left-radius: 4px;
}

/* ===== 正在输入/工具状态动画 ===== */
.typing-block {
  align-self: flex-start;
  max-width: 85%;
}
.tool-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 12px;
  font-size: 12.5px;
  color: #3b82f6;
}
.tool-status .el-icon {
  animation: spin 1.2s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typing-dot 1.4s infinite;
}
.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing-dot {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-4px); }
}

/* ===== 错误提示 ===== */
.error-bubble {
  align-self: center;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #fef2f2;
  border-radius: 12px;
  color: #dc2626;
  font-size: 12.5px;
}

/* ===== 输入区 ===== */
.chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--border, #e5e7eb);
  background: #fff;
  flex-shrink: 0;
}
.chat-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding-right: 40px;
}
.send-btn {
  border: none;
  background: var(--primary, #3b82f6);
  color: #fff;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: opacity 0.2s, transform 0.15s;
}
.send-btn:hover:not(:disabled) {
  opacity: 0.9;
}
.send-btn:active:not(:disabled) {
  transform: scale(0.92);
}
.send-btn:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

/* ===== 过渡动画 ===== */
.btn-fade-enter-active, .btn-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.btn-fade-enter-from, .btn-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.panel-slide-enter-active {
  transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.panel-slide-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.panel-slide-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
.panel-slide-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.97);
}
</style>
