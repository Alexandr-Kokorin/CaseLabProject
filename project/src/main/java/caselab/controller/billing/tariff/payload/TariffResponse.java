package caselab.controller.billing.tariff.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record TariffResponse(
    @Schema(description = "id тарифа", example = "1")
    Long id,

    @Schema(description = "Название тарифа", example = "Тариф №1")
    String name,

    @Schema(description = "Описание тарифа", example = "Тариф предназначен для организации от 100 человек")
    String tariffDetails,

    @Schema(description = "Цена тарифа", example = "1000")
    Double price,

    @Schema(description = "Количество пользователей для тарифа", example = "1000")
    Integer userCount
) {
}
