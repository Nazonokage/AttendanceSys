package qr_attendance_sys.attendanceSys.service;

import com.google.zxing.*;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Map;

@Service
public class QrCodeService {
    public byte[] png(String studentId) throws IOException {
        try {
            var matrix = new QRCodeWriter().encode(studentId, BarcodeFormat.QR_CODE, 720, 720,
                Map.of(EncodeHintType.CHARACTER_SET, "UTF-8", EncodeHintType.MARGIN, 4));
            var output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException e) {
            throw new IllegalArgumentException("Student ID is too long to encode as a QR code.", e);
        }
    }
}
