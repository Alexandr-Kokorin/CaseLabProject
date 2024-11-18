package caselab.controller.analytics.payload;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record ReportDocuments(
    LocalDate date,
    Long created
) {
}
