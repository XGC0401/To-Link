<template>
  <NuxtLayout name="default">
    <div class="community-container">
      <div class="community-toolbar">
        <h2>{{ $t('nearbyCommunity') }}</h2>
        <el-space>
          <el-select
            v-model="selectedCategory"
            :placeholder="$t('allCategories')"
            clearable
            style="width: 220px;"
          >
            <el-option :label="$t('all')" value="" />
            <el-option :label="$t('ngo')" value="NGO" />
            <el-option :label="$t('charityOrg')" value="Charity Organization" />
            <el-option :label="$t('communityCenter')" value="Community Center" />
            <el-option :label="$t('socialClub')" value="Social Club" />
            <el-option :label="$t('religiousOrg')" value="Religious Organization" />
            <el-option :label="$t('youthCenter')" value="Youth Center" />
          </el-select>
          <el-input
            v-model="searchQuery"
            :placeholder="$t('searchCommunities')"
            clearable
            style="width: 250px;"
          />
          <el-button @click="centerOnUser" :icon="Location" v-if="userLocation">
            {{ $t('myLocation') }}
          </el-button>
        </el-space>
      </div>

      <div class="map-section">
        <div class="map-container" ref="mapContainer" id="community-map"></div>
        <div v-if="loadingLocation" class="map-loading">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>{{ $t('gettingLocation') }}</p>
        </div>
        <div v-if="loadingCommunities" class="map-loading">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>{{ $t('loadingCommunities') }}</p>
        </div>
      </div>

      <!-- Community Details Dialog -->
      <el-dialog
        v-model="dialogVisible"
        :title="selectedCommunity?.name"
        width="600px"
      >
        <div v-if="selectedCommunity" class="community-details">
          <div class="community-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item :label="$t('category')">
                {{ getCategoryTranslation(selectedCommunity.category) }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('distance')">
                {{ selectedCommunity.distance }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('address')" :span="2">
                {{ selectedCommunity.address }}
              </el-descriptions-item>
              <el-descriptions-item :label="$t('hours')" :span="2">
                {{ selectedCommunity.hours }}
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedCommunity.phone" :label="$t('phone')" :span="2">
                {{ selectedCommunity.phone }}
              </el-descriptions-item>
              <el-descriptions-item v-if="selectedCommunity.website" :label="$t('website')" :span="2">
                <a :href="selectedCommunity.website" target="_blank">{{ selectedCommunity.website }}</a>
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <div class="community-events">
            <h3>{{ $t('communityEvents') }}</h3>
            <el-empty 
              v-if="selectedCommunity.events.length === 0"
              :description="$t('noEvents')"
            />
            <div v-else class="events-list">
              <el-card 
                v-for="event in selectedCommunity.events" 
                :key="event.id"
                class="event-card"
              >
                <template #header>
                  <div class="event-header">
                    <el-tag :type="event.type === 'event' ? 'success' : 'primary'" size="small">
                      {{ event.type === 'event' ? $t('event') : $t('program') }}
                    </el-tag>
                    <span class="event-date">{{ event.date }}</span>
                  </div>
                </template>
                <h4>{{ event.title }}</h4>
                <p>{{ event.content }}</p>
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
import { Location, Loading } from '@element-plus/icons-vue'
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
const selectedCommunity = ref<any>(null)
const mapContainer = ref<HTMLElement | null>(null)
const loadingLocation = ref(true)
const loadingCommunities = ref(false)
const userLocation = ref<{ lat: number; lng: number } | null>(null)
let map: L.Map | null = null
const communityMarkers: L.Marker[] = []

const communities = ref<any[]>([])

const fetchNearbyCommunities = async (lat: number, lng: number) => {
  loadingCommunities.value = true
  communities.value = []
  
  try {
    // Build Overpass query for community organizations
    const radius = 3000 // 3km radius
    
    const query = `[out:json][timeout:25];
(
  node["amenity"="community_centre"](around:${radius},${lat},${lng});
  way["amenity"="community_centre"](around:${radius},${lat},${lng});
  node["amenity"="social_facility"](around:${radius},${lat},${lng});
  way["amenity"="social_facility"](around:${radius},${lat},${lng});
  node["amenity"="place_of_worship"](around:${radius},${lat},${lng});
  way["amenity"="place_of_worship"](around:${radius},${lat},${lng});
  node["office"="ngo"](around:${radius},${lat},${lng});
  way["office"="ngo"](around:${radius},${lat},${lng});
  node["office"="charity"](around:${radius},${lat},${lng});
  way["office"="charity"](around:${radius},${lat},${lng});
  node["leisure"="sports_centre"]["club"](around:${radius},${lat},${lng});
  way["leisure"="sports_centre"]["club"](around:${radius},${lat},${lng});
  node["office"="association"](around:${radius},${lat},${lng});
  way["office"="association"](around:${radius},${lat},${lng});
);
out center;
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
      ElMessage.info(t('noCommunityFound'))
      loadFallbackCommunities(lat, lng)
      return
    }
    
    // Process results and categorize
    const processedCommunities = data.elements
      .filter((element: any) => element.tags && element.tags.name)
      .map((element: any) => {
        const amenity = element.tags.amenity
        const office = element.tags.office
        let category = 'Community Center'
        
        if (office === 'ngo') category = 'NGO'
        else if (office === 'charity') category = 'Charity Organization'
        else if (office === 'association') category = 'Community Organization'
        else if (amenity === 'community_centre') category = 'Community Center'
        else if (amenity === 'social_facility') category = 'Social Club'
        else if (amenity === 'place_of_worship') category = 'Religious Organization'
        else if (element.tags.leisure === 'sports_centre' && element.tags.club) category = 'Youth Center'
        
        // Get coordinates - handle both nodes and ways
        const lat = element.lat || element.center?.lat
        const lon = element.lon || element.center?.lon
        
        if (!lat || !lon) return null
        
        // Calculate distance
        const distance = calculateDistance(lat, lng, lat, lon)
        
        return {
          id: element.id,
          name: element.tags.name,
          category: category,
          address: element.tags['addr:street'] 
            ? `${element.tags['addr:housenumber'] || ''} ${element.tags['addr:street']}`.trim()
            : element.tags['addr:city'] || t('addressNotAvailable'),
          distance: `${distance.toFixed(1)} km`,
          hours: element.tags.opening_hours || t('hoursNotAvailable'),
          lat: lat,
          lng: lon,
          phone: element.tags.phone || null,
          website: element.tags.website || null,
          events: generateMockEvents(element.tags.name, category)
        }
      })
      .filter((item: any) => item !== null) // Remove items without coordinates
      .slice(0, 200) // Limit to 200 communities
    
    communities.value = processedCommunities
    
    if (processedCommunities.length === 0) {
      ElMessage.info(t('noCommunityFound'))
      loadFallbackCommunities(lat, lng)
    }
  } catch (error) {
    console.error('Error fetching communities:', error)
    ElMessage.error(t('failedToLoadCommunities'))
    loadFallbackCommunities(lat, lng)
  } finally {
    loadingCommunities.value = false
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

const generateMockEvents = (name: string, category: string): any[] => {
  // Generate realistic events based on community name/category
  const events = []
  const randomChance = Math.random()
  
  if (randomChance > 0.4) {
    events.push({
      id: Math.random(),
      type: 'event',
      title: t('communityMockMeetupTitle', { name }),
      content: t('communityMockMeetupContent'),
      date: '2026-02-15'
    })
  }
  
  if (randomChance > 0.6) {
    events.push({
      id: Math.random(),
      type: 'program',
      title: t('communityMockVolunteerTitle'),
      content: t('communityMockVolunteerContent'),
      date: '2026-02-20'
    })
  }

  if (category === 'Youth Center' && randomChance > 0.5) {
    events.push({
      id: Math.random(),
      type: 'event',
      title: t('communityMockYouthWorkshopTitle'),
      content: t('communityMockYouthWorkshopContent'),
      date: '2026-02-18'
    })
  }
  
  return events
}

const loadFallbackCommunities = (lat: number, lng: number) => {
  // Fallback mock data if API fails
  communities.value = [
    {
      id: 1,
      name: t('communityMockCenterName'),
      category: 'Community Center',
      address: t('communityMockNearbyLocation'),
      distance: '0.5 km',
      hours: '9:00 AM - 6:00 PM',
      lat: lat + 0.004,
      lng: lng + 0.005,
      phone: '123-456-7890',
      website: 'https://example.com',
      events: [
        {
          id: 1,
          type: 'event',
          title: t('communityMockHealthFairTitle'),
          content: t('communityMockHealthFairContent'),
          date: '2026-02-10'
        },
        {
          id: 2,
          type: 'program',
          title: t('communityMockSeniorSupportTitle'),
          content: t('communityMockSeniorSupportContent'),
          date: t('communityMockEveryTuesday')
        }
      ]
    },
    {
      id: 2,
      name: t('communityMockHopeFoundationName'),
      category: 'NGO',
      address: t('communityMockNearbyLocation'),
      distance: '0.8 km',
      hours: '10:00 AM - 5:00 PM',
      lat: lat - 0.006,
      lng: lng + 0.004,
      phone: '098-765-4321',
      website: 'https://hopefoundation.org',
      events: [
        {
          id: 3,
          type: 'program',
          title: t('communityMockFoodDriveTitle'),
          content: t('communityMockFoodDriveContent'),
          date: '2026-02-05'
        }
      ]
    },
    {
      id: 3,
      name: t('communityMockYouthCenterName'),
      category: 'Youth Center',
      address: t('communityMockNearbyLocation'),
      distance: '1.2 km',
      hours: '2:00 PM - 8:00 PM',
      lat: lat + 0.008,
      lng: lng - 0.006,
      phone: '555-123-4567',
      website: null,
      events: [
        {
          id: 4,
          type: 'event',
          title: t('communityMockTeenGameNightTitle'),
          content: t('communityMockTeenGameNightContent'),
          date: '2026-02-08'
        }
      ]
    }
  ]
}

const filteredCommunities = computed(() => {
  return communities.value.filter(community => {
    const matchesSearch = community.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
                         community.category.toLowerCase().includes(searchQuery.value.toLowerCase())
    const matchesCategory = !selectedCategory.value || community.category === selectedCategory.value
    return matchesSearch && matchesCategory
  })
})

const getCategoryTranslation = (category: string) => {
  const categoryMap: Record<string, string> = {
    'NGO': t('ngo'),
    'Charity Organization': t('charityOrg'),
    'Community Center': t('communityCenter'),
    'Social Club': t('socialClub'),
    'Religious Organization': t('religiousOrg'),
    'Youth Center': t('youthCenter')
  }
  return categoryMap[category] || category
}

const selectCommunity = (community: any) => {
  selectedCommunity.value = community
  dialogVisible.value = true
}

const initMap = (lat: number, lng: number) => {
  if (!mapContainer.value || map) return

  // Initialize the map
  map = L.map('community-map').setView([lat, lng], 16)

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

  // Fetch real nearby communities
  fetchNearbyCommunities(lat, lng)
}

const addCommunityMarkers = () => {
  if (!map) return

  // Clear existing markers
  communityMarkers.forEach(marker => marker.remove())
  communityMarkers.length = 0

  // Add markers for filtered communities
  filteredCommunities.value.forEach(community => {
    const hasEvents = community.events.length > 0
    
    const communityIcon = L.divIcon({
      className: 'community-marker-leaflet',
      html: `
        <div class="community-marker-icon ${hasEvents ? 'has-events' : ''}">
          <span class="community-initial">${community.name.charAt(0)}</span>
          ${hasEvents ? `<span class="event-badge">${community.events.length}</span>` : ''}
        </div>
        <div class="community-marker-label">${community.name}</div>
      `,
      iconSize: [60, 60],
      iconAnchor: [30, 30]
    })

    const marker = L.marker([community.lat, community.lng], { icon: communityIcon })
      .addTo(map!)
      .on('click', () => selectCommunity(community))

    communityMarkers.push(marker)
  })
}

// Watch for changes and update markers
watch(filteredCommunities, () => {
  if (map) {
    addCommunityMarkers()
  }
})

// Watch for communities data changes and add markers
watch(communities, () => {
  if (map && communities.value.length > 0) {
    addCommunityMarkers()
  }
})

// Watch for language changes and refetch data
watch(language, () => {
  if (userLocation.value) {
    fetchNearbyCommunities(userLocation.value.lat, userLocation.value.lng)
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
        // Fallback to a default location
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
.community-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.community-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.community-toolbar h2 {
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

.community-details {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.community-info {
  width: 100%;
}

.community-events h3 {
  margin: 0 0 16px 0;
  color: #333;
  font-size: 24px;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.event-card {
  border: 1px solid #f0f0f0;
  box-shadow: none;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.event-date {
  font-size: 16px;
  color: #999;
}

.event-card h4 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
}

.event-card p {
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

.community-marker-leaflet {
  background: transparent !important;
  border: none !important;
}

.community-marker-icon {
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

.community-initial {
  font-size: 26px;
  font-weight: bold;
  color: #667eea;
}

.community-marker-icon:hover {
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  border-color: #667eea;
}

.community-marker-icon.has-events {
  border: 3px solid #667eea;
  background: #f0f4ff;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3); }
  50% { box-shadow: 0 2px 16px rgba(102, 126, 234, 0.6); }
}

.event-badge {
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

.community-marker-label {
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
