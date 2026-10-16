package qr_attendance_sys.attendanceSys.model;
import jakarta.persistence.*;
@Entity @Table(name="students")   
public class Student {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String idNum;
 private String yearLevel;
 @Column(nullable=false) private String firstName;
 @Column(nullable=false) private String lastName;

    public Student() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIdNum() { return idNum; }
    public void setIdNum(String idNum) { this.idNum = idNum; }
    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}
