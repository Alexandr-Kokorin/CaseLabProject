package caselab.service.document;

import caselab.controller.document.facade.payload.UpdateDocumentRequest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.document.version.DocumentPermissionAlreadyGrantedException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.DocumentTypeNotFoundException;
import caselab.exception.status.StatusIncorrectForDeleteDocumentException;
import caselab.exception.status.StatusIncorrectForUpdateDocumentException;
import caselab.service.document.mapper.DocumentMapper;
import caselab.service.util.DocumentPermissionUtilService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@SuppressWarnings("MultipleStringLiterals")
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypeRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final DocumentPermissionRepository documentPermissionRepository;
    private final DocumentMapper documentMapper;

    private final DocumentPermissionUtilService docPermissionService;

    public DocumentResponse createDocument(DocumentRequest documentRequest, ApplicationUser creator) {
        var document = documentMapper.requestToEntity(documentRequest);

        document.setDocumentType(getDocumentTypeById(documentRequest.documentTypeId()));
        document.setStatus(DocumentStatus.DRAFT);
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

    private Document getDocumentEntityById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
    }

    public DocumentResponse getDocumentById(Long id, ApplicationUser user) {
        var document = getDocumentEntityById(id);

        docPermissionService.assertHasPermission(user, document, DocumentPermissionName::any, "Any");
        return documentMapper.entityToResponse(document);
    }

    private List<DocumentResponse> toDocumentResponse(Stream<Document> documents) {
        return documents.map(documentMapper::entityToResponse).toList();
    }

    public List<DocumentResponse> getAllDocuments(ApplicationUser user) {
        return toDocumentResponse(
            user
                .getUsersToDocuments()
                .stream()
                .map(UserToDocument::getDocument)
        );
    }

    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest updateDocumentRequest, ApplicationUser user) {
        var document = getDocumentEntityById(id);

        docPermissionService.assertHasPermission(user, document, DocumentPermissionName::canEdit, "Edit");
        docPermissionService.assertHasDocumentStatus(
            document,
            List.of(DocumentStatus.DRAFT, DocumentStatus.SIGNATURE_REJECTED,
                DocumentStatus.VOTING_REJECTED, DocumentStatus.ARCHIVED),
            new StatusIncorrectForUpdateDocumentException());

        document.setName(updateDocumentRequest.getName());
        document.setStatus(DocumentStatus.DRAFT);
        return documentMapper.entityToResponse(documentRepository.save(document));
    }

    public DocumentResponse grantReadDocumentPermission(
        Long id,
        ApplicationUser user,
        ApplicationUser by
    ) {
        var document = getDocumentEntityById(id);
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

    public void documentToArchive(Long documentId, ApplicationUser user) {
        var document = getDocumentEntityById(documentId);

        docPermissionService.assertHasPermission(user, document, DocumentPermissionName::isCreator, "Creator");
        docPermissionService.assertHasDocumentStatus(
            document,
            List.of(DocumentStatus.DRAFT, DocumentStatus.SIGNATURE_REJECTED, DocumentStatus.SIGNATURE_ACCEPTED,
                DocumentStatus.VOTING_REJECTED, DocumentStatus.VOTING_ACCEPTED),
            new StatusIncorrectForDeleteDocumentException());

        document.setStatus(DocumentStatus.ARCHIVED);
        documentRepository.save(document);
    }

    private DocumentType getDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }
}
