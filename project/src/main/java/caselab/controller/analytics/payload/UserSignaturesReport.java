package caselab.controller.analytics.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Отчёт о среднем времени обработки документов пользователем")
public record UserSignaturesReport(
    @JsonProperty("email")
    @Schema(name = "Адрес электронной почты пользователя", example = "user@example.com")
    String email,
    @JsonProperty("avg_time_for_signing")
    @Schema(name = "Среднее время обработки документов", example = "140")
    Long avgTimeForSigning
) {
}
