package caselab.controller.version.payload;

import java.util.List;

public record CreateDocumentVersionRequest(
    Long documentId,
    String name,
    String content,
    List<AttributeValuePair> attributes
) {
}
