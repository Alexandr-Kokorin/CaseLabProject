package caselab.service.document.mapper;

import caselab.Application;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.domain.DocumentElasticTest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.UserToDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(classes = Application.class)
public class DocumentMapperTest extends DocumentElasticTest {
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



        assertAll(
            "Group of tests for map request to entity",
            ()-> assertThat(document).isNotNull(),
            ()-> assertThat(document.getName()).isEqualTo("Test Document")
        );
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

        assertAll(
            "Mapped response should not be null and properties should be mapped correctly",

            () -> assertThat(response).isNotNull(),
            () -> assertEquals(1L, response.id()),
            () -> assertEquals("Test Document", response.name()),
            () -> assertEquals(1L, response.documentTypeId()),
            () -> assertNotNull(response.documentVersionIds()),
            () -> assertEquals(1, response.documentVersionIds().size()),
            () -> assertEquals(101L, response.documentVersionIds().get(0)),
            () -> assertNotNull(response.usersPermissions()),
            () -> assertEquals(1, response.usersPermissions().size())
        );
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

        assertAll(
            "Grouped assertions for null when DocumentRequest has null documentTypeId",
            ()->assertThat(document).isNotNull(),
            ()->assertThat(document.getDocumentType()).isNull()
        );
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

        assertAll(
            "tests for empty usersPermissions",
            ()->assertThat(document).isNotNull(),
            ()->assertThat(document.getUsersToDocuments()).isNull()
        );
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

        assertAll(
            "Group asserts when name is null",
            ()->assertThat(document).isNotNull(),
            ()->assertThat(document.getName()).isNull()
        );
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

        assertAll(
            "Group asserts when documentType is null",
            ()->assertThat(response).isNotNull(),
            ()->assertThat(response.documentTypeId()).isNull()
        );
    }


}
