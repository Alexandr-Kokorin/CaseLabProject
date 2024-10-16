package caselab.service.document;

import caselab.Application;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.controller.document.payload.UserToDocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.service.document.mapper.DocumentMapper;
import caselab.service.document.mapper.UserToDocumentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = Application.class)
public class DocumentMapperTest {

    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private UserToDocumentMapper userToDocumentMapper;

/*
    @Test
    @DisplayName("Should map Document to DocumentResponse")
    public void testMapDocumentToDocumentResponse() {
        // Arrange
        DocumentType documentType = new DocumentType();
        documentType.setId(2001L);
        documentType.setName("Document Type");

        UserToDocument user1 = UserToDocument.builder()
            .id(1L)
            .applicationUser(ApplicationUser.builder()
                .id(1001L)
                .build())
            .build();

        UserToDocument user2 = UserToDocument.builder()
            .id(2L)
            .applicationUser(ApplicationUser
                .builder()
                .id(1002L)
                .build())
            .build();

        Document document = Document.builder()
            .id(1L)
            .name("Test Document")
            .documentType(documentType)
            .usersToDocuments(Arrays.asList(user1, user2))
            .build();

        // Act
        DocumentResponse documentResponse = documentMapper.entityToResponse(document);

        // Assert
        assertAll(
            "Grouped assertions for map Document to DocumentResponse",
            () -> assertNotNull(documentResponse),
            () -> assertEquals(1L, documentResponse.id()),
            () -> assertEquals("Test Document", documentResponse.name()),
            () -> assertEquals(2001L, documentResponse.documentTypeId())
        );
    }
*/
    @Test
    @DisplayName("Should handle null values correctly")
    public void testMapWithNullValues() {
        // Act
        Document document = documentMapper.requestToEntity(null);
        DocumentResponse documentResponse = documentMapper.entityToResponse(null);

        // Assert
        assertNull(document);
        assertNull(documentResponse);
    }
}
