<template>
    <div class="entry-page">
        <div
            v-if="!showLogin"
            class="cover-screen"
            :class="{ 'cover-fade-out': isFadingOut }"
        >
            <div class="cover-overlay">
                <div class="cover-decor decor-left"></div>
                <div class="cover-decor decor-right"></div>

                <div class="cover-content">
                    <div class="cover-card">
                        <div class="brand-mark">🔗</div>
                        <h1>{{ t('appName') }}</h1>

                        <button
                            class="cover-enter-button"
                            type="button"
                            @click="openLogin"
                            :aria-label="t('openLoginPage')"
                            :disabled="isFadingOut"
                        >
                            <span class="cover-enter-button-text">{{ t('openLoginPage') }}</span>
                            <span class="cover-enter-arrow">→</span>
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <div v-else class="login-wrapper">
            <loginBox />
        </div>
    </div>
</template>

<style scoped>
.entry-page {
    min-height: 100vh;
    background: #0f172a;
}

.cover-screen {
    width: 100%;
    min-height: 100vh;
    border: none;
    padding: 0;
    cursor: default;
    background-image: url('/login-cover.svg');
    background-size: cover;
    background-position: center;
    background-repeat: no-repeat;
    opacity: 1;
    transition: opacity 0.8s ease, transform 0.8s ease;
    position: relative;
}

.cover-screen.cover-fade-out {
    opacity: 0;
    transform: scale(1.02);
}

.cover-overlay {
    position: relative;
    min-height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background:
        radial-gradient(circle at 18% 20%, rgba(255, 255, 255, 0.18), transparent 38%),
        radial-gradient(circle at 82% 78%, rgba(96, 165, 250, 0.2), transparent 42%),
        linear-gradient(145deg, rgba(15, 23, 42, 0.42), rgba(15, 23, 42, 0.62));
    overflow: hidden;
}

.cover-decor {
    position: absolute;
    border-radius: 999px;
    pointer-events: none;
    filter: blur(2px);
}

.decor-left {
    width: 260px;
    height: 260px;
    left: 8%;
    top: 18%;
    background: radial-gradient(circle at 35% 35%, rgba(167, 139, 250, 0.55), rgba(167, 139, 250, 0));
}

.decor-right {
    width: 320px;
    height: 320px;
    right: 8%;
    bottom: 14%;
    background: radial-gradient(circle at 40% 35%, rgba(56, 189, 248, 0.5), rgba(56, 189, 248, 0));
}

.cover-content {
    width: min(92%, 620px);
    position: relative;
    z-index: 1;
}

.cover-card {
    padding: 46px 40px;
    border-radius: 24px;
    text-align: center;
    color: #fff;
    border: 1px solid rgba(255, 255, 255, 0.28);
    background:
        linear-gradient(150deg, rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0.08));
    box-shadow:
        0 28px 56px rgba(2, 6, 23, 0.45),
        inset 0 1px 0 rgba(255, 255, 255, 0.45);
    backdrop-filter: blur(10px);
}

.brand-mark {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    margin: 0 auto 16px;
    display: grid;
    place-items: center;
    font-size: 32px;
    background: linear-gradient(140deg, rgba(255, 255, 255, 0.28), rgba(255, 255, 255, 0.08));
    border: 1px solid rgba(255, 255, 255, 0.35);
}

.cover-content h1 {
    margin: 0 0 22px;
    font-size: clamp(34px, 5vw, 56px);
    font-weight: 800;
    letter-spacing: 0.03em;
    text-shadow: 0 10px 24px rgba(15, 23, 42, 0.45);
}

.cover-enter-button {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 12px 22px;
    border-radius: 999px;
    border: 1px solid rgba(255, 255, 255, 0.42);
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.3), rgba(255, 255, 255, 0.16));
    color: #fff;
    font-size: 17px;
    font-weight: 700;
    cursor: pointer;
    transition: transform 0.22s ease, background 0.22s ease, box-shadow 0.22s ease;
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.35);
}

.cover-enter-button:hover {
    background: linear-gradient(145deg, rgba(255, 255, 255, 0.38), rgba(255, 255, 255, 0.2));
    transform: translateY(-2px);
    box-shadow: 0 14px 28px rgba(15, 23, 42, 0.42);
}

.cover-enter-button:active {
    transform: translateY(0);
}

.cover-enter-button:disabled {
    opacity: 0.7;
    cursor: default;
}

.cover-enter-button-text {
    white-space: nowrap;
}

.cover-enter-arrow {
    display: inline-block;
    font-size: 18px;
}

.login-wrapper {
    height: 100vh;
    min-height: 100vh;
}

@media (max-width: 768px) {
    .cover-card {
        padding: 34px 22px;
    }

    .brand-mark {
        width: 56px;
        height: 56px;
        font-size: 28px;
    }
}
</style>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import loginBox from '../components/loginBox.vue'

const showLogin = ref(false)
const isFadingOut = ref(false)
const { t } = useI18n()

const openLogin = () => {
    if (isFadingOut.value || showLogin.value) {
        return
    }

    isFadingOut.value = true
    window.setTimeout(() => {
        showLogin.value = true
        isFadingOut.value = false
    }, 800)
}
</script>