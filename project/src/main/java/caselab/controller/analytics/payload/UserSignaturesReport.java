package caselab.controller.analytics.payload;

import lombok.Builder;

@Builder
public record UserSignaturesReport(
    String email,
    Long avgTimeForSigning
) {
}
