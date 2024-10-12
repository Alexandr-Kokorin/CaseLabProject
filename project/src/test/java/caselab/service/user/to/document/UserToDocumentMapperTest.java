package caselab.service.user.to.document;

import caselab.controller.document.payload.user.to.document.dto.UserToDocumentRequest;
import caselab.controller.document.payload.user.to.document.dto.UserToDocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.UserToDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserToDocumentMapperTest {

    private final UserToDocumentMapper userToDocumentMapper = Mappers.getMapper(UserToDocumentMapper.class);

    @Test
    @DisplayName("Should map UserToDocument to UserToDocumentResponse")
    public void testMapUserToDocumentToResponse() {
        // Arrange
        ApplicationUser applicationUser = ApplicationUser.builder().id(1001L).build();

        UserToDocument userToDocument = UserToDocument.builder().id(1L).applicationUser(applicationUser).build();

        // Act
        UserToDocumentResponse response = userToDocumentMapper.userToDocumentToResponse(userToDocument);
        // Assert
        assertNotNull(response);
        assertEquals(1001L, response.id());
    }

    @Test
    @DisplayName("Should map UserToDocumentRequest to UserToDocument")
    public void testMapUserToDocumentRequestToUserToDocument() {
        // Arrange
        List<Long> permissionIds = Arrays.asList(1L, 2L);
        UserToDocumentRequest request =
            UserToDocumentRequest.builder().userId(1001L).documentPermissionId(permissionIds).build();

        // Act
        UserToDocument userToDocument = userToDocumentMapper.userToDocumentRequestToUserToDocument(request);

        // Assert
        assertNotNull(userToDocument);
        assertEquals(1001L, userToDocument.getApplicationUser().getId());

        // Проверяем маппинг списка documentPermissionId в список DocumentPermission
        List<Long> mappedPermissionIds = userToDocument.getDocumentPermissions().stream().map(DocumentPermission::getId)
            .collect(Collectors.toList());
        assertEquals(permissionIds, mappedPermissionIds);
    }

    @Test
    @DisplayName("Should map UserToDocumentRequest with null values correctly")
    public void testMapWithNullValues() {
        // Arrange
        UserToDocumentRequest request = UserToDocumentRequest.builder().userId(null).documentPermissionId(null).build();

        // Act
        UserToDocument userToDocument = userToDocumentMapper.userToDocumentRequestToUserToDocument(request);

        // Assert
        assertNull(userToDocument.getApplicationUser());
        assertNull(userToDocument.getDocumentPermissions());
    }

    @Test
    @DisplayName("Should map empty documentPermissionId list")
    public void testMapEmptyDocumentPermissions() {
        // Arrange
        UserToDocumentRequest request =
            UserToDocumentRequest.builder().userId(1001L).documentPermissionId(List.of()).build();

        // Act
        UserToDocument userToDocument = userToDocumentMapper.userToDocumentRequestToUserToDocument(request);

        // Assert
        assertNotNull(userToDocument);
        assertEquals(1001L, userToDocument.getApplicationUser().getId());
        assertEquals(0, userToDocument.getDocumentPermissions().size());
    }
}
