<template>
  <NuxtLayout name="default">
    <div class="shops-container">
      <div class="shops-toolbar">
        <h2>{{ $t('nearbyShops') }}</h2>
        <el-space>
          <el-select
            v-model="selectedCategory"
            :placeholder="$t('allCategories')"
            clearable
            style="width: 200px;"
          >
            <el-option :label="$t('all')" value="" />
            <el-option :label="$t('restaurant')" value="Restaurant" />
            <el-option :label="$t('convenienceStore')" value="Convenience Store" />
            <el-option :label="$t('coffeeShop')" value="Coffee Shop" />
            <el-option :label="$t('supermarket')" value="Supermarket" />
            <el-option :label="$t('pharmacy')" value="Pharmacy" />
            <el-option :label="$t('bakery')" value="Bakery" />
          </el-select>
          <el-input
            v-model="searchQuery"
            :placeholder="$t('searchShops')"
            clearable
            style="width: 250px;"
          />
          <el-button @click="centerOnUser" :icon="Location" v-if="userLocation">
            {{ $t('myLocation') }}
          </el-button>
        </el-space>
      </div>

      <div class="map-section">
        <div class="map-container" ref="mapContainer" id="map"></div>
        <div v-if="loadingLocation" class="map-loading">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>{{ $t('gettingLocation') }}</p>
        </div>
        <div v-if="loadingShops" class="map-loading">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>{{ $t('loadingShops') }}</p>
        </div>
      </div>

      <!-- Shop Details Dialog -->
      <el-dialog
        v-model="dialogVisible"
        :title="selectedShop?.name"
        width="600px"
      >
        <div v-if="selectedShop" class="shop-details">
          <div class="shop-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item :label="$t('category')">
                {{ getCategoryTranslation(selectedShop.category) }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('distance')">
                {{ selectedShop.distance }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('address')" :span="2">
                {{ selectedShop.address }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('hours')" :span="2">
                {{ selectedShop.hours }}
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedShop.phone" :label="$t('phone')" :span="2">
                {{ selectedShop.phone }}
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedShop.website" :label="$t('website')" :span="2">
                <a :href="selectedShop.website" target="_blank">{{ selectedShop.website }}</a>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="shop-announcements">
            <h3>{{ $t('announcementsAndSales') }}</h3>
            <el-empty 
              v-if="selectedShop.announcements.length === 0"
              :description="$t('noAnnouncements')"
            />
            <div v-else class="announcements-list">
              <el-card 
                v-for="announcement in selectedShop.announcements" 
                :key="announcement.id"
                class="announcement-card"
              >
                <template #header>
                  <div class="announcement-header">
                    <el-tag :type="announcement.type === 'sale' ? 'danger' : 'info'" size="small">
                      {{ announcement.type === 'sale' ? $t('sale') : $t('notice') }}
                    </el-tag>
                    <span class="announcement-date">{{ announcement.date }}</span>
                  </div>
                </template>
                <h4>{{ announcement.title }}</h4>
                <p>{{ announcement.content }}</p>
              </el-card>
            </div>
          </div>
        </div>
      </el-dialog>
    </div>
  </NuxtLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Location, Shop, Loading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t, locale } = useI18n()
const language = computed(() => locale.value as 'en' | 'zh')
const searchQuery = ref('')
const selectedCategory = ref('')
const dialogVisible = ref(false)
const selectedShop = ref<any>(null)
const mapContainer = ref<HTMLElement | null>(null)
const loadingLocation = ref(true)
const loadingShops = ref(false)
const userLocation = ref<{ lat: number; lng: number } | null>(null)
let map: L.Map | null = null
const shopMarkers: L.Marker[] = []

const shops = ref<any[]>([])

// Mapping of categories to OSM amenity tags
const categoryToOSM: Record<string, string[]> = {
  'Restaurant': ['fast_food', 'restaurant'],
  'Convenience Store': ['convenience'],
  'Coffee Shop': ['cafe'],
  'Supermarket': ['supermarket'],
  'Pharmacy': ['pharmacy'],
  'Bakery': ['bakery']
}

const fetchNearbyShops = async (lat: number, lng: number) => {
  loadingShops.value = true
  shops.value = []
  
  try {
    // Build Overpass query for multiple amenity types
    const radius = 2000 // 2km radius
    const amenities = ['fast_food', 'restaurant', 'convenience', 'cafe', 'supermarket', 'pharmacy', 'bakery']
    const amenityQuery = amenities.map(a => `node["amenity"="${a}"](around:${radius},${lat},${lng});`).join('\n        ')
    
    const query = `[out:json][timeout:25];
(
  ${amenityQuery}
);
out body;
>;
out skel qt;`
    
    const response = await fetch('https://overpass-api.de/api/interpreter', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: 'data=' + encodeURIComponent(query)
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()
    
    if (!data.elements || data.elements.length === 0) {
      ElMessage.info(t('noShopsFound'))
      loadFallbackShops(lat, lng)
      return
    }
    
    // Process results and categorize
    const processedShops = data.elements
      .filter((element: any) => element.tags && element.tags.name)
      .map((element: any) => {
        const amenity = element.tags.amenity
        let category = 'Other'
        
        if (amenity === 'fast_food' || amenity === 'restaurant') category = 'Restaurant'
        else if (amenity === 'convenience') category = 'Convenience Store'
        else if (amenity === 'cafe') category = 'Coffee Shop'
        else if (amenity === 'supermarket') category = 'Supermarket'
        else if (amenity === 'pharmacy') category = 'Pharmacy'
        else if (amenity === 'bakery') category = 'Bakery'
        
        // Calculate distance
        const distance = calculateDistance(lat, lng, element.lat, element.lon)
        
        return {
          id: element.id,
          name: element.tags.name,
          category: category,
          address: element.tags['addr:street'] 
            ? `${element.tags['addr:housenumber'] || ''} ${element.tags['addr:street']}`.trim()
            : element.tags['addr:city'] || t('addressNotAvailable'),
          distance: `${distance.toFixed(1)} km`,
          hours: element.tags.opening_hours || t('hoursNotAvailable'),
          lat: element.lat,
          lng: element.lon,
          brand: element.tags.brand || null,
          phone: element.tags.phone || null,
          website: element.tags.website || null,
          announcements: generateMockAnnouncements(element.tags.name, element.tags.brand)
        }
      })
      .slice(0, 200) // Limit to 200 shops
    
    shops.value = processedShops
    
    if (processedShops.length === 0) {
      ElMessage.info(t('noShopsFound'))
      loadFallbackShops(lat, lng)
    }
  } catch (error) {
    console.error('Error fetching shops:', error)
    ElMessage.error(t('failedToLoadShops'))
    loadFallbackShops(lat, lng)
  } finally {
    loadingShops.value = false
  }
}

const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number): number => {
  const R = 6371 // Radius of Earth in km
  const dLat = (lat2 - lat1) * Math.PI / 180
  const dLon = (lon2 - lon1) * Math.PI / 180
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  return R * c
}

const generateMockAnnouncements = (name: string, brand: string | null): any[] => {
  // Generate realistic announcements based on shop name/brand
  const announcements = []
  const randomChance = Math.random()
  
  if (randomChance > 0.5) {
    announcements.push({
      id: Math.random(),
      type: 'sale',
      title: t('specialOffer', { shopName: name }),
      content: t('shopMockLatestDealsContent'),
      date: '2026-01-28'
    })
  }
  
  if (randomChance > 0.7) {
    announcements.push({
      id: Math.random(),
      type: 'notice',
      title: t('updatedOpeningHours', { shopName: name }),
      content: t('shopMockExtendedHoursContent'),
      date: '2026-01-27'
    })
  }
  
  return announcements
}

const loadFallbackShops = (lat: number, lng: number) => {
  // Fallback mock data if API fails
  shops.value = [
    {
      id: 1,
      name: "McDonald's",
      category: 'Restaurant',
      address: t('communityMockNearbyLocation'),
      distance: '0.3 km',
      hours: '6:00 AM - 11:00 PM',
      lat: lat + 0.003,
      lng: lng + 0.004,
      phone: '1-800-244-6227',
      website: 'https://www.mcdonalds.com',
      announcements: [
        {
          id: 1,
          type: 'sale',
          title: t('shopMockBigMacDealTitle'),
          content: t('shopMockBigMacDealContent'),
          date: '2026-01-28'
        }
      ]
    },
    {
      id: 2,
      name: 'Starbucks',
      category: 'Coffee Shop',
      address: t('communityMockNearbyLocation'),
      distance: '0.5 km',
      hours: '6:00 AM - 9:00 PM',
      lat: lat - 0.005,
      lng: lng + 0.003,
      phone: '1-800-782-7282',
      website: 'https://www.starbucks.com',
      announcements: []
    }
  ]
}

const filteredShops = computed(() => {
  return shops.value.filter(shop => {
    const matchesSearch = shop.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         shop.category.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesCategory = !selectedCategory.value || shop.category === selectedCategory.value
    return matchesSearch && matchesCategory
  })
})

const getCategoryTranslation = (category: string) => {
  const categoryMap: Record<string, string> = {
    'Restaurant': t('restaurant'),
    'Convenience Store': t('convenienceStore'),
    'Coffee Shop': t('coffeeShop'),
    'Supermarket': t('supermarket'),
    'Pharmacy': t('pharmacy'),
    'Bakery': t('bakery')
  }
  return categoryMap[category] || category
}

const selectShop = (shop: any) => {
  selectedShop.value = shop
  dialogVisible.value = true
}

const initMap = (lat: number, lng: number) => {
  if (!mapContainer.value || map) return

  // Initialize the map
  map = L.map('map').setView([lat, lng], 16)

  // Add OpenStreetMap tile layer
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
  }).addTo(map)

  // Add user location marker
  const userIcon = L.divIcon({
    className: 'user-marker',
    html: '<div class="user-marker-icon">📍</div>',
    iconSize: [40, 40],
    iconAnchor: [20, 20]
  })
  L.marker([lat, lng], { icon: userIcon }).addTo(map)
    .bindPopup(t('youAreHere'))

  // Fetch real nearby shops
  fetchNearbyShops(lat, lng)
}

const addShopMarkers = () => {
  if (!map) return

  // Clear existing markers
  shopMarkers.forEach(marker => marker.remove())
  shopMarkers.length = 0

  // Add markers for filtered shops
  filteredShops.value.forEach(shop => {
    const hasAnnouncements = shop.announcements.length > 0
    
    const shopIcon = L.divIcon({
      className: 'shop-marker-leaflet',
      html: `
        <div class="shop-marker-icon ${hasAnnouncements ? 'has-announcements' : ''}">
          <span class="shop-initial">${shop.name.charAt(0)}</span>
          ${hasAnnouncements ? `<span class="announcement-badge">${shop.announcements.length}</span>` : ''}
        </div>
        <div class="shop-marker-label">${shop.name}</div>
      `,
      iconSize: [60, 60],
      iconAnchor: [30, 30]
    })

    const marker = L.marker([shop.lat, shop.lng], { icon: shopIcon })
      .addTo(map!)
      .on('click', () => selectShop(shop))

    shopMarkers.push(marker)
  })
}

// Watch for changes and update markers
watch(filteredShops, () => {
  if (map) {
    addShopMarkers()
  }
})

// Watch for shops data changes and add markers
watch(shops, () => {
  if (map && shops.value.length > 0) {
    addShopMarkers()
  }
})

// Watch for language changes and refetch data
watch(language, () => {
  if (userLocation.value) {
    fetchNearbyShops(userLocation.value.lat, userLocation.value.lng)
  }
})

const centerOnUser = () => {
  if (map && userLocation.value) {
    map.setView([userLocation.value.lat, userLocation.value.lng], 16)
  }
}

const getUserLocation = () => {
  loadingLocation.value = true
  
  if ('geolocation' in navigator) {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const lat = position.coords.latitude
        const lng = position.coords.longitude
        userLocation.value = { lat, lng }
        loadingLocation.value = false
        initMap(lat, lng)
      },
      (error) => {
        console.error('Error getting location:', error)
        loadingLocation.value = false
        // Fallback to a default location (e.g., San Francisco)
        const defaultLat = 37.7749
        const defaultLng = -122.4194
        userLocation.value = { lat: defaultLat, lng: defaultLng }
        initMap(defaultLat, defaultLng)
        ElMessage.warning(t('couldNotGetLocation'))
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 0
      }
    )
  } else {
    loadingLocation.value = false
    // Fallback location
    const defaultLat = 37.7749
    const defaultLng = -122.4194
    userLocation.value = { lat: defaultLat, lng: defaultLng }
    initMap(defaultLat, defaultLng)
    ElMessage.error(t('geolocationNotSupported'))
  }
}

onMounted(() => {
  getUserLocation()
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
.shops-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.shops-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.shops-toolbar h2 {
  margin: 0;
  color: #333;
  font-size: 32px;
}

.map-section {
  width: 100%;
  height: 600px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  position: relative;
}

.map-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.map-loading {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.95);
  z-index: 1000;
}

.map-loading p {
  margin-top: 16px;
  color: #666;
  font-size: 20px;
}

.shop-details {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.shop-info {
  width: 100%;
}

.shop-announcements h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 24px;
}

.announcements-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-card {
  border: 1px solid #f0f0f0;
  box-shadow: none;
}

.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.announcement-date {
  font-size: 16px;
  color: #999;
}

.announcement-card h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
}

.announcement-card p {
  margin: 0;
  color: #666;
  font-size: 18px;
  line-height: 1.6;
}

:deep(.el-dialog__header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 20px;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-badge__content) {
  background-color: #f56c6c;
  border: 2px solid white;
}

/* Dark mode styles for el-descriptions */
html.dark :deep(.el-descriptions) {
  --el-descriptions-item-bordered-label-background: #262727 !important;
  --el-descriptions-item-bordered-content-background: #1a1a1a !important;
  --el-border-color: #404040 !important;
  --el-fill-color-lighter: #262727 !important;
  --el-fill-color-blank: #1a1a1a !important;
  --el-bg-color: #1a1a1a !important;
}

html.dark :deep(.el-descriptions__label),
html.dark :deep(.el-descriptions__label.el-descriptions__cell.is-bordered-label) {
  color: #e5e7eb !important;
  background-color: #262727 !important;
}

html.dark :deep(.el-descriptions__content),
html.dark :deep(.el-descriptions__content.el-descriptions__cell.is-bordered-content) {
  color: #d1d5db !important;
  background-color: #1a1a1a !important;
}

html.dark :deep(.el-descriptions__cell) {
  border-color: #404040 !important;
}

html.dark :deep(.el-descriptions .el-descriptions__body) {
  background-color: transparent !important;
}

html.dark :deep(.el-descriptions tr) {
  background-color: transparent !important;
}

html.dark :deep(.el-descriptions__table) {
  background-color: transparent !important;
}

/* Dark mode for empty state */
html.dark :deep(.el-empty__image svg) {
  fill: #4b5563 !important;
}

html.dark :deep(.el-empty__image svg path) {
  fill: #4b5563 !important;
}

html.dark :deep(.el-empty__description) {
  color: #9ca3af !important;
}
</style>

<style>
/* Leaflet custom marker styles - needs to be global */
.user-marker {
  background: transparent !important;
  border: none !important;
}

.user-marker-icon {
  font-size: 32px;
  text-align: center;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.shop-marker-leaflet {
  background: transparent !important;
  border: none !important;
}

.shop-marker-icon {
  position: relative;
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid #ddd;
}

.shop-initial {
  font-size: 26px;
  font-weight: bold;
  color: #07b981;
}

.shop-marker-icon:hover {
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border-color: #07b981;
}

.shop-marker-icon.has-announcements {
  border: 3px solid #07b981;
  background: #f0fdf4;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(7, 185, 129, 0.3); }
  50% { box-shadow: 0 2px 16px rgba(7, 185, 129, 0.6); }
}

.announcement-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #f56c6c;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: bold;
  border: 2px solid white;
}

.shop-marker-label {
  position: absolute;
  top: 52px;
  left: 50%;
  transform: translateX(-50%);
  padding: 4px 8px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
  color: #333;
  pointer-events: none;
}

/* Override Leaflet default styles */
.leaflet-popup-content-wrapper {
  border-radius: 8px;
}

.leaflet-popup-content {
  margin: 12px;
  font-size: 18px;
}
</style>
