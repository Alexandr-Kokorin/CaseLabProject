package caselab.controller.users.payload;

import lombok.Builder;

@Builder
public record UserUpdateRequest(
    String displayName,
    String password
) {
}
