package caselab.service.document;

import caselab.Application;
import caselab.controller.document.payload.DocumentPermissionResponse;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.service.document.mapper.DocumentPermissionMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(classes = Application.class)
public class DocumentPermissionMapperTest {
    @Autowired
    private DocumentPermissionMapper documentPermissionMapper;

    // TODO - что-нибудь сделать, сейчас не работает
    /*
    @Test
    @DisplayName("Should map DocumentPermission to DocumentPermissionResponse correctly")
    public void shouldMapEntityToResponse_correctly() {
        DocumentPermission documentPermission = DocumentPermission.builder()
            .id(1L)
            .name(DocumentPermissionName.READ)
            .build();

        DocumentPermissionResponse response = documentPermissionMapper.entityToResponse(documentPermission);

        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.id()).isEqualTo(1L),
            () -> assertThat(response.name()).isEqualTo(DocumentPermissionName.READ)
        );
    }

    @Test
    @DisplayName("Should return null when DocumentPermission is null")
    public void shouldReturnNullWhenEntityIsNull() {
        DocumentPermissionResponse response = documentPermissionMapper.entityToResponse(null);

        assertAll(
            () -> assertThat(response).isNull()
        );
    }

    @Test
    @DisplayName("Should handle DocumentPermission with null values correctly")
    public void shouldHandleNullValuesCorrectly() {
        DocumentPermission documentPermission = DocumentPermission.builder()
            .id(null)
            .name(null)
            .build();

        DocumentPermissionResponse response = documentPermissionMapper.entityToResponse(documentPermission);

        assertAll(
            () -> assertThat(response).isNotNull(),
            () -> assertThat(response.id()).isNull(),
            () -> assertThat(response.name()).isNull()
        );
    }
     */
}
