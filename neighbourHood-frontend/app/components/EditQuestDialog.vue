<template>
  <el-dialog 
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('edit') + ' ' + $t('questRequests')"
    width="700px"
  >
    <el-form
      v-if="quest"
      ref="questFormRef"
      :model="formData"
      :rules="questRules"
      label-position="top"
    >
      <el-form-item :label="$t('title')" prop="title">
        <el-input
          v-model="formData.title"
          :placeholder="$t('enterQuestTitle')"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item :label="$t('detail')" prop="detail">
        <el-input
          v-model="formData.detail"
          type="textarea"
          :placeholder="$t('enterQuestDetails')"
          :rows="6"
          maxlength="500"
          show-word-limit
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
            :min="10" 
            :max="500" 
            :step="10"
            show-stops
            style="flex: 1;"
          />
          <el-input-number 
            v-model="formData.rewardPoints" 
            :min="10" 
            :max="500"
            :step="10"
            style="width: 120px; margin-left: 16px;"
          />
        </div>
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

  <el-dialog v-model="imagePreviewVisible" width="560px">
    <img style="width: 100%;" :src="previewImageUrl" :alt="$t('imagePreview')" />
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules, type UploadProps, type UploadUserFile } from 'element-plus'
import { Check, Plus } from '@element-plus/icons-vue'

const { t } = useI18n()

interface Quest {
  id: number
  authorId: number | string
  author: string
  avatar: string
  title: string
  detail: string
  tags: string[]
  paymentMethod: 'face-to-face' | 'online'
  rewardPoints: number
  time: string
  photos?: string[]
  acceptedBy: string | null
}

const props = defineProps<{
  modelValue: boolean
  quest: Quest | null
  availableTags: string[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', data: { id: number, title: string, detail: string, tags: string[], paymentMethod: string, rewardPoints: number, photos: string[] }): void
}>()

const questFormRef = ref<FormInstance>()
const saving = ref(false)

const formData = reactive({
  title: '',
  detail: '',
  tags: [] as string[],
  paymentMethod: 'face-to-face' as 'face-to-face' | 'online',
  rewardPoints: 50
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

const questRules = reactive<FormRules>({
  title: [
    {
      required: true,
      message: t('pleaseEnterTitle'),
      trigger: 'blur'
    }
  ],
  detail: [
    {
      required: true,
      message: t('pleaseEnterQuestDetails'),
      trigger: 'blur'
    }
  ]
})

// Load quest data when dialog opens
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.quest) {
    const normalizedAvailableTags = props.availableTags.filter((tag) => !isOtherTag(tag))
    const presetTags = props.quest.tags.filter((tag) => normalizedAvailableTags.includes(tag))
    const extraTags = props.quest.tags.filter((tag) => !normalizedAvailableTags.includes(tag))

    formData.title = props.quest.title
    formData.detail = props.quest.detail
    formData.tags = [...presetTags]
    formData.paymentMethod = props.quest.paymentMethod
    formData.rewardPoints = props.quest.rewardPoints

    customTags.value = [...extraTags]
    showCustomTagInput.value = customTags.value.length > 0
    customTagInput.value = ''

    fileList.value = (props.quest.photos || []).map((photo, index) => ({
      name: `photo-${index + 1}`,
      url: photo
    }))
  }
})

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

async function handleSave() {
  if (!questFormRef.value || !props.quest) return

  await questFormRef.value.validate((valid) => {
    if (valid) {
      saving.value = true
      
      emit('save', {
        id: props.quest!.id,
        title: formData.title,
        detail: formData.detail,
        tags: [...formData.tags, ...customTags.value],
        paymentMethod: formData.paymentMethod,
        rewardPoints: formData.rewardPoints,
        photos: fileList.value
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
      })
      
      saving.value = false
      emit('update:modelValue', false)
    }
  })
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
}
</style>
