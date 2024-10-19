package caselab.service.document.type.to.attribute;

import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.service.types.mapper.DocumentTypeToAttributeMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class DocumentTypeToAttributeMapperTest {

    private final DocumentTypeToAttributeMapper mapper = Mappers.getMapper(DocumentTypeToAttributeMapper.class);

    @Test
    public void testDocumentTypeToAttributeToDocumentTypeToAttributeResponse() {
        // Arrange
        Attribute attribute = new Attribute();
        attribute.setId(1L);

        DocumentTypeToAttribute documentTypeToAttribute = new DocumentTypeToAttribute();
        documentTypeToAttribute.setAttribute(attribute);
        documentTypeToAttribute.setIsOptional(true);

        // Act
        DocumentTypeToAttributeResponse response = mapper.entityToResponse(documentTypeToAttribute);

        // Assert
        assertAll(
            () -> assertThat(response).isNotNull(),
        () -> assertThat(response.attributeId()).isEqualTo(1L),
        () -> assertThat(response.isOptional()).isNotNull(),
        () -> assertThat(response.isOptional()).isEqualTo(true)
        );
    }

}
