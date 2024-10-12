package caselab.controller.secutiry.payload;

import lombok.Builder;

@Builder
public record AuthenticationRequest(
    String email,
    String password
) {
}
