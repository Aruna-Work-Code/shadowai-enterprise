# ShadowAI: AI Governance Decision Assistant
Full stack in one repo.
- `backend/`  Spring Boot 3 / Java 21, JWT + RBAC, Flyway migrations, REST/SSE, Decision Engine (**original code, unchanged**)
- `frontend/` React + Vite app: company site, Governance Fabric sign-in, and the full workspace (Command Center, Requests, Exceptions, Investigations, AI Tools, Shadow AI Discovery, Policies, Integrations, Audit Log, Notifications, Product Guide, offline demo mode)
- Database: PostgreSQL. Flyway in the backend creates every table on first start, so there is no SQL to run by hand.

## Run locally
1. `docker compose up -d` (existing compose file: Postgres + backend; check ports inside it)
2. `cd frontend && npm install && npm run dev`  → http://localhost:5173
Demo users (seeded by the backend on an empty database): `priya.employee`, `aruna.it`, `neha.manager`, password `Demo@123`.

## Deploy for free: see DEPLOY.md




--------------------
For more details to know how to deploy using the free tier:
https://drive.google.com/file/d/1C_6C90-V-5wn5CayKbySQ8z7qGYcnU1S/view?usp=drive_link
