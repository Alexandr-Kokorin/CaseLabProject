package caselab.controller.substitution.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
@Schema(description = "Запрос на создание замещения пользователя")
public record SubstitutionRequest(
    @Schema(description = "Адрес электронной почты замещающего", example = "user@example.com")
    @Pattern(message = "{user.email.invalid}",
             regexp = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @NotBlank(message = "{user.mail.is_blank}")
    @JsonProperty("substitution_user_email")
    String substitutionUserEmail,
    @Schema(description = "Дата, до которой будет длиться замещение", example = "2024-11-27T18:24:18.8302576+03:00")
    @NotNull(message = "{substitution.assigned.is_empty}")
    OffsetDateTime assigned
) {
}
