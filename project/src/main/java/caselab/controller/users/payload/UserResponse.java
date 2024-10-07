package caselab.controller.users.payload;

import caselab.controller.document.payload.DocumentResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String login,
    String displayName,
    List<DocumentResponse> documents
) {
}
