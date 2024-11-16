package caselab.controller.billing.tariff.payload;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record CreateTariffRequest(
    String name,
    String tariffDetails,
    Double price,
    Integer userCount
) {
}
