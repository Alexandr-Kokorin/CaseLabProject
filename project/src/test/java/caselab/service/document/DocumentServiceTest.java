package caselab.service.document;

import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.controller.document.payload.UserToDocumentResponse;
import caselab.domain.IntegrationTest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.DocumentType;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.entity.DocumentNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Rollback
public class DocumentServiceTest extends IntegrationTest {

    @Autowired
    private DocumentService documentService;
    @Autowired
    private DocumentTypesRepository documentTypeRepository;
    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    private Long documentTypeId;
    private String user1Id;
    private String user2Id;
    private DocumentRequest documentRequest;
    private UserToDocumentRequest UTD1;
    private UserToDocumentRequest UTD2;

    @BeforeEach
    public void setUp() {
        DocumentType documentType = new DocumentType();
        documentType.setName("Test Document Type");
        documentType = documentTypeRepository.save(documentType);
        documentTypeId = documentType.getId();

        ApplicationUser user1 = ApplicationUser.builder()
            .email("Test email 1 ")
            .displayName("Test display name 1")
            .hashedPassword("abc")
            .build();
        ApplicationUser user2 = ApplicationUser.builder()
            .email("Test email 2 ")
            .displayName("Test display name 2")
            .hashedPassword("abc")
            .build();
        user1 = applicationUserRepository.save(user1);
        user1Id = user1.getEmail();
        user2 = applicationUserRepository.save(user2);
        user2Id = user2.getEmail();
        UTD1 = UserToDocumentRequest.builder()
            .documentPermissionIds(List.of(1L))
            .email(user1Id)
            .build();
        UTD2 = UserToDocumentRequest.builder()
            .documentPermissionIds(List.of(1L))
            .email(user2Id)
            .build();
        documentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .usersPermissions(Arrays.asList(UTD1, UTD2))
            .name("Test name")
            .build();

    }

    @DisplayName("Should create document")
    @Test
    public void testCreateDocument() {
        // Act
        DocumentResponse result = documentService.createDocument(documentRequest);
        // Assert
        assertAll(
            "Grouped assertions for created document",
            () -> assertNotNull(result),
            () -> assertNotNull(result.id()),
            () -> assertEquals(documentTypeId, result.documentTypeId()),
            () -> assertEquals("Test name", result.name())
        );
    }

    @DisplayName("Should update document type for document")
    @Test
    public void testUpdateDocument() {
        // Act
        DocumentResponse result = documentService.createDocument(documentRequest);
        Long id = result.id();

        DocumentType newDocumentType = new DocumentType();
        newDocumentType.setName("Test Document Type 2");
        newDocumentType = documentTypeRepository.save(newDocumentType);
        Long updatedDocumentTypeId = newDocumentType.getId();

        DocumentRequest updatingDocumentRequest =
            DocumentRequest.builder()
                .documentTypeId(updatedDocumentTypeId)
                .usersPermissions(Arrays.asList(UTD1, UTD2))
                .name(result.name())
                .build();
        DocumentResponse updatingDocumentResponseDTO = documentService.updateDocument(id, updatingDocumentRequest);

        // Assert
        assertAll(
            "Grouped assertions for updated document",
            () -> assertNotNull(updatingDocumentResponseDTO),
            () -> assertEquals(id, updatingDocumentResponseDTO.id()),
            () -> assertEquals(updatedDocumentTypeId, updatingDocumentResponseDTO.documentTypeId()),
            () -> assertEquals("Test name", result.name())
        );
    }

    @DisplayName("Should delete document")
    @Test
    public void testDeleteDocument() {
        // Act
        DocumentResponse result = documentService.createDocument(documentRequest);
        Long id = result.id();
        // Act
        documentService.deleteDocument(id);

        // Assert
        assertThrows(DocumentNotFoundException.class, () -> documentService.getDocumentById(id));
    }

    @DisplayName("Should not found document")
    @Test
    public void testDeleteDocumentNotFound() {
        // Act & Assert
        assertThrows(DocumentNotFoundException.class, () -> documentService.deleteDocument(Long.MAX_VALUE));
    }

    @DisplayName("Should return document")
    @Test
    public void testGetDocumentById() {
        // Arrange
        DocumentResponse result = documentService.createDocument(documentRequest);

        // Act
        DocumentResponse findById = documentService.getDocumentById(result.id());

        // Assert
        assertAll(
            "Grouped assertions for get document by id",
            () -> assertNotNull(result),
            () -> assertEquals(result.id(), findById.id()),
            () -> assertEquals(documentTypeId, findById.documentTypeId()),
            () -> assertEquals("Test name", result.name())
        );
    }

}
