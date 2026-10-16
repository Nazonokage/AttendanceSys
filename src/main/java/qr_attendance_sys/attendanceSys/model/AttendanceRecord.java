package qr_attendance_sys.attendanceSys.model;
import jakarta.persistence.*;
import java.time.*;
@Entity @Table(name="attendance_records",uniqueConstraints=@UniqueConstraint(columnNames={"student_id","date"}))   
public class AttendanceRecord {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="student_id") private Student student;
 @Column(nullable=false) private LocalDate date;
 @Column(nullable=false) private LocalTime timeIn;
 private LocalTime timeOut;

    public AttendanceRecord() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }
}
