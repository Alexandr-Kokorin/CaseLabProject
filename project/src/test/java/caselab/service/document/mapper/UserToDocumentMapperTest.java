package caselab.service.document.mapper;

import caselab.Application;
import caselab.controller.document.payload.UserToDocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.DocumentPermissionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;


@SpringBootTest(classes = Application.class)
public class UserToDocumentMapperTest {

    @Autowired
    private UserToDocumentMapper userToDocumentMapper;


    @Test
    @DisplayName("Should map UserToDocument entity to UserToDocumentResponse correctly")
    public void shouldMapEntityToResponse_correctly() {
        var applicationUser = ApplicationUser.builder()
            .email("user@example.com")
            .build();

        var document = Document.builder()
            .name("Test Document")
            .build();

        var documentPermission = DocumentPermission.builder()
            .id(1L)
            .name(DocumentPermissionName.READ)
            .build();

        var userToDocument = UserToDocument.builder()
            .applicationUser(applicationUser)
            .document(document)
            .documentPermissions(List.of(documentPermission))
            .build();

        UserToDocumentResponse response = userToDocumentMapper.entityToResponse(userToDocument);

        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.email()).isEqualTo("user@example.com"),
            () -> assertThat(response.documentPermissions()).isNotNull(),
            () -> assertThat(response.documentPermissions()).hasSize(1),
            () -> assertThat(response.documentPermissions().get(0).name().toString()).isEqualTo("READ")
        );
    }

    @Test
    @DisplayName("Should handle null permissions in UserToDocument entity")
    public void shouldHandleNullPermissions() {
        var applicationUser = ApplicationUser.builder()
            .email("user@example.com")
            .build();

        var document = Document.builder()
            .name("Test Document")
            .build();

        var userToDocument = UserToDocument.builder()
            .applicationUser(applicationUser)
            .document(document)
            .build();

        UserToDocumentResponse response = userToDocumentMapper.entityToResponse(userToDocument);

        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.email()).isEqualTo("user@example.com"),
            () -> assertThat(response.documentPermissions()).isNull()
        );
    }

    @Test
    @DisplayName("Should handle null ApplicationUser in UserToDocument entity")
    public void shouldHandleNullApplicationUser() {
        var document = Document.builder()
            .name("Test Document")
            .build();

        var documentPermission = DocumentPermission.builder()
            .id(1L)
            .name(DocumentPermissionName.READ)
            .build();

        var userToDocument = UserToDocument.builder()
            .applicationUser(null)
            .document(document)
            .documentPermissions(List.of(documentPermission))
            .build();

        UserToDocumentResponse response = userToDocumentMapper.entityToResponse(userToDocument);

        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.email()).isNull(),
            () -> assertThat(response.documentPermissions()).isNotNull(),
            () -> assertThat(response.documentPermissions()).hasSize(1),
            () -> assertThat(response.documentPermissions().get(0).name().toString()).isEqualTo("READ")
        );
    }

}
