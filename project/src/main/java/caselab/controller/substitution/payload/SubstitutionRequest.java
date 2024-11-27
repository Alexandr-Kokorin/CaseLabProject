package caselab.controller.substitution.payload;

import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record SubstitutionRequest(
    String substitutionUserEmail,
    OffsetDateTime assigned
) {
}
