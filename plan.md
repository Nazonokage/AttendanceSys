# Project Implementation Plan: Spring Boot QR Attendance System

## 1. Package Structure
Create the following packages under `src/main/java/qr_attendance_sys/attendanceSys/`:
*   `model` (JPA Entities)
*   `repository` (Spring Data JPA Interfaces)
*   `service` (Business Logic & File Parsing)
*   `controller` (REST & Web Endpoints)
*   `dto` (Data Transfer Objects for API requests/responses)

## 2. Domain Models (Entities)
**Student Entity (`model/Student.java`)**
*   `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;`
*   `@Column(unique = true, nullable = false) String idNum;`
*   `String firstName;`
*   `String lastName;`

**AttendanceRecord Entity (`model/AttendanceRecord.java`)**
*   `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;`
*   `@ManyToOne @JoinColumn(name = "student_id") Student student;`
*   `LocalDate date;`
*   `LocalTime timeIn;`
*   `LocalTime timeOut;`

**SystemConfig Entity (`model/SystemConfig.java`)**
*   `@Id String configKey;` (e.g., "ATTENDANCE_MODE")
*   `String configValue;` (e.g., "TIME_IN_OUT" or "SINGLE_SCAN")

## 3. Repositories
Create interfaces extending `JpaRepository`:
*   `StudentRepository`: Add `Optional<Student> findByIdNum(String idNum);`
*   `AttendanceRecordRepository`: Add `Optional<AttendanceRecord> findByStudentAndDate(Student student, LocalDate date);`
*   `SystemConfigRepository`: Standard CRUD.

## 4. Services
**StudentService (`service/StudentService.java`)**
*   Method to register a single student.
*   Method to process `.txt` file uploads. 
    *   Must support reading lines formatted as `lastname, fname, idnum` or `idnum, lname, fname`.
    *   Skip duplicates (check by `idNum`).

**AttendanceService (`service/AttendanceService.java`)**
*   Method `processScan(String idNum)`:
    1. Look up `Student` by `idNum`. If not found, throw an error.
    2. Check `SystemConfig` for "ATTENDANCE_MODE".
    3. If "SINGLE_SCAN": Create a record with `timeIn` if it doesn't exist for today.
    4. If "TIME_IN_OUT": 
       * If no record for today exists -> Create new record, set `timeIn`.
       * If record exists and `timeOut` is null -> Update record, set `timeOut`.
       * If record exists and both times are set -> Return "Already completed for today".

## 5. Controllers
**ApiController (`controller/ApiController.java`) - `@RestController`**
*   `POST /api/scan`: Accepts JSON `{ "idNum": "12345" }`, calls `AttendanceService.processScan()`.
*   `POST /api/students/upload`: Accepts `MultipartFile`, calls `StudentService` file parser.
*   `POST /api/config`: Updates attendance mode.

**WebController (`controller/WebController.java`) - `@Controller`**
*   `GET /`: Returns `index.html` (Scanner page).
*   `GET /admin`: Returns `admin.html` (Dashboard). Passes all students and attendance records to the model.

## 6. Frontend Requirements (`src/main/resources/templates/`)
**`index.html` (Scanner)**
*   Include `<script src="https://unpkg.com/html5-qrcode"></script>`.
*   Initialize `Html5QrcodeScanner`.
*   On successful scan, make a `fetch()` POST request to `/api/scan`. Display success/error messages cleanly.

**`admin.html` (Dashboard)**
*   Use Bootstrap or basic CSS.
*   Section 1: Toggle button/dropdown to switch between "Single Scan" and "Time In/Out".
*   Section 2: File upload form targeting `/api/students/upload`.
*   Section 3: Manual entry form (search by ID or Name).
*   Section 4: Data table displaying today's attendance records.

## 7. Dockerization
**`Dockerfile` (in root directory)**
*   Use `eclipse-temurin:21-jdk-alpine`.
*   Expose port 8080.
*   Ensure a Docker volume can be mounted to `/app/attendance.db` so the SQLite database persists.

**`docker-compose.yml` (in root directory)**
*   Define the Spring Boot app service.
*   Mount volume: `./data:/app/data` to persist the SQLite file locally.