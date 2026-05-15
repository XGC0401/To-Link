import { getPreservedAppState, savePreservedAppState } from '~/api/auth'

const PRESERVED_KEY_PATTERNS = [
  /^userProfile(?::.+)?$/,
  /^userSettings$/,
  /^friendsList(?::.+)?$/,
  /^friendSuggestions(?::.+)?$/,
  /^friendDirectory(?::.+)?$/,
  /^ignoredFriendSuggestions(?::.+)?$/,
  /^postLike:\d+$/,
  /^postLikeCount:\d+$/,
  /^postComments:\d+$/,
  /^userQuests$/,
  /^redeemedRewardsBackpack$/,
  /^redeemedRewardsBackpackHistory$/,
  /^userRewardPoints(?::.+)?$/,
  /^chatConversations(?::.+)?$/,
  /^userPresence:.+$/,
  /^appNotifications:.+$/
]

const SENSITIVE_KEYS = new Set([
  'token',
  'userToken'
])

const shouldPreserveKey = (key: string) => {
  if (!key || SENSITIVE_KEYS.has(key)) {
    return false
  }
  return PRESERVED_KEY_PATTERNS.some((pattern) => pattern.test(key))
}

const snapshotState = () => {
  const state: Record<string, string> = {}
  for (let i = 0; i < localStorage.length; i += 1) {
    const key = localStorage.key(i)
    if (!key || !shouldPreserveKey(key)) {
      continue
    }
    const value = localStorage.getItem(key)
    if (value === null) {
      continue
    }
    state[key] = value
  }
  return state
}

const applyStateSnapshot = (state: Record<string, string> | undefined | null) => {
  if (!state || typeof state !== 'object') {
    return
  }

  Object.entries(state).forEach(([key, value]) => {
    if (!shouldPreserveKey(key)) {
      return
    }
    localStorage.setItem(key, value)
  })
}

const getToken = () => {
  const token = localStorage.getItem('token')
  if (token && token !== 'null' && token !== 'undefined') {
    return token
  }

  const legacy = localStorage.getItem('userToken')
  if (legacy && legacy !== 'null' && legacy !== 'undefined') {
    localStorage.setItem('token', legacy)
    return legacy
  }

  return null
}

export default defineNuxtPlugin(() => {
  if (typeof window === 'undefined') {
    return
  }

  let isApplyingServerState = false
  let hasHydrated = false
  let saveTimer: ReturnType<typeof setTimeout> | null = null
  let lastStateSignature = ''

  const buildStateSignature = (state: Record<string, string>) => {
    const sortedEntries = Object.entries(state).sort(([left], [right]) => left.localeCompare(right))
    return JSON.stringify(sortedEntries)
  }

  const scheduleSave = () => {
    if (isApplyingServerState || !getToken()) {
      return
    }

    if (saveTimer) {
      clearTimeout(saveTimer)
    }

    saveTimer = setTimeout(async () => {
      saveTimer = null
      const state = snapshotState()
      const signature = buildStateSignature(state)
      if (signature === lastStateSignature) {
        return
      }
      lastStateSignature = signature
      await savePreservedAppState(state)
    }, 1200)
  }

  const hydrateFromServer = async () => {
    if (hasHydrated || !getToken()) {
      return
    }

    const [error, response] = await getPreservedAppState()
    if (error || !response?.success) {
      return
    }

    isApplyingServerState = true
    try {
      applyStateSnapshot(response.data?.state)
      lastStateSignature = buildStateSignature(snapshotState())
      hasHydrated = true
    } finally {
      isApplyingServerState = false
    }
  }

  window.addEventListener('storage', scheduleSave)
  window.addEventListener('focus', hydrateFromServer)
  window.addEventListener('focus', scheduleSave)
  window.addEventListener('beforeunload', scheduleSave)

  window.setInterval(scheduleSave, 3000)
  hydrateFromServer()
})
