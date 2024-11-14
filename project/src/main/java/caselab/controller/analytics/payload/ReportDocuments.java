package caselab.controller.analytics.payload;

import lombok.Builder;

@Builder
public record ReportDocuments(
    Long created,
    Long sentOnSigning,
    Long signed,
    Long notSigned
) {
}
