package caselab.controller.users.payload;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
    @Size(max = 50, message = "{user.update.request.display_name.invalid_size}")
    String displayName,

    @Size(min = 6, max = 100, message = "{user.update.request.password.invalid_size}")
    String password
) {
}
