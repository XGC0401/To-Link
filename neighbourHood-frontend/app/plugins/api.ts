export default defineNuxtPlugin(() => {
  const config = useRuntimeConfig()
  
  // Set global axios instance baseURL from runtime config
  window.$apiBaseUrl = config.public.apiBaseUrl
})
