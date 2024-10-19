package caselab.service.types;

import caselab.controller.attribute.payload.AttributeRequest;
import caselab.controller.attribute.payload.AttributeResponse;
import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.controller.types.payload.DocumentTypeToAttributeResponse;
import caselab.domain.IntegrationTest;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.entity.DocumentTypeNotFoundException;
import caselab.service.attribute.AttributeService;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DocumentTypesServiceTest extends IntegrationTest {

    @Autowired
    private DocumentTypesService documentTypesService;
    @Autowired
    private DocumentTypesRepository documentTypesRepository;
    @Autowired
    private AttributeService attributeService;

    private List<AttributeResponse> attributesForCreation;
    private List<AttributeResponse> attributesForUpdate;
    private List<DocumentTypeToAttributeRequest> dttrListForCreation;
    private List<DocumentTypeToAttributeRequest> dttrListForUpdate;

    @BeforeEach
    public void setUp() {
        // Создаем атрибуты
        AttributeResponse savedAttribute1 = createAttribute("main block", "string");
        AttributeResponse savedAttribute2 = createAttribute("date", "date");
        AttributeResponse savedAttribute3 = createAttribute("expired", "boolean");

        // Инициализируем списки для создания
        attributesForCreation = List.of(savedAttribute1, savedAttribute2);
        List<Boolean> isOptionalListForCreation = List.of(true, false);
        dttrListForCreation = createDocumentTypeToAttributeRequests(attributesForCreation, isOptionalListForCreation);

        // Инициализируем списки для обновления
        attributesForUpdate = List.of(savedAttribute1, savedAttribute3);
        List<Boolean> isOptionalListForUpdate = List.of(true, false);
        dttrListForUpdate = createDocumentTypeToAttributeRequests(attributesForUpdate, isOptionalListForUpdate);
    }

    private AttributeResponse createAttribute(String name, String type) {
        AttributeRequest attributeRequest = new AttributeRequest(name, type);
        return attributeService.createAttribute(attributeRequest);
    }

    private List<DocumentTypeToAttributeRequest> createDocumentTypeToAttributeRequests(
        List<AttributeResponse> attributes,
        List<Boolean> isOptionalList
    ) {
        List<DocumentTypeToAttributeRequest> dttrList = new ArrayList<>();
        for (int i = 0; i < attributes.size(); i++) {
            dttrList.add(DocumentTypeToAttributeRequest.builder()
                .attributeId(attributes.get(i).id())
                .isOptional(isOptionalList.get(i))
                .build());
        }
        return dttrList;
    }

    private DocumentTypeRequest createDocumentTypeRequest(String name, List<DocumentTypeToAttributeRequest> dttrList) {
        return new DocumentTypeRequest(name, dttrList);
    }

    @Test
    @Transactional
    @Rollback
    public void createDocumentTypeValid() {
        var request = createDocumentTypeRequest("test", dttrListForCreation);

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertAll(
            "Grouped assertions for created document type",
            () -> assertThat(createdDocumentType.id()).isNotNull(),
            () -> assertEquals(createdDocumentType.name(), request.name()),
            () -> assertTrue(documentTypesRepository.existsById(createdDocumentType.id())),
            () -> assertEquals(documentTypesRepository.findAll().size(), 1),
            () -> assertEquals(createdDocumentType.attributeResponses().stream()
                .map(DocumentTypeToAttributeResponse::attributeId)
                .toList(), attributesForCreation.stream().map(AttributeResponse::id).toList())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findExistedDocumentTypeById() {
        var request = createDocumentTypeRequest("test", dttrListForCreation);

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
        assertThrows(DocumentTypeNotFoundException.class, () -> documentTypesService.findDocumentTypeById(1L));
    }
/*
    @Test
    @Transactional
    @Rollback
    public void deleteExistedDocumentTypeById() {
        var request = createDocumentTypeRequest("test", null);

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertAll(
            "Grouped assertions for deleted document type",
            () -> assertDoesNotThrow(() -> documentTypesService.deleteDocumentTypeById(createdDocumentType.id())),
            () -> assertFalse(documentTypesRepository.existsById(createdDocumentType.id())),
            () -> assertEquals(documentTypesRepository.findAll().size(), 0)
        );
    }
*/
    @Test
    @Transactional
    @Rollback
    public void deleteNotExistedDocumentTypeById() {
        assertThrows(DocumentTypeNotFoundException.class, () -> documentTypesService.deleteDocumentTypeById(1L));
    }

    @Test
    @Transactional
    @Rollback
    public void updateExistedDocumentType() {
        var requestForCreating = createDocumentTypeRequest("test", dttrListForCreation);
        var requestForUpdating = createDocumentTypeRequest("test2", dttrListForUpdate);

        var createdDocumentType = documentTypesService.createDocumentType(requestForCreating);
        var updatedDocumentType = documentTypesService.updateDocumentType(createdDocumentType.id(), requestForUpdating);

        assertAll(
            "Grouped assertions for updated document type",
            () -> assertThat(updatedDocumentType).isNotNull(),
            () -> assertEquals(updatedDocumentType.name(), requestForUpdating.name()),
            () -> assertEquals(documentTypesRepository.findAll().size(), 1),
            () -> assertEquals(
                updatedDocumentType.attributeResponses().stream()
                    .map(DocumentTypeToAttributeResponse::attributeId)
                    .toList(),
                attributesForUpdate.stream().map(AttributeResponse::id).toList()
            )
        );
    }

    @Test
    @Transactional
    @Rollback
    public void updateNotExistedDocumentType() {
        assertThrows(
            DocumentTypeNotFoundException.class,
            () -> documentTypesService.updateDocumentType(1L, new DocumentTypeRequest("test2", null))
        );
    }
}

