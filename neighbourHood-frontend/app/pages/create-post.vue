<template>
  <NuxtLayout name="default">
    <div class="create-post-container">
      <el-card class="create-post-card">
        <template #header>
          <div class="card-header">
            <h2>{{ $t('createNewPostTitle') }}</h2>
          </div>
        </template>

        <el-form ref="postFormRef" :model="postForm" :rules="postRules" label-position="top" class="post-form">
          <el-form-item :label="$t('timeSelect')" prop="time">
            <el-date-picker v-model="postForm.time" is-range :range-separator="$t('To')"
              :start-placeholder="t('startTime')" type="datetimerange" :end-placeholder="t('endTime')"
              value-format="YYYY-MM-DD HH:mm:ss" :disabled-date="disabledDate" />
          </el-form-item>

          <el-form-item :label="$t('title')" prop="title">
            <el-input v-model="postForm.title" :placeholder="$t('enterTitle')" maxlength="100" show-word-limit
              size="large" />
          </el-form-item>

          <el-form-item :label="$t('category')" prop="request_type">
            <el-select v-model="postForm.request_type" :placeholder="$t('selectCategory')" size="large" style="width: 240px">
              <el-option :label="$t('shopping')" :value="0" />
              <el-option :label="$t('repair')" :value="1" />
              <el-option :label="$t('care')" :value="2" />
              <el-option :label="$t('daily')" :value="3" />
              <el-option :label="$t('other')" :value="4" />
            </el-select>
          </el-form-item>


          <el-form-item :label="$t('content')" prop="content">
            <el-input v-model="postForm.content" type="textarea" :placeholder="$t('shareThoughts')"
              :autosize="{ minRows: 4, maxRows: 4 }" maxlength="2000" :show-word-limit="false" :resize="'none'" />
          </el-form-item>

          <el-form-item :label="$t('imagesOptional')">
            <el-upload v-model:file-list="fileList" list-type="picture-card" :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove" :limit="5" accept="image/*">
              <el-icon>
                <Plus />
              </el-icon>
            </el-upload>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button size="large" @click="handleCancel">
                {{ $t('cancel') }}
              </el-button>
              <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
                {{ $t('publishPost') }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" :alt="$t('imagePreview')" />
    </el-dialog>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules, type UploadFile, type UploadProps, type UploadUserFile } from 'element-plus'
import {
  Plus,
  Calendar,
  Notification,
} from '@element-plus/icons-vue'
import type { Post, PostRequest } from '~/api/types/post'
import { publishPost } from '~/api/post'

const router = useRouter()
const { t } = useI18n()
const submitting = ref(false)
const postFormRef = ref<FormInstance>()
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const fileList = ref<UploadFile[]>([])
const time = ref<[Date, Date]>([
  new Date(),
  new Date(),
])

const postForm = reactive({
  title: '',
  content: '',
  request_type: 0,
  paymentMethod: 0,
  is_important: false,
  redeemPoints: 0,
  type: 0,
  time: null as [string, string] | null,
})



const postRules = reactive<FormRules>({
  title: [
    {
      required: true,
      message: t('pleaseEnterTitle'),
      trigger: 'blur'
    },
    {
      min: 3,
      max: 100,
      message: t('titleLength'),
      trigger: 'blur'
    }
  ],
  request_type: [
    {
      required: true,
      message: t('pleaseSelectCategory'),
      trigger: 'change'
    }
  ],
  time: [
    {
      required: true,
      message: t('enterContent'),
      trigger: 'change'
    }
  ],
  content: [
    {
      required: true,
      message: t('enterContent'),
      trigger: 'blur'
    },
    {
      min: 10,
      max: 2000,
      message: t('contentLength'),
      trigger: 'blur'
    }
  ]
})

const disabledDate = (time: { getTime: () => number }) => {
  const now = new Date();
  return time.getTime() < new Date(new Date().setHours(0, 0, 0, 0)).getTime();
};

const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url!
  dialogVisible.value = true
}

const handleRemove: UploadProps['onRemove'] = (file, uploadFiles) => {
  console.log('File removed:', file)
}

const handleSubmit = async () => {
  if (!postFormRef.value) return

  await postFormRef.value.validate((valid) => {
    if (valid) {
      createPost()
    } else {
      ElMessage.warning(t('fillRequired'))
    }
  })
}

const createPost = async () => {
  submitting.value = true

  try {
    const formData = new FormData();
    formData.append("title", postForm.title);
    formData.append("type", postForm.type.toString());
    formData.append("content", postForm.content);
    formData.append("request_type", postForm.request_type.toString());
    formData.append("is_important", postForm.is_important.toString());
    formData.append("redeemPoints", (postForm.redeemPoints ?? 0).toString());
    if (postForm.time && postForm.time[0]) {
      formData.append("startTime", postForm.time[0]);
      if (postForm.time[1]) {
        formData.append("endTime", postForm.time[1]);
      }
    }

    formData.append("payment_method", (postForm.paymentMethod ? postForm.paymentMethod.toString() : "0"));

    fileList.value.forEach((f) => {
      if (f.raw) {
        formData.append("files", f.raw);
      }
    })

    const [error, response] = await publishPost(formData)
    if (error || !response?.success) {
      throw new Error(response?.message || t('postFailed'))
    }

    const savedProfileRaw = localStorage.getItem('userProfile')
    const savedProfile = savedProfileRaw ? JSON.parse(savedProfileRaw) : null
    const localPostsRaw = localStorage.getItem('userPosts')
    const localPosts = localPostsRaw ? JSON.parse(localPostsRaw) : []

    const optimisticPost: Post = {
      id: Date.now(),
      type: postForm.type,
      user: {
        uuid: String(savedProfile?.uuid || savedProfile?.email || Date.now()),
        username: savedProfile?.name || savedProfile?.username || 'You',
        email: savedProfile?.email || undefined
      },
      title: postForm.title,
      content: postForm.content,
      request_type: postForm.request_type,
      share_count: 0,
      is_important: postForm.is_important,
      redeemPoints: postForm.redeemPoints ?? 0,
      paymentMethod: postForm.paymentMethod,
      createTime: new Date(),
      startTime: postForm.time?.[0] ? new Date(postForm.time[0]) : undefined,
      endTime: postForm.time?.[1] ? new Date(postForm.time[1]) : undefined
    }

    localStorage.setItem('userPosts', JSON.stringify([optimisticPost, ...localPosts]))

    ElMessage.success(t('postSuccess'))

    // Redirect to posts page
    router.push('/posts')
  } catch (error) {
    console.error('Error creating post:', error)
    ElMessage.error(t('postFailed'))
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.create-post-container {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.create-post-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header h2 {
  margin: 0;
  color: #333;
  font-size: 32px;
  font-weight: 600;
}

.post-form {
  padding: 20px 0;
}

.post-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
  font-size: 19px;
}

.post-form :deep(.el-textarea__inner) {
  font-family: inherit;
  font-size: 18px;
  line-height: 1.6;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

:deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
  border-radius: 8px;
}

:deep(.el-select .el-icon) {
  vertical-align: middle;
}

.tag-hint {
  margin-top: 8px;
  font-size: 16px;
  color: #999;
}

/* Dark mode support */
.dark .post-form :deep(.el-form-item__label) {
  color: #e5e5e5;
}

.dark .post-form :deep(.el-input__wrapper) {
  background-color: #3d3d3d;
  box-shadow: 0 0 0 1px #555 inset;
}

.dark .post-form :deep(.el-input__inner) {
  color: #e5e5e5;
}

.dark .post-form :deep(.el-textarea__inner) {
  background-color: #3d3d3d;
  color: #e5e5e5;
  box-shadow: 0 0 0 1px #555 inset;
}

.dark .post-form :deep(.el-date-editor) {
  background-color: #3d3d3d;
  box-shadow: 0 0 0 1px #555 inset;
}

.dark .post-form :deep(.el-date-editor .el-input__inner) {
  color: #e5e5e5;
}

.dark .post-form :deep(.el-date-editor .el-icon) {
  color: #e5e5e5;
}

.dark .post-form :deep(.el-range-separator) {
  color: #e5e5e5;
}

.dark .post-form :deep(.el-upload--picture-card) {
  background-color: #3d3d3d;
  border-color: #555;
}

.dark .post-form :deep(.el-upload--picture-card .el-icon) {
  color: #e5e5e5;
}

.dark .post-form :deep(.el-upload-list--picture-card .el-upload-list__item) {
  background-color: #3d3d3d;
  border-color: #555;
}

.dark .form-actions {
  border-top-color: #3d3d3d;
}

.dark .card-header h2 {
  color: #e5e5e5;
}

/* Dark mode for date picker dropdown */
.dark :deep(.el-picker-panel) {
  background-color: #2d2d2d;
  border-color: #555;
}

.dark :deep(.el-date-picker__header-label),
.dark :deep(.el-date-table th),
.dark :deep(.el-date-table td.available),
.dark :deep(.el-date-table td .el-date-table-cell__text),
.dark :deep(.el-time-panel__btn),
.dark :deep(.el-time-spinner__item) {
  color: #e5e5e5;
}

.dark :deep(.el-picker-panel__icon-btn) {
  color: #e5e5e5;
}

.dark :deep(.el-date-table td) {
  color: #e5e5e5;
}

.dark :deep(.el-date-table td.prev-month),
.dark :deep(.el-date-table td.next-month) {
  color: #888;
}

.dark :deep(.el-date-table td.disabled) {
  background-color: #1a1a1a;
  color: #555;
}

.dark :deep(.el-date-table td.in-range) {
  background-color: #3d3d3d;
}

.dark :deep(.el-date-table td:hover) {
  background-color: #3d3d3d;
}

.dark :deep(.el-date-table td.current:not(.disabled) .el-date-table-cell__text) {
  background-color: var(--el-color-primary);
  color: #fff;
}

.dark :deep(.el-date-table td.today .el-date-table-cell__text) {
  color: var(--el-color-primary);
}

.dark :deep(.el-date-table td.start-date .el-date-table-cell__text),
.dark :deep(.el-date-table td.end-date .el-date-table-cell__text) {
  background-color: var(--el-color-primary);
  color: #fff;
}

.dark :deep(.el-time-panel) {
  background-color: #2d2d2d;
  border-color: #555;
}

.dark :deep(.el-time-spinner__list) {
  color: #e5e5e5;
}

.dark :deep(.el-date-range-picker__content) {
  background-color: #2d2d2d;
}

.dark :deep(.el-date-range-picker__time-header) {
  border-color: #555;
  color: #e5e5e5;
}

.dark :deep(.el-picker-panel__footer) {
  background-color: #2d2d2d;
  border-color: #555;
}

@media (max-width: 768px) {
  .create-post-container {
    padding: 10px;
  }

  .card-header h2 {
    font-size: 26px;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>

<style>
/* Global dark mode styles for date picker popper (teleported to body) */
html.dark .el-picker-panel {
  background-color: #2d2d2d !important;
  border-color: #555 !important;
}

html.dark .el-picker-panel__body {
  background-color: #2d2d2d !important;
}

html.dark .el-date-picker__header {
  background-color: #2d2d2d !important;
  border-bottom-color: #555 !important;
}

html.dark .el-date-picker__header-label,
html.dark .el-date-table th {
  color: #e5e5e5 !important;
}

html.dark .el-picker-panel__icon-btn {
  color: #e5e5e5 !important;
}

html.dark .el-picker-panel__icon-btn:hover {
  color: var(--el-color-primary) !important;
}

/* All date cells - current month */
html.dark .el-date-table td.available {
  color: #e5e5e5 !important;
}

html.dark .el-date-table td.available .el-date-table-cell__text {
  color: #e5e5e5 !important;
  background-color: transparent !important;
}

html.dark .el-date-table td.available:hover .el-date-table-cell__text {
  background-color: #3d3d3d !important;
  color: #e5e5e5 !important;
}

/* Previous and next month dates */
html.dark .el-date-table td.prev-month,
html.dark .el-date-table td.next-month {
  color: #999 !important;
}

html.dark .el-date-table td.prev-month .el-date-table-cell__text,
html.dark .el-date-table td.next-month .el-date-table-cell__text {
  color: #999 !important;
}

/* Disabled dates */
html.dark .el-date-table td.disabled {
  background-color: transparent !important;
}

html.dark .el-date-table td.disabled .el-date-table-cell__text {
  color: #555 !important;
  background-color: transparent !important;
}

/* Today's date */
html.dark .el-date-table td.today .el-date-table-cell__text {
  color: var(--el-color-primary) !important;
  font-weight: bold !important;
}

/* Selected dates */
html.dark .el-date-table td.current:not(.disabled) .el-date-table-cell__text {
  background-color: var(--el-color-primary) !important;
  color: #fff !important;
}

/* Date range selection */
html.dark .el-date-table td.in-range .el-date-table-cell__text {
  background-color: #3d3d3d !important;
  color: #e5e5e5 !important;
}

html.dark .el-date-table td.start-date .el-date-table-cell__text,
html.dark .el-date-table td.end-date .el-date-table-cell__text {
  background-color: var(--el-color-primary) !important;
  color: #fff !important;
}

/* Footer buttons */
html.dark .el-picker-panel__footer {
  background-color: #2d2d2d !important;
  border-top-color: #555 !important;
}

html.dark .el-picker-panel__footer .el-button {
  background-color: #3d3d3d !important;
  border-color: #555 !important;
  color: #e5e5e5 !important;
}

html.dark .el-picker-panel__footer .el-button:hover {
  background-color: #4d4d4d !important;
}

/* Time panel */
html.dark .el-time-panel {
  background-color: #2d2d2d !important;
  border-color: #555 !important;
}

html.dark .el-time-spinner__item {
  color: #e5e5e5 !important;
}

html.dark .el-time-spinner__item:hover {
  background-color: #3d3d3d !important;
}

/* Date range picker */
html.dark .el-date-range-picker__content {
  background-color: #2d2d2d !important;
}

html.dark .el-date-range-picker__time-header {
  border-color: #555 !important;
  color: #e5e5e5 !important;
}

html.dark .el-date-range-picker .el-picker-panel__content {
  background-color: #2d2d2d !important;
}
</style>
