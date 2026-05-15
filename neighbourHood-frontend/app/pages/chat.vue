<template>
  <NuxtLayout name="default">
    <div class="chat-page-shell">
      <div class="chat-aura aura-left"></div>
      <div class="chat-aura aura-right"></div>
      <div class="chat-pattern"></div>
    <div class="chat-full">
      <div class="chat-container">
      <!-- Conversations List -->
      <div class="conversations-panel">
        <div class="search-bar">
          <div class="search-actions">
            <el-button type="primary" plain @click="showCreateGroupDialog = true">
              {{ $t('createGroupChat') }}
            </el-button>
          </div>
          <el-input
            v-model="searchConversation"
            :placeholder="$t('search')"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <div class="conversations-list">
          <div
            v-for="conversation in filteredConversations"
            :key="conversation.id"
            :class="['conversation-item', { active: selectedConversationId === conversation.id }]"
            @click="selectConversation(conversation)"
          >
            <el-avatar :size="48" :src="conversation.avatar" />
            <div class="conversation-info">
              <div class="conversation-header">
                <h4 class="conversation-name">{{ conversation.name }}</h4>
                <span class="conversation-time">{{ conversation.lastMessageTime }}</span>
              </div>
              <p class="conversation-preview">{{ conversation.lastMessage }}</p>
            </div>
            <el-dropdown trigger="click" @command="(cmd) => handleConversationCommand(cmd, conversation)">
              <el-button text :icon="MoreFilled" circle class="conversation-menu-btn" @click.stop />
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="rename">{{ $t('renameChatTitle') }}</el-dropdown-item>
                  <el-dropdown-item command="delete">{{ $t('deleteChatTitle') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-badge :value="conversation.unread" v-if="conversation.unread > 0" />
          </div>
        </div>
      </div>

      <!-- Chat Area -->
      <div class="chat-panel">
        <template v-if="selectedConversation">
          <!-- Chat Header -->
          <div class="chat-header">
            <div class="chat-header-left">
              <el-avatar :size="40" :src="selectedConversation.avatar" />
              <div class="chat-header-info">
                <h3>{{ selectedConversation.name }}</h3>
                <p class="status">{{ $t('activeNow') }}</p>
              </div>
            </div>
            <div class="chat-header-right">
              <el-button
                text
                :type="isConversationBlocked(selectedConversation) ? 'danger' : undefined"
                @click="toggleConversationBlock(selectedConversation)"
              >
                {{ isConversationBlocked(selectedConversation) ? $t('unblock') : $t('blockUser') }}
              </el-button>
              <el-button text :icon="Phone" circle />
              <el-button text :icon="VideoCamera" circle />
              <el-button text :icon="MoreFilled" circle />
            </div>
          </div>

          <!-- Messages -->
          <div class="messages-container" ref="messagesContainer">
            <div v-if="showConversationWelcomeHint" class="new-chat-welcome">
              {{ newChatWelcomeText }}
            </div>
            <div
              v-for="message in selectedConversation.messages"
              :key="message.id"
              :class="['message', message.sender === 'user' ? 'sent' : 'received']"
            >
              <div v-if="message.sender === 'other'" class="message-avatar">
                <el-avatar :size="32" :src="selectedConversation.avatar" />
              </div>
              <div class="message-content">
                <!-- Normal Message Bubble -->
                <div v-if="!message.type || message.type === 'normal'" class="message-bubble">
                  {{ message.text }}
                </div>
                
                <!-- Photo Message -->
                <div v-else-if="message.type === 'photo'" class="message-bubble file-message">
                  <div class="file-preview-container">
                    <el-image
                      :src="message.fileUrl"
                      :alt="message.fileName"
                      fit="cover"
                      class="message-image"
                      :preview-src-list="message.fileUrl ? [message.fileUrl] : []"
                    />
                  </div>
                  <div class="file-info">
                    <span class="file-name">{{ message.fileName }}</span>
                    <a :href="message.fileUrl" :download="message.fileName" class="file-download">
                      <el-icon><Download /></el-icon>
                      {{ $t('download') }}
                    </a>
                  </div>
                </div>
                
                <!-- Video Message -->
                <div v-else-if="message.type === 'video'" class="message-bubble file-message">
                  <div class="file-preview-container">
                    <video :src="message.fileUrl" controls class="message-video"></video>
                  </div>
                  <div class="file-info">
                    <span class="file-name">{{ message.fileName }}</span>
                    <a :href="message.fileUrl" :download="message.fileName" class="file-download">
                      <el-icon><Download /></el-icon>
                      {{ $t('download') }}
                    </a>
                  </div>
                </div>
                
                <!-- Document Message -->
                <div v-else-if="message.type === 'document'" class="message-bubble file-message document-message">
                  <div class="document-icon">
                    <el-icon :size="40"><Document /></el-icon>
                  </div>
                  <div class="file-info">
                    <span class="file-name">{{ message.fileName }}</span>
                    <span class="file-size" v-if="message.fileSize">{{ formatFileSize(message.fileSize) }}</span>
                  </div>
                  <a :href="message.fileUrl" :download="message.fileName" class="file-download-btn">
                    <el-icon><Download /></el-icon>
                    {{ $t('download') }}
                  </a>
                </div>
                
                <!-- Audio Message -->
                <div v-else-if="message.type === 'audio'" class="message-bubble file-message">
                  <div class="audio-container">
                    <audio :src="message.fileUrl" controls class="message-audio"></audio>
                  </div>
                  <div class="file-info">
                    <span class="file-name">{{ message.fileName }}</span>
                    <a :href="message.fileUrl" :download="message.fileName" class="file-download">
                      <el-icon><Download /></el-icon>
                      {{ $t('download') }}
                    </a>
                  </div>
                </div>
                
                <!-- Location Message -->
                <div v-else-if="message.type === 'location'" class="message-bubble location-message">
                  <div class="location-header">
                    <el-icon :size="20"><Location /></el-icon>
                    <span>{{ $t('sharedLocation') }}</span>
                  </div>
                  <div class="location-map-container">
                    <div :id="`map-${message.id}`" class="leaflet-map"></div>
                  </div>
                  <div class="location-info">
                    <span class="location-coords">{{ message.latitude?.toFixed(6) }}, {{ message.longitude?.toFixed(6) }}</span>
                    <a :href="`https://www.openstreetmap.org/?mlat=${message.latitude}&mlon=${message.longitude}#map=17/${message.latitude}/${message.longitude}`" target="_blank" class="location-link">
                      <el-icon><MapLocation /></el-icon>
                      {{ $t('openInMaps') }}
                    </a>
                  </div>
                </div>
                
                <!-- Quest Introduction Message Bubble -->
                <div 
                  v-else-if="message.type === 'quest-introduction'" 
                  class="message-bubble quest-intro-bubble clickable-quest"
                  @click="openQuestDetail(message)"
                >
                  <div class="quest-intro-header">
                    <el-icon><Memo /></el-icon>
                    <span>{{ $t('missionApplication') }}</span>
                  </div>
                  <el-divider style="margin: 8px 0;" />
                  <div class="quest-intro-mission">
                    <strong>{{ $t('mission') }}</strong>
                    <p class="mission-title">{{ message.questInfo?.title }}</p>
                    <div class="mission-tags">
                      <el-tag type="warning" size="small">
                        {{ message.questInfo?.rewardPoints }} {{ $t('points') }}
                      </el-tag>
                      <el-tag 
                        :type="message.questInfo?.paymentMethod === 'face-to-face' ? 'success' : 'info'" 
                        size="small"
                      >
                        {{ message.questInfo?.paymentMethod === 'face-to-face' ? 
                           $t('faceToFace') : 
                           $t('online') }}
                      </el-tag>
                    </div>
                  </div>
                  <el-divider style="margin: 8px 0;" />
                  <div class="quest-intro-text">
                    <strong>{{ $t('introduction') }}</strong>
                    <p>{{ message.text }}</p>
                  </div>
                  <div class="click-to-view">
                    <el-icon><View /></el-icon>
                    <span>{{ $t('clickToViewDetails') }}</span>
                  </div>
                </div>
                
                <span class="message-time">{{ message.time }}</span>
              </div>
            </div>
          </div>

          <!-- Message Input -->
          <div class="message-input-area">
            <input 
              ref="fileInputRef" 
              type="file" 
              style="display: none;" 
              @change="handleFileSelect"
              :accept="fileAcceptType"
            />
            <el-input
              v-model="messageInput"
              :placeholder="$t('typeMessage')"
              :disabled="isConversationBlocked(selectedConversation)"
              @keyup.enter="sendMessage"
            />
            <div class="composer-actions">
              <el-button text :icon="IceCreamRound" circle size="large" />
              <el-dropdown trigger="click" @command="handleAttachmentCommand">
                <el-button text :icon="Plus" circle size="large" />
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="photo">
                      <el-icon><Picture /></el-icon>
                      <span style="margin-left: 8px;">{{ $t('photo') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="video">
                      <el-icon><VideoPlay /></el-icon>
                      <span style="margin-left: 8px;">{{ $t('video') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="document">
                      <el-icon><Document /></el-icon>
                      <span style="margin-left: 8px;">{{ $t('document') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="audio">
                      <el-icon><Headset /></el-icon>
                      <span style="margin-left: 8px;">{{ $t('audio') }}</span>
                    </el-dropdown-item>
                    <el-dropdown-item command="location">
                      <el-icon><Location /></el-icon>
                      <span style="margin-left: 8px;">{{ $t('location') }}</span>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button type="primary" :icon="Promotion" circle size="large" :disabled="isConversationBlocked(selectedConversation)" @click="sendMessage" />
            </div>
          </div>
        </template>
        <template v-else>
          <div class="empty-chat">
            <el-icon><ChatDotRound /></el-icon>
            <p>{{ $t('selectConversation') }}</p>
          </div>
        </template>
      </div>
    </div>
    </div>
    </div>
  </NuxtLayout>

  <!-- Quest Detail Dialog -->
  <QuestDetailDialog
    v-model="showQuestDetailDialog"
    :quest="selectedQuestDetail"
    :language="language"
    :can-accept="false"
    :accept-button-tooltip="$t('questAlreadyAccepted')"
  />

  <el-dialog v-model="showCreateGroupDialog" :title="$t('createGroupChat')" width="560px">
    <el-form label-position="top">
      <el-form-item :label="$t('groupChatName')">
        <el-input v-model="groupChatName" :placeholder="$t('groupChatNamePlaceholder')" />
      </el-form-item>
      <el-form-item :label="$t('groupMembers')">
        <el-select v-model="selectedGroupMembers" multiple filterable :placeholder="$t('selectGroupMembers')" style="width: 100%">
          <el-option
            v-for="option in availableGroupMembers"
            :key="option.id"
            :label="option.name"
            :value="option.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showCreateGroupDialog = false">{{ $t('cancel') }}</el-button>
      <el-button type="primary" :disabled="!canCreateGroupConversation" @click="createGroupConversation">{{ $t('createGroupChat') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showRenameDialog" :title="$t('renameChatTitle')" width="460px" align-center>
    <el-input v-model="renameValue" :placeholder="$t('renameChatPlaceholder')" maxlength="60" />
    <template #footer>
      <el-button @click="showRenameDialog = false">{{ $t('cancel') }}</el-button>
      <el-button type="primary" @click="confirmRenameConversation">{{ $t('save') }}</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showDeleteDialog" :title="$t('deleteChatTitle')" width="460px" align-center>
    <p class="chat-delete-text">{{ $t('deleteChatConfirm') }}</p>
    <template #footer>
      <el-button @click="showDeleteDialog = false">{{ $t('cancel') }}</el-button>
      <el-button type="danger" @click="confirmDeleteConversation">{{ $t('delete') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import QuestDetailDialog from '~/components/QuestDetailDialog.vue'
import {
  Search,
  Phone,
  VideoCamera,
  MoreFilled,
  Plus,
  IceCreamRound,
  Promotion,
  ChatDotRound,
  Memo,
  Picture,
  VideoPlay,
  Document,
  Headset,
  Location,
  Download,
  MapLocation,
  View
} from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { blockUser, unblockUser } from '~/api/auth'
import { getChatConversations, getChatMessages, markConversationRead, sendChatMessage, updateMyPresence } from '~/api/chat'
import { loadBlacklist, removeBlockedPerson, saveBlacklist, upsertBlockedPerson, isBlockedByCandidates, type BlockedPerson } from '~/utils/blacklist'
import { useWebSocket } from '~/utils/websocket'

// Add Leaflet CSS
useHead({
  link: [
    {
      rel: 'stylesheet',
      href: 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css',
      integrity: 'sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=',
      crossorigin: ''
    }
  ]
})

const route = useRoute()
const { t, locale } = useI18n()
const language = computed(() => locale.value as 'en' | 'zh')
const searchConversation = ref('')
const messageInput = ref('')
const selectedConversationId = ref<number | null>(null)
const messagesContainer = ref<HTMLElement>()
const fileInputRef = ref<HTMLInputElement>()
const fileAcceptType = ref('')
const currentAttachmentType = ref('')
const mapInstances = ref<Map<number, any>>(new Map())
const showQuestDetailDialog = ref(false)
const selectedQuestDetail = ref<any>(null)

interface Message {
  id: number
  text: string
  sender: 'user' | 'other'
  time: string
  type?: 'normal' | 'quest-introduction' | 'photo' | 'video' | 'document' | 'audio' | 'location'
  questInfo?: {
    title: string
    detail: string
    rewardPoints: number
    paymentMethod: 'face-to-face' | 'online'
    tags: string[]
  }
  fileUrl?: string
  fileName?: string
  fileSize?: number
  latitude?: number
  longitude?: number
}

interface Conversation {
  id: number
  name: string
  avatar: string
  participantEmail?: string
  lastMessage: string
  lastMessageTime: string
  unread: number
  messages: Message[]
  members?: Array<{ id: string | number; name: string }>
  isGroup?: boolean
}

interface GroupMemberOption {
  id: number
  name: string
}

const blockedPeople = ref<BlockedPerson[]>([])
const currentUserEmail = ref('')
const currentUserName = ref('')
const currentUserAvatar = ref('')
const showCreateGroupDialog = ref(false)
const groupChatName = ref('')
const selectedGroupMembers = ref<number[]>([])
const showRenameDialog = ref(false)
const showDeleteDialog = ref(false)
const renameValue = ref('')
const selectedManageConversationId = ref<string | number | null>(null)
let conversationPollHandle: number | null = null

const getPreferredTimeFormat = () => {
  try {
    const raw = localStorage.getItem('userSettings')
    const parsed = raw ? JSON.parse(raw) : null
    return parsed?.timeFormat === '24h' ? '24h' : '12h'
  } catch {
    return '12h'
  }
}

const formatClockTime = (date = new Date()) => {
  const hour12 = getPreferredTimeFormat() !== '24h'
  const localeCode = locale.value === 'zh' ? 'zh-HK' : 'en-US'
  return date.toLocaleTimeString(localeCode, {
    hour: '2-digit',
    minute: '2-digit',
    hour12,
    timeZone: 'Asia/Hong_Kong'
  })
}

const parseBackendTime = (rawValue?: string) => {
  const raw = String(rawValue || '').trim()
  if (!raw) {
    return null
  }

  const normalized = raw.replace(' ', 'T')
  const localMatch = normalized.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$/)
  if (localMatch) {
    const [, year, month, day, hour, minute, second = '0'] = localMatch
    return new Date(Number(year), Number(month) - 1, Number(day), Number(hour), Number(minute), Number(second))
  }

  const parsed = new Date(normalized)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

const toStableConversationId = (seed: string) => {
  const normalized = String(seed || '').trim().toLowerCase()
  if (!normalized) {
    return Date.now()
  }
  let hash = 0
  for (let i = 0; i < normalized.length; i += 1) {
    hash = ((hash << 5) - hash) + normalized.charCodeAt(i)
    hash |= 0
  }
  return Math.abs(hash) + 10000
}

const getScopedConversationKey = (email: string) => {
  const normalized = String(email || '').toLowerCase().trim()
  return normalized ? `chatConversations:${normalized}` : 'chatConversations'
}

const moveConversationToTop = (id: number | string) => {
  const index = conversations.value.findIndex((item) => String(item.id) === String(id))
  if (index <= 0) return
  const [picked] = conversations.value.splice(index, 1)
  if (picked) {
    conversations.value.unshift(picked)
  }
}

const toDisplayTime = (isoLike?: string) => {
  if (!isoLike) {
    return formatClockTime()
  }
  const parsed = parseBackendTime(isoLike)
  if (!parsed || Number.isNaN(parsed.getTime())) {
    return formatClockTime()
  }
  return formatClockTime(parsed)
}

const loadConversationsFromServer = async () => {
  const [error, response] = await getChatConversations()
  if (error || !response?.success || !Array.isArray(response.data)) {
    return false
  }

  if (response.data.length === 0) {
    return false
  }

  const serverConversations: Conversation[] = response.data.map((item) => {
    const existing = conversations.value.find((current) =>
      String(current.id) === String(item.conversationId) ||
      String(current.participantEmail || '').toLowerCase() === String(item.peerEmail || '').toLowerCase()
    )

    return {
      id: item.conversationId,
      name: item.peerName || item.peerEmail,
      avatar: existing?.avatar || 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
      participantEmail: item.peerEmail,
      lastMessage: item.lastMessage || '',
      lastMessageTime: toDisplayTime(item.lastMessageTime),
      unread: Number(item.unreadCount || 0),
      messages: Array.isArray(existing?.messages) ? existing.messages : []
    }
  })

  const merged: Conversation[] = [...serverConversations]
  const existingIds = new Set(serverConversations.map((item) => String(item.id)))
  const existingEmails = new Set(serverConversations.map((item) => String(item.participantEmail || '').toLowerCase()))

  conversations.value.forEach((item) => {
    const itemId = String(item.id)
    const itemEmail = String(item.participantEmail || '').toLowerCase()
    if (existingIds.has(itemId)) {
      return
    }
    if (itemEmail && existingEmails.has(itemEmail)) {
      return
    }
    merged.push(item)
  })

  conversations.value = merged
  return true
}

const loadMessagesFromServer = async (conversation: Conversation, sinceId?: number) => {
  if (!conversation.participantEmail) {
    return
  }

  const [error, response] = await getChatMessages(conversation.participantEmail, sinceId)
  if (error || !response?.success || !Array.isArray(response.data)) {
    return
  }

  const mapped = response.data.map((item) => ({
    id: item.id,
    text: item.content,
    sender: item.senderEmail?.toLowerCase() === currentUserEmail.value ? 'user' : 'other',
    time: toDisplayTime(item.sentAt),
    type: item.messageType === 'text' ? 'normal' : (item.messageType as Message['type'])
  } as Message))

  if (sinceId) {
    conversation.messages.push(...mapped)
  } else {
    if (mapped.length === 0 && conversation.messages.length > 0) {
      return
    }
    conversation.messages = mapped
  }
}

const saveRecipientConversation = (recipientEmail: string, conv: Conversation, message: Message) => {
  const key = getScopedConversationKey(recipientEmail)
  const raw = localStorage.getItem(key)
  const list = raw ? JSON.parse(raw) : []
  const arr = Array.isArray(list) ? list : []

  const senderName = currentUserName.value || 'User'
  const senderAvatar = currentUserAvatar.value || 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg'
  const senderConversationId = toStableConversationId(currentUserEmail.value || senderName)

  const recipientMessage: Message = {
    ...message,
    sender: 'other'
  }

  const existingIndex = arr.findIndex((item: Conversation) => String(item.id) === String(senderConversationId))
  if (existingIndex >= 0) {
    const existing = arr[existingIndex]
    const nextMessages = Array.isArray(existing.messages) ? [...existing.messages, recipientMessage] : [recipientMessage]
    arr[existingIndex] = {
      ...existing,
      name: senderName,
      avatar: senderAvatar,
      lastMessage: message.type === 'normal' || !message.type ? message.text : `${t(message.type)}: ${message.fileName || ''}`,
      lastMessageTime: message.time,
      unread: Number(existing.unread || 0) + 1,
      messages: nextMessages
    }
    const [picked] = arr.splice(existingIndex, 1)
    arr.unshift(picked)
  } else {
    arr.unshift({
      id: senderConversationId,
      name: senderName,
      avatar: senderAvatar,
      participantEmail: currentUserEmail.value,
      unread: 1,
      lastMessage: message.type === 'normal' || !message.type ? message.text : `${t(message.type)}: ${message.fileName || ''}`,
      lastMessageTime: message.time,
      messages: [recipientMessage]
    })
  }

  localStorage.setItem(key, JSON.stringify(arr))
}

const conversations = ref<Conversation[]>([
  {
    id: 1,
    name: 'John Doe',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    participantEmail: 'john.doe@example.com',
    lastMessage: 'Hey, how are you?',
    lastMessageTime: '10:30',
    unread: 1,
    messages: [
      { id: 1, text: 'Hey, how are you?', sender: 'other', time: '10:30' }
    ]
  },
  {
    id: 2,
    name: 'Jane Smith',
    avatar: 'https://cube.elemecdn.com/3/dc/1ea6beec64f4a146f6f02a42cc5f7.svg',
    participantEmail: 'jane.smith@example.com',
    lastMessage: 'See you tomorrow!',
    lastMessageTime: '09:15',
    unread: 0,
    messages: [
      { id: 1, text: 'See you tomorrow!', sender: 'other', time: '09:15' }
    ]
  },
  {
    id: 3,
    name: 'Community Group',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    lastMessage: 'Community event tomorrow at 5pm',
    lastMessageTime: '08:30',
    unread: 0,
    isGroup: true,
    messages: [
      { id: 1, text: 'Community event tomorrow at 5pm', sender: 'other', time: '08:30' }
    ]
  }
])

// Load conversations from backend on mount
onMounted(async () => {
  try {
    const profileRaw = localStorage.getItem('userProfile')
    const profile = profileRaw ? JSON.parse(profileRaw) : {}
    currentUserEmail.value = String(profile?.email || '').toLowerCase()
    currentUserName.value = String(profile?.name || profile?.username || '')
    currentUserAvatar.value = String(profile?.avatar || '')
  } catch {
    currentUserEmail.value = ''
  }

  if (currentUserEmail.value) {
    localStorage.setItem(`userPresence:${currentUserEmail.value}`, JSON.stringify({
      status: 'online',
      lastActiveAt: Date.now()
    }))
    await updateMyPresence('online')
  }

  blockedPeople.value = loadBlacklist()
  await loadConversationsFromServer()
  
  const peerEmail = String(route.query.peerEmail || '').toLowerCase().trim()
  const userId = route.query.userId
  if (peerEmail) {
    const byEmail = conversations.value.find((c) => String(c.participantEmail || '').toLowerCase() === peerEmail)
    if (byEmail) {
      await selectConversation(byEmail)
    } else {
      const fallbackName = String(route.query.userName || peerEmail.split('@')[0] || 'User')
      const created: Conversation = {
        id: Date.now(),
        name: fallbackName,
        avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
        participantEmail: peerEmail,
        lastMessage: '',
        lastMessageTime: formatClockTime(),
        unread: 0,
        messages: []
      }
      conversations.value.unshift(created)
      await selectConversation(created)
    }
  } else if (userId) {
    const conversationId = parseInt(userId as string)
    const conversation = conversations.value.find(c => c.id === conversationId)
    if (conversation) {
      await selectConversation(conversation)
    } else {
      // Create a new conversation if it doesn't exist
      createNewConversation(conversationId)
    }
  }

  conversationPollHandle = window.setInterval(async () => {
    const previouslySelected = selectedConversation.value?.id
    await loadConversationsFromServer()
    if (previouslySelected) {
      const nextSelected = conversations.value.find((item) => item.id === previouslySelected)
      if (nextSelected && selectedConversationId.value === previouslySelected) {
        const lastId = nextSelected.messages[nextSelected.messages.length - 1]?.id
        await loadMessagesFromServer(nextSelected, lastId)
      }
    }
  }, 1000)

  // Initialize WebSocket for real-time messaging
  const ws = useWebSocket()
  try {
    await ws.initWebSocket()
    
    // Listen for incoming messages via WebSocket
    ws.onMessage((msg) => {
      const conversation = conversations.value.find((c) =>
        String(c.participantEmail || '').toLowerCase() === String(msg.senderEmail || '').toLowerCase() ||
        String(c.participantEmail || '').toLowerCase() === String(msg.senderEmail || '').toLowerCase()
      )
      
      if (conversation) {
        const newMsg: Message = {
          id: msg.id,
          text: msg.content,
          sender: msg.senderEmail?.toLowerCase() === currentUserEmail.value ? 'user' : 'other',
          time: toDisplayTime(msg.sentAt),
          type: msg.messageType === 'text' ? 'normal' : (msg.messageType as Message['type'])
        }
        conversation.messages.push(newMsg)
        moveConversationToTop(conversation.id)
      }
    })
    
    // Listen for conversation updates
    ws.onConversationUpdate((update) => {
      loadConversationsFromServer()
    })
    
    console.log('WebSocket connected successfully')
  } catch (error) {
    console.warn('WebSocket connection failed, using polling as fallback:', error)
  }

  window.addEventListener('app:emergency-message-sent', handleEmergencyMessageSent)
})

const filteredConversations = computed(() => {
  return conversations.value.filter(conv =>
    !isConversationBlocked(conv) && conv.name.toLowerCase().includes(searchConversation.value.toLowerCase())
  )
})

const availableGroupMembers = computed<GroupMemberOption[]>(() => {
  return conversations.value
    .filter((conversation) => !conversation.isGroup)
    .filter((conversation) => !isConversationBlocked(conversation))
    .map((conversation) => ({
      id: conversation.id,
      name: conversation.name
    }))
})

const canCreateGroupConversation = computed(() => {
  const hasName = groupChatName.value.trim().length > 0
  if (!hasName) {
    return false
  }

  const selectableIds = new Set(availableGroupMembers.value.map((member) => String(member.id)))
  const validMemberCount = selectedGroupMembers.value
    .map((id) => String(id))
    .filter((id) => selectableIds.has(id)).length

  // Creator + at least 2 selected members = minimum 3 people.
  return validMemberCount >= 2
})

const selectedConversation = computed(() => {
  return conversations.value.find(c => c.id === selectedConversationId.value)
})

const showConversationWelcomeHint = computed(() => {
  return !!selectedConversation.value && selectedConversation.value.messages.length === 0
})

const newChatWelcomeText = computed(() => {
  return language.value === 'zh'
    ? '先打個招呼吧，友善交流會讓對話更順利。'
    : 'Start with a friendly hello and keep the conversation respectful.'
})

const isConversationBlocked = (conversation?: Conversation) => {
  if (!conversation) {
    return false
  }

  const memberCandidates = [conversation.id, conversation.name]
  if (Array.isArray(conversation.members)) {
    for (const member of conversation.members) {
      memberCandidates.push(member.id, member.name)
    }
  }

  return isBlockedByCandidates(blockedPeople.value, memberCandidates)
}

const selectConversation = async (conversation: Conversation) => {
  selectedConversationId.value = conversation.id
  conversation.unread = 0
  await loadMessagesFromServer(conversation)
  if (conversation.participantEmail) {
    await markConversationRead(conversation.participantEmail)
  }
  nextTick(() => {
    scrollToBottom()
  })
}


const createNewConversation = (userId: number) => {
  const storedDirectory = (() => {
    try {
      return JSON.parse(localStorage.getItem('friendDirectory') || '[]')
    } catch {
      return []
    }
  })

  const mappedDirectory = Array.isArray(storedDirectory)
    ? Object.fromEntries(storedDirectory.map((item: any) => [String(item.id), { name: item.name, avatar: item.avatar, email: item.email }]))
    : {}

  const friendData = mappedDirectory[String(userId)] as { name: string; avatar: string; email?: string } | undefined
  if (friendData) {
    const newConversation: Conversation = {
      id: Number.isNaN(userId) ? Date.now() : userId,
      name: friendData.name,
      avatar: friendData.avatar,
      lastMessage: '',
      participantEmail: (friendData as any).email,
      lastMessageTime: formatClockTime(),
      unread: 0,
      messages: []
    }
    conversations.value.unshift(newConversation)
    selectConversation(newConversation)
  }
}

const handleConversationCommand = (command: string, conversation: Conversation) => {
  selectedManageConversationId.value = conversation.id
  if (command === 'rename') {
    renameValue.value = conversation.name
    showRenameDialog.value = true
    return
  }
  if (command === 'delete') {
    showDeleteDialog.value = true
  }
}

const confirmRenameConversation = () => {
  const name = renameValue.value.trim()
  if (!name) {
    ElMessage.warning(t('renameChatRequired'))
    return
  }

  const current = conversations.value.find((item) => item.id === selectedManageConversationId.value)
  if (!current) {
    return
  }

  current.name = name
  showRenameDialog.value = false
  ElMessage.success(t('save'))
}

const confirmDeleteConversation = () => {
  const index = conversations.value.findIndex((item) => item.id === selectedManageConversationId.value)
  if (index < 0) {
    return
  }
  const [removed] = conversations.value.splice(index, 1)
  if (removed && selectedConversationId.value === removed.id) {
    selectedConversationId.value = null
  }
  showDeleteDialog.value = false
}

const sendMessage = async () => {
  if (!messageInput.value.trim() || !selectedConversation.value) return
  if (isConversationBlocked(selectedConversation.value)) {
    ElMessage.warning(t('conversationBlocked'))
    return
  }

  if (!selectedConversation.value.participantEmail) {
    ElMessage.warning(language.value === 'zh' ? '無法發送訊息' : 'Unable to send message')
    return
  }

  const outgoingText = messageInput.value.trim()
  
  // Optimistic render: show message immediately
  const optimisticId = Date.now()
  const optimisticMessage: Message = {
    id: optimisticId,
    text: outgoingText,
    sender: 'user',
    time: formatClockTime(),
    type: 'normal'
  }
  
  selectedConversation.value.messages.push(optimisticMessage)
  selectedConversation.value.lastMessage = outgoingText
  selectedConversation.value.lastMessageTime = optimisticMessage.time
  moveConversationToTop(selectedConversation.value.id)
  messageInput.value = ''
  
  nextTick(() => {
    scrollToBottom()
  })
  
  // Try to send via WebSocket first for real-time delivery
  const ws = useWebSocket()
  if (ws.isWebSocketConnected()) {
    try {
      await ws.sendMessageViaWebSocket(selectedConversation.value.participantEmail, outgoingText, 'text')
      // Mark optimistic message as sent
      const msgIndex = selectedConversation.value.messages.findIndex((m) => m.id === optimisticId)
      if (msgIndex >= 0) {
        // Message is confirmed on server, no need to update
      }
      return
    } catch (wsError) {
      console.warn('WebSocket send failed, falling back to REST:', wsError)
      // Fall through to REST API
    }
  }
  
  // Fallback to REST API
  const [error, response] = await sendChatMessage(selectedConversation.value.participantEmail, outgoingText, 'text')
  if (error || !response?.success || !response.data) {
    ElMessage.error(language.value === 'zh' ? '訊息傳送失敗' : 'Message failed to send')
    const idx = selectedConversation.value.messages.findIndex((m) => m.id === optimisticId)
    if (idx >= 0) {
      selectedConversation.value.messages.splice(idx, 1)
    }
    return
  }

  // Replace optimistic message with confirmed server version
  const msgIndex = selectedConversation.value.messages.findIndex((m) => m.id === optimisticId)
  if (msgIndex >= 0) {
    selectedConversation.value.messages[msgIndex] = {
      id: response.data.id,
      text: response.data.content,
      sender: 'user',
      time: toDisplayTime(response.data.sentAt),
      type: 'normal'
    }
  }

  if (currentUserEmail.value) {
    localStorage.setItem(`userPresence:${currentUserEmail.value}`, JSON.stringify({
      status: 'online',
      lastActiveAt: Date.now()
    }))
    await updateMyPresence('online')
  }

}

// Handle attachment type selection
const handleAttachmentCommand = (command: string) => {
  currentAttachmentType.value = command
  
  // Set file accept type based on selection
  switch (command) {
    case 'photo':
      fileAcceptType.value = 'image/*'
      break
    case 'video':
      fileAcceptType.value = 'video/*'
      break
    case 'document':
      fileAcceptType.value = '.pdf,.doc,.docx,.txt,.xls,.xlsx,.ppt,.pptx'
      break
    case 'audio':
      fileAcceptType.value = 'audio/*'
      break
    case 'location':
      // Handle location sharing separately
      handleLocationShare()
      return
    default:
      fileAcceptType.value = '*'
  }
  
  // Trigger file input
  nextTick(() => {
    fileInputRef.value?.click()
  })
}

// Handle file selection
const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file || !selectedConversation.value) return
  if (isConversationBlocked(selectedConversation.value)) {
    ElMessage.warning(t('conversationBlocked'))
    target.value = ''
    return
  }
  
  // Validate file size (max 10MB)
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    ElMessage.error(t('fileTooLarge'))
    target.value = ''
    return
  }
  
  const fileType = currentAttachmentType.value
  let displayText = file.name
  
  // Convert file to base64 data URL for persistence
  const reader = new FileReader()
  
  reader.onload = (e) => {
    const fileUrl = e.target?.result as string
    
    const newMessage: Message = {
      id: (selectedConversation.value!.messages.length || 0) + 1,
      text: displayText,
      sender: 'user',
      time: formatClockTime(),
      type: fileType as 'photo' | 'video' | 'document' | 'audio',
      fileUrl: fileUrl,
      fileName: file.name,
      fileSize: file.size
    }
    
    selectedConversation.value!.messages.push(newMessage)
    selectedConversation.value!.lastMessage = `${t(fileType)}: ${file.name}`
    selectedConversation.value!.lastMessageTime = newMessage.time
    moveConversationToTop(selectedConversation.value!.id)

    if (selectedConversation.value!.participantEmail) {
      saveRecipientConversation(selectedConversation.value!.participantEmail, selectedConversation.value!, newMessage)
    }
    
    ElMessage.success(t('fileSent'))
    
    nextTick(() => {
      scrollToBottom()
    })
  }
  
  reader.onerror = () => {
    ElMessage.error(t('fileFailed'))
  }
  
  // Read file as data URL
  reader.readAsDataURL(file)
  
  // Clear file input
  target.value = ''
}

// Handle location sharing
const handleLocationShare = () => {
  if (!selectedConversation.value) return
  if (isConversationBlocked(selectedConversation.value)) {
    ElMessage.warning(t('conversationBlocked'))
    return
  }
  
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude, accuracy } = position.coords
        const locationText = `${t('location')}: ${latitude.toFixed(6)}, ${longitude.toFixed(6)}`
        
        const newMessage: Message = {
          id: (selectedConversation.value!.messages.length || 0) + 1,
          text: locationText,
          sender: 'user',
          time: formatClockTime(),
          type: 'location',
          latitude: latitude,
          longitude: longitude
        }
        
        selectedConversation.value!.messages.push(newMessage)
        selectedConversation.value!.lastMessage = locationText
        selectedConversation.value!.lastMessageTime = newMessage.time
        moveConversationToTop(selectedConversation.value!.id)

        if (selectedConversation.value!.participantEmail) {
          saveRecipientConversation(selectedConversation.value!.participantEmail, selectedConversation.value!, newMessage)
        }
        
        ElMessage.success(t('locationShared'))
        
        nextTick(() => {
          scrollToBottom()
        })
      },
      (error) => {
        ElMessage.error(t('locationError'))
        console.error('Location error:', error)
      }
    )
  } else {
    ElMessage.error(t('locationNotSupported'))
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const handleEmergencyMessageSent = (event: Event) => {
  const detail = (event as CustomEvent).detail
  if (detail?.conversationId !== 100) return

  // 重新從 localStorage 讀取最新對話
  const storedConversations = localStorage.getItem('chatConversations')
  if (!storedConversations) return

  const parsed = JSON.parse(storedConversations)
  
  // 更新本地的 conversations 資料
  parsed.forEach((stored: Conversation) => {
    const existing = conversations.value.find(c => c.id === stored.id)
    if (existing) {
      existing.messages = stored.messages
      existing.lastMessage = stored.lastMessage
      existing.lastMessageTime = stored.lastMessageTime
    }
  })

  // 如果當前正在看 Community Group，直接滾到最底部看新訊息
  if (selectedConversationId.value === 100) {
    nextTick(() => {
      scrollToBottom()
    })
  }
}

// Format file size
const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

// Initialize Leaflet maps for location messages
const initializeLocationMaps = async () => {
  // Dynamically import Leaflet
  const L = await import('leaflet')
  
  if (!selectedConversation.value) return
  
  selectedConversation.value.messages.forEach((message) => {
    if (message.type === 'location' && message.latitude && message.longitude) {
      const mapId = `map-${message.id}`
      const mapElement = document.getElementById(mapId)
      
      if (mapElement && !mapInstances.value.has(message.id)) {
        // Initialize map
        const map = L.map(mapId).setView([message.latitude, message.longitude], 17)
        
        // Add tile layer
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap contributors',
          maxZoom: 19
        }).addTo(map)
        
        // Add marker
        const marker = L.marker([message.latitude, message.longitude]).addTo(map)
        
        // Add circle around marker
        const circle = L.circle([message.latitude, message.longitude], {
          color: '#07b981',
          fillColor: '#07b981',
          fillOpacity: 0.2,
          radius: 30 // 30 meters
        }).addTo(map)
        
        // Store map instance
        mapInstances.value.set(message.id, map)
      }
    }
  })
}

// Cleanup map instances
const cleanupMaps = () => {
  mapInstances.value.forEach((map) => {
    map.remove()
  })
  mapInstances.value.clear()
}

watch(() => selectedConversationId.value, () => {
  nextTick(() => {
    scrollToBottom()
    // Cleanup old maps
    cleanupMaps()
    // Initialize new maps after a short delay
    setTimeout(() => {
      initializeLocationMaps()
    }, 100)
  })
})

// Save conversations to localStorage when they change
watch(conversations, (newConversations) => {
  const scopedKey = getScopedConversationKey(currentUserEmail.value)
  localStorage.setItem(scopedKey, JSON.stringify(newConversations))
  localStorage.setItem('chatConversations', JSON.stringify(newConversations))
  // Reinitialize maps when messages change
  nextTick(() => {
    setTimeout(() => {
      initializeLocationMaps()
    }, 100)
  })
}, { deep: true })

// Cleanup on unmount
onBeforeUnmount(() => {
  if (conversationPollHandle) {
    window.clearInterval(conversationPollHandle)
    conversationPollHandle = null
  }
  
  // Disconnect WebSocket
  const ws = useWebSocket()
  ws.disconnect()
  
  cleanupMaps()
  if (currentUserEmail.value) {
    localStorage.setItem(`userPresence:${currentUserEmail.value}`, JSON.stringify({
      status: 'offline',
      lastActiveAt: Date.now()
    }))
    updateMyPresence('offline')
  }
  window.removeEventListener('app:emergency-message-sent', handleEmergencyMessageSent)
})

// Open quest detail dialog
const openQuestDetail = (message: Message) => {
  if (message.questInfo) {
    selectedQuestDetail.value = {
      id: message.id,
      authorId: 1,
      author: selectedConversation.value?.name || 'Unknown',
      avatar: selectedConversation.value?.avatar || '',
      title: message.questInfo.title,
      detail: message.questInfo.detail,
      tags: message.questInfo.tags,
      paymentMethod: message.questInfo.paymentMethod,
      rewardPoints: message.questInfo.rewardPoints,
      time: message.time,
      acceptedBy: null
    }
    showQuestDetailDialog.value = true
  }
}

const updateBlockedStorage = (next: BlockedPerson[]) => {
  blockedPeople.value = next
  saveBlacklist(next)

  if (selectedConversation.value && isConversationBlocked(selectedConversation.value)) {
    selectedConversationId.value = null
  }
}

const toggleConversationBlock = async (conversation: Conversation) => {
  if (conversation.isGroup) {
    ElMessage.warning(t('groupBlockHint'))
    return
  }

  const currentId = String(conversation.id)
  const blocked = isConversationBlocked(conversation)
  if (blocked) {
    const next = removeBlockedPerson(blockedPeople.value, currentId)
    updateBlockedStorage(next)
    if (currentId.includes('-')) {
      await unblockUser(currentId)
    }
    ElMessage.success(t('userUnblocked'))
    return
  }

  const next = upsertBlockedPerson(blockedPeople.value, {
    id: currentId,
    label: conversation.name
  })
  updateBlockedStorage(next)
  if (currentId.includes('-')) {
    await blockUser(currentId)
  }
  ElMessage.success(t('userBlocked'))
}

const createGroupConversation = () => {
  const name = groupChatName.value.trim()
  if (!name) {
    ElMessage.warning(t('groupChatValidation'))
    return
  }

  const selectableMemberIds = new Set(availableGroupMembers.value.map((member) => String(member.id)))
  const sanitizedSelected = selectedGroupMembers.value
    .map((id) => String(id))
    .filter((id) => selectableMemberIds.has(id))

  if (sanitizedSelected.length < 2) {
    ElMessage.warning(language.value === 'zh'
      ? '群組聊天至少需要 3 人（包括你自己）'
      : 'Group chat needs at least 3 people (including you)')
    return
  }

  const members = availableGroupMembers.value.filter((member) => sanitizedSelected.includes(String(member.id)))
  if (members.length < 2) {
    ElMessage.warning(language.value === 'zh'
      ? '只能選擇一般聊天成員，不能加入其他群組'
      : 'Only direct-chat contacts can be selected. Group chats cannot be added as members.')
    return
  }

  const now = formatClockTime()
  const newConversation: Conversation = {
    id: Date.now(),
    name,
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    lastMessage: t('groupChatCreatedMessage'),
    lastMessageTime: now,
    unread: 0,
    isGroup: true,
    members,
    messages: [
      {
        id: 1,
        text: `${t('groupChatCreatedMessage')}: ${members.map((member) => member.name).join(', ')}`,
        sender: 'user',
        time: now
      }
    ]
  }

  conversations.value.unshift(newConversation)
  selectConversation(newConversation)
  showCreateGroupDialog.value = false
  groupChatName.value = ''
  selectedGroupMembers.value = []
  ElMessage.success(t('groupChatCreatedSuccess'))
}
</script>

<style scoped>
.chat-page-shell {
  position: relative;
  border-radius: 28px;
  padding: 20px;
  overflow: hidden;
  border: 1px solid rgba(109, 125, 219, 0.28);
  background:
    radial-gradient(140% 120% at 0% 0%, rgba(79, 70, 229, 0.22), rgba(255, 255, 255, 0) 58%),
    radial-gradient(130% 140% at 100% 0%, rgba(6, 182, 212, 0.16), rgba(255, 255, 255, 0) 62%),
    linear-gradient(166deg, rgba(255, 255, 255, 0.96), rgba(242, 247, 255, 0.92));
  box-shadow: 0 40px 72px rgba(39, 52, 130, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.chat-aura {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.chat-page-shell .aura-left {
  width: 300px;
  height: 300px;
  top: -110px;
  left: -80px;
  background: radial-gradient(circle at 30% 30%, rgba(99, 102, 241, 0.54), rgba(99, 102, 241, 0));
}

.chat-page-shell .aura-right {
  width: 330px;
  height: 330px;
  bottom: -130px;
  right: -90px;
  background: radial-gradient(circle at 45% 40%, rgba(6, 182, 212, 0.48), rgba(6, 182, 212, 0));
}

.chat-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(99, 102, 241, 0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(99, 102, 241, 0.05) 1px, transparent 1px);
  background-size: 24px 24px;
  mask-image: radial-gradient(circle at 50% 35%, #000 40%, transparent 82%);
}

.chat-full {
  height: 88vh;
  display: flex;
  border-radius: 22px;
  overflow: hidden;
  border: 1px solid rgba(129, 140, 248, 0.2);
  box-shadow: 0 30px 58px rgba(40, 53, 124, 0.18);
  position: relative;
  z-index: 1;
}

.search-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.chat-container {
  display: flex;
  width: 100%;
  background:
    radial-gradient(120% 130% at 0% 0%, rgba(99, 102, 241, 0.13), rgba(255, 255, 255, 0) 58%),
    radial-gradient(120% 140% at 100% 0%, rgba(14, 165, 233, 0.1), rgba(255, 255, 255, 0) 60%),
    linear-gradient(160deg, #ffffff, #f4f8ff);
}

.conversations-panel {
  width: 320px;
  border-right: 1px solid var(--tl-border);
  display: flex;
  flex-direction: column;
  background: var(--tl-surface);
}

.search-bar {
  padding: 16px;
  border-bottom: 1px solid var(--tl-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-bar :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px var(--tl-border) inset;
}

.search-actions :deep(.el-button) {
  width: 100%;
  justify-content: center;
  border-radius: 10px;
  font-weight: 700;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.conversation-item {
  display: flex;
  align-items: center;
  padding: 9px 10px;
  border: 1px solid var(--tl-border);
  border-radius: 10px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: background-color 0.25s, border-color 0.25s;
}

.conversation-menu-btn {
  color: var(--tl-text-muted);
}

.chat-delete-text {
  margin: 4px 0;
  color: var(--tl-text);
}

.conversation-item:hover {
  background: color-mix(in srgb, var(--el-color-primary) 7%, var(--tl-surface));
  border-color: var(--el-color-primary);
}

.conversation-item.active {
  background: color-mix(in srgb, var(--el-color-primary) 12%, var(--tl-surface));
  border-color: var(--el-color-primary);
  box-shadow: none;
}

.conversation-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.conversation-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.3;
  color: var(--tl-text);
}

.conversation-time {
  font-size: 12px;
  line-height: 1.3;
  white-space: nowrap;
  color: var(--tl-text-muted);
}

.conversation-preview {
  margin: 0;
  font-size: 14px;
  color: var(--tl-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.96), rgba(246, 249, 255, 0.93));
}

.empty-chat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6b76a8;
}

.empty-chat .el-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.chat-header {
  padding: 16px;
  border-bottom: 1px solid rgba(129, 140, 248, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.9), rgba(241, 246, 255, 0.86));
  backdrop-filter: blur(6px);
}

.chat-header-left {
  display: flex;
  align-items: center;
}

.chat-header-info {
  margin-left: 12px;
}

.chat-header-info h3 {
  margin: 0;
  font-size: 19px;
  font-weight: 500;
  color: #1f2a56;
}

.chat-header-info .status {
  margin: 0;
  font-size: 16px;
  color: #6671a2;
}

.chat-header-right {
  display: flex;
  gap: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: calc(100vh - 250px);
  min-height: 300px;
}

.new-chat-welcome {
  margin: auto;
  max-width: 380px;
  text-align: center;
  color: var(--tl-text);
  border: 1px dashed var(--tl-border);
  border-radius: 14px;
  padding: 12px 16px;
  background: color-mix(in srgb, var(--tl-surface) 88%, transparent);
  font-weight: 600;
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
  padding: 11px 14px;
  border-radius: 16px;
  font-size: 18px;
  word-wrap: break-word;
  line-height: 1.4;
  border: 1px solid transparent;
  box-shadow: 0 10px 18px rgba(57, 70, 148, 0.12);
}

.message.received .message-bubble {
  background: linear-gradient(145deg, #ffffff, #edf3ff);
  border-color: rgba(129, 140, 248, 0.16);
  color: #1e2952;
}

.message.sent .message-bubble {
  background: linear-gradient(145deg, #4f46e5, #6366f1);
  border-color: rgba(175, 190, 255, 0.3);
  color: #fff;
}

.quest-intro-bubble {
  max-width: 80% !important;
  padding: 12px 14px !important;
  border-radius: 14px !important;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: #fff !important;
  box-shadow: 0 12px 22px rgba(94, 92, 212, 0.28);
}

.clickable-quest {
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.clickable-quest:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.45);
}

.clickable-quest:active {
  transform: translateY(0);
}

.click-to-view {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 12px;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  font-size: 14px;
  opacity: 0.9;
}

.click-to-view .el-icon {
  font-size: 18px;
}

.quest-intro-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 19px;
}

.quest-intro-header .el-icon {
  font-size: 24px;
}

.quest-intro-mission {
  margin: 8px 0;
}

.quest-intro-mission strong {
  display: block;
  margin-bottom: 4px;
  font-size: 17px;
  opacity: 0.9;
}

.mission-title {
  margin: 4px 0 8px 0;
  font-size: 18px;
  font-weight: 500;
}

.mission-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.quest-intro-text {
  margin: 8px 0 0 0;
}

.quest-intro-text strong {
  display: block;
  margin-bottom: 4px;
  font-size: 17px;
  opacity: 0.9;
}

.quest-intro-text p {
  margin: 0;
  font-size: 18px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.quest-intro-bubble :deep(.el-divider) {
  background-color: rgba(255, 255, 255, 0.3);
}

.quest-intro-bubble :deep(.el-tag) {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.message-time {
  font-size: 16px;
  color: #6f78a8;
  padding: 0 4px;
}

.file-message {
  padding: 8px !important;
  max-width: 350px;
}

.file-preview-container {
  margin-bottom: 8px;
}

.message-image {
  width: 100%;
  max-width: 300px;
  max-height: 300px;
  border-radius: 10px;
  cursor: pointer;
}

.message-video {
  width: 100%;
  max-width: 300px;
  border-radius: 10px;
}

.message-audio {
  width: 100%;
  max-width: 300px;
}

.file-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.file-name {
  font-size: 14px;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #6d76a5;
  margin-left: 8px;
}

.file-download {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4f46e5;
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
}

.file-download:hover {
  color: #3730a3;
  text-decoration: underline;
}

.document-message {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px !important;
  background: linear-gradient(145deg, #ffffff, #edf3ff) !important;
  color: #1e2952 !important;
}

.message.sent .document-message {
  background: linear-gradient(145deg, #4f46e5, #6366f1) !important;
  color: #fff !important;
}

.document-icon {
  color: #4f46e5;
  flex-shrink: 0;
}

.message.sent .document-icon {
  color: #fff;
}

.document-message .file-info {
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
  border-top: none;
  margin-top: 0;
  padding-top: 0;
}

.file-download-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: linear-gradient(145deg, #4f46e5, #6366f1);
  color: white;
  text-decoration: none;
  border-radius: 10px;
  font-size: 14px;
  white-space: nowrap;
  cursor: pointer;
  flex-shrink: 0;
}

.file-download-btn:hover {
  background: linear-gradient(145deg, #4338ca, #4f46e5);
}

.message.sent .file-download-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.message.sent .file-download-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.message.sent .document-message .file-name {
  color: #fff;
}

.message.sent .document-message .file-size {
  color: rgba(255, 255, 255, 0.8);
}

.audio-container {
  width: 100%;
}

.location-message {
  padding: 12px !important;
  max-width: 350px;
}

.location-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-weight: 600;
  color: #4f46e5;
}

.location-map-container {
  position: relative;
  width: 100%;
  height: 200px;
  margin-bottom: 8px;
  border-radius: 10px;
  overflow: hidden;
}

.leaflet-map {
  width: 100%;
  height: 100%;
  border-radius: 10px;
  z-index: 1;
}

.location-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.location-coords {
  font-size: 12px;
  color: #5d678f;
  font-family: monospace;
}

.location-link {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #4f46e5;
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
}

.location-link:hover {
  color: #3730a3;
  text-decoration: underline;
}

.message-input-area {
  padding: 14px 16px;
  border-top: 1px solid rgba(129, 140, 248, 0.2);
  display: flex;
  gap: 8px;
  align-items: flex-end;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.92), rgba(241, 246, 255, 0.88));
}

.message-input-area :deep(.el-input) {
  flex: 1;
}

.composer-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: 6px;
}

.message-input-area :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.22) inset, 0 8px 16px rgba(67, 80, 154, 0.1);
}

.conversations-list::-webkit-scrollbar,
.messages-container::-webkit-scrollbar {
  width: 6px;
}

.conversations-list::-webkit-scrollbar-track,
.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.conversations-list::-webkit-scrollbar-thumb,
.messages-container::-webkit-scrollbar-thumb {
  background: #bcc6ea;
  border-radius: 3px;
}

.conversations-list::-webkit-scrollbar-thumb:hover,
.messages-container::-webkit-scrollbar-thumb:hover {
  background: #9aa7d9;
}

:global(.dark) .chat-full {
  border-color: rgba(129, 140, 248, 0.36);
  box-shadow: 0 34px 64px rgba(1, 5, 17, 0.68);
}

:global(.dark) .chat-page-shell {
  border-color: rgba(129, 140, 248, 0.45);
  background:
    radial-gradient(145% 125% at 0% 0%, rgba(79, 70, 229, 0.46), rgba(10, 16, 34, 0.08) 58%),
    radial-gradient(135% 150% at 100% 0%, rgba(6, 182, 212, 0.24), rgba(10, 16, 34, 0.08) 62%),
    linear-gradient(165deg, rgba(12, 18, 38, 0.98), rgba(20, 28, 56, 0.95));
  box-shadow: 0 44px 80px rgba(1, 5, 15, 0.72), inset 0 1px 0 rgba(169, 188, 255, 0.2);
}

:global(.dark) .conversation-menu-btn,
:global(.dark) .chat-delete-text {
  color: #e5e7eb !important;
}

:global(.dark) .chat-pattern {
  background-image: linear-gradient(rgba(129, 140, 248, 0.08) 1px, transparent 1px), linear-gradient(90deg, rgba(129, 140, 248, 0.08) 1px, transparent 1px);
}

:global(.dark) .chat-container {
  background:
    radial-gradient(130% 130% at 0% 0%, rgba(79, 70, 229, 0.34), rgba(16, 22, 45, 0.08) 58%),
    radial-gradient(130% 145% at 100% 0%, rgba(14, 165, 233, 0.18), rgba(16, 22, 45, 0.08) 62%),
    linear-gradient(160deg, #11162d, #1a2345) !important;
}

:global(.dark) .conversations-panel {
  background: linear-gradient(160deg, rgba(24, 31, 61, 0.95), rgba(18, 24, 49, 0.92)) !important;
  border-right-color: rgba(129, 140, 248, 0.34) !important;
}

:global(.dark) .search-bar {
  border-bottom-color: rgba(129, 140, 248, 0.3) !important;
}

:global(.dark) .search-bar :deep(.el-input__wrapper) {
  background: rgba(16, 22, 44, 0.88) !important;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.36) inset, 0 8px 16px rgba(1, 5, 15, 0.52) !important;
}

:global(.dark) .conversation-item {
  border-bottom-color: rgba(129, 140, 248, 0.24) !important;
}

:global(.dark) .conversation-item:hover {
  background-color: rgba(79, 70, 229, 0.24) !important;
}

:global(.dark) .conversation-item.active {
  background-color: rgba(79, 70, 229, 0.32) !important;
  box-shadow: inset 3px 0 0 rgba(165, 180, 252, 0.85);
}

:global(.dark) .conversation-name,
:global(.dark) .chat-header-info h3 {
  color: #e8eeff !important;
}

:global(.dark) .conversation-time,
:global(.dark) .conversation-preview,
:global(.dark) .chat-header-info .status,
:global(.dark) .empty-chat,
:global(.dark) .location-coords,
:global(.dark) .file-size,
:global(.dark) .message-time {
  color: #b8c3ed !important;
}

:global(.dark) .chat-panel {
  background: linear-gradient(160deg, rgba(21, 28, 57, 0.95), rgba(16, 22, 45, 0.92)) !important;
}

:global(.dark) .chat-header {
  background: linear-gradient(150deg, rgba(24, 31, 61, 0.95), rgba(18, 24, 49, 0.92)) !important;
  border-bottom-color: rgba(129, 140, 248, 0.32) !important;
}

:global(.dark) .message.received .message-bubble,
:global(.dark) .document-message,
:global(.dark) .location-message {
  background: linear-gradient(150deg, rgba(36, 42, 74, 0.88), rgba(24, 31, 58, 0.84)) !important;
  border-color: rgba(129, 140, 248, 0.3);
  color: #e5e5e5 !important;
}

:global(.dark) .message.sent .message-bubble,
:global(.dark) .message.sent .document-message {
  background: linear-gradient(145deg, #4f46e5, #6366f1) !important;
  color: #fff !important;
}

:global(.dark) .message-input-area {
  background: linear-gradient(150deg, rgba(24, 31, 61, 0.92), rgba(18, 24, 49, 0.9)) !important;
  border-top-color: rgba(129, 140, 248, 0.32) !important;
}

:global(.dark) .message-input-area :deep(.el-input__wrapper) {
  background: rgba(16, 22, 44, 0.88) !important;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.38) inset, 0 8px 16px rgba(1, 5, 15, 0.52) !important;
}

:global(.dark) .message-input-area :deep(.el-input__inner),
:global(.dark) .message-input-area :deep(.el-button--text),
:global(.dark) .file-name {
  color: #e5e5e5 !important;
}

:global(.dark) .message-input-area :deep(.el-button--text:hover),
:global(.dark) .location-link,
:global(.dark) .location-header,
:global(.dark) .file-download,
:global(.dark) .document-icon {
  color: #a5b4fc !important;
}

:global(.dark) .location-link:hover,
:global(.dark) .file-download:hover {
  color: #c7d2fe !important;
}

:global(.dark) .message-image,
:global(.dark) .message-video {
  border: 1px solid rgba(129, 140, 248, 0.28);
}

:global(.dark) .clickable-quest:hover {
  box-shadow: 0 10px 20px rgba(102, 126, 234, 0.62);
}

:global(.dark) .click-to-view {
  border-top-color: rgba(255, 255, 255, 0.2);
}

@media (max-width: 1024px) {
  .chat-full {
    height: auto;
    min-height: 80vh;
  }

  .chat-container {
    flex-direction: column;
  }

  .conversations-panel {
    width: 100%;
    max-height: 280px;
  }
}

.chat-page-shell {
  backdrop-filter: blur(16px);
}

.chat-page-shell::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  pointer-events: none;
}

.chat-full {
  box-shadow: 0 40px 70px rgba(33, 46, 122, 0.22);
}

.chat-header {
  backdrop-filter: blur(10px);
}

.conversation-item.active {
  box-shadow: inset 3px 0 0 rgba(79, 70, 229, 0.7), 0 0 24px rgba(99, 102, 241, 0.24);
}

.message.sent .message-bubble {
  box-shadow: 0 12px 22px rgba(75, 88, 191, 0.28);
}

.message.received .message-bubble {
  box-shadow: 0 12px 22px rgba(76, 90, 170, 0.18);
}

.chat-page-shell :deep(.el-button.is-circle) {
  box-shadow: 0 8px 18px rgba(80, 93, 181, 0.2);
}

:global(.dark) .chat-page-shell {
  backdrop-filter: blur(20px);
}

:global(.dark) .chat-page-shell::before {
  border-color: rgba(171, 185, 255, 0.28);
}

:global(.dark) .chat-full {
  box-shadow: 0 44px 78px rgba(1, 5, 18, 0.74);
}

:global(.dark) .conversation-item.active {
  box-shadow: inset 3px 0 0 rgba(165, 180, 252, 0.85), 0 0 24px rgba(129, 140, 248, 0.26);
}

:global(.dark) .message.sent .message-bubble {
  box-shadow: 0 14px 24px rgba(79, 70, 229, 0.34);
}

:global(.dark) .chat-page-shell :deep(.el-button.is-circle) {
  box-shadow: 0 10px 20px rgba(2, 6, 19, 0.65);
}

@media (max-width: 1200px) {
  .conversations-panel {
    width: 320px;
  }
}

/* Marketplace-inspired override */
.chat-aura,
.chat-pattern {
  display: none !important;
}

.chat-page-shell {
  padding: 0 !important;
  border: none !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  backdrop-filter: none !important;
}

.chat-page-shell::before,
.chat-page-shell::after {
  display: none !important;
}

.chat-full {
  border: 1px solid var(--tl-border) !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 12px rgba(15, 23, 42, 0.06) !important;
}

.chat-container,
.conversations-panel,
.chat-panel,
.chat-header,
.message-input-area {
  background: var(--tl-surface) !important;
}

.conversations-panel,
.chat-header,
.message-input-area {
  border-color: var(--tl-border) !important;
}

.conversation-item {
  border-bottom-color: #f1f3f7 !important;
}

.conversation-item:hover {
  background: var(--tl-surface-soft) !important;
  transform: none !important;
}

.conversation-item.active {
  background: var(--tl-surface-active) !important;
  box-shadow: inset 3px 0 0 #ff6f00 !important;
}

.message.received .message-bubble {
  background: var(--tl-bg) !important;
  border-color: var(--tl-border) !important;
  color: var(--tl-text) !important;
}

.message.sent .message-bubble,
.message.sent .document-message,
.file-download-btn {
  background: #ff6f00 !important;
  border-color: #ff6f00 !important;
}

.file-download,
.location-link,
.location-header,
.document-icon {
  color: #ff6f00 !important;
}

.chat-page-shell :deep(.el-input__wrapper),
.search-bar :deep(.el-input__wrapper) {
  background: var(--tl-surface) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
  border-radius: 10px !important;
}

:global(.dark) .chat-page-shell {
  background: transparent !important;
}

:global(.dark) .conversation-item {
  border-bottom-color: var(--tl-border) !important;
}

:global(.dark) .conversation-item:hover {
  background: var(--tl-surface-soft) !important;
}

:global(.dark) .conversation-item.active {
  background: var(--tl-surface-active) !important;
  box-shadow: inset 3px 0 0 #ff9f52 !important;
}

:global(.dark) .message.received .message-bubble {
  background: var(--tl-bg) !important;
  border-color: var(--tl-border) !important;
}

:global(.dark) .chat-page-shell :deep(.el-input__wrapper),
:global(.dark) .search-bar :deep(.el-input__wrapper) {
  background: var(--tl-bg) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
}

@media (max-width: 768px) {
  .chat-page-shell {
    padding: 0 !important;
  }

  .chat-full {
    min-height: 74vh;
    border-radius: 10px !important;
  }

  .chat-header {
    padding: 10px 12px;
  }

  .messages-area {
    padding: 12px;
  }

  .message-input-area {
    padding: 10px 12px;
  }
}

/* ============ TABLET BREAKPOINT (481-1024px) ============ */
@media (max-width: 1024px) {
  .chat-container {
    flex-direction: column;
    gap: 12px;
  }

  .sidebar {
    max-height: 30vh;
    border-right: none;
    border-bottom: 1px solid #e5e5e5;
  }

  .chat-full {
    min-height: 60vh;
  }

  .search-bar :deep(.el-input) {
    height: 44px;
  }

  .message-input-area :deep(.el-input) {
    height: 44px;
  }

  .message-input-area :deep(.el-button) {
    min-height: 44px;
  }
}

/* ============ PHONE BREAKPOINT (≤480px) ============ */
@media (max-width: 480px) {
  .chat-page-shell {
    padding: 0 !important;
    gap: 8px;
  }

  .chat-container {
    flex-direction: column;
    gap: 10px;
    height: auto;
  }

  .sidebar {
    max-height: 25vh;
    border-radius: 10px;
    border-right: none;
    border-bottom: 1px solid #e5e5e5;
    padding: 10px;
    overflow-y: auto;
  }

  .search-bar {
    padding: 10px;
    gap: 8px;
  }

  .search-bar :deep(.el-input) {
    height: 44px;
    font-size: 13px;
  }

  .conversation-list {
    gap: 6px;
  }

  .conversation-item {
    padding: 10px;
    border-radius: 8px;
    gap: 8px;
  }

  .conversation-item :deep(.el-avatar) {
    width: 40px;
    height: 40px;
  }

  .conversation-info {
    min-width: 0;
  }

  .conversation-name {
    font-size: 12px;
  }

  .conversation-preview {
    font-size: 11px;
  }

  .chat-full {
    min-height: 60vh;
    border-radius: 12px;
    display: flex;
    flex-direction: column;
  }

  .chat-header {
    padding: 10px 12px;
    font-size: 14px;
  }

  .chat-header-title {
    font-size: 14px;
  }

  .chat-header :deep(.el-button) {
    min-width: 36px;
    min-height: 36px;
    padding: 6px;
  }

  .messages-area {
    padding: 10px;
    gap: 8px;
  }

  .message-group {
    gap: 6px;
    margin-bottom: 10px;
  }

  .message {
    margin: 6px 0;
  }

  .message-bubble {
    max-width: 85%;
    padding: 8px 10px;
    font-size: 13px;
    border-radius: 10px;
  }

  .message-time {
    font-size: 10px;
    margin-top: 4px;
  }

  .message-input-area {
    padding: 10px;
    gap: 8px;
    flex-wrap: wrap;
  }

  .message-input-area :deep(.el-input) {
    height: 44px;
    font-size: 13px;
    flex: 1;
    min-width: 200px;
  }

  .message-input-area :deep(.el-button) {
    min-width: 40px;
    min-height: 40px;
    padding: 8px;
    font-size: 12px;
  }

  .message-input-area .emoji-picker {
    min-width: 40px;
    min-height: 40px;
  }

  .system-message {
    font-size: 11px;
    padding: 6px 8px;
  }

  .message-tooltip {
    font-size: 11px;
  }
}

/* ============ EXTRA SMALL DEVICES (≤360px) ============ */
@media (max-width: 360px) {
  .sidebar {
    max-height: 20vh;
    padding: 8px;
  }

  .conversation-item {
    padding: 8px;
  }

  .conversation-name {
    font-size: 11px;
  }

  .conversation-preview {
    font-size: 10px;
  }

  .chat-header {
    padding: 8px 10px;
    font-size: 13px;
  }

  .chat-header-title {
    font-size: 13px;
  }

  .messages-area {
    padding: 8px;
  }

  .message-bubble {
    font-size: 12px;
    padding: 6px 8px;
  }

  .message-time {
    font-size: 9px;
  }

  .message-input-area :deep(.el-input) {
    min-width: 180px;
    font-size: 12px;
    height: 40px;
  }

  .message-input-area :deep(.el-button) {
    min-width: 36px;
    min-height: 36px;
    font-size: 11px;
  }

  .system-message {
    font-size: 10px;
  }

  .search-bar :deep(.el-input) {
    height: 40px;
    font-size: 12px;
  }
}
</style>
