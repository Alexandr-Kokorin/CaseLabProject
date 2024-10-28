package caselab.controller.version.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

@Builder
public record CreateDocumentVersionRequest(
    @JsonProperty("document_id")
    Long documentId,
    @JsonProperty("name")
    String name,
    @JsonProperty("attributes")
    List<AttributeValuePair> attributes
) {
}
