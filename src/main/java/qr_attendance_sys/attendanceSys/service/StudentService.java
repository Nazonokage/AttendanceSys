package qr_attendance_sys.attendanceSys.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import qr_attendance_sys.attendanceSys.model.Student;
import qr_attendance_sys.attendanceSys.repository.StudentRepository;
import java.io.*;
import java.nio.charset.StandardCharsets;
@Service 
public class StudentService {
    public StudentService(StudentRepository students) {
        this.students = students;
    }

 private final StudentRepository students;
 @Transactional
 public boolean register(String id, String first, String last) {return register(id,first,last,null);}
 @Transactional
 public boolean register(String id, String first, String last, String yearLevel) {
  yearLevel = normalizeYearLevel(yearLevel);
  if(id==null || first==null || last==null || id.isBlank() || first.isBlank() || last.isBlank()) throw new IllegalArgumentException("ID, first name, and last name are required.");
  if(students.findByIdNum(id.trim()).isPresent()) return false;
  var student=new Student();student.setIdNum(id.trim());student.setYearLevel(yearLevel);student.setFirstName(first.trim());student.setLastName(last.trim());students.save(student);return true;
 }
 public static String normalizeYearLevel(String yearLevel) {
  if(yearLevel==null || yearLevel.isBlank()) return null;
  String value=yearLevel.trim();
  if(!value.matches("[1-6]")) throw new IllegalArgumentException("Year level must be between 1 and 6.");
  return value;
 }
 @Transactional
 public void assignYearLevel(Long id, String yearLevel) {
  var student=students.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND,"Student not found."));
  student.setYearLevel(normalizeYearLevel(yearLevel));students.save(student);
 }
 public record UploadResult(int imported, int duplicates, int invalid) {}
 @Transactional
 public UploadResult upload(MultipartFile file, String format) throws IOException {return upload(file,format,null);}
 @Transactional
 public UploadResult upload(MultipartFile file, String format, String yearLevel) throws IOException {
  yearLevel=normalizeYearLevel(yearLevel);
  if(file.isEmpty()) throw new IllegalArgumentException("Choose a non-empty text file.");
  if(!"ID_FIRST".equals(format) && !"ID_LAST".equals(format)) throw new IllegalArgumentException("Choose a valid column order.");
  int imported=0,duplicates=0,invalid=0;
  try(var reader=new BufferedReader(new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8))) {
   String line;
   while((line=reader.readLine())!=null) {
    line=line.replace("\uFEFF","").trim();if(line.isEmpty()) continue;
    String[] p=line.split(",",-1);
    if(p.length!=3) {invalid++;continue;}
    String id=p["ID_FIRST".equals(format)?0:2], last=p["ID_FIRST".equals(format)?1:0], first=p["ID_FIRST".equals(format)?2:1];
    try {if(register(id,first,last,yearLevel)) imported++;else duplicates++;} catch(IllegalArgumentException e) {invalid++;}
   }
  }
  return new UploadResult(imported,duplicates,invalid);
 }
}
