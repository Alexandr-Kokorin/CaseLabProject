package caselab.service.document.mapper;

import caselab.Application;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.DocumentPermissionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Application.class)
public class DocumentMapperTest {
    @Autowired
    private DocumentMapper documentMapper;
    @Test
    @DisplayName("Should map DocumentRequest to Document entity correctly")
    public void shouldMapRequestToEntity_correctly() {

        var userToDocumentRequest = UserToDocumentRequest.builder()
            .email("user1@example.com")
            .documentPermissionIds(List.of(1L, 2L, 3L))
            .build();

        var documentType = DocumentType.builder()
            .id(1L)
            .name("test")
            .build();

        var documentRequest = DocumentRequest.builder()
            .name("Test Document")
            .documentTypeId(documentType.getId())
            .usersPermissions(List.of(userToDocumentRequest))
            .build();


        Document document = documentMapper.requestToEntity(documentRequest);


        assertNotNull(document, "Mapped document should not be null");
        assertEquals("Test Document", document.getName(), "Document name should be mapped correctly");
        assertNotNull(document.getDocumentType(), "Document type should not be null");
        assertEquals(1L, document.getDocumentType().getId(), "Document type ID should be mapped correctly");
        assertNotNull(document.getUsersToDocuments(), "Users permissions should not be null");
        assertEquals(1, document.getUsersToDocuments().size(), "Users permissions list size should match");
    }


    @Test
    @DisplayName("Should map Document entity to DocumentResponse correctly")
    public void shouldMapEntityToResponse_correctly() {
        var documentType = DocumentType.builder()
            .id(1L)
            .name("Internal Document")
            .build();

        var documentVersion = DocumentVersion.builder()
            .id(101L)
            .name("Version 1")
            .build();

        var userToDocument = UserToDocument.builder()
            .applicationUser(ApplicationUser.builder().email("user@example.com").build())
            .build();

        var document = Document.builder()
            .id(1L)
            .name("Test Document")
            .documentType(documentType)
            .documentVersions(List.of(documentVersion))
            .usersToDocuments(List.of(userToDocument))
            .build();

        DocumentResponse response = documentMapper.entityToResponse(document);

        assertNotNull(response, "Mapped response should not be null");
        assertEquals(1L, response.id(), "Document ID should match");
        assertEquals("Test Document", response.name(), "Document name should be mapped correctly");
        assertEquals(1L, response.documentTypeId(), "Document type ID should be mapped correctly");
        assertNotNull(response.documentVersionIds(), "Document version IDs should not be null");
        assertEquals(1, response.documentVersionIds().size(), "Document version IDs list size should match");
        assertEquals(101L, response.documentVersionIds().get(0), "Document version ID should match");
        assertNotNull(response.usersPermissions(), "User permissions should not be null");
        assertEquals(1, response.usersPermissions().size(), "Users permissions list size should match");
    }


    @Test
    @DisplayName("Should return null when DocumentRequest has null documentTypeId")
    public void shouldReturnNullWhenDocumentTypeIdIsNull() {
        var userToDocumentRequest = UserToDocumentRequest.builder()
            .email("user1@example.com")
            .documentPermissionIds(List.of(1L, 2L, 3L))
            .build();

        var documentRequest = DocumentRequest.builder()
            .name("Test Document")
            .documentTypeId(null)
            .usersPermissions(List.of(userToDocumentRequest))
            .build();

        Document document = documentMapper.requestToEntity(documentRequest);


        assertNotNull(document, "Mapped document should not be null");
        assertNull(document.getDocumentType().getId(), "Document type ID should be null when documentTypeId is null");
    }

    @Test
    @DisplayName("Should return null when DocumentRequest has empty usersPermissions")
    public void shouldReturnNullWhenUsersPermissionsIsEmpty() {

        var documentRequest = DocumentRequest.builder()
            .name("Test Document")
            .documentTypeId(1L)
            .usersPermissions(List.of()) // Empty usersPermissions
            .build();

        Document document = documentMapper.requestToEntity(documentRequest);


        assertNotNull(document, "Mapped document should not be null");
        assertNotNull(document.getUsersToDocuments(), "Users permissions should not be null");
        assertTrue(document.getUsersToDocuments().isEmpty(), "Users permissions list should be empty");
    }

    @Test
    @DisplayName("Should return null when DocumentRequest has null name")
    public void shouldReturnNullWhenNameIsNull() {
        var userToDocumentRequest = UserToDocumentRequest.builder()
            .email("user1@example.com")
            .documentPermissionIds(List.of(1L, 2L, 3L))
            .build();

        var documentRequest = DocumentRequest.builder()
            .name(null) // Null name
            .documentTypeId(1L)
            .usersPermissions(List.of(userToDocumentRequest))
            .build();


        Document document = documentMapper.requestToEntity(documentRequest);


        assertNotNull(document, "Mapped document should not be null");
        assertNull(document.getName(), "Document name should be null when name is null");
    }

    @Test
    @DisplayName("Should return null when Document entity has null documentType")
    public void shouldReturnNullWhenDocumentTypeIsNullInEntity() {
        var documentVersion = DocumentVersion.builder()
            .id(101L)
            .name("Version 1")
            .build();



        var document = Document.builder()
            .id(1L)
            .name("Test Document")
            .documentType(null) // Null documentType
            .documentVersions(List.of(documentVersion))
            .build();

        DocumentResponse response = documentMapper.entityToResponse(document);


        assertNotNull(response, "Mapped response should not be null");
        assertNull(response.documentTypeId(), "Document type ID should be null when documentType is null");
    }

    @Test
    @DisplayName("Should return empty list for documentVersionIds when documentVersions are null")
    public void shouldReturnEmptyListWhenDocumentVersionsAreNull() {
        var documentType = DocumentType.builder()
            .id(1L)
            .name("Internal Document")
            .build();

        var document = Document.builder()
            .id(1L)
            .name("Test Document")
            .documentType(documentType)
            .documentVersions(null) // Null documentVersions
            .usersToDocuments(List.of())
            .build();

        DocumentResponse response = documentMapper.entityToResponse(document);

        assertNotNull(response, "Mapped response should not be null");
        assertNotNull(response.documentVersionIds(), "Document version IDs should not be null");
        assertTrue(response.documentVersionIds().isEmpty(), "Document version IDs list should be empty");
    }

}
