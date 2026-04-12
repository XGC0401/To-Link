# To-Link

## Render deployment

This repo already includes a Render blueprint in [render.yaml](render.yaml) for:

- `neighbourhood-backend` (Spring Boot)
- `neighbourhood-frontend` (Nuxt)

### Before you deploy

Prepare these Render environment variables for the backend service:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SUPABASE_ENDPOINT`
- `SUPABASE_ACCESS_KEY`
- `SUPABASE_SECRET_KEY`
- `SUPABASE_BUCKET`
- `CORS_ALLOWED_ORIGINS`

Prepare this variable for the frontend service:

- `NUXT_PUBLIC_API_BASE_URL`

### Recommended Render setup order

1. Push this repository to GitHub.
2. In Render, choose New + and select Blueprint.
3. Connect this repository so Render reads [render.yaml](render.yaml).
4. Set all backend environment variables in the `neighbourhood-backend` service.
5. Deploy the backend first and copy its public URL.
6. Set `NUXT_PUBLIC_API_BASE_URL` in the `neighbourhood-frontend` service to `https://your-backend.onrender.com/api`.
7. Set `CORS_ALLOWED_ORIGINS` in the backend to your frontend URL, for example `https://your-frontend.onrender.com`.
8. Redeploy both services if you changed either URL.

### Expected result

After deployment Render will generate public links in this format:

- Backend: `https://neighbourhood-backend.onrender.com`
- Frontend: `https://neighbourhood-frontend.onrender.com`

Your actual subdomain may vary if the name is already taken.

### Notes

- The backend build uses `mvn clean package -DskipTests` because the checked-in Maven wrapper is incomplete.
- The backend now expects secrets from Render environment variables instead of repository defaults.