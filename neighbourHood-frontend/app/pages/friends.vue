<template>
  <NuxtLayout name="default">
    <div class="friends-page-shell">
      <div class="friends-light light-a"></div>
      <div class="friends-light light-b"></div>
      <div class="friends-pattern"></div>
    <div class="friends-toolbar">
      <div class="friends-toolbar-search">
        <el-input
          v-model="searchQuery"
          :placeholder="$t('searchFriends')"
          clearable
          style="width: 300px;"
        />
      </div>
      <div class="friends-toolbar-actions">
        <el-select v-model="filterStatus" :placeholder="$t('status')" style="width: 150px;">
          <el-option :label="$t('all')" value="" />
          <el-option :label="$t('online')" value="online" />
          <el-option :label="$t('offline')" value="offline" />
        </el-select>
      </div>
    </div>

    <div class="friends-discover-panel">
      <div class="section-inline-header">
        <h3>{{ $t('discoverPeople') }}</h3>
        <el-input v-model="discoverQuery" :placeholder="$t('searchUsersToAdd')" clearable style="width: 320px;" />
      </div>

      <el-empty v-if="filteredDiscoverUsers.length === 0" :description="$t('noUsersFound')" />
      <div v-else class="discover-grid">
        <el-card v-for="user in filteredDiscoverUsers" :key="`discover-${user.id}`" class="discover-card">
          <div class="discover-content">
            <div class="discover-main">
              <el-avatar :size="52" :src="user.avatar" />
              <div>
                <div class="discover-name">{{ user.name }}</div>
                <div class="discover-bio">{{ user.bio }}</div>
              </div>
            </div>
            <el-button type="primary" plain @click="addFriend(user)">{{ $t('addFriend') }}</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <div class="friends-grid">
      <el-card v-for="friend in filteredFriends" :key="friend.id" class="friend-card">
        <div class="friend-content">
          <div class="friend-avatar-wrapper">
            <el-avatar :size="80" :src="friend.avatar" />
            <span :class="['online-indicator', friend.status]"></span>
          </div>
          
          <div class="friend-info">
            <h3>{{ friend.name }}</h3>
            <p class="friend-status">{{ friend.status === 'online' ? $t('online') : $t('offline') }}</p>
          </div>

          <div class="friend-actions">
            <el-button size="small">
              <template #icon>
                <el-icon><Phone /></el-icon>
              </template>
              {{ $t('call') }}
            </el-button>
            <el-button type="primary" size="small" @click="sendMessage(friend)">
              <template #icon>
                <el-icon><Message /></el-icon>
              </template>
              {{ $t('message') }}
            </el-button>
          </div>

          <p class="friend-bio">{{ friend.bio }}</p>
        </div>
      </el-card>
    </div>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Phone } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t, locale } = useI18n()
const language = computed(() => locale.value as 'en' | 'zh')
const searchQuery = ref('')
const filterStatus = ref('')
const discoverQuery = ref('')

interface Friend {
  id: number | string
  name: string
  avatar: string
  status: 'online' | 'offline'
  bio: string
}

const friends = ref<Friend[]>([
  {
    id: 1,
    name: 'John Doe',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    status: 'online',
    bio: 'Love hiking and outdoor activities'
  },
  {
    id: 2,
    name: 'Jane Smith',
    avatar: 'https://cube.elemecdn.com/3/dc/1ea6beec64f4a146f6f02a42cc5f7.svg',
    status: 'online',
    bio: 'Coffee enthusiast and bookworm'
  },
  {
    id: 3,
    name: 'Mike Johnson',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    status: 'offline',
    bio: 'Tech enthusiast and gamer'
  },
  {
    id: 4,
    name: 'Sarah Williams',
    avatar: 'https://cube.elemecdn.com/3/dc/1ea6beec64f4a146f6f02a42cc5f7.svg',
    status: 'online',
    bio: 'Yoga instructor and wellness coach'
  },
  {
    id: 5,
    name: 'David Brown',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    status: 'offline',
    bio: 'Photographer and travel blogger'
  },
  {
    id: 6,
    name: 'Emily Davis',
    avatar: 'https://cube.elemecdn.com/3/dc/1ea6beec64f4a146f6f02a42cc5f7.svg',
    status: 'online',
    bio: 'Artist and creative designer'
  }
])

const discoverUsers = ref<Friend[]>([
  {
    id: 11,
    name: 'Kevin Lau',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    status: 'offline',
    bio: 'Community volunteer and language buddy'
  },
  {
    id: 12,
    name: 'Wendy Ng',
    avatar: 'https://cube.elemecdn.com/3/dc/1ea6beec64f4a146f6f02a42cc5f7.svg',
    status: 'online',
    bio: 'UI designer, loves neighborhood events'
  },
  {
    id: 13,
    name: 'Alex Wong',
    avatar: 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg',
    status: 'online',
    bio: 'Guitar player and pet lover'
  }
])

const buildGeneratedUsers = (): Friend[] => {
  if (typeof window === 'undefined') {
    return []
  }

  const toStableId = (seed: string) => {
    let hash = 0
    for (let i = 0; i < seed.length; i += 1) {
      hash = ((hash << 5) - hash) + seed.charCodeAt(i)
      hash |= 0
    }
    return Math.abs(hash) + 10000
  }

  const users: Friend[] = []
  for (let i = 0; i < localStorage.length; i += 1) {
    const key = localStorage.key(i)
    if (!key || !key.startsWith('userProfile:')) {
      continue
    }
    try {
      const parsed = JSON.parse(localStorage.getItem(key) || '{}')
      const email = String(parsed?.email || '').trim()
      const name = String(parsed?.name || parsed?.username || email || '').trim()
      if (!name) {
        continue
      }
      users.push({
        id: toStableId(email || key),
        name,
        avatar: String(parsed?.avatar || 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg'),
        status: 'offline',
        bio: String(parsed?.bio || parsed?.status || 'To-Link user')
      })
    } catch {
      // Ignore malformed entries.
    }
  }
  return users
}

const persistFriendsState = () => {
  if (typeof window === 'undefined') {
    return
  }
  localStorage.setItem('friendsList', JSON.stringify(friends.value))
  localStorage.setItem('friendSuggestions', JSON.stringify(discoverUsers.value))
  localStorage.setItem('friendDirectory', JSON.stringify([...friends.value, ...discoverUsers.value]))
}

const loadFriendsState = () => {
  if (typeof window === 'undefined') {
    return
  }

  try {
    const savedFriends = JSON.parse(localStorage.getItem('friendsList') || '[]')
    if (Array.isArray(savedFriends) && savedFriends.length > 0) {
      friends.value = savedFriends
    }
  } catch {
    // Ignore malformed storage.
  }

  try {
    const savedSuggestions = JSON.parse(localStorage.getItem('friendSuggestions') || '[]')
    if (Array.isArray(savedSuggestions) && savedSuggestions.length > 0) {
      discoverUsers.value = savedSuggestions
    }
  } catch {
    // Ignore malformed storage.
  }

  const generatedUsers = buildGeneratedUsers()
  const knownIds = new Set([...friends.value, ...discoverUsers.value].map((item) => String(item.id)))
  for (const user of generatedUsers) {
    if (!knownIds.has(String(user.id))) {
      discoverUsers.value.push(user)
      knownIds.add(String(user.id))
    }
  }

  persistFriendsState()
}

const filteredFriends = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  return friends.value.filter(friend => {
    const matchesSearch = !query || friend.name.toLowerCase().includes(query) || friend.bio.toLowerCase().includes(query)
    const matchesStatus = !filterStatus.value || friend.status === filterStatus.value
    return matchesSearch && matchesStatus
  })
})

const filteredDiscoverUsers = computed(() => {
  const query = discoverQuery.value.toLowerCase().trim()
  const friendIds = new Set(friends.value.map((friend) => String(friend.id)))
  return discoverUsers.value
    .filter((user) => !friendIds.has(String(user.id)))
    .filter((user) => !query || user.name.toLowerCase().includes(query) || user.bio.toLowerCase().includes(query))
})

const addFriend = (user: Friend) => {
  if (friends.value.some((friend) => String(friend.id) === String(user.id))) {
    ElMessage.info(t('alreadyFriends'))
    return
  }
  friends.value.unshift({ ...user, status: user.status || 'offline' })
  discoverUsers.value = discoverUsers.value.filter((item) => String(item.id) !== String(user.id))
  persistFriendsState()
  ElMessage.success(t('friendAdded'))
}

const sendMessage = (friend: Friend) => {
  if (typeof window !== 'undefined') {
    const conversations = JSON.parse(localStorage.getItem('chatConversations') || '[]')
    const existing = conversations.find((item: any) => String(item.id) === String(friend.id))
    if (!existing) {
      conversations.unshift({
        id: friend.id,
        name: friend.name,
        avatar: friend.avatar,
        unread: 0,
        lastMessage: '',
        lastMessageTime: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        messages: []
      })
      localStorage.setItem('chatConversations', JSON.stringify(conversations))
    }
  }
  router.push({ path: '/chat', query: { userId: String(friend.id) } })
}

onMounted(() => {
  loadFriendsState()
})

watch([friends, discoverUsers], () => {
  persistFriendsState()
}, { deep: true })
</script>

<style scoped>
.friends-page-shell {
  position: relative;
  border-radius: 28px;
  padding: 22px;
  overflow: hidden;
  border: 1px solid rgba(109, 125, 219, 0.28);
  background:
    radial-gradient(140% 120% at 0% 0%, rgba(79, 70, 229, 0.22), rgba(255, 255, 255, 0) 58%),
    radial-gradient(130% 140% at 100% 0%, rgba(6, 182, 212, 0.16), rgba(255, 255, 255, 0) 62%),
    linear-gradient(166deg, rgba(255, 255, 255, 0.96), rgba(242, 247, 255, 0.92));
  box-shadow: 0 40px 72px rgba(39, 52, 130, 0.2), inset 0 1px 0 rgba(255, 255, 255, 0.78);
}

.friends-light {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
}

.light-a {
  width: 260px;
  height: 260px;
  top: -90px;
  left: -70px;
  background: radial-gradient(circle at 30% 30%, rgba(99, 102, 241, 0.54), rgba(99, 102, 241, 0));
}

.light-b {
  width: 300px;
  height: 300px;
  bottom: -120px;
  right: -80px;
  background: radial-gradient(circle at 45% 40%, rgba(6, 182, 212, 0.48), rgba(6, 182, 212, 0));
}

.friends-pattern {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image: linear-gradient(rgba(99, 102, 241, 0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(99, 102, 241, 0.05) 1px, transparent 1px);
  background-size: 24px 24px;
  mask-image: radial-gradient(circle at 50% 35%, #000 40%, transparent 82%);
}

.friends-toolbar {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
  align-items: center;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(129, 140, 248, 0.2);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.95), rgba(244, 248, 255, 0.92));
  box-shadow: 0 16px 30px rgba(47, 60, 137, 0.14);
  position: relative;
  z-index: 1;
}

.friends-toolbar-search {
  flex: 1;
}

.friends-toolbar-actions {
  display: flex;
  justify-content: flex-end;
}

.friends-toolbar :deep(.el-input__wrapper),
.friends-toolbar :deep(.el-select__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.2) inset, 0 8px 16px rgba(67, 80, 154, 0.1);
}

.friends-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(300px, 100%), 1fr));
  gap: 22px;
  position: relative;
  z-index: 1;
}

.friends-discover-panel {
  margin-bottom: 20px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid rgba(129, 140, 248, 0.2);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.95), rgba(244, 248, 255, 0.92));
  box-shadow: 0 16px 30px rgba(47, 60, 137, 0.14);
  position: relative;
  z-index: 1;
}

.section-inline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  gap: 12px;
}

.section-inline-header h3 {
  margin: 0;
  color: #1d2850;
}

.discover-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(min(280px, 100%), 1fr));
  gap: 12px;
}

.discover-card {
  border-radius: 14px;
  border: 1px solid rgba(129, 140, 248, 0.18);
}

.discover-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.discover-main {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.discover-name {
  font-weight: 700;
  color: #1d2850;
  line-height: 1.3;
}

.discover-bio {
  font-size: 14px;
  color: #5c6590;
  line-height: 1.4;
}

.friend-card {
  min-width: 0;
  border-radius: 20px;
  border: 1px solid rgba(129, 140, 248, 0.18);
  background:
    radial-gradient(130% 130% at 0% 0%, rgba(99, 102, 241, 0.12), rgba(255, 255, 255, 0) 60%),
    linear-gradient(150deg, rgba(255, 255, 255, 0.96), rgba(245, 249, 255, 0.94));
  box-shadow: 0 20px 36px rgba(47, 60, 137, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.76);
  transition: transform 0.3s, box-shadow 0.3s;
}

.friend-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 26px 44px rgba(47, 60, 137, 0.22);
}

.friend-content {
  padding: 24px 20px;
  text-align: center;
}

.friend-avatar-wrapper {
  position: relative;
  display: inline-block;
  margin-bottom: 12px;
}

.online-indicator {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 3px solid #fff;
  bottom: 0;
  right: 0;
  box-shadow: 0 0 0 4px rgba(15, 23, 42, 0.08);
}

.online-indicator.online {
  background: #67c23a;
}

.online-indicator.offline {
  background: #999;
}

.friend-info h3 {
  margin: 12px 0 4px 0;
  color: #1d2850;
  font-size: 20px;
  letter-spacing: 0.01em;
}

.friend-status {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #6b74a3;
}

.friend-bio {
  margin: 8px 0;
  color: #596289;
  font-size: 17px;
  line-height: 1.5;
}

.friend-actions {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  margin-bottom: 10px;
  justify-content: center;
}

.friend-actions button {
  flex: 1;
  border-radius: 12px;
}

@media (max-width: 1200px) {
  .friends-toolbar {
    flex-wrap: wrap;
    align-items: stretch;
  }

  .friends-toolbar-search,
  .friends-toolbar-actions {
    width: 100%;
  }

  .friends-toolbar :deep(.el-input),
  .friends-toolbar :deep(.el-select) {
    width: 100% !important;
  }
}

@media (max-width: 768px) {
  .friends-grid {
    grid-template-columns: 1fr;
  }

  .friend-actions {
    flex-direction: column;
  }
}

.friends-page-shell :deep(.el-button--primary) {
  background: linear-gradient(145deg, #4f46e5, #6366f1) !important;
  border: 1px solid rgba(177, 188, 255, 0.55) !important;
}

:global(.dark) .friends-toolbar {
  border-color: rgba(129, 140, 248, 0.34);
  background: linear-gradient(150deg, rgba(23, 30, 58, 0.95), rgba(16, 22, 45, 0.92));
  box-shadow: 0 18px 34px rgba(2, 6, 20, 0.56);
}

:global(.dark) .friends-toolbar :deep(.el-input__wrapper),
:global(.dark) .friends-toolbar :deep(.el-select__wrapper) {
  background: rgba(16, 22, 44, 0.88);
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.36) inset, 0 8px 16px rgba(1, 5, 15, 0.52);
}

:global(.dark) .friend-card {
  border-color: rgba(129, 140, 248, 0.3);
  background:
    radial-gradient(130% 130% at 0% 0%, rgba(79, 70, 229, 0.28), rgba(21, 28, 57, 0.1) 60%),
    linear-gradient(155deg, rgba(24, 31, 61, 0.95), rgba(18, 24, 49, 0.92));
  box-shadow: 0 22px 38px rgba(2, 6, 20, 0.58), inset 0 1px 0 rgba(160, 178, 255, 0.14);
}

:global(.dark) .friend-info h3 {
  color: #e8eeff;
}

:global(.dark) .friend-status {
  color: #b8c3ed;
}

:global(.dark) .friend-bio {
  color: #d2dcff;
}

:global(.dark) .discover-name {
  color: #eff6ff;
}

:global(.dark) .discover-bio {
  color: #cbd5e1;
}

:global(.dark) .online-indicator {
  border-color: #1b2244;
  box-shadow: 0 0 0 4px rgba(129, 140, 248, 0.2);
}

:global(.dark) .friends-page-shell {
  border-color: rgba(129, 140, 248, 0.45);
  background:
    radial-gradient(145% 125% at 0% 0%, rgba(79, 70, 229, 0.46), rgba(10, 16, 34, 0.08) 58%),
    radial-gradient(135% 150% at 100% 0%, rgba(6, 182, 212, 0.24), rgba(10, 16, 34, 0.08) 62%),
    linear-gradient(165deg, rgba(12, 18, 38, 0.98), rgba(20, 28, 56, 0.95));
  box-shadow: 0 44px 80px rgba(1, 5, 15, 0.72), inset 0 1px 0 rgba(169, 188, 255, 0.2);
}

:global(.dark) .friends-pattern {
  background-image: linear-gradient(rgba(129, 140, 248, 0.08) 1px, transparent 1px), linear-gradient(90deg, rgba(129, 140, 248, 0.08) 1px, transparent 1px);
}

.friends-page-shell {
  backdrop-filter: blur(14px);
}

.friends-page-shell::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: 26px;
  border: 1px solid rgba(255, 255, 255, 0.42);
  pointer-events: none;
}

.friend-card {
  overflow: hidden;
}

.friend-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(118deg, rgba(255, 255, 255, 0.34), rgba(255, 255, 255, 0) 45%);
  pointer-events: none;
}

.friend-info h3 {
  text-transform: uppercase;
  letter-spacing: 0.05em;
  font-size: 15px;
  font-weight: 700;
}

.friends-page-shell :deep(.el-button) {
  text-transform: uppercase;
  letter-spacing: 0.04em;
  font-size: 12px;
}

:global(.dark) .friends-page-shell {
  backdrop-filter: blur(18px);
}

:global(.dark) .friends-page-shell::before {
  border-color: rgba(171, 185, 255, 0.28);
}

:global(.dark) .friend-card::after {
  background: linear-gradient(118deg, rgba(199, 210, 254, 0.14), rgba(199, 210, 254, 0) 45%);
}

/* Marketplace-inspired override */
.friends-light,
.friends-pattern {
  display: none !important;
}

.friends-page-shell {
  padding: 0 !important;
  border: none !important;
  border-radius: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
  backdrop-filter: none !important;
}

.friends-page-shell::before,
.friends-page-shell::after {
  display: none !important;
}

.friends-toolbar,
.friend-card {
  background: var(--tl-surface) !important;
  border: 1px solid var(--tl-border) !important;
  border-radius: 12px !important;
  box-shadow: var(--tl-shadow) !important;
}

.friend-card:hover {
  transform: none !important;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08) !important;
}

.friend-card::after {
  display: none !important;
}

.friend-info h3 {
  text-transform: none !important;
  letter-spacing: 0 !important;
  font-size: 20px !important;
}

.friends-page-shell :deep(.el-input__wrapper),
.friends-page-shell :deep(.el-select__wrapper) {
  background: var(--tl-surface) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
  border-radius: 10px !important;
}

.friends-page-shell :deep(.el-button--primary) {
  background: #ff6f00 !important;
  border-color: #ff6f00 !important;
}

.friends-page-shell :deep(.el-button--primary:hover) {
  background: #f06500 !important;
  border-color: #f06500 !important;
}

:global(.dark) .friends-page-shell :deep(.el-input__wrapper),
:global(.dark) .friends-page-shell :deep(.el-select__wrapper) {
  background: var(--tl-bg) !important;
  box-shadow: 0 0 0 1px var(--tl-border) inset !important;
}

/* ============ TABLET BREAKPOINT (481-1024px) ============ */
@media (max-width: 1024px) {
  .friends-grid {
    gap: 16px;
  }

  .friend-card {
    padding: 16px;
  }

  .friend-avatar-wrapper :deep(.el-avatar) {
    width: 64px;
    height: 64px;
  }

  .friend-info h3 {
    font-size: 16px;
  }

  .friend-actions {
    gap: 8px;
  }

  .friend-actions .el-button {
    font-size: 13px;
    padding: 6px 12px;
  }
}

/* ============ PHONE BREAKPOINT (≤480px) ============ */
@media (max-width: 480px) {
  .friends-page-shell {
    padding: 8px;
  }

  .friends-toolbar {
    flex-direction: column;
    gap: 10px;
    padding: 12px;
    border-radius: 12px;
    margin-bottom: 12px;
  }

  .friends-toolbar-search,
  .friends-toolbar-actions {
    width: 100%;
  }

  .friends-toolbar :deep(.el-input),
  .friends-toolbar :deep(.el-select) {
    width: 100% !important;
    height: 44px !important;
  }

  .friends-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .friend-card {
    padding: 12px;
    border-radius: 10px;
  }

  .friend-content {
    gap: 12px;
  }

  .friend-avatar-wrapper {
    min-width: 56px;
    width: 56px;
  }

  .friend-avatar-wrapper :deep(.el-avatar) {
    width: 56px;
    height: 56px;
  }

  .friend-info {
    flex: 1;
    min-width: 0;
  }

  .friend-info h3 {
    font-size: 14px;
    margin: 0 0 4px;
  }

  .friend-status {
    font-size: 12px;
    margin: 0;
  }

  .friend-actions {
    flex-direction: column;
    width: 100%;
    gap: 8px;
  }

  .friend-actions .el-button {
    width: 100%;
    height: 40px;
    font-size: 12px;
  }

  .friend-actions :deep(.el-icon) {
    margin-right: 4px;
  }
}

/* ============ EXTRA SMALL DEVICES (≤360px) ============ */
@media (max-width: 360px) {
  .friend-card {
    padding: 10px;
  }

  .friend-avatar-wrapper {
    min-width: 48px;
    width: 48px;
  }

  .friend-avatar-wrapper :deep(.el-avatar) {
    width: 48px;
    height: 48px;
  }

  .friend-info h3 {
    font-size: 13px;
  }

  .friend-status {
    font-size: 11px;
  }

  .friend-actions .el-button {
    height: 36px;
    font-size: 11px;
    padding: 4px 8px;
  }

  .friends-toolbar-search :deep(.el-input) {
    font-size: 12px;
  }

  .friends-toolbar-actions :deep(.el-select) {
    font-size: 12px;
  }
}
</style>
