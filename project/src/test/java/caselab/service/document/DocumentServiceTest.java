package caselab.service.document;

import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.IntegrationTest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.enums.Role;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
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
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentTypesRepository documentTypeRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    private Long documentTypeId;
    private Long user1Id;
    private Long user2Id;
    private Long attributeId;

    @BeforeEach
    public void setUp() {
        DocumentType documentType = new DocumentType();
        documentType.setName("Test Document Type");
        documentType = documentTypeRepository.save(documentType);
        documentTypeId = documentType.getId();

        ApplicationUser user1 = ApplicationUser.builder()
            .login("Test login 1 ")
            .displayName("Test display name 1")
            .role(Role.USER)
            .hashedPassword("abc")
            .build();
        ApplicationUser user2 = ApplicationUser.builder()
            .login("Test login 2 ")
            .displayName("Test display name 2")
            .role(Role.USER)
            .hashedPassword("abc")
            .build();
        user1 = applicationUserRepository.save(user1);
        user1Id = user1.getId();
        user2 = applicationUserRepository.save(user2);
        user2Id = user2.getId();
        Attribute attribute = new Attribute();
        attribute.setName("Test attribute 1");
        attribute.setType("String");
        attribute.setDocumentTypes(List.of(documentType));
        attribute = attributeRepository.save(attribute);
        attributeId = attribute.getId();

    }

    @DisplayName("Should create document")
    @Test
    public void testCreateDocument() {
        // Arrange
        DocumentAttributeValueDTO attributeValueDTO = DocumentAttributeValueDTO.builder()
            .id(attributeId)
            .value("Test Value")
            .build();
        DocumentRequest documentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .applicationUserIds(Arrays.asList(user1Id, user2Id))
            .attributeValues(Collections.singletonList(attributeValueDTO))
            .build();

        // Act
        DocumentResponse result = documentService.createDocument(documentRequest);

        // Assert
        assertAll(
            "Grouped assertions for created document",
            () -> assertNotNull(result),
            () -> assertNotNull(result.id()),
            () -> assertEquals(documentTypeId, result.documentTypeId()),
            () -> assertEquals(Arrays.asList(user1Id, user2Id), result.applicationUserIds()),
            () -> assertEquals(attributeId, result.attributeValues().get(0).id()),
            () -> assertEquals("Test Value", result.attributeValues().get(0).value())
        );
    }

    @DisplayName("Should update document type for document")
    @Test
    public void testUpdateDocument() {
        // Arrange
        DocumentAttributeValueDTO attributeValueDTO = DocumentAttributeValueDTO.builder()
            .id(attributeId)
            .value("Test Value")
            .build();
        DocumentRequest documentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .applicationUserIds(Arrays.asList(user1Id, user2Id))
            .attributeValues(Collections.singletonList(attributeValueDTO))
            .build();

        // Act
        DocumentResponse result = documentService.createDocument(documentRequest);
        Long id = result.id();

        DocumentType newDocumentType = new DocumentType();
        newDocumentType.setName("Test Document Type 2");
        newDocumentType = documentTypeRepository.save(newDocumentType);
        Long updatedDocumentTypeId = newDocumentType.getId();

        DocumentRequest updatingDocumentRequest = DocumentRequest.builder()
            .documentTypeId(updatedDocumentTypeId)
            .applicationUserIds(result.applicationUserIds())
            .attributeValues(result.attributeValues())
            .id(result.id())
            .build();
        DocumentResponse updatingDocumentResponseDTO =
            documentService.updateDocument(id, updatingDocumentRequest);

        // Assert
        assertAll(
            "Grouped assertions for updated document",
            () -> assertNotNull(updatingDocumentResponseDTO),
            () -> assertEquals(id, updatingDocumentResponseDTO.id()),
            () -> assertEquals(updatedDocumentTypeId, updatingDocumentResponseDTO.documentTypeId()),
            () -> assertEquals(Arrays.asList(user1Id, user2Id), updatingDocumentResponseDTO.applicationUserIds()),
            () -> assertEquals(attributeId, updatingDocumentResponseDTO.attributeValues().get(0).id()),
            () -> assertEquals("Test Value", updatingDocumentResponseDTO.attributeValues().get(0).value())
        );
    }

    @DisplayName("Should delete document")
    @Test
    public void testDeleteDocument() {
        // Arrange
        DocumentAttributeValueDTO attributeValueDTO = DocumentAttributeValueDTO.builder()
            .id(attributeId)
            .value("Test value")
            .build();
        DocumentRequest documentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .applicationUserIds(Arrays.asList(user1Id, user2Id))
            .attributeValues(Collections.singletonList(attributeValueDTO))
            .build();

        DocumentResponse result = documentService.createDocument(documentRequest);
        Long id = result.id();
        // Act
        documentService.deleteDocument(id);

        // Assert
        assertThrows(NoSuchElementException.class, () -> documentService.getDocumentById(id));
    }

    @DisplayName("Should not found document")
    @Test
    public void testDeleteDocumentNotFound() {
        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> documentService.deleteDocument(1L));
    }

    @DisplayName("Should return document")
    @Test
    public void testGetDocumentById() {
        // Arrange
        DocumentAttributeValueDTO attributeValueDTO = DocumentAttributeValueDTO.builder()
            .id(attributeId)
            .value("Test Value")
            .build();
        DocumentRequest documentRequest = DocumentRequest.builder()
            .documentTypeId(documentTypeId)
            .applicationUserIds(Arrays.asList(user1Id, user2Id))
            .attributeValues(Collections.singletonList(attributeValueDTO))
            .build();
        DocumentResponse result = documentService.createDocument(documentRequest);

        // Act
        DocumentResponse findById = documentService.getDocumentById(result.id());

        // Assert
        assertAll(
            "Grouped assertions for get document by id",
            () -> assertNotNull(result),
            () -> assertEquals(result.id(), findById.id()),
            () -> assertEquals(documentTypeId, findById.documentTypeId()),
            () -> assertEquals(Arrays.asList(user1Id, user2Id), findById.applicationUserIds()),
            () -> assertEquals(attributeId, findById.attributeValues().get(0).id()),
            () -> assertEquals("Test Value", findById.attributeValues().get(0).value())
        );
    }

}
