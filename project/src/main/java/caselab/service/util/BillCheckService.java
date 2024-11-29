package caselab.service.util;

import caselab.domain.entity.Bill;
import caselab.domain.entity.Organization;
import caselab.domain.repository.BillRepository;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillCheckService {

    private final BillRepository billRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 0 * * ?") // Проверка запускается каждый день в полночь
    public void checkAllBills() {
        LocalDateTime now = LocalDateTime.now();

        List<Bill> expiredBills = billRepository.findAllByPaidUntilBefore(now);
        for (Bill bill : expiredBills) {
            Organization organization = bill.getUser().getOrganization();
            if (organization != null && organization.isActive()) {
                organization.setActive(false);
                log.info("Организация '{}' (ID: {}) деактивирована из-за истечения срока оплаты счета (ID: {}).",
                    organization.getName(), organization.getId(), bill.getId());

                try {
                    // Генерация и отправка чека
                    File qrCodeFile = generateQrCodeForBill(bill);
                    sendBillNotification(bill, qrCodeFile);
                } catch (Exception e) {
                    log.error("Ошибка при отправке чека для счета (ID: {}): {}", bill.getId(), e.getMessage());
                }
            }
        }
    }
    public File generateQrCodeForBill(Bill bill) throws Exception {
        String qrContent = String.format(
            "Bill ID: %d\nOrganization: %s\nAmount: %s\nDue Date: %s",
            bill.getId(),
            bill.getUser().getOrganization().getName(),
            bill.getTariff().getPrice(), // Предполагается, что у тарифа есть цена
            bill.getPaidUntil()
        );

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 300, 300, hints);
        BufferedImage qrImage = toBufferedImage(bitMatrix);

        File qrFile = new File("qr_code_" + bill.getId() + ".png");
        ImageIO.write(qrImage, "PNG", qrFile);

        return qrFile;
    }

    private BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
            }
        }
        return image;
    }

    private void sendBillNotification(Bill bill, File qrCodeFile) {
        EmailNotificationDetails emailDetails = EmailNotificationDetails.builder()
            .recipient(bill.getUser().getEmail())
            .sender("admin@solifex.ru")
            .subject("Оплата счета")
            .text(String.format("Ваш счет (ID: %d) не был оплачен. Проверьте вложенный чек для подробностей.", bill.getId()))
            .attachments(List.of(qrCodeFile))
            .build();

        emailService.sendNotification(emailDetails);
        log.info("Чек для счета (ID: {}) отправлен на почту: {}", bill.getId(), bill.getUser().getEmail());
    }
}


