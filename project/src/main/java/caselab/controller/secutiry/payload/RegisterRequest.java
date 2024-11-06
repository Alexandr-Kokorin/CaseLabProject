package caselab.controller.secutiry.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на регистрацию, содержащий информацию о новом пользователе")
public record RegisterRequest(
    @Schema(description = "Адрес электронной почты пользователя", example = "user@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotBlank(message = "{user.mail.is_blank}")
    String email,

    @Schema(description = "Отображаемое имя пользователя", example = "Иван Иванов")
    @NotBlank(message = "{user.display_name.is_blank}")
    @Size(max = 50, message = "{user.update.request.display_name.invalid_size}")
    @JsonProperty("display_name")
    String displayName,

    @Schema(description = "Пароль пользователя", example = "password123")
    @Size(min = 8, max = 32, message = "{user.update.request.password.invalid_size}")
    @NotBlank(message = "{user.password.is_blank}")
    String password
) {
}
