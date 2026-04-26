<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('createQuestRequest')"
    width="600px"
  >
    <el-form :model="formData" label-position="top">
      <el-form-item :label="$t('title')">
        <el-input
          v-model="formData.title"
          :placeholder="$t('enterQuestTitle')"
        />
      </el-form-item>

      <el-form-item :label="$t('detail')">
        <el-input
          v-model="formData.detail"
          type="textarea"
          :rows="4"
          :placeholder="$t('enterQuestDetails')"
        />
      </el-form-item>

      <el-form-item :label="$t('tags')">
        <div class="tag-selector">
          <el-button
            v-for="tag in availableTags"
            :key="tag"
            :type="isTagSelected(tag) ? 'primary' : 'default'"
            @click="toggleTag(tag)"
          >
            {{ tag }}
          </el-button>
        </div>

        <div v-if="showCustomTagInput" class="custom-tag-section">
          <el-input
            v-model="customTagInput"
            :placeholder="$t('addTags')"
            @keyup.enter.prevent="addCustomTag"
          >
            <template #append>
              <el-button :icon="Check" @click="addCustomTag" />
            </template>
          </el-input>
        </div>

        <div v-if="customTags.length > 0" class="selected-custom-tags">
          <el-tag
            v-for="tag in customTags"
            :key="tag"
            closable
            round
            @close="removeCustomTag(tag)"
          >
            {{ tag }}
          </el-tag>
        </div>
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

      <el-form-item :label="$t('paymentMethod')">
        <el-radio-group v-model="formData.paymentMethod">
          <el-radio-button label="face-to-face">
            {{ $t('faceToFace') }}
          </el-radio-button>
          <el-radio-button label="online">
            {{ $t('online') }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('rewardPoints')">
        <div class="reward-points-input">
          <el-slider
            v-model="formData.rewardPoints"
            :min="0"
            :max="500"
            :step="50"
            :marks="marks"
            show-stops
          />
          <el-input-number
            v-model="formData.rewardPoints"
            :min="0"
            :max="500"
            :step="10"
            controls-position="right"
            style="width: 140px;"
          />
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">
        {{ $t('cancel') }}
      </el-button>
      <el-button type="primary" @click="handleCreate">
        {{ $t('create') }}
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="imagePreviewVisible" width="560px">
    <img style="width: 100%;" :src="previewImageUrl" :alt="$t('imagePreview')" />
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type UploadProps, type UploadUserFile } from 'element-plus'
import { Check, Plus } from '@element-plus/icons-vue'

interface QuestFormData {
  title: string
  detail: string
  tags: string[]
  paymentMethod: 'face-to-face' | 'online'
  rewardPoints: number
  photos: string[]
}

defineProps<{
  modelValue: boolean
  language: 'en' | 'zh'
  availableTags: string[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'create', data: QuestFormData): void
}>()

const { t } = useI18n()

const marks = {
  0: '0',
  50: '50',
  100: '100',
  150: '150',
  200: '200',
  250: '250',
  300: '300',
  350: '350',
  400: '400',
  450: '450',
  500: '500'
}

const formData = reactive<QuestFormData>({
  title: '',
  detail: '',
  tags: [],
  paymentMethod: 'face-to-face',
  rewardPoints: 50,
  photos: []
})

const customTagInput = ref('')
const customTags = ref<string[]>([])
const showCustomTagInput = ref(false)
const fileList = ref<UploadUserFile[]>([])
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const isOtherTag = (tag: string) => tag === t('other')

const isTagSelected = (tag: string) => {
  if (isOtherTag(tag)) {
    return showCustomTagInput.value
  }

  return formData.tags.includes(tag)
}

function toggleTag(tag: string) {
  if (isOtherTag(tag)) {
    showCustomTagInput.value = !showCustomTagInput.value
    return
  }

  const index = formData.tags.indexOf(tag)
  if (index > -1) {
    formData.tags.splice(index, 1)
  } else {
    formData.tags.push(tag)
  }
}

function addCustomTag() {
  const value = customTagInput.value.trim()
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

  if (customTags.value.includes(value)) {
    ElMessage.warning(t('tagAlreadyExists'))
    return
  }

  customTags.value.push(value)
  customTagInput.value = ''
}

function removeCustomTag(tag: string) {
  customTags.value = customTags.value.filter((item) => item !== tag)
}

const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  previewImageUrl.value = uploadFile.url || ''
  imagePreviewVisible.value = true
}

function handleCreate() {
  const photos = fileList.value
    .map((file) => {
      if (file.url) {
        return file.url
      }

      if (file.raw) {
        return URL.createObjectURL(file.raw)
      }

      return ''
    })
    .filter(Boolean)

  emit('create', {
    ...formData,
    tags: [...formData.tags, ...customTags.value],
    photos
  })

  formData.title = ''
  formData.detail = ''
  formData.tags = []
  formData.paymentMethod = 'face-to-face'
  formData.rewardPoints = 50
  formData.photos = []
  customTagInput.value = ''
  customTags.value = []
  showCustomTagInput.value = false
  fileList.value = []
}
</script>

<style scoped>
.tag-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.custom-tag-section {
  margin-top: 10px;
  max-width: 420px;
}

.selected-custom-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.reward-points-input {
  display: flex;
  align-items: center;
  gap: 16px;
  width: 100%;
}

.reward-points-input :deep(.el-slider) {
  flex: 1;
  margin-right: 12px;
  padding: 0 8px 20px 8px;
}

.reward-points-input :deep(.el-slider__runway) {
  height: 8px;
}

.reward-points-input :deep(.el-slider__bar) {
  height: 8px;
}

.reward-points-input :deep(.el-slider__button-wrapper) {
  top: -16px;
}

.reward-points-input :deep(.el-slider__button) {
  width: 24px;
  height: 24px;
}

.reward-points-input :deep(.el-slider__stop) {
  width: 4px;
  height: 8px;
  background-color: #dcdfe6;
}

.reward-points-input :deep(.el-slider__marks-text) {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

:deep(.el-input-number) {
  line-height: 1;
}

:deep(.el-input-number__decrease),
:deep(.el-input-number__increase) {
  background-color: transparent;
  color: #606266;
}

:deep(.el-input-number__decrease:hover),
:deep(.el-input-number__increase:hover) {
  color: var(--el-color-primary);
}

.dark :deep(.el-input-number__decrease),
.dark :deep(.el-input-number__increase) {
  color: #e5e5e5 !important;
  background-color: transparent !important;
}

.dark :deep(.el-input-number__decrease:hover),
.dark :deep(.el-input-number__increase:hover) {
  color: var(--el-color-primary) !important;
}

.dark :deep(.el-input-number .el-input__wrapper) {
  background-color: #3d3d3d;
  box-shadow: 0 0 0 1px #555 inset;
}

.dark :deep(.el-input-number .el-input__inner) {
  color: #e5e5e5;
}

.dark :deep(.el-slider__button) {
  border-color: var(--el-color-primary);
  background-color: #fff;
}

.dark :deep(.el-slider__bar) {
  background-color: var(--el-color-primary);
}

.dark :deep(.el-slider__runway) {
  background-color: #4c4d4f;
}

.dark :deep(.el-slider__stop) {
  background-color: #6c6d6f;
}

.dark :deep(.el-slider__marks-text) {
  color: #a8abb2;
}
</style>
