<template>
  <NuxtPage />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { loadAndApplyThemeFromStorage } from '@/utils/theme'

onMounted(() => {
  loadAndApplyThemeFromStorage()
})
</script>

<style>
html{
  height: 100%;
  min-height: 100vh;
}
</style>
