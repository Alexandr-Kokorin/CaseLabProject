package caselab.controller.users.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на обновление данных о пользователе")
public record UserUpdateRequest(
    @JsonProperty("display_name")
    @NotBlank(message = "{user.display_name.is_blank}")
    @Size(max = 50, message = "{user.update.request.display_name.invalid_size}")
    @Schema(description = "Отображаемое имя пользователя", example = "Иван Иванов")
    String displayName,
    @JsonProperty("password")
    @NotBlank
    @Size(min = 8, max = 32, message = "{user.update.request.password.invalid_size}")
    @Schema(description = "Пароль пользователя", example = "password123")
    String password
) {
}
