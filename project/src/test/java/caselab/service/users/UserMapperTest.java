package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.DocumentVersion;
import caselab.service.document.mapper.DocumentMapper;
import caselab.service.users.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
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
            .email("testUser")
            .displayName("Test User")
            .build();
    }
/*
    @Test
    void entityToResponse_shouldConvertUserToUserResponse() {
        DocumentVersion document = DocumentVersion.builder().id(100L).build();
//        user.setDocuments(List.of(document));

//        DocumentResponse documentResponse = DocumentResponse.builder()
//            .id(100L)
//            .documentTypeId(1L)
//            .applicationUserIds(List.of(1L))
//            .attributeValues(List.of())
//            .build();
//
//        when(documentMapper.entityToResponse(document)).thenReturn(documentResponse);

        UserResponse response = userMapper.entityToResponse(user);

        assertAll(
            "User and Document fields mapping",
            () -> assertAll(
                "User fields",
                () -> assertThat(response.email()).isEqualTo("testUser"),
                () -> assertThat(response.displayName()).isEqualTo("Test User")
            )//,

//            () -> assertAll(
//                "Document fields",
//                () -> assertThat(response.documents()).hasSize(1),
//                () -> assertThat(response.documents().getFirst().id()).isEqualTo(100L),
//                () -> assertThat(response.documents().getFirst().documentTypeId()).isEqualTo(1L),
//                () -> assertThat(response.documents().getFirst().usersPermissions()).hasSize(1)
//            )
        );
    }
*/
    @Test
    void entityToResponse_shouldReturnNullWhenUserIsNull() {
        UserResponse response = userMapper.entityToResponse(null);

        assertThat(response).isNull();
    }
/*
    @Test
    void entityToResponse_shouldHandleNullDocuments() {
        UserResponse response = userMapper.entityToResponse(user);

        assertAll(
            "User fields and no document mapping",
            () -> assertThat(response.email()).isEqualTo("testUser"),
            () -> assertThat(response.displayName()).isEqualTo("Test User")
        );
    }
*/
    @Test
    void emptyEntityToResponse_shouldReturnNull() {
        UserResponse userResponse = userMapper.entityToResponse(null);

        assertThat(userResponse).isNull();
    }
}
