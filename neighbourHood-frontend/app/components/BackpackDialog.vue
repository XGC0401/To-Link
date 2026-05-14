<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('myBackpack')"
    width="560px"
    align-center
  >
    <div class="backpack-content">
      <div class="backpack-toolbar">
        <el-input
          v-model="searchText"
          clearable
          :placeholder="$t('searchBackpack')"
          class="backpack-search"
        />
        <el-button plain @click="showHistory = !showHistory">
          {{ showHistory ? $t('backpackItems') : $t('history') }}
        </el-button>
      </div>

      <el-empty v-if="filteredItems.length === 0" :description="$t('noRedeemedRewards')" />

      <div v-else class="backpack-list">
        <div v-for="item in filteredItems" :key="item.id" class="backpack-item" :class="itemClass(item)" @click="handleItemClick(item)">
          <div class="item-main">
            <div class="item-name">{{ item.nameKey ? $t(item.nameKey) : item.name }}</div>
            <div class="item-desc">{{ item.descriptionKey ? $t(item.descriptionKey) : item.description }}</div>
          </div>
          <div class="item-meta">
            <el-tag :type="itemTagType(item)">{{ item.points }} {{ $t('points') }}</el-tag>
            <span v-if="getStatus(item) === 'used'" class="item-status item-status-used">{{ $t('used') }}</span>
            <span v-else-if="getStatus(item) === 'expired'" class="item-status item-status-expired">{{ $t('expired') }}</span>
            <span class="item-time">{{ $t('redeemedAt') }}: {{ formatDate(item.redeemedAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'

interface RedeemedRewardItem {
  id: string
  name: string
  description: string
  nameKey?: string
  descriptionKey?: string
  points: number
  redeemedAt: string
  usedAt?: string | null
  expiresAt?: string | null
}

const props = defineProps<{
  modelValue: boolean
  items: RedeemedRewardItem[]
  historyItems?: RedeemedRewardItem[]
}>()

const { t, locale } = useI18n()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'use-item', value: RedeemedRewardItem): void
  (e: 'update:items', value: RedeemedRewardItem[]): void
}>()

const searchText = ref('')
const showHistory = ref(false)

const getStatus = (item: RedeemedRewardItem) => {
  if (item.usedAt) {
    return 'used'
  }
  const expiresAt = item.expiresAt || ''
  if (expiresAt && new Date(expiresAt).getTime() < Date.now()) {
    return 'expired'
  }
  return 'active'
}

const itemTagType = (item: RedeemedRewardItem) => {
  const status = getStatus(item)
  if (status === 'used') return 'success'
  if (status === 'expired') return 'danger'
  return 'warning'
}

const itemClass = (item: RedeemedRewardItem) => {
  const status = getStatus(item)
  return {
    'is-clickable': status === 'active' && !showHistory.value,
    'is-disabled': status !== 'active' || showHistory.value
  }
}

const filteredItems = computed(() => {
  const q = searchText.value.trim().toLowerCase()
  const source = showHistory.value
    ? (props.historyItems || []).filter((item) => getStatus(item) !== 'active')
    : props.items
  if (!q) return source
  return source.filter((item) => {
    return [item.name, item.description].some((v) => String(v || '').toLowerCase().includes(q))
  })
})

const handleItemClick = async (item: RedeemedRewardItem) => {
  if (showHistory.value || getStatus(item) !== 'active') {
    return
  }

  try {
    await ElMessageBox.confirm(
      `${item.nameKey ? t(item.nameKey) : item.name}\n${item.descriptionKey ? t(item.descriptionKey) : String(item.description || '')}\n\n${t('useItemConfirm')}`,
      t('confirm'),
      {
        confirmButtonText: t('yes'),
        cancelButtonText: t('no'),
        type: 'warning'
      }
    )

    emit('use-item', item)
    ElMessage.success(t('used'))
  } catch {
    // user cancelled
  }
}

function formatDate(isoDate: string) {
  const dt = new Date(isoDate)
  if (Number.isNaN(dt.getTime())) return '-'
  return dt.toLocaleString(locale.value === 'zh' ? 'zh-HK' : 'en-US')
}
</script>

<style scoped>
.backpack-content {
  padding: 6px 0;
}

.backpack-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 12px;
}

.backpack-search {
  flex: 1;
}

.backpack-list {
  display: grid;
  gap: 12px;
  max-height: 52vh;
  overflow: auto;
  padding-right: 4px;
}

.backpack-item {
  border: 1px solid #ebeef5;
  border-radius: 10px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.backpack-item.is-clickable {
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.backpack-item.is-clickable:hover {
  border-color: #67a4ff;
  box-shadow: 0 8px 20px rgba(103, 164, 255, 0.2);
}

.backpack-item.is-disabled {
  opacity: 0.86;
}

.item-main {
  min-width: 0;
}

.item-name {
  font-weight: 700;
  color: var(--tl-text);
}

.item-desc {
  color: #606266;
  font-size: 14px;
  margin-top: 4px;
}

.item-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  white-space: nowrap;
}

.item-time {
  color: #909399;
  font-size: 12px;
}

.item-status {
  font-size: 13px;
  font-weight: 700;
}

.item-status-used {
  color: #15803d;
}

.item-status-expired {
  color: #dc2626;
}
</style>
