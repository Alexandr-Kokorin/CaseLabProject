package caselab.service.version;

import caselab.controller.version.payload.AttributeValuePair;
import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.attribute.value.AttributeValue;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.AttributeValueRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.domain.storage.FileStorage;
import caselab.exception.document.version.MissingAttributesException;
import caselab.exception.document.version.MissingDocumentPermissionException;
import caselab.exception.entity.not_found.AttributeNotFoundException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.DocumentVersionNotFoundException;
import caselab.service.users.ApplicationUserService;
import caselab.service.version.mapper.DocumentVersionMapper;
import caselab.service.version.mapper.DocumentVersionUpdater;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentVersionService {
    private final DocumentVersionRepository documentVersionRepository;
    private final ApplicationUserService userService;
    private final DocumentRepository documentRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentVersionUpdater documentVersionUpdater;
    private final FileStorage documentVersionStorage;

    private boolean checkLacksPermission(
        ApplicationUser user,
        Document document,
        Predicate<DocumentPermissionName> permission
    ) {
        Optional<UserToDocument> userToDocument = userToDocumentRepository.findByApplicationUserIdAndDocumentId(
            user.getId(),
            document.getId()
        );
        return userToDocument.map(toDocument -> toDocument
            .getDocumentPermissions()
            .stream()
            .map(DocumentPermission::getName)
            .noneMatch(permission)).orElse(true);
    }

    private DocumentVersionResponse hideInaccessibleFields(
        DocumentVersionResponse response,
        ApplicationUser user,
        Document document
    ) {
        if (checkLacksPermission(user, document, DocumentPermissionName::canRead)) {
            response.setAttributes(null);
            response.setContentName(null);
        }

        return response;
    }

    private void checkMandatoryAttributesPresent(CreateDocumentVersionRequest body, Document document) {
        Set<Long> presentAttributesIds = body
            .attributes()
            .stream()
            .map(AttributeValuePair::attributeId).collect(Collectors.toSet());

        Stream<Long> mandatoryAttributesIds = document
            .getDocumentType()
            .getDocumentTypesToAttributes()
            .stream()
            .filter(dtta -> !dtta.getIsOptional())
            .map(DocumentTypeToAttribute::getAttribute)
            .map(Attribute::getId);

        if (!mandatoryAttributesIds.allMatch(presentAttributesIds::contains)) {
            throw new MissingAttributesException();
        }
    }

    public DocumentVersionResponse createDocumentVersion(
        CreateDocumentVersionRequest body,
        MultipartFile file,
        Authentication auth
    ) {
        ApplicationUser user = userService.findUserByAuthentication(auth);
        return createDocumentVersion(body, file, user);
    }

    public DocumentVersionResponse createDocumentVersion(
        CreateDocumentVersionRequest body,
        MultipartFile file,
        ApplicationUser user
    ) {

        Document document = documentRepository
            .findById(body.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(body.documentId()));

        if (checkLacksPermission(user, document, DocumentPermissionName::canEdit)) {
            throw new MissingDocumentPermissionException(DocumentPermissionName.EDIT.name());
        }
        checkMandatoryAttributesPresent(body, document);

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setName(body.name());
        documentVersion.setCreatedAt(OffsetDateTime.now());
        documentVersion.setContentName(
            documentVersionStorage.put(file)
        );
        documentVersion.setDocument(document);

        var attributeValues = body
            .attributes()
            .stream()
            .map(
                pair -> {
                    var value = new AttributeValue();
                    value.setAppValue(pair.value());
                    value.setAttribute(
                        attributeRepository
                            .findById(pair.attributeId())
                            .orElseThrow(() -> new AttributeNotFoundException(pair.attributeId()))
                    );
                    value.setDocumentVersion(documentVersion);
                    return value;
                }
            ).toList();

        var saved = documentVersionRepository.save(documentVersion);
        attributeValueRepository.saveAll(attributeValues);

        return documentVersionMapper.map(saved);
    }

    public DocumentVersionResponse getDocumentVersionById(Long id, Authentication auth) {
        ApplicationUser user = userService.findUserByAuthentication(auth);
        return getDocumentVersionById(id, user);
    }

    public DocumentVersionResponse getDocumentVersionById(Long id, ApplicationUser user) {
        DocumentVersion documentVersion = documentVersionRepository.findById(id).orElseThrow(
            () -> new DocumentVersionNotFoundException(id)
        );

        userToDocumentRepository.findByApplicationUserIdAndDocumentId(
            user.getId(), documentVersion.getDocument().getId()
        ).orElseThrow(
            () -> new MissingDocumentPermissionException("Any")
        );

        return hideInaccessibleFields(
            documentVersionMapper.map(documentVersion),
            user,
            documentVersion.getDocument()
        );
    }

    public List<DocumentVersionResponse> getDocumentVersions(Authentication auth) {
        ApplicationUser user = userService.findUserByAuthentication(auth);
        Stream<DocumentVersion> documentVersions = user
            .getUsersToDocuments()
            .stream()
            .map(UserToDocument::getDocument)
            .collect(Collectors.toSet())  // Чтобы убрать повторяющиеся документы
            .stream()
            .flatMap(doc -> doc.getDocumentVersions().stream());

        return documentVersions
            .map(docV -> hideInaccessibleFields(
                documentVersionMapper.map(docV),
                user,
                docV.getDocument()
            ))
            .toList();
    }

    public InputStream getDocumentVersionContent(Long id, Authentication auth) {
        ApplicationUser user = userService.findUserByAuthentication(auth);

        DocumentVersion documentVersion = documentVersionRepository.findById(id).orElseThrow(
            () -> new DocumentVersionNotFoundException(id)
        );

        if (checkLacksPermission(user, documentVersion.getDocument(), DocumentPermissionName::canRead)) {
            throw new MissingDocumentPermissionException(DocumentPermissionName.READ.name());
        }

        return documentVersionStorage.get(documentVersion.getContentName());
    }

    public DocumentVersionResponse updateDocumentVersion(
        Long id,
        UpdateDocumentVersionRequest body,
        Authentication auth
    ) {
        ApplicationUser user = userService.findUserByAuthentication(auth);
        DocumentVersion documentVersion = documentVersionRepository.findById(id).orElseThrow(
            () -> new DocumentVersionNotFoundException(id)
        );
        if (checkLacksPermission(user, documentVersion.getDocument(), DocumentPermissionName::canEdit)) {
            throw new MissingDocumentPermissionException(DocumentPermissionName.EDIT.name());
        }
        documentVersionUpdater.update(body, documentVersion);
        var saved = documentVersionRepository.save(documentVersion);
        return hideInaccessibleFields(
            documentVersionMapper.map(saved),
            user,
            saved.getDocument()
        );
    }
}
