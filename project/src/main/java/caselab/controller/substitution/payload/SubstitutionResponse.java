package caselab.controller.substitution.payload;

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
