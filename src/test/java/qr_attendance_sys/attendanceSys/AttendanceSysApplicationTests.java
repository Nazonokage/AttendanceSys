package qr_attendance_sys.attendanceSys;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import qr_attendance_sys.attendanceSys.service.*;
import qr_attendance_sys.attendanceSys.repository.*;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:sqlite::memory:","spring.jpa.hibernate.ddl-auto=create-drop"})
class AttendanceSysApplicationTests {
 @Autowired StudentService students;
 @Autowired AttendanceService attendance;
 @Autowired StudentRepository studentRepository;
 @Autowired AttendanceRecordRepository records;
 @Autowired SystemConfigRepository configs;
 @Autowired org.thymeleaf.TemplateEngine templates;
 @Autowired qr_attendance_sys.attendanceSys.controller.WebController web;
 @Test void rendersScannerAndDashboardWithAttendance() {
  students.register("001","Ana","Cruz");attendance.processScan("001");
  var model=new org.springframework.ui.ExtendedModelMap();web.admin("",model);
  var context=new org.thymeleaf.context.Context();context.setVariables(model.asMap());
  assertThat(templates.process("admin",context)).contains("Ana","001","Time in");
  assertThat(templates.process("index",new org.thymeleaf.context.Context())).contains("Scan QR code");
 }
 @Autowired qr_attendance_sys.attendanceSys.service.QrCodeService qr;
 @Autowired qr_attendance_sys.attendanceSys.service.QrPdfService qrPdf;
 @Autowired qr_attendance_sys.attendanceSys.controller.QrController qrController;
 @Test void yearLevelsAndFilteredExport() throws Exception {
  students.register("OLD", "Existing", "Student");
  var existing = studentRepository.findByIdNum("OLD").orElseThrow();
  assertThat(existing.getYearLevel()).isNull();
  students.assignYearLevel(existing.getId(), "2");
  assertThat(studentRepository.findByIdNum("OLD").orElseThrow().getYearLevel()).isEqualTo("2");
  var upload = new MockMultipartFile("file", "list.txt", "text/plain", "NEW,Cruz,Ana".getBytes());
  students.upload(upload, "ID_FIRST", "1");
  assertThat(studentRepository.findByIdNum("NEW").orElseThrow().getYearLevel()).isEqualTo("1");
  assertThat(studentRepository.findByYearLevelOrderByLastNameAscFirstNameAscIdNumAsc("1")).extracting(s -> s.getIdNum()).containsExactly("NEW");
  assertThatThrownBy(() -> students.assignYearLevel(existing.getId(), "7")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(() -> qrController.export("6")).isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
  var response = qrController.export("1");
  assertThat(response.getHeaders().getFirst("Content-Disposition")).contains("year-1");
  try (var document = org.apache.pdfbox.Loader.loadPDF(response.getBody())) {
   assertThat(document.getNumberOfPages()).isEqualTo(1);
  }
 }
 @Test void qrImagesAndMultiPagePdfAreReadable() throws Exception {
  for(int i=0;i<7;i++) students.register("00"+i, i==0 ? "Ana Maria With A Longer First Name" : "Juan", "Dela Peña", "1");
  var image = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(qr.png("000")));
  assertThat(decodeQr(image)).isEqualTo("000");
  byte[] pdf = qrPdf.generate("1",studentRepository.findByYearLevelOrderByLastNameAscFirstNameAscIdNumAsc("1"));
  java.nio.file.Files.write(java.nio.file.Path.of("target/qr-preview.pdf"),pdf);
  try (var document = org.apache.pdfbox.Loader.loadPDF(pdf)) {
   assertThat(document.getNumberOfPages()).isEqualTo(2);
   var renderer = new org.apache.pdfbox.rendering.PDFRenderer(document);
   var firstPage = renderer.renderImageWithDPI(0,144);
   javax.imageio.ImageIO.write(firstPage,"png",new java.io.File("target/qr-preview.png"));
   assertThat(decodeQr(firstPage.getSubimage(150,165,345,345))).isEqualTo("000");
   var lastPage = renderer.renderImageWithDPI(1,144);
   javax.imageio.ImageIO.write(lastPage,"png",new java.io.File("target/qr-preview-last.png"));
   assertThat(decodeQr(lastPage.getSubimage(150,165,345,345))).isEqualTo("006");
  }
 }
 private String decodeQr(java.awt.image.BufferedImage image) throws Exception {
  var bitmap = new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(new com.google.zxing.client.j2se.BufferedImageLuminanceSource(image)));
  return new com.google.zxing.MultiFormatReader().decode(bitmap).getText();
 }
 @BeforeEach void reset(){records.deleteAll();studentRepository.deleteAll();configs.deleteAll();}
 @Test void firstScanDoesNotAlsoRecordTimeOut() {
  students.register("001","Ana","Cruz");
  assertThat(attendance.processScan("001")).contains("Time in");
  var record=records.findAll().getFirst();
  assertThat(record.getTimeOut()).isNull();
  attendance.processScan("001");
  assertThat(records.findAll().getFirst().getTimeOut()).isNull();
  record.setTimeIn(LocalTime.now(java.time.ZoneId.of("Asia/Manila")).minusMinutes(2));records.save(record);
  assertThat(attendance.processScan("001")).contains("Time out");
  assertThat(attendance.processScan("001")).contains("Already");
  assertThat(records.count()).isEqualTo(1);
 }
 @Test void singleScanAndUnknownStudent() {
  students.register("001","Ana","Cruz");attendance.setMode("SINGLE_SCAN");
  attendance.processScan("001");attendance.processScan("001");
  assertThat(records.count()).isEqualTo(1);assertThat(records.findAll().getFirst().getTimeOut()).isNull();
  assertThatThrownBy(()->attendance.processScan("unknown")).isInstanceOf(IllegalArgumentException.class);
  assertThatThrownBy(()->attendance.setMode("invalid")).isInstanceOf(IllegalArgumentException.class);
 }
 @Test void importsBothOrdersAndReportsBadLines() throws Exception {
  var first=new MockMultipartFile("file","students.txt","text/plain","001,Cruz,Ana\n001,Cruz,Ana\ninvalid\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
  assertThat(students.upload(first,"ID_FIRST")).isEqualTo(new StudentService.UploadResult(1,1,1));
  var last=new MockMultipartFile("file","other.txt","text/plain","Santos,Juan,002".getBytes(java.nio.charset.StandardCharsets.UTF_8));
  assertThat(students.upload(last,"ID_LAST").imported()).isEqualTo(1);
  assertThat(studentRepository.findByIdNum("002").orElseThrow().getFirstName()).isEqualTo("Juan");
 }
}

