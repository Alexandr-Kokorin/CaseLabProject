package caselab.service.document;

import caselab.controller.document.facade.payload.PatchDocumentRequest;
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
import caselab.service.util.DocumentUtilService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@SuppressWarnings("MultipleStringLiterals")
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentUtilService documentUtilService;

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypeRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final DocumentPermissionRepository documentPermissionRepository;

    private final DocumentMapper documentMapper;

    public DocumentResponse createDocument(DocumentRequest documentRequest, ApplicationUser creator) {
        var document = documentMapper.requestToEntity(documentRequest);

        document.setDocumentType(findDocumentTypeById(documentRequest.documentTypeId()));
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

    public DocumentResponse getDocumentById(Long id, ApplicationUser user) {
        var document = findDocumentById(id);

        documentUtilService.assertHasPermission(user, document, DocumentPermissionName::any, "Any");
        return documentMapper.entityToResponse(document);
    }

    public List<DocumentResponse> getAllDocuments(ApplicationUser user) {
        return toDocumentResponse(
            user
                .getUsersToDocuments()
                .stream()
                .map(UserToDocument::getDocument)
        );
    }

    public List<DocumentResponse> getAllDocuments() {
        return toDocumentResponse(
            documentRepository.findAll().stream()
        );
    }

    private List<DocumentResponse> toDocumentResponse(Stream<Document> documents) {
        return documents.map(documentMapper::entityToResponse).toList();
    }

    public DocumentResponse updateDocument(Long id, UpdateDocumentRequest updateRequest, ApplicationUser user) {
        var document = findDocumentById(id);

        validateDocumentForUpdate(document, user);

        document.setDocumentType(findDocumentTypeById(updateRequest.getDocumentTypeId()));
        document.setName(updateRequest.getName());
        document.setStatus(DocumentStatus.DRAFT);
        return documentMapper.entityToResponse(documentRepository.save(document));
    }

    public DocumentResponse patchDocument(Long id, PatchDocumentRequest request, ApplicationUser user) {
        var document = findDocumentById(id);

        validateDocumentForUpdate(document, user);

        documentMapper.patchDocumentFromPatchRequest(document, request);

        var documentTypeId = request.getDocumentTypeId();
        if (Objects.nonNull(documentTypeId)) {
            var documentType = findDocumentTypeById(documentTypeId);
            document.setDocumentType(documentType);
        }

        document.setStatus(DocumentStatus.DRAFT);

        return documentMapper.entityToResponse(documentRepository.save(document));
    }

    private void validateDocumentForUpdate(Document document, ApplicationUser user) {
        documentUtilService.assertHasPermission(user, document, DocumentPermissionName::canEdit, "Edit");

        documentUtilService.assertHasDocumentStatus(
            document,
            List.of(DocumentStatus.DRAFT, DocumentStatus.SIGNATURE_REJECTED,
                DocumentStatus.VOTING_REJECTED, DocumentStatus.ARCHIVED
            ),
            new StatusIncorrectForUpdateDocumentException()
        );
    }

    public DocumentResponse grantReadDocumentPermission(
        Long id,
        ApplicationUser user,
        ApplicationUser by
    ) {
        var document = findDocumentById(id);
        documentUtilService.assertHasPermission(by, document, DocumentPermissionName::isCreator, "Creator");
        if (!documentUtilService.checkLacksPermission(user, document, DocumentPermissionName::canRead)) {
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
        var document = findDocumentById(documentId);

        documentUtilService.assertHasPermission(user, document, DocumentPermissionName::isCreator, "Creator");
        documentUtilService.assertHasDocumentStatus(
            document,
            List.of(DocumentStatus.DRAFT, DocumentStatus.SIGNATURE_REJECTED, DocumentStatus.SIGNATURE_ACCEPTED,
                DocumentStatus.VOTING_REJECTED, DocumentStatus.VOTING_ACCEPTED
            ),
            new StatusIncorrectForDeleteDocumentException()
        );

        document.setStatus(DocumentStatus.ARCHIVED);
        documentRepository.save(document);
    }

    private DocumentType findDocumentTypeById(Long id) {
        return documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }

    private Document findDocumentById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
    }
}
