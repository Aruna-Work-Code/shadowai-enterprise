# ShadowAI web (new frontend, backend unchanged)
Run: `npm install && npm run dev` (needs the Spring Boot backend on :8080, or set VITE_API_URL).
Deploy (Cloudflare Pages, Free): connect the GitHub repo, build command `npm run build`, output `dist`, env `VITE_API_URL=https://<your-northflank-backend>`.
Backend (Northflank): deploy the existing `backend/` Dockerfile, port 8080, add the Postgres addon, set DB_URL (jdbc:postgresql://host:port/db), DB_USERNAME, DB_PASSWORD, a 64+ char JWT_SECRET, CORS_ALLOWED_ORIGINS=https://<your-pages-domain>, WEBHOOK_HMAC_REQUIRED=true, and JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75 -Xss256k. Health check path: /actuator/health.
