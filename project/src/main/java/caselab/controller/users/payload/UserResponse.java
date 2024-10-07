package caselab.controller.users.payload;

import lombok.Builder;

@Builder
public record UserResponse(
    Long id,
    String login,
    String displayName
    //List<DocumentResponse> documents
) {
}
