# QR Attendance System

A Spring Boot application for registering students, distributing QR cards, and recording daily attendance. Uses Java 21, Thymeleaf, and a local SQLite database.

## Features

- Register students individually or import a text file.
- Search the student directory by ID or name; assign Years 1-6.
- Record attendance using a camera QR scanner or manual ID entry.
- Choose Single Scan or Time In/Out attendance mode.
- Open a student's QR code full screen from their directory row, or download a PNG.
- Download printable QR cards for a whole year level, six per A4 page.
- Store attendance dates in the Asia/Manila timezone.

## 1. Requirements

For a normal installation:

- **JDK 21**, including the `java` and `javac` commands.
- **Git** to clone the repository (or download and extract the source ZIP).
- Internet access for the first Maven build and the camera scanner's CDN library.
- A browser. Camera scanning needs localhost or HTTPS and camera permission.

The Maven wrapper is included; a separate Maven installation is optional. Docker users only need Git and Docker with Compose; the container builds with Java 21 itself.

Check your Java installation:

```text
java -version
javac -version
```

Both should report version 21. Set `JAVA_HOME` to your JDK folder if necessary. In PowerShell, for the current terminal only:

```powershell
# Example: replace this with your actual JDK installation directory.
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
java -version
javac -version
```

## 2. Get the project

```text
git clone https://github.com/Nazonokage/AttendanceSys.git
cd AttendanceSys
```

If you already have this project, open a terminal in the directory containing `pom.xml` and `mvnw.cmd`. Do not clone another copy inside it.

## 3. Run on Windows (PowerShell)

First build and run all tests:

```powershell
.\mvnw.cmd clean package
```

Wait for **BUILD SUCCESS**, then start the packaged application:

```powershell
java -jar .\target\attendanceSys-0.0.1-SNAPSHOT.jar
```

Keep that terminal open. Startup is complete when it reports Tomcat on port 8080 and `Started AttendanceSysApplication`.

Open:

- Scanner: <http://localhost:8080/>
- Admin dashboard: <http://localhost:8080/admin>

To stop the application, press **Ctrl+C** in its terminal. For later launches, run the same `java -jar` command from the project directory. Rebuild after changing the source or pulling updates.

For development, you can instead run:

```powershell
.\mvnw.cmd spring-boot:run
```

If Maven is already installed, `mvn clean package` and `mvn spring-boot:run` are alternatives.

## 4. Run on macOS or Linux

From the project directory:

```sh
chmod +x mvnw
./mvnw clean package
java -jar target/attendanceSys-0.0.1-SNAPSHOT.jar
```

Use the same browser URLs above. Press **Ctrl+C** to stop. During development, use `./mvnw spring-boot:run`.

## 5. Run with Docker

Start Docker first, then run from the project directory:

```text
docker compose up --build -d
docker compose logs -f app
```

Open <http://localhost:8080/admin> when the startup message appears. Press Ctrl+C to leave the log view; the container keeps running in the background.

Stop the application:

```text
docker compose down
```

Rebuild after source changes:

```text
docker compose up --build -d
```

SQLite is stored in `./data/attendance.db` through the Compose bind mount. Stopping or rebuilding the container retains that directory. Local Java runs use `./attendance.db` instead; these are separate databases by default. Docker includes the fonts needed for PDF labels.

To change Docker's host port, change the left-hand port in `docker-compose.yml`, for example `"8082:8080"`, then reopen the site at port 8082.

## 6. Register or import students

Go to **Admin dashboard**.

### Individual registration

1. Enter the student ID, first name, and last name.
2. Select a year level, or leave it Unassigned.
3. Click **Register student**.

IDs are text: `00123` stays `00123`. A duplicate ID is skipped rather than overwriting the existing student.

### Text import

Create a UTF-8 `.txt` file with one student per line and **no header**. Maximum upload size is 2 MB. Choose the matching column order in the upload form.

**ID, last name, first name:**

```text
00123,Dela Cruz,Ana
00124,Santos,Juan
```

**Last name, first name, ID:**

```text
Dela Cruz,Ana,00123
Santos,Juan,00124
```

Select the year level for the file and click **Import students**. The result reports imported students, duplicates, and invalid lines. Blank lines are ignored. Each nonblank line needs exactly three nonempty fields; names cannot contain commas in this simple format.

The selected year applies only to newly imported students. Duplicate imports do not update names or years. Do not add a fourth year-level column.

### Existing students

Use **Student directory** to search by ID or name. Choose a student's year in their row and click **Save year**. Select Unassigned to clear their year without deleting their attendance. Older students remain Unassigned until updated.

## 7. Give students their QR codes

### Print cards for a year level

1. Assign the students to their year level first.
2. Under **Student directory**, choose the export year.
3. Click **Download year-level QR PDF**.
4. Print the PDF on A4 at actual size, cut out the cards, and distribute them.

The PDF contains **all registered students in that year**, regardless of directory search or today's attendance. Cards are sorted by last name, first name, and ID, with six cards per page and automatic pagination. If the year has no students, the dashboard shows a message instead of downloading an empty PDF.

### Show or save one student's code

1. Find the student in the directory.
2. Click **Full-screen QR** on their row.
3. Take a photo or screenshot, or click **Download PNG** to save the code directly.
4. Click **Close** or press **Escape** to return to the directory. Some browsers first exit fullscreen before closing the dialog.

Browsers that support fullscreen enter it after the button click. Otherwise, the QR dialog fills the browser window. The view displays the student's name, ID, and year alongside the code.

QR generation and PDF export run locally without a third-party QR service. QR codes contain only the exact student ID. Anyone holding a copy can present it as that student; the app does not verify the identity of the person scanning.

## 8. Record attendance

Choose the attendance mode in the admin dashboard and click **Save mode**:

| Mode | First scan today | Later scans |
| --- | --- | --- |
| Time In/Out | Records time in | Records time out after at least 30 seconds; completed records stay unchanged |
| Single Scan | Records time in | Reports already recorded; no time out |

Open the scanner page, enable the camera, and present the student's QR code. The browser needs camera permission. The scanner suppresses repeated callbacks for the same code for 30 seconds; the server also prevents an immediate time out.

For a camera-free workflow, enter the student ID on the scanner page or use **Manual attendance** in the dashboard. Unregistered IDs are rejected. Refresh the dashboard to see attendance changes made from another tab or device. Switching modes retains existing records.

The camera library loads from a CDN, so the scanner needs internet access. Registration, manual attendance, QR generation, and PDF export work without that library after the application has been built.

A phone visiting `http://<computer-ip>:8080` may not be allowed to use its camera because that address is not localhost or HTTPS. Use HTTPS for remote camera scanning. There is currently no administrator login; keep the app on a trusted local network and add authentication before public deployment.

## 9. Configuration and database storage

| Setting | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port for local Java runs |
| `ATTENDANCE_DB_URL` | `jdbc:sqlite:attendance.db` | SQLite JDBC URL |
| Attendance timezone | `Asia/Manila` | Defined in `AttendanceService` |

Example PowerShell session using another port and an absolute database path:

```powershell
# The database's parent directory must already exist.
$env:PORT = '8082'
$env:ATTENDANCE_DB_URL = 'jdbc:sqlite:D:/attendance-data/attendance.db'
java -jar .\target\attendanceSys-0.0.1-SNAPSHOT.jar
```

On macOS/Linux:

```sh
PORT=8082 ATTENDANCE_DB_URL=jdbc:sqlite:/absolute/existing/folder/attendance.db \
  java -jar target/attendanceSys-0.0.1-SNAPSHOT.jar
```

Environment variables set this way affect only the current terminal and its child processes. Docker supplies its own database URL inside the container.

The default local database path is relative to the working directory. Launch from the same project directory each time, or configure an absolute path. Otherwise, it can look as though your records disappeared because a different database was opened.

### Backups and upgrades

1. Stop the application with Ctrl+C, or `docker compose down`.
2. Copy `attendance.db` (local Java) or the whole `data` directory (Docker) to a backup location.
3. Pull source updates with `git pull`.
4. Rebuild using `./mvnw clean package` / `.\mvnw.cmd clean package`, or rebuild Docker.
5. Restart using the same database path.

Hibernate updates the database schema on startup, including the optional year-level column. Student and attendance data are excluded from Git and should not be pushed to GitHub. To restore, stop the app and replace the database with your backup before starting again.

## 10. Troubleshooting

### `ClassFormatError: Invalid method Code length 0` or generated constructor errors

This error was observed when an editor generated malformed Lombok constructor bytecode. The project now uses explicit Java constructors and accessors and no longer requires Lombok.

Stop the app and run a clean build from the project directory:

```powershell
.\mvnw.cmd clean package
java -jar .\target\attendanceSys-0.0.1-SNAPSHOT.jar
```

Run the packaged JAR after BUILD SUCCESS rather than launching an old editor run configuration against stale `target/classes` output. In VS Code, refresh/reimport the Maven project; if diagnostics persist, use **Java: Clean Java Language Server Workspace** from the Command Palette and reload the workspace.

### `JAVA_HOME` is missing, Java is not found, or invalid target release

Install JDK 21, verify both `java -version` and `javac -version`, and set `JAVA_HOME` as shown above. Reopen the terminal after changing permanent environment settings.

### Port 8080 is already in use

Stop your previous app instance, or set `PORT` to another number before starting. For Docker, change the host-port mapping instead. Use the new port in the browser URL.

### Maven cannot download dependencies

The first build requires access to Maven Central. Check the network/proxy settings and rerun without offline mode (`-o`). If the wrapper is blocked but Maven is installed, try `mvn clean package`. Do not copy compiled classes from another build into `target`.

### Clean build cannot delete a file / SQLite database is locked

Stop the app and old test runs first, then retry the clean build. Avoid running multiple app processes against the same SQLite file. Tests use an isolated in-memory database.

### Camera or QR scanner will not load

Allow camera access, use localhost or HTTPS, and check internet access to the scanner CDN. Close other apps using the camera. Manual ID entry remains available.

### Empty year-level PDF

Assign the students to the intended year and save their rows first. Unassigned students are excluded from year exports. A duplicate import does not assign a year to an existing student.

### Records seem missing after restarting

Check the working directory and `ATTENDANCE_DB_URL`. Local Java and Docker use different default database locations. Do not delete either database while investigating.

## 11. Tests and project layout

```powershell
# Windows
.\mvnw.cmd test
```

```sh
# macOS/Linux
./mvnw test
```

Integration tests cover attendance rules, text import, year assignment/filtering, template rendering, QR decoding, and multipage PDF generation. PDF previews generated by tests live under the ignored `target` directory.

- `src/main/java/qr_attendance_sys/attendanceSys/model`: persisted students, attendance, settings.
- `repository`: Spring Data database queries.
- `service`: registration/import, attendance, QR, and PDF logic.
- `controller`: pages and HTTP endpoints.
- `src/main/resources/templates`: scanner and dashboard.
- `src/main/resources/static`: page styles and browser behavior.
- `src/main/resources/application.properties`: default configuration.

## 12. API reference

| Method | Path | Input / result |
| --- | --- | --- |
| POST | `/api/students` | JSON: `idNum`, `firstName`, `lastName`, optional `yearLevel` |
| POST | `/api/students/upload` | Multipart: `file`, `format` (`ID_FIRST` or `ID_LAST`), optional `yearLevel` |
| POST | `/api/scan` | JSON: `idNum` |
| POST | `/api/config` | Form field `mode`: `TIME_IN_OUT` or `SINGLE_SCAN` |
| POST | `/api/students/{databaseId}/year-level` | Form field `yearLevel`: 1-6, or empty to clear |
| GET | `/api/students/{databaseId}/qr.png` | PNG image; add `?download=true` for download |
| GET | `/api/students/qr-codes.pdf?yearLevel=1` | All Year 1 QR cards; empty year returns 404, unsupported year returns 400 |

`databaseId` is the numeric database key used by directory buttons, not the student's printed ID. QR payloads use the printed student ID.

See [CHANGELOG.md](CHANGELOG.md) for change history.
