package caselab.controller.signature.payload;

public record SignatureCreateRequest(
    Long documentVersionId,
    String name,
    Long userId
) {
}
