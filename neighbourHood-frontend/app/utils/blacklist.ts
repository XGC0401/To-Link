export interface BlockedPerson {
  id: string
  label: string
  backendUuid?: string
  createdAt: string
}

const BLACKLIST_KEY = 'blockedPeople'

const normalize = (value: unknown) => String(value || '').trim().toLowerCase()

export const loadBlacklist = (): BlockedPerson[] => {
  if (typeof window === 'undefined') {
    return []
  }

  try {
    const raw = localStorage.getItem(BLACKLIST_KEY)
    if (!raw) {
      return []
    }
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) {
      return []
    }
    return parsed
      .filter((item) => item && item.id && item.label)
      .map((item) => ({
        id: String(item.id),
        label: String(item.label),
        backendUuid: item.backendUuid ? String(item.backendUuid) : undefined,
        createdAt: item.createdAt ? String(item.createdAt) : new Date().toISOString()
      }))
  } catch {
    return []
  }
}

export const saveBlacklist = (people: BlockedPerson[]) => {
  if (typeof window === 'undefined') {
    return
  }
  localStorage.setItem(BLACKLIST_KEY, JSON.stringify(people))
}

export const isBlockedByCandidates = (blocked: BlockedPerson[], candidates: unknown[]) => {
  const blockedSet = new Set(blocked.flatMap((person) => [normalize(person.id), normalize(person.label)]))
  return candidates.some((candidate) => blockedSet.has(normalize(candidate)))
}

export const upsertBlockedPerson = (blocked: BlockedPerson[], person: Omit<BlockedPerson, 'createdAt'>) => {
  const personId = normalize(person.id)
  const personLabel = normalize(person.label)

  const exists = blocked.some((item) => {
    const itemId = normalize(item.id)
    const itemLabel = normalize(item.label)
    return itemId === personId || itemLabel === personLabel
  })

  if (exists) {
    return blocked
  }

  return [
    {
      ...person,
      id: String(person.id),
      label: String(person.label),
      backendUuid: person.backendUuid ? String(person.backendUuid) : undefined,
      createdAt: new Date().toISOString()
    },
    ...blocked
  ]
}

export const removeBlockedPerson = (blocked: BlockedPerson[], idOrLabel: string) => {
  const target = normalize(idOrLabel)
  return blocked.filter((item) => normalize(item.id) !== target && normalize(item.label) !== target)
}
