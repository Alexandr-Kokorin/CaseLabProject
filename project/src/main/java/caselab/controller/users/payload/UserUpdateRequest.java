package caselab.controller.users.payload;

import lombok.Builder;

@Builder
public record UserUpdateRequest(
    String login,
    String displayName,
    String password
) {
}
