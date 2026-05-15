<template>
  <NuxtLayout name="default">
    <div class="create-quest-container">
      <el-card class="create-quest-card">
        <template #header>
          <div class="card-header">
            <h2>{{ $t('createQuestRequest') }}</h2>
          </div>
        </template>

        <el-form ref="questFormRef" :model="questForm" :rules="questRules" label-position="top" class="quest-form">
          <el-form-item :label="$t('title')" prop="title">
            <el-input ref="titleInput" v-model="questForm.title" :placeholder="$t('enterQuestTitle')" maxlength="100" show-word-limit @keyup.enter="handleTitleEnter" />
          </el-form-item>

          <el-form-item :label="$t('detail')" prop="detail">
            <el-input
              v-model="questForm.detail"
              type="textarea"
              :placeholder="$t('enterQuestDetails')"
              :rows="5"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-form-item :label="$t('tags')">
            <div class="quest-tags-editor">
              <div class="tag-input-row">
                <el-input v-model="tagInput" :placeholder="$t('addTags')" @keyup.enter.prevent="addTag">
                  <template #append>
                    <el-button :icon="Check" @click="addTag" />
                  </template>
                </el-input>
              </div>
              <div v-if="questForm.tags.length > 0" class="selected-tags">
                <el-tag v-for="tag in questForm.tags" :key="tag" closable round @close="removeTag(tag)">
                  {{ tag }}
                </el-tag>
              </div>
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
            <el-radio-group v-model="questForm.paymentMethod">
              <el-radio-button label="face-to-face">{{ $t('faceToFace') }}</el-radio-button>
              <el-radio-button label="online">{{ $t('online') }}</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item :label="$t('rewardPoints')">
            <el-input-number v-model="questForm.rewardPoints" :min="10" :max="500" :step="10" />
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button size="large" @click="router.back()">{{ $t('cancel') }}</el-button>
              <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
                {{ $t('create') }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <el-dialog v-model="imagePreviewVisible" width="560px" align-center>
      <img style="width: 100%;" :src="previewImageUrl" :alt="$t('imagePreview')" />
    </el-dialog>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, type FormInstance, type FormRules, type UploadProps, type UploadUserFile } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'

const router = useRouter()
const { t } = useI18n()
const questFormRef = ref<FormInstance>()
const titleInput = ref<any>(null)
const submitting = ref(false)
const fileList = ref<UploadUserFile[]>([])
const tagInput = ref('')
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const questForm = reactive({
  title: '',
  detail: '',
  tags: [] as string[],
  paymentMethod: 'face-to-face' as 'face-to-face' | 'online',
  rewardPoints: 50
})

const questRules = reactive<FormRules>({
  title: [{ required: true, message: t('pleaseEnterTitle'), trigger: 'blur' }],
  detail: [{ required: true, message: t('pleaseEnterQuestDetails'), trigger: 'blur' }]
})

const addTag = () => {
  const value = tagInput.value.trim()
  if (!value) return
  if (!value.startsWith('#')) {
    ElMessage.warning(t('tagMustStartWithHash'))
    return
  }
  if (questForm.tags.includes(value)) {
    ElMessage.warning(t('tagAlreadyExists'))
    return
  }
  questForm.tags.push(value)
  tagInput.value = ''
}

const removeTag = (tag: string) => {
  questForm.tags = questForm.tags.filter((item) => item !== tag)
}

const handleTitleEnter = () => {
  handleSubmit()
}

const handlePictureCardPreview: UploadProps['onPreview'] = (uploadFile) => {
  previewImageUrl.value = uploadFile.url || ''
  imagePreviewVisible.value = true
}

const handleSubmit = async () => {
  if (!questFormRef.value || submitting.value) return
  await questFormRef.value.validate((valid) => {
    if (valid) {
      createQuest()
    } else {
      ElMessage.warning(t('fillRequired'))
    }
  })
}

const createQuest = () => {
  submitting.value = true
  try {
    const profile = JSON.parse(localStorage.getItem('userProfile') || '{}')
    const existingQuests = JSON.parse(localStorage.getItem('userQuests') || '[]')

    const nextId = existingQuests.length > 0
      ? Math.max(...existingQuests.map((item: any) => Number(item.id) || 0)) + 1
      : 1

    const photos = fileList.value
      .map((file) => {
        if (file.url) return file.url
        if (file.raw) return URL.createObjectURL(file.raw)
        return ''
      })
      .filter(Boolean)

    const newQuest = {
      id: nextId,
      authorId: String(profile?.uuid || profile?.email || 'guest'),
      author: String(profile?.name || profile?.username || 'Current User'),
      avatar: String(profile?.avatar || 'https://cube.elemecdn.com/0/88/03b0f476b63c5258a53e1b43f2ecb3.svg'),
      title: questForm.title,
      detail: questForm.detail,
      tags: [...questForm.tags],
      paymentMethod: questForm.paymentMethod,
      rewardPoints: questForm.rewardPoints,
      photos,
      time: 'Just now',
      acceptedBy: null,
      acceptedById: null,
      status: 'open',
      pendingApplicants: []
    }

    localStorage.setItem('userQuests', JSON.stringify([newQuest, ...existingQuests]))
    ElMessage.success(t('questCreatedSuccess'))
    router.push('/posts')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-quest-container {
  width: 100%;
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.create-quest-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
}

.card-header h2 {
  margin: 0;
}

.quest-form {
  padding: 12px 0;
}

.quest-tags-editor {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.form-actions {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
