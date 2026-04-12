<template>
  <NuxtLayout name="default">
    <div class="chat-full">
      <div class="chat-container">
        <div class="history-panel">
          <div class="history-header">
            <h4>{{ $t('chatHistory') }}</h4>
            <el-button type="primary" text size="small" class="history-new-chat" @click="startNewChat">
              + {{ $t('newChat') }}
            </el-button>
          </div>

          <div class="history-list">
            <div
              v-for="session in chatSessions"
              :key="session.id"
              class="history-item"
              :class="{ active: session.id === activeSessionId }"
              @click="loadSession(session.id)"
            >
              <div class="history-item-title">{{ session.title }}</div>
              <div class="history-item-footer">
                <span class="history-item-time">{{ formatSessionTime(session.updatedAt) }}</span>
                <el-dropdown trigger="click" @command="(action) => handleHistoryAction(action, session.id)">
                  <el-button text class="history-more" @click.stop>...</el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="rename">{{ $t('edit') }}</el-dropdown-item>
                      <el-dropdown-item command="delete">{{ $t('delete') }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </div>

            <p v-if="chatSessions.length === 0" class="history-empty">
              {{ $t('noChatHistory') }}
            </p>
          </div>
        </div>

        <div class="chat-panel">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="chat-header-left">
              <el-icon :size="32" style="color: var(--el-color-primary)"><Service /></el-icon>
              <div class="chat-header-info">
                <h3>{{ $t('aiAssistant') }}</h3>
                <p class="status">{{ $t('buildingInfo') }}</p>
              </div>
            </div>
          </div>

          <!-- Messages -->
          <div class="messages-container" ref="messagesContainer">
            <!-- Welcome Message -->
            <div v-if="messages.length === 0" class="welcome-section">
              <el-icon :size="60" style="color: var(--el-color-primary)"><Service /></el-icon>
              <p class="welcome-text">{{ $t('aiWelcome') }}</p>
              
              <div class="example-questions">
                <h4>{{ $t('exampleQuestions') }}</h4>
                <el-button 
                  v-for="(question, index) in exampleQuestions" 
                  :key="index"
                  text
                  class="example-btn"
                  @click="askQuestion(question)"
                >
                  {{ question }}
                </el-button>
              </div>
            </div>

            <!-- Messages -->
            <template v-else>
              <div 
                v-for="(message, index) in messages" 
                :key="index"
                :class="['message', message.type === 'user' ? 'sent' : 'received']"
              >
                <div v-if="message.type === 'ai'" class="message-avatar">
                  <el-icon :size="32" style="color: var(--el-color-primary)"><Service /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-bubble">{{ message.text }}</div>
                  <span class="message-time">{{ message.time }}</span>
                </div>
              </div>
              
              <!-- Typing indicator -->
              <div v-if="isTyping" class="message received">
                <div class="message-avatar">
                  <el-icon :size="32" style="color: var(--el-color-primary)"><Service /></el-icon>
                </div>
                <div class="message-content">
                  <div class="message-bubble">
                    <div class="typing-indicator">
                      <span></span>
                      <span></span>
                      <span></span>
                    </div>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <!-- Message Input -->
          <div class="message-input-area">
            <el-input
              v-model="userInput"
              type="textarea"
              :autosize="{ minRows: 2, maxRows: 6 }"
              :placeholder="$t('typeQuestion')"
              @keydown.enter.exact.prevent="sendMessage"
            />
            <el-button 
              type="primary" 
              :icon="Promotion" 
              circle 
              size="large" 
              @click="sendMessage"
              :disabled="!userInput.trim()"
            />
          </div>
        </div>
      </div>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted } from 'vue'
import { Service, Promotion } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

type MessageType = 'user' | 'ai'

interface ChatMessage {
  type: MessageType
  text: string
  time: string
}

interface ChatSession {
  id: string
  title: string
  isCustomTitle: boolean
  messages: ChatMessage[]
  updatedAt: string
}

const CHAT_STORAGE_KEY = 'ai-chat-sessions-v1'
const DEFAULT_BUILDING_ID = 'default'

const { t, locale } = useI18n()
const language = computed(() => locale.value as 'en' | 'zh')

const userInput = ref('')
const messages = ref<ChatMessage[]>([])
const isTyping = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)
const chatSessions = ref<ChatSession[]>([])
const activeSessionId = ref('')

const exampleQuestions = computed(() => [
  t('exampleQ1'),
  t('exampleQ2'),
  t('exampleQ3'),
  t('exampleQ4')
])

interface N8nAssistantResponse {
  reply?: unknown
  output?: unknown
  answer?: unknown
}

const toText = (value: unknown): string => {
  if (typeof value === 'string') {
    return value.trim()
  }

  if (value === null || value === undefined) {
    return ''
  }

  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value)
  }

  if (typeof value === 'object') {
    const objectValue = value as Record<string, unknown>
    const nestedCandidates = [objectValue.reply, objectValue.output, objectValue.answer, objectValue.text, objectValue.message]
    for (const candidate of nestedCandidates) {
      const nestedText = toText(candidate)
      if (nestedText) {
        return nestedText
      }
    }
  }

  return ''
}

const getBuildingId = (): string => {
  if (!import.meta.client) return DEFAULT_BUILDING_ID
  return localStorage.getItem('building_id') || DEFAULT_BUILDING_ID
}

const getNoInfoMessage = (): string => {
  return t('aiNoInfoMessage')
}

const getConnectionErrorMessage = (): string => {
  return t('aiConnectionErrorMessage')
}

const getAIResponse = async (question: string): Promise<string> => {
  try {
    const result = await $fetch<N8nAssistantResponse | string>('/api/ai-assistant', {
      method: 'POST',
      body: {
        message: question,
        building_id: getBuildingId(),
        lang: language.value
      }
    })

    if (typeof result === 'string') {
      return result.trim() || getNoInfoMessage()
    }

    const reply = toText(result.reply) || toText(result.output) || toText(result.answer)
    return reply || getNoInfoMessage()
  } catch (error) {
    console.error('n8n assistant request failed', error)
    return getConnectionErrorMessage()
  }
}

const getCurrentTime = (): string => {
  const now = new Date()
  return now.toLocaleTimeString(language.value === 'zh' ? 'zh-HK' : 'en-US', { 
    hour: '2-digit', 
    minute: '2-digit' 
  })
}

const askQuestion = async (question: string) => {
  userInput.value = question
  await sendMessage()
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const getSessionTitle = (sessionMessages: ChatMessage[]): string => {
  const firstUserMessage = sessionMessages.find((message) => message.type === 'user')
  if (!firstUserMessage) {
    return t('newChat')
  }

  const text = firstUserMessage.text.trim()
  if (text.length <= 32) {
    return text
  }

  return `${text.slice(0, 32)}...`
}

const saveChatSessions = () => {
  if (!import.meta.client) return

  localStorage.setItem(CHAT_STORAGE_KEY, JSON.stringify({
    sessions: chatSessions.value,
    activeSessionId: activeSessionId.value
  }))
}

const createSession = (): ChatSession => ({
  id: `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`,
  title: t('newChat'),
  isCustomTitle: false,
  messages: [],
  updatedAt: new Date().toISOString()
})

const syncActiveSession = () => {
  const targetIndex = chatSessions.value.findIndex((session) => session.id === activeSessionId.value)
  if (targetIndex === -1) return

  const clonedMessages = [...messages.value]
  const existingSession = chatSessions.value[targetIndex]
  if (!existingSession) return

  chatSessions.value[targetIndex] = {
    ...existingSession,
    messages: clonedMessages,
    title: existingSession.isCustomTitle ? existingSession.title : getSessionTitle(clonedMessages),
    updatedAt: new Date().toISOString()
  }

  const updatedSession = chatSessions.value[targetIndex]
  if (!updatedSession) return

  chatSessions.value.splice(targetIndex, 1)
  chatSessions.value.unshift(updatedSession)
  saveChatSessions()
}

const startNewChat = () => {
  const session = createSession()
  activeSessionId.value = session.id
  messages.value = []
  isTyping.value = false
  chatSessions.value.unshift(session)
  saveChatSessions()
}

const loadSession = (sessionId: string) => {
  const targetSession = chatSessions.value.find((session) => session.id === sessionId)
  if (!targetSession) return

  activeSessionId.value = targetSession.id
  messages.value = [...targetSession.messages]
  isTyping.value = false
  saveChatSessions()
  scrollToBottom()
}

const handleHistoryAction = (action: string, sessionId: string) => {
  if (action === 'rename') {
    renameSession(sessionId)
    return
  }

  if (action === 'delete') {
    deleteSession(sessionId)
  }
}

const renameSession = async (sessionId: string) => {
  const targetSession = chatSessions.value.find((session) => session.id === sessionId)
  if (!targetSession) return

  try {
    const { value } = await ElMessageBox.prompt(
      t('renameChatPlaceholder'),
      t('renameChatTitle'),
      {
        confirmButtonText: t('save'),
        cancelButtonText: t('cancel'),
        inputValue: targetSession.title,
        inputPattern: /\S+/,
        inputErrorMessage: t('renameChatRequired')
      }
    )

    const newTitle = (value || '').trim()
    if (!newTitle) {
      ElMessage.warning(t('renameChatRequired'))
      return
    }

    const targetIndex = chatSessions.value.findIndex((session) => session.id === sessionId)
    if (targetIndex === -1) return

    const currentSession = chatSessions.value[targetIndex]
    if (!currentSession) return

    chatSessions.value[targetIndex] = {
      ...currentSession,
      title: newTitle,
      isCustomTitle: true,
      updatedAt: new Date().toISOString()
    }

    saveChatSessions()
  } catch {
  }
}

const deleteSession = async (sessionId: string) => {
  const targetIndex = chatSessions.value.findIndex((session) => session.id === sessionId)
  if (targetIndex === -1) return

  try {
    await ElMessageBox.confirm(
      t('deleteChatConfirm'),
      t('deleteChatTitle'),
      {
        confirmButtonText: t('delete'),
        cancelButtonText: t('cancel'),
        type: 'warning'
      }
    )

    const deletingActiveSession = activeSessionId.value === sessionId
    chatSessions.value.splice(targetIndex, 1)

    if (chatSessions.value.length === 0) {
      startNewChat()
      return
    }

    if (deletingActiveSession) {
      const fallbackSession = chatSessions.value[Math.min(targetIndex, chatSessions.value.length - 1)]
      if (!fallbackSession) {
        startNewChat()
        return
      }

      activeSessionId.value = fallbackSession.id
      messages.value = [...fallbackSession.messages]
      isTyping.value = false
      scrollToBottom()
    }

    saveChatSessions()
  } catch {
  }
}

const formatSessionTime = (value: string): string => {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''

  return date.toLocaleString(language.value === 'zh' ? 'zh-HK' : 'en-US', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  if (!import.meta.client) return

  const rawData = localStorage.getItem(CHAT_STORAGE_KEY)
  if (!rawData) {
    startNewChat()
    return
  }

  try {
    const parsed = JSON.parse(rawData) as { sessions?: unknown; activeSessionId?: unknown }
    const sessions = Array.isArray(parsed.sessions) ? parsed.sessions : []

    chatSessions.value = sessions
      .filter((session): session is Partial<ChatSession> & { id: string } => {
        return typeof session === 'object' && session !== null && typeof (session as { id?: unknown }).id === 'string'
      })
      .map((session) => ({
        id: session.id,
        title: typeof session.title === 'string' ? session.title : t('newChat'),
        isCustomTitle: typeof session.isCustomTitle === 'boolean' ? session.isCustomTitle : false,
        messages: Array.isArray(session.messages) ? session.messages : [],
        updatedAt: typeof session.updatedAt === 'string' ? session.updatedAt : new Date().toISOString()
      }))

    if (chatSessions.value.length === 0) {
      startNewChat()
      return
    }

    const storedActiveSessionId = typeof parsed.activeSessionId === 'string' ? parsed.activeSessionId : ''
    const initialSession = chatSessions.value.find((session) => session.id === storedActiveSessionId) || chatSessions.value[0]
    if (!initialSession) {
      startNewChat()
      return
    }

    activeSessionId.value = initialSession.id
    messages.value = [...initialSession.messages]
    scrollToBottom()
  } catch {
    startNewChat()
  }
})

const sendMessage = async () => {
  if (!userInput.value.trim()) return

  if (!activeSessionId.value) {
    startNewChat()
  }

  const question = userInput.value.trim()
  
  // Add user message
  messages.value.push({
    type: 'user',
    text: question,
    time: getCurrentTime()
  })
  syncActiveSession()
  scrollToBottom()
  
  userInput.value = ''
  
  // Show typing indicator
  isTyping.value = true

  const answer = await getAIResponse(question)
  isTyping.value = false

  messages.value.push({
    type: 'ai',
    text: answer,
    time: getCurrentTime()
  })
  syncActiveSession()
  scrollToBottom()
}
</script>

<style scoped>
.chat-full {
  height: 88vh;
  display: flex;
}

.chat-container {
  display: flex;
  width: 100%;
  background: #fff;
}

.history-panel {
  width: 280px;
  border-right: 1px solid #e5e5e5;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow-x: hidden;
}

.history-header {
  padding: 16px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.history-header h4 {
  margin: 0;
  font-size: 20px;
  color: #333;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-new-chat {
  flex-shrink: 0;
  min-width: 0 !important;
}

.history-header :deep(.history-new-chat.el-button) {
  --el-button-size: 34px;
  --el-button-font-size: 16px;
  --el-button-padding-horizontal: 12px;
  min-width: 0 !important;
  width: auto !important;
  height: 34px !important;
  line-height: 30px !important;
  padding: 0 12px !important;
}

.history-new-chat :deep(span) {
  display: inline-block;
  max-width: none;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-list {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px;
}

.history-item {
  width: 100% !important;
  box-sizing: border-box;
  margin: 0 0 8px !important;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  padding: 7px 8px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
  cursor: pointer;
}

.history-item:hover {
  border-color: var(--el-color-primary);
}

.history-item-title {
  width: 100%;
  font-size: 15px;
  color: #333;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item-time {
  font-size: 13px;
  color: #999;
}

.history-item-footer {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
}

.history-more {
  padding: 0 4px;
  min-width: 18px;
  height: 18px;
  font-size: 16px;
  line-height: 1;
}

.history-item.active {
  border-color: var(--el-color-primary);
  background: color-mix(in srgb, var(--el-color-primary) 8%, #fff);
}

.history-empty {
  margin: 16px 8px;
  color: #999;
  font-size: 14px;
}

/* Chat Panel */
.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
}

/* Chat Header */
.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
}

.chat-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chat-header-info h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.status {
  margin: 0;
  font-size: 16px;
  color: #999;
}

/* Messages */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: calc(100vh - 250px);
  min-height: 300px;
}

.welcome-section {
  text-align: center;
  padding: 40px 20px;
  margin: auto;
}

.welcome-text {
  font-size: 18px;
  color: #666;
  line-height: 1.6;
  margin: 20px 0 30px;
}

.example-questions {
  max-width: 600px;
  margin: 0 auto;
}

.example-questions h4 {
  font-size: 24px;
  margin-bottom: 20px;
  color: #333;
  font-weight: 600;
  text-align: center;
}

.example-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  width: 100% !important;
  text-align: left;
  padding: 16px 20px !important;
  margin: 0 0 12px 0 !important;
  border: 2px solid #e0e0e0 !important;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 500;
  transition: all 0.3s;
  box-sizing: border-box;
}

.example-btn:first-child {
  margin-left: 0 !important;
}

.example-btn :deep(span) {
  width: 100%;
  display: block;
}

.example-btn:hover {
  background-color: #f5f7fa;
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.message {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 8px;
}

.message.sent {
  justify-content: flex-end;
}

.message.sent .message-content {
  align-items: flex-end;
}

.message.received .message-avatar {
  flex-shrink: 0;
}

.message-avatar {
  display: flex;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message.received .message-content {
  align-items: flex-start;
}

.message.sent .message-content {
  align-items: flex-end;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 12px;
  border-radius: 18px;
  font-size: 18px;
  word-wrap: break-word;
  line-height: 1.4;
}

.message.received .message-bubble {
  background-color: #f0f0f0;
  color: #333;
}

.message.sent .message-bubble {
  background-color: var(--el-color-primary);
  color: #fff;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #999;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.7;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.message-time {
  font-size: 16px;
  color: #999;
  padding: 0 4px;
}

/* Message Input Area */
.message-input-area {
  padding: 16px 20px;
  border-top: 1px solid #e5e5e5;
  display: flex;
  gap: 12px;
  align-items: flex-end;
  background: #fff;
}

.message-input-area :deep(.el-textarea) {
  flex: 1;
}

.message-input-area :deep(.el-textarea__inner) {
  font-size: 20px;
  line-height: 1.5;
  padding: 16px 18px;
  min-height: 64px !important;
}

.message-input-area :deep(.el-button) {
  width: 50px;
  height: 50px;
}

/* Dark mode styles */
html.dark .chat-container {
  background-color: #1a1a1a;
}

html.dark .chat-panel {
  background-color: #1a1a1a;
}

html.dark .history-panel {
  background-color: #1a1a1a;
  border-right-color: #404040;
}

html.dark .history-header {
  border-bottom-color: #404040;
}

html.dark .history-header h4 {
  color: #e5e7eb;
}

html.dark .history-item {
  border-color: #404040;
}

html.dark .history-item-title {
  color: #e5e7eb;
}

html.dark .history-item-time,
html.dark .history-empty {
  color: #9ca3af;
}

html.dark .history-item.active {
  background: color-mix(in srgb, var(--el-color-primary) 20%, #1a1a1a);
}

html.dark .chat-header {
  background-color: #1a1a1a;
  border-bottom-color: #404040;
}

html.dark .chat-header-info h3 {
  color: #e5e7eb;
}

html.dark .status {
  color: #9ca3af;
}

html.dark .welcome-text {
  color: #9ca3af;
}

html.dark .example-questions h4 {
  color: #e5e7eb;
}

html.dark .example-btn {
  border-color: #404040;
  color: #d1d5db;
}

html.dark .example-btn:hover {
  background-color: #262727;
  border-color: var(--el-color-primary);
}

html.dark .message.received .message-bubble {
  background-color: #262727;
  color: #e5e7eb;
}

html.dark .message-input-area {
  border-top-color: #404040;
  background-color: #1a1a1a;
}
</style>
