package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.DocumentElasticTest;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.service.types.mapper.DocumentTypeMapper;
import caselab.service.types.mapper.DocumentTypeToAttributeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Arrays;
import java.util.List;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class DocumentTypeMapperTest extends DocumentElasticTest {
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
            .isOptional(true)
            .build();

        DocumentTypeToAttribute dtta2 = DocumentTypeToAttribute.builder()
            .attribute(attribute2)
            .isOptional(false)
            .build();

        DocumentType documentType = DocumentType.builder()
            .id(100L)
            .name("Test Document Type")
            .documentTypesToAttributes(Arrays.asList(dtta1, dtta2))
            .build();

        // Выполняем маппинг
        DocumentTypeResponse response = documentTypeMapper.entityToResponse(documentType);

        // Проверяем результат через assertAll и assertThat
        assertAll(
            () -> assertThat(response, is(notNullValue())),
            () -> assertThat(response.id(), is(equalTo(documentType.getId()))),
            () -> assertThat(response.name(), is(equalTo(documentType.getName())))
        );

        List<DocumentTypeToAttributeResponse> attributeResponses = response.attributeResponses();
        assertAll(
            () -> assertThat(attributeResponses, is(notNullValue())),
            () -> assertThat(attributeResponses.size(), is(equalTo(2)))
        );

        // Проверяем атрибуты
        DocumentTypeToAttributeResponse attrResponse1 = attributeResponses.get(0);
        assertAll(
            () -> assertThat(attrResponse1.attributeId(), is(equalTo(attribute1.getId()))),
            () -> assertThat(attrResponse1.isOptional(), is(equalTo(dtta1.getIsOptional())))
        );

        DocumentTypeToAttributeResponse attrResponse2 = attributeResponses.get(1);
        assertAll(
            () -> assertThat(attrResponse2.attributeId(), is(equalTo(attribute2.getId()))),
            () -> assertThat(attrResponse2.isOptional(), is(equalTo(dtta2.getIsOptional())))
        );
    }
/*
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

        // Проверяем результат через assertAll и assertThat
        assertAll(
            () -> assertThat(documentType, is(notNullValue())),
            () -> assertThat(documentType.getName(), is(equalTo(request.name())))
        );

        List<DocumentTypeToAttribute> documentTypesToAttributes = documentType.getDocumentTypesToAttributes();
        assertAll(
            () -> assertThat(documentTypesToAttributes, is(notNullValue())),
            () -> assertThat(documentTypesToAttributes.size(), is(equalTo(2)))
        );

        // Проверяем атрибуты
        DocumentTypeToAttribute dtta1 = documentTypesToAttributes.get(0);
        assertAll(
            () -> assertThat(dtta1.getAttributes().getId(), is(equalTo(dttar1.attributeId()))),
            () -> assertThat(dtta1.getIsOptional(), is(equalTo(dttar1.isOptional())))
        );

        DocumentTypeToAttribute dtta2 = documentTypesToAttributes.get(1);
        assertAll(
            () -> assertThat(dtta2.getAttributes().getId(), is(equalTo(dttar2.attributeId()))),
            () -> assertThat(dtta2.getIsOptional(), is(equalTo(dttar2.isOptional())))
        );
    }

 */
}
