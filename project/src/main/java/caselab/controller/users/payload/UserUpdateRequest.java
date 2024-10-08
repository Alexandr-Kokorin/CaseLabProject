package caselab.controller.users.payload;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
    @Size(max = 50, message = "Display name must not exceed 50 characters")
    String displayName,

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    String password
) {
}
