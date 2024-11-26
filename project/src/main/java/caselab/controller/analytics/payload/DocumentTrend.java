package caselab.controller.analytics.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(name = "Отчёт о подписанных и отклонённых документах по дням")
public record DocumentTrend(
    @JsonProperty("date")
    @Schema(description = "Дата отчёта")
    LocalDate date,
    @JsonProperty("count_signed")
    @Schema(description = "Количество подписанных документов за день", example = "10")
    Long countSigned,
    @JsonProperty("count_refused")
    @Schema(description = "Количество отклонённых документов за день", example = "12")
    Long countRefused
) {
}
