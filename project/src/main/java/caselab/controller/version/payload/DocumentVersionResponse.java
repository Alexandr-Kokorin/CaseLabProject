package caselab.controller.version.payload;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class DocumentVersionResponse {
    Long id;
    List<AttributeValuePair> attributes;
    String name;
    OffsetDateTime createdAt;
    Long documentId;
    List<Long> signatureIds;
    List<Long> votingProcessesId;
    String contentName;
}