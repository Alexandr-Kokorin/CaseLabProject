package caselab.service.users;

import caselab.controller.users.payload.UserResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.UserToDocument;
import caselab.service.users.mapper.UserMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private ApplicationUser user;

    @BeforeEach
    void setUp() {
        openMocks(this);

        user = ApplicationUser.builder()
            .id(1L)
            .email("johndoe@gmail.com")
            .displayName("JohnDoe")
            .build();
    }

    @Test
    void entityToResponse_shouldConvertUserToUserResponse() {
        Document document = Document.builder().id(100L).build();

        UserToDocument userToDocument = UserToDocument.builder().document(document).build();

        user.setUsersToDocuments(List.of(userToDocument));

        UserResponse response = userMapper.entityToResponse(user);

        assertAll(
            "User and Document fields mapping",
            () -> assertAll(
                "User fields",
                () -> assertThat(response.email()).isEqualTo("johndoe@gmail.com"),
                () -> assertThat(response.displayName()).isEqualTo("JohnDoe")
            ),
            () -> assertAll(
                "Document fields",
                () -> assertThat(response.documentIds()).hasSize(1),
                () -> {
                    assert response.documentIds() != null;
                    assertThat(response.documentIds().getFirst()).isEqualTo(100L);
                }
            )
        );
    }

    @Test
    void entityToResponse_shouldReturnNullWhenUserIsNull() {
        UserResponse response = userMapper.entityToResponse(null);

        assertThat(response).isNull();
    }
}
