<template>
  <el-container class="app-layout">
    <!-- Header -->
    <el-header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="app-title" @click="goToHome" style="cursor: pointer;">🔗 {{ $t('appName') }}</h1>
          <div class="top-nav">
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/home') }"
              @click="handleMenuClick('/home')"
            >
              <el-icon><HomeFilled /></el-icon>
              <span>{{ $t('home') }}</span>
            </el-button>
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/posts') }"
              @click="handleMenuClick('/posts')"
            >
              <el-icon><DocumentCopy /></el-icon>
              <span>{{ $t('posts') }}</span>
            </el-button>
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/nearbyShops') }"
              @click="handleMenuClick('/nearbyShops')"
            >
              <el-icon><Location /></el-icon>
              <span>{{ $t('nearbyShops') }}</span>
            </el-button>
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/nearbyCommunity') }"
              @click="handleMenuClick('/nearbyCommunity')"
            >
              <el-icon><OfficeBuilding /></el-icon>
              <span>{{ $t('nearbyCommunity') }}</span>
            </el-button>
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/chat') }"
              @click="handleMenuClick('/chat')"
            >
              <el-icon><ChatLineRound /></el-icon>
              <span>{{ $t('messages') }}</span>
            </el-button>
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/friends') }"
              @click="handleMenuClick('/friends')"
            >
              <el-icon><User /></el-icon>
              <span>{{ $t('friends') }}</span>
            </el-button>
            <el-button
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes('/ai-chat') }"
              @click="handleMenuClick('/ai-chat')"
            >
              <el-icon><Service /></el-icon>
              <span>{{ $t('aiAssistant') }}</span>
            </el-button>
          </div>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" :src="userAvatar" />
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  {{ $t('profile') }}
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  {{ $t('settings') }}
                </el-dropdown-item>
                <el-dropdown-item command="language">
                  {{ $t('language') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  {{ $t('logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" class="notification-dropdown" @visible-change="handleNotificationVisible">
            <el-badge :value="notifications" :hidden="notifications === 0" class="notification-badge">
              <el-button text :icon="Bell" />
            </el-badge>
            <template #dropdown>
              <el-dropdown-menu class="notification-menu">
                <div class="notification-menu-header">
                  <span>{{ $t('notifications') }}</span>
                  <el-button text size="small" @click="markAllNotificationsRead">{{ $t('markAllRead') }}</el-button>
                </div>
                <el-dropdown-item v-if="notificationItems.length === 0" disabled>
                  {{ $t('noNotifications') }}
                </el-dropdown-item>
                <el-dropdown-item
                  v-for="item in notificationItems.slice(0, 8)"
                  :key="item.id"
                  class="notification-item"
                  :class="{ 'notification-item-unread': !item.read, 'notification-item-clickable': item.type === 'chat' || item.type === 'post_removed' }"
                  @click="handleNotificationClick(item)"
                >
                  <div class="notification-item-content">
                    <p class="notification-item-title">{{ item.title }}</p>
                    <p class="notification-item-message">{{ item.message }}</p>
                    <p class="notification-item-time">{{ formatRelativeTime(item.time) }}</p>
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown @command="handleEmergencyCommand">
            <el-button type="danger" circle size="large" class="emergency-btn">
              <span class="emergency-icon">!</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="fireAlert">
                  <el-icon><WarningFilled /></el-icon>
                  {{ $t('fireEmergency') }}
                </el-dropdown-item>
                <el-dropdown-item command="powerOutage">
                  <el-icon><Lightning /></el-icon>
                  {{ $t('powerOutage') }}
                </el-dropdown-item>
                <el-dropdown-item command="floodAlert">
                  <el-icon><Watermelon /></el-icon>
                  {{ $t('floodAlert') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="callSecurity">
                  <el-icon><Phone /></el-icon>
                  {{ $t('callSecurity') }}
                </el-dropdown-item>
                <el-dropdown-item command="emergencyGroup">
                  <el-icon><ChatLineRound /></el-icon>
                  {{ $t('emergencyGroupChat') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown trigger="click" @command="handleOthersCommand">
            <el-button text class="top-nav-button">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ $t('assistanceSupport') }}</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="appFeedback">
                  <el-icon><ChatDotSquare /></el-icon>
                  {{ $t('appFeedback') }}
                </el-dropdown-item>
                <el-dropdown-item command="communityFeedback">
                  <el-icon><Comment /></el-icon>
                  {{ $t('communityFeedback') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="helpCenter">
                  <el-icon><QuestionFilled /></el-icon>
                  {{ $t('helpCenter') }}
                </el-dropdown-item>
                <el-dropdown-item command="aboutUs">
                  <el-icon><InfoFilled /></el-icon>
                  {{ $t('aboutUs') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="privacyPolicy">
                  <el-icon><Lock /></el-icon>
                  {{ $t('privacyPolicy') }}
                </el-dropdown-item>
                <el-dropdown-item command="termsOfService">
                  <el-icon><Document /></el-icon>
                  {{ $t('termsOfService') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="version">
                  <el-icon><Stamp /></el-icon>
                  {{ $t('version') }}: v1.0.0
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <!-- Main Content -->
    <el-container class="main-content">
      <!-- Page Content -->
      <el-main class="app-main">
        <slot></slot>
      </el-main>
    </el-container>

    <!-- Feedback Dialog -->
    <FeedbackDialog
      v-model="showFeedbackDialog"
      :title="feedbackTitle"
      :feedback-type="feedbackType"
      @submit="handleFeedbackSubmit"
    />

    <!-- Post Removal Detail Dialog -->
    <el-dialog
      v-model="showRemovalDetailDialog"
      :title="$t('postRemovalDetailTitle')"
      width="520px"
      :close-on-click-modal="false"
    >
      <div v-if="activeRemovalNotification" class="removal-detail-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="$t('postTitle')">
            {{ activeRemovalNotification.meta?.postTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('postCreateDate')">
            {{ formatRemovalDate(activeRemovalNotification.meta?.postCreateTime) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('postRemovalDate')">
            {{ formatRemovalDate(activeRemovalNotification.meta?.removalTime) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('removalReason')">
            {{ $t('deleteTag_' + activeRemovalNotification.meta?.removalTag) }}
          </el-descriptions-item>
          <el-descriptions-item :label="$t('removalDescription')">
            {{ activeRemovalNotification.meta?.removalDescription || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="activeRemovalNotification.meta?.postContent" class="removal-post-content">
          <p class="removal-section-label">{{ $t('postDescription') }}</p>
          <p class="removal-post-text">{{ activeRemovalNotification.meta.postContent }}</p>
        </div>
        <div
          v-if="activeRemovalNotification.meta?.postPhotos && activeRemovalNotification.meta.postPhotos.length > 0"
          class="removal-photos"
        >
          <p class="removal-section-label">{{ $t('postPhotos') }}</p>
          <div class="removal-photos-grid">
            <el-image
              v-for="(photo, idx) in activeRemovalNotification.meta.postPhotos"
              :key="idx"
              :src="photo.url || photo"
              fit="cover"
              class="removal-photo-item"
              :preview-src-list="activeRemovalNotification.meta.postPhotos.map((p: any) => p.url || p)"
            />
          </div>
        </div>

        <!-- False Removal Report -->
        <el-divider />
        <div v-if="!showFalseReportForm" class="false-report-trigger">
          <el-button type="warning" plain @click="showFalseReportForm = true">
            {{ $t('falseRemovalReportButton') }}
          </el-button>
        </div>
        <div v-else class="false-report-form">
          <p class="removal-section-label">{{ $t('falseRemovalReportLabel') }}</p>
          <el-input
            v-model="falseReportReason"
            type="textarea"
            :rows="4"
            :placeholder="$t('falseRemovalReportPlaceholder')"
            :maxlength="500"
            show-word-limit
          />
          <div class="false-report-actions">
            <el-button @click="showFalseReportForm = false">{{ $t('cancel') }}</el-button>
            <el-button
              type="primary"
              :disabled="falseReportReason.trim().length < 10"
              @click="submitFalseReport"
            >
              {{ $t('submit') }}
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </el-container>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { navigateTo } from '#app'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import FeedbackDialog from '~/components/FeedbackDialog.vue'
import {
  HomeFilled,
  DocumentCopy,
  Location,
  OfficeBuilding,
  User,
  Service,
  Plus,
  Search,
  Bell,
  ChatLineRound,
  ChatDotSquare,
  Comment,
  WarningFilled,
  Lightning,
  Watermelon,
  Phone,
  QuestionFilled,
  InfoFilled,
  Lock,
  Document,
  Stamp
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const { locale, t, setLocale } = useI18n()
const switchLocalePath = useSwitchLocalePath()
const searchQuery = ref('')
const activeMenuPath = ref(route.path)
const userProfile = ref<any>(null)
const userSettings = ref<any>(null)
const showFeedbackDialog = ref(false)
const feedbackTitle = ref('')
const feedbackType = ref<'app' | 'community'>('app')

interface AppNotification {
  id: string
  title: string
  message: string
  time: string
  read: boolean
  type?: 'chat' | 'post_removed' | 'generic'
  meta?: Record<string, any>
}

const notificationItems = ref<AppNotification[]>([])

const notifications = computed(() => {
  return notificationItems.value.filter((item) => !item.read).length
})

// Computed property for user avatar
const userAvatar = computed(() => {
  return userProfile.value?.avatar || 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg'
})

// Apply font size to document
const applyFontSize = (size: number | string) => {
  let fontSize: string
  if (typeof size === 'number') {
    fontSize = `${size}px`
  } else {
    const fontSizes: Record<string, string> = {
      small: '16px',
      medium: '18px',
      large: '22px',
      xlarge: '26px'
    }
    fontSize = fontSizes[size] || fontSizes.medium
  }
  document.documentElement.style.setProperty('--base-font-size', fontSize)
  document.body.style.fontSize = fontSize
}

// Apply theme color to CSS variables
const applyThemeColor = (color: string) => {
  document.documentElement.style.setProperty('--el-color-primary', color)
  document.documentElement.style.setProperty('--user-theme-color', color)
}

// Apply compact mode to document
const applyCompactMode = (isCompact: boolean) => {
  if (isCompact) {
    document.documentElement.style.setProperty('--spacing-unit', '8px')
    document.documentElement.style.setProperty('--card-padding', '12px')
    document.documentElement.style.setProperty('--element-height', '32px')
  } else {
    document.documentElement.style.setProperty('--spacing-unit', '16px')
    document.documentElement.style.setProperty('--card-padding', '20px')
    document.documentElement.style.setProperty('--element-height', '40px')
  }
}

// Apply dark mode to document
const applyDarkMode = (isDark: boolean) => {
  if (isDark) {
    document.documentElement.classList.add('dark')
    document.body.classList.add('dark')
    document.body.style.backgroundColor = '#1a1a1a'
    document.body.style.color = '#e5e5e5'
  } else {
    document.documentElement.classList.remove('dark')
    document.body.classList.remove('dark')
    document.body.style.backgroundColor = '#f5f7fa'
    document.body.style.color = '#333'
  }
}

// Load user settings from localStorage
const loadUserSettings = () => {
  const savedSettings = localStorage.getItem('userSettings')
  if (savedSettings) {
    userSettings.value = JSON.parse(savedSettings)
    // Apply settings
    if (userSettings.value.fontSizeValue) {
      applyFontSize(userSettings.value.fontSizeValue)
    }
    if (userSettings.value.themeColor) {
      applyThemeColor(userSettings.value.themeColor)
    }
    if (userSettings.value.darkMode !== undefined) {
      applyDarkMode(userSettings.value.darkMode)
    }
    if (userSettings.value.compactMode !== undefined) {
      applyCompactMode(userSettings.value.compactMode)
    }
  } else {
    applyDarkMode(false)
  }
}

// Load user profile from localStorage
const loadUserProfile = () => {
  const savedProfile = localStorage.getItem('userProfile')
  if (savedProfile) {
    userProfile.value = JSON.parse(savedProfile)
  }
}

// Load user language preference from localStorage
const loadUserLanguage = () => {
  const savedLanguage = localStorage.getItem('userLanguage')
  if (savedLanguage && (savedLanguage === 'en' || savedLanguage === 'zh')) {
    if (locale.value !== savedLanguage) {
      setLocale(savedLanguage)
    }
  }
}

const persistNotifications = () => {
  localStorage.setItem('appNotifications', JSON.stringify(notificationItems.value))
}

const loadNotifications = () => {
  const raw = localStorage.getItem('appNotifications')
  if (!raw) {
    notificationItems.value = []
    return
  }

  try {
    const parsed = JSON.parse(raw)
    if (Array.isArray(parsed)) {
      notificationItems.value = parsed
    }
  } catch {
    notificationItems.value = []
  }
}

const addNotification = (title: string, message: string, type?: AppNotification['type'], meta?: Record<string, any>) => {
  const item: AppNotification = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2, 7)}`,
    title,
    message,
    time: new Date().toISOString(),
    read: false,
    type: type || 'generic',
    meta
  }

  notificationItems.value = [item, ...notificationItems.value].slice(0, 50)
  persistNotifications()
}

const markAllNotificationsRead = () => {
  notificationItems.value = notificationItems.value.map((item) => ({
    ...item,
    read: true
  }))
  persistNotifications()
}

const handleNotificationVisible = (visible: boolean) => {
  if (visible) {
    markAllNotificationsRead()
  }
}

const formatNotificationTime = (time: string) => {
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) {
    return ''
  }

  return date.toLocaleString(locale.value === 'zh' ? 'zh-HK' : 'en-US', {
    hour: '2-digit',
    minute: '2-digit',
    month: 'short',
    day: 'numeric'
  })
}

const formatRelativeTime = (time: string) => {
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''
  const diff = Math.floor((Date.now() - date.getTime()) / 1000)
  if (diff < 60) return t('justNow')
  const mins = Math.floor(diff / 60)
  if (mins < 60) return t('minutesAgo', { n: mins })
  const hours = Math.floor(mins / 60)
  if (hours < 24) return t('hoursAgo', { n: hours })
  const days = Math.floor(hours / 24)
  return t('daysAgo', { n: days })
}

const formatRemovalDate = (time?: string) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return time
  return date.toLocaleString(locale.value === 'zh' ? 'zh-HK' : 'en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

// Removal detail dialog state
const showRemovalDetailDialog = ref(false)
const activeRemovalNotification = ref<AppNotification | null>(null)
const showFalseReportForm = ref(false)
const falseReportReason = ref('')

const submitFalseReport = () => {
  if (!activeRemovalNotification.value) return
  const reports = JSON.parse(localStorage.getItem('falseRemovalReports') || '[]')
  reports.push({
    id: `report-${Date.now()}`,
    notificationId: activeRemovalNotification.value.id,
    postId: activeRemovalNotification.value.meta?.postId,
    postTitle: activeRemovalNotification.value.meta?.postTitle,
    reason: falseReportReason.value.trim(),
    submittedAt: new Date().toISOString()
  })
  localStorage.setItem('falseRemovalReports', JSON.stringify(reports))
  ElMessage.success(t('falseRemovalReportSubmitted'))
  showFalseReportForm.value = false
  falseReportReason.value = ''
  showRemovalDetailDialog.value = false
}

const handleNotificationClick = (item: AppNotification) => {
  if (item.type === 'chat' && item.meta?.conversationId) {
    router.push({ path: '/chat', query: { userId: String(item.meta.conversationId) } })
  } else if (item.type === 'post_removed') {
    activeRemovalNotification.value = item
    showFalseReportForm.value = false
    falseReportReason.value = ''
    showRemovalDetailDialog.value = true
  }
}

// Listen for notification events dispatched by other pages (chat, home)
const handleAppNotificationEvent = (event: Event) => {
  const detail = (event as CustomEvent).detail
  if (!detail) return
  if (detail.type === 'chat') {
    addNotification(
      t('chatNotificationTitle', { name: detail.senderName }),
      detail.message,
      'chat',
      { conversationId: detail.conversationId, senderName: detail.senderName }
    )
  } else if (detail.type === 'post_removed') {
    addNotification(
      t('postRemovedNotificationTitle'),
      t('postRemovedNotificationMessage', { tag: t('deleteTag_' + detail.removalTag) }),
      'post_removed',
      {
        postId: detail.postId,
        postTitle: detail.postTitle,
        postContent: detail.postContent,
        postPhotos: detail.postPhotos,
        postCreateTime: detail.postCreateTime,
        removalTime: detail.removalTime,
        removalTag: detail.removalTag,
        removalDescription: detail.removalDescription
      }
    )
  }
}

// Load profile on mount
onMounted(() => {
  loadUserLanguage()
  loadUserProfile()
  loadUserSettings()
  loadNotifications()
  
  // Listen for storage changes (when profile is updated in another tab or by profile page)
  window.addEventListener('storage', () => {
    loadUserProfile()
    loadUserSettings()
    loadUserLanguage()
  })

  // Listen for in-page notification events from chat.vue and home.vue
  window.addEventListener('app:notification', handleAppNotificationEvent)
})

onUnmounted(() => {
  window.removeEventListener('app:notification', handleAppNotificationEvent)
})

// Watch route changes and update active menu
watch(() => route.path, (newPath) => {
  activeMenuPath.value = newPath
  // Reload profile and settings when navigating (in case they were updated)
  loadUserProfile()
  loadUserSettings()
}, { immediate: true })

const handleMenuClick = (path: string) => {
  navigateTo(path)
}

const goToHome = () => {
  router.push('/home')
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userToken')
    router.push('/')
  } else if (command === 'language') {
    const newLocale = locale.value === 'en' ? 'zh' : 'en'
    await setLocale(newLocale)
    // Save language preference to localStorage
    localStorage.setItem('userLanguage', newLocale)
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}

const goToCreatePost = () => {
  router.push('/create-post')
}

const handleOthersCommand = (command: string) => {
  switch (command) {
    case 'appFeedback':
      feedbackTitle.value = t('appFeedback')
      feedbackType.value = 'app'
      showFeedbackDialog.value = true
      break
    case 'communityFeedback':
      feedbackTitle.value = t('communityFeedback')
      feedbackType.value = 'community'
      showFeedbackDialog.value = true
      break
    case 'helpCenter':
      ElMessage.info(t('helpCenterComingSoon'))
      break
    case 'aboutUs':
      ElMessage.info(t('aboutUsMessage'))
      break
    case 'privacyPolicy':
      ElMessage.info(t('privacyPolicyComingSoon'))
      break
    case 'termsOfService':
      ElMessage.info(t('termsOfServiceComingSoon'))
      break
    case 'version':
      ElMessage.info(t('versionMessage'))
      break
  }
}

const handleFeedbackSubmit = (data: any) => {
  console.log('Feedback submitted:', data)
  // TODO: Send feedback to backend API
  // For now, just store in localStorage
  const feedbacks = JSON.parse(localStorage.getItem('feedbacks') || '[]')
  feedbacks.push({
    ...data,
    timestamp: new Date().toISOString()
  })
  localStorage.setItem('feedbacks', JSON.stringify(feedbacks))

  addNotification(
    feedbackType.value === 'app' ? t('appFeedback') : t('communityFeedback'),
    t('feedbackSubmitted')
  )
}

// Handle emergency dropdown commands
const handleEmergencyCommand = (command: string) => {
  const now = new Date()
  const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })

  const templates: Record<string, string> = {
    fireAlert: t('fireEmergency') + ' — ' + t('pleaseHelp'),
    powerOutage: t('powerOutage') + ' — ' + t('pleaseCheck'),
    floodAlert: t('floodAlert') + ' — ' + t('pleaseHelp'),
    callSecurity: t('callSecurity') + ' — ' + t('callSecurityNow'),
    emergencyGroup: t('emergencyGroupChat') + ' — ' + t('urgentPleaseRespond')
  }

  const text = templates[command] || t('emergencyAlert')

  try {
    const raw = localStorage.getItem('chatConversations')
    const conversations = raw ? JSON.parse(raw) : []

    let conv = conversations.find((c: any) => c.id === 100)
    if (!conv) {
      conv = {
        id: 100,
        name: 'Community Group',
        avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
        lastMessage: '',
        lastMessageTime: timeStr,
        unread: 0,
        messages: []
      }
      conversations.unshift(conv)
    }

    const newMsg = {
      id: (conv.messages.length || 0) + 1,
      text,
      sender: 'user',
      time: timeStr
    }

    conv.messages.push(newMsg)
    conv.lastMessage = text
    conv.lastMessageTime = timeStr

    localStorage.setItem('chatConversations', JSON.stringify(conversations))

    addNotification(t('notice'), text)

    ElMessage.success(t('emergencySent'))
    // Optionally navigate to chat page
    router.push({ path: '/chat', query: { userId: '100' } })
  } catch (e) {
    console.error('Failed to send emergency message', e)
    ElMessage.error(t('emergencyFailed'))
  }
}
</script>

<style scoped>
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f7fa;
}

.app-header {
  background: white;
  border-bottom: 1px solid #e5e5e5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  padding: 0 !important;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  padding: 0 20px;
}

.header-left {
  flex: 0 0 auto;
  width: 260px;
}

.app-title {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #333;
  transition: color 0.3s;
}

.app-title:hover {
  color: #07b981;
}

.header-center {
  flex: 1;
  margin: 0 40px;
}

.search-input {
  width: 100%;
  max-width: 400px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.notification-badge {
  cursor: pointer;
}

.notification-menu {
  min-width: 320px;
  max-width: 360px;
}

.notification-menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid #eceff5;
  font-weight: 600;
}

.notification-item {
  white-space: normal;
  padding-top: 6px;
  padding-bottom: 6px;
}

.notification-item-content {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.notification-item-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #25305c;
}

.notification-item-message {
  margin: 0;
  font-size: 13px;
  color: #5f6c97;
  line-height: 1.4;
}

.notification-item-time {
  margin: 0;
  font-size: 12px;
  color: #8b94b4;
}

.notification-item-unread {
  background: rgba(64, 158, 255, 0.08);
}

.notification-item-clickable {
  cursor: pointer;
}

.notification-item-clickable:hover .notification-item-title {
  color: var(--el-color-primary);
  text-decoration: underline;
}

.removal-detail-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.removal-section-label {
  margin: 0 0 6px;
  font-weight: 600;
  font-size: 13px;
  color: #5f6c97;
}

.removal-post-content {
  margin-top: 4px;
}

.removal-post-text {
  margin: 0;
  font-size: 14px;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

.removal-photos-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.removal-photo-item {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  object-fit: cover;
}

.false-report-trigger {
  text-align: center;
}

.false-report-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.false-report-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.emergency-btn {
  animation: emergency-pulse 2s ease-in-out infinite;
  box-shadow: 0 0 15px rgba(255, 0, 0, 0.5) !important;
  width: 44px !important;
  height: 44px !important;
  background: linear-gradient(135deg, #ff0000, #cc0000) !important;
  border: 2px solid #ff0000 !important;
  transition: all 0.3s ease;
}

.emergency-icon {
  font-size: 28px;
  font-weight: 900;
  color: white;
  line-height: 1;
}

.emergency-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 0 25px rgba(255, 0, 0, 0.8) !important;
  animation: emergency-pulse 1s ease-in-out infinite;
}

@keyframes emergency-pulse {
  0%, 100% {
    box-shadow: 0 0 15px rgba(255, 0, 0, 0.5);
  }
  50% {
    box-shadow: 0 0 30px rgba(255, 0, 0, 0.9), 0 0 40px rgba(255, 0, 0, 0.6);
  }
}

.el-dropdown-link {
  cursor: pointer;
}

.main-content {
  flex: 1;
  display: flex;
}

.app-sidebar {
  width: 260px;
  background: white;
  border-right: 1px solid #e5e5e5;
  overflow-y: auto;
  font-size: var(--base-font-size, 14px);
}

.sidebar-header {
  padding: var(--spacing-unit, 16px);
  border-bottom: 1px solid #f0f0f0;
}

.create-post-btn {
  width: 100%;
  font-size: var(--base-font-size, 14px);
  height: var(--element-height, 40px);
  background-color: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
}

.create-post-btn:hover,
.create-post-btn:focus {
  background-color: color-mix(in srgb, var(--el-color-primary) 80%, black) !important;
  border-color: color-mix(in srgb, var(--el-color-primary) 80%, black) !important;
}

.create-post-btn:active {
  background-color: color-mix(in srgb, var(--el-color-primary) 70%, black) !important;
  border-color: color-mix(in srgb, var(--el-color-primary) 70%, black) !important;
}

.sidebar-menu {
  border: none;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #666;
  transition: all 0.3s;
  font-size: var(--base-font-size, 14px);
  height: var(--element-height, 40px);
  line-height: var(--element-height, 40px);
  padding: 0 var(--spacing-unit, 16px);
  margin: 6px 10px;
  border-radius: 12px;
  border: 1px solid rgba(99, 102, 241, 0.12);
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.08), rgba(14, 165, 233, 0.05));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35);
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: #666;
  transition: color 0.3s;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  color: var(--el-color-primary) !important;
  background-color: color-mix(in srgb, var(--el-color-primary) 10%, transparent) !important;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon) {
  color: var(--el-color-primary) !important;
}

.sidebar-menu :deep(.el-menu-item.is-active),
.sidebar-menu :deep(.el-menu-item.menu-active) {
  color: var(--el-color-primary) !important;
  background-color: color-mix(in srgb, var(--el-color-primary) 10%, transparent) !important;
  transform: translateY(-1px);
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.sidebar-menu :deep(.el-menu-item.is-active .el-icon),
.sidebar-menu :deep(.el-menu-item.menu-active .el-icon) {
  color: var(--el-color-primary) !important;
}

.sidebar-menu :deep(.el-menu-item.menu-nearby-shop),
.sidebar-menu :deep(.el-menu-item.menu-nearby-community) {
  border: 1px solid transparent;
}

.sidebar-menu :deep(.el-menu-item.menu-nearby-shop) {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.14), rgba(14, 165, 233, 0.08));
  border-color: rgba(59, 130, 246, 0.24);
}

.sidebar-menu :deep(.el-menu-item.menu-nearby-community) {
  background: linear-gradient(135deg, rgba(168, 85, 247, 0.14), rgba(99, 102, 241, 0.08));
  border-color: rgba(168, 85, 247, 0.22);
}

.sidebar-menu :deep(.el-menu-item.menu-nearby-shop:hover),
.sidebar-menu :deep(.el-menu-item.menu-nearby-community:hover),
.sidebar-menu :deep(.el-menu-item.menu-nearby-shop.menu-active),
.sidebar-menu :deep(.el-menu-item.menu-nearby-community.menu-active) {
  color: #1e2a4a !important;
  transform: translateY(-1px);
}

.sidebar-menu :deep(.el-menu-item.menu-nearby-shop .el-icon),
.sidebar-menu :deep(.el-menu-item.menu-nearby-community .el-icon) {
  color: #3b4f88 !important;
}

.sidebar-others {
  padding: var(--spacing-unit, 16px);
  border-top: 1px solid #f0f0f0;
}

.sidebar-others :deep(.el-button) {
  color: #666;
  font-size: var(--base-font-size, 14px);
}

.sidebar-others :deep(.el-button:hover) {
  color: #07b981;
}

.others-info-group {
  position: relative;
  display: inline-block;
  width: 100%;
}

.others-info-button {
  position: relative;
  width: 100%;
  padding: 9px 11px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background: rgba(79, 70, 229, 0.9);
  border: none;
  border-radius: 12px;
  outline: none;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s ease;
}

.others-info-button:hover {
  background: rgba(67, 56, 202, 0.9);
}

.others-info-glow {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(168, 85, 247, 0.2), rgba(236, 72, 153, 0.2));
  filter: blur(18px);
  transition: opacity 0.3s;
}

.others-info-group:hover .others-info-glow {
  opacity: 0.75;
}

.others-info-button-content {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
}

.others-info-button-icon {
  width: 14px;
  height: 14px;
}

.app-main {
  flex: 1;
  padding: var(--card-padding, 20px);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  font-size: var(--base-font-size, 14px);
  background: transparent;
}

/* Scrollbar */
.app-sidebar::-webkit-scrollbar {
  width: 6px;
}

.app-sidebar::-webkit-scrollbar-track {
  background: transparent;
}

.app-sidebar::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.app-sidebar::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.app-main::-webkit-scrollbar {
  width: 6px;
}

.app-main::-webkit-scrollbar-track {
  background: transparent;
}

.app-main::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.app-main::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>

<style>
/* Global styles for menu items - not scoped to ensure they override Element Plus defaults */
.sidebar-menu .el-menu-item {
  color: #666 !important;
  transition: all 0.3s !important;
  font-size: var(--base-font-size, 14px) !important;
  margin: 6px 10px !important;
  border-radius: 12px !important;
  border: 1px solid rgba(99, 102, 241, 0.12) !important;
  background: linear-gradient(135deg, rgba(79, 70, 229, 0.08), rgba(14, 165, 233, 0.05)) !important;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.35) !important;
}

.sidebar-menu .el-menu-item .el-icon {
  color: #666 !important;
  transition: color 0.3s !important;
  font-size: calc(var(--base-font-size, 14px) * 1.2) !important;
}

.sidebar-menu .el-menu-item:hover {
  color: var(--el-color-primary) !important;
  background-color: color-mix(in srgb, var(--el-color-primary) 10%, transparent) !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.45) !important;
}

.sidebar-menu .el-menu-item:hover .el-icon {
  color: var(--el-color-primary) !important;
}

.sidebar-menu .el-menu-item.is-active,
.sidebar-menu .el-menu-item.menu-active {
  color: var(--el-color-primary) !important;
  background-color: color-mix(in srgb, var(--el-color-primary) 10%, transparent) !important;
  transform: translateY(-1px) !important;
  box-shadow: 0 8px 18px rgba(79, 70, 229, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.45) !important;
}

.sidebar-menu .el-menu-item.is-active .el-icon,
.sidebar-menu .el-menu-item.menu-active .el-icon {
  color: var(--el-color-primary) !important;
}

/* Dark mode styles */
.dark .app-layout {
  background: #1a1a1a !important;
}

.dark .app-header {
  background: #2a2a2a !important;
  border-bottom-color: #404040 !important;
}

.dark .app-title {
  color: #e5e5e5 !important;
}

.dark .notification-menu-header {
  border-bottom-color: #3f4660;
}

.dark .notification-item-title {
  color: #e6ebff;
}

.dark .notification-item-message {
  color: #b7c1e8;
}

.dark .notification-item-time {
  color: #8f9ac8;
}

.dark .notification-item-unread {
  background: rgba(64, 158, 255, 0.18);
}

.dark .app-sidebar {
  background: #2a2a2a !important;
  border-right-color: #404040 !important;
}

.dark .sidebar-header {
  border-bottom-color: #404040 !important;
}

.dark .sidebar-menu {
  background: #2a2a2a !important;
}

.dark .sidebar-menu .el-menu-item {
  color: #b0b0b0 !important;
  background: transparent !important;
}

.dark .sidebar-menu .el-menu-item .el-icon {
  color: #b0b0b0 !important;
}

.dark .sidebar-menu .el-menu-item:hover {
  color: #07b981 !important;
  background-color: #3a3a3a !important;
}

.dark .sidebar-menu .el-menu-item:hover .el-icon {
  color: #07b981 !important;
}

.dark .sidebar-menu .el-menu-item.is-active,
.dark .sidebar-menu .el-menu-item.menu-active {
  color: #07b981 !important;
  background-color: #3a3a3a !important;
}

.dark .sidebar-menu .el-menu-item.is-active .el-icon,
.dark .sidebar-menu .el-menu-item.menu-active .el-icon {
  color: #07b981 !important;
}

.dark .el-divider {
  border-color: #404040 !important;
}

.dark .sidebar-others {
  border-top-color: #404040 !important;
}

.dark .app-sidebar .sidebar-others .el-button--text {
  color: #b0b0b0 !important;
  background-color: transparent !important;
  border: none !important;
}

.dark .app-sidebar .sidebar-others .el-button--text:not(:hover) {
  background-color: transparent !important;
}

.dark .app-sidebar .sidebar-others .el-button--text:hover {
  color: #07b981 !important;
  background-color: #3a3a3a !important;
}

.dark .app-main {
  background: transparent !important;
  color: #e5e5e5 !important;
}

/* Dark mode for Element Plus components */
.dark .el-card {
  background: #2a2a2a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-card__header {
  color: #fff !important;
  border-bottom-color: #404040 !important;
}

.dark .el-input__wrapper {
  background-color: #3a3a3a !important;
  box-shadow: 0 0 0 1px #404040 inset !important;
}

.dark .el-input__inner {
  color: #e5e5e5 !important;
}

.dark .el-input__inner::placeholder {
  color: #888 !important;
}

.dark .el-button--text {
  color: #e5e5e5 !important;
}

.dark .el-button--text:hover {
  color: #07b981 !important;
}

.dark .el-dropdown-menu {
  background: #2a2a2a !important;
  border-color: #404040 !important;
}

.dark .el-dropdown-menu__item {
  color: #e5e5e5 !important;
}

.dark .el-dropdown-menu__item:hover {
  background: #3a3a3a !important;
  color: #07b981 !important;
}

.dark .el-badge__content {
  background-color: #07b981 !important;
}

.dark .el-icon {
  color: #e5e5e5 !important;
}

.dark .el-textarea__inner {
  background-color: #3a3a3a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-select__wrapper {
  background-color: #3a3a3a !important;
  box-shadow: 0 0 0 1px #404040 inset !important;
}

.dark .el-select-dropdown {
  background: #2a2a2a !important;
  border-color: #404040 !important;
}

.dark .el-select-dropdown__item {
  color: #e5e5e5 !important;
}

.dark .el-select-dropdown__item:hover {
  background: #3a3a3a !important;
}

.dark .el-popper {
  background: #2a2a2a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-dialog {
  background: #2a2a2a !important;
  border-color: #404040 !important;
}

.dark .el-dialog__header {
  color: #fff !important;
  border-bottom-color: #404040 !important;
}

.dark .el-dialog__body {
  color: #e5e5e5 !important;
}

.dark .el-dialog__title {
  color: #fff !important;
}

.dark .el-form-item__label {
  color: #e5e5e5 !important;
}

.dark .el-tag {
  background-color: #3a3a3a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-tag.el-tag--warning {
  background-color: #4a3a2a !important;
  border-color: #e6a23c !important;
  color: #f0a020 !important;
}

.dark .el-tag.el-tag--success {
  background-color: #2a4a3a !important;
  border-color: #07b981 !important;
  color: #07b981 !important;
}

.dark .el-tag.el-tag--info {
  background-color: #2a3a4a !important;
  border-color: #409eff !important;
  color: #409eff !important;
}

.dark .el-tag.el-tag--danger {
  background-color: #4a2a2a !important;
  border-color: #f56c6c !important;
  color: #f56c6c !important;
}

.dark .el-switch__core {
  background-color: #404040 !important;
  border-color: #404040 !important;
}

.dark .el-switch__label {
  color: #e5e5e5 !important;
}

.dark .el-tabs__nav {
  background: transparent !important;
}

.dark .el-tabs__item {
  color: #b0b0b0 !important;
}

.dark .el-tabs__item.is-active {
  color: #07b981 !important;
}

.dark .el-tabs__item:hover {
  color: #07b981 !important;
}

.dark .el-tabs__active-bar {
  background-color: #07b981 !important;
}

.dark .el-radio__label {
  color: #e5e5e5 !important;
}

.dark .el-checkbox__label {
  color: #e5e5e5 !important;
}

.dark .el-radio-button__inner {
  background-color: #3a3a3a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-radio-button__original-radio:checked + .el-radio-button__inner {
  background-color: #07b981 !important;
  border-color: #07b981 !important;
  color: #fff !important;
}

/* Dark mode for general text elements */
.dark h1,
.dark h2,
.dark h3,
.dark h4,
.dark h5,
.dark h6 {
  color: #fff !important;
}

.dark p {
  color: #e5e5e5 !important;
}

.dark span {
  color: #e5e5e5 !important;
}

.dark label {
  color: #e5e5e5 !important;
}

.dark a {
  color: #07b981 !important;
}

.dark a:hover {
  color: #09d69a !important;
}

.dark .el-descriptions__label {
  color: #b0b0b0 !important;
}

.dark .el-descriptions__content {
  color: #e5e5e5 !important;
}

.dark .el-time-picker__editor {
  color: #e5e5e5 !important;
}

.dark .el-picker-panel {
  background-color: #2a2a2a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-date-picker__header-label {
  color: #e5e5e5 !important;
}

.dark .el-picker-panel__icon-btn {
  color: #e5e5e5 !important;
}

.dark .el-date-table th {
  color: #b0b0b0 !important;
}

.dark .el-date-table td {
  color: #e5e5e5 !important;
}

.dark .el-color-picker__panel {
  background-color: #2a2a2a !important;
  border-color: #404040 !important;
}

/* Dark mode for Message/Notification popups */
.dark .el-message {
  background-color: #2a2a2a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-message__content {
  color: #e5e5e5 !important;
}

.dark .el-message.el-message--success {
  background-color: #2a4a3a !important;
  border-color: #07b981 !important;
}

.dark .el-message.el-message--warning {
  background-color: #4a3a2a !important;
  border-color: #e6a23c !important;
}

.dark .el-message.el-message--error {
  background-color: #4a2a2a !important;
  border-color: #f56c6c !important;
}

.dark .el-message.el-message--info {
  background-color: #2a3a4a !important;
  border-color: #409eff !important;
}

.dark .el-notification {
  background-color: #2a2a2a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-notification__title {
  color: #fff !important;
}

.dark .el-notification__content {
  color: #e5e5e5 !important;
}

.dark .el-notification.el-notification--success {
  background-color: #2a4a3a !important;
  border-color: #07b981 !important;
}

.dark .el-notification.el-notification--warning {
  background-color: #4a3a2a !important;
  border-color: #e6a23c !important;
}

.dark .el-notification.el-notification--error {
  background-color: #4a2a2a !important;
  border-color: #f56c6c !important;
}

.dark .el-notification.el-notification--info {
  background-color: #2a3a4a !important;
  border-color: #409eff !important;
}

.dark .el-message-box {
  background-color: #2a2a2a !important;
  border-color: #404040 !important;
}

.dark .el-message-box__title {
  color: #fff !important;
}

.dark .el-message-box__content {
  color: #e5e5e5 !important;
}

.dark .el-message-box__message {
  color: #e5e5e5 !important;
}

.dark .el-button {
  background-color: #3a3a3a !important;
  border-color: #404040 !important;
  color: #e5e5e5 !important;
}

.dark .el-button:hover {
  background-color: #4a4a4a !important;
  border-color: #505050 !important;
  color: #fff !important;
}

.dark .el-button--primary {
  background-color: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
  color: #fff !important;
}

.dark .el-button--primary:hover {
  opacity: 0.8;
}

.dark .el-button--warning {
  background-color: #e6a23c !important;
  border-color: #e6a23c !important;
  color: #fff !important;
}

.dark .el-button--warning:hover {
  background-color: #ebb563 !important;
  border-color: #ebb563 !important;
}

.dark .el-button--success {
  background-color: #07b981 !important;
  border-color: #07b981 !important;
  color: #fff !important;
}

.dark .el-button--success:hover {
  background-color: #09d69a !important;
  border-color: #09d69a !important;
}

.dark .el-button:disabled,
.dark .el-button.is-disabled {
  background-color: #404040 !important;
  border-color: #404040 !important;
  color: #666 !important;
  cursor: not-allowed !important;
}

/* Marketplace-inspired override */
.app-layout {
  background: #f7f8fa !important;
}

.app-header {
  background: #ffffff !important;
  border-bottom: 1px solid #eceef3 !important;
  box-shadow: 0 1px 0 rgba(16, 24, 40, 0.03);
}

.app-title {
  color: #ff6f00 !important;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.header-content {
  min-height: 72px;
  height: auto;
  padding-top: 10px;
  padding-bottom: 10px;
}

.header-left {
  flex: 1 1 auto !important;
  width: auto !important;
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.top-nav {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.top-nav-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 10px;
  color: #4b5563 !important;
  padding: 8px 10px !important;
  height: auto !important;
}

.top-nav-button:hover {
  background: #fff4eb !important;
  color: #ff6f00 !important;
}

.top-nav-active {
  background: #fff0e4 !important;
  color: #ff6f00 !important;
}

.top-nav-active .el-icon,
.top-nav-button:hover .el-icon {
  color: #ff6f00 !important;
}

.main-content {
  display: block !important;
}

.app-main {
  width: 100%;
}

.app-sidebar {
  background: #ffffff !important;
  border-right: 1px solid #eceef3 !important;
}

.create-post-btn {
  background: #ff6f00 !important;
  border-color: #ff6f00 !important;
  border-radius: 10px !important;
  font-weight: 700 !important;
}

.create-post-btn:hover,
.create-post-btn:focus {
  background: #f06500 !important;
  border-color: #f06500 !important;
}

.sidebar-menu,
.sidebar-menu .el-menu {
  background: #ffffff !important;
}

.sidebar-menu .el-menu-item,
.sidebar-menu :deep(.el-menu-item) {
  margin: 4px 8px !important;
  border-radius: 10px !important;
  border: 1px solid transparent !important;
  background: #ffffff !important;
  box-shadow: none !important;
  color: #1f2937 !important;
  transform: none !important;
}

.sidebar-menu .el-menu-item .el-icon,
.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: #4b5563 !important;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu :deep(.el-menu-item:hover) {
  background: #fff4eb !important;
  color: #ff6f00 !important;
}

.sidebar-menu .el-menu-item.is-active,
.sidebar-menu .el-menu-item.menu-active,
.sidebar-menu :deep(.el-menu-item.is-active),
.sidebar-menu :deep(.el-menu-item.menu-active) {
  background: #fff0e4 !important;
  color: #ff6f00 !important;
  border-color: #ffd6b8 !important;
}

.sidebar-menu .el-menu-item.is-active .el-icon,
.sidebar-menu .el-menu-item.menu-active .el-icon,
.sidebar-menu :deep(.el-menu-item.is-active .el-icon),
.sidebar-menu :deep(.el-menu-item.menu-active .el-icon) {
  color: #ff6f00 !important;
}

.others-info-button {
  background: #ff6f00 !important;
  border-radius: 10px !important;
}

.others-info-button:hover {
  background: #f06500 !important;
}

.others-info-glow {
  display: none !important;
}

.app-main {
  background: #f7f8fa !important;
}

.dark .app-layout {
  background: #111827 !important;
}

.dark .app-header,
.dark .app-sidebar {
  background: #111827 !important;
  border-color: #1f2937 !important;
}

.dark .app-main {
  background: #111827 !important;
}

.dark .app-title {
  color: #ff8a33 !important;
}

.dark .top-nav-button {
  color: #d1d5db !important;
}

.dark .top-nav-button:hover {
  background: #1f2937 !important;
  color: #ff8a33 !important;
}

.dark .top-nav-active {
  background: #2b1b0d !important;
  color: #ff9f52 !important;
}

.dark .sidebar-menu,
.dark .sidebar-menu .el-menu {
  background: #111827 !important;
}

.dark .sidebar-menu .el-menu-item,
.dark .sidebar-menu :deep(.el-menu-item) {
  background: #111827 !important;
  color: #d1d5db !important;
}

.dark .sidebar-menu .el-menu-item:hover,
.dark .sidebar-menu :deep(.el-menu-item:hover) {
  background: #1f2937 !important;
  color: #ff8a33 !important;
}

.dark .sidebar-menu .el-menu-item.is-active,
.dark .sidebar-menu .el-menu-item.menu-active,
.dark .sidebar-menu :deep(.el-menu-item.is-active),
.dark .sidebar-menu :deep(.el-menu-item.menu-active) {
  background: #2b1b0d !important;
  border-color: #6b3f16 !important;
  color: #ff9f52 !important;
}

.dark .el-button--primary:disabled,
.dark .el-button--primary.is-disabled {
  background-color: #404040 !important;
  border-color: #404040 !important;
  color: #666 !important;
  opacity: 1 !important;
}

.dark .el-dialog__footer {
  border-top-color: #404040 !important;
}
</style>
