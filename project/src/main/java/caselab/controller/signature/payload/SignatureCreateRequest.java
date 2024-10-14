package caselab.controller.signature.payload;

import lombok.Builder;

@Builder
public record SignatureCreateRequest(
    Long documentVersionId,
    String name,
    String email
) {
}
