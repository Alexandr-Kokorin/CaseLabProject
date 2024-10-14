package caselab.service.document.type.to.attribute;

import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class DocumentTypeToAttributeMapperTest {

    private final DocumentTypeToAttributeMapper mapper = Mappers.getMapper(DocumentTypeToAttributeMapper.class);

    @Test
    public void testDocumentTypeToAttributeRequestToDocumentTypeToAttribute() {
        // Arrange
        DocumentTypeToAttributeRequest request = DocumentTypeToAttributeRequest.builder()
            .attributeId(1L)
            .isOptional(true)
            .build();

        // Act
        DocumentTypeToAttribute result = mapper.documentTypeToAttributeRequestToDocumentTypeToAttribute(request);

        // Assert
        assertAll(
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.getAttribute()).isNotNull(),
            () -> assertThat(result.getAttribute().getId()).isEqualTo(1L),
            () -> assertThat(result.getOptional()).isEqualTo(true)
        );
    }

    @Test
    public void testDocumentTypeToAttributeToDocumentTypeToAttributeResponse() {
        // Arrange
        Attribute attribute = new Attribute();
        attribute.setId(1L);

        DocumentTypeToAttribute documentTypeToAttribute = new DocumentTypeToAttribute();
        documentTypeToAttribute.setAttribute(attribute);
        documentTypeToAttribute.setOptional(true);

        // Act
        DocumentTypeToAttributeResponse response = mapper.documentTypeToAttributeToDocumentTypeToAttributeResponse(documentTypeToAttribute);

        // Assert
        assertAll(
            () -> assertThat(response).isNotNull(),
        () -> assertThat(response.attributeId()).isEqualTo(1L),
        () -> assertThat(response.isOptional()).isNotNull(),
        () -> assertThat(response.isOptional()).isEqualTo(true)
        );
    }

    @Test
    public void testMapAttributeIdToAttribute() {
        // Arrange
        Long attributeId = 1L;

        // Act
        Attribute attribute = DocumentTypeToAttributeMapper.mapAttributeIdToAttribute(attributeId);

        // Assert
        assertThat(attribute).isNotNull();
        assertThat(attribute.getId()).isEqualTo(1L);
    }

}
