package qr_attendance_sys.attendanceSys.service;

import org.springframework.stereotype.Service;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import qr_attendance_sys.attendanceSys.model.Student;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.imageio.ImageIO;
import java.util.List;

@Service
public class QrPdfService {
    private final QrCodeService qr;

    public QrPdfService(QrCodeService qr) { this.qr = qr; }

    // Raster labels preserve Unicode names and fit long names within the card.
    private byte[] label(Student student) throws IOException {
        var image = new BufferedImage(960, 160, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, 960, 160);
        g.setColor(Color.BLACK);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String[] lines = {student.getLastName() + ", " + student.getFirstName(), "ID: " + student.getIdNum()};
        for (int i = 0; i < lines.length; i++) {
            int size = 36;
            do {g.setFont(new Font(Font.SANS_SERIF, i == 0 ? Font.BOLD : Font.PLAIN, size--));}
            while(g.getFontMetrics().stringWidth(lines[i]) > 920 && size > 1);
            g.drawString(lines[i], (960 - g.getFontMetrics().stringWidth(lines[i])) / 2, 58 + i * 70);
        }
        g.dispose();
        var output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output); return output.toByteArray();
    }

    public byte[] generate(String yearLevel, List<Student> students) throws IOException {
        if (students.isEmpty()) throw new IllegalArgumentException("No students are registered in this year level.");
        try (var document = new PDDocument(); var output = new ByteArrayOutputStream()) {
            int pages = (students.size() + 5) / 6;
            for (int pageIndex = 0; pageIndex < pages; pageIndex++) {
                var page = new PDPage(PDRectangle.A4); document.addPage(page);
                try (var content = new PDPageContentStream(document, page)) {
                    text(document, content, 18, 36, 800, "Student QR cards / Year " + yearLevel);
                    text(document, content, 10, 36, 780, "Cut out and distribute. Scan the QR code to record attendance.");
                    for (int slot = 0; slot < 6; slot++) {
                        int index = pageIndex * 6 + slot;
                        if (index >= students.size()) break;
                        var student = students.get(index);
                        float x = 36 + (slot % 2) * 266;
                        float y = 530 - (slot / 2) * 244;
                        content.setStrokingColor(Color.LIGHT_GRAY); content.addRect(x, y, 252, 232); content.stroke();
                        var code = PDImageXObject.createFromByteArray(document, qr.png(student.getIdNum()), "QR");
                        content.drawImage(code, x + 40, y + 55, 172, 172);
                        content.drawImage(PDImageXObject.createFromByteArray(document, label(student), "Student"), x + 6, y + 9, 240, 40);
                    }
                    text(document, content, 10, 36, 30, "Year " + yearLevel + " | " + students.size() + " students | Page " + (pageIndex + 1) + " of " + pages);
                }
            }
            document.save(output); return output.toByteArray();
        }
    }

    private void text(PDDocument document, PDPageContentStream content, int size, float x, float y, String text) throws IOException {
        var font = new Font(Font.SANS_SERIF, Font.PLAIN, size * 3);
        var measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB).createGraphics();
        measure.setFont(font);
        int width = measure.getFontMetrics().stringWidth(text) + 4;
        int height = measure.getFontMetrics().getHeight() + 4;
        int ascent = measure.getFontMetrics().getAscent();
        measure.dispose();
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK); g.setFont(font);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawString(text, 2, ascent); g.dispose();
        var output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output);
        content.drawImage(PDImageXObject.createFromByteArray(document, output.toByteArray(), "Label"), x, y - (height - ascent) / 3f, width / 3f, height / 3f);
    }
}
