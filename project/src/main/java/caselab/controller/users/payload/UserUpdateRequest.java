package caselab.controller.users.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserUpdateRequest(
    @NotBlank(message = "{user.display_name.is_blank}")
    @Size(max = 50, message = "{user.update.request.display_name.invalid_size}")
    String displayName,
    @NotBlank
    @Size(min = 8, max = 32, message = "{user.update.request.password.invalid_size}")
    String password
) {
}
