package caselab.controller.billing.bill.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
@Schema(description = "Запрос, содержащий айди тарифа")
public record UpdateBillRequest(
    @Positive(message = "Tariff id must be a positive number")
    @NotNull(message = "Tariff id must not be null")
    @JsonProperty("tariff_id")
    @Schema(description = "Айди тарифа на котрый будет заменен предыдущий", example = "2")
    Long tariffId
) {
}
