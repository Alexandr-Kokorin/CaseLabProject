package caselab.controller.department.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на добавление работника")
public record AddEmployeeRequest(
    @Schema(description = "Адрес электронной почты пользователя", example = "user@example.com")
    @Email(message = "{user.email.invalid}")
    @JsonProperty("user_email")
    String userEmail,
    @Schema(description = "Должность пользователя", example = "Программист")
    @NotBlank(message = "{position.not.blank}")
    @JsonProperty("position")
    String position
) {
}
