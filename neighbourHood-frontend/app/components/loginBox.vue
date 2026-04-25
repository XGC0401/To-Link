<template>
  <div class="login-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="brand-side">
      <div class="brand-content">
        <h1 class="brand-title">{{ t('brandTitle') }}</h1>
        <p class="brand-description">{{ t('brandDescription') }}</p>
      </div>
    </div>

    <div class="form-side">
      <div class="tech-bg">
        <svg class="tech-grid" viewBox="0 0 800 600" preserveAspectRatio="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
          <defs>
            <linearGradient id="lg" x1="0" x2="1">
              <stop offset="0" stop-color="rgba(255,255,255,0.05)" />
              <stop offset="1" stop-color="rgba(255,255,255,0.02)" />
            </linearGradient>
          </defs>
          <rect width="100%" height="100%" fill="url(#lg)" />
          <g class="grid-lines" stroke="rgba(255,255,255,0.03)" stroke-width="1">
            <!-- vertical lines -->
            <g>
              <line v-for="i in 20" :key="i" :x1="(i*40)" y1="0" :x2="(i*40)" y2="600" />
            </g>
            <!-- horizontal lines -->
            <g>
              <line v-for="j in 15" :key="j" x1="0" :y1="(j*40)" x2="800" :y2="(j*40)" />
            </g>
          </g>
        </svg>
      </div>

      <el-card ref="cardRef" class="login-card">
        <div class="theme-switcher-left">
          <el-switch
            v-model="isDarkMode"
            @change="toggleTheme"
            size="large"
            :active-action-icon="Moon"
            :inactive-action-icon="Sunny"
            inline-prompt
          />
        </div>
        <div class="locale-switcher-right">
          <el-dropdown @command="handleLanguageChange">
            <el-button type="default">
              {{ currentLanguage?.name }}<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="lang in availableLanguages" :key="lang.code" :command="lang.code">
                  {{ lang.name }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <div class="mode-tabs">
          <button 
            class="tab-button" 
            :class="{ active: isLoginMode }"
            @click="isLoginMode = true"
          >
            {{ t('login') }}
          </button>
          <button 
            class="tab-button" 
            :class="{ active: !isLoginMode }"
            @click="isLoginMode = false"
          >
            {{ t('register') }}
          </button>
        </div>

        <!-- Login Form -->
        <div v-if="isLoginMode" class="form-wrapper">
          <div class="login-header">
            <h1 class="login-title">{{ t('loginTitle') }}</h1>
            <p class="login-subtitle">{{ t('loginSubtitle') }}</p>
          </div>

          <el-alert v-if="showError" :title="errorMessage" type="error" :closeable="true" @close="showError = false"
            show-icon class="error-alert" />

          <el-form :model="loginForm" ref="loginFormRef" label-position="top" :rules="loginRules">
            <el-form-item :label="t('email')" prop="email">
              <el-input v-model="loginForm.email" :placeholder="t('emailPlaceholder')"></el-input>
            </el-form-item>

            <el-form-item :label="t('password')" prop="password">
              <el-input :type="passwordVisible ? 'text' : 'password'" v-model="loginForm.password" :placeholder="t('passwordPlaceholder')">
                <template #suffix>
                  <el-button text @click="togglePasswordVisibility">
                    <el-icon>
                      <component :is="passwordVisible ? TurnOff : View" />
                    </el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <el-checkbox v-model="rememberMe">{{ t('rememberMe') }}</el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleLogin" class="log-in-btn" :loading="isLoggingIn" :disabled="isLoggingIn">
                {{ isLoggingIn ? t('loggingIn') : t('login') }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- Register Form -->
        <div v-else class="form-wrapper">
          <div class="login-header">
            <h1 class="login-title">{{ t('createAccount') || 'Create Account' }}</h1>
            <p class="login-subtitle">{{ t('registerSubtitle') || 'Sign up to join our community' }}</p>
          </div>

          <el-alert v-if="showError" :title="errorMessage" type="error" :closeable="true" @close="showError = false"
            show-icon class="error-alert" />

          <el-form :model="registerForm" ref="registerFormRef" label-position="top" :rules="registerRules">
            <el-form-item :label="t('name') || 'Name'" prop="name">
              <el-input v-model="registerForm.name" :placeholder="t('namePlaceholder') || 'e.g., Chan Tai Man'"></el-input>
            </el-form-item>

            <el-form-item :label="t('age') || 'Age'" prop="age">
              <el-input v-model="registerForm.age" :placeholder="t('agePlaceholder') || 'e.g., 18'"></el-input>
            </el-form-item>

            <el-form-item :label="t('hkid') || 'HKID'" prop="hkid">
              <el-input v-model="registerForm.hkid" :placeholder="t('hkidPlaceholder') || 'e.g., S112233(4)'"></el-input>
            </el-form-item>

            <el-form-item :label="t('address1') || 'Address Line 1'" prop="address1">
              <el-input v-model="registerForm.address1" :placeholder="t('address1Placeholder') || 'Flat/Unit and Floor'">
              </el-input>
            </el-form-item>

            <el-form-item :label="t('address2') || 'Address Line 2'" prop="address2">
              <el-input v-model="registerForm.address2"
                :placeholder="t('address2Placeholder') || 'Building/Block and Street'">
              </el-input>
            </el-form-item>

            <el-form-item :label="t('address3') || 'Address Line 3 (Optional)'" prop="address3">
              <el-input v-model="registerForm.address3"
                :placeholder="t('address3Placeholder') || 'District and Region (Optional)'"></el-input>
            </el-form-item>

            <el-form-item :label="t('phone') || 'Phone'" prop="phone">
              <el-input v-model="registerForm.phone" :placeholder="t('phonePlaceholder') || 'e.g., 9123 4567'">
                <template #prepend>
                  <el-select v-model="selectedCountryDialCode" class="country-code-select" filterable>
                    <!-- Nearest section -->
                    <el-optgroup v-if="nearestCountryOption" :label="`${t('nearestCountrySection') || 'Nearest'} - ${nearestCity || (t('unknownLocation') || 'Unknown')}`" class="country-group-nearest">
                      <el-option
                        :key="`nearest-${nearestCountryOption.countryCode}`"
                        :label="`${getLocalizedCountryName(nearestCountryOption.countryCode, nearestCountryOption.countryName)} (${nearestCountryOption.dialCode})`"
                        :value="nearestCountryOption.dialCode"
                      />
                    </el-optgroup>

                    <!-- Alphabetical country groups -->
                    <el-optgroup
                      v-for="group in countryGroupedByLetter"
                      :key="`group-${group.letter}`"
                      :label="group.letter"
                      class="country-group"
                    >
                      <el-option
                        :key="`separator-${group.letter}`"
                        :label="`${group.letter} -------`"
                        :value="`__separator_${group.letter}`"
                        disabled
                        class="country-divider-option"
                      />
                      <el-option
                        v-for="country in group.countries"
                        :key="`${country.countryCode}-${country.dialCode}`"
                        :label="country.dialCode"
                        :value="country.dialCode"
                      >
                        <span>{{ getLocalizedCountryName(country.countryCode, country.countryName) }} ({{ country.dialCode }})</span>
                      </el-option>
                    </el-optgroup>
                  </el-select>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item :label="t('email')" prop="email">
              <el-input v-model="registerForm.email" :placeholder="t('emailPlaceholder')"></el-input>
            </el-form-item>

            <el-form-item :label="t('password')" prop="password">
              <el-input :type="registerPasswordVisible ? 'text' : 'password'" v-model="registerForm.password" :placeholder="t('passwordPlaceholder')">
                <template #suffix>
                  <el-button text @click="toggleRegisterPasswordVisibility">
                    <el-icon>
                      <component :is="registerPasswordVisible ? TurnOff : View" />
                    </el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item :label="t('status') || 'Status (Optional)'" prop="status">
              <el-select v-model="registerForm.status" :placeholder="t('selectStatus') || 'Select status'" clearable>
                <el-option :label="t('statusStudent') || 'Student'" value="Student" />
                <el-option :label="t('statusWorker') || 'Worker'" value="Worker" />
                <el-option :label="t('statusFreelancer') || 'Freelancer'" value="Freelancer" />
                <el-option :label="t('statusRetired') || 'Retired'" value="Retired" />
                <el-option :label="t('statusOther') || 'Other'" value="Other" />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleRegister" class="log-in-btn" :loading="isRegistering" :disabled="isRegistering">
                {{ isRegistering ? t('registering') || 'Registering...' : t('register') || 'Register' }}
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted, onUnmounted } from 'vue'
import { navigateTo } from '#app'
import type { FormInstance } from 'element-plus'
import type { loginParams, registerParams } from '../api/types/common'
import { useI18n } from 'vue-i18n'
import { getUser, login, register } from '~/api/auth'
import { ArrowDown, Moon, Sunny, View, TurnOff } from '@element-plus/icons-vue'
import { Storage } from '~/utils/storage'
import { ElMessage } from 'element-plus'

interface CountryDialOption {
  countryName: string
  countryCode: string
  dialCode: string
}

interface CountryGroup {
  letter: string
  countries: CountryDialOption[]
}

const { t, locale, setLocale } = useI18n()
const isDarkMode = ref(false)
const isLoginMode = ref(true)

const passwordVisible = ref(false)
const registerPasswordVisible = ref(false)
const rememberMe = ref(false)
const cardRef = ref<HTMLElement | null>(null)

const availableLanguages = computed(() => [
  { code: 'en', name: t('languageEnglish') },
  { code: 'zh', name: t('languageChineseTraditional') },
])

const currentLanguage = computed(() =>
  availableLanguages.value.find(lang => lang.code === locale.value)
)

const loginForm = reactive<loginParams>({
  email: '',
  password: ''
})

const registerForm = reactive<registerParams>({
  name: '',
  age: '',
  hkid: '',
  address1: '',
  address2: '',
  address3: '',
  phone: '',
  email: '',
  password: '',
  status: ''
})

const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()
const showError = ref(false)
const errorMessage = ref('')
const isLoggingIn = ref(false)
const isRegistering = ref(false)
const selectedCountryDialCode = ref('+852')
const countryDialOptions = ref<CountryDialOption[]>([])
const detectedCountryCode = ref('')
const nearestCity = ref('')

const getDisplayLocale = () => (locale.value === 'zh' ? 'zh-Hant' : 'en')

const getLocalizedCountryName = (countryCode: string, fallbackName: string) => {
  try {
    const displayNames = new Intl.DisplayNames([getDisplayLocale()], { type: 'region' })
    return displayNames.of(countryCode) || fallbackName
  } catch {
    return fallbackName
  }
}

const localizedCountryDialOptions = computed(() => {
  return [...countryDialOptions.value].sort((a, b) => {
    const nameA = getLocalizedCountryName(a.countryCode, a.countryName)
    const nameB = getLocalizedCountryName(b.countryCode, b.countryName)
    return nameA.localeCompare(nameB)
  })
})

const nearestCountryOption = computed(() => {
  if (!detectedCountryCode.value) {
    return null
  }
  return localizedCountryDialOptions.value.find((item) => item.countryCode === detectedCountryCode.value) || null
})

const countryGroupedByLetter = computed<CountryGroup[]>(() => {
  const allOptions = localizedCountryDialOptions.value
  const nearest = nearestCountryOption.value
  
  let countriesForGrouping = allOptions
  if (nearest) {
    countriesForGrouping = allOptions.filter((item) => item.countryCode !== nearest.countryCode)
  }

  const grouped = new Map<string, CountryDialOption[]>()
  for (const country of countriesForGrouping) {
    const localizedName = getLocalizedCountryName(country.countryCode, country.countryName)
    const letter = localizedName.charAt(0).toUpperCase()
    if (!grouped.has(letter)) {
      grouped.set(letter, [])
    }
    grouped.get(letter)!.push(country)
  }

  const result: CountryGroup[] = []
  const sortedLetters = Array.from(grouped.keys()).sort()
  for (const letter of sortedLetters) {
    result.push({
      letter,
      countries: grouped.get(letter)!.sort((a, b) => {
        const nameA = getLocalizedCountryName(a.countryCode, a.countryName)
        const nameB = getLocalizedCountryName(b.countryCode, b.countryName)
        return nameA.localeCompare(nameB)
      }),
    })
  }

  return result
})

const loginRules = computed(() => ({
  email: [
    { required: true, message: t('emailPlaceholder'), trigger: 'blur' },
  ],
  password: [
    {
      required: true,
      message: t('passwordPlaceholder'), trigger: 'blur'
    },
  ]
}))

const registerRules = computed(() => ({
  name: [
    { required: true, message: t('nameRequired') || 'Name is required', trigger: 'blur' },
  ],
  age: [
    { required: true, message: t('ageRequired') || 'Age is required', trigger: 'blur' },
  ],
  hkid: [
    { required: true, message: t('hkidRequired') || 'HKID is required', trigger: 'blur' },
  ],
  address1: [
    { required: true, message: t('address1Required') || 'Address line 1 is required', trigger: 'blur' },
  ],
  address2: [
    { required: true, message: t('address2Required') || 'Address line 2 is required', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: t('phoneRequired') || 'Phone number is required', trigger: 'blur' },
    { pattern: /^[0-9()\-\s]{6,20}$/, message: t('phoneInvalid') || 'Invalid phone number', trigger: 'blur' },
  ],
  email: [
    { required: true, message: t('emailRequired') || 'Email is required', trigger: 'blur' },
    { type: 'email', message: t('emailInvalid') || 'Invalid email', trigger: 'blur' },
  ],
  password: [
    { required: true, message: t('passwordRequired') || 'Password is required', trigger: 'blur' },
    { min: 6, message: t('passwordMinLength') || 'Password must be at least 6 characters', trigger: 'blur' },
  ],
}))

const handleLogin = async () => {
  if (isLoggingIn.value) {
    return
  }

  if (!loginFormRef.value) {
    return
  }

  isLoggingIn.value = true
  try {
    await loginFormRef.value.validate()
    const [error, data] = await login(loginForm)
    if (!error && data) {
      const token = typeof data.data === 'string' ? data.data : ''
      const isValidJwt = token.split('.').length === 3
      if (!isValidJwt) {
        showError.value = true
        errorMessage.value = t('invalidCredentials')
        Storage.remove("token")
        return
      }
      Storage.set("token", token)
      if (rememberMe.value) {
        localStorage.setItem('rememberEmail', 'true')
        localStorage.setItem('rememberEmailValue', loginForm.email)
      } else {
        localStorage.removeItem('rememberEmail')
        localStorage.removeItem('rememberEmailValue')
      }
      getUser()
      await navigateTo('/home')
    } else if (error) {
      showError.value = true
      const errorCode = error.response?.data?.code || 'UNKNOWN'
      const serverMessage = error.response?.data?.message || error.response?.data || ''
      if (serverMessage) {
        errorMessage.value = `[${errorCode}] ${String(serverMessage)}`
      } else if (error.response?.status) {
        errorMessage.value = `[${errorCode}] ${t('invalidCredentials')} (HTTP ${error.response.status})`
      } else {
        errorMessage.value = `[${errorCode}] ${t('invalidCredentials')}`
      }
    }
  } catch (error) {
    showError.value = true
    errorMessage.value = t('logingFailed')
  } finally {
    isLoggingIn.value = false
  }
}

const handleRegister = async () => {
  if (isRegistering.value) {
    return
  }

  if (!registerFormRef.value) {
    return
  }

  isRegistering.value = true
  try {
    await registerFormRef.value.validate()
    const registerPayload: registerParams = {
      ...registerForm,
      phone: `${selectedCountryDialCode.value} ${registerForm.phone}`.trim(),
    }

    const [error, data] = await register(registerPayload)
    if (!error && data) {
      showError.value = false
      // After successful registration, switch to login mode
      isLoginMode.value = true
      // Clear form
      registerForm.name = ''
      registerForm.age = ''
      registerForm.hkid = ''
      registerForm.address1 = ''
      registerForm.address2 = ''
      registerForm.address3 = ''
      registerForm.phone = ''
      registerForm.email = ''
      registerForm.password = ''
      registerForm.status = ''
      ElMessage.success(t('registrationSuccess') || 'Registration successful! Please log in.')
    } else if (error) {
      showError.value = true
      const errorCode = error.response?.data?.code || 'UNKNOWN'
      const serverMessage = error.response?.data?.message || error.response?.data || ''
      if (serverMessage) {
        errorMessage.value = `[${errorCode}] ${String(serverMessage)}`
      } else if (error.response?.status) {
        errorMessage.value = `[${errorCode}] ${t('registrationFailed')} (HTTP ${error.response.status})`
      } else {
        errorMessage.value = `[${errorCode}] ${t('registrationFailed') || 'Registration failed'}`
      }
    }
  } catch (error) {
    showError.value = true
    errorMessage.value = t('registrationFailed') || 'Registration failed'
  } finally {
    isRegistering.value = false
  }
}

const handleLanguageChange = () => {
  const newLocale = locale.value === 'en' ? 'zh' : 'en'
  setLocale(newLocale)
  localStorage.setItem('userLanguage', newLocale)
}

const togglePasswordVisibility = () => {
  passwordVisible.value = !passwordVisible.value
}

const toggleRegisterPasswordVisibility = () => {
  registerPasswordVisible.value = !registerPasswordVisible.value
}

const toggleTheme = () => {
  localStorage.setItem('loginDarkMode', isDarkMode.value.toString())
  applyTheme()
}

const applyTheme = () => {
  if (isDarkMode.value) {
    document.body.classList.add('dark')
  } else {
    document.body.classList.remove('dark')
  }
}

const fallbackCountryDialOptions: CountryDialOption[] = [
  { countryName: 'China', countryCode: 'CN', dialCode: '+86' },
  { countryName: 'Hong Kong', countryCode: 'HK', dialCode: '+852' },
  { countryName: 'Japan', countryCode: 'JP', dialCode: '+81' },
  { countryName: 'Singapore', countryCode: 'SG', dialCode: '+65' },
  { countryName: 'South Korea', countryCode: 'KR', dialCode: '+82' },
  { countryName: 'Taiwan', countryCode: 'TW', dialCode: '+886' },
  { countryName: 'United Kingdom', countryCode: 'GB', dialCode: '+44' },
  { countryName: 'United States', countryCode: 'US', dialCode: '+1' },
].sort((a, b) => a.countryName.localeCompare(b.countryName))

const loadCountryDialOptions = async () => {
  try {
    const res = await fetch('https://restcountries.com/v3.1/all?fields=name,cca2,idd')
    if (!res.ok) {
      throw new Error('Failed to fetch country list')
    }

    const countries = await res.json()
    const parsed = (countries as Array<any>)
      .map((item) => {
        const countryName = item?.name?.common
        const countryCode = item?.cca2?.toUpperCase?.()
        const root = item?.idd?.root || ''
        const suffix = Array.isArray(item?.idd?.suffixes) && item.idd.suffixes.length > 0
          ? String(item.idd.suffixes[0])
          : ''
        const dialCode = `${root}${suffix}`

        return {
          countryName,
          countryCode,
          dialCode,
        }
      })
      .filter((item) => item.countryName && item.countryCode && /^\+\d+$/.test(item.dialCode))

    const uniqueByCountry = new Map<string, CountryDialOption>()
    for (const item of parsed) {
      if (!uniqueByCountry.has(item.countryCode)) {
        uniqueByCountry.set(item.countryCode, item as CountryDialOption)
      }
    }

    countryDialOptions.value = Array.from(uniqueByCountry.values())

    if (!countryDialOptions.value.some((item) => item.dialCode === selectedCountryDialCode.value)) {
      selectedCountryDialCode.value = countryDialOptions.value[0]?.dialCode || '+852'
    }
  } catch {
    countryDialOptions.value = fallbackCountryDialOptions
  }
}

const detectNearestCountry = async () => {
  if (!navigator.geolocation) {
    return
  }

  navigator.geolocation.getCurrentPosition(async (position) => {
    try {
      const { latitude, longitude } = position.coords
      const reverseUrl = `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${latitude}&lon=${longitude}`
      const reverseRes = await fetch(reverseUrl)
      if (!reverseRes.ok) {
        return
      }

      const reverseData = await reverseRes.json()
      const countryCode = reverseData?.address?.country_code?.toUpperCase?.() || ''
      if (!countryCode) {
        return
      }

      const address = reverseData?.address || {}
      nearestCity.value = address.city || address.town || address.village || address.county || address.state || ''

      detectedCountryCode.value = countryCode
      const nearest = countryDialOptions.value.find((item) => item.countryCode === countryCode)
      if (nearest) {
        selectedCountryDialCode.value = nearest.dialCode
      }
    } catch {
      // Ignore location lookup failures and keep alphabetical list fallback.
    }
  })
}

onMounted(() => {
  loadCountryDialOptions()
  detectNearestCountry()

  const savedLanguage = localStorage.getItem('userLanguage')
  if (savedLanguage && (savedLanguage === 'en' || savedLanguage === 'zh')) {
    setLocale(savedLanguage)
  }
  
  const savedRemember = localStorage.getItem('rememberEmail')
  if (savedRemember === 'true') {
    rememberMe.value = true
    loginForm.email = localStorage.getItem('rememberEmailValue') || ''
  }

  const savedTheme = localStorage.getItem('loginDarkMode')
  if (savedTheme === 'true') {
    isDarkMode.value = true
    applyTheme()
  }

  const handlePointer = (e: PointerEvent) => {
    if (!cardRef.value) return
    const rect = cardRef.value.getBoundingClientRect()
    const cx = rect.left + rect.width / 2
    const cy = rect.top + rect.height / 2
    const dx = e.clientX - cx
    const dy = e.clientY - cy
    const rx = (dy / rect.height) * -6
    const ry = (dx / rect.width) * 8
    cardRef.value.style.transform = `perspective(900px) rotateX(${rx}deg) rotateY(${ry}deg) translateZ(8px)`
  }

  const handlePointerLeave = () => {
    if (!cardRef.value) return
    cardRef.value.style.transform = ''
  }

  window.addEventListener('pointermove', handlePointer)
  window.addEventListener('pointerleave', handlePointerLeave)

  onUnmounted(() => {
    window.removeEventListener('pointermove', handlePointer)
    window.removeEventListener('pointerleave', handlePointerLeave)
  })
})
</script>

<style scoped>
.login-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: stretch;
  width: 100%;
  height: 100%;
  min-height: 100%;
  overflow: hidden;
  font-family: "Inter", "Segoe UI", Helvetica, Arial, sans-serif;
  background: radial-gradient(1200px 650px at 10% -10%, rgba(122, 92, 255, 0.16), transparent 58%),
    radial-gradient(1000px 620px at 95% 110%, rgba(0, 204, 255, 0.14), transparent 58%),
    linear-gradient(145deg, #eff3ff 0%, #f8f9ff 45%, #eef4ff 100%);
  transition: background-color 0.35s ease, background 0.35s ease;
}

.login-container.dark-mode {
  background: radial-gradient(1200px 700px at 5% -5%, rgba(126, 87, 194, 0.2), transparent 60%),
    radial-gradient(1000px 620px at 95% 110%, rgba(0, 183, 255, 0.16), transparent 58%),
    linear-gradient(145deg, #0e111a 0%, #111827 44%, #0b1324 100%);
}

.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  z-index: 0;
  pointer-events: none;
}

.login-container::before {
  width: 680px;
  height: 680px;
  border-radius: 50%;
  top: -320px;
  left: -180px;
  background: radial-gradient(circle, rgba(110, 89, 255, 0.22) 0%, rgba(110, 89, 255, 0) 66%);
}

.login-container::after {
  width: 640px;
  height: 640px;
  border-radius: 50%;
  right: -220px;
  bottom: -300px;
  background: radial-gradient(circle, rgba(0, 195, 255, 0.2) 0%, rgba(0, 195, 255, 0) 65%);
}

.brand-side {
  position: relative;
  z-index: 1;
  flex: 1;
  min-height: 100%;
  background: linear-gradient(135deg, #182748 0%, #233d7a 38%, #335db0 65%, #3897c6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px;
  overflow: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.15);
}

.brand-side::before {
  content: '';
  position: absolute;
  inset: -20% -10%;
  background:
    linear-gradient(120deg, transparent 40%, rgba(255, 255, 255, 0.08) 50%, transparent 60%),
    radial-gradient(circle at 20% 30%, rgba(255, 255, 255, 0.14), transparent 32%),
    radial-gradient(circle at 80% 70%, rgba(255, 255, 255, 0.12), transparent 34%);
  background-size: 240% 240%, 100% 100%, 100% 100%;
  animation: shimmerFlow 12s ease-in-out infinite;
  z-index: 0;
}

.brand-side::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    repeating-linear-gradient(0deg, rgba(255, 255, 255, 0.04) 0, rgba(255, 255, 255, 0.04) 1px, transparent 1px, transparent 36px),
    repeating-linear-gradient(90deg, rgba(255, 255, 255, 0.035) 0, rgba(255, 255, 255, 0.035) 1px, transparent 1px, transparent 36px);
  opacity: 0.38;
  z-index: 0;
}

.brand-content {
  text-align: left;
  color: white;
  z-index: 1;
  max-width: 460px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
  min-height: 100%;
  padding-top: 150px;
}

.brand-title {
  font-size: clamp(52px, 5.4vw, 80px);
  font-weight: 800;
  margin: 0 0 14px 0;
  letter-spacing: -1.4px;
  line-height: 0.98;
  text-shadow: 0 6px 18px rgba(0, 0, 0, 0.22);
  animation: floatIn 900ms ease both;
}

.brand-description {
  font-size: clamp(18px, 1.45vw, 22px);
  opacity: 0.96;
  line-height: 1.55;
  margin-top: 8px;
  max-width: 420px;
}

@keyframes floatIn {
  from { transform: translateY(18px); opacity: 0 }
  to { transform: translateY(0); opacity: 1 }
}

@keyframes shimmerFlow {
  0%, 100% {
    background-position: 0% 50%, 0% 0%, 0% 0%;
  }
  50% {
    background-position: 100% 50%, 0% 0%, 0% 0%;
  }
}

.form-side {
  position: relative;
  z-index: 2;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 56px;
  background: transparent;
  overflow-y: auto;
  max-height: 100vh;
}

.login-container.dark-mode .brand-side {
  background: linear-gradient(140deg, #0d1328 0%, #1a2c5f 40%, #203d75 68%, #20557f 100%);
}

.theme-switcher-left {
  position: absolute;
  top: 22px;
  left: 22px;
  z-index: 5;
}

.theme-switcher-left :deep(.el-switch) {
  --el-switch-on-color: #6d68ff;
  --el-switch-off-color: rgba(184, 191, 214, 0.85);
  height: 34px;
}

.theme-switcher-left :deep(.el-switch__core) {
  height: 32px !important;
  min-width: 64px !important;
  border-radius: 16px !important;
}

.theme-switcher-left :deep(.el-switch__action) {
  width: 24px !important;
  height: 24px !important;
}

.theme-switcher-left :deep(.el-switch__inner) {
  font-size: 14px !important;
}

.theme-switcher-left :deep(.el-icon) {
  font-size: 15px !important;
}

.locale-switcher-right {
  position: absolute;
  top: 22px;
  right: 22px;
  z-index: 5;
}

.locale-switcher-right :deep(.el-button) {
  background: rgba(255, 255, 255, 0.9);
  color: #24324d;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(30, 36, 68, 0.14);
  font-weight: 600;
}

.login-container.dark-mode .locale-switcher-right :deep(.el-button) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #e7ebff !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
  box-shadow: 0 8px 18px rgba(0, 0, 0, 0.25);
}

.login-card {
  width: 100%;
  max-width: 540px;
  border-radius: 24px;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.84) 0%, rgba(246, 248, 255, 0.66) 100%) !important;
  backdrop-filter: blur(18px) saturate(140%);
  -webkit-backdrop-filter: blur(18px) saturate(140%);
  border: 1px solid rgba(255, 255, 255, 0.75);
  box-shadow: 0 24px 54px rgba(16, 24, 40, 0.24), inset 0 1px 0 rgba(255, 255, 255, 0.88) !important;
  transition: transform 220ms ease, box-shadow 220ms ease;
  overflow: hidden;
  max-height: 90vh;
  overflow-y: auto;
}

.tech-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  overflow: hidden;
}

.tech-grid {
  width: 100%;
  height: 100%;
  opacity: 0.64;
  transform: scale(1.05) translateY(-6%);
  animation: gridFloat 12s linear infinite;
}

@keyframes gridFloat {
  from { transform: scale(1.02) translateY(-4%) rotate(0deg); }
  to { transform: scale(1.06) translateY(-6%) rotate(3deg); }
}

.login-card {
  position: relative;
  z-index: 2;
}

.login-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at 6% 8%, rgba(255, 255, 255, 0.55), transparent 28%),
    radial-gradient(circle at 92% 92%, rgba(94, 114, 228, 0.16), transparent 35%);
  pointer-events: none;
}

.login-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 30px 64px rgba(16, 24, 40, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.9) !important;
}

.login-container.dark-mode .login-card {
  background: linear-gradient(160deg, rgba(21, 30, 52, 0.82) 0%, rgba(17, 24, 39, 0.74) 100%) !important;
  border: 1px solid rgba(255, 255, 255, 0.12);
  box-shadow: 0 28px 62px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.08) !important;
}

.login-card :deep(.el-card__body) {
  padding: 58px 46px 46px;
  position: relative;
  background: transparent;
  transition: background-color 0.3s ease;
  overflow: visible;
}

.mode-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  border-bottom: 2px solid rgba(0, 0, 0, 0.1);
  justify-content: center;
}

.tab-button {
  padding: 12px 24px;
  border: none;
  background: transparent;
  color: #5b6785;
  font-weight: 600;
  cursor: pointer;
  position: relative;
  transition: color 0.3s ease;
  font-size: 16px;
}

.tab-button.active {
  color: #1a2238;
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #6d68ff, #00ccff);
}

.login-container.dark-mode .tab-button {
  color: #8a96b8;
}

.login-container.dark-mode .tab-button.active {
  color: #edf2ff;
}

.form-wrapper {
  animation: slideIn 0.3s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 34px;
}

.login-title {
  font-size: clamp(32px, 3.5vw, 48px);
  font-weight: 700;
  margin: 0 0 10px 0;
  color: #1a2238;
  letter-spacing: -0.8px;
  text-shadow: 0 8px 22px rgba(30, 47, 84, 0.16);
  transition: color 0.3s ease;
}

.login-container.dark-mode .login-title {
  color: #edf2ff;
  text-shadow: 0 10px 26px rgba(0, 0, 0, 0.4);
}

.login-subtitle {
  font-size: 16px;
  color: #5b6785;
  margin: 0;
  font-weight: 500;
  letter-spacing: 0.3px;
  transition: color 0.3s ease;
}

.login-container.dark-mode .login-subtitle {
  color: #b8c2dd;
}

.login-container :deep(.el-form) {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.login-container :deep(.el-form-item__label) {
  margin-bottom: 10px;
  color: #2f3b5a;
  font-weight: 600;
  font-size: 16px;
  letter-spacing: 0.2px;
  transition: color 0.3s ease;
}

.login-container.dark-mode :deep(.el-form-item__label) {
  color: #dce4ff !important;
}

.login-container :deep(.el-input__wrapper) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(171, 183, 214, 0.45), 0 8px 18px rgba(42, 58, 103, 0.08) !important;
  padding: 0 14px;
  min-height: 50px;
  transition: box-shadow 0.28s ease, transform 0.28s ease;
}

.login-container :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(105, 103, 255, 0.38), 0 12px 24px rgba(59, 71, 127, 0.18) !important;
  transform: translateY(-1px);
}

.login-container :deep(.el-input__inner) {
  color: #1c2740;
  font-size: 15px;
  font-weight: 500;
}

.login-container.dark-mode :deep(.el-input__wrapper) {
  background-color: rgba(43, 55, 84, 0.72) !important;
  box-shadow: 0 0 0 1px rgba(130, 145, 189, 0.28), 0 10px 20px rgba(0, 0, 0, 0.28) !important;
}

.login-container.dark-mode :deep(.el-input__inner) {
  color: #ecf2ff !important;
}

.login-container.dark-mode :deep(.el-input__inner::placeholder) {
  color: #9eabcf !important;
}

.log-in-btn {
  width: 100% !important;
  height: 52px !important;
  border-radius: 14px !important;
  font-size: 16px !important;
  font-weight: 700 !important;
  letter-spacing: 0.35px;
  background: linear-gradient(95deg, #5558ff 0%, #4a78ff 40%, #4cb8ff 100%) !important;
  color: #fff !important;
  border: none !important;
  box-shadow: 0 14px 28px rgba(73, 102, 235, 0.35), inset 0 1px 0 rgba(255, 255, 255, 0.35);
  transition: transform 0.25s ease, box-shadow 0.25s ease, filter 0.25s ease;
}

.log-in-btn:hover {
  filter: brightness(1.04);
  transform: translateY(-2px);
  box-shadow: 0 18px 32px rgba(73, 102, 235, 0.42), inset 0 1px 0 rgba(255, 255, 255, 0.45);
}

.log-in-btn:active {
  transform: translateY(0);
}

.login-container :deep(.el-checkbox__label) {
  color: #4a587f;
  font-size: 14px;
  font-weight: 500;
}

.login-container.dark-mode :deep(.el-checkbox__label) {
  color: #c5d1f2;
}

.login-container.dark-mode :deep(.el-alert--error) {
  background: rgba(255, 107, 107, 0.14);
  border: 1px solid rgba(255, 122, 122, 0.3);
}

.login-container.dark-mode :deep(.el-alert__title) {
  color: #ffd3d3;
}

.login-container :deep(.el-select__wrapper) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(171, 183, 214, 0.45), 0 8px 18px rgba(42, 58, 103, 0.08) !important;
  transition: box-shadow 0.28s ease, transform 0.28s ease;
}

.login-container.dark-mode :deep(.el-select__wrapper) {
  background-color: rgba(43, 55, 84, 0.72) !important;
  box-shadow: 0 0 0 1px rgba(130, 145, 189, 0.28), 0 10px 20px rgba(0, 0, 0, 0.28) !important;
}

.country-code-select {
  width: 190px;
}

.login-container :deep(.el-input-group__prepend) {
  background: linear-gradient(180deg, rgba(251, 253, 255, 0.98) 0%, rgba(242, 247, 255, 0.95) 100%) !important;
  border: 1px solid rgba(171, 183, 214, 0.52) !important;
  border-right: 1px solid rgba(171, 183, 214, 0.45) !important;
  border-radius: 12px 0 0 12px !important;
  padding: 0 10px !important;
  transition: border-color 0.25s ease, background 0.25s ease;
}

.login-container :deep(.country-code-select .el-select__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  min-height: 44px;
  padding-left: 2px !important;
  padding-right: 2px !important;
}

.login-container.dark-mode :deep(.el-input-group__prepend) {
  background: linear-gradient(180deg, rgba(47, 61, 94, 0.94) 0%, rgba(33, 45, 75, 0.92) 100%) !important;
  border-color: rgba(130, 145, 189, 0.28) !important;
}

.login-container.dark-mode :deep(.country-code-select .el-select__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
}

.login-container :deep(.country-code-select .el-select__selected-item),
.login-container :deep(.country-code-select .el-select__placeholder) {
  font-weight: 600;
  color: #3a4a72 !important;
}

.login-container.dark-mode :deep(.country-code-select .el-select__selected-item),
.login-container.dark-mode :deep(.country-code-select .el-select__placeholder) {
  color: #dde7ff !important;
}

.login-container :deep(.country-code-select .el-select__caret) {
  color: #6d7da7 !important;
  font-size: 13px;
}

.login-container.dark-mode :deep(.country-code-select .el-select__caret) {
  color: #9eb0dc !important;
}

.login-container :deep(.el-input-group__prepend:hover) {
  border-color: rgba(86, 122, 229, 0.48) !important;
}

.login-container :deep(.el-input-group:focus-within .el-input-group__prepend) {
  border-color: rgba(86, 122, 229, 0.7) !important;
  background: linear-gradient(180deg, rgba(252, 254, 255, 0.99) 0%, rgba(241, 247, 255, 0.98) 100%) !important;
}

.login-container.dark-mode :deep(.el-input-group:focus-within .el-input-group__prepend) {
  border-color: rgba(126, 164, 255, 0.55) !important;
  background: linear-gradient(180deg, rgba(55, 72, 109, 0.98) 0%, rgba(39, 52, 84, 0.95) 100%) !important;
}

.login-container :deep(.country-meta-option) {
  color: #6f7b99 !important;
  font-size: 12px;
  font-weight: 600;
}

.login-container :deep(.country-divider-option) {
  color: #9aa5c3 !important;
  letter-spacing: 1px;
  text-align: center;
}

.login-container.dark-mode :deep(.country-meta-option) {
  color: #9fb0d8 !important;
}

.login-container.dark-mode :deep(.country-divider-option) {
  color: #7f8db0 !important;
}

.login-container :deep(.country-group-nearest .el-option) {
  padding-left: 20px;
}

.login-container :deep(.country-group .el-option) {
  padding-left: 20px;
}

.login-container :deep(.el-optgroup__title) {
  color: #2f3b5a;
  font-weight: 700;
  font-size: 13px;
  letter-spacing: 0.5px;
}

.login-container.dark-mode :deep(.el-optgroup__title) {
  color: #dce4ff;
}

.login-container :deep(.el-input-number__wrapper) {
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.8) !important;
  box-shadow: 0 0 0 1px rgba(171, 183, 214, 0.45), 0 8px 18px rgba(42, 58, 103, 0.08) !important;
  transition: box-shadow 0.28s ease, transform 0.28s ease;
}

.login-container.dark-mode :deep(.el-input-number__wrapper) {
  background-color: rgba(43, 55, 84, 0.72) !important;
  box-shadow: 0 0 0 1px rgba(130, 145, 189, 0.28), 0 10px 20px rgba(0, 0, 0, 0.28) !important;
}

@media (max-width: 1200px) {
  .brand-side {
    padding: 42px;
  }

  .form-side {
    padding: 42px;
  }

  .login-card :deep(.el-card__body) {
    padding: 56px 40px 42px;
  }
}

@media (max-width: 1024px) {
  .login-container {
    flex-direction: column;
  }

  .brand-side,
  .form-side {
    width: 100%;
    flex: none;
  }

  .brand-side {
    min-height: 34vh;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  }

  .brand-content {
    max-width: 560px;
    text-align: center;
  }

  .brand-description {
    margin-left: auto;
    margin-right: auto;
  }

  .form-side {
    min-height: 66vh;
  }
}

@media (max-width: 640px) {
  .brand-side {
    padding: 28px 20px;
    min-height: 30vh;
  }

  .form-side {
    padding: 18px;
    min-height: 70vh;
  }

  .theme-switcher-left {
    top: 14px;
    left: 14px;
  }

  .locale-switcher-right {
    top: 14px;
    right: 14px;
  }

  .login-card {
    border-radius: 18px;
  }

  .login-card :deep(.el-card__body) {
    padding: 52px 22px 28px;
  }
}
</style>
