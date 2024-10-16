package caselab.controller.users.payload;

import java.util.List;
import lombok.Builder;

@Builder
public record UserResponse(
    String email,
    String displayName,
    List<Long> documentIds
) {
}
