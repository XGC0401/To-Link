<template>
  <el-dialog 
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('edit') + ' ' + $t('posts')"
    width="700px"
    align-center
  >
    <el-form
      v-if="post"
      ref="postFormRef"
      :model="formData"
      :rules="postRules"
      label-position="top"
    >
      <el-form-item :label="$t('title')" prop="title">
        <el-input
          ref="titleInput"
          v-model="formData.title"
          :placeholder="$t('enterTitle')"
          maxlength="100"
          show-word-limit
          @keyup.enter="handleTitleEnter"
        />
      </el-form-item>

      <el-form-item :label="$t('category')" prop="request_type">
        <el-select
          v-model="formData.request_type"
          :placeholder="$t('selectCategory')"
          style="width: 100%;"
        >
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

      <el-form-item v-if="formData.request_type === 4" :label="$t('customCategory')" prop="custom_category">
        <el-input
          v-model="formData.custom_category"
          :placeholder="$t('customCategoryPlaceholder')"
          maxlength="40"
          show-word-limit
        />
      </el-form-item>

      <el-form-item :label="$t('tagsOptional')">
        <div class="post-tags-editor">
          <div class="tag-input-row">
            <el-input
              v-model="tagInput"
              :placeholder="$t('addTags')"
              @keyup.enter.prevent="addTag"
            />
            <el-button :icon="Plus" @click="addTag" />
          </div>
          <div v-if="formData.tags.length > 0" class="selected-tags">
            <el-tag v-for="tag in formData.tags" :key="tag" closable @close="removeTag(tag)">
              {{ tag }}
            </el-tag>
          </div>
          <p v-else class="tag-hint">{{ $t('tagHint') }}</p>
        </div>
      </el-form-item>

      <el-form-item :label="$t('content')" prop="content">
        <el-input
          v-model="formData.content"
          type="textarea"
          :placeholder="$t('shareThoughts')"
          :rows="8"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item :label="$t('imagesOptional')">
        <el-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :limit="5"
          :auto-upload="false"
          accept="image/*"
          :on-preview="handlePictureCardPreview"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">
        {{ $t('cancel') }}
      </el-button>
      <el-button 
        type="primary" 
        :loading="saving"
        @click="handleSave"
      >
        {{ $t('save') }}
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="imagePreviewVisible" width="560px" align-center>
    <img style="width: 100%;" :src="previewImageUrl" :alt="$t('imagePreview')" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadProps, UploadUserFile } from 'element-plus'

const { t, locale } = useI18n()
const language = computed(() => locale.value as 'en' | 'zh')

interface Post {
  id: number
  request_type: number
  custom_category?: string
  tags?: string[]
  postPhotos?: Array<{ id?: string; url: string }>
  title: string
  content: string
}

const props = defineProps<{
  modelValue: boolean
  post: Post | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', data: { id: number, title: string, content: string, request_type: number, custom_category?: string, tags: string[], photos: string[] }): void
}>()

const postFormRef = ref<FormInstance>()
const titleInput = ref<any>(null)
const saving = ref(false)

const formData = reactive({
  title: '',
  request_type: 0,
  custom_category: '',
  tags: [] as string[],
  content: ''
})

const fileList = ref<UploadUserFile[]>([])
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')
const tagInput = ref('')

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
        if (formData.request_type !== 4) {
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

// Load post data when dialog opens
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.post) {
    formData.title = props.post.title
    formData.content = props.post.content
    formData.request_type = Number(props.post.request_type ?? 0)
    formData.custom_category = String(props.post.custom_category || '')
    formData.tags = Array.isArray(props.post.tags) ? [...props.post.tags] : []
    fileList.value = (props.post.postPhotos || []).map((photo, index) => ({
      name: `photo-${index + 1}`,
      url: photo.url
    }))
  }
})

const addTag = () => {
  const value = tagInput.value.trim()
  if (!value) return
  if (!value.startsWith('#') || /\s/.test(value) || formData.tags.includes(value)) return
  formData.tags.push(value)
  tagInput.value = ''
}

const removeTag = (tag: string) => {
  formData.tags = formData.tags.filter((item) => item !== tag)
}

const handleTitleEnter = () => {
  handleSave()
}

const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  previewImageUrl.value = uploadFile.url || (uploadFile.raw ? URL.createObjectURL(uploadFile.raw) : '')
  imagePreviewVisible.value = true
}

const fileToDataUrl = (file: File) => new Promise<string>((resolve, reject) => {
  const reader = new FileReader()
  reader.onload = () => resolve(typeof reader.result === 'string' ? reader.result : '')
  reader.onerror = () => reject(reader.error)
  reader.readAsDataURL(file)
})

async function handleSave() {
  if (!postFormRef.value || !props.post) return

  const valid = await postFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const photos = await Promise.all(
      fileList.value.map(async (file) => {
        if (file.url) {
          return file.url
        }
        if (file.raw) {
          return await fileToDataUrl(file.raw)
        }
        return ''
      })
    )

    emit('save', {
      id: props.post.id,
      title: formData.title,
      content: formData.content,
      request_type: formData.request_type,
      custom_category: formData.request_type === 4 ? formData.custom_category.trim() : undefined,
      tags: [...formData.tags],
      photos: photos.filter(Boolean)
    })

    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.el-form {
  padding: 8px 0;
}

.post-tags-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}

.tag-input-row {
  display: flex;
  gap: 8px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-hint {
  margin: 0;
  color: var(--tl-text-muted);
}
</style>
