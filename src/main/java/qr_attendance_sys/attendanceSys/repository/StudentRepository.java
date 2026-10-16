package qr_attendance_sys.attendanceSys.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import qr_attendance_sys.attendanceSys.model.*;
import java.util.*;
import java.time.LocalDate;
public interface StudentRepository extends JpaRepository<Student, Long> { List<Student> findByYearLevelOrderByLastNameAscFirstNameAscIdNumAsc(String yearLevel); Optional<Student> findByIdNum(String idNum); List<Student> findByIdNumContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String id, String first, String last); }