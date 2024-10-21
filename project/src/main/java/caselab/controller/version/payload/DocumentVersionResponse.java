package caselab.controller.version.payload;

import java.time.OffsetDateTime;
import java.util.List;

public record DocumentVersionResponse(
    Long id,
    List<AttributeValuePair> attributes,
    String name,
    OffsetDateTime createdAt,
    Long documentId,
    List<Long> signatureIds,
    List<Long> votingProcessesId
) {
}
