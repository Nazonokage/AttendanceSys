package qr_attendance_sys.attendanceSys.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import qr_attendance_sys.attendanceSys.model.*;
import qr_attendance_sys.attendanceSys.repository.*;
import java.time.*;
@Service 
public class AttendanceService {
    public AttendanceService(StudentRepository students, AttendanceRecordRepository records, SystemConfigRepository configs) {
        this.students = students;
        this.records = records;
        this.configs = configs;
    }

 private final StudentRepository students;
 private final AttendanceRecordRepository records;
 private final SystemConfigRepository configs;
 private final Clock clock = Clock.system(ZoneId.of("Asia/Manila"));
 public String mode() { return configs.findById("ATTENDANCE_MODE").map(SystemConfig::getConfigValue).orElse("TIME_IN_OUT"); }
 public void setMode(String mode) {
  if (!"SINGLE_SCAN".equals(mode) && !"TIME_IN_OUT".equals(mode)) throw new IllegalArgumentException("Choose a valid attendance mode.");
  configs.save(new SystemConfig("ATTENDANCE_MODE",mode));
 }
 public LocalDate today() { return LocalDate.now(clock); }
 @Transactional
 public synchronized String processScan(String idNum) {
  Student student=students.findByIdNum(idNum.trim()).orElseThrow(()->new IllegalArgumentException("Student ID is not registered."));
  var existing=records.findByStudentAndDate(student,today());
  LocalTime now=LocalTime.now(clock).withNano(0);
  if(existing.isEmpty()) {
   var record=new AttendanceRecord(); record.setStudent(student);record.setDate(today());record.setTimeIn(now);records.save(record);
   return student.getFirstName()+": Time in recorded.";
  }
  var record=existing.get();
  if("SINGLE_SCAN".equals(mode()) || record.getTimeOut()!=null) return "Already recorded for today.";
  if(Duration.between(record.getTimeIn(),now).getSeconds()<30) return "Time in recorded. Wait at least 30 seconds before scanning out.";
  record.setTimeOut(now);records.save(record);return student.getFirstName()+": Time out recorded.";
 }
}
