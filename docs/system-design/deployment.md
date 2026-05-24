# Deployment Guide — CorpCare

## Architecture (Production)

```
                         Internet
                            │
              ┌─────────────┴─────────────┐
              │                           │
              ▼                           ▼
     ┌────────────────┐         ┌──────────────────┐
     │  Vercel        │         │  Render          │
     │  (Frontend)    │         │  (Backend)       │
     │  Static SPA    │  API    │  Docker Web      │
     │  React 18      │◄────────│  Spring Boot     │
     │                │  calls  │  :8080           │
     └────────────────┘         └────────┬─────────┘
                                         │
                                         ▼
                                 ┌──────────────────┐
                                 │  Filess.io       │
                                 │  (MySQL 8.0)     │
                                 │  Free Tier       │
                                 │  Max 5 conns     │
                                 └──────────────────┘
```

## Services

| Service | Platform | URL |
|---------|----------|-----|
| **Frontend** | Vercel (static site) | `https://corpcare-afxw.vercel.app` |
| **Backend** | Render (Docker web service) | `https://corpcare.onrender.com` |
| **Database** | Filess.io (MySQL) | `td8n6s.h.filess.io:3307` |

## How Deployment Works

Push to GitHub `main` → both auto-deploy:

```
Git Push
   │
   ├──► Render detects push → rebuilds Docker image → restarts backend
   │
   └──► Vercel detects push → rebuilds frontend → updates static site
```

## Environment Variables (set in dashboards)

### Render (backend)

| Variable | Value |
|----------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://td8n6s.h.filess.io:3307/corpcare_simplestdo?useSSL=false` |
| `SPRING_DATASOURCE_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `SPRING_DATASOURCE_USERNAME` | `corpcare_simplestdo` |
| `SPRING_DATASOURCE_PASSWORD` | (your password) |
| `TWILIO_ACCOUNT_SID` | For WhatsApp |
| `TWILIO_AUTH_TOKEN` | For WhatsApp |
| `TWILIO_WHATSAPP_FROM` | `+14155238886` |
| `BOLNA_API_KEY` | For voice calls |
| `BOLNA_AGENT_ID` | For voice calls |

### Vercel (frontend)

| Variable | Value |
|----------|-------|
| `VITE_API_URL` | `https://corpcare.onrender.com/api` |

## Local Development

```bash
# Backend (uses H2 by default — no MySQL needed)
cd corpcare && mvn spring-boot:run

# Frontend
cd corpcare-ui && npm run dev
```

## Database

- **Hosted on**: Filess.io (free MySQL)
- **Connection limit**: 5 concurrent connections
- **HikariCP pool**: configured for max 5 connections
- **Fallback**: H2 file mode if MySQL env vars not set

## Notes

- Backend sleeps after 15 min inactivity on Render free tier
- First request after idle takes ~30s to wake up
- Upgrade Render to $7/mo for no sleeping
- Data persists on Filess.io permanently
