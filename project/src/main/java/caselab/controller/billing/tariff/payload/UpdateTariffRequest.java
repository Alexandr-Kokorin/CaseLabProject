package caselab.controller.billing.tariff.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;

@Builder
public record UpdateTariffRequest(
    @Schema(description = "Название тарифа", example = "Тариф №1")
    @NotBlank(message = "tariff.name.is_blank")
    String name,
    @Schema(description = "Описание тарифа", example = "Тариф предназначен для организации от 100 человек")
    @NotBlank(message = "tariff.details.is_blank")
    String tariffDetails,
    @Schema(description = "Цена тарифа", example = "1000")
    @PositiveOrZero(message = "tariff.price.is_negative")
    @NotNull(message = "tariff.price.is_null")
    Double price,
    @Schema(description = "Количество пользователей для тарифа", example = "1000")
    @PositiveOrZero(message = "tariff.user.is_negative")
    @NotNull(message = "tariff.user.is_null")
    Integer userCount
) {
}
