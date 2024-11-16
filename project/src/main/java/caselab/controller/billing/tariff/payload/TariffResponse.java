package caselab.controller.billing.tariff.payload;

import lombok.Builder;

@Builder
public record TariffResponse(
    Long id,
    String name,
    String tariffDetails,
    Double price,
    Integer userCount
) {
}
