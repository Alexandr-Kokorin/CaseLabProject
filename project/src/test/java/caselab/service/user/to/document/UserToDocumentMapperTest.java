package caselab.service.user.to.document;

import caselab.controller.document.payload.UserToDocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.UserToDocument;
import caselab.service.document.mapper.UserToDocumentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UserToDocumentMapperTest {

    private final UserToDocumentMapper userToDocumentMapper = Mappers.getMapper(UserToDocumentMapper.class);

    @Test
    @DisplayName("Should map UserToDocument to UserToDocumentResponse")
    public void testMapUserToDocumentToResponse() {
        // Arrange
        ApplicationUser applicationUser = ApplicationUser.builder()
            .id(1001L)
            .build();

        UserToDocument userToDocument = UserToDocument.builder()
            .id(1L)
            .applicationUser(applicationUser)
            .build();

        // Act
        UserToDocumentResponse response = userToDocumentMapper.entityToResponse(userToDocument);
        // Assert
        assertNotNull(response);
    }
}
