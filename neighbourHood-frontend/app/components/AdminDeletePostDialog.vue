<template>
  <el-dialog
    v-model="visible"
    :title="$t('adminDeletePostTitle')"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form :model="form" label-position="top" class="admin-delete-form">
      <!-- Step 1: Tag reason -->
      <el-form-item :label="$t('adminDeleteTagLabel')" required>
        <el-select v-model="form.tag" :placeholder="$t('adminDeleteTagPlaceholder')" style="width: 100%">
          <el-option
            v-for="opt in tagOptions"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
      </el-form-item>

      <!-- Step 2: Description -->
      <el-form-item :label="$t('adminDeleteDescLabel')" required>
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          :placeholder="$t('adminDeleteDescPlaceholder')"
          :maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- Step 3: Confirm phrase -->
      <el-form-item :label="$t('adminDeleteConfirmLabel')" required>
        <p class="confirm-hint">{{ $t('adminDeleteConfirmHint') }}</p>
        <el-input
          v-model="form.confirmText"
          :placeholder="$t('adminDeleteConfirmPlaceholder')"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">{{ $t('cancel') }}</el-button>
      <el-button
        type="danger"
        :disabled="!canConfirm"
        @click="handleConfirm"
      >
        {{ $t('adminDeleteConfirmButton') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'confirm', payload: { tag: string; description: string }): void
}>()

const { t } = useI18n()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = ref({ tag: '', description: '', confirmText: '' })

const tagOptions = computed(() => [
  { value: 'spam', label: t('deleteTagSpam') },
  { value: 'inappropriate', label: t('deleteTagInappropriate') },
  { value: 'misinformation', label: t('deleteTagMisinformation') },
  { value: 'harassment', label: t('deleteTagHarassment') },
  { value: 'duplicate', label: t('deleteTagDuplicate') },
  { value: 'other', label: t('deleteTagOther') }
])

const CONFIRM_PHRASE = 'post remove confirm'

const canConfirm = computed(() =>
  form.value.tag !== '' &&
  form.value.description.trim().length >= 5 &&
  form.value.confirmText.trim().toLowerCase() === CONFIRM_PHRASE
)

const handleConfirm = () => {
  if (!canConfirm.value) return
  emit('confirm', { tag: form.value.tag, description: form.value.description.trim() })
  handleClose()
}

const handleClose = () => {
  visible.value = false
}

// Reset form when dialog opens
watch(() => props.modelValue, (val) => {
  if (val) {
    form.value = { tag: '', description: '', confirmText: '' }
  }
})
</script>

<style scoped>
.admin-delete-form {
  padding: 4px 0;
}

.confirm-hint {
  margin: 0 0 8px;
  color: var(--el-color-danger);
  font-size: 13px;
  font-weight: 500;
}
</style>
