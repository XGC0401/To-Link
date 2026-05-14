<template>
  <el-container class="app-layout">
    <!-- Header -->
    <el-header class="app-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="app-title" @click="goToHome" style="cursor: pointer;">🔗 {{ $t('appName') }}</h1>
          <!-- Hamburger menu button (adaptive) -->
          <el-button
            class="hamburger-menu-btn"
            :class="{ 'hamburger-menu-btn--visible': shouldShowHamburger }"
            text
            :icon="mobileMenuOpen ? Close : Menu"
            @click="mobileMenuOpen = !mobileMenuOpen"
            :aria-label="$t('menuTitle').replace('\n', ' ')"
          >
            <span class="hamburger-menu-label">{{ t('menuShort') }}</span>
          </el-button>

          <div class="top-nav" :class="{ 'top-nav--collapsed': shouldUseHamburgerOnly }">
            <!-- Visible nav buttons -->
            <el-button
              v-for="(item, index) in visibleNavItems"
              :key="item.id"
              text
              class="top-nav-button"
              :class="{ 'top-nav-active': activeMenuPath.includes(item.path) }"
              @click="handleMenuClick(item.path)"
            >
              <el-icon>
                <component :is="item.icon" />
              </el-icon>
              <span>{{ t(item.labelKey) }}</span>
            </el-button>

            <!-- More dropdown button (shown only if there are hidden items) -->
            <el-dropdown
              v-if="hiddenNavItems.length > 0 && !shouldUseHamburgerOnly"
              trigger="click"
              @command="handleMoreMenuCommand"
            >
              <span class="top-nav-more-dots" :class="{ 'top-nav-active': isMoreMenuActive }">
                <el-icon class="top-nav-more-icon" aria-hidden="true"><Plus /></el-icon>
                <span class="top-nav-more-label">{{ t('topNavMore') }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in hiddenNavItems"
                    :key="item.id"
                    :command="item.path"
                  >
                    <el-icon>
                      <component :is="item.icon" />
                    </el-icon>
                    {{ t(item.labelKey) }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- Hidden width sizer for accurate responsive calculation -->
            <div class="top-nav-sizer" aria-hidden="true">
              <el-button
                v-for="item in navItems"
                :key="`sizer-${item.id}`"
                text
                class="top-nav-button top-nav-button--sizer"
              >
                <el-icon>
                  <component :is="item.icon" />
                </el-icon>
                <span>{{ t(item.labelKey) }}</span>
              </el-button>
              <span class="top-nav-more-dots">
                <el-icon class="top-nav-more-icon"><Plus /></el-icon>
                <span class="top-nav-more-label">{{ t('topNavMore') }}</span>
              </span>
            </div>
          </div>
        </div>
        <div class="header-right">
          <el-badge :value="backpackCount" :hidden="backpackCount === 0" class="backpack-header-badge">
            <el-button
              text
              class="backpack-header-button"
              @click="showBackpackDialog = true"
              :title="$t('myBackpack')"
              :aria-label="$t('myBackpack')"
            >
              <span class="backpack-emoji">🎒</span>
            </el-button>
          </el-badge>
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

    <!-- Mobile Menu Drawer (phone only) -->
    <el-drawer
      v-model="mobileMenuOpen"
      direction="ltr"
      :size="260"
      class="mobile-nav-drawer"
      destroy-on-close
    >
      <template #header>
        <div class="mobile-drawer-title">
          <div class="mobile-drawer-brand-row">
            <img class="mobile-drawer-logo" src="/to-link-logo.svg" alt="To-Link logo" />
          </div>
          <div class="mobile-drawer-menu-label">{{ t('menuShort') }}</div>
        </div>
      </template>

      <div class="mobile-nav-content">
        <!-- Mobile Navigation Items -->
        <div class="mobile-nav-items">
          <el-button
            v-for="item in navItems"
            :key="item.id"
            text
            class="mobile-nav-button"
            :class="{ 'mobile-nav-active': activeMenuPath.includes(item.path) }"
            @click="handleMenuClick(item.path); mobileMenuOpen = false"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ t(item.labelKey) }}</span>
          </el-button>
        </div>

        <!-- Divider -->
        <el-divider />

        <!-- Quick Actions -->
        <div class="mobile-nav-actions">
          <el-button
            text
            class="mobile-nav-action-btn"
            @click="router.push('/profile'); mobileMenuOpen = false"
          >
            <el-icon><User /></el-icon>
            {{ $t('profile') }}
          </el-button>
          <el-button
            text
            class="mobile-nav-action-btn"
            @click="router.push('/settings'); mobileMenuOpen = false"
          >
            <el-icon><Setting /></el-icon>
            {{ $t('settings') }}
          </el-button>
          <el-button
            text
            class="mobile-nav-action-btn"
            @click="handleLanguageToggle"
          >
            <el-icon><Setting /></el-icon>
            {{ locale === 'en' ? '中文' : 'English' }}
          </el-button>
          <el-button
            text
            class="mobile-nav-action-btn"
            type="danger"
            @click="handleLogout(); mobileMenuOpen = false"
          >
            <el-icon><Close /></el-icon>
            {{ $t('logout') }}
          </el-button>
        </div>
      </div>
    </el-drawer>

    <!-- Feedback Dialog -->
    <FeedbackDialog
      v-model="showFeedbackDialog"
      :title="feedbackTitle"
      :feedback-type="feedbackType"
      @submit="handleFeedbackSubmit"
    />

    <BackpackDialog v-model="showBackpackDialog" :items="redeemedRewards" @clear="clearBackpack" />

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
import BackpackDialog from '~/components/BackpackDialog.vue'
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
  Stamp,
  Menu,
  Close,
  Setting
} from '@element-plus/icons-vue'
import { applyCompactMode, applyDarkMode, applyFontSize, applyThemeColor } from '@/utils/theme'

const router = useRouter()
const route = useRoute()
const { locale, t, setLocale } = useI18n()
const switchLocalePath = useSwitchLocalePath()
const searchQuery = ref('')
const activeMenuPath = ref(route.path)
const mobileMenuOpen = ref(false)
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const shouldUseHamburgerOnly = ref(false)

// Responsive navigation items
interface NavItem {
  id: string
  path: string
  labelKey: string
  icon: any
}

const navItems: NavItem[] = [
  { id: 'home', path: '/home', labelKey: 'home', icon: HomeFilled },
  { id: 'posts', path: '/posts', labelKey: 'posts', icon: DocumentCopy },
  { id: 'nearby-shops', path: '/nearbyShops', labelKey: 'nearbyShops', icon: Location },
  { id: 'community', path: '/nearbyCommunity', labelKey: 'nearbyCommunity', icon: OfficeBuilding },
  { id: 'messages', path: '/chat', labelKey: 'messages', icon: ChatLineRound },
  { id: 'friends', path: '/friends', labelKey: 'friends', icon: User },
  { id: 'ai-assistant', path: '/ai-chat', labelKey: 'aiAssistant', icon: Service }
]

const visibleNavItems = ref<NavItem[]>(navItems)
const hiddenNavItems = ref<NavItem[]>([])
let navRecalculateRaf: number | null = null

const isMoreMenuActive = computed(() => {
  return hiddenNavItems.value.some(item => activeMenuPath.value.includes(item.path))
})

const shouldShowHamburger = computed(() => {
  return viewportWidth.value <= 480 || shouldUseHamburgerOnly.value
})

const MIN_VISIBLE_WITH_MORE = 2

// Calculate visible items based on actual rendered widths.
const calculateVisibleItems = () => {
  if (typeof window === 'undefined') return

  const topNav = document.querySelector('.top-nav') as HTMLElement | null
  const headerLeft = document.querySelector('.header-left') as HTMLElement | null
  const appTitle = document.querySelector('.app-title') as HTMLElement | null
  if (!topNav || !headerLeft || !appTitle) return

  const sizerButtons = Array.from(topNav.querySelectorAll('.top-nav-sizer .top-nav-button--sizer')) as HTMLElement[]
  const moreDots = topNav.querySelector('.top-nav-sizer .top-nav-more-dots') as HTMLElement | null

  if (sizerButtons.length !== navItems.length) return

  const headerGap = Number.parseFloat(window.getComputedStyle(headerLeft).gap || '16') || 16
  const titleWidth = Math.ceil(appTitle.getBoundingClientRect().width)
  const containerWidth = Math.max(0, Math.floor(headerLeft.clientWidth - titleWidth - headerGap))
  const widths = sizerButtons.map((el) => Math.ceil(el.getBoundingClientRect().width))
  // Reserve a little extra width for the dropdown trigger wrapper/padding.
  const moreWidth = moreDots ? Math.ceil(moreDots.getBoundingClientRect().width) + 10 : 52
  const gap = 6

  let visibleCount = 0
  let used = 0

  for (let i = 0; i < widths.length; i++) {
    const hasRemaining = i < widths.length - 1
    const nextUsed = used + (visibleCount > 0 ? gap : 0) + widths[i]
    const reservedForMore = hasRemaining ? (gap + moreWidth) : 0
    if (nextUsed + reservedForMore <= containerWidth) {
      visibleCount += 1
      used = nextUsed
    } else {
      break
    }
  }

  visibleCount = Math.min(Math.max(1, visibleCount), navItems.length)
  const hasHiddenItems = visibleCount < navItems.length
  shouldUseHamburgerOnly.value = viewportWidth.value > 480 && hasHiddenItems && visibleCount < MIN_VISIBLE_WITH_MORE
  visibleNavItems.value = navItems.slice(0, visibleCount)
  hiddenNavItems.value = navItems.slice(visibleCount)
}

const scheduleVisibleItemsCalculation = () => {
  if (typeof window === 'undefined') return

  viewportWidth.value = window.innerWidth

  if (navRecalculateRaf !== null) {
    window.cancelAnimationFrame(navRecalculateRaf)
  }

  // Defer measurement until layout settles after resize/flex reflow.
  navRecalculateRaf = window.requestAnimationFrame(() => {
    navRecalculateRaf = window.requestAnimationFrame(() => {
      navRecalculateRaf = null
      calculateVisibleItems()
    })
  })
}

// Setup ResizeObserver for responsive behavior
let resizeObserver: ResizeObserver | null = null
const userProfile = ref<any>(null)
const userSettings = ref<any>(null)
const showFeedbackDialog = ref(false)
const showBackpackDialog = ref(false)
const redeemedRewards = ref<Array<{
  id: string
  name: string
  description: string
  points: number
  redeemedAt: string
}>>([])
const feedbackTitle = ref('')
const feedbackType = ref<'app' | 'community'>('app')

const loadRedeemedRewards = () => {
  try {
    const raw = localStorage.getItem('redeemedRewardsBackpack')
    if (!raw) {
      redeemedRewards.value = []
      return
    }
    const parsed = JSON.parse(raw)
    redeemedRewards.value = Array.isArray(parsed) ? parsed : []
  } catch {
    redeemedRewards.value = []
  }
}

const backpackCount = computed(() => redeemedRewards.value.length)

const clearBackpack = () => {
  redeemedRewards.value = []
  localStorage.setItem('redeemedRewardsBackpack', JSON.stringify(redeemedRewards.value))
  window.dispatchEvent(new CustomEvent('app:backpack-updated'))
  ElMessage.success(t('backpackCleared'))
}

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

const isAdminUser = computed(() => {
  // Check loaded reactive profile first
  const reactiveEmail = String(userProfile.value?.email || '').toLowerCase()
  if (reactiveEmail === 'admin@gmail.com') return true

  // Fallback: read directly from localStorage (covers cases where reactive ref isn't populated yet)
  if (typeof window !== 'undefined') {
    try {
      const raw = localStorage.getItem('userProfile')
      if (raw) {
        const parsed = JSON.parse(raw)
        const storedEmail = String(parsed?.email || '').toLowerCase()
        return storedEmail === 'admin@gmail.com'
      }
    } catch {
      // ignore
    }
  }

  return false
})

const secretVersionClickCount = ref(0)

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
    const parsed = JSON.parse(savedProfile)
    const email = String(parsed?.email || '').toLowerCase()
    if (email) {
      const scopedRaw = localStorage.getItem(`userProfile:${email}`)
      if (scopedRaw) {
        try {
          userProfile.value = JSON.parse(scopedRaw)
          return
        } catch {
          // fallback to global profile
        }
      }
    }
    userProfile.value = parsed
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
  loadRedeemedRewards()
  
  // Listen for storage changes (when profile is updated in another tab or by profile page)
  window.addEventListener('storage', () => {
    loadUserProfile()
    loadUserSettings()
    loadUserLanguage()
    loadRedeemedRewards()
  })

  // Listen for in-page notification events from chat.vue and home.vue
  window.addEventListener('app:notification', handleAppNotificationEvent)
  window.addEventListener('app:backpack-updated', loadRedeemedRewards)

  // Setup responsive navbar
  const topNav = document.querySelector('.top-nav')
  const headerContent = document.querySelector('.header-content')
  if (topNav && typeof ResizeObserver !== 'undefined') {
    resizeObserver = new ResizeObserver(() => {
      scheduleVisibleItemsCalculation()
    })
    resizeObserver.observe(topNav)
    if (headerContent) {
      resizeObserver.observe(headerContent)
    }
  }

  // Initial calculation with a small delay to ensure DOM is fully rendered
  setTimeout(() => {
    scheduleVisibleItemsCalculation()
  }, 100)

  // Also listen to window resize
  window.addEventListener('resize', scheduleVisibleItemsCalculation)
  if (window.visualViewport) {
    window.visualViewport.addEventListener('resize', scheduleVisibleItemsCalculation)
  }
})

onUnmounted(() => {
  window.removeEventListener('app:notification', handleAppNotificationEvent)
  window.removeEventListener('app:backpack-updated', loadRedeemedRewards)
  window.removeEventListener('resize', scheduleVisibleItemsCalculation)
  if (window.visualViewport) {
    window.visualViewport.removeEventListener('resize', scheduleVisibleItemsCalculation)
  }
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
  if (navRecalculateRaf !== null) {
    window.cancelAnimationFrame(navRecalculateRaf)
    navRecalculateRaf = null
  }
})

// Watch route changes and update active menu
watch(() => route.path, (newPath) => {
  activeMenuPath.value = newPath
  // Reload profile and settings when navigating (in case they were updated)
  loadUserProfile()
  loadUserSettings()
}, { immediate: true })

watch(() => locale.value, () => {
  scheduleVisibleItemsCalculation()
})

const handleMenuClick = (path: string) => {
  navigateTo(path)
}

const handleMoreMenuCommand = (path: string) => {
  handleMenuClick(path)
}

const goToHome = () => {
  router.push('/home')
}

const handleLogout = async () => {
  localStorage.removeItem('token')
  localStorage.removeItem('userToken')
  clearAccountScopedStorage()
  await router.push('/')
}

const handleLanguageToggle = async () => {
  const newLocale = locale.value === 'en' ? 'zh' : 'en'
  await setLocale(newLocale)
  localStorage.setItem('userLanguage', newLocale)
}

const clearAccountScopedStorage = () => {
  const keysToClear = [
    'userPosts',
    'cachedPosts',
    'deletedPostIds',
    'deletedAllPosts',
    'postRemovalNotifications',
    'appNotifications',
    'chatConversations',
    'userQuests',
    'adminSecretSettingUnlocked',
    'userProfile'
  ]

  keysToClear.forEach((key) => localStorage.removeItem(key))
}

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    localStorage.removeItem('token')
    localStorage.removeItem('userToken')
    clearAccountScopedStorage()
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
      if (isAdminUser.value) {
        secretVersionClickCount.value += 1
        if (secretVersionClickCount.value >= 3) {
          localStorage.setItem('adminSecretSettingUnlocked', '1')
          window.dispatchEvent(new CustomEvent('app:secret-setting-unlocked'))
          secretVersionClickCount.value = 0
        }
      } else {
        secretVersionClickCount.value = 0
      }
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

    window.dispatchEvent(new CustomEvent('app:emergency-message-sent', {
      detail: { conversationId: 100 }
    }))

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
  flex: 0 0 auto;
}

.backpack-header-button {
  border-radius: 999px;
  width: 34px;
  height: 34px;
  padding: 0 !important;
}

.backpack-header-badge {
  display: inline-flex;
}

.backpack-emoji {
  font-size: 18px;
  line-height: 1;
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

/* Marketplace-inspired override */
.app-layout {
  background: var(--tl-bg) !important;
}

.app-header {
  background: var(--tl-surface) !important;
  border-bottom: 1px solid var(--tl-border) !important;
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
  gap: 12px;
  flex-wrap: nowrap;
}

.header-left {
  flex: 1 1 auto !important;
  width: auto !important;
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
  position: relative;
}

.top-nav {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  flex: 1 1 auto;
  gap: 6px;
  width: 0;
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 2px;
  scrollbar-width: thin;
  position: relative;
}

.top-nav--collapsed {
  position: absolute;
  left: -9999px;
  top: 0;
  visibility: hidden;
  pointer-events: none;
}

.top-nav-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border-radius: 10px;
  color: var(--tl-text-muted) !important;
  padding: 8px 10px !important;
  height: auto !important;
  flex: 0 0 auto;
  white-space: nowrap;
}

.top-nav-icon-button {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--tl-text-muted);
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s ease;
  flex: 0 0 auto;
  font-size: 18px;
}

.top-nav-icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
}

.top-nav-icon-button:hover {
  background: var(--tl-surface-soft);
  color: #ff6f00;
}

.top-nav-icon-active {
  background: var(--tl-surface-active);
  color: #ff6f00;
}

.top-nav-more-dots {
  color: var(--tl-text-muted);
  cursor: pointer;
  padding: 8px 10px;
  border-radius: 10px;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
}

.top-nav-more-icon {
  font-size: 16px;
}

.top-nav-more-label {
  font: inherit;
}

.top-nav-more-dots:hover {
  background: var(--tl-surface-soft);
  color: #ff6f00;
}

.top-nav-more-dots.top-nav-active {
  background: var(--tl-surface-active);
  color: #ff6f00;
}

.top-nav-button span {
  white-space: nowrap;
}

.top-nav-sizer {
  position: absolute;
  top: 0;
  left: 0;
  height: 0;
  overflow: hidden;
  visibility: hidden;
  pointer-events: none;
  white-space: nowrap;
}

.top-nav-button--sizer {
  margin-right: 6px;
}

.more-dots {
  font-size: 20px;
  font-weight: bold;
  letter-spacing: 2px;
  line-height: 1;
}

.top-nav-button:hover {
  background: var(--tl-surface-soft) !important;
  color: #ff6f00 !important;
}

.top-nav-active {
  background: var(--tl-surface-active) !important;
  color: #ff6f00 !important;
}

.top-nav-active .el-icon,
.top-nav-button:hover .el-icon {
  color: #ff6f00 !important;
}

.top-nav::-webkit-scrollbar {
  height: 6px;
}

.top-nav::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.5);
  border-radius: 999px;
}

.top-nav::-webkit-scrollbar-track {
  background: transparent;
}

.main-content {
  display: block !important;
}

.app-main {
  width: 100%;
}

.app-sidebar {
  background: var(--tl-surface) !important;
  border-right: 1px solid var(--tl-border) !important;
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
  background: var(--tl-surface) !important;
}

.sidebar-menu .el-menu-item,
.sidebar-menu :deep(.el-menu-item) {
  margin: 4px 8px !important;
  border-radius: 10px !important;
  border: 1px solid transparent !important;
  background: var(--tl-surface) !important;
  box-shadow: none !important;
  color: var(--tl-text-strong) !important;
  transform: none !important;
}

.sidebar-menu .el-menu-item .el-icon,
.sidebar-menu :deep(.el-menu-item .el-icon) {
  color: var(--tl-text-muted) !important;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu :deep(.el-menu-item:hover) {
  background: var(--tl-surface-soft) !important;
  color: #ff6f00 !important;
}

.sidebar-menu .el-menu-item.is-active,
.sidebar-menu .el-menu-item.menu-active,
.sidebar-menu :deep(.el-menu-item.is-active),
.sidebar-menu :deep(.el-menu-item.menu-active) {
  background: var(--tl-surface-active) !important;
  color: #ff6f00 !important;
  border-color: var(--tl-border-strong) !important;
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
  background: var(--tl-bg) !important;
}

:global(.dark) .el-button--primary:disabled,
:global(.dark) .el-button--primary.is-disabled {
  background-color: #404040 !important;
  border-color: #404040 !important;
  color: #666 !important;
  opacity: 1 !important;
}

:global(.dark) .el-dialog__footer {
  border-top-color: #404040 !important;
}

/* ============ PHONE/TABLET RESPONSIVE STYLES ============ */

/* Hamburger menu button styling */
.hamburger-menu-btn {
  display: none;
  margin-left: 8px;
  width: auto;
  min-width: 38px;
  height: 38px;
  padding: 0 10px;
  gap: 6px;
  border-radius: 10px;
  border: 1px solid rgba(255, 111, 0, 0.22);
  background: var(--tl-surface-soft);
  color: #ff6f00;
  font-size: 18px !important;
  transition: all 0.2s ease;
}

.hamburger-menu-btn--visible {
  display: inline-flex;
}

.hamburger-menu-label {
  font-size: 14px;
  font-weight: 600;
  line-height: 1;
}

.hamburger-menu-btn:hover {
  background: var(--tl-surface-active);
  border-color: rgba(255, 111, 0, 0.4);
}

/* Mobile menu drawer */
.mobile-nav-drawer :deep(.el-drawer) {
  width: 260px !important;
}

.mobile-nav-drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 22px 22px 18px;
  border-bottom: 1px solid var(--tl-border);
  color: var(--tl-text-strong);
  font-weight: 700;
}

.mobile-nav-drawer :deep(.el-drawer__title) {
  white-space: pre-line;
  line-height: 1.2;
}

.mobile-drawer-title {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.mobile-drawer-brand-row {
  display: flex;
  align-items: center;
  min-height: 44px;
}

.mobile-drawer-logo {
  height: 38px;
  width: auto;
  display: block;
}

.mobile-drawer-menu-label {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 0.02em;
  color: var(--tl-text-strong);
  line-height: 1.1;
}

.mobile-nav-drawer :deep(.el-drawer__body) {
  padding: 12px;
  background: linear-gradient(180deg, #fffaf6 0%, #ffffff 100%);
}

:global(.dark) .hamburger-menu-btn {
  background: #1f2937;
  border-color: #374151;
  color: #ff9f52;
}

:global(.dark) .hamburger-menu-btn:hover {
  background: #253247;
  border-color: #4b5563;
}

:global(.dark) .mobile-nav-drawer :deep(.el-drawer__header) {
  border-bottom-color: #374151;
  color: #e5e7eb;
  background: #1f2937;
}

:global(.dark) .mobile-nav-drawer :deep(.el-drawer__body) {
  background: linear-gradient(180deg, #111827 0%, #1f2937 100%);
}

:global(.dark) .mobile-drawer-menu-label {
  color: #e5e7eb;
}

:global(.dark) .mobile-nav-button {
  color: #d1d5db;
  border-color: transparent;
  background: rgba(31, 41, 55, 0.92);
}

:global(.dark) .mobile-nav-button:hover {
  color: #ffb170;
  border-color: rgba(255, 159, 82, 0.4);
  background-color: #253247;
}

:global(.dark) .mobile-nav-button.mobile-nav-active {
  color: #ffb170;
  border-color: rgba(255, 159, 82, 0.45);
  background: linear-gradient(135deg, #2c313d, #253247);
}

:global(.dark) .mobile-nav-action-btn {
  color: #d1d5db;
  border-color: transparent;
  background: rgba(31, 41, 55, 0.92);
}

:global(.dark) .mobile-nav-action-btn:hover {
  color: #ffb170;
  border-color: rgba(255, 159, 82, 0.35);
  background-color: #253247;
}

.mobile-nav-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-nav-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-button {
  width: 100%;
  height: 46px;
  justify-content: flex-start;
  padding: 0 14px;
  font-size: 14px;
  color: #374151;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s;
  border-radius: 10px;
}

.mobile-nav-button .el-icon {
  margin-right: 12px;
  font-size: 18px;
}

.mobile-nav-button:hover {
  color: #ff6f00;
  border-color: rgba(255, 111, 0, 0.28);
  background-color: #fff5ec;
}

.mobile-nav-button.mobile-nav-active {
  color: #ff6f00;
  border-color: rgba(255, 111, 0, 0.35);
  background: linear-gradient(135deg, #fff1e5, #ffe7d4);
  font-weight: 600;
}

.mobile-nav-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-action-btn {
  width: 100%;
  height: 44px;
  justify-content: flex-start;
  padding: 0 14px;
  font-size: 14px;
  color: #4b5563;
  border: 1px solid transparent;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s;
  border-radius: 10px;
}

.mobile-nav-action-btn .el-icon {
  margin-right: 12px;
  font-size: 16px;
}

.mobile-nav-action-btn:hover {
  color: #ff6f00;
  border-color: rgba(255, 111, 0, 0.26);
  background-color: #fff5ec;
}

.mobile-nav-action-btn.is-disabled,
.mobile-nav-action-btn:disabled {
  opacity: 0.6;
}

/* ============ TABLET BREAKPOINT (481-1024px) ============ */
@media (max-width: 1024px) {
  .header-left {
    width: auto;
    flex: 1 1 auto;
    min-width: 0;
  }

  .app-title {
    font-size: 24px;
  }

  .top-nav {
    flex: 1 1 auto;
    width: auto;
    max-width: none;
  }

  .header-right {
    gap: 8px;
  }

  .backpack-header-button,
  .notification-badge {
    width: 40px;
    height: 40px;
    min-width: 40px;
  }

  .el-button :deep(.el-icon) {
    font-size: 18px !important;
  }
}

/* ============ PHONE BREAKPOINT (≤480px) ============ */
@media (max-width: 480px) {
  /* Show hamburger menu */
  .hamburger-menu-btn {
    display: inline-flex;
    min-width: 40px;
    height: 40px;
    padding: 0 10px;
  }

  /* Adjust header layout */
  .header-content {
    height: 56px;
    padding: 0 12px;
    gap: 8px;
  }

  .header-left {
    width: auto;
    flex: 1;
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .app-title {
    font-size: 20px;
    margin: 0;
    white-space: nowrap;
  }

  /* Hide top nav on phone */
  .top-nav {
    display: none;
  }

  /* Hide desktop menu elements on phone */
  .top-nav-button span {
    display: none;
  }

  .assistanceSupport {
    display: none;
  }

  /* Resize header right items for phone */
  .header-right {
    gap: 4px;
  }

  .backpack-header-button {
    width: 36px;
    height: 36px;
    min-width: 36px;
  }

  .notification-badge {
    width: 36px;
    height: 36px;
    min-width: 36px;
  }

  .notification-badge :deep(.el-icon) {
    font-size: 16px !important;
  }

  .emergency-btn {
    width: 36px !important;
    height: 36px !important;
  }

  .emergency-icon {
    font-size: 20px !important;
  }

  /* Reduce header element sizes for mobile */
  .el-avatar {
    width: 32px !important;
    height: 32px !important;
  }

  /* Dropdown menus should be full width on phone */
  .notification-menu {
    min-width: 280px !important;
    max-width: calc(100vw - 24px) !important;
  }

  /* Reduce app main padding */
  .app-main {
    padding: 12px;
  }

  /* Adjust dialog sizes for phone */
  .el-dialog {
    width: 95vw !important;
    max-width: 95vw !important;
  }

  /* Mobile drawer should not have scrollbar overlay */
  .mobile-nav-drawer :deep(.el-drawer) {
    width: 80vw !important;
    max-width: 300px !important;
  }

  /* Dark mode for mobile drawer */
  :global(.dark) .mobile-nav-button {
    color: #b0b0b0;
  }

  :global(.dark) .mobile-nav-button:hover {
    color: #07b981;
    background-color: #3a3a3a;
  }

  :global(.dark) .mobile-nav-button.mobile-nav-active {
    color: #07b981;
    background-color: #2b1b0d;
  }

  :global(.dark) .mobile-nav-action-btn {
    color: #b0b0b0;
  }

  :global(.dark) .mobile-nav-action-btn:hover {
    background-color: #3a3a3a;
  }
}

/* ============ EXTRA SMALL DEVICES (≤360px) ============ */
@media (max-width: 360px) {
  .header-left {
    flex: 1;
  }

  .app-title {
    font-size: 18px;
  }

  .header-right {
    gap: 2px;
  }

  .backpack-header-button,
  .notification-badge,
  .emergency-btn {
    width: 32px !important;
    height: 32px !important;
  }

  .backpack-emoji {
    font-size: 14px;
  }

  .emergency-icon {
    font-size: 16px !important;
  }

  .el-avatar {
    width: 28px !important;
    height: 28px !important;
  }

  .notification-menu {
    min-width: 240px !important;
    max-width: calc(100vw - 16px) !important;
  }

  .app-main {
    padding: 8px;
  }
}
</style>
