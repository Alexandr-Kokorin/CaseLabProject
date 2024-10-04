package caselab.controller.secutiry.payload;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
    String token
) {
}
