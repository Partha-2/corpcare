# API Flows — CorpCare

## Base URL

Development: `http://localhost:8080/api`
Production: `https://your-domain.com/api`

## Response Envelope

All APIs return a standard `ApiResponse<T>` wrapper:

```json
{
  "status": "success" | "error",
  "message": "...",
  "data": { ... }
}
```

## Core Flows

### 1. Employee Booking Flow

```
Employee Portal                    Backend                        Database
     │                               │                              │
     │  1. Login                       │                              │
     │  POST /api/employees/verify    │                              │
     │  { email, employeeCode }       │                              │
     │───────────────────────────────▶│                              │
     │◀───────────────────────────────│  { employee data }          │
     │                               │                              │
     │  2. View available slots       │                              │
     │  GET /api/hospitals/1/slots/   │                              │
     │      available                 │                              │
     │───────────────────────────────▶│──────────────────────────────▶│
     │◀───────────────────────────────│◀──────────────────────────────│
     │                               │                              │
     │  3. Book appointment           │                              │
     │  POST /api/appointments       │                              │
     │  { employeeId, slotId }       │                              │
     │───────────────────────────────▶│                              │
     │                               │  1. Check slot.isBooked      │
     │                               │  2. Check 1-person-1-slot    │
     │                               │  3. Create Appointment       │
     │                               │──────────────────────────────▶│
     │                               │  4. Mark slot as booked      │
     │                               │──────────────────────────────▶│
     │                               │                              │
     │                               │  5. Send WhatsApp ───────────│─▶ Twilio
     │                               │  6. Trigger Bolna call ──────│─▶ Bolna
     │                               │                              │
     │◀───────────────────────────────│  { appointment+slot data } │
```

### 2. Cancel Appointment Flow

```
User                               Backend                        Database
 │                                    │                              │
 │  PUT /api/appointments/{id}/cancel │                              │
 │───────────────────────────────────▶│                              │
 │                                    │  1. Find appointment        │
 │                                    │──────────────────────────────▶│
 │                                    │  2. Mark slot.isBooked=false │
 │                                    │──────────────────────────────▶│
 │                                    │  3. Delete appointment       │
 │                                    │──────────────────────────────▶│
 │◀───────────────────────────────────│  { success: true }          │
```

### 3. Admin — Client & Employee Creation

```
Admin Portal                        Backend                         DB
     │                                  │                          │
     │  1. Create Client                │                          │
     │  POST /api/clients               │                          │
     │  { companyName, industry, ... }  │                          │
     │─────────────────────────────────▶│─────────────────────────▶│
     │◀─────────────────────────────────│  { client data }         │
     │                                  │                          │
     │  2. Add Employee to Client       │                          │
     │  POST /api/clients/{id}/employees│                          │
     │  { employeeCode, fullName, ... } │                          │
     │─────────────────────────────────▶│                          │
     │                                  │  Enforce max 100        │
     │                                  │─────────────────────────▶│
     │◀─────────────────────────────────│  { employee data }       │
```

### 4. Hospital — Slot Creation

```
Hospital Portal                      Backend                        DB
     │                                  │                          │
     │  POST /api/hospitals/{id}/slots  │                          │
     │  { date, shift, startTime,       │                          │
     │    endTime }                     │                          │
     │─────────────────────────────────▶│─────────────────────────▶│
     │◀─────────────────────────────────│  { slot created }        │
```

## API Reference

### Clients

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/clients` | List all clients |
| POST | `/api/clients` | Create new client |
| GET | `/api/clients/{id}` | Get client by ID |
| GET | `/api/clients/{id}/employees` | List employees for client |
| POST | `/api/clients/{id}/employees` | Add employee to client |
| PUT | `/api/clients/{id}` | Update client |
| DELETE | `/api/clients/{id}` | Delete client |

### Employees

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/employees` | List all employees |
| GET | `/api/employees/{id}` | Get employee by ID |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |
| POST | `/api/employees/verify` | Login (email + employeeCode) |

### Appointments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/appointments` | Book appointment |
| GET | `/api/appointments/employee/{id}` | Get employee's appointments |
| GET | `/api/appointments/hospital/{id}` | Get hospital's appointments |
| PUT | `/api/appointments/{id}/cancel` | Cancel appointment |

### Hospitals

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/hospitals` | List all hospitals |
| GET | `/api/hospitals/{id}` | Get hospital by ID |
| POST | `/api/hospitals` | Create hospital |
| PUT | `/api/hospitals/{id}` | Update hospital |
| DELETE | `/api/hospitals/{id}` | Delete hospital |
| GET | `/api/hospitals/{id}/slots` | List all slots for hospital |
| GET | `/api/hospitals/{id}/slots/available` | List available slots |
| POST | `/api/hospitals/{id}/slots` | Create slot |

### Employee Vitals

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/vitals/employee/{id}` | Get vitals for employee |
| POST | `/api/vitals` | Create/upsert vitals |
| PUT | `/api/vitals/{id}` | Update vitals |

### Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/notifications` | Send WhatsApp + Bolna (orchestrated) |

## Error Codes

| HTTP Status | Error | Description |
|-------------|-------|-------------|
| 400 | Bad Request | Validation failure (missing/invalid fields) |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate booking, employee limit reached |
| 500 | Internal Error | Server-side failure |

## Bulk Operations

Currently all operations are single-resource. Future enhancements:
- Export employee reports as CSV
- Bulk slot creation
- Batch employee registration
