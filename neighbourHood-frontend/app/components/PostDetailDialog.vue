<template>
  <el-dialog 
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="displayTitle"
    width="700px"
    align-center
  >
    <div v-if="post" class="post-detail-content">
      <div class="detail-author">
        <el-avatar :size="50" :src="displayAvatar" />
        <div class="author-info">
          <p class="author-name">{{ displayAuthor }}</p>
          <p class="post-time">{{ displayTime }}</p>
        </div>
      </div>

      <el-divider />

      <div class="detail-body">
        <p>{{ String(post.content || '') }}</p>
        <el-tag v-if="displayCategory" class="post-tag" size="large">
          {{ displayCategory }}
        </el-tag>
      </div>

      <el-divider />

      <div class="detail-footer">
        <el-space>
          <el-button class="detail-like-btn" @click="toggleLike">
            <span class="detail-like-heart" :class="{ 'is-liked': isLiked }">{{ isLiked ? '❤' : '♡' }}</span>
            <span class="detail-like-count">{{ likeCount }} {{ $t('likes') }}</span>
          </el-button>
          <el-button :icon="ChatDotRound">{{ comments.length }} {{ $t('comments') }}</el-button>
          <el-button :icon="Share">{{ $t('share') }}</el-button>
        </el-space>
      </div>

      <el-divider />

      <div class="comments-section">
        <h4>{{ $t('comments') }}</h4>
        <div class="comment-editor">
          <el-input
            v-model="commentDraft"
            type="textarea"
            :rows="3"
            :placeholder="$t('enterContent')"
          />
          <div class="comment-actions">
            <el-button v-if="editingCommentId" @click="cancelEdit">{{ $t('cancel') }}</el-button>
            <el-button type="primary" @click="submitComment">{{ editingCommentId ? $t('save') : $t('publishPost') }}</el-button>
          </div>
        </div>

        <el-empty v-if="comments.length === 0" :description="$t('comments')" />
        <div v-else class="comment-list">
          <div v-for="comment in comments" :key="comment.id" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.author }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
            <div class="comment-item-actions">
              <el-button text size="small" @click="startEdit(comment)">{{ $t('edit') }}</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { ChatDotRound, Share } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { likePost } from '~/api/post'

interface PostUser {
  username?: string
  email?: string
}

interface Post {
  id: number
  author?: string
  avatar?: string
  title?: string
  content?: string
  time?: string
  category?: string
  request_type?: number
  custom_category?: string
  createTime?: Date | string
  user?: PostUser
  likes?: number
}

interface PostComment {
  id: string
  author: string
  content: string
  createdAt: string
}

const { t, locale } = useI18n()

const props = defineProps<{
  modelValue: boolean
  post: Post | null
  language: 'en' | 'zh'
}>()

defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const commentDraft = ref('')
const editingCommentId = ref<string | null>(null)
const comments = ref<PostComment[]>([])
const isLiked = ref(false)
const likeCount = ref(0)

const postStorageId = computed(() => String(props.post?.id || 'unknown'))
const commentsKey = computed(() => `postComments:${postStorageId.value}`)
const likeKey = computed(() => `postLike:${postStorageId.value}`)
const likeCountKey = computed(() => `postLikeCount:${postStorageId.value}`)

const displayTitle = computed(() => String(props.post?.title || ''))
const displayAuthor = computed(() => String(props.post?.author || props.post?.user?.username || props.post?.user?.email || t('unknownUser')))
const displayAvatar = computed(() => String(props.post?.avatar || ''))
const displayCategory = computed(() => {
  if (props.post?.category) return props.post.category
  if (props.post?.request_type === 4 && props.post?.custom_category) return props.post.custom_category
  return ''
})
const displayTime = computed(() => formatDate(props.post?.time || props.post?.createTime))

const formatDate = (value: unknown) => {
  const dt = new Date(String(value || ''))
  if (Number.isNaN(dt.getTime())) return t('justNow')
  return dt.toLocaleString(locale.value === 'zh' ? 'zh-HK' : 'en-US')
}

const loadComments = () => {
  try {
    const parsed = JSON.parse(localStorage.getItem(commentsKey.value) || '[]')
    comments.value = Array.isArray(parsed) ? parsed : []
  } catch {
    comments.value = []
  }
}

const saveComments = () => {
  localStorage.setItem(commentsKey.value, JSON.stringify(comments.value))
}

const loadLikeState = () => {
  isLiked.value = localStorage.getItem(likeKey.value) === '1'
  const count = Number(localStorage.getItem(likeCountKey.value))
  likeCount.value = Number.isNaN(count) ? Number(props.post?.likes || 0) : count
}

const toggleLike = async () => {
  const prevLiked = isLiked.value
  const prevCount = likeCount.value

  if (isLiked.value) {
    isLiked.value = false
    likeCount.value = Math.max(0, likeCount.value - 1)
  } else {
    isLiked.value = true
    likeCount.value += 1
  }
  localStorage.setItem(likeKey.value, isLiked.value ? '1' : '0')
  localStorage.setItem(likeCountKey.value, String(likeCount.value))

  if (typeof props.post?.id === 'number') {
    const [error] = await likePost(props.post.id)
    if (error) {
      isLiked.value = prevLiked
      likeCount.value = prevCount
      localStorage.setItem(likeKey.value, prevLiked ? '1' : '0')
      localStorage.setItem(likeCountKey.value, String(prevCount))
      ElMessage.error(t('likeUpdateFailed'))
    }
  }
}

const submitComment = () => {
  const text = commentDraft.value.trim()
  if (!text) {
    ElMessage.warning(t('enterContent'))
    return
  }

  if (editingCommentId.value) {
    const target = comments.value.find((item) => item.id === editingCommentId.value)
    if (target) {
      target.content = text
    }
    editingCommentId.value = null
  } else {
    comments.value.unshift({
      id: `${Date.now()}-${Math.random().toString(16).slice(2, 7)}`,
      author: t('you'),
      content: text,
      createdAt: new Date().toISOString()
    })
  }

  commentDraft.value = ''
  saveComments()
}

const startEdit = (comment: PostComment) => {
  editingCommentId.value = comment.id
  commentDraft.value = comment.content
}

const cancelEdit = () => {
  editingCommentId.value = null
  commentDraft.value = ''
}

watch(() => props.modelValue, (open) => {
  if (!open || !props.post) return
  loadComments()
  loadLikeState()
})
</script>

<style scoped>
.post-detail-content {
  padding: 16px 0;
}

.detail-author {
  display: flex;
  align-items: center;
  gap: 16px;
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

.detail-body {
  padding: 16px 0;
}

.detail-body p {
  font-size: 20px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 16px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.post-tag {
  margin-top: 8px;
}

.detail-footer {
  display: flex;
  justify-content: center;
}

.comments-section h4 {
  margin: 0 0 10px;
}

.comment-editor {
  display: grid;
  gap: 8px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.comment-list {
  margin-top: 10px;
  display: grid;
  gap: 10px;
}

.comment-item {
  border: 1px solid var(--tl-border);
  border-radius: 10px;
  padding: 10px;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.comment-author {
  font-weight: 700;
}

.comment-time {
  color: var(--tl-text-muted);
  font-size: 12px;
}

.comment-content {
  margin: 8px 0;
}

.comment-item-actions {
  display: flex;
  justify-content: flex-end;
}

.detail-like-btn {
  gap: 6px;
}

.detail-like-heart {
  font-size: 18px;
  line-height: 1;
  color: var(--tl-text-muted);
}

.detail-like-heart.is-liked {
  color: #dc2626;
}

.detail-like-count {
  color: var(--tl-text);
}
</style>
