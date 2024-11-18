package caselab.controller.analytics.payload;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record DocumentTrend(
    LocalDate date,
    Long countSigned,
    Long countRefused
) {
}
