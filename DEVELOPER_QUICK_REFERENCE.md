# To-Link Development Quick Reference

## Project Structure

```
neighbourHood-backend/          # Java Spring Boot backend
├── src/main/java/com/feature/neighbourHood_backend/
│   ├── controller/             # API endpoints
│   ├── service/                # Business logic
│   ├── model/                  # DTOs and entities
│   ├── repository/             # Database access
│   └── util/                   # Utilities, exceptions
└── pom.xml                     # Maven dependencies

neighbourHood-frontend/         # Nuxt 3 frontend
├── app/
│   ├── pages/                  # Route pages
│   ├── components/             # Reusable components
│   ├── api/                    # API client methods
│   ├── layouts/                # Layout templates
│   ├── middleware/             # Auth middleware
│   ├── plugins/                # Vue plugins
│   ├── utils/                  # Helper functions
│   ├── assets/                 # Styles, images
│   └── app.vue                 # Root component
├── i18n/locale/                # Translations (en.json, zh.json)
├── public/                     # Static files
├── server/api/                 # Nuxt server routes
└── nuxt.config.ts             # Nuxt configuration
```

## Common Tasks

### Add a New i18n Key
1. Add key to `i18n/locale/en.json`
2. Add same key to `i18n/locale/zh.json` with translated value
3. Use in component: `{{ $t('keyName') }}` or `const msg = t('keyName')`

### Persist User State to localStorage
```typescript
// Save
localStorage.setItem(key, JSON.stringify(value))

// Load
const value = JSON.parse(localStorage.getItem(key) || 'null')

// Clear
localStorage.removeItem(key)
```

### Create a New Centered Dialog
```vue
<el-dialog 
  v-model="showDialog" 
  :title="$t('dialogTitle')"
  align-center
  width="560px"
>
  <!-- content -->
</el-dialog>
```

### Add a Backend API Endpoint
1. Create DTO in `model/DTO/`
2. Add method to service in `service/`
3. Map endpoint in controller with `@PostMapping` or `@GetMapping`
4. Return `ApiResponse<T>` wrapper

Example:
```java
@PostMapping("/endpoint")
public ApiResponse<String> endpoint(@RequestBody RequestDTO req, @AuthenticationPrincipal CustomUserDetails user) {
    // Call service
    service.method(user.getId(), req.getField());
    return ApiResponse.success("Message");
}
```

### Add Frontend API Method
```typescript
// In app/api/service.ts
export const methodName = async (param: Type): Promise<ApiResponse<ResponseType>> => {
  return $fetch('/api/endpoint', {
    method: 'POST',
    ...getConfig(),
    body: { param }
  })
}
```

### Debug localStorage
```javascript
// In browser console
localStorage.clear()           // Clear all
localStorage.getItem('key')    // Get value
Object.keys(localStorage)      // List all keys
```

## API Response Format

All backend endpoints return:
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

Error responses:
```json
{
  "code": 400,
  "message": "Error description",
  "data": null
}
```

## Authentication

- JWT token stored in localStorage
- Passed in request headers via `getConfig()` in API calls
- Validated in `auth.global.ts` middleware
- User info available as `CustomUserDetails` in controllers

## Form Validation

```typescript
const form = reactive({
  field: ''
})

const rules = {
  field: [
    { required: true, message: $t('required'), trigger: 'blur' }
  ]
}

// In template
<el-form :model="form" :rules="rules">
  <el-form-item prop="field">
    <el-input v-model="form.field" />
  </el-form-item>
</el-form>
```

## Component Composition Pattern

```vue
<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from '#app'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'

// Composition functions
const router = useRouter()
const route = useRoute()
const { t } = useI18n()

// State
const form = reactive({ field: '' })
const loading = ref(false)

// Computed
const isValid = computed(() => form.field.length > 0)

// Watch
watch(() => route.params.id, (newId) => {
  // Reload data
})

// Methods
const handleSubmit = async () => {
  loading.value = true
  try {
    const result = await apiMethod(form)
    ElMessage.success(t('success'))
  } catch (error) {
    ElMessage.error(t('error'))
  } finally {
    loading.value = false
  }
}
</script>
```

## Styling Best Practices

- Use CSS variables for theming: `var(--el-dialog-width)`, `var(--el-text-color-primary)`
- Mobile-first media queries: `@media (max-width: 768px)`
- Dark mode compatible classes automatically applied by Element Plus
- Global styles in `assets/css/global.css`
- Component scoped styles in `<style scoped>` blocks

## Testing Command

Frontend:
```bash
cd neighbourHood-frontend
npm run build  # Production build
```

Backend:
```bash
cd neighbourHood-backend
mvn clean compile  # Compile only, no tests
```

## Common Error Solutions

| Error | Solution |
|-------|----------|
| i18n key not found | Add key to both en.json and zh.json |
| Dialog not centered | Add `align-center` attr + check global.css |
| localStorage undefined | Ensure code runs in browser (not SSR) |
| API 401 Unauthorized | Check JWT token validity, re-login if needed |
| Component not visible | Check z-index, parent overflow hidden, display: none |
| Form validation failing | Verify rules match model structure exactly |
| Image upload not working | Check `:auto-upload="false"`, handle file list |

## File Naming Conventions

- **Pages**: lowercase with hyphens (home.vue, create-post.vue)
- **Components**: PascalCase (PostCard.vue, EditDialog.vue)
- **API files**: camelCase with method (auth.ts, post.ts)
- **DTOs**: PascalCase with DTO suffix (UserDTO.java, CreatePostDTO.java)
- **Services**: PascalCase with Service suffix (UserService.java)
- **Controllers**: PascalCase with Controller suffix (UserController.java)

## Dependencies

**Frontend:**
- Nuxt 3, Vue 3, TypeScript
- Element Plus (UI components)
- Vue-i18n (localization)
- Axios (HTTP client)

**Backend:**
- Spring Boot 3.x
- Spring Security (JWT auth)
- JPA/Hibernate (ORM)
- PostgreSQL (database)
- Maven (build tool)

## Useful VSCode Extensions

- Volar (Vue language support)
- ESLint
- Prettier
- Thunder Client (API testing)
- Git Lens
