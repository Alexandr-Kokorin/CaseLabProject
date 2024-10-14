package caselab.controller.secutiry.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Schema(description = "Запрос аутентификации, содержащий учетные данные пользователя")
@Builder
public record AuthenticationRequest(
    @Schema(description = "Адрес электронной почты пользователя", example = "user@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotBlank(message = "{user.email.is_blank}")
    String email,

    @Schema(description = "Пароль пользователя", example = "password123")
    @Size(min = 8, max = 32, message = "{user.update.request.password.invalid_size}")
    @NotBlank(message = "{user.password.is_blank}")
    String password
) {
}
