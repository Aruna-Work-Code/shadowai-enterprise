# ShadowAI: free deployment guide

## 0. Honest reality check
No host gives unlimited, limit-free hosting. This guide uses services that are **free permanently (not trials)**, and lists each limit so nothing surprises you.

| Layer | Service | Free forever? | Real limits |
|---|---|---|---|
| Frontend | Cloudflare Pages | Yes | 500 builds/month, 20,000 files, 25 MiB per file. Static traffic is not metered. |
| Backend + DB | Northflank Sandbox | Yes ("free forever, not a trial") | 2 services, 1 database, 2 cron jobs. Always on. **Credit card required to sign up.** CPU/RAM of free services is not published (see step 2.5). Described by Northflank as for testing, not production. |
| Fallback | Oracle Cloud Always Free | Yes | Now 2 OCPU / 12 GB Arm. Idle instances can be reclaimed. Card required. |


> **No credit card? Use Path B (end of this file): Cloudflare Pages + Render + Neon.** Northflank (sections 2 to 6) and Oracle both require a card at sign-up.

## 1. Put the code on GitHub
1. Create an empty **private** repo named `shadowai-enterprise` on github.com.
2. In the unzipped folder:
```
git init && git add . && git commit -m "ShadowAI"
git branch -M main
git remote add origin https://github.com/<you>/shadowai-enterprise.git
git push -u origin main
```

## 2. Backend + database on Northflank
1. Sign up at northflank.com (free Sandbox, add a card; do **not** pick a paid plan). Create a project `shadowai`, pick the region closest to you.
2. **Addons → Create addon → PostgreSQL**, free sandbox option. Wait until it is running. Open it and note host, port, database, username, password from "Connection details".
3. **Services → Create service → Combined service**, connect your GitHub repo, branch `main`.
   - Build: **Dockerfile**, build context `/backend`, Dockerfile path `/backend/Dockerfile`.
   - Networking: port `8080`, protocol HTTP, **publicly exposed**.
   - Resources: choose the **free** option. If you can pick a size, choose the largest free one (aim for 512 MB or more).
4. Runtime environment variables:

| Name | Value |
|---|---|
| DB_URL | `jdbc:postgresql://<host>:<port>/<database>` (add `?sslmode=require` if the addon says TLS is required) |
| DB_USERNAME / DB_PASSWORD | from the addon |
| JWT_SECRET | output of `openssl rand -base64 48` (never keep the default) |
| JWT_EXPIRATION_MINUTES | 30 |
| JWT_REFRESH_EXPIRATION_DAYS | 30 |
| CORS_ALLOWED_ORIGINS | set in step 4 (use `http://localhost:5173` for now) |
| WEBHOOK_HMAC_REQUIRED | true |
| WEBHOOK_MAX_SKEW_SECONDS | 300 |
| JAVA_TOOL_OPTIONS | `-XX:+UseContainerSupport -XX:MaxRAMPercentage=65 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -XX:MaxMetaspaceSize=128m` |
| SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE | 5 |
| SERVER_TOMCAT_THREADS_MAX | 20 |

5. **Health check:** add a readiness probe, HTTP, path `/actuator/health`, port 8080, with a generous initial delay (120 s) because a small CPU starts Spring Boot slowly.
6. Deploy. The first Docker build runs Maven, so it takes several minutes. Then open `https://<your-service-url>/actuator/health`. You should see `{"status":"UP"}`. Flyway creates every table automatically on first start.
7. **Memory test (important).** Northflank does not publish the free service's RAM. Smallest paid plans are 256 MB and 512 MB. Spring Boot is tight on 256 MB. If logs show `OutOfMemoryError` or the container restarts in a loop, go to section 6.

## 3. Frontend on Cloudflare Pages
1. dash.cloudflare.com → Workers & Pages → Create → **Pages → Connect to Git** → pick the repo.
2. Framework preset: none/Vite. Root directory `frontend`. Build command `npm run build`. Output directory `dist`.
3. Environment variables (Production): `VITE_API_URL` = `https://<your-northflank-url>` (no trailing slash) and `NODE_VERSION` = `20`.
4. Deploy. You get `https://<project>.pages.dev` (free; a custom domain is optional and not free).
5. If you change `VITE_API_URL` later, trigger a new deploy, because Vite bakes it in at build time.

## 4. Connect them (CORS)
In Northflank set `CORS_ALLOWED_ORIGINS=https://<project>.pages.dev` (exact, no trailing slash) and redeploy the backend.

## 5. Verify
1. Open the pages.dev URL. On `#/app` the "Governance API" row should turn READY.
2. Sign in as a demo role (priya.employee / aruna.it / neha.manager, password Demo@123).
3. Requests → New Request → submit → Evaluate. Check Policies, Audit Log and the map.
4. "Explore the demo workspace" works even if the backend is down.

## Troubleshooting
- **Row stays CONNECTING:** open the Northflank URL + `/actuator/health` directly. Not UP means check logs.
- **Browser console shows a CORS error:** `CORS_ALLOWED_ORIGINS` is wrong (https, no slash, exact domain).
- **Sign-in fails with a correct password:** check backend logs for the data seeder; demo users are created on first start of an empty database.
- **Flyway/connection error:** recheck `DB_URL`, user and password, and the `sslmode` option.
- **Pages build fails:** confirm root directory is `frontend` and `NODE_VERSION=20`.

## Keep it free
Never click Upgrade or add paid resources. Deploy sparingly (Docker builds use build minutes). Back up the database now and then: `pg_dump "<connection-string>" > backup.sql`. Keep your old Render deployment until this one is proven.

## 6. Fallback: one free VM on Oracle Cloud (if Northflank runs out of memory)
Terms checked Oct 2026: Always Free Arm capacity is 2 OCPU / 12 GB; idle Always Free instances may be reclaimed (7 days of CPU, network and, on Arm shapes, memory all under 20%); signup needs a card; capacity in some regions is limited.
1. Create an Ampere A1 Ubuntu VM (2 OCPU / 12 GB, or smaller). Open ports 80 and 443 in the VCN security list and the VM firewall.
2. Install Docker: `curl -fsSL https://get.docker.com | sh`. Clone your repo.
3. Create `.env` next to `docker-compose.production.yml` with POSTGRES_USER, POSTGRES_PASSWORD, DB_URL=`jdbc:postgresql://db:5432/aigovernance`, DB_USERNAME, DB_PASSWORD, JWT_SECRET, CORS_ALLOWED_ORIGINS and the values from section 2.
4. `docker compose -f docker-compose.production.yml up -d --build`. The frontend container serves the site and proxies `/api` to the backend.
5. HTTPS is required (browsers block mixed content). Put Caddy in front with a free DuckDNS subdomain for automatic certificates.
In this setup the frontend and API share one origin, so you can skip Cloudflare Pages.


---
# Path B: no credit card at all (Cloudflare Pages + Render + Neon)
Trade-off: the backend **sleeps after 15 idle minutes and takes about a minute to wake**. Nothing in a no-card free tier avoids that. The website itself loads instantly, shows live status, and "Explore the demo workspace" works with no backend.

## B1. Database: Neon (free, no card, no expiry)
1. neon.com → sign up → create a project (region near you) and a database.
2. Copy the **direct** connection details (not the "-pooler" host: Flyway needs a direct connection). Build: `DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require`, plus `DB_USERNAME` and `DB_PASSWORD`.
3. Limits: roughly 0.5 to 1 GB storage, 100 compute-hours a month, compute suspends after 5 idle minutes and wakes in well under a second. Free has only a short restore window, so run `pg_dump` now and then. Do not use Render's free Postgres (it expires after 30 days).

## B2. Backend: Render free web service (no card per Render's own page; confirm at sign-up)
1. render.com → New → **Web Service** → connect the GitHub repo. Environment: **Docker**. Root directory `backend`. Instance type **Free** (512 MB, 0.1 CPU). Use a **new service**: do not touch the one used by your other site.
2. Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (from Neon), `JWT_SECRET` (`openssl rand -base64 48`), `JWT_EXPIRATION_MINUTES=30`, `JWT_REFRESH_EXPIRATION_DAYS=30`, `WEBHOOK_HMAC_REQUIRED=true`, `WEBHOOK_MAX_SKEW_SECONDS=300`, `CORS_ALLOWED_ORIGINS=http://localhost:5173` (changed in B4), `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5`, `JAVA_TOOL_OPTIONS=-XX:+UseContainerSupport -XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss256k -XX:MaxMetaspaceSize=128m`.
3. Health check path: `/actuator/health`. Deploy and open `https://<name>.onrender.com/actuator/health` (first start can take 2 to 3 minutes).
4. **750-hour rule:** Render gives 750 free hours per month for the whole workspace, and a sleeping service uses none. If your other Render site and this one together run more than 750 hours, **all free services in the workspace are suspended until next month**. Keep both sleeping normally, and do not ping this service around the clock.

## B3. Frontend: Cloudflare Pages
Same as section 3 above, with `VITE_API_URL=https://<name>.onrender.com`.

## B4. CORS and test
Set `CORS_ALLOWED_ORIGINS=https://<project>.pages.dev` on Render and redeploy. Then follow section 5 (Verify). Expect the first sign-in after idle time to wait about a minute while the status rows show CONNECTING.
