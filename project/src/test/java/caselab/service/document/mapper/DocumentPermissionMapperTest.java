package caselab.service.document.mapper;

import caselab.Application;
import caselab.controller.document.payload.DocumentPermissionResponse;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.enums.DocumentPermissionName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = Application.class)
public class DocumentPermissionMapperTest {
    @Autowired
    private DocumentPermissionMapper documentPermissionMapper;

    @Test
    @DisplayName("Should map DocumentPermission to DocumentPermissionResponse correctly")
    public void shouldMapEntityToResponse_correctly() {
        DocumentPermission documentPermission = DocumentPermission.builder()
            .id(1L)
            .name(DocumentPermissionName.READ)
            .build();


        DocumentPermissionResponse response = documentPermissionMapper.entityToResponse(documentPermission);


        assertEquals(1L, response.id(), "Document permission ID should match");
        assertEquals(DocumentPermissionName.READ, response.name(), "Document permission name should match");
    }

    @Test
    @DisplayName("Should return null when DocumentPermission is null")
    public void shouldReturnNullWhenEntityIsNull() {
        DocumentPermissionResponse response = documentPermissionMapper.entityToResponse(null);


        assertNull(response, "Mapped response should be null when input entity is null");
    }

    @Test
    @DisplayName("Should handle DocumentPermission with null values correctly")
    public void shouldHandleNullValuesCorrectly() {
        DocumentPermission documentPermission = DocumentPermission.builder()
            .id(null)
            .name(null)
            .build();


        DocumentPermissionResponse response = documentPermissionMapper.entityToResponse(documentPermission);


        assertNull(response.id(), "Document permission ID should be null");
        assertNull(response.name(), "Document permission name should be null");
    }
}
