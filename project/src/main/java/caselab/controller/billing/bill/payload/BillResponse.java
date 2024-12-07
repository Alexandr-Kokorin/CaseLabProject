package caselab.controller.billing.bill.payload;

import caselab.controller.billing.tariff.payload.TariffResponse;
import caselab.controller.organization.payload.OrganizationResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDateTime;

@Schema(description = "Ответ, содержащий информацию о биллинге")
@Builder
public record BillResponse(
    @JsonProperty("id")
    @Schema(description = "Id биллинга", example = "1")
    Long id,
    @JsonProperty("tariff")
    @Schema(description = "Тариф", example = "1")
    TariffResponse tariff,
    @JsonProperty("issued_at")
    @Schema(description = "Дата оформления")
    LocalDateTime issuedAt,
    @JsonProperty("organization")
    @Schema(description = "Организация")
    OrganizationResponse organization,
    @JsonProperty("email")
    @Schema(description = "Почта администраора")
    String email
) {
}
