package qr_attendance_sys.attendanceSys.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import qr_attendance_sys.attendanceSys.repository.StudentRepository;
import qr_attendance_sys.attendanceSys.service.*;
import java.io.IOException;

@RestController 
public class QrController {
    public QrController(StudentRepository students, QrCodeService qr, QrPdfService pdf) {
        this.students = students;
        this.qr = qr;
        this.pdf = pdf;
    }

    private final StudentRepository students;
    private final QrCodeService qr;
    private final QrPdfService pdf;

    @GetMapping(value="/api/students/{id}/qr.png", produces=MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> image(@PathVariable Long id, @RequestParam(defaultValue="false") boolean download) throws IOException {
        var student = students.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, (download ? "attachment" : "inline") + "; filename=student-" + id + "-qr.png")
            .body(qr.png(student.getIdNum()));
    }

    @GetMapping(value="/api/students/qr-codes.pdf", produces=MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> export(@RequestParam String yearLevel) throws IOException {
        String year = StudentService.normalizeYearLevel(yearLevel);
        if (year == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a year level.");
        var selected = students.findByYearLevelOrderByLastNameAscFirstNameAscIdNumAsc(year);
        if (selected.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No students in this year level.");
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=year-" + year + "-student-qr-codes.pdf")
            .body(pdf.generate(year, selected));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> invalid(IllegalArgumentException e) {return ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body(e.getMessage());}
}
