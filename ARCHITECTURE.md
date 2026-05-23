# System Architecture — CorpCare

## Overview

CorpCare is a B2B corporate employee health management platform with 4 portals, real-time notifications via WhatsApp & voice AI, and role-based access control.

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                            │
│                                                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐        │
│  │  Admin   │  │  Client  │  │ Hospital │  │ Employee │        │
│  │  Portal  │  │  Portal  │  │  Portal  │  │  Portal  │        │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘        │
│       │             │             │             │               │
│       └─────────────┼─────────────┼─────────────┘               │
│                     │    React    │                             │
│                   SPA + React Router + Axios                     │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │            Vite Dev Server / Nginx (Prod)                │   │
│  │    Dev: proxy /api → localhost:8080                      │   │
│  │    Prod: nginx reverse proxy → backend:8080              │   │
│  └──────────────────────┬───────────────────────────────────┘   │
└─────────────────────────┼───────────────────────────────────────┘
                          │ HTTP REST (JSON)
                          ▼
┌──────────────────────────────────────────────────────────────────┐
│                      API LAYER (Spring Boot)                     │
│                                                                  │
│  ┌───────────────┐  ┌────────────────┐  ┌───────────────────┐  │
│  │  Controllers  │  │   Services     │  │  GlobalException  │  │
│  │  (REST)       │──▶  (Business)    │──▶  Handler          │  │
│  └───────┬───────┘  └───────┬────────┘  └───────────────────┘  │
│          │                   │                                   │
│          ▼                   ▼                                   │
│  ┌───────────────┐  ┌────────────────┐                         │
│  │   Repos       │  │  Notification  │                         │
│  │  (Spring JPA) │  │  Service       │                         │
│  └───────┬───────┘  └───────┬────────┘                         │
│          │                   │                                   │
│          ▼                   ▼                                   │
│  ┌──────────────┐   ┌─────────────────┐                        │
│  │   MySQL DB   │   │  Twilio + Bolna │                        │
│  │  (corpcare)  │   │  (WhatsApp+Voice)│                       │
│  └──────────────┘   └─────────────────┘                        │
└──────────────────────────────────────────────────────────────────┘
```

## Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Frontend** | React 18 + Vite + React Router 6 | SPA with 4 portal UIs |
| **HTTP Client** | Axios | API communication |
| **Backend** | Spring Boot 3.4.1 (Java 17) | REST API server |
| **ORM** | Spring Data JPA + Hibernate | Database access |
| **Database** | MySQL 8.0 | Persistent storage |
| **Validation** | Jakarta Validation | Request DTO validation |
| **Notifications** | Twilio SDK 12.x | WhatsApp messaging |
| **Voice AI** | Bolna.ai REST API | Outbound voice calls |
| **Build (BE)** | Maven (Spring Boot plugin) | JAR packaging |
| **Build (FE)** | Vite | Static production build |
| **Container** | Docker + Docker Compose | Deployment |
| **Prod Proxy** | Nginx | Static serving + reverse proxy |

## Four Portal Architecture

### Admin Portal
- **Route**: `/admin/*`
- **Role**: System administrator
- **Features**: Create clients, create hospitals, manage all data, view system-wide stats
- **Access**: Direct (no login)

### Client Portal
- **Route**: `/client/*`
- **Role**: Client/corporate HR admin
- **Features**: Manage employees (max 100), view vitals, book appointments, view history
- **Access**: Direct (no login)

### Hospital Portal
- **Route**: `/hospital/*`
- **Role**: Hospital staff
- **Features**: Create/manage slots, view appointments, cancel bookings
- **Access**: Direct (no login)

### Employee Portal
- **Route**: `/employee/*`
- **Role**: End user (corporate employee)
- **Features**: Login (email + employeeCode), view vitals, self-booking, appointment history
- **Access**: Verified login only

## Data Flow — Appointment Booking

```
Employee Portal          Backend                    Database          Notifications
     │                      │                          │                   │
     │  POST /api/apps      │                          │                   │
     │─────────────────────▶│                          │                   │
     │                      │  ▶ Check slot.isBooked   │                   │
     │                      │  ▶ Check 1-person-1-slot │                   │
     │                      │  ▶ Create Appointment    │                   │
     │                      │─────────────────────────▶│                   │
     │                      │◀─────────────────────────│                   │
     │                      │                          │                   │
     │                      │  ▶ Fetch employee+slot   │                   │
     │                      │  ▶ Send WhatsApp msg ────┼──▶ Twilio API ──▶ │
     │                      │  ▶ Trigger voice call ───┼──▶ Bolna API ───▶ │
     │◀─────────────────────│                          │                   │
     │  { success: true }   │                          │                   │
```

## Notification Flow

```
AppointmentService
      │
      ├──► NotificationService.sendAppointmentNotification()
      │         │
      │         ├──► sendWhatsApp()
      │         │       │
      │         │       └──► Twilio API (POST /2010-04-01/Accounts/{sid}/Messages.json)
      │         │             │
      │         │             └──► wa.me/{phone}?text=... (link to WhatsApp)
      │         │
      │         └──► triggerBolnaCall()
      │                 │
      │                 └──► POST https://api.bolna.ai/call
      │                       Authorization: Bearer {api_key}
      │                       {
      │                         "agent_id": "...",
      │                         "recipient_phone_number": "...",
      │                         "user_data": { name, hospital, city, date, shift }
      │                       }
      │                       │
      │                       └──► Bolna calls recipient via SIP/PSTN
      │
      └──► Returns success immediately (async notifications)
```

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **No Spring Security** | Each portal is a separate UI view; employee login uses simple email+code verification. Keeps complexity low for a B2B internal tool. |
| **MySQL persistence** | Need real persistence across restarts (H2 is for testing only). MySQL is production-standard. |
| **Twilio + Bolna split** | Twilio for reliable WhatsApp, Bolna for AI-powered voice calls. Best-of-breed combo. |
| **1-person-1-slot** | Prevent duplicate bookings via backend check before persisting. |
| **Client max 100 employees** | Enforced via count query before insert. Returns 409 Conflict if exceeded. |
| **Slot-based booking** | Pre-defined time slots prevent double-booking conflicts (optimistic via isBooked flag). |
| **Cancellation = Soft delete/status change** | PUT not DELETE — preserves audit trail, frees the slot for rebooking. |
| **Async notifications** | Notifications fire after DB commit; failures logged but don't block the booking response. |
| **Portaled navigation** | Each user type sees only their relevant UI, reducing confusion in a multi-tenant system. |

## Build & Run

```bash
# Development
cd corpcare && mvn spring-boot:run                    # Backend on :8080
cd corpcare-ui && npm run dev                          # Frontend on :5173

# Production (Docker)
docker compose up --build                              # All services
```

## Security Considerations

- Employee authentication via email + employeeCode (no session tokens yet)
- No Spring Security (intentional — internal B2B tool)
- API keys for Twilio/Bolna stored as environment variables, never hardcoded
- MySQL root password configurable via env var in production
- Backend only accessible via nginx reverse proxy in production
