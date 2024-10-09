package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.domain.IntegrationTest;
import caselab.domain.repository.DocumentTypesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DocumentTypesServiceTest extends IntegrationTest {

    @Autowired
    private DocumentTypesService documentTypesService;
    @Autowired
    private DocumentTypesRepository documentTypesRepository;

    @Test
    @Transactional
    @Rollback
    public void createDocumentTypeValid() {
        var request = new DocumentTypeRequest("test");

        var createdDocumentType = documentTypesService.createDocumentType(request);

        assertAll(
            "Grouped assertions for created document type",
            () -> assertThat(createdDocumentType.id()).isNotNull(),
            () -> assertEquals(createdDocumentType.name(), request.name()),
            () -> assertTrue(documentTypesRepository.existsById(createdDocumentType.id())),
            () -> assertEquals(documentTypesRepository.findAll().size(), 1)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findExistedDocumentTypeById() {
        var request = new DocumentTypeRequest("test");

        var createdDocumentType = documentTypesService.createDocumentType(request);
        var foundDocumentType = documentTypesService.findDocumentTypeById(createdDocumentType.id());

        assertAll(
            "Grouped assertions for found document type",
            () -> assertThat(foundDocumentType).isNotNull(),
            () -> assertEquals(foundDocumentType.name(), request.name()),
            () -> assertEquals(foundDocumentType.id(), createdDocumentType.id())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findNotExistedDocumentTypeById() {
        assertThrows(NoSuchElementException.class, () -> documentTypesService.findDocumentTypeById(1L));
    }

    @Test
    @Transactional
    @Rollback
    public void deleteExistedDocumentTypeById() {
        var request = new DocumentTypeRequest("test");

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
        assertThrows(NoSuchElementException.class, () -> documentTypesService.deleteDocumentTypeById(1L));
    }

    @Test
    @Transactional
    @Rollback
    public void updateExistedDocumentType() {
        var requestForCreating = new DocumentTypeRequest("test");
        var requestForUpdating = new DocumentTypeRequest("test2");

        var createdDocumentType = documentTypesService.createDocumentType(requestForCreating);
        var updatedDocumentType = documentTypesService.updateDocumentType(createdDocumentType.id(), requestForUpdating);

        assertAll(
            "Grouped assertions for updated document type",
            () -> assertThat(updatedDocumentType).isNotNull(),
            () -> assertEquals(updatedDocumentType.name(), requestForUpdating.name()),
            () -> assertEquals(documentTypesRepository.findAll().size(), 1)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void updateNotExistedDocumentType() {
        assertThrows(
            NoSuchElementException.class,
            () -> documentTypesService.updateDocumentType(1L, new DocumentTypeRequest("test2"))
        );
    }
}
