package caselab.service.notification.email;

import caselab.exception.NotificationException;
import caselab.service.notification.NotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificationService<EmailNotificationDetails> {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendNotification(EmailNotificationDetails emailDetails) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            createMimeMessageHelper(message, emailDetails);
        } catch (MessagingException e) {
            throw new NotificationException();
        }

        javaMailSender.send(message);
        log.info("Email sent successfully to {}", emailDetails.recipient());
    }

    private void createMimeMessageHelper(MimeMessage message, EmailNotificationDetails emailDetails)
        throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(emailDetails.sender());
        helper.setTo(emailDetails.recipient());
        helper.setSubject(emailDetails.subject());
        helper.setText(emailDetails.text(), emailDetails.text());

        if (emailDetails.attachments() != null) {
            for (File attachment : emailDetails.attachments()) {
                helper.addAttachment(attachment.getName(), attachment);
            }
        }
    }
}
