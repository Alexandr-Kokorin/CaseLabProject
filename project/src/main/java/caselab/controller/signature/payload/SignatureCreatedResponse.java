package caselab.controller.signature.payload;

import caselab.domain.entity.enums.SignatureStatus;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder
public record SignatureCreatedResponse(
    Long id,
    String name,
    SignatureStatus status,
    OffsetDateTime sentAt,
    OffsetDateTime signedAt,
    String signatureData
) {
}
