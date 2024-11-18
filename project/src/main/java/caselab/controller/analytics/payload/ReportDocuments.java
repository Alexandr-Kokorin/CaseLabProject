package caselab.controller.analytics.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(name = "Отчёт о количестве созданных документов за день")
public record ReportDocuments(
    @JsonProperty("date")
    @Schema(name = "Дата отчёта")
    LocalDate date,
    @JsonProperty("created")
    @Schema(name = "Количество созданных документов за день", example = "190")
    Long created
) {
}
