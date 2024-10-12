package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.service.document.type.to.attribute.DocumentTypeToAttributeMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class DocumentTypeMapperTest {

    @Autowired
    private DocumentTypeMapper documentTypeMapper;
    @Autowired
    private DocumentTypeToAttributeMapper documentTypeToAttributeMapper;

    @Test
    public void testEntityToResponse() {
        // Создаем тестовые данные
        Attribute attribute1 = Attribute.builder().id(1L).name("Attribute1").build();
        Attribute attribute2 = Attribute.builder().id(2L).name("Attribute2").build();

        DocumentTypeToAttribute dtta1 = DocumentTypeToAttribute.builder()
            .attribute(attribute1)
            .optional(true)
            .build();

        DocumentTypeToAttribute dtta2 = DocumentTypeToAttribute.builder()
            .attribute(attribute2)
            .optional(false)
            .build();

        DocumentType documentType = DocumentType.builder()
            .id(100L)
            .name("Test Document Type")
            .documentTypesToAttributes(Arrays.asList(dtta1, dtta2))
            .build();

        // Выполняем маппинг
        DocumentTypeResponse response = documentTypeMapper.entityToResponse(documentType);

        // Проверяем результат
        assertNotNull(response);
        assertEquals(documentType.getId(), response.id());
        assertEquals(documentType.getName(), response.name());

        List<DocumentTypeToAttributeResponse> attributeResponses = response.attributeResponses();
        assertNotNull(attributeResponses);
        assertEquals(2, attributeResponses.size());

        // Проверяем атрибуты
        DocumentTypeToAttributeResponse attrResponse1 = attributeResponses.get(0);
        assertEquals(attribute1.getId(), attrResponse1.attributeId());
        assertEquals(dtta1.getOptional(), attrResponse1.isOptional());

        DocumentTypeToAttributeResponse attrResponse2 = attributeResponses.get(1);
        assertEquals(attribute2.getId(), attrResponse2.attributeId());
        assertEquals(dtta2.getOptional(), attrResponse2.isOptional());
    }

    @Test
    public void testRequestToEntity() {
        // Создаем тестовые данные
        DocumentTypeToAttributeRequest dttar1 = DocumentTypeToAttributeRequest.builder()
            .attributeId(1L)
            .isOptional(true)
            .build();

        DocumentTypeToAttributeRequest dttar2 = DocumentTypeToAttributeRequest.builder()
            .attributeId(2L)
            .isOptional(false)
            .build();

        DocumentTypeRequest request = DocumentTypeRequest.builder()
            .name("Test Document Type")
            .attributeRequests(Arrays.asList(dttar1, dttar2))
            .build();

        // Выполняем маппинг
        DocumentType documentType = documentTypeMapper.requestToEntity(request);

        // Проверяем результат
        assertNotNull(documentType);
        assertEquals(request.name(), documentType.getName());

        List<DocumentTypeToAttribute> documentTypesToAttributes = documentType.getDocumentTypesToAttributes();
        assertNotNull(documentTypesToAttributes);
        assertEquals(2, documentTypesToAttributes.size());

        // Проверяем атрибуты
        DocumentTypeToAttribute dtta1 = documentTypesToAttributes.get(0);
        assertEquals(dttar1.attributeId(), dtta1.getAttribute().getId());
        assertEquals(dttar1.isOptional(), dtta1.getOptional());

        DocumentTypeToAttribute dtta2 = documentTypesToAttributes.get(1);
        assertEquals(dttar2.attributeId(), dtta2.getAttribute().getId());
        assertEquals(dttar2.isOptional(), dtta2.getOptional());
    }
}
