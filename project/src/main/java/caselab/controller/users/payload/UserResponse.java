package caselab.controller.users.payload;

import caselab.controller.document.payload.document.dto.DocumentResponse;
import java.util.List;
import lombok.Builder;

@Builder
public record UserResponse(
    String email,
    String displayName,
    List<DocumentResponse> documents
) {
}
