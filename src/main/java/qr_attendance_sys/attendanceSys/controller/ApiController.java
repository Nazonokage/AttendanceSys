package qr_attendance_sys.attendanceSys.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import qr_attendance_sys.attendanceSys.service.*;
import java.io.IOException;
import java.util.Map;
@RestController @RequestMapping("/api") 
public class ApiController {
    public ApiController(AttendanceService attendance, StudentService students) {
        this.attendance = attendance;
        this.students = students;
    }

 private final AttendanceService attendance;
 private final StudentService students;
 public record ScanRequest(String idNum) {}
 public record StudentRequest(String idNum,String firstName,String lastName,String yearLevel) {}
 @PostMapping("/scan") public Map<String,String> scan(@RequestBody ScanRequest request) {
  if(request.idNum()==null || request.idNum().isBlank()) throw new IllegalArgumentException("Student ID is required.");
  return Map.of("message",attendance.processScan(request.idNum()));
 }
 @PostMapping("/students") public Map<String,String> register(@RequestBody StudentRequest request) {
  return Map.of("message",students.register(request.idNum(),request.firstName(),request.lastName(),request.yearLevel())?"Student registered.":"Student ID already registered.");
 }
 @PostMapping("/students/upload") public StudentService.UploadResult upload(@RequestParam MultipartFile file,@RequestParam(defaultValue="ID_FIRST") String format,@RequestParam(required=false) String yearLevel) throws IOException {return students.upload(file,format,yearLevel);}
 @PostMapping("/students/{id}/year-level") public Map<String,String> yearLevel(@PathVariable Long id,@RequestParam(required=false) String yearLevel) {students.assignYearLevel(id,yearLevel);return Map.of("message","Year level updated.");}
 @PostMapping("/config") public Map<String,String> config(@RequestParam String mode) {attendance.setMode(mode);return Map.of("message","Attendance mode updated.");}
 @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<?> invalid(IllegalArgumentException error) {return ResponseEntity.badRequest().body(Map.of("message",error.getMessage()));}
 @ExceptionHandler(IOException.class) public ResponseEntity<?> uploadError() {return ResponseEntity.badRequest().body(Map.of("message","Unable to read uploaded file."));}
}
