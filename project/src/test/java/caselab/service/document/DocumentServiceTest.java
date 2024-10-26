package caselab.service.document;

import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.DocumentPermissionNotFoundException;
import caselab.exception.entity.not_found.DocumentTypeNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.document.mapper.DocumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private DocumentTypesRepository documentTypeRepository;
    @Mock
    private ApplicationUserRepository applicationUserRepository;
    @Mock
    private DocumentPermissionRepository documentPermissionRepository;
    @Mock
    private UserToDocumentRepository userToDocumentRepository;
    @Mock
    private DocumentMapper documentMapper;
    @InjectMocks
    private DocumentService documentService;

    private Document document;
    private DocumentRequest documentRequest;

    @BeforeEach
    public void setup() {
        document = new Document();
        document.setId(1L);
        document.setDocumentType(new DocumentType());
        document.setDocumentVersions(new ArrayList<>());

        documentRequest = DocumentRequest.builder()
            .documentTypeId(1L)
            .usersPermissions(List.of(new UserToDocumentRequest("user1@example.com", List.of(1L))))
            .build();


    }

    @Test
    @DisplayName("Should create document")
    public void createDocumentValid_shouldReturnCreatedDocument() {

        var documentRequest = getDocumentRequest();
        var mappedDocumentRequest = getDocument(documentRequest);
        var documentType = getDocumentType();
        var savedDocument = getDocument(documentRequest);
        var documentResponse = getDocumentResponse(savedDocument);
        var mockPermission = getDocumentPermission();
        var mockUser = getApplicationUser();

        when(applicationUserRepository.findByEmail("user1@example.com"))
            .thenReturn(Optional.of(mockUser));
        when(documentMapper.requestToEntity(Mockito.any(DocumentRequest.class)))
            .thenReturn(mappedDocumentRequest);
        when(documentTypeRepository.findById(documentRequest.documentTypeId()))
            .thenReturn(Optional.of(documentType));
        when(documentRepository.save(Mockito.any(Document.class)))
            .thenReturn(savedDocument);
        when(documentMapper.entityToResponse(Mockito.any(Document.class)))
            .thenReturn(documentResponse);
        when(documentPermissionRepository.findById(1L))
            .thenReturn(Optional.of(mockPermission));


        var resultOfCreating = documentService.createDocument(documentRequest);

        assertAll(
            "Grouped assertions for created document",
            () -> assertThat(resultOfCreating.id()).isNotNull(),
            () -> assertThat(resultOfCreating.name()).isEqualTo(documentRequest.name()),
            () -> assertThat(resultOfCreating.documentTypeId()).isEqualTo(documentRequest.documentTypeId()),
            () -> assertThat(resultOfCreating.documentVersionIds()).isEmpty(),
            () -> assertThat(resultOfCreating.usersPermissions()).isNotNull()
        );
    }

    @Test
    @DisplayName("Create document for non-existent user")
    public void createDocumentForUserNotExist_shouldThrowEntityNotFoundException() {
        var documentRequest = getDocumentRequest();

        var mappedDocumentRequest = getDocument(documentRequest);
        var documentType = getDocumentType();
        var mockUser = getApplicationUser();

        when(applicationUserRepository.findByEmail("user1@example.com"))
            .thenReturn(Optional.of(mockUser));
        when(documentMapper.requestToEntity(Mockito.any(DocumentRequest.class)))
            .thenReturn(mappedDocumentRequest);
        when(documentTypeRepository.findById(documentRequest.documentTypeId()))
            .thenReturn(Optional.of(documentType));
        when(applicationUserRepository.findByEmail(documentRequest.usersPermissions().get(0).email()))
            .thenThrow(new UserNotFoundException(documentRequest.usersPermissions().get(0).email()));

        assertThrows(UserNotFoundException.class, () -> documentService.createDocument(documentRequest));
    }

    @Test
    @DisplayName("Should throw DocumentPermissionNotFoundException when creating document with invalid permissions")
    public void createDocumentInvalidPermission_shouldThrowDocumentPermissionNotFoundException() {
        var documentRequest = getDocumentRequest();
        var mockUser = getApplicationUser();
        var documentType = getDocumentType();


        when(documentTypeRepository.findById(documentRequest.documentTypeId()))
            .thenReturn(Optional.of(documentType));
        when(applicationUserRepository.findByEmail("user1@example.com"))
            .thenReturn(Optional.of(mockUser));
        when(documentPermissionRepository.findById(1L))
            .thenThrow(new DocumentPermissionNotFoundException(1L));
        when(documentMapper.requestToEntity(documentRequest))
            .thenReturn(getDocument(documentRequest));

        assertThrows(DocumentPermissionNotFoundException.class, () -> documentService.createDocument(documentRequest));
    }
    @Test
    @DisplayName("Should get document by ID")
    public void getDocumentByIdValid_shouldReturnDocumentResponse() {
        Long documentId = 1L;
        var document = getDocument(documentRequest);
        var documentResponse = getDocumentResponse(document);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(documentMapper.entityToResponse(document)).thenReturn(documentResponse);

        var result = documentService.getDocumentById(documentId);

        assertAll(
            "Grouped assertions for document by ID",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.id()).isEqualTo(documentId),
            () -> assertThat(result.name()).isEqualTo(documentResponse.name())
        );
    }

    @Test
    @DisplayName("Should get all documents")
    public void getAllDocumentsValid_shouldReturnListOfDocumentResponses() {
        var document1 = getDocument(documentRequest);
        var document2 = getDocument(documentRequest);
        var documents = List.of(document1, document2);
        var documentResponses = documents.stream()
            .map(documentMapper::entityToResponse)
            .toList();

        when(documentRepository.findAll()).thenReturn(documents);
        when(documentMapper.entityToResponse(any(Document.class)))
            .thenReturn(documentResponses.get(0), documentResponses.get(1));

        var result = documentService.getAllDocuments();

        assertAll(
            "Grouped assertions for all documents",
            () -> assertThat(result).isNotNull(),
            () -> assertThat(result.size()).isEqualTo(2),
            () -> assertThat(result.get(0)).isEqualTo(documentResponses.get(0)),
            () -> assertThat(result.get(1)).isEqualTo(documentResponses.get(1))
        );
    }

    @Test
    @DisplayName("Should update document")
    public void updateDocumentValid_shouldReturnUpdatedDocumentResponse() {
        Long documentId = 1L;
        var document = getDocument(documentRequest);
        var updatedDocumentRequest = DocumentRequest.builder()
            .documentTypeId(1L)
            .name("Updated Document")
            .usersPermissions(List.of(new UserToDocumentRequest("user1@example.com", List.of(1L))))
            .build();
        var updatedDocument = getDocument(updatedDocumentRequest);
        var documentResponse = getDocumentResponse(updatedDocument);
        var mockUser = getApplicationUser();
        var mockPermission = getDocumentPermission();

        when(applicationUserRepository.findByEmail("user1@example.com"))
            .thenReturn(Optional.of(mockUser));
        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(documentMapper.requestToEntity(updatedDocumentRequest)).thenReturn(updatedDocument);
        when(documentTypeRepository.findById(updatedDocumentRequest.documentTypeId())).thenReturn(Optional.of(document.getDocumentType()));
        when(documentRepository.save(any(Document.class))).thenReturn(updatedDocument);
        when(documentMapper.entityToResponse(updatedDocument)).thenReturn(documentResponse);
        when(documentPermissionRepository.findById(1L))
            .thenReturn(Optional.of(mockPermission));
        var result = documentService.updateDocument(documentId, updatedDocumentRequest);
        assertAll(
            "Grouped assertions for update document",
            ()-> assertThat(result).isNotNull(),
            ()-> assertThat(result.name()).isEqualTo("Updated Document")
        );
    }
    @Test
    @DisplayName("Should throw DocumentNotFoundException when updating non-existent document")
    public void updateDocumentNotFound_shouldThrowDocumentNotFoundException() {
        Long documentId = 999L;
        var updatedDocumentRequest = getDocumentRequest();

        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        assertThrows(DocumentNotFoundException.class, () -> documentService.updateDocument(documentId, updatedDocumentRequest));
    }
    @Test
    @DisplayName("Should throw DocumentTypeNotFoundException when updating document with non-existent type")
    public void updateDocumentInvalidType_shouldThrowDocumentTypeNotFoundException() {
        Long documentId = 1L;

        var documentRequest = getDocumentRequest();

        var document = getDocument(documentRequest);

        var updatedDocumentRequest = getDocumentRequest();

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(documentTypeRepository.findById(updatedDocumentRequest.documentTypeId()))
            .thenThrow(new DocumentTypeNotFoundException(updatedDocumentRequest.documentTypeId()));
        when(documentMapper.requestToEntity(updatedDocumentRequest))
            .thenReturn(getDocument(updatedDocumentRequest));

        assertThrows(DocumentTypeNotFoundException.class, () -> documentService.updateDocument(documentId, updatedDocumentRequest));
    }

    @Test
    @DisplayName("Should delete document by ID")
    public void deleteDocumentValid_shouldDeleteDocument() {
        Long documentId = 1L;

        when(documentRepository.existsById(documentId)).thenReturn(true);

        documentService.deleteDocument(documentId);

        verify(documentRepository).deleteById(documentId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent document")
    public void deleteDocumentNotFound_shouldThrowDocumentNotFoundException() {
        Long documentId = 1L;

        when(documentRepository.existsById(documentId)).thenReturn(false);

        assertThrows(DocumentNotFoundException.class, () -> documentService.deleteDocument(documentId));
    }



    private DocumentRequest getDocumentRequest() {
        return DocumentRequest.builder()
            .documentTypeId(1L)
            .name("test")
            .usersPermissions(List.of(new UserToDocumentRequest("user1@example.com", List.of(1L))))
            .build();
    }

    private Document getDocument(DocumentRequest request) {
        return Document.builder()
            .id(1L)
            .documentType(getDocumentType())
            .name(request.name())
            .documentVersions(new ArrayList<>())
            .build();
    }
    private ApplicationUser getApplicationUser() {
        return ApplicationUser.builder()
            .id(1L)
            .email("user1@example.com")
            .build();
    }

    private DocumentType getDocumentType() {
        return DocumentType.builder().id(1L).name("test").build();
    }

    private DocumentResponse getDocumentResponse(Document document) {
        return DocumentResponse.builder()
            .id(document.getId())
            .documentTypeId(document.getDocumentType().getId())
            .name(document.getName())
            .documentVersionIds(new ArrayList<>())
            .usersPermissions(new ArrayList<>())
            .build();
    }
    private DocumentPermission getDocumentPermission(){
        return DocumentPermission.builder()
            .id(1L)
            .name(DocumentPermissionName.CREATOR)
            .build();
    }

}
