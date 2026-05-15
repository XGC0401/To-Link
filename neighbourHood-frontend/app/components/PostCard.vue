<template>
  <el-card class="post-card">
    <template #header>
      <div class="post-header">
        <div class="post-author">
          <el-avatar :size="40" />
          <div class="author-info">
            <p class="author-name">{{ displayAuthorName }}</p>
            <p class="post-time">{{ formatDateTime(post.createTime) }}</p>
          </div>
        </div>
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button text :icon="MoreFilled" circle />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="isOwnPost" command="edit">
                <el-icon>
                  <Edit />
                </el-icon>
                {{ $t('edit') }}
              </el-dropdown-item>
              <el-dropdown-item v-if="isOwnPost" command="delete">
                <el-icon>
                  <Delete />
                </el-icon>
                {{ $t('delete') }}
              </el-dropdown-item>
              <el-dropdown-item v-if="!isOwnPost" command="report" :divided="isOwnPost">
                <el-icon>
                  <Warning />
                </el-icon>
                {{ $t('report') }}
              </el-dropdown-item>
              <el-dropdown-item v-if="!isOwnPost" command="block">
                <el-icon>
                  <CircleClose />
                </el-icon>
                {{ $t('blockUser') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </template>

    <div class="post-content">
      <div class="post-information">
        <p class="post-title">{{ truncateText(post.title, 50) }}</p>
      </div>
      <p class="post-text">
        {{ truncateText(post.content, 100) }}
        <span v-if="post.content.length > 100" class="read-more" @click="$emit('showDetail', post)">
          ...
        </span>
      </p>
      <div class="image">
        <el-image v-if="post.postPhotos" v-for="(photo, index) in post.postPhotos" :src="photo.url" :fit="'cover'"
          style="width: 100px; height: 100px" />
      </div>
      <el-tag class="tag" size="large">{{ tagValue }}</el-tag>
      <div v-if="Array.isArray(post.tags) && post.tags.length > 0" class="post-tags">
        <el-tag v-for="tag in post.tags" :key="tag" class="post-chip" size="small">{{ tag }}</el-tag>
      </div>
    </div>

    <template #footer>
      <div class="post-footer">
        <el-space>
          <el-button text :icon="Star" :class="{ 'is-liked': isLiked }" @click="handleLike">{{ likeCount }}</el-button>
          <el-button text :icon="ChatDotRound" @click="emit('comment', props.post)">{{ commentCount }}</el-button>
        </el-space>
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Star, ChatDotRound, MoreFilled, Edit, Delete, Warning, CircleClose } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { likePost } from '~/api/post'
import type { Post } from '../api/types/post'

const { t } = useI18n()

const props = defineProps<{
  post: Post
  language?: 'en' | 'zh'
  currentUserEmail?: string
  currentUserId?: string
  currentUserDisplayName?: string
}>()

const emit = defineEmits<{
  (e: 'showDetail', post: Post): void
  (e: 'edit', post: Post): void
  (e: 'delete', post: Post): void
  (e: 'report', post: Post): void
  (e: 'block', post: Post): void
  (e: 'comment', post: Post): void
}>()

const isOwnPost = computed(() => {
  const postEmail = String((props.post.user?.email || (props.post as any).email || (props.post as any).authorEmail || '')).toLowerCase()
  const myEmail = String(props.currentUserEmail || '').toLowerCase()
  if (postEmail && myEmail && postEmail === myEmail) {
    return true
  }

  const postUserId = String((props.post.user?.uuid || (props.post as any).authorId || (props.post as any).userId || '')).toLowerCase()
  const myUserId = String(props.currentUserId || '').toLowerCase()
  return !!(postUserId && myUserId && postUserId === myUserId)
})

const isLiked = ref(false)
const likeCount = ref(Number((props.post as any).likes || 0))
const commentCount = ref(0)

const likeStorageKey = computed(() => `postLike:${props.post.id}`)
const likeCountStorageKey = computed(() => `postLikeCount:${props.post.id}`)
const commentCountStorageKey = computed(() => `postComments:${props.post.id}`)

const loadCommentCount = () => {
  try {
    const parsed = JSON.parse(localStorage.getItem(commentCountStorageKey.value) || '[]')
    commentCount.value = Array.isArray(parsed) ? parsed.length : 0
  } catch {
    commentCount.value = 0
  }
}

onMounted(() => {
  const likedRaw = localStorage.getItem(likeStorageKey.value)
  isLiked.value = likedRaw === '1'

  const storedCountRaw = localStorage.getItem(likeCountStorageKey.value)
  const storedCount = Number(storedCountRaw)
  if (!Number.isNaN(storedCount) && storedCount >= 0) {
    likeCount.value = storedCount
  }
  loadCommentCount()
})

watch(() => props.post.id, () => {
  const likedRaw = localStorage.getItem(likeStorageKey.value)
  isLiked.value = likedRaw === '1'
  const storedCountRaw = localStorage.getItem(likeCountStorageKey.value)
  const storedCount = Number(storedCountRaw)
  likeCount.value = !Number.isNaN(storedCount) && storedCount >= 0
    ? storedCount
    : Number((props.post as any).likes || 0)
  loadCommentCount()
})

async function handleLike() {
  const previousLiked = isLiked.value
  const previousCount = likeCount.value

  if (isLiked.value) {
    isLiked.value = false
    likeCount.value = Math.max(0, likeCount.value - 1)
    localStorage.setItem(likeStorageKey.value, '0')
  } else {
    isLiked.value = true
    likeCount.value += 1
    localStorage.setItem(likeStorageKey.value, '1')
  }
  localStorage.setItem(likeCountStorageKey.value, String(likeCount.value))

  if (typeof props.post.id === 'number') {
    const [error] = await likePost(props.post.id)
    if (error) {
      isLiked.value = previousLiked
      likeCount.value = previousCount
      localStorage.setItem(likeStorageKey.value, previousLiked ? '1' : '0')
      localStorage.setItem(likeCountStorageKey.value, String(previousCount))
      ElMessage.error(t('likeUpdateFailed'))
    }
  }
}

// Display name: use profile display name for own posts, otherwise use username from post
const displayAuthorName = computed(() => {
  if (isOwnPost.value && props.currentUserDisplayName) {
    return props.currentUserDisplayName
  }
  return props.post.user ? props.post.user.username : "null"
})

function handleCommand(command: string) {
  switch (command) {
    case 'edit':
      emit('edit', props.post)
      break
    case 'delete':
      emit('delete', props.post)
      break
    case 'report':
      emit('report', props.post)
      break
    case 'block':
      emit('block', props.post)
      break
  }
}

function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) {
    return text
  }
  return text.substring(0, maxLength)
}

function formatDateTime(dateTime: Date | undefined): string {
  if (!dateTime) return t('justNow')
  try {
    const dt = new Date(dateTime)
    if (Number.isNaN(dt.getTime())) {
      return t('justNow')
    }
    return dt.toLocaleString(props.language === 'zh' ? 'zh-HK' : 'en-US')
  } catch {
    return t('justNow')
  }
}

// async function handleLike() {
//   let isSubmitting = true
//   try {
//     await likePost(props.post.id);
//   } catch (e) {
//     console.log(e)
//   } finally {
//     isSubmitting = false;
//   }
// }

const tagValue = computed(() => {
  if (props.post.request_type === 4 && props.post.custom_category) {
    return props.post.custom_category
  }

  switch (props.post.request_type) {
    case 0: return t('shopping')
    case 1: return t('repair')
    case 2: return t('care')
    case 3: return t('daily')
    case 5: return t('eventOrganizing')
    case 6: return t('studySupport')
    case 7: return t('petSupport')
    case 8: return t('sportsAndWellness')
    case 9: return t('lostItem')
    case 10: return t('foundItem')
    case 11: return t('secondHandSell')
    case 12: return t('secondHandWant')
    case 4: return t('other')
    default: return t('other')
  }
})
</script>

<style scoped>
.tag {
  max-width: 100px;
  margin-top: auto;
}

.post-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.post-information p {
  margin: 0 !important;
  color: black;
  line-height: 5px;
}

.post-title {
  font-size: 20px;
  font-weight: 800;
}

.post-range {
  font-size: 10px;
}

.image {
  display: flex;
  gap: 20px;
  overflow: hidden;
  margin-bottom: 20px;
  ;
}

.post-card {
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  min-width: 0;
  width: 100%;
  max-width: 100%;
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  margin: 0;
  font-weight: 600;
  color: #333;
}

.post-time {
  margin: 0;
  font-size: 16px;
  color: #999;
}


.post-title {
  overflow: hidden;
  white-space: nowrap;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.post-chip {
  border-radius: 999px;
}

.post-content p {
  margin: 12px 0 12px 0;
  color: #666;
  line-height: 1.6;
}


.post-text {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.read-more {
  color: #409eff;
  cursor: pointer;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.3s;
}

.read-more:hover {
  color: #66b1ff;
  text-decoration: underline;
}

.post-footer {
  display: flex;
  justify-content: space-between;
}

.is-liked {
  color: #dc2626 !important;
}
</style>
