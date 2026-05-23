# Data Model — CorpCare

## Entity Relationship Diagram

```mermaid
erDiagram
    Client ||--o{ Employee : "has"
    Client ||--o{ Hospital : "manages"
    Employee ||--o| EmployeeVitals : "has"
    Employee ||--o{ Appointment : "books"
    Appointment ||--|| AppointmentSlot : "uses"
    Hospital ||--o{ AppointmentSlot : "offers"

    Client {
        Long id PK
        string companyName
        string industry
        string address
        string contactEmail
        string contactPhone
    }

    Employee {
        Long id PK
        Long clientId FK
        string employeeCode UK
        string fullName
        string email
        string phone
        string department
        string designation
    }

    EmployeeVitals {
        Long id PK
        Long employeeId FK UK
        Double height
        Double weight
        string bloodGroup
        string bloodPressure
        int heartRate
        string medicalConditions
    }

    Hospital {
        Long id PK
        string name
        string location
        string city
        string contactEmail
        string contactPhone
    }

    AppointmentSlot {
        Long id PK
        Long hospitalId FK
        LocalDate date
        string shift
        LocalTime startTime
        LocalTime endTime
        boolean isBooked
    }

    Appointment {
        Long id PK
        Long employeeId FK
        Long slotId FK
        LocalDateTime bookedAt
    }
```

## Entity Descriptions

### Client
Represents a corporate client (company) that registers employees for health checkups.

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| companyName | String | Not null, unique |
| industry | String | Not null |
| address | String | Not null |
| contactEmail | String | Not null |
| contactPhone | String | Not null |

### Employee
An individual employee belonging to a client, who can book health checkup appointments.

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| clientId | Long | FK → Client, not null |
| employeeCode | String | Not null, unique per client |
| fullName | String | Not null |
| email | String | Not null |
| phone | String | Optional, used for notifications |
| department | String | Optional |
| designation | String | Optional |

**Constraints:**
- `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "employee_code"}))`
- Max 100 employees per client (enforced in `EmployeeService.createEmployee`)

### EmployeeVitals
Health vitals data for an employee (one-to-one).

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| employeeId | Long | FK → Employee, unique (one-to-one) |
| height | Double | Optional |
| weight | Double | Optional |
| bloodGroup | String | Optional |
| bloodPressure | String | Optional |
| heartRate | Integer | Optional |
| medicalConditions | String | Optional (comma-separated) |

### Hospital
A hospital/lab where health checkups are conducted.

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| name | String | Not null |
| location | String | Not null |
| city | String | Not null |
| contactEmail | String | Not null |
| contactPhone | String | Not null |

### AppointmentSlot
A time slot at a hospital for health checkups.

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| hospitalId | Long | FK → Hospital, not null |
| date | LocalDate | Not null |
| shift | String | ENUM: MORNING, AFTERNOON, EVENING |
| startTime | LocalTime | Not null |
| endTime | LocalTime | Not null |
| isBooked | Boolean | Default false |

### Appointment
Links an employee to a booked slot (one appointment per slot).

| Field | Type | Constraints |
|-------|------|-------------|
| id | Long | PK, auto-generated |
| employeeId | Long | FK → Employee, not null |
| slotId | Long | FK → AppointmentSlot, not null, unique |
| bookedAt | LocalDateTime | Not null |

**Constraints:**
- Slot is marked `isBooked = true` when appointment is created
- Cancel flow: slot `isBooked` set to `false` and appointment deleted (PUT cancel)
- 1-person-1-slot: employee can have only one active appointment at a time (enforced in `AppointmentService`)

## Indexes (via JPA)

- `employee(client_id, employee_code)` — unique composite index for login lookup
- `appointment_slot(hospital_id, date)` — for loading slots by hospital+date
- `appointment(employee_id)` — for fetching employee's bookings
- `appointment(slot_id)` — unique index ensures 1 appointment per slot
