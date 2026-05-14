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
          <el-form-item :label="$t('title')" prop="title">
            <el-input v-model="postForm.title" :placeholder="$t('enterTitle')" maxlength="100" show-word-limit
              size="large" />
          </el-form-item>

          <el-form-item :label="$t('category')" prop="request_type">
            <el-select v-model="postForm.request_type" :placeholder="$t('selectCategory')" size="large" class="create-post-category-select">
              <el-option :label="$t('shopping')" :value="0" />
              <el-option :label="$t('repair')" :value="1" />
              <el-option :label="$t('care')" :value="2" />
              <el-option :label="$t('daily')" :value="3" />
              <el-option :label="$t('eventOrganizing')" :value="5" />
              <el-option :label="$t('studySupport')" :value="6" />
              <el-option :label="$t('petSupport')" :value="7" />
              <el-option :label="$t('sportsAndWellness')" :value="8" />
              <el-option :label="$t('lostItem')" :value="9" />
              <el-option :label="$t('foundItem')" :value="10" />
              <el-option :label="$t('secondHandSell')" :value="11" />
              <el-option :label="$t('secondHandWant')" :value="12" />
              <el-option :label="$t('other')" :value="4" />
            </el-select>
          </el-form-item>

          <el-form-item v-if="postForm.request_type === 4" :label="$t('customCategory')" prop="custom_category">
            <el-input
              v-model="postForm.custom_category"
              :placeholder="$t('customCategoryPlaceholder')"
              maxlength="40"
              show-word-limit
              size="large"
            />
          </el-form-item>

          <el-form-item :label="$t('tagsOptional')">
            <div class="post-tags-editor">
              <div class="tag-input-row">
                <el-input
                  v-model="tagInput"
                  :placeholder="$t('addTags')"
                  @keyup.enter.prevent="addTag"
                >
                  <template #append>
                    <el-button :icon="Check" @click="addTag" />
                  </template>
                </el-input>
              </div>

              <div v-if="postForm.tags.length > 0" class="selected-tags">
                <el-tag
                  v-for="tag in postForm.tags"
                  :key="tag"
                  round
                  closable
                  @close="removeTag(tag)"
                >
                  {{ tag }}
                </el-tag>
              </div>
              <p v-else class="tag-hint">{{ $t('tagHint') }}</p>
            </div>
          </el-form-item>


          <el-form-item :label="$t('content')" prop="content">
            <el-input v-model="postForm.content" type="textarea" :placeholder="$t('shareThoughts')"
              :autosize="{ minRows: 4, maxRows: 4 }" maxlength="2000" :show-word-limit="false" :resize="'none'" />
          </el-form-item>

          <el-form-item :label="$t('imagesOptional')">
            <el-upload v-model:file-list="fileList" list-type="picture-card" :on-preview="handlePictureCardPreview"
              :on-remove="handleRemove" :limit="5" accept="image/*" :auto-upload="false">
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules, type UploadFile, type UploadProps, type UploadUserFile } from 'element-plus'
import {
  Plus,
  Check,
} from '@element-plus/icons-vue'
import { publishPost } from '~/api/post'

const router = useRouter()
const { t } = useI18n()
const submitting = ref(false)
const postFormRef = ref<FormInstance>()
const dialogVisible = ref(false)
const dialogImageUrl = ref('')
const fileList = ref<UploadFile[]>([])
const tagInput = ref('')

const postForm = reactive({
  title: '',
  content: '',
  request_type: 0,
  custom_category: '',
  tags: [] as string[],
  paymentMethod: 0,
  is_important: false,
  redeemPoints: 0,
  type: 0,
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
  custom_category: [
    {
      validator: (_rule, value, callback) => {
        if (postForm.request_type !== 4) {
          callback()
          return
        }

        if (!value || !String(value).trim()) {
          callback(new Error(t('pleaseEnterCustomCategory')))
          return
        }

        callback()
      },
      trigger: 'blur'
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

const addTag = () => {
  const value = tagInput.value.trim()
  if (!value) {
    return
  }

  if (!value.startsWith('#')) {
    ElMessage.warning(t('tagMustStartWithHash'))
    return
  }

  if (/\s/.test(value)) {
    ElMessage.warning(t('tagNoSpaces'))
    return
  }

  if (postForm.tags.includes(value)) {
    ElMessage.warning(t('tagAlreadyExists'))
    return
  }

  postForm.tags.push(value)
  tagInput.value = ''
}

const removeTag = (tag: string) => {
  postForm.tags = postForm.tags.filter((item) => item !== tag)
}

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
  if (submitting.value) {
    return
  }
  submitting.value = true

  try {
    const formData = new FormData();
    formData.append("title", postForm.title);
    formData.append("type", postForm.type.toString());
    formData.append("content", postForm.content);
    formData.append("request_type", postForm.request_type.toString());
    if (postForm.request_type === 4 && postForm.custom_category.trim()) {
      formData.append("custom_category", postForm.custom_category.trim());
    }
    formData.append("is_important", postForm.is_important.toString());
    formData.append("redeemPoints", (postForm.redeemPoints ?? 0).toString());

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

.create-post-category-select {
  width: 240px;
  max-width: 100%;
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

.post-tags-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.tag-input-row {
  width: 100%;
  max-width: 520px;
}

.tag-input-row :deep(.el-input-group__append) {
  border: none;
  background: var(--tl-surface);
  border-radius: 0 10px 10px 0;
  box-shadow: 0 0 0 1px var(--tl-border) inset;
}

.tag-input-row :deep(.el-input-group__append .el-button) {
  border: none !important;
  background: transparent !important;
  color: var(--tl-text-muted);
  padding-inline: 12px;
  min-height: 38px;
  box-shadow: none !important;
}

.tag-input-row :deep(.el-input-group__append .el-button:hover) {
  color: var(--el-color-primary);
}

.selected-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Dark mode support */
:global(.dark) .post-form :deep(.el-form-item__label) {
  color: #e5e5e5;
}

:global(.dark) .post-form :deep(.el-input__wrapper) {
  background-color: #3d3d3d;
  box-shadow: 0 0 0 1px #555 inset;
}

:global(.dark) .tag-input-row :deep(.el-input-group__append) {
  background: var(--tl-bg);
  box-shadow: 0 0 0 1px var(--tl-border) inset;
}

:global(.dark) .tag-input-row :deep(.el-input-group__append .el-button) {
  color: var(--tl-text-muted);
}

:global(.dark) .post-form :deep(.el-input__inner) {
  color: #e5e5e5;
}

:global(.dark) .post-form :deep(.el-textarea__inner) {
  background-color: #3d3d3d;
  color: #e5e5e5;
  box-shadow: 0 0 0 1px #555 inset;
}

:global(.dark) .post-form :deep(.el-date-editor) {
  background-color: #3d3d3d;
  box-shadow: 0 0 0 1px #555 inset;
}

:global(.dark) .post-form :deep(.el-date-editor .el-input__inner) {
  color: #e5e5e5;
}

:global(.dark) .post-form :deep(.el-date-editor .el-icon) {
  color: #e5e5e5;
}

:global(.dark) .post-form :deep(.el-range-separator) {
  color: #e5e5e5;
}

:global(.dark) .post-form :deep(.el-upload--picture-card) {
  background-color: #3d3d3d;
  border-color: #555;
}

:global(.dark) .post-form :deep(.el-upload--picture-card .el-icon) {
  color: #e5e5e5;
}

:global(.dark) .post-form :deep(.el-upload-list--picture-card .el-upload-list__item) {
  background-color: #3d3d3d;
  border-color: #555;
}

:global(.dark) .form-actions {
  border-top-color: #3d3d3d;
}

:global(.dark) .card-header h2 {
  color: #e5e5e5;
}

/* Dark mode for date picker dropdown */
:global(.dark) :deep(.el-picker-panel) {
  background-color: #2d2d2d;
  border-color: #555;
}

:global(.dark) :deep(.el-date-picker__header-label),
:global(.dark) :deep(.el-date-table th),
:global(.dark) :deep(.el-date-table td.available),
:global(.dark) :deep(.el-date-table td .el-date-table-cell__text),
:global(.dark) :deep(.el-time-panel__btn),
:global(.dark) :deep(.el-time-spinner__item) {
  color: #e5e5e5;
}

:global(.dark) :deep(.el-picker-panel__icon-btn) {
  color: #e5e5e5;
}

:global(.dark) :deep(.el-date-table td) {
  color: #e5e5e5;
}

:global(.dark) :deep(.el-date-table td.prev-month),
:global(.dark) :deep(.el-date-table td.next-month) {
  color: #888;
}

:global(.dark) :deep(.el-date-table td.disabled) {
  background-color: #1a1a1a;
  color: #555;
}

:global(.dark) :deep(.el-date-table td.in-range) {
  background-color: #3d3d3d;
}

:global(.dark) :deep(.el-date-table td:hover) {
  background-color: #3d3d3d;
}

:global(.dark) :deep(.el-date-table td.current:not(.disabled) .el-date-table-cell__text) {
  background-color: var(--el-color-primary);
  color: #fff;
}

:global(.dark) :deep(.el-date-table td.today .el-date-table-cell__text) {
  color: var(--el-color-primary);
}

:global(.dark) :deep(.el-date-table td.start-date .el-date-table-cell__text),
:global(.dark) :deep(.el-date-table td.end-date .el-date-table-cell__text) {
  background-color: var(--el-color-primary);
  color: #fff;
}

:global(.dark) :deep(.el-time-panel) {
  background-color: #2d2d2d;
  border-color: #555;
}

:global(.dark) :deep(.el-time-spinner__list) {
  color: #e5e5e5;
}

:global(.dark) :deep(.el-date-range-picker__content) {
  background-color: #2d2d2d;
}

:global(.dark) :deep(.el-date-range-picker__time-header) {
  border-color: #555;
  color: #e5e5e5;
}

:global(.dark) :deep(.el-picker-panel__footer) {
  background-color: #2d2d2d;
  border-color: #555;
}

@media (max-width: 1200px) {
  .create-post-container {
    max-width: 100%;
    padding: 16px;
  }
}

@media (max-width: 1024px) {
  .create-post-category-select {
    width: 100%;
  }

  .tag-input-row {
    max-width: 100%;
  }
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
:global(html.dark) .el-picker-panel {
  background-color: #2d2d2d !important;
  border-color: #555 !important;
}

:global(html.dark) .el-picker-panel__body {
  background-color: #2d2d2d !important;
}

:global(html.dark) .el-date-picker__header {
  background-color: #2d2d2d !important;
  border-bottom-color: #555 !important;
}

:global(html.dark) .el-date-picker__header-label,
:global(html.dark) .el-date-table th {
  color: #e5e5e5 !important;
}

:global(html.dark) .el-picker-panel__icon-btn {
  color: #e5e5e5 !important;
}

:global(html.dark) .el-picker-panel__icon-btn:hover {
  color: var(--el-color-primary) !important;
}

/* All date cells - current month */
:global(html.dark) .el-date-table td.available {
  color: #e5e5e5 !important;
}

:global(html.dark) .el-date-table td.available .el-date-table-cell__text {
  color: #e5e5e5 !important;
  background-color: transparent !important;
}

:global(html.dark) .el-date-table td.available:hover .el-date-table-cell__text {
  background-color: #3d3d3d !important;
  color: #e5e5e5 !important;
}

/* Previous and next month dates */
:global(html.dark) .el-date-table td.prev-month,
:global(html.dark) .el-date-table td.next-month {
  color: #999 !important;
}

:global(html.dark) .el-date-table td.prev-month .el-date-table-cell__text,
:global(html.dark) .el-date-table td.next-month .el-date-table-cell__text {
  color: #999 !important;
}

/* Disabled dates */
:global(html.dark) .el-date-table td.disabled {
  background-color: transparent !important;
}

:global(html.dark) .el-date-table td.disabled .el-date-table-cell__text {
  color: #555 !important;
  background-color: transparent !important;
}

/* Today's date */
:global(html.dark) .el-date-table td.today .el-date-table-cell__text {
  color: var(--el-color-primary) !important;
  font-weight: bold !important;
}

/* Selected dates */
:global(html.dark) .el-date-table td.current:not(.disabled) .el-date-table-cell__text {
  background-color: var(--el-color-primary) !important;
  color: #fff !important;
}

/* Date range selection */
:global(html.dark) .el-date-table td.in-range .el-date-table-cell__text {
  background-color: #3d3d3d !important;
  color: #e5e5e5 !important;
}

:global(html.dark) .el-date-table td.start-date .el-date-table-cell__text,
:global(html.dark) .el-date-table td.end-date .el-date-table-cell__text {
  background-color: var(--el-color-primary) !important;
  color: #fff !important;
}

/* Footer buttons */
:global(html.dark) .el-picker-panel__footer {
  background-color: #2d2d2d !important;
  border-top-color: #555 !important;
}

:global(html.dark) .el-picker-panel__footer .el-button {
  background-color: #3d3d3d !important;
  border-color: #555 !important;
  color: #e5e5e5 !important;
}

:global(html.dark) .el-picker-panel__footer .el-button:hover {
  background-color: #4d4d4d !important;
}

/* Time panel */
:global(html.dark) .el-time-panel {
  background-color: #2d2d2d !important;
  border-color: #555 !important;
}

:global(html.dark) .el-time-spinner__item {
  color: #e5e5e5 !important;
}

:global(html.dark) .el-time-spinner__item:hover {
  background-color: #3d3d3d !important;
}

/* Date range picker */
:global(html.dark) .el-date-range-picker__content {
  background-color: #2d2d2d !important;
}

:global(html.dark) .el-date-range-picker__time-header {
  border-color: #555 !important;
  color: #e5e5e5 !important;
}

:global(html.dark) .el-date-range-picker .el-picker-panel__content {
  background-color: #2d2d2d !important;
}

/* ============ TABLET BREAKPOINT (481-1024px) ============ */
@media (max-width: 1024px) {
  .create-post-container {
    max-width: 100%;
    padding: 16px;
  }

  .el-form :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  .el-form :deep(.el-input),
  .el-form :deep(.el-select),
  .el-form :deep(.el-textarea) {
    height: 44px;
  }

  .el-form :deep(.el-button) {
    height: 44px;
    min-width: 100px;
  }
}

/* ============ PHONE BREAKPOINT (≤480px) ============ */
@media (max-width: 480px) {
  .create-post-page {
    padding: 8px;
  }

  .create-post-container {
    max-width: 100%;
    padding: 12px;
    border-radius: 12px;
    gap: 12px;
  }

  .create-post-title {
    font-size: 16px;
    margin-bottom: 12px;
  }

  .el-form {
    gap: 12px;
  }

  .el-form :deep(.el-form-item) {
    margin-bottom: 16px;
  }

  .el-form :deep(.el-form-item__label) {
    font-size: 13px;
    line-height: 44px;
    margin-bottom: 6px;
  }

  .el-form :deep(.el-input),
  .el-form :deep(.el-select) {
    height: 44px;
    font-size: 13px;
  }

  .el-form :deep(.el-input__inner),
  .el-form :deep(.el-select__wrapper) {
    padding: 8px 12px;
  }

  .el-form :deep(.el-textarea) {
    min-height: 120px;
  }

  .el-form :deep(.el-textarea__inner) {
    font-size: 13px;
    padding: 8px 12px;
  }

  .el-form :deep(.el-button) {
    height: 44px;
    width: 100%;
    font-size: 14px;
  }

  .el-form :deep(.el-button--primary) {
    height: 48px;
    font-size: 14px;
    font-weight: 600;
  }

  .el-form :deep(.el-radio),
  .el-form :deep(.el-checkbox) {
    min-height: 48px;
  }

  .el-form :deep(.el-radio__label),
  .el-form :deep(.el-checkbox__label) {
    font-size: 13px;
    padding: 0 8px;
  }

  .el-form :deep(.el-picker-panel) {
    width: calc(100vw - 24px) !important;
    max-width: 360px !important;
  }

  .form-actions {
    flex-direction: column;
    gap: 10px;
  }

  .form-actions .el-button {
    width: 100%;
    height: 44px;
  }
}

/* ============ EXTRA SMALL DEVICES (≤360px) ============ */
@media (max-width: 360px) {
  .create-post-container {
    padding: 10px;
  }

  .create-post-title {
    font-size: 14px;
  }

  .el-form :deep(.el-form-item__label) {
    font-size: 12px;
  }

  .el-form :deep(.el-input),
  .el-form :deep(.el-select) {
    height: 40px;
    font-size: 12px;
  }

  .el-form :deep(.el-textarea__inner) {
    font-size: 12px;
    min-height: 100px;
  }

  .el-form :deep(.el-button) {
    height: 40px;
    font-size: 12px;
  }

  .el-form :deep(.el-button--primary) {
    height: 44px;
  }

  .el-form :deep(.el-picker-panel) {
    width: calc(100vw - 16px) !important;
  }
}
</style>
