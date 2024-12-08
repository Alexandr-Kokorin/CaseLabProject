package caselab.controller.organization.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record CreateOrganizationRequest(

    @Schema(description = "Имя организации", example = "Company name")
    @Size(min = 2, max = 100, message = "{organization.name.invalid_size}")
    String name,

    @Schema(description = "ИНН организации, состоящий из 10 цифр", example = "1234567890")
    @NotBlank(message = "{organization.inn.is_blank}")
    @Pattern(message = "{organization.inn.invalid_format}", regexp = "\\d{10}")
    String inn,

    @Schema(description = "Адрес электронной почты админа организации", example = "admin@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotBlank(message = "{user.mail.is_blank}")
    String email,

    @Schema(description = "Отображаемое имя админа организации", example = "Иван Иванов")
    @NotBlank(message = "{user.display_name.is_blank}")
    @Size(max = 50, message = "{user.update.request.display_name.invalid_size}")
    @JsonProperty("display_name")
    String displayName,

    @Schema(description = "Пароль админа организации", example = "password123")
    @Size(min = 8, max = 32, message = "{user.update.request.password.invalid_size}")
    @NotBlank(message = "{user.password.is_blank}")
    String password
) {
}
