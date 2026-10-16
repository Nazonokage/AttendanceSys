package qr_attendance_sys.attendanceSys.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import qr_attendance_sys.attendanceSys.model.*;
import java.util.*;
import java.time.LocalDate;
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {  }