package caselab.service.notification.email;

import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendNotification_ShouldCreateAndSendEmail() throws Exception {
        EmailNotificationDetails emailDetails = EmailNotificationDetails.builder()
                .sender("sender@example.com")
                .recipient("recipient@example.com")
                .subject("Test subject")
                .text("Test email body")
                .attachments(Arrays.asList(new File("attachment1.txt"), new File("attachment2.txt")))
                .build();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendNotification(emailDetails);

        verify(javaMailSender).createMimeMessage();
        verify(javaMailSender).send(mimeMessage);
    }
}
