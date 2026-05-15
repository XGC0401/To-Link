const DEFAULT_AVATAR = 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg'

const parseJson = (raw: string | null) => {
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return null
  }
}

const getProfileKeyByEmail = (email?: string) => {
  const normalized = String(email || '').trim().toLowerCase()
  return normalized ? `userProfile:${normalized}` : ''
}

export const getLatestAvatarByEmail = (email?: string, fallbackAvatar?: string) => {
  if (typeof window === 'undefined') {
    return fallbackAvatar || DEFAULT_AVATAR
  }

  const key = getProfileKeyByEmail(email)
  if (!key) {
    return fallbackAvatar || DEFAULT_AVATAR
  }

  const profile = parseJson(localStorage.getItem(key))
  const avatar = String(profile?.avatar || '').trim()
  return avatar || fallbackAvatar || DEFAULT_AVATAR
}

export const refreshAvatarInList = <T extends { avatar?: string; email?: string; participantEmail?: string }>(
  list: T[]
): T[] => {
  return list.map((item) => {
    const email = String(item.email || item.participantEmail || '').trim().toLowerCase()
    const nextAvatar = getLatestAvatarByEmail(email, String(item.avatar || ''))
    if (nextAvatar === item.avatar) {
      return item
    }
    return {
      ...item,
      avatar: nextAvatar
    }
  })
}

export const syncConversationAvatarCaches = (currentUserEmail?: string) => {
  if (typeof window === 'undefined') {
    return
  }

  const scopedKey = currentUserEmail
    ? `chatConversations:${String(currentUserEmail).toLowerCase().trim()}`
    : 'chatConversations'

  const keys = [scopedKey, 'chatConversations']
  keys.forEach((key) => {
    const parsed = parseJson(localStorage.getItem(key))
    if (!Array.isArray(parsed)) {
      return
    }
    const refreshed = refreshAvatarInList(parsed)
    localStorage.setItem(key, JSON.stringify(refreshed))
  })
}
