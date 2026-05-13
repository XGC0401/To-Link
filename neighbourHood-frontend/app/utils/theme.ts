export interface ThemeSettings {
  darkMode?: boolean
  themeColor?: string
  fontSizeValue?: number | string
  compactMode?: boolean
}

const FALLBACK_THEME_COLOR = '#07b981'

const fontSizeMap: Record<string, string> = {
  small: '16px',
  medium: '18px',
  large: '22px',
  xlarge: '26px'
}

const ensureBrowser = (): boolean => {
  return typeof window !== 'undefined' && typeof document !== 'undefined'
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
