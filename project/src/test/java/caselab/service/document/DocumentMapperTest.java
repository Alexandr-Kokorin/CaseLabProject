package caselab.service.document;

import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.AttributeValue;
import caselab.domain.entity.AttributeValueId;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DocumentMapperTest {

    private final DocumentMapper documentMapper = Mappers.getMapper(DocumentMapper.class);

    private Document document;
    private final DocumentType documentType = createDocumentType();
    private final ApplicationUser user1 = createApplicationUser(1L, "user1");
    private final ApplicationUser user2 = createApplicationUser(2L, "user2");
    private final Attribute attribute1 = createAttribute(1L, "Color", "String");
    private final Attribute attribute2 = createAttribute(2L, "Size", "Integer");

    @BeforeEach
    void setUp() {
        document = Document.builder()
            .id(1L)
            .documentType(documentType)
            .applicationUsers(Arrays.asList(user1, user2))
            .attributeValues(Arrays.asList(
                createAttributeValue(1L, attribute1, "Red"),
                createAttributeValue(2L, attribute2, "42")
            ))
            .build();
    }

    @Test
    void entityToResponse_shouldMapDocumentToResponse() {
        DocumentResponse response = documentMapper.entityToResponse(document);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void entityToResponse_shouldReturnNullWhenDocumentIsNull() {
        DocumentResponse response = documentMapper.entityToResponse(null);

        assertNull(response);
    }

    @Test
    void entityToResponse_shouldReturnNullDocumentTypeWhenDocumentTypeIsNull() {
        document.setDocumentType(null);
        DocumentResponse response = documentMapper.entityToResponse(document);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertNull(response.documentTypeId());
    }

    private static DocumentType createDocumentType() {
        return DocumentType.builder()
            .id(1L)
            .name("Contract")
            .build();
    }

    private static ApplicationUser createApplicationUser(Long id, String login) {
        return ApplicationUser.builder()
            .id(id)
            .login(login)
            .build();
    }

    private static Attribute createAttribute(Long id, String name, String type) {
        return Attribute.builder()
            .id(id)
            .name(name)
            .type(type)
            .build();
    }

    private static AttributeValue createAttributeValue(Long attributeId, Attribute attribute, String value) {
        return AttributeValue.builder()
            .id(new AttributeValueId(1L, attributeId))
            .attribute(attribute)
            .appValue(value)
            .build();
    }
}
