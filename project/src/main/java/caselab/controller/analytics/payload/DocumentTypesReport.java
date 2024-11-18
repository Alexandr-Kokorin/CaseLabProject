package caselab.controller.analytics.payload;

import lombok.Builder;

@Builder
public record DocumentTypesReport(
    String name,
    Long avgTime
) {
}
