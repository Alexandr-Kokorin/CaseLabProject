package caselab.controller.substitution.payload;

import lombok.Builder;
import java.time.OffsetDateTime;

@Builder
public record SubstitutionRequest(
    String substitutionUserEmail,
    OffsetDateTime assigned
) {
}
