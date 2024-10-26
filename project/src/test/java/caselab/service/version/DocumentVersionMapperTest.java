package caselab.service.version;

import caselab.controller.version.payload.AttributeValuePair;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.attribute.value.AttributeValue;
import caselab.domain.entity.attribute.value.AttributeValueId;
import caselab.service.version.mapper.DocumentVersionMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class DocumentVersionMapperTest {
    @Autowired
    private DocumentVersionMapper documentVersionMapper;

    @Test
    void testMap_normalUseCase() {
        var attrs = LongStream.of(1L, 2L, 3L).mapToObj(
            i -> {
                var attr = new Attribute();
                attr.setId(i);
                return attr;
            }
        ).toList();

        Long documentId = 1L;
        Long documentVersionId = 1L;

        var document = new Document();
        document.setId(documentId);

        var documentVersion = new DocumentVersion();
        documentVersion.setId(documentVersionId);
        documentVersion.setName("documentVersion");

        var createdAt = OffsetDateTime.now();
        documentVersion.setCreatedAt(createdAt);

        documentVersion.setDocument(document);
        documentVersion.setContentName("/smth");
        documentVersion.setVotingProcesses(List.of());
        documentVersion.setSignatures(List.of());

        var attrValues = LongStream.of(1L, 2L, 3L).mapToObj(
            i -> new AttributeValue(
                new AttributeValueId(documentVersionId, i),
                documentVersion,
                attrs.get((int) (i - 1)),
                "" + i
            )
        ).toList();
        documentVersion.setAttributeValues(attrValues);

        var result = documentVersionMapper.map(documentVersion);
        var expected = new DocumentVersionResponse(
            documentVersionId,
            List.of(
                new AttributeValuePair(1L, "1"),
                new AttributeValuePair(2L, "2"),
                new AttributeValuePair(3L, "3")
            ),
            "documentVersion",
            createdAt,
            documentId,
            List.of(),
            List.of(),
            "/smth"
        );

        assertEquals(expected, result);
    }
}
