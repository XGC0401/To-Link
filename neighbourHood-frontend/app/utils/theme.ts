export interface ThemeSettings {
  darkMode?: boolean
  themeColor?: string
  fontSizeValue?: number | string
  compactMode?: boolean
}

const FALLBACK_THEME_COLOR = '#07b981'
const DARK_OVERRIDE_STYLE_ID = 'to-link-runtime-dark-overrides'
let darkOverrideHeadObserver: MutationObserver | null = null

const RUNTIME_DARK_CSS = `
html.dark,
html.dark body {
  color-scheme: dark !important;
}

html.dark body,
html.dark #__nuxt,
html.dark #app,
html.dark #__nuxt #app {
  background: #0f172a !important;
  color: #e5e7eb !important;
}

html.dark .el-popper,
html.dark .el-overlay,
html.dark .el-overlay-dialog,
html.dark .el-dropdown__popper,
html.dark .el-dropdown-menu,
html.dark .el-select-dropdown,
html.dark .el-picker-panel,
html.dark .el-date-picker,
html.dark .el-time-panel,
html.dark .el-tooltip__popper,
html.dark .el-popover,
html.dark .el-dialog,
html.dark .el-dialog__header,
html.dark .el-dialog__body,
html.dark .el-dialog__footer,
html.dark .el-drawer,
html.dark .el-drawer__header,
html.dark .el-drawer__body,
html.dark .el-message-box,
html.dark .el-message,
html.dark .el-notification,
html.dark .el-menu,
html.dark .el-menu-item,
html.dark .el-sub-menu__title,
html.dark .el-table,
html.dark .el-table__inner-wrapper,
html.dark .el-table tr,
html.dark .el-table th,
html.dark .el-table td,
html.dark .el-descriptions,
html.dark .el-descriptions__body,
html.dark .el-descriptions__table,
html.dark .el-descriptions-item__cell {
  background: #182235 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}

html.dark #__nuxt #app .home-page-shell,
html.dark #__nuxt #app .header-content,
html.dark #__nuxt #app .app-header,
html.dark #__nuxt #app .top-nav,
html.dark #__nuxt #app .top-nav-button,
html.dark #__nuxt #app .top-nav-more-dots,
html.dark #__nuxt #app .top-nav-active,
html.dark #__nuxt #app .main-content,
html.dark #__nuxt #app .app-main,
html.dark #__nuxt #app .app-sidebar,
html.dark #__nuxt #app .sidebar-menu,
html.dark #__nuxt #app .sidebar-menu .el-menu,
html.dark #__nuxt #app .sidebar-menu .el-menu-item,
html.dark #__nuxt #app .chat-page-shell,
html.dark #__nuxt #app .friends-page-shell,
html.dark #__nuxt #app .posts-page-shell,
html.dark #__nuxt #app .profile-page-shell,
html.dark #__nuxt #app .settings-page-shell,
html.dark #__nuxt #app .post-page-shell,
html.dark #__nuxt #app .create-post-page-shell,
html.dark #__nuxt #app .welcome-card,
html.dark #__nuxt #app .weather-card,
html.dark #__nuxt #app [class*='page'],
html.dark #__nuxt #app [class*='shell'],
html.dark #__nuxt #app [class*='container'],
html.dark #__nuxt #app [class*='panel'],
html.dark #__nuxt #app [class*='section'],
html.dark #__nuxt #app [class*='card'],
html.dark #__nuxt #app [class*='toolbar'],
html.dark #__nuxt #app [class*='sidebar'],
html.dark #__nuxt #app [class*='header'],
html.dark #__nuxt #app [class*='content'] {
  background: #182235 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}

html.dark #__nuxt #app button,
html.dark #__nuxt #app .el-button,
html.dark #__nuxt #app [class*='button'],
html.dark #__nuxt #app [class*='btn'] {
  background: #1f2c44 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}

html.dark #__nuxt #app button:hover,
html.dark #__nuxt #app .el-button:hover,
html.dark #__nuxt #app [class*='button']:hover,
html.dark #__nuxt #app [class*='btn']:hover {
  background: #24324a !important;
  color: #ffffff !important;
  border-color: #475569 !important;
}

html.dark #__nuxt #app input,
html.dark #__nuxt #app textarea,
html.dark #__nuxt #app select,
html.dark #__nuxt #app .el-input__wrapper,
html.dark #__nuxt #app .el-input__inner,
html.dark #__nuxt #app .el-textarea__inner,
html.dark #__nuxt #app .el-select__wrapper {
  background: #0b1220 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}

html.dark #__nuxt #app .el-card,
html.dark #__nuxt #app .el-dialog,
html.dark #__nuxt #app .el-drawer,
html.dark #__nuxt #app .el-dropdown-menu,
html.dark #__nuxt #app .el-select-dropdown,
html.dark #__nuxt #app .el-popover,
html.dark #__nuxt #app .el-message,
html.dark #__nuxt #app .el-notification,
html.dark #__nuxt #app .el-message-box {
  background: #182235 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}

html.dark body [class*='page'],
html.dark body [class*='shell'],
html.dark body [class*='container'],
html.dark body [class*='panel'],
html.dark body [class*='section'],
html.dark body [class*='layout'],
html.dark body [class*='menu'],
html.dark body [class*='sidebar'],
html.dark body [class*='toolbar'] {
  background: #182235 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}

html.dark body [style*='background: #fff'],
html.dark body [style*='background:#fff'],
html.dark body [style*='background: white'],
html.dark body [style*='background:white'],
html.dark body [style*='background-color: #fff'],
html.dark body [style*='background-color:#fff'],
html.dark body [style*='background-color: white'],
html.dark body [style*='background-color:white'] {
  background: #182235 !important;
  background-color: #182235 !important;
  color: #e5e7eb !important;
  border-color: #334155 !important;
}
`

const fontSizeMap: Record<string, string> = {
  small: '16px',
  medium: '18px',
  large: '22px',
  xlarge: '26px'
}

const ensureBrowser = (): boolean => {
  return typeof window !== 'undefined' && typeof document !== 'undefined'
}

const disconnectDarkOverrideObserver = (): void => {
  if (darkOverrideHeadObserver) {
    darkOverrideHeadObserver.disconnect()
    darkOverrideHeadObserver = null
  }
}

const keepDarkOverrideStyleLast = (): void => {
  const style = document.getElementById(DARK_OVERRIDE_STYLE_ID)
  if (style && document.head.lastElementChild !== style) {
    document.head.appendChild(style)
  }
}

const ensureDarkOverrideObserver = (): void => {
  disconnectDarkOverrideObserver()
  darkOverrideHeadObserver = new MutationObserver(() => {
    keepDarkOverrideStyleLast()
  })
  darkOverrideHeadObserver.observe(document.head, { childList: true })
}

const syncRuntimeDarkOverrides = (enabled: boolean): void => {
  if (!ensureBrowser()) return

  const existing = document.getElementById(DARK_OVERRIDE_STYLE_ID)
  if (!enabled) {
    disconnectDarkOverrideObserver()
    if (existing?.parentNode) {
      existing.parentNode.removeChild(existing)
    }
    return
  }

  if (existing && existing.tagName === 'STYLE') {
    existing.textContent = RUNTIME_DARK_CSS
    keepDarkOverrideStyleLast()
    ensureDarkOverrideObserver()
    return
  }

  const style = document.createElement('style')
  style.id = DARK_OVERRIDE_STYLE_ID
  style.textContent = RUNTIME_DARK_CSS
  document.head.appendChild(style)
  keepDarkOverrideStyleLast()
  ensureDarkOverrideObserver()
}

export const applyThemeColor = (color?: string): void => {
  if (!ensureBrowser()) return
  const nextColor = color || FALLBACK_THEME_COLOR
  document.documentElement.style.setProperty('--el-color-primary', nextColor)
  document.documentElement.style.setProperty('--user-theme-color', nextColor)
}

export const applyFontSize = (size?: number | string): void => {
  if (!ensureBrowser()) return

  let fontSize = fontSizeMap.medium
  if (typeof size === 'number') {
    fontSize = `${size}px`
  } else if (typeof size === 'string' && size.trim()) {
    fontSize = fontSizeMap[size] || size
    if (!fontSize.endsWith('px')) {
      fontSize = fontSizeMap[size] || fontSizeMap.medium
    }
  }

  document.documentElement.style.setProperty('--base-font-size', fontSize)
}

export const applyCompactMode = (isCompact?: boolean): void => {
  if (!ensureBrowser()) return

  if (isCompact) {
    document.documentElement.style.setProperty('--spacing-unit', '8px')
    document.documentElement.style.setProperty('--card-padding', '12px')
    document.documentElement.style.setProperty('--element-height', '32px')
    return
  }

  document.documentElement.style.setProperty('--spacing-unit', '16px')
  document.documentElement.style.setProperty('--card-padding', '20px')
  document.documentElement.style.setProperty('--element-height', '40px')
}

export const applyDarkMode = (isDark?: boolean): void => {
  if (!ensureBrowser()) return

  const darkEnabled = Boolean(isDark)
  const root = document.documentElement
  const body = document.body

  root.classList.toggle('dark', darkEnabled)
  root.setAttribute('data-theme', darkEnabled ? 'dark' : 'light')

  if (body) {
    body.classList.toggle('dark', darkEnabled)
    body.setAttribute('data-theme', darkEnabled ? 'dark' : 'light')
  }

  syncRuntimeDarkOverrides(darkEnabled)
}

export const readSavedThemeSettings = (): ThemeSettings => {
  if (!ensureBrowser()) return {}

  try {
    const raw = localStorage.getItem('userSettings')
    if (!raw) return {}
    const parsed = JSON.parse(raw)
    return typeof parsed === 'object' && parsed ? parsed : {}
  } catch {
    return {}
  }
}

export const applyThemeFromSettings = (settings: ThemeSettings): void => {
  applyThemeColor(settings.themeColor)
  applyFontSize(settings.fontSizeValue)
  applyCompactMode(settings.compactMode)
  applyDarkMode(settings.darkMode)
}

export const loadAndApplyThemeFromStorage = (): ThemeSettings => {
  const settings = readSavedThemeSettings()
  applyThemeFromSettings(settings)
  return settings
}
