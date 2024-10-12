package caselab.service.types;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.IntegrationTest;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.EntityNotFoundException;
import caselab.service.attribute.AttributeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DocumentTypesServiceTest extends IntegrationTest {

    @Autowired
    private DocumentTypesService documentTypesService;
    @Autowired
    private DocumentTypesRepository documentTypesRepository;
    @Autowired
    private AttributeService attributeService;

    @Test
    @Transactional
    @Rollback
    public void createDocumentTypeValid() {
        // Создаем аттрибуты
        AttributeRequest attributeRequest1 = new AttributeRequest("main block", "string");
        AttributeResponse savedAttribute1 = attributeService.createAttribute(attributeRequest1);
        AttributeRequest attributeRequest2 = new AttributeRequest("date", "date");
        AttributeResponse savedAttribute2 = attributeService.createAttribute(attributeRequest2);
        List<DocumentTypeToAttributeRequest> DTTRList = List.of(
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute1.id())
                .isOptional(true)
                .build(),
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute2.id())
                .isOptional(false)
                .build()
        );

        var request = new DocumentTypeRequest("test", DTTRList);

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertAll(
            "Grouped assertions for created document type",
            () -> assertThat(createdDocumentType.id()).isNotNull(),
            () -> assertEquals(createdDocumentType.name(), request.name()),
            () -> assertTrue(documentTypesRepository.existsById(createdDocumentType.id())),
            () -> assertEquals(documentTypesRepository.findAll().size(), 1),
            () -> assertEquals(createdDocumentType.attributeResponses().stream()
                .map(DocumentTypeToAttributeResponse::attributeId)
                .toList(), List.of(savedAttribute1.id(), savedAttribute2.id())
        ));
    }

    @Test
    @Transactional
    @Rollback
    public void findExistedDocumentTypeById() {
        // Создаем аттрибуты
        AttributeRequest attributeRequest1 = new AttributeRequest("main block", "string");
        AttributeResponse savedAttribute1 = attributeService.createAttribute(attributeRequest1);
        AttributeRequest attributeRequest2 = new AttributeRequest("date", "date");
        AttributeResponse savedAttribute2 = attributeService.createAttribute(attributeRequest2);
        List<DocumentTypeToAttributeRequest> DTTRList = List.of(
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute1.id())
                .isOptional(true)
                .build(),
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute2.id())
                .isOptional(false)
                .build()
        );
        var request = new DocumentTypeRequest("test", DTTRList);

        var createdDocumentType = documentTypesService.createDocumentType(request);
        var foundDocumentType = documentTypesService.findDocumentTypeById(createdDocumentType.id());

        assertAll(
            "Grouped assertions for found document type",
            () -> assertThat(foundDocumentType).isNotNull(),
            () -> assertEquals(foundDocumentType.name(), request.name()),
            () -> assertEquals(foundDocumentType.id(), createdDocumentType.id()),
            () -> assertEquals(foundDocumentType.attributeResponses(), createdDocumentType.attributeResponses())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findNotExistedDocumentTypeById() {
        assertThrows(EntityNotFoundException.class, () -> documentTypesService.findDocumentTypeById(1L));
    }

    @Test
    @Transactional
    @Rollback
    public void deleteExistedDocumentTypeById() {

        var request = new DocumentTypeRequest("test", null);

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertAll(
            "Grouped assertions for deleted document type",
            () -> assertDoesNotThrow(() -> documentTypesService.deleteDocumentTypeById(createdDocumentType.id())),
            () -> assertFalse(documentTypesRepository.existsById(createdDocumentType.id())),
            () -> assertEquals(documentTypesRepository.findAll().size(), 0)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void deleteNotExistedDocumentTypeById() {
        assertThrows(EntityNotFoundException.class, () -> documentTypesService.deleteDocumentTypeById(1L));
    }

    @Test
    @Transactional
    @Rollback
    public void updateExistedDocumentType() {
        // Создаем аттрибуты
        AttributeRequest attributeRequest1 = new AttributeRequest("main block", "string");
        AttributeResponse savedAttribute1 = attributeService.createAttribute(attributeRequest1);
        AttributeRequest attributeRequest2 = new AttributeRequest("date", "date");
        AttributeResponse savedAttribute2 = attributeService.createAttribute(attributeRequest2);
        List<DocumentTypeToAttributeRequest> DTTRList1 = List.of(
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute1.id())
                .isOptional(true)
                .build(),
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute2.id())
                .isOptional(false)
                .build()
        );
        AttributeRequest attributeRequest3 = new AttributeRequest("expired", "boolean");
        AttributeResponse savedAttribute3 = attributeService.createAttribute(attributeRequest3);
        List<DocumentTypeToAttributeRequest> DTTRList2 = List.of(
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute1.id())
                .isOptional(true)
                .build(),
            DocumentTypeToAttributeRequest.builder()
                .attributeId(savedAttribute3.id())
                .isOptional(false)
                .build()
        );
        var requestForCreating = new DocumentTypeRequest("test", DTTRList1);
        var requestForUpdating = new DocumentTypeRequest("test2", DTTRList2);

        var createdDocumentType = documentTypesService.createDocumentType(requestForCreating);
        var updatedDocumentType = documentTypesService.updateDocumentType(createdDocumentType.id(), requestForUpdating);

        assertAll(
            "Grouped assertions for updated document type",
            () -> assertThat(updatedDocumentType).isNotNull(),
            () -> assertEquals(updatedDocumentType.name(), requestForUpdating.name()),
            () -> assertEquals(documentTypesRepository.findAll().size(), 1),
            () -> assertEquals(updatedDocumentType.attributeResponses().stream()
                    .map(DocumentTypeToAttributeResponse::attributeId)
                    .toList(),
                List.of(savedAttribute1.id(), savedAttribute3.id()))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void updateNotExistedDocumentType() {
        assertThrows(
            EntityNotFoundException.class,
            () -> documentTypesService.updateDocumentType(1L, new DocumentTypeRequest("test2", null))
        );
    }

}
