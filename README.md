# CorpCare — B2B Corporate Employee Health Management Platform

A full-stack platform for managing corporate employee health checkups.  
Corporate clients register employees, hospitals offer slots, employees book appointments with date selection, and the system sends **WhatsApp (Twilio) + AI voice call (Bolna.ai - Nova)** confirmations automatically.

## Demo

- **Frontend:** https://corpcare-afxw.vercel.app
- **Admin Login:** Password `admin123` (configurable via `ADMIN_PASSWORD` env var)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 18, Vite, React Router 6, Axios |
| **Backend** | Java 17, Spring Boot 3.4.1, Spring Data JPA, Hibernate |
| **Database** | MySQL 8.0 (prod via Filess.io free tier), H2 file (dev, zero setup) |
| **Notifications** | Twilio SDK (WhatsApp), Bolna.ai REST API (Voice AI - Nova agent) |
| **AI Chat** | Groq (Llama 3.3 70B) via backend proxy at `/api/chat` |
| **Auth** | Admin: password gate; Employee: email + employeeCode verification |
| **Build** | Maven (BE), Vite (FE) |
| **Dark Mode** | CSS variables + localStorage toggle |

## Four Portal Architecture

### 👑 Admin Portal (`/admin/*`)
Password-protected. Create/manage clients & hospitals with inline edit, search, delete. Expand rows to manage employees/slots.

### 🏢 Client Portal (`/client/*`)
HR manages employees (max 100), records vitals, books appointments with date picker, views history.

### 🏥 Hospital Portal (`/hospital/*`)
Staff creates 8-hour slots (Morning/Evening/Night), views booked appointments with employee codes, cancels bookings with confirm dialog.

### 👤 Employee Portal (`/employee/*`)
Login via email + employee code, self-booking with date + slot selection, vitals entry, appointment history. Gets WhatsApp + voice call on booking.

## Key Features

| Feature | Detail |
|---------|--------|
| **Date picker** | Employees select date before available slots |
| **1 slot = 1 booking** | Slot locked after booking, freed on cancel |
| **1 employee = 1 active booking** | Backend enforces no duplicate bookings |
| **Max 100 employees/client** | Count check before insert |
| **Search + filter** | SearchBar component on admin tables |
| **Inline edit** | Edit clients/hospitals directly in table rows |
| **Delete with cascade** | Delete client → removes employees/vitals/appointments |
| **Toast notifications** | Auto-dismiss popup replacing alert banners |
| **Confirm dialogs** | Before cancelling appointments |
| **Loading states** | Spinner on every page while data loads |
| **Error Boundary** | Catches JS crashes, shows reload button |
| **Dark mode** | Persistent toggle (🌙/☀️) |
| **Responsive** | Optimized for mobile at 768px and 480px |
| **Groq AI Chatbot** | FAQ + AI chat via `/api/chat` proxy |
| **Nova voice call** | Conversational 5-step Bolna.ai agent |
| **WhatsApp message** | Twilio with appointment details |

## Business Rules

| Rule | Enforcement |
|------|-------------|
| **1 slot = 1 booking** | `slot.isBooked` flag — returns 409 if already booked |
| **1 person = 1 slot** | Backend checks no existing active appointment for employee |
| **Max 100 employees/client** | Count query before insert — returns 409 if exceeded |
| **Notifications** | WhatsApp (Twilio) + Voice AI call (Bolna Nova) on every booking |
| **Cancellation** | `@Transactional` — marks slot free + deletes appointment |

## Notification Flow

```
Appointment Booking
      │
      ├──► WhatsApp (Twilio)
      │     └── "Your appointment confirmed at Apollo Bengaluru on 2026-05-25"
      │
      └──► Voice Call (Bolna.ai - Nova)
            └── 5-step conversation: greet → introduce → confirm details → ask confirmation → close
                Handles: can't hear, wrong number, no response, reschedule requests
```

Both notifications are **asynchronous** — the booking response is immediate, notifications fire in the background.

## API Reference

**Base URL:** `http://localhost:8080`

All authenticated endpoints require header: `Authorization: Bearer {token}`

---

### Auth (no token required)

| Method | URL | Body | Returns |
|--------|-----|------|---------|
| POST | `/api/auth/admin` | `{"password":"admin123"}` | `{ token, name }` |
| POST | `/api/auth/employee` | `{"email":"...","employeeCode":"..."}` | `{ token, employee }` |
| POST | `/api/auth/client` | `{"email":"...","password":"..."}` | `{ token, client }` |
| POST | `/api/auth/hospital` | `{"email":"...","password":"..."}` | `{ token, hospital }` |

---

### Admin (password: `admin123`)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/stats` | Dashboard statistics |
| GET | `/api/clients` | List all clients |
| POST | `/api/clients` | Create client |
| PUT | `/api/clients/{id}` | Update client |
| DELETE | `/api/clients/{id}` | Delete client + employees + vitals |
| GET | `/api/clients/{clientId}/employees` | List employees under client |
| POST | `/api/clients/{clientId}/employees` | Add employee to client |
| GET | `/api/employees/{employeeId}` | Get single employee |
| GET | `/api/employees/{employeeId}/vitals` | Get employee vitals |
| GET | `/api/hospitals` | List all hospitals |
| POST | `/api/hospitals` | Create hospital |
| PUT | `/api/hospitals/{id}` | Update hospital |
| DELETE | `/api/hospitals/{id}` | Delete hospital + slots + appointments |
| GET | `/api/hospitals/{hospitalId}/slots` | List all slots |
| POST | `/api/hospitals/{hospitalId}/slots` | Create slot |
| GET | `/api/hospitals/{hospitalId}/slots/available` | Available slots |
| GET | `/api/hospitals/{hospitalId}/slots/available?date=YYYY-MM-DD` | Filter by date |
| GET | `/api/appointments` | List all appointments |
| GET | `/api/appointments/employee/{id}` | Appointments by employee |
| GET | `/api/appointments/hospital/{id}` | Appointments by hospital |
| PUT | `/api/appointments/{id}/cancel` | Cancel appointment |
| GET | `/api/report-analyzer/history` | All report analysis history |
| GET | `/api/report-analyzer/history/{id}` | Report detail |
| GET | `/api/report-analyzer/history/{id}/parameters` | Report with individual parameter columns |
| GET | `/api/report-analyzer/critical` | Reports with abnormal values |
| GET | `/api/chat` | Get chat status |
| POST | `/api/chat` | Send chat message |

---

### Employee (login: email + employeeCode)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/stats` | Dashboard stats |
| GET | `/api/hospitals` | Browse hospitals |
| GET | `/api/hospitals/{id}/slots/available` | Check available slots |
| GET | `/api/hospitals/{id}/slots/available?date=YYYY-MM-DD` | Filter by date |
| POST | `/api/appointments` | Book appointment |
| GET | `/api/appointments/employee/{myId}` | My appointments |
| PUT | `/api/appointments/{id}/cancel` | Cancel my appointment |
| GET | `/api/employees/{myId}/vitals` | My vitals |
| POST | `/api/employees/{myId}/vitals` | Update my vitals |
| POST | `/api/report-analyzer/analyze` | Upload and analyze PDF (form-data: file) |
| POST | `/api/report-analyzer/debug-text` | Extract raw PDF text (form-data: file) |
| GET | `/api/report-analyzer/history` | My past reports |
| GET | `/api/report-analyzer/history/{id}` | Past report detail |
| GET | `/api/report-analyzer/history/{id}/parameters` | Past report with flat parameters |
| GET | `/api/report-analyzer/critical` | My critical reports |

---

### Client (login: email + password, default: `client123`)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/stats` | Dashboard stats |
| GET | `/api/clients/{myId}/employees` | My employees |
| POST | `/api/clients/{myId}/employees` | Add employee |
| GET | `/api/employees/{id}/vitals` | Employee vitals |
| POST | `/api/employees/{id}/vitals` | Update employee vitals |
| GET | `/api/appointments` | My employees' appointments |
| POST | `/api/appointments` | Book for my employee |
| GET | `/api/hospitals` | Browse hospitals |

---

### Hospital (login: email + password, default: `hospital123`)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/api/stats` | Dashboard stats |
| GET | `/api/hospitals/{myId}/slots` | My slots |
| POST | `/api/hospitals/{myId}/slots` | Create slot |
| GET | `/api/hospitals/{myId}/slots/available` | Available slots |
| GET | `/api/appointments/hospital/{myId}` | My appointments |
| PUT | `/api/appointments/{id}/cancel` | Cancel appointment |

---

### Public (no auth required)

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/` | Home page |
| GET | `/api/stats` | Public dashboard stats |
| POST | `/api/health/analyze` | Analyze PDF (multipart `file`) |
| GET | `/api/health/report/{employeeCode}` | Get health report by employee code (e.g. `EMP001`) |
| POST | `/api/report-analyzer/analyze` | Analyze PDF (saves only if auth header present) |
| POST | `/api/report-analyzer/debug-text` | Extract PDF raw text |
| POST | `/api/employees/verify` | Verify employee credentials |

## Quick Start

### Prerequisites
- Java 17+, Node.js 20+, Maven

### Development

```bash
# Terminal 1: Backend
cd corpcare
# Set env vars for notifications (optional — app works without them)
export TWILIO_ACCOUNT_SID=...
export TWILIO_AUTH_TOKEN=...
export BOLNA_API_KEY=...
mvn spring-boot:run
# → http://localhost:8080

# Terminal 2: Frontend
cd corpcare-ui
npm install
npm run dev
# → http://localhost:5173
```

The database (H2 file mode) is auto-created and seeded with sample data on first run.  
No MySQL setup needed for development — just run.

## Default Test Data

| Type | Data |
|------|------|
| **Client** | Virat Kohli Fitness Pvt Ltd |
| **Employees** | Rohit Sharma (VK001), Rahul Dravid (VK002) |
| **Hospitals** | Apollo Bengaluru |
| **Slots** | Available slots across next 2 days |
| **Test login** | `rohit@vkohli.fit` / `VK001` |

## Project Structure

```
corpcare/                       # Spring Boot backend
├── src/main/java/com/corpcare/
│   ├── config/                 # DataSeeder, CORS config
│   ├── controller/             # REST controllers
│   ├── dto/                    # Request/response DTOs
│   ├── entity/                 # JPA entities (Client, Employee, Hospital, Slot, Appointment, Vitals)
│   ├── enums/                  # ShiftType, BloodGroup
│   ├── exception/              # Global exception handler + BusinessException
│   ├── repository/             # Spring Data JPA repos
│   └── service/                # Business logic + Twilio/Bolna notifications
└── src/main/resources/
    └── application.properties  # H2 default, MySQL via env vars

corpcare-ui/                    # React frontend
├── src/
│   ├── api/axios.js            # Axios config (auto-detects Vercel vs localhost)
│   ├── components/             # Toast, ChatBot, ErrorBoundary, ThemeToggle, SearchBar, Loading
│   ├── pages/
│   │   ├── admin/              # Clients, Hospitals (search + inline edit + delete)
│   │   ├── client/             # Dashboard, Employees, Vitals, BookAppointment, Appointments
│   │   ├── hospital/           # Dashboard, Slots, Appointments
│   │   ├── employee/           # Login, Dashboard, Book, Vitals, Appointments
│   │   └── Landing.jsx         # 10-section landing page
│   └── App.jsx                 # Routes, ErrorBoundary, ThemeToggle, ToastContainer, ChatBot
├── public/favicon.svg
└── vercel.json
```

## System Design

Detailed architecture documentation is available in the [docs/system-design](docs/system-design/) directory:

| Document | Description |
|----------|-------------|
| [data-model.md](docs/system-design/data-model.md) | Entity Relationship Diagram and schema |
| [api-flow.md](docs/system-design/api-flow.md) | API sequence diagrams and complete reference |
