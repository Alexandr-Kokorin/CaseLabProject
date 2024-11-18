package caselab.controller.analytics.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(name = "Отчёт о среднем времени обработки типов документов")
public record DocumentTypesReport(
    @JsonProperty("name")
    @Schema(name = "Название типа документа", example = "Кадровый")
    String name,
    @JsonProperty("avg_time")
    @Schema(name = "Среднее время обработки типа документа", example = "120")
    Long avgTime
) {
}
