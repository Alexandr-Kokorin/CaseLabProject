package caselab.controller.delegating.payload;

import lombok.Builder;

@Builder
public record SubstitutionRequest(
    String delegatingUserEmail
) {
}
