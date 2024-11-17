package caselab.controller.billing.tariff.payload;

import lombok.Builder;

@Builder
public record UpdateTariffRequest(
    String name,
    String tariffDetails,
    Double price,
    Integer userCount
) {
}
