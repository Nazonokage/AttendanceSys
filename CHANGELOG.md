# Changelog

## 2026-10-16 - Startup repair and setup guide

- Replaced Lombok-generated constructors and accessors with explicit Java code after the editor produced an invalid AttendanceRecord constructor.
- Removed the Lombok dependency and annotation-processor configuration.
- Added a full README covering setup, Maven and Docker runs, QR distribution, configuration, backups, and startup troubleshooting.

## 2026-10-16 - Student QR distribution

### Added
- Optional year level (Years 1-6) for student registration and imports.
- Per-student year assignment in the student directory, including an Unassigned option for existing students.
- A Full-screen QR button on every directory row. Shows the student's name, ID, year, and QR code in a browser-filling dialog; requests browser fullscreen when supported.
- Close button and Escape support, plus an individual PNG download.
- Year-level PDF export from the directory: six labeled QR cards per A4 page, sorted by last name, first name, and ID, with automatic pagination.
- Empty-year feedback, QR decoding checks, PDF pagination checks, and year-level validation tests.

### Compatibility
- QR payloads contain the exact student ID, so existing attendance scanning behavior continues to work.
- Existing students and attendance records are retained. Students without a year remain Unassigned until updated.
- Existing three-column text imports remain supported; the selected year applies to newly imported students only. Duplicate imports do not change existing students.
- QR generation and PDF export run locally without an external QR service.
- Docker runtime includes fonts needed to render student labels in PDF cards.

## 2026-10-16 - Initial attendance system
- Spring Boot, SQLite, scanner and admin dashboard.
- Student registration, search, text import, manual attendance, and QR scanning.
- Single Scan and Time In/Out modes, 30-second repeat-scan protection, and Asia/Manila attendance dates.
- Docker setup and integration tests.
