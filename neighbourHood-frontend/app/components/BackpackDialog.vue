<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="$emit('update:modelValue', $event)"
    :title="$t('myBackpack')"
    width="560px"
  >
    <div class="backpack-content">
      <div class="backpack-toolbar">
        <el-input
          v-model="searchText"
          clearable
          :placeholder="$t('searchBackpack')"
          class="backpack-search"
        />
        <el-button plain type="danger" :disabled="items.length === 0" @click="$emit('clear')">
          {{ $t('clearBackpack') }}
        </el-button>
      </div>

      <el-empty v-if="filteredItems.length === 0" :description="$t('noRedeemedRewards')" />

      <div v-else class="backpack-list">
        <div v-for="item in filteredItems" :key="item.id" class="backpack-item">
          <div class="item-main">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-desc">{{ item.description }}</div>
          </div>
          <div class="item-meta">
            <el-tag type="warning">{{ item.points }} {{ $t('points') }}</el-tag>
            <span class="item-time">{{ $t('redeemedAt') }}: {{ formatDate(item.redeemedAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface RedeemedRewardItem {
  id: string
  name: string
  description: string
  points: number
  redeemedAt: string
}

const props = defineProps<{
  modelValue: boolean
  items: RedeemedRewardItem[]
}>()

defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'clear'): void
}>()

const searchText = ref('')

const filteredItems = computed(() => {
  const q = searchText.value.trim().toLowerCase()
  if (!q) return props.items
  return props.items.filter((item) => {
    return [item.name, item.description].some((v) => String(v || '').toLowerCase().includes(q))
  })
})

function formatDate(isoDate: string) {
  const dt = new Date(isoDate)
  if (Number.isNaN(dt.getTime())) return '-'
  return dt.toLocaleString()
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

.item-main {
  min-width: 0;
}

.item-name {
  font-weight: 700;
  color: #303133;
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
</style>
