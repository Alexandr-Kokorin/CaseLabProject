package caselab.controller.users.payload;

public record UserResponse(
    Long id,
    String login,
    String displayName
    //List<DocumentResponse> documents
) {
}
