# 5 API Debug Traces — File-by-File Flow

## 1. POST /api/auth/employee — Employee Login + JWT

```
Client ──► SecurityConfig ──► AuthController ──► EmployeeRepository ──► JwtUtil ──► Response
```

### Files traversed (in order)

| # | File | Line(s) | What happens |
|---|------|---------|--------------|
| 1 | `config/SecurityConfig.java` | 32 | `.permitAll()` → no auth required |
| 2 | `controller/AuthController.java` | 48-58 | `employeeLogin()` — reads `email` + `employeeCode` from body |
| 3 | `entity/Employee.java` | 9-75 | JPA entity — maps to `employees` table |
| 4 | `repository/EmployeeRepository.java` | 10 | `findByEmailAndEmployeeCode()` → Spring Data JPA generates the SQL |
| 5 | `exception/BusinessException.java` | — | thrown if employee not found → `GlobalExceptionHandler` catches it |
| 6 | `exception/GlobalExceptionHandler.java` | 23-28 | returns `409 CONFLICT` with error message |
| 7 | `config/JwtUtil.java` | 25-35 | `generateToken(subject, role, name, userId)` → signs JWT with HMAC-SHA256 |
| 8 | `config/SecurityUtil.java` | — | **Not used** (this endpoint is public) |
| 9 | `dto/ApiResponse.java` | — | Response wrapper: `{ success: true, message, data: { token, employee } }` |

### Key SQL
```sql
SELECT * FROM employees WHERE email = 'rohit@vkohli.fit' AND employee_code = 'VK001'
```

### If it fails
| Symptom | Where to set breakpoint |
|---------|------------------------|
| Empty body → 400 | `AuthController.java:49` |
| Employee not found → 409 | `EmployeeRepository.java:10` → SQL in console |
| JWT signing error → 500 | `JwtUtil.java:26` |

---

## 2. POST /api/appointments — Book Appointment (core business logic)

```
Client ──► SecurityConfig ──► JwtAuthFilter ──► AppointmentController ──► AppointmentService ──► NotificationService ──► Response
```

### Files traversed (in order)

| # | File | Line(s) | What happens |
|---|------|---------|--------------|
| 1 | `config/SecurityConfig.java` | 40 | `.anyRequest().authenticated()` → token required |
| 2 | `config/JwtAuthFilter.java` | 29-51 | Extracts `Bearer` token → parses JWT → creates `JwtUser(role, userId)` → sets `SecurityContextHolder` |
| 3 | `config/JwtUtil.java` | 37-43 | `validateToken()` — verifies HMAC signature + expiry |
| 4 | `config/SecurityUtil.java` | 14-16 | `requireAuthenticated()` — reads `JwtUser` from context, throws 403 if null |
| 5 | `controller/AppointmentController.java` | 35-55 | `bookAppointment()` — validates role (HOSPITAL denied), checks ownership |
| 6 | `entity/AppointmentRequest.java` | — | DTO — holds `employeeId`, `slotId` |
| 7 | `service/EmployeeService.java` | 51-54 | `getEmployeeById()` — loads `Employee` entity |
| 8 | `service/AppointmentSlotService.java` | — | `getSlotById()` — loads `AppointmentSlot` entity |
| 9 | `entity/AppointmentSlot.java` | — | Checks `getIsBooked()` — if true → 409 |
| 10 | `repository/AppointmentRepository.java` | 8 | `findByEmployeeId()` — checks for existing booking (1 emp = 1 booking rule) |
| 11 | `exception/BusinessException.java` | — | Thrown if slot booked or employee has active booking |
| 12 | `service/AppointmentService.java` | 37-60 | `bookAppointment()` — orchestrates: validate → lock slot → save → notify |
| 13 | `repository/AppointmentSlotRepository.java` | — | `save()` — sets `slot.isBooked = true` |
| 14 | `repository/AppointmentRepository.java` | — | `save()` — persists `Appointment` |
| 15 | `service/NotificationService.java` | 51-184 | `notifyAppointmentBooked()` — fires WhatsApp (Twilio) + voice call (Bolna) async |

### Key SQL
```sql
SELECT * FROM slots WHERE id = ?
SELECT * FROM appointments WHERE employee_id = ?
UPDATE slots SET is_booked = true WHERE id = ?
INSERT INTO appointments (employee_id, slot_id, booked_at) VALUES (?, ?, NOW())
```

### Business rules enforced
- Slot not already booked → else 409
- Employee has no active booking → else 409
- Hospital role cannot book → 403
- Client can only book for their own employees
- Employee can only book for themselves

### If it fails
| Symptom | Where to set breakpoint |
|---------|------------------------|
| Auth error → 401 | `JwtAuthFilter.java:34` (check token parsing) |
| Access denied → 403 | `AppointmentController.java:39-49` (check role + userId) |
| "Slot already booked" → 409 | `AppointmentSlot.java` → `getIsBooked()` |
| "Employee already has booking" → 409 | `AppointmentService.java:47` |
| Notification fails | `NotificationService.java` (logged, doesn't block response) |

---

## 3. GET /api/clients/{clientId}/employees — List Employees Under Client

```
Client ──► SecurityConfig ──► JwtAuthFilter ──► ClientController ──► ClientService ──► EmployeeRepository ──► Response
```

### Files traversed (in order)

| # | File | Line(s) | What happens |
|---|------|---------|--------------|
| 1 | `config/SecurityConfig.java` | 40 | Token required |
| 2 | `config/JwtAuthFilter.java` | 29-51 | Parse token → set auth context |
| 3 | `config/SecurityUtil.java` | 37-44 | `requireOwnership(clientId, "CLIENT")` — admin bypass or ownership check |
| 4 | `controller/ClientController.java` | 81-86 | `getEmployees()` — calls `SecurityUtil.requireOwnership()` then service |
| 5 | `service/EmployeeService.java` | 46-49 | `getEmployeesByClient(clientId)` — delegates to repo |
| 6 | `repository/EmployeeRepository.java` | 8 | `findByClientId(clientId)` → Spring Data JPA query |
| 7 | `entity/Employee.java` | 9-75 | JPA entity mapped to response |

### Key SQL
```sql
SELECT * FROM employees WHERE client_id = ?
```

### Access logic
- **ADMIN**: always allowed (line 40 of SecurityUtil)
- **CLIENT**: allowed only if `userId == clientId` (line 41)
- **EMPLOYEE / HOSPITAL**: denied

### If it fails
| Symptom | Where to set breakpoint |
|---------|------------------------|
| 403 Access denied | `SecurityUtil.java:37-44` — check `user.role()` and `user.userId()` vs `clientId` |
| Empty `[]` | `EmployeeRepository.java:8` — check if employees exist for that client in DB |
| 404 Client not found | `ClientService.java:50-53` |

---

## 4. POST /api/report-analyzer/analyze — Upload PDF + AI Analysis

```
Client ──► SecurityConfig ──► ReportAnalyzerController ──► ReportAnalyzerService ──► Tesseract OCR ──► Response
```

### Files traversed (in order)

| # | File | Line(s) | What happens |
|---|------|---------|--------------|
| 1 | `config/SecurityConfig.java` | 34 | `POST /api/report-analyzer/**` → `.permitAll()` (public) |
| 2 | `controller/ReportAnalyzerController.java` | 35-59 | `analyze()` — accepts `MultipartFile`, validates file type/size |
| 3 | `service/ReportAnalyzerService.java` | 103-107 | `analyze()` — extracts text → detects vendor → parses 20 parameters |
| 4 | `service/ReportAnalyzerService.java` | 113-144 | `extractTextRaw()` — PDFBox text extraction → falls back to Tesseract OCR if <100 chars/page |
| 5 | `service/ReportAnalyzerService.java` | 395-403 | `detectVendor()` — scores text for "Shivani"/"Star Lab"/"Unknown" |
| 6 | `service/ReportAnalyzerService.java` | 146-181 | `parseReport()` — iterates 20 `ParamDef` definitions, matches regex against text |
| 7 | `dto/ReportAnalysisResult.java` | — | Result DTO — vendor, patient info, 20 parameters, alerts, confidence |
| 8 | `entity/ReportDetail.java` | 9-132 | Saved to DB if auth header present (controller line 50-54 saves it) |
| 9 | `repository/ReportDetailRepository.java` | 12 | `findByEmployeeIdOrderByCreatedAtDesc()` — for history |

### Key processing pipeline
```
PDF bytes ──► PDFBox text extract ──► OCR fallback (Tesseract)
    ──► Vendor classifier (regex scoring)
    ──► 20 param regex extraction (haemoglobin, RBC, WBC, etc.)
    ──► Range validation (HIGH/LOW/NORMAL/NOT_FOUND)
    ──► Alert generation (critical values)
    ──► Response
```

### If it fails
| Symptom | Where to set breakpoint |
|---------|------------------------|
| "No file uploaded" | `ReportAnalyzerController.java:39` |
| "Only PDF files" | `ReportAnalyzerController.java:43` |
| "Analysis failed" | `ReportAnalyzerService.java:103-107` (catch block) |
| OCR not working | `ReportAnalyzerService.java:127-138` — check `tesseract.setDatapath()` |
| All params NOT_FOUND | `ReportAnalyzerService.java:156-175` — check `findParameter()` regex |
| Wrong vendor detected | `ReportAnalyzerService.java:395-403` — check `detectVendor()` |

---

## 5. POST /api/health/analyze — PDF Health Analysis (PdfExtractionService path)

```
Client ──► SecurityConfig ──► HealthReportController ──► PdfExtractionService ──► VendorClassifierService ──► TemplateMatchingEngine ──► HealthRangeValidator ──► HealthRecommendationService ──► Response
```

### Files traversed (in order)

| # | File | Line(s) | What happens |
|---|------|---------|--------------|
| 1 | `config/SecurityConfig.java` | 37 | `/api/health/**` → `.permitAll()` (public) |
| 2 | `controller/HealthReportController.java` | 27-42 | `analyzePdf()` — validates file, calls `PdfExtractionService.analyze()` |
| 3 | `service/PdfExtractionService.java` | 32-66 | `analyze()` — full extraction pipeline |
| 4 | `service/VendorClassifierService.java` | 56-108 | `classify()` — scores text by Shivani/Star Lab/Generic patterns (regex) |
| 5 | `service/TemplateMatchingEngine.java` | — | `extract(text, vendorFormat)` — parses vendor-specific layout |
| 6 | `service/HealthRangeValidator.java` | — | `validate(key, value, isMale)` — returns `HealthParameter` with HIGH/LOW/NORMAL status |
| 7 | `service/HealthRecommendationService.java` | 12-26 | `generateNotifications(parameters)` — generates alert messages for abnormal values |
| 8 | `dto/HealthAnalysisReport.java` | 8-59 | Result DTO — `toResultMap()` flattens patient info + parameters + notifications |
| 9 | `dto/HealthParameter.java` | — | Single parameter: name, value, unit, range, status, color, recommendation |

### Key processing pipeline
```
PDF bytes ──► PDFBox text extraction
    ──► VendorClassifierService.classify() (Shivani/Star Lab/Generic)
    ──► TemplateMatchingEngine.extract() (vendor-specific regex)
    ──► HealthRangeValidator.validate() (range check per parameter)
    ──► HealthRecommendationService.generateNotifications() (alert text)
    ──► Response
```

### Difference from ReportAnalyzer
This endpoint uses a different pipeline:
| Feature | `/api/report-analyzer/analyze` | `/api/health/analyze` |
|---------|-------------------------------|----------------------|
| OCR support | Yes (Tesseract fallback) | No (PDFBox only) |
| Vendor detection | Simple keyword match | Pattern-scoring system |
| Parameters extracted | 20 param definitions | 16 hardcoded keys |
| DB persistence | Yes (if auth present) | No |

### If it fails
| Symptom | Where to set breakpoint |
|---------|------------------------|
| "No file uploaded" | `HealthReportController.java:29` |
| "Only PDF files" | `HealthReportController.java:33` |
| "Failed to analyze PDF" | `PdfExtractionService.java:32` (catch block) |
| Wrong vendor | `VendorClassifierService.java:56-108` — check scoring |
| Parameters not extracted | `TemplateMatchingEngine.extract()` — check vendor-specific parsing |
| Range validation wrong | `HealthRangeValidator.validate()` — check male/female logic |
