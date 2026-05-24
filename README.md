# CorpCare — B2B Corporate Employee Health Management Platform

A full-stack platform for managing corporate employee health checkups.  
Corporate clients register employees, hospitals offer slots, employees book appointments, and the system sends **WhatsApp + AI voice call** confirmations automatically.

## System Architecture

```
┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Admin       │  │  Client      │  │  Hospital    │  │  Employee    │
│  Portal      │  │  Portal      │  │  Portal      │  │  Portal      │
├──────────────┤  ├──────────────┤  ├──────────────┤  ├──────────────┤
│              │  │ Employee Mgmt│  │ Slot Mgmt    │  │ Self-Booking │
│ Client Mgmt  │  │ Vitals Entry │  │ Appointments │  │ Vitals       │
│ Hospital Mgmt│  │ Appointments │  │ Cancel       │  │ History      │
│              │  │              │  │              │  │              │
└──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
       │                 │                 │                 │
       └─────────────────┴─── React SPA ───┴─────────────────┘
                           │
                    Nginx / Vite Proxy
                           │
                    Spring Boot REST API
                           │
                    ┌──────┴──────┐
                    │   MySQL 8   │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
         Twilio        Bolna.ai       Email
       (WhatsApp)     (Voice Call)   (Future)
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18, Vite, React Router 6, Axios |
| **Backend** | Java 17, Spring Boot 3.4.1, Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0 (prod), H2 (dev) |
| **Notifications** | Twilio SDK (WhatsApp), Bolna.ai REST API (Voice AI) |
| **Build** | Maven (BE), Vite (FE) |
| **Deployment** | Docker, Docker Compose, Nginx |
| **CI/CD** | GitHub Actions |

## Four Portal Architecture

### 👑 Admin Portal (`/admin/*`)
Create/manage corporate clients and hospitals. System-wide dashboard with stats.

### 🏢 Client Portal (`/client/*`)
HR manages employees (max 100), records vitals, books appointments, views history.

### 🏥 Hospital Portal (`/hospital/*`)
Staff creates time slots, views upcoming appointments, cancels bookings.

### 👤 Employee Portal (`/employee/*`)
Login via email + employee code, self-booking with slot selection, vitals entry, appointment history.

## Business Rules

| Rule | Enforcement |
|------|-------------|
| **1 slot = 1 booking** | `slot.isBooked` flag — returns 409 if already booked |
| **1 person = 1 slot** | Backend checks no existing active appointment for employee |
| **Max 100 employees/client** | Count query before insert — returns 409 if exceeded |
| **Notifications** | WhatsApp (Twilio) + Voice AI call (Bolna) on every booking |
| **Cancellation** | PUT marks slot free + deletes appointment — enables rebooking |

## API Overview

See [docs/system-design/api-flow.md](docs/system-design/api-flow.md) for full API documentation with sequence diagrams.

| Resource | Key Endpoints |
|----------|--------------|
| Clients | `GET/POST /api/clients`, `PUT/DELETE /api/clients/{id}` |
| Employees | `POST /api/clients/{id}/employees`, `POST /api/employees/verify` |
| Hospitals | `GET/POST /api/hospitals`, `POST /api/hospitals/{id}/slots` |
| Slots | `GET /api/hospitals/{id}/slots/available` |
| Appointments | `POST /api/appointments`, `PUT /api/appointments/{id}/cancel` |
| Vitals | `GET/POST /api/vitals/employee/{id}` |

## Quick Start

### Prerequisites
- Java 17+, Node.js 20+, MySQL 8.0, Maven

### Development

```bash
# Terminal 1: Backend
cd corpcare
# Set env vars for notifications (optional — app works without them)
export TWILIO_ACCOUNT_SID=...
export TWILIO_AUTH_TOKEN=...
export TWILIO_WHATSAPP_FROM=+14155238886
export BOLNA_API_KEY=...
export BOLNA_AGENT_ID=...
mvn spring-boot:run
# → http://localhost:8080

# Terminal 2: Frontend
cd corpcare-ui
npm install
npm run dev
# → http://localhost:5173
```

The database is auto-created and seeded with sample data on first run.

### Production (Render + Vercel — free)

```bash
# Backend → https://render.com (Docker web service)
# Frontend → https://vercel.com (static site from corpcare-ui/)
```

## Default Test Data

| Type | Data |
|------|------|
| **Client** | Virat Kohli Fitness Pvt Ltd |
| **Employees** | Rohit Sharma (VK001), Rahul Dravid (VK002) |
| **Hospitals** | Apollo Bengaluru |
| **Slots** | 4 available slots across next 2 days |

## System Design

Detailed architecture documentation is available in the [docs/system-design](docs/system-design/) directory:

| Document | Description |
|----------|-------------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | High-level system architecture with diagrams |
| [data-model.md](docs/system-design/data-model.md) | Entity Relationship Diagram and schema |
| [api-flow.md](docs/system-design/api-flow.md) | API sequence diagrams and complete reference |
| [deployment.md](docs/system-design/deployment.md) | Docker, CI/CD, and production deployment guide |

## Notification Flow

```
Appointment Booking
      │
      ├──► WhatsApp (Twilio)
      │     └── "Your appointment confirmed at Apollo Bengaluru on 2026-05-25"
      │
      └──► Voice Call (Bolna.ai)
            └── AI agent calls employee with appointment details
```

Both notifications are **asynchronous** — the booking response is immediate, notifications fire in the background.

## Project Structure

```
corpcare/                   # Spring Boot backend
├── src/main/java/com/corpcare/
│   ├── config/             # DataSeeder, CORS config
│   ├── controller/         # REST controllers
│   ├── dto/                # Request/response DTOs
│   ├── entity/             # JPA entities
│   ├── enums/              # Enumerations (Shift)
│   ├── exception/          # Global exception handler
│   ├── repository/         # Spring Data JPA repos
│   └── service/            # Business logic + notifications
├── Dockerfile
├── docker-compose.yml
└── docs/system-design/

corpcare-ui/                # React frontend
├── src/
│   ├── api/                # Axios config
│   ├── components/         # Navbar, portal navigation
│   ├── pages/
│   │   ├── admin/          # Admin portal
│   │   ├── client/         # Client portal
│   │   ├── hospital/       # Hospital portal
│   │   └── employee/       # Employee portal
│   └── App.jsx             # Router setup
├── Dockerfile
├── nginx/default.conf
└── vercel.json
```

## Auto-Deploy

Push to `main` automatically deploys both:

| Platform | Service | Trigger |
|----------|---------|---------|
| **Render** | Backend (Docker) | Any push to `main` |
| **Vercel** | Frontend (static) | Any push to `main` |
