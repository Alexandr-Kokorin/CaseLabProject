package caselab.service.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.controller.document.payload.user.to.document.dto.UserToDocumentRequest;
import caselab.controller.document.payload.user.to.document.dto.UserToDocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DocumentMapperTest {

    private final DocumentMapper documentMapper = Mappers.getMapper(DocumentMapper.class);

    @Test @DisplayName("Should map DocumentRequest to Document") public void testMapDocumentRequestToDocument() {
        // Arrange
        UserToDocumentRequest UTD1 =
            UserToDocumentRequest.builder().documentPermissionId(List.of(1L)).userId(1001L).build();
        UserToDocumentRequest UTD2 =
            UserToDocumentRequest.builder().documentPermissionId(List.of(2L)).userId(1002L).build();

        DocumentRequest documentRequest =
            DocumentRequest.builder().documentTypeId(2001L).usersPermissions(Arrays.asList(UTD1, UTD2))
                .name("Test Document").build();

        // Act
        Document document = documentMapper.documentRequestToDocument(documentRequest);

        // Assert
        assertAll(
            "Grouped assertions for map DocumentRequest to Document",
            () -> assertNotNull(document),
            () -> assertEquals("Test Document", document.getName()),
            () -> assertEquals(2001L, document.getDocumentType().getId()),
            () -> assertEquals(
                Arrays.asList(1001L, 1002L),
                document.getUsersToDocuments().stream()
                    .map(userToDocument -> userToDocument.getApplicationUser().getId()).collect(Collectors.toList())
            )
        );
    }

    @Test @DisplayName("Should map Document to DocumentResponse") public void testMapDocumentToDocumentResponse() {
        // Arrange
        DocumentType documentType = new DocumentType();
        documentType.setId(2001L);
        documentType.setName("Document Type");

        UserToDocument user1 =
            UserToDocument.builder().id(1L).applicationUser(ApplicationUser.builder().id(1001L).build()).build();

        UserToDocument user2 =
            UserToDocument.builder().id(2L).applicationUser(ApplicationUser.builder().id(1002L).build()).build();

        Document document = Document.builder().id(1L).name("Test Document").documentType(documentType)
            .usersToDocuments(Arrays.asList(user1, user2)).build();

        // Act
        DocumentResponse documentResponse = documentMapper.documentToDocumentResponse(document);

        // Assert
        assertAll(
            "Grouped assertions for map Document to DocumentResponse",
            () -> assertNotNull(documentResponse),
            () -> assertEquals(1L, documentResponse.id()),
            () -> assertEquals("Test Document", documentResponse.name()),
            () -> assertEquals(2001L, documentResponse.documentTypeId()),
            () -> assertEquals(
                Arrays.asList(1001L, 1002L),
                documentResponse.usersPermissions().stream().map(UserToDocumentResponse::id)
                    .collect(Collectors.toList())
            )
        );
    }

    @Test @DisplayName("Should handle null values correctly") public void testMapWithNullValues() {
        // Act
        Document document = documentMapper.documentRequestToDocument(null);
        DocumentResponse documentResponse = documentMapper.documentToDocumentResponse(null);

        // Assert
        assertNull(document);
        assertNull(documentResponse);
    }
}
