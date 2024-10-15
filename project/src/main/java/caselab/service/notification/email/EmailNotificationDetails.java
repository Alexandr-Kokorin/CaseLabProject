package caselab.service.notification.email;

import caselab.service.notification.NotificationDetails;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.File;
import java.util.List;
import lombok.Builder;

@Builder
public record EmailNotificationDetails(
    @NotBlank(message = "Sender cannot be blank")
    @Email(message = "Sender must be a valid email address")
    String sender,

    @NotBlank(message = "Recipient cannot be blank")
    @Email(message = "Recipient must be a valid email address")
    String recipient,

    @NotBlank(message = "Subject cannot be blank")
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    String subject,

    @NotBlank(message = "Text cannot be blank")
    String text,

    List<File> attachments
) implements NotificationDetails {
}
