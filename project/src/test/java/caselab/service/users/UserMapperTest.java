package caselab.service.users;

import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.users.payload.UserResponse;
import caselab.controller.users.payload.UserUpdateRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.service.document.DocumentMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Mock
    private DocumentMapper documentMapper;

    private ApplicationUser user;

    @BeforeEach
    void setUp() {
        openMocks(this);

        user = ApplicationUser.builder()
            .id(1L)
            .login("testUser")
            .displayName("Test User")
            .build();
    }

    @Test
    void entityToResponse_shouldConvertUserToUserResponse() {
        Document document = Document.builder().id(100L).build();
        user.setDocuments(List.of(document));

        DocumentResponse documentResponse = DocumentResponse.builder()
            .id(100L)
            .documentTypeId(1L)
            .applicationUserIds(List.of(1L))
            .attributeValues(List.of())
            .build();

        when(documentMapper.entityToResponse(document)).thenReturn(documentResponse);

        UserResponse response = userMapper.entityToResponse(user);

        assertAll(
            "User and Document fields mapping",
            () -> assertAll(
                "User fields",
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.login()).isEqualTo("testUser"),
                () -> assertThat(response.displayName()).isEqualTo("Test User")
            ),

            () -> assertAll(
                "Document fields",
                () -> assertThat(response.documents()).hasSize(1),
                () -> assertThat(response.documents().getFirst().id()).isEqualTo(100L),
                () -> assertThat(response.documents().getFirst().documentTypeId()).isEqualTo(1L),
                () -> assertThat(response.documents().getFirst().applicationUserIds()).hasSize(1)
            )
        );
    }

    @Test
    void entityToResponse_shouldReturnNullWhenUserIsNull() {
        UserResponse response = userMapper.entityToResponse(null);

        assertThat(response).isNull();
    }

    @Test
    void updateUserFromUpdateRequest_shouldHandleNullUpdateRequest() {
        ApplicationUser user = mock(ApplicationUser.class);
        userMapper.updateUserFromUpdateRequest(null, user);

        verifyNoInteractions(user);
    }

    @Test
    void updateUserFromUpdateRequest_shouldUpdateDisplayNameWhenNotNull() {
        ApplicationUser user = new ApplicationUser();
        user.setDisplayName("Old Name");

        UserUpdateRequest updateRequest = mock(UserUpdateRequest.class);
        when(updateRequest.displayName()).thenReturn("New Name");

        userMapper.updateUserFromUpdateRequest(updateRequest, user);

        assertThat(user.getDisplayName()).isEqualTo("New Name");
    }

    @Test
    void updateUserFromUpdateRequest_shouldHandleNullUser() {
        ApplicationUser user = mock(ApplicationUser.class);
        UserUpdateRequest updateRequest = mock(UserUpdateRequest.class);
        userMapper.updateUserFromUpdateRequest(updateRequest, null);

        verifyNoInteractions(user);
    }

    @Test
    void entityToResponse_shouldHandleNullDocuments() {
        UserResponse response = userMapper.entityToResponse(user);

        assertAll(
            "User fields and no document mapping",
            () -> assertThat(response.id()).isEqualTo(1L),
            () -> assertThat(response.login()).isEqualTo("testUser"),
            () -> assertThat(response.displayName()).isEqualTo("Test User"),
            () -> assertThat(response.documents()).isNull()
        );
    }

    @Test
    void emptyEntityToResponse_shouldReturnNull() {
        UserResponse userResponse = userMapper.entityToResponse(null);

        assertThat(userResponse).isNull();
    }
}
