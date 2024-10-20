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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

        assertNotNull(response);
        assertEquals("user@example.com", response.email(), "Email should be mapped correctly");
        assertNotNull(response.documentPermissions(), "Permissions should not be null");
        assertEquals(1, response.documentPermissions().size(), "Permissions list size should match");
        assertEquals("READ", response.documentPermissions().get(0).name().toString(), "Permission name should match");
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


        assertNotNull(response);
        assertEquals("user@example.com", response.email(), "Email should be mapped correctly");
        assertNull(response.documentPermissions());
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


        assertNotNull(response);
        assertNull(response.email(), "Email should be null when ApplicationUser is null");
        assertNotNull(response.documentPermissions(), "Permissions should not be null");
        assertEquals(1, response.documentPermissions().size(), "Permissions list size should match");
        assertEquals("READ", response.documentPermissions().get(0).name().toString(), "Permission name should match");
    }

}
