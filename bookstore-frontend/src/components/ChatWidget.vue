<template>
  <div class="chat-widget">
    <!-- 折叠状态：浮动按钮 -->
    <Transition name="btn-fade">
      <button v-if="!visible" class="chat-toggle-btn" @click="chatStore.open()" title="在线客服">
        <el-icon :size="24"><ChatDotRound /></el-icon>
        <span v-if="hasUnread" class="unread-dot"></span>
      </button>
    </Transition>

    <!-- 展开状态：聊天窗口 -->
    <Transition name="panel-slide">
      <div v-if="visible" class="chat-panel">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="chat-header-left">
            <div class="chat-avatar">
              <el-icon :size="18"><Service /></el-icon>
            </div>
            <div>
              <div class="chat-title">书虫小助手</div>
              <div class="chat-subtitle">在线客服</div>
            </div>
          </div>
          <button class="chat-close-btn" @click="chatStore.close()" title="收起">
            <el-icon :size="16"><Close /></el-icon>
          </button>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesContainer">
          <!-- 欢迎消息 -->
          <div v-if="messages.length === 0" class="welcome-bubble">
            <div class="welcome-icon">
              <el-icon :size="24"><Reading /></el-icon>
            </div>
            <div class="welcome-text">
              您好！我是<strong>书虫小助手</strong>，很高兴为您服务。<br />
              您可以问我图书推荐、下单流程、网站使用等问题，请问有什么可以帮您的？
            </div>
            <div class="asks">
              <button v-for="q in sampleAsks" :key="q" class="ask" @click="ask(q)">{{ q }}</button>
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
            placeholder="输入您的问题"
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
                <el-icon :size="16"><Promotion /></el-icon>
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
import { storeToRefs } from 'pinia'
import { ChatDotRound, Close, Promotion, WarningFilled, Search, Service, Reading } from '@element-plus/icons-vue'
import { useChatStore } from '../stores/chat'
import { getOrCreateSession, streamChat, getSessionMessages } from '../api/chat'

// ---------- 状态 ----------
const chatStore = useChatStore()
const { visible } = storeToRefs(chatStore)
const messages = ref([])
const inputText = ref('')
const isTyping = ref(false)
const toolStatus = ref('')
const errorMsg = ref('')
const sessionId = ref(null)
const hasUnread = ref(false)
const messagesContainer = ref(null)

// ---------- 方法 ----------

/** 建立会话并恢复历史（打开时首次调用） */
async function ensureSession() {
  if (sessionId.value) return
  try {
    sessionId.value = await getOrCreateSession()
    const history = await getSessionMessages(sessionId.value)
    if (history.length > 0) messages.value = history
  } catch {
    errorMsg.value = '连接客服服务失败，请稍后再试'
  }
  await nextTick()
  scrollToBottom()
}

/** 欢迎态的示例问题：点一下直接发问 */
const sampleAsks = [
  '推荐几本值得读的小说',
  '销量榜前三名是谁',
  '《活着》还有库存吗',
]
async function ask(text) {
  if (isTyping.value) return
  inputText.value = text
  await sendMessage()
}

watch(visible, (v) => {
  if (!v) return
  hasUnread.value = false
  errorMsg.value = ''
  ensureSession()
})

/** 发送消息 */
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isTyping.value) return

  inputText.value = ''
  errorMsg.value = ''

  if (!sessionId.value) {
    try {
      sessionId.value = await getOrCreateSession()
    } catch {
      errorMsg.value = '连接客服服务失败，请稍后再试'
      return
    }
  }

  messages.value.push({ role: 'user', content: text })
  await nextTick()
  scrollToBottom()

  messages.value.push({ role: 'assistant', content: '' })
  const aiIndex = messages.value.length - 1
  isTyping.value = true
  toolStatus.value = ''

  await streamChat(sessionId.value, text, {
    onToken(token) {
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
    if (el) el.scrollTop = el.scrollHeight
  })
}

watch(messages, () => scrollToBottom(), { deep: true })
</script>

<style scoped>
/* ===== 浮动按钮：Action Blue 圆形 ===== */
.chat-toggle-btn {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  width: 56px;
  height: 56px;
  background: var(--primary);
  color: var(--on-primary);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition), transform 0.15s ease;
}
.chat-toggle-btn:hover { background: var(--primary-focus); }
.chat-toggle-btn:active { transform: scale(0.95); }
.unread-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--on-primary);
  border: 2px solid var(--primary);
}

/* ===== 聊天面板 ===== */
.chat-panel {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  width: min(380px, calc(100vw - 32px));
  height: min(560px, calc(100vh - 48px));
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-lg);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ===== 头部 ===== */
.chat-header {
  height: 60px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--canvas-parchment);
  border-bottom: 1px solid var(--hairline);
  flex-shrink: 0;
}
.chat-header-left { display: flex; align-items: center; gap: 12px; }
.chat-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--primary);
  color: var(--on-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.chat-title {
  font-family: var(--font-display);
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.374px;
  color: var(--ink);
}
.chat-subtitle {
  font-family: var(--font-text);
  font-size: 12px;
  letter-spacing: -0.12px;
  color: var(--ink-muted-80);
}
.chat-close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(210, 210, 215, 0.64);
  color: var(--ink);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.chat-close-btn:hover { background: rgba(190, 190, 195, 0.72); }

/* ===== 消息区域 ===== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--canvas);
}
.chat-messages::-webkit-scrollbar { width: 6px; }
.chat-messages::-webkit-scrollbar-thumb {
  background: var(--hairline);
  border-radius: var(--radius-pill);
}

/* ===== 欢迎气泡 ===== */
.welcome-bubble { text-align: center; padding: 24px 12px; }
.welcome-icon {
  width: 52px;
  height: 52px;
  margin: 0 auto 14px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  color: var(--on-primary);
}
.welcome-text {
  font-family: var(--font-text);
  font-size: 14px;
  line-height: 1.5;
  letter-spacing: -0.224px;
  color: var(--ink-muted-80);
}
.asks {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}
.ask {
  font-family: var(--font-text);
  font-size: 13px;
  letter-spacing: -0.2px;
  color: var(--ink-muted-80);
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  padding: 6px 12px;
  cursor: pointer;
  transition: border-color var(--transition), color var(--transition);
}
.ask:hover { border-color: var(--primary); color: var(--primary); }
.ask:active { transform: scale(0.95); }

/* ===== 消息气泡 ===== */
.message-bubble { display: flex; max-width: 85%; }
.message-user { align-self: flex-end; }
.message-assistant { align-self: flex-start; }
.message-content {
  padding: 10px 16px;
  border-radius: var(--radius-lg);
  font-family: var(--font-text);
  font-size: 15px;
  line-height: 1.45;
  letter-spacing: -0.24px;
  word-break: break-word;
  white-space: pre-wrap;
}
.message-user .message-content {
  background: var(--primary);
  color: var(--on-primary);
  border-bottom-right-radius: var(--radius-xs);
}
.message-assistant .message-content {
  background: var(--canvas-parchment);
  color: var(--ink);
  border-bottom-left-radius: var(--radius-xs);
}

/* ===== 工具状态 / 正在输入 ===== */
.typing-block { align-self: flex-start; max-width: 85%; }
.tool-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--canvas-parchment);
  border-radius: var(--radius-lg);
  font-family: var(--font-text);
  font-size: 13px;
  letter-spacing: -0.2px;
  color: var(--ink-muted-80);
}
.tool-status .el-icon { animation: spin 1.2s linear infinite; }
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.typing-indicator { display: flex; gap: 5px; padding: 8px 4px; }
.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--ink-muted-48);
  animation: typing-dot 1.4s ease-in-out infinite;
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
  padding: 8px 16px;
  background: var(--ink);
  color: var(--on-dark);
  border-radius: var(--radius-pill);
  font-family: var(--font-text);
  font-size: 13px;
  letter-spacing: -0.2px;
}

/* ===== 输入区 ===== */
.chat-input-area {
  padding: 12px 16px;
  border-top: 1px solid var(--hairline);
  background: var(--canvas);
  flex-shrink: 0;
}
.chat-input :deep(.el-input__wrapper) {
  box-shadow: none;
  background: var(--canvas);
  border: 1px solid var(--hairline);
  border-radius: var(--radius-pill);
  padding: 4px 44px 4px 16px;
  height: 40px;
}
.chat-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none;
  border-color: var(--primary);
}
.send-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: var(--primary);
  color: var(--on-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background var(--transition);
}
.send-btn:hover:not(:disabled) { background: var(--primary-focus); }
.send-btn:disabled {
  background: var(--hairline);
  color: var(--ink-muted-48);
  cursor: not-allowed;
}
.send-btn:focus-visible,
.chat-toggle-btn:focus-visible { outline: 2px solid var(--primary-focus); outline-offset: 2px; }
.chat-close-btn:focus-visible { outline: 2px solid var(--primary-focus); outline-offset: 2px; }

/* ===== 过渡 ===== */
.btn-fade-enter-active, .btn-fade-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.btn-fade-enter-from, .btn-fade-leave-to { opacity: 0; transform: scale(0.9); }
.panel-slide-enter-active { transition: opacity 0.25s ease, transform 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.panel-slide-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.panel-slide-enter-from, .panel-slide-leave-to { opacity: 0; transform: translateY(16px) scale(0.98); }
</style>
