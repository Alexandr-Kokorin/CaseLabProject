package caselab.controller.delegating.payload;

import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record SubstitutionResponse(
    Long id,
    OffsetDateTime assigned,
    String currentUserEmail,
    String substitutionUserEmail
) {
}
