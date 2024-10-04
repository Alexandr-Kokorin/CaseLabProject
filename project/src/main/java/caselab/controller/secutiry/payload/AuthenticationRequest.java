package caselab.controller.secutiry.payload;

import lombok.Builder;

@Builder
public record AuthenticationRequest(
    String login,
    String password
) {
}
