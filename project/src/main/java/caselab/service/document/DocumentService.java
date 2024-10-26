package caselab.service.document;

import caselab.controller.document.facade.payload.UpdateDocumentRequest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.document.version.DocumentPermissionAlreadyGrantedException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.DocumentPermissionNotFoundException;
import caselab.exception.entity.not_found.DocumentTypeNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.document.mapper.DocumentMapper;
import caselab.service.util.DocumentPermissionUtilService;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypeRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final DocumentPermissionRepository documentPermissionRepository;
    private final DocumentMapper documentMapper;

    private final DocumentPermissionUtilService docPermissionService;

    public DocumentResponse createDocument(DocumentRequest documentRequest, ApplicationUser creator) {
        var document = documentMapper.requestToEntity(documentRequest);

        document.setDocumentType(getDocumentTypeById(documentRequest.documentTypeId()));
        document.setDocumentVersions(List.of());
        documentRepository.save(document);

        var creatorPermission = new UserToDocument();
        creatorPermission.setApplicationUser(creator);
        creatorPermission.setDocument(document);
        creatorPermission.setDocumentPermissions(List.of(
            documentPermissionRepository.findDocumentPermissionByName(DocumentPermissionName.CREATOR)
        ));

        creatorPermission = userToDocumentRepository.save(creatorPermission);
        document.setUsersToDocuments(List.of(creatorPermission));

        return documentMapper.entityToResponse(document);
    }

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        var document = documentMapper.requestToEntity(documentRequest);

        document.setDocumentType(getDocumentTypeById(documentRequest.documentTypeId()));
        document.setDocumentVersions(List.of());
        validatePermissions(documentRequest);
        documentRepository.save(document);
        document.setUsersToDocuments(saveUserToDocuments(documentRequest, document));

        return documentMapper.entityToResponse(document);
    }

    private Document getDocumentEntityById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    public DocumentResponse getDocumentById(Long id) {
        return documentMapper.entityToResponse(getDocumentEntityById(id));
    }

    public DocumentResponse getDocumentById(Long id, ApplicationUser user) {
        var document = getDocumentEntityById(id);

        docPermissionService.assertHasPermission(user, document, DocumentPermissionName::any, "Any");
        return documentMapper.entityToResponse(document);
    }

    private Stream<Document> availableDocuments(ApplicationUser user) {
        return user.getUsersToDocuments().stream().map(UserToDocument::getDocument);
    }

    private List<DocumentResponse> toDocumentResponse(Stream<Document> documents) {
        return documents.map(documentMapper::entityToResponse).toList();
    }

    public List<DocumentResponse> getAllDocuments(ApplicationUser user) {
        return toDocumentResponse(availableDocuments(user));
    }

    public List<DocumentResponse> getAllDocuments() {
        return toDocumentResponse(documentRepository.findAll().stream());
    }

    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest updateDocumentRequest, ApplicationUser user) {
        var document = documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));

        docPermissionService.assertHasPermission(user, document, DocumentPermissionName::canEdit, "Edit");
        document.setName(updateDocumentRequest.getName());
        return documentMapper.entityToResponse(documentRepository.save(document));
    }

    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        var document = documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
        var updateDocument = documentMapper.requestToEntity(documentRequest);

        updateDocument.setId(document.getId());
        updateDocument.setDocumentType(getDocumentTypeById(documentRequest.documentTypeId()));
        updateDocument.setDocumentVersions(new ArrayList<>(document.getDocumentVersions()));
        validatePermissions(documentRequest);
        documentRepository.save(updateDocument);
        updateDocument.setUsersToDocuments(saveUserToDocuments(documentRequest, updateDocument));

        return documentMapper.entityToResponse(updateDocument);
    }

    public DocumentResponse grantReadDocumentPermission(
        Long id,
        ApplicationUser user,
        ApplicationUser by
    ) {
        var document = documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
        docPermissionService.assertHasPermission(by, document, DocumentPermissionName::isCreator, "Creator");
        if (!docPermissionService.checkLacksPermission(user, document, DocumentPermissionName::canRead)) {
            throw new DocumentPermissionAlreadyGrantedException("Read");
        }
        var permission = documentPermissionRepository.findDocumentPermissionByName(DocumentPermissionName.READ);
        var userToDocument = new UserToDocument();
        userToDocument.setApplicationUser(user);
        userToDocument.setDocument(document);
        userToDocument.setDocumentPermissions(List.of(permission));
        userToDocumentRepository.save(userToDocument);
        document.getUsersToDocuments().add(userToDocument);
        return documentMapper.entityToResponse(document);
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException(id);
        }
        documentRepository.deleteById(id);
    }

    public void documentToArchive(Long documentId, ApplicationUser user) {
        var document =
            documentRepository.findById(documentId).orElseThrow(() -> new DocumentNotFoundException(documentId));
        docPermissionService.assertHasPermission(user, document, DocumentPermissionName::isCreator, "Archive");
        document.setStatus(DocumentStatus.ARCHIVED);
        documentRepository.save(document);
    }

    private DocumentType getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }

    private void validatePermissions(DocumentRequest documentRequest) {
        for (UserToDocumentRequest userToDocumentRequest : documentRequest.usersPermissions()) {
            applicationUserRepository.findByEmail(userToDocumentRequest.email())
                .orElseThrow(() -> new UserNotFoundException(userToDocumentRequest.email()));
            for (Long id : userToDocumentRequest.documentPermissionIds()) {
                documentPermissionRepository.findById(id)
                    .orElseThrow(() -> new DocumentPermissionNotFoundException(id));
            }
        }
    }

    private List<UserToDocument> saveUserToDocuments(DocumentRequest documentRequest, Document document) {
        List<UserToDocument> userToDocuments = new ArrayList<>();
        for (UserToDocumentRequest userToDocumentRequest : documentRequest.usersPermissions()) {
            userToDocuments.add(createUserToDocument(userToDocumentRequest, document));
        }
        return userToDocumentRepository.saveAll(userToDocuments);
    }

    private UserToDocument createUserToDocument(UserToDocumentRequest userToDocumentRequest, Document document) {
        var user = applicationUserRepository.findByEmail(userToDocumentRequest.email()).orElseThrow();

        var userToDocument =
            userToDocumentRepository.findByApplicationUserIdAndDocumentId(user.getId(), document.getId())
                .orElse(UserToDocument.builder()
                    .applicationUser(user)
                    .document(document)
                    .build());
        userToDocument.setDocumentPermissions(getDocumentPermissions(userToDocumentRequest.documentPermissionIds()));

        return userToDocument;
    }

    private List<DocumentPermission> getDocumentPermissions(List<Long> documentPermissionIds) {
        List<DocumentPermission> documentPermissions = new ArrayList<>();
        for (Long id : documentPermissionIds) {
            documentPermissions.add(documentPermissionRepository.findById(id).orElseThrow());
        }
        return documentPermissions;
    }
}
