package caselab.service.document.type.to.attribute;

import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertNotNull(result);
        assertNotNull(result.getAttribute());
        assertEquals(1L, result.getAttribute().getId());
        assertTrue(result.getOptional());
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
        assertNotNull(response);
        assertEquals(1L, response.attributeId());
        assertNotNull(response.isOptional());
        assertTrue(response.isOptional());
    }

    @Test
    public void testMapAttributeIdToAttribute() {
        // Arrange
        Long attributeId = 1L;

        // Act
        Attribute attribute = DocumentTypeToAttributeMapper.mapAttributeIdToAttribute(attributeId);

        // Assert
        assertNotNull(attribute);
        assertEquals(1L, attribute.getId());
    }

    @Test
    public void testMapAttributeIdToAttribute_Null() {
        // Act
        Attribute attribute = DocumentTypeToAttributeMapper.mapAttributeIdToAttribute(null);

        // Assert
        assertNull(attribute);
    }
}
