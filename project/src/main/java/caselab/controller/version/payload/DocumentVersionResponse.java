package caselab.controller.version.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersionResponse {
    Long id;
    List<AttributeValuePair> attributes;
    String name;
    OffsetDateTime createdAt;
    Long documentId;
    List<Long> signatureIds;
    List<Long> votingProcessesId;
    String contentUrl;
}
