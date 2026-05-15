<template>
  <NuxtLayout name="default">
    <div class="settings-page-shell">
      <div class="settings-aura aura-left"></div>
      <div class="settings-aura aura-right"></div>
      <div class="settings-pattern"></div>
    <div class="settings-container">
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h2>{{ $t('settings') }}</h2>
            <div class="settings-header-actions">
              <el-button size="large" @click="handleCancel">
                {{ $t('cancel') }}
              </el-button>
              <el-button
                type="primary"
                size="large"
                :loading="saving"
                @click="handleSave"
              >
                {{ $t('saveChanges') }}
              </el-button>
            </div>
          </div>
        </template>

        <el-tabs v-model="activeTab" class="settings-tabs">
          <!-- Appearance Tab -->
          <el-tab-pane :label="$t('appearance')" name="appearance">
            <el-form label-position="top" class="settings-form">
              <!-- Theme Color -->
              <el-form-item :label="$t('themeColor')">
                <div class="color-picker-wrapper">
                  <el-color-picker v-model="settingsForm.themeColor" size="large" />
                  <span class="color-value">{{ settingsForm.themeColor }}</span>
                  <div class="color-preview" :style="{ backgroundColor: settingsForm.themeColor }">
                    <el-icon><Check /></el-icon>
                  </div>
                </div>
                <div class="theme-preview-text" :style="{ color: settingsForm.themeColor }">
                  {{ $t('themePreview') }}
                </div>
              </el-form-item>

              <!-- Font Size -->
              <el-form-item :label="$t('fontSize')">
                <div class="font-size-slider-container">
                  <span class="slider-label">{{ $t('small') }}</span>
                  <div class="slider-wrapper">
                    <el-slider 
                      v-model="settingsForm.fontSizeValue" 
                      :min="14" 
                      :max="32" 
                      :step="2"
                      :show-tooltip="true"
                      :format-tooltip="formatFontSizeTooltip"
                      @change="handleFontSizeChange"
                    />
                  </div>
                  <span class="slider-label">{{ $t('extraLarge') }}</span>
                </div>
                <p class="sample-text" :style="{ fontSize: settingsForm.fontSizeValue + 'px' }">
                  {{ $t('fontSizePreview') }}
                </p>
              </el-form-item>

              <!-- Dark Mode -->
              <el-form-item :label="$t('displayMode')">
                <el-switch
                  v-model="settingsForm.darkMode"
                  :active-text="$t('darkMode')"
                  :inactive-text="$t('lightMode')"
                  size="large"
                />
              </el-form-item>

              <!-- Compact Mode -->
              <el-form-item :label="$t('layoutDensity')">
                <el-switch
                  v-model="settingsForm.compactMode"
                  :active-text="$t('compactMode')"
                  :inactive-text="$t('comfortableMode')"
                  size="large"
                />
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- Notifications Tab -->
          <el-tab-pane :label="$t('notifications')" name="notifications">
            <el-form label-position="top" class="settings-form">
              <el-form-item :label="$t('notificationPreferences')">
                <div class="notification-settings">
                  <div class="notification-item">
                    <span class="notification-label">{{ $t('questNotifications') }}</span>
                    <el-switch
                      v-model="settingsForm.notifications.quests"
                      :inactive-text="$t('off')"
                      :active-text="$t('on')"
                      size="large"
                    />
                  </div>
                  <div class="notification-item">
                    <span class="notification-label">{{ $t('messageNotifications') }}</span>
                    <el-switch
                      v-model="settingsForm.notifications.messages"
                      :inactive-text="$t('off')"
                      :active-text="$t('on')"
                      size="large"
                    />
                  </div>
                  <div class="notification-item">
                    <span class="notification-label">{{ $t('postNotifications') }}</span>
                    <el-switch
                      v-model="settingsForm.notifications.posts"
                      :inactive-text="$t('off')"
                      :active-text="$t('on')"
                      size="large"
                    />
                  </div>
                  <div class="notification-item">
                    <span class="notification-label">{{ $t('friendNotifications') }}</span>
                    <el-switch
                      v-model="settingsForm.notifications.friends"
                      :inactive-text="$t('off')"
                      :active-text="$t('on')"
                      size="large"
                    />
                  </div>
                  <div class="notification-item">
                    <span class="notification-label">{{ $t('systemNotifications') }}</span>
                    <el-switch
                      v-model="settingsForm.notifications.system"
                      :inactive-text="$t('off')"
                      :active-text="$t('on')"
                      size="large"
                    />
                  </div>
                </div>
              </el-form-item>

              <!-- Notification Sound -->
              <el-form-item :label="$t('notificationSound')">
                <div class="notification-item">
                  <span class="notification-label">{{ $t('notificationSound') }}</span>
                  <el-switch
                    v-model="settingsForm.notificationSound"
                    :inactive-text="$t('off')"
                    :active-text="$t('on')"
                    size="large"
                  />
                </div>
              </el-form-item>

              <!-- Email Notifications -->
              <el-form-item :label="$t('emailNotifications')">
                <div class="notification-item">
                  <span class="notification-label">{{ $t('emailNotifications') }}</span>
                  <el-switch
                    v-model="settingsForm.emailNotifications"
                    :inactive-text="$t('off')"
                    :active-text="$t('on')"
                    size="large"
                  />
                </div>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- Language & Region Tab -->
          <el-tab-pane :label="$t('languageAndRegion')" name="language">
            <el-form label-position="top" class="settings-form">
              <el-form-item :label="$t('language')">
                <el-select v-model="settingsForm.language" size="large" @change="handleLanguageChange">
                  <el-option :label="$t('languageEnglish')" value="en">
                    <span>{{ $t('languageEnglishWithFlag') }}</span>
                  </el-option>
                  <el-option :label="$t('languageChineseTraditional')" value="zh">
                    <span>{{ $t('languageChineseTraditionalWithFlag') }}</span>
                  </el-option>
                </el-select>
              </el-form-item>

              <el-form-item :label="$t('timeFormat')">
                <el-radio-group v-model="settingsForm.timeFormat" size="large">
                  <el-radio-button label="12h">12-{{ $t('hour') }}</el-radio-button>
                  <el-radio-button label="24h">24-{{ $t('hour') }}</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item :label="$t('dateFormat')">
                <el-select v-model="settingsForm.dateFormat" size="large">
                  <el-option label="MM/DD/YYYY" value="MM/DD/YYYY" />
                  <el-option label="DD/MM/YYYY" value="DD/MM/YYYY" />
                  <el-option label="YYYY-MM-DD" value="YYYY-MM-DD" />
                </el-select>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- Privacy & Security Tab -->
          <el-tab-pane :label="$t('privacyAndSecurity')" name="privacy">
            <el-form label-position="top" class="settings-form">
              <el-form-item :label="$t('profileVisibility')">
                <el-radio-group v-model="settingsForm.profileVisibility" size="large">
                  <el-radio label="public">{{ $t('public') }}</el-radio>
                  <el-radio label="friends">{{ $t('friendsOnly') }}</el-radio>
                  <el-radio label="private">{{ $t('private') }}</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item :label="$t('showOnlineStatus')">
                <el-radio-group v-model="settingsForm.onlineStatus" size="large">
                  <el-radio-button label="online">{{ $t('online') }}</el-radio-button>
                  <el-radio-button label="busy">{{ $t('busy') }}</el-radio-button>
                  <el-radio-button label="offline">{{ $t('offline') }}</el-radio-button>
                  <el-radio-button label="hidden">{{ $t('hidden') }}</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item :label="$t('allowMessages')">
                <el-radio-group v-model="settingsForm.allowMessages" size="large">
                  <el-radio label="everyone">{{ $t('everyone') }}</el-radio>
                  <el-radio label="friends">{{ $t('friendsOnly') }}</el-radio>
                  <el-radio label="none">{{ $t('noOne') }}</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane
            v-if="isAdminUser && secretSettingUnlocked"
            :label="$t('secret')"
            name="secret"
          >
            <el-form label-position="top" class="settings-form">
              <el-form-item :label="$t('deleteAllPostsTitle')">
                <p class="secret-warning-text">{{ $t('secretDeleteAllPostsWarning') }}</p>
                <div class="secret-actions">
                  <el-button type="danger" size="large" @click="deleteAllPostsFromSecret">
                    {{ $t('deleteAllPosts') }}
                  </el-button>
                  <el-button size="large" @click="hideSecretSetting">
                    {{ $t('hide') }}
                  </el-button>
                </div>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

      </el-card>
    </div>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import { applyCompactMode, applyDarkMode, applyFontSize, applyThemeColor } from '@/utils/theme'
import { updateMyPresence } from '~/api/chat'

const router = useRouter()
const { t, locale, setLocale } = useI18n()
const saving = ref(false)
const activeTab = ref('appearance')
const isAdminUser = ref(false)
const secretSettingUnlocked = ref(false)

const settingsForm = reactive({
  themeColor: '#409EFF',
  fontSize: 'medium',
  fontSizeValue: 18,
  darkMode: false,
  compactMode: false,
  notifications: {
    quests: true,
    messages: true,
    posts: false,
    friends: true,
    system: true
  },
  notificationSound: true,
  emailNotifications: false,
  language: 'en',
  timeFormat: '12h',
  dateFormat: 'MM/DD/YYYY',
  profileVisibility: 'public',
  onlineStatus: 'online',
  allowMessages: 'everyone'
})

const formatFontSizeTooltip = (value: number) => {
  return `${value}px`
}

const handleFontSizeChange = (value: number) => {
  applyFontSize(value)
}

// Watch theme color changes and apply immediately
watch(() => settingsForm.themeColor, (newColor) => {
  if (newColor) {
    applyThemeColor(newColor)
  }
})

// Watch font size value changes and apply immediately
watch(() => settingsForm.fontSizeValue, (newSize) => {
  if (newSize) {
    applyFontSize(newSize)
  }
})

// Watch compact mode changes and apply immediately
watch(() => settingsForm.compactMode, (isCompact) => {
  applyCompactMode(isCompact)
})

// Watch dark mode changes and apply immediately
watch(() => settingsForm.darkMode, (isDark) => {
  applyDarkMode(isDark)
})

// Hide Secret tab when user navigates away from it
watch(activeTab, (newTab, oldTab) => {
  if (oldTab === 'secret' && newTab !== 'secret') {
    secretSettingUnlocked.value = false
    localStorage.removeItem('adminSecretSettingUnlocked')
  }
})

// Load settings from localStorage on mount
onMounted(() => {
  const savedSettings = localStorage.getItem('userSettings')
  if (savedSettings) {
    const parsed = JSON.parse(savedSettings)
    if (!parsed.onlineStatus && typeof parsed.showOnlineStatus === 'boolean') {
      parsed.onlineStatus = parsed.showOnlineStatus ? 'online' : 'hidden'
    }
    Object.assign(settingsForm, parsed)
  }
  
  // Apply saved settings
  applyThemeColor(settingsForm.themeColor)
  applyFontSize(settingsForm.fontSizeValue)
  applyCompactMode(settingsForm.compactMode)
  applyDarkMode(settingsForm.darkMode)
  settingsForm.language = locale.value as string

  syncSecretSettingState()
  window.addEventListener('app:secret-setting-unlocked', syncSecretSettingState)
  window.addEventListener('storage', syncSecretSettingState)
})

onUnmounted(() => {
  window.removeEventListener('app:secret-setting-unlocked', syncSecretSettingState)
  window.removeEventListener('storage', syncSecretSettingState)
})

const handleLanguageChange = async (newLang: string) => {
  try {
    // Update settingsForm
    settingsForm.language = newLang
    
    // Save language to localStorage first
    localStorage.setItem('userLanguage', newLang)
    
    // Update locale - this triggers i18n to switch languages (use setLocale to load lazy messages)
    await setLocale(newLang)
    
    // Also save the entire settings to persist the language change
    settingsForm.language = newLang
    localStorage.setItem('userSettings', JSON.stringify(settingsForm))
    
    // Wait for next tick to ensure locale is updated
    await nextTick()
    
    // Show success message with the new language
    ElMessage.success(t('languageChanged'))
    
    // Reload page to ensure all nested components and Element Plus components update
    setTimeout(() => {
      window.location.reload()
    }, 800)
  } catch (error) {
    console.error('Error changing language:', error)
    ElMessage.error(t('languageChangeFailed'))
  }
}

const handleSave = async () => {
  saving.value = true

  try {
    let currentEmail = ''
    try {
      const profileRaw = localStorage.getItem('userProfile')
      const profile = profileRaw ? JSON.parse(profileRaw) : null
      currentEmail = String(profile?.email || '').toLowerCase()
    } catch {
      currentEmail = ''
    }

    if (currentEmail) {
      const presenceStatus = settingsForm.onlineStatus === 'hidden' ? 'offline' : settingsForm.onlineStatus
      localStorage.setItem(`userPresence:${currentEmail}`, JSON.stringify({
        status: presenceStatus,
        lastActiveAt: Date.now()
      }))
      await updateMyPresence(presenceStatus)
    }

    // Save to localStorage
    localStorage.setItem('userSettings', JSON.stringify(settingsForm))

    ElMessage.success(t('settingsSaved'))

    // Redirect back to previous page or home
    router.push('/home')
  } catch (error) {
    console.error('Error saving settings:', error)
    ElMessage.error(t('settingsSaveFailed'))
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.back()
}

const syncSecretSettingState = () => {
  const profileRaw = localStorage.getItem('userProfile')
  let email = ''
  if (profileRaw) {
    try {
      const parsed = JSON.parse(profileRaw)
      email = String(parsed?.email || '').toLowerCase()
    } catch {
      email = ''
    }
  }

  isAdminUser.value = email === 'admin@gmail.com'
  secretSettingUnlocked.value = isAdminUser.value && localStorage.getItem('adminSecretSettingUnlocked') === '1'

  if (!secretSettingUnlocked.value && activeTab.value === 'secret') {
    activeTab.value = 'appearance'
  }
}

const hideSecretSetting = () => {
  localStorage.removeItem('adminSecretSettingUnlocked')
  secretSettingUnlocked.value = false
  activeTab.value = 'appearance'
}

const deleteAllPostsFromSecret = () => {
  ElMessageBox.confirm(
    t('confirmDeleteAllPostsMessage'),
    t('deleteAllPostsTitle'),
    {
      confirmButtonText: t('delete'),
      cancelButtonText: t('cancel'),
      type: 'warning'
    }
  ).then(() => {
    localStorage.setItem('deletedAllPosts', '1')
    localStorage.setItem('userPosts', JSON.stringify([]))
    localStorage.setItem('deletedPostIds', JSON.stringify([]))
    localStorage.removeItem('cachedPosts')
    ElMessage.success(t('allPostsDeletedSuccess'))
  }).catch(() => {
    // no-op
  })
}
</script>

<style scoped>
.settings-page-shell {
  position: relative;
  border-radius: 28px;
  padding: 24px;
  overflow: hidden;
  border: 1px solid rgba(109, 125, 219, 0.28);
  background:
    radial-gradient(140% 120% at 0% 0%, rgba(79, 70, 229, 0.22), rgba(255, 255, 255, 0) 58%),
    radial-gradient(130% 140% at 100% 0%, rgba(6, 182, 212, 0.16), rgba(255, 255, 255, 0) 62%),
    linear-gradient(166deg, rgba(255, 255, 255, 0.96), rgba(242, 247, 255, 0.92));
  box-shadow: 0 40px 72px rgba(39, 52, 130, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.settings-aura {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.aura-left {
  width: 300px;
  height: 300px;
  top: -110px;
  left: -80px;
  background: radial-gradient(circle at 30% 30%, rgba(99, 102, 241, 0.54), rgba(99, 102, 241, 0));
}

.aura-right {
  width: 330px;
  height: 330px;
  bottom: -130px;
  right: -90px;
  background: radial-gradient(circle at 45% 40%, rgba(6, 182, 212, 0.48), rgba(6, 182, 212, 0));
}

.settings-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(99, 102, 241, 0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(99, 102, 241, 0.05) 1px, transparent 1px);
  background-size: 24px 24px;
  mask-image: radial-gradient(circle at 50% 35%, #000 40%, transparent 82%);
}

.settings-container {
  width: 100%;
  max-width: 980px;
  margin: 0 auto;
  padding: 24px;
  position: relative;
  z-index: 1;
}

.settings-card {
  border-radius: 22px;
  border: 1px solid rgba(129, 140, 248, 0.22);
  background:
    radial-gradient(130% 130% at 0% 0%, rgba(99, 102, 241, 0.14), rgba(255, 255, 255, 0) 60%),
    radial-gradient(130% 145% at 100% 0%, rgba(14, 165, 233, 0.12), rgba(255, 255, 255, 0) 62%),
    linear-gradient(155deg, rgba(255, 255, 255, 0.96), rgba(244, 248, 255, 0.94));
  box-shadow: 0 28px 56px rgba(41, 54, 128, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.card-header h2 {
  margin: 0;
  color: #202d5b;
  font-size: 34px;
  font-weight: 600;
  letter-spacing: 0.02em;
  text-shadow: 0 2px 12px rgba(99, 102, 241, 0.2);
}

.settings-tabs {
  margin-top: 22px;
}

.settings-tabs :deep(.el-tabs__header) {
  margin-bottom: 18px;
}

.settings-tabs :deep(.el-tabs__nav-scroll) {
  padding: 6px 8px;
  display: flex;
  justify-content: center;
  border-radius: 10px;
  background: var(--tl-surface);
  box-shadow: 0 0 0 1px var(--tl-border) inset;
}

.settings-tabs :deep(.el-tabs__nav) {
  display: inline-flex;
  justify-content: center;
}

.settings-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.settings-tabs :deep(.el-tabs__item) {
  font-weight: 600;
  letter-spacing: 0.01em;
  text-align: center;
  padding: 6px 18px;
}

.settings-form {
  padding: 22px 0 8px;
}

.settings-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #243265;
  font-size: 19px;
  letter-spacing: 0.01em;
}

.settings-form :deep(.el-input__wrapper),
.settings-form :deep(.el-select__wrapper),
.settings-form :deep(.el-radio-button__inner) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.2) inset, 0 10px 18px rgba(67, 80, 154, 0.1);
}

.settings-form :deep(.el-radio-group .el-radio-button__inner) {
  background: var(--tl-surface) !important;
  color: #2d3a6a !important;
  border-color: #c9d2ff !important;
}

.settings-form :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
  color: #ffffff !important;
  box-shadow: none !important;
}

.color-picker-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.color-value {
  font-family: monospace;
  color: #5a648e;
  font-size: 18px;
}

.color-preview {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 26px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s;
}

.theme-preview-text {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 600;
  transition: color 0.3s;
}

.font-size-slider-container {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 14px;
  border: 1px solid rgba(129, 140, 248, 0.18);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.74), rgba(241, 246, 255, 0.68));
}

.slider-wrapper {
  flex: 1;
  min-width: 500px;
  max-width: 800px;
}

.slider-label {
  font-size: 18px;
  color: #5a648e;
  white-space: nowrap;
  font-weight: 500;
  flex-shrink: 0;
}

.font-size-slider-container :deep(.el-slider) {
  width: 100%;
}

.font-size-slider-container :deep(.el-slider__runway) {
  height: 12px;
  border-radius: 6px;
}

.font-size-slider-container :deep(.el-slider__bar) {
  height: 12px;
  border-radius: 6px;
}

.font-size-slider-container :deep(.el-slider__button) {
  width: 32px;
  height: 32px;
  border-width: 3px;
}

.font-size-slider-container :deep(.el-slider__button-wrapper) {
  width: 32px;
  height: 32px;
  top: -10px;
}

.sample-text {
  margin-top: 16px;
  padding: 12px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.88), rgba(241, 246, 255, 0.8));
  border: 1px solid rgba(129, 140, 248, 0.16);
  border-radius: 12px;
  transition: font-size 0.3s;
}

.sample-text.font-small {
  font-size: 16px;
}

.sample-text.font-medium {
  font-size: 18px;
}

.sample-text.font-large {
  font-size: 22px;
}

.sample-text.font-xlarge {
  font-size: 26px;
}

.secret-warning-text {
  margin: 0 0 12px;
  color: #7a2432;
  font-size: 16px;
}

.secret-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.notification-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  padding-right: 32px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.86), rgba(241, 246, 255, 0.78));
  border: 1px solid rgba(129, 140, 248, 0.18);
  border-radius: 12px;
  box-shadow: 0 10px 18px rgba(58, 72, 152, 0.08);
  transition: background 0.3s;
  min-width: 400px;
}

.notification-item:hover {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.94), rgba(232, 240, 255, 0.86));
}

.notification-label {
  font-size: 19px;
  font-weight: 500;
  color: #243265;
  flex: 1;
}

.notification-settings :deep(.el-switch__label) {
  color: #333;
  font-weight: 500;
}

.notification-settings :deep(.el-switch__label.is-active) {
  color: var(--el-color-primary) !important;
}

/* Dark mode styles */
:global(.dark) .notification-item {
  background: #2a2a2a;
}

:global(.dark) .notification-item:hover {
  background: #333;
}

:global(.dark) .notification-label {
  color: #e5e5e5;
}

:global(.dark) .notification-settings :deep(.el-switch__label) {
  color: #e5e5e5;
}

:global(.dark) .notification-settings :deep(.el-switch__label.is-active) {
  color: var(--el-color-primary) !important;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 24px;
  border-top: 1px solid rgba(129, 140, 248, 0.2);
  margin-top: 20px;
}

.form-actions :deep(.el-button) {
  border-radius: 12px;
  min-width: 130px;
}

@media (max-width: 768px) {
  .settings-container {
    padding: 10px;
  }

  .card-header h2 {
    font-size: 20px;
  }

  .font-size-group {
    flex-wrap: wrap;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions .el-button {
    width: 100%;
  }
}

/* Dark Mode Styles */
:global(.dark) .settings-card {
  background:
    radial-gradient(130% 130% at 0% 0%, rgba(79, 70, 229, 0.35), rgba(16, 22, 45, 0.1) 60%),
    radial-gradient(130% 145% at 100% 0%, rgba(14, 165, 233, 0.2), rgba(16, 22, 45, 0.08) 62%),
    linear-gradient(160deg, rgba(17, 22, 45, 0.96), rgba(24, 31, 60, 0.94)) !important;
  border-color: rgba(129, 140, 248, 0.38) !important;
  color: #e5e5e5 !important;
  box-shadow: 0 30px 58px rgba(2, 6, 20, 0.64), inset 0 1px 0 rgba(168, 186, 255, 0.16);
}

:global(.dark) .settings-page-shell {
  border-color: rgba(129, 140, 248, 0.45);
  background:
    radial-gradient(145% 125% at 0% 0%, rgba(79, 70, 229, 0.46), rgba(10, 16, 34, 0.08) 58%),
    radial-gradient(135% 150% at 100% 0%, rgba(6, 182, 212, 0.24), rgba(10, 16, 34, 0.08) 62%),
    linear-gradient(165deg, rgba(12, 18, 38, 0.98), rgba(20, 28, 56, 0.95));
  box-shadow: 0 44px 80px rgba(1, 5, 15, 0.72), inset 0 1px 0 rgba(169, 188, 255, 0.2);
}

:global(.dark) .settings-pattern {
  background-image: linear-gradient(rgba(129, 140, 248, 0.08) 1px, transparent 1px), linear-gradient(90deg, rgba(129, 140, 248, 0.08) 1px, transparent 1px);
}

:global(.dark) .card-header h2 {
  color: #e5e5e5 !important;
}

:global(.dark) .sample-text {
  background: linear-gradient(150deg, rgba(36, 42, 74, 0.88), rgba(24, 31, 58, 0.84)) !important;
  border-color: rgba(129, 140, 248, 0.32) !important;
  color: #e5e5e5 !important;
}

:global(.dark) .slider-label {
  color: #b8c3ed;
}

:global(.dark) .theme-preview-text {
  opacity: 0.9;
}

:global(.dark) .notification-settings :deep(.el-switch__label) {
  color: #e5e5e5 !important;
}

:global(.dark) .notification-settings :deep(.el-switch__label.is-active) {
  color: var(--el-color-primary) !important;
}

:global(.dark) .notification-item {
  background: linear-gradient(150deg, rgba(36, 42, 74, 0.88), rgba(24, 31, 58, 0.84)) !important;
  border-color: rgba(129, 140, 248, 0.32) !important;
  box-shadow: 0 12px 22px rgba(2, 6, 20, 0.5);
}

:global(.dark) .notification-item:hover {
  background: linear-gradient(150deg, rgba(45, 52, 92, 0.9), rgba(31, 39, 70, 0.88)) !important;
}

:global(.dark) .notification-label {
  color: #e8eeff !important;
}

:global(.dark) .form-actions {
  border-top-color: rgba(129, 140, 248, 0.3) !important;
}

:global(.dark) .settings-form :deep(.el-input__wrapper),
:global(.dark) .settings-form :deep(.el-select__wrapper),
:global(.dark) .settings-form :deep(.el-radio-button__inner) {
  background: rgba(16, 22, 44, 0.88) !important;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.38) inset, 0 10px 18px rgba(1, 5, 15, 0.54) !important;
}

:global(.dark) .font-size-slider-container {
  border-color: rgba(129, 140, 248, 0.32);
  background: linear-gradient(150deg, rgba(36, 42, 74, 0.84), rgba(24, 31, 58, 0.8));
}

.settings-page-shell {
  backdrop-filter: blur(14px);
}

.settings-page-shell::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  pointer-events: none;
}

.card-header h2 {
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 20px;
}

.settings-tabs :deep(.el-tabs__item) {
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-size: 12px;
}

.notification-item,
.font-size-slider-container,
.sample-text,
.settings-card {
  backdrop-filter: blur(8px);
}

.form-actions :deep(.el-button) {
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

:global(.dark) .settings-page-shell {
  backdrop-filter: blur(18px);
}

:global(.dark) .settings-page-shell::before {
  border-color: rgba(171, 185, 255, 0.28);
}

:global(.dark) .card-header h2 {
  text-shadow: 0 0 18px rgba(129, 140, 248, 0.38);
}

/* Marketplace-inspired override */
.settings-aura,
.settings-pattern {
  display: none !important;
}

.settings-page-shell {
  padding: 0 !important;
  border: none !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  backdrop-filter: none !important;
}

.settings-page-shell::before,
.settings-page-shell::after {
  display: none !important;
}

.settings-container {
  max-width: 1020px !important;
  padding: 0 !important;
}

.settings-card,
.notification-item,
.font-size-slider-container,
.sample-text {
  background: var(--tl-surface) !important;
  border: 1px solid var(--tl-border) !important;
  border-radius: 12px !important;
  box-shadow: var(--tl-shadow) !important;
  backdrop-filter: none !important;
}

.card-header h2 {
  text-transform: none !important;
  letter-spacing: 0 !important;
  font-size: 28px !important;
  color: var(--tl-text) !important;
  text-shadow: none !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.settings-header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.settings-tabs :deep(.el-tabs__item) {
  text-transform: none !important;
  letter-spacing: 0 !important;
  font-size: 14px !important;
}

.settings-tabs :deep(.el-tabs__item.is-active) {
  color: #ff6f00 !important;
}

.settings-tabs :deep(.el-tabs__active-bar) {
  background: #ff6f00 !important;
}

.settings-form :deep(.el-form-item__label) {
  font-size: 15px !important;
  color: var(--tl-text) !important;
}

.settings-form :deep(.el-input__wrapper),
.settings-form :deep(.el-select__wrapper),
.settings-form :deep(.el-radio-button__inner) {
  background: var(--tl-surface) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
  border-radius: 10px !important;
}

.settings-form :deep(.el-switch__label) {
  color: var(--tl-text-muted) !important;
  font-weight: 600;
}

.settings-form :deep(.el-switch__label.is-active) {
  color: var(--el-color-primary) !important;
}

.settings-form :deep(.el-radio-button__inner) {
  color: var(--tl-text) !important;
  font-weight: 600;
}

.settings-form :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  color: #ffffff !important;
  background: var(--el-color-primary) !important;
  border-color: var(--el-color-primary) !important;
  box-shadow: none !important;
}

.notification-settings :deep(.el-switch__label) {
  color: var(--tl-text) !important;
  font-weight: 600;
}

.form-actions :deep(.el-button) {
  text-transform: none !important;
  letter-spacing: 0 !important;
}

.form-actions :deep(.el-button--primary) {
  background: #ff6f00 !important;
  border-color: #ff6f00 !important;
}

.form-actions :deep(.el-button--primary:hover) {
  background: #f06500 !important;
  border-color: #f06500 !important;
}

:global(.dark) .settings-form :deep(.el-input__wrapper),
:global(.dark) .settings-form :deep(.el-select__wrapper),
:global(.dark) .settings-form :deep(.el-radio-button__inner) {
  background: var(--tl-bg) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
}

:global(.dark) .settings-form :deep(.el-switch__label) {
  color: var(--theme-text-muted) !important;
}

:global(.dark) .settings-form :deep(.el-switch__label.is-active) {
  color: var(--theme-text) !important;
}

:global(.dark) .settings-form :deep(.el-radio-button__inner) {
  color: #e5e7eb !important;
  background: #1f2937 !important;
  border-color: #475569 !important;
}

:global(.dark) .settings-tabs :deep(.el-tabs__item.is-active) {
  color: #ff9f52 !important;
}

:global(.dark) .settings-tabs :deep(.el-tabs__nav-scroll) {
  background: var(--tl-bg) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
}

:global(.dark) .settings-tabs :deep(.el-tabs__active-bar) {
  background: #ff9f52 !important;
}

@media (max-width: 1200px) {
  .settings-container {
    padding: 16px;
  }

  .card-header {
    flex-wrap: wrap;
    align-items: flex-start;
  }

  .settings-header-actions {
    width: 100%;
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .slider-wrapper {
    min-width: 0;
  }
}

@media (max-width: 1024px) {
  .font-size-slider-container {
    flex-wrap: wrap;
  }

  .notification-item {
    min-width: 0;
    padding-left: 14px;
    padding-right: 14px;
  }

  .notification-label {
    font-size: 17px;
  }
}

@media (max-width: 768px) {
  .settings-header-actions .el-button {
    width: 100%;
  }

  .notification-item {
    flex-wrap: wrap;
    gap: 8px;
  }
}

/* ============ TABLET BREAKPOINT (481-1024px) ============ */
@media (max-width: 1024px) {
  .settings-container {
    padding: 14px;
  }

  .settings-card {
    padding: 14px;
  }

  .settings-card h3 {
    font-size: 16px;
  }

  .el-form :deep(.el-button) {
    height: 44px;
    min-width: 100px;
  }

  .el-slider {
    padding: 14px 0;
  }
}

/* ============ PHONE BREAKPOINT (≤480px) ============ */
@media (max-width: 480px) {
  .settings-page {
    padding: 8px;
  }

  .settings-container {
    padding: 12px;
    gap: 12px;
  }

  .settings-header {
    flex-direction: column;
    gap: 10px;
    padding: 12px;
  }

  .settings-header h1 {
    font-size: 18px;
    margin: 0;
  }

  .settings-header-actions {
    width: 100%;
    gap: 8px;
  }

  .settings-header-actions .el-button {
    width: 100%;
    height: 44px;
    font-size: 12px;
  }

  .settings-card {
    padding: 12px;
    border-radius: 12px;
    margin-bottom: 12px;
  }

  .settings-card h3 {
    font-size: 14px;
    margin: 0 0 12px;
  }

  .settings-item {
    flex-direction: column;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid #f0f0f0;
  }

  .settings-item:last-child {
    border-bottom: none;
  }

  .settings-item-label {
    font-size: 13px;
  }

  .settings-item-description {
    font-size: 11px;
    color: #999;
  }

  .settings-item-control {
    width: 100%;
  }

  .font-size-slider-container {
    flex-direction: column;
    gap: 12px;
  }

  .font-size-label {
    font-size: 13px;
  }

  .el-slider {
    padding: 12px 0;
  }

  .el-slider :deep(.el-slider__button) {
    width: 20px;
    height: 20px;
  }

  .color-picker-wrapper {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
  }

  .color-option {
    width: 44px;
    height: 44px;
  }

  .notification-item {
    flex-direction: column;
    gap: 10px;
    padding: 12px 0;
  }

  .notification-label {
    font-size: 13px;
  }

  .el-switch {
    height: 30px;
    min-width: 52px;
  }

  .el-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  .el-form :deep(.el-button) {
    height: 44px;
    width: 100%;
    font-size: 13px;
  }

  .el-form :deep(.el-button--primary) {
    height: 48px;
    font-weight: 600;
  }

  .theme-color-grid {
    grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
    gap: 10px;
  }

  .theme-color-item {
    width: 100%;
    aspect-ratio: 1;
    border-radius: 8px;
  }
}

/* ============ EXTRA SMALL DEVICES (≤360px) ============ */
@media (max-width: 360px) {
  .settings-container {
    padding: 10px;
  }

  .settings-card {
    padding: 10px;
  }

  .settings-header h1 {
    font-size: 16px;
  }

  .settings-card h3 {
    font-size: 13px;
  }

  .settings-header-actions .el-button {
    height: 40px;
    font-size: 11px;
  }

  .settings-item-label {
    font-size: 12px;
  }

  .settings-item-description {
    font-size: 10px;
  }

  .color-option {
    width: 40px;
    height: 40px;
  }

  .notification-label {
    font-size: 12px;
  }

  .el-form :deep(.el-button) {
    height: 40px;
    font-size: 12px;
  }

  .el-form :deep(.el-button--primary) {
    height: 44px;
  }

  .font-size-label {
    font-size: 12px;
  }

  .theme-color-grid {
    grid-template-columns: repeat(auto-fill, minmax(36px, 1fr));
  }
}
</style>
