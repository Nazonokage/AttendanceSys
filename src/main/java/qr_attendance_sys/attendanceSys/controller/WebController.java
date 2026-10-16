package qr_attendance_sys.attendanceSys.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import qr_attendance_sys.attendanceSys.service.AttendanceService;
import qr_attendance_sys.attendanceSys.repository.*;
@Controller 
public class WebController {
    public WebController(StudentRepository students, AttendanceRecordRepository records, AttendanceService attendance) {
        this.students = students;
        this.records = records;
        this.attendance = attendance;
    }

 private final StudentRepository students;
 private final AttendanceRecordRepository records;
 private final AttendanceService attendance;
 @GetMapping("/") public String scanner() {return "index";}
 @GetMapping("/admin") public String admin(@RequestParam(defaultValue="") String q,Model model) {
  model.addAttribute("students",students.findByIdNumContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(q,q,q));
  model.addAttribute("records",records.findByDateOrderByTimeInDesc(attendance.today()));
  model.addAttribute("mode",attendance.mode());model.addAttribute("today",attendance.today());model.addAttribute("q",q);return "admin";
 }
}
