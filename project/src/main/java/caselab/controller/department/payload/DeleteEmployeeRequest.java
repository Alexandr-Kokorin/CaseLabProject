package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на удаление работника")
public record DeleteEmployeeRequest(
    @Schema(description = "Адрес электронной почты пользователя", example = "user@example.com")
    @Email(message = "{user.email.invalid}")
    @JsonProperty("user_email")
    String userEmail
) {
}
