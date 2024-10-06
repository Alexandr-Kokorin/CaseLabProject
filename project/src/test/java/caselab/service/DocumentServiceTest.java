package caselab.service;

import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentDTO;
import caselab.controller.document.payload.DocumentResponseDTO;
import caselab.domain.IntegrationTest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.enums.Role;
import caselab.domain.entity.exception.ResourceNotFoundException;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.service.document.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentTypeId(documentTypeId);
        documentDTO.setApplicationUserIds(Arrays.asList(user1Id, user2Id));
        DocumentAttributeValueDTO attributeValueDTO = new DocumentAttributeValueDTO();
        attributeValueDTO.setId(attributeId);
        attributeValueDTO.setValue("Test Value");
        documentDTO.setAttributeValues(Collections.singletonList(attributeValueDTO));

        // Act
        DocumentResponseDTO result = documentService.createDocument(documentDTO);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(documentTypeId, result.getDocumentTypeId());
        assertEquals(Arrays.asList(user1Id, user2Id), result.getApplicationUserIds());
        assertEquals(attributeId, result.getAttributeValues().get(0).getId());
        assertEquals("Test Value", result.getAttributeValues().get(0).getValue());
    }

    @DisplayName("Should update document type for document")
    @Test
    public void testUpdateDocument() {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentTypeId(documentTypeId);
        documentDTO.setApplicationUserIds(Arrays.asList(user1Id, user2Id));
        DocumentAttributeValueDTO attributeValueDTO = new DocumentAttributeValueDTO();
        attributeValueDTO.setId(attributeId);
        attributeValueDTO.setValue("Test Value");
        documentDTO.setAttributeValues(Collections.singletonList(attributeValueDTO));

        // Act
        DocumentResponseDTO result = documentService.createDocument(documentDTO);
        Long id = result.getId();

        DocumentType newDocumentType = new DocumentType();
        newDocumentType.setName("Test Document Type 2");
        newDocumentType = documentTypeRepository.save(newDocumentType);
        Long updatedDocumentTypeId = newDocumentType.getId();

        DocumentDTO updatingDocumentDTO = new DocumentDTO();
        updatingDocumentDTO.setDocumentTypeId(updatedDocumentTypeId);
        updatingDocumentDTO.setApplicationUserIds(result.getApplicationUserIds());
        updatingDocumentDTO.setAttributeValues(result.getAttributeValues());
        updatingDocumentDTO.setId(result.getId());
        DocumentResponseDTO updatingDocumentResponseDTO = documentService.updateDocument(id, updatingDocumentDTO);

        // Assert
        assertNotNull(updatingDocumentResponseDTO);
        assertEquals(id, updatingDocumentResponseDTO.getId());
        assertEquals(updatedDocumentTypeId, updatingDocumentResponseDTO.getDocumentTypeId());
        assertEquals(Arrays.asList(user1Id, user2Id), updatingDocumentResponseDTO.getApplicationUserIds());
        assertEquals(attributeId, updatingDocumentResponseDTO.getAttributeValues().get(0).getId());
        assertEquals("Test Value", updatingDocumentResponseDTO.getAttributeValues().get(0).getValue());
    }

    @DisplayName("Should delete document")
    @Test
    public void testDeleteDocument() {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentTypeId(documentTypeId);
        documentDTO.setApplicationUserIds(Arrays.asList(user1Id, user2Id));
        DocumentAttributeValueDTO attributeValueDTO = new DocumentAttributeValueDTO();
        attributeValueDTO.setId(attributeId);
        attributeValueDTO.setValue("Test Value");
        documentDTO.setAttributeValues(Collections.singletonList(attributeValueDTO));
        DocumentResponseDTO result = documentService.createDocument(documentDTO);
        Long id = result.getId();
        // Act
        documentService.deleteDocument(id);

        // Assert
        assertThrows(ResourceNotFoundException.class, () -> documentService.getDocumentById(id));
    }

    @DisplayName("Should not found document")
    @Test
    public void testDeleteDocumentNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> documentService.deleteDocument(1L));
    }

    @DisplayName("Should return document")
    @Test
    public void testGetDocumentById() {
        // Arrange
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentTypeId(documentTypeId);
        documentDTO.setApplicationUserIds(Arrays.asList(user1Id, user2Id));
        DocumentAttributeValueDTO attributeValueDTO = new DocumentAttributeValueDTO();
        attributeValueDTO.setId(attributeId);
        attributeValueDTO.setValue("Test Value");
        documentDTO.setAttributeValues(Collections.singletonList(attributeValueDTO));
        DocumentResponseDTO result = documentService.createDocument(documentDTO);

        // Act
        DocumentResponseDTO findById = documentService.getDocumentById(result.getId());

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), findById.getId());
        assertEquals(documentTypeId, findById.getDocumentTypeId());
        assertEquals(Arrays.asList(user1Id, user2Id), findById.getApplicationUserIds());
        assertEquals(attributeId, findById.getAttributeValues().get(0).getId());
        assertEquals("Test Value", findById.getAttributeValues().get(0).getValue());
    }

}
