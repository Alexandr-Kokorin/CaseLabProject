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
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.GlobalPermissionName;
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
import caselab.exception.status.StatusIncorrectForUpdateDocumentVersionException;
import caselab.service.users.ApplicationUserService;
import caselab.service.util.DocumentPermissionUtilService;
import caselab.service.util.PageUtil;
import caselab.service.util.UserPermissionUtil;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentVersionService {

    private final ApplicationUserService userService;  // TODO: сменить на UserFromAuthenticationUtilService
    private final DocumentPermissionUtilService documentPermissionUtilService;

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentVersionUpdater documentVersionUpdater;
    private final FileStorage documentVersionStorage;

    private boolean checkLacksPermission(// TODO: сменить на DocumentPermissionUtilService
        ApplicationUser user,
        Document document,
        Predicate<DocumentPermissionName> permission
    ) {
        Optional<UserToDocument> userToDocument = userToDocumentRepository.findByApplicationUserIdAndDocumentId(
            user.getId(),
            document.getId()
        );
        return userToDocument
            .filter(it -> it.getDocumentPermissions()
                .stream()
                .map(DocumentPermission::getName)
                .anyMatch(permission))
            .isPresent();
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

    // На данный момент (на данной версии приложения) разрешение READ имеют лишь те пользователи,
    // которым этот документ был отослан на подпись.
    // Эти пользователи могут видеть документ и все его версии до тех пор, пока не будет создан новый черновик -
    // в этом случае доступ READ теряется.
    // Данный метод отнимает у всех таких пользователей право на чтение
    private void clearReaders(Document document) {
        document
            .getUsersToDocuments()
            .stream()
            .map(UserToDocument::getApplicationUser)
            .filter(
                user -> checkLacksPermission(user, document, x -> x == DocumentPermissionName.READ)
            )
            .map(user -> userToDocumentRepository.findByApplicationUserIdAndDocumentId(user.getId(), document.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(
                utd -> utd.getDocumentPermissions().stream().anyMatch(
                    per -> per.getName() == DocumentPermissionName.READ
                )
            )
            .forEach(
                userToDocumentRepository::delete
            );
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

        Document document = documentRepository.findById(body.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(body.documentId()));

        if (checkLacksPermission(user, document, DocumentPermissionName::canEdit)) {
            throw new MissingDocumentPermissionException(DocumentPermissionName.EDIT.name());
        }
        checkMandatoryAttributesPresent(body, document);

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setName(body.name());
        documentVersion.setCreatedAt(OffsetDateTime.now());
        if (file != null) {
            documentVersion.setContentName(documentVersionStorage.put(file));
        }
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
        saved.setAttributeValues(attributeValues);

        DocumentVersionResponse versionResponse;

        try {
            versionResponse = documentVersionMapper.map(saved);
        } catch (Exception e) {
            documentVersionStorage.delete(documentVersion.getContentName());
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        clearReaders(document);  // TODO: проверить в тесте контроллера, что это работает
        return versionResponse;
    }

    public DocumentVersionResponse getDocumentVersionById(Long id, Authentication auth) {
        ApplicationUser user = userService.findUserByAuthentication(auth);
        return getDocumentVersionById(id, user);
    }

    public DocumentVersionResponse getDocumentVersionById(Long id, ApplicationUser user) {
        DocumentVersion documentVersion = documentVersionRepository.findById(id)
            .orElseThrow(() -> new DocumentVersionNotFoundException(id));

        userToDocumentRepository.findByApplicationUserIdAndDocumentId(
            user.getId(), documentVersion.getDocument().getId()
        ).orElseThrow(() -> new MissingDocumentPermissionException("Any"));

        return hideInaccessibleFields(
            documentVersionMapper.map(documentVersion),
            user,
            documentVersion.getDocument()
        );
    }

    public DocumentVersionResponse getDocumentVersionById(Long id) {
        DocumentVersion documentVersion = documentVersionRepository.findById(id)
            .orElseThrow(() -> new DocumentVersionNotFoundException(id));

        return documentVersionMapper.map(documentVersion);
    }

    public Page<DocumentVersionResponse> getDocumentVersionsByDocumentId(
        Long id,
        Integer pageNum,
        Integer pageSize,
        String sortStrategy,
        Authentication auth
    ) {
        ApplicationUser user = userService.findUserByAuthentication(auth);

        Document document = documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));

        if (!checkLacksPermission(user, document, DocumentPermissionName::isCreator)) {
            UserPermissionUtil.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        }

        Page<DocumentVersion> versions = documentVersionRepository.findByDocumentId(
            PageUtil.toPageable(pageNum, pageSize, Sort.by("createdAt"), sortStrategy),
            id
        );

        Page<DocumentVersionResponse> responsePage = versions
            .map(docV -> hideInaccessibleFields(
                documentVersionMapper.map(docV),
                user,
                docV.getDocument()
            ));

        return responsePage;
    }

    public InputStream getDocumentVersionContent(Long id, Authentication auth) {
        ApplicationUser user = userService.findUserByAuthentication(auth);

        DocumentVersion documentVersion = documentVersionRepository.findById(id)
            .orElseThrow(() -> new DocumentVersionNotFoundException(id));

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
        DocumentVersion documentVersion = documentVersionRepository.findById(id)
            .orElseThrow(() -> new DocumentVersionNotFoundException(id));

        if (checkLacksPermission(user, documentVersion.getDocument(), DocumentPermissionName::canEdit)) {
            throw new MissingDocumentPermissionException(DocumentPermissionName.EDIT.name());
        }
        documentPermissionUtilService.assertHasDocumentStatus(
            documentVersion.getDocument(),
            List.of(DocumentStatus.DRAFT),
            new StatusIncorrectForUpdateDocumentVersionException()
        );

        documentVersionUpdater.update(body, documentVersion);
        var saved = documentVersionRepository.save(documentVersion);
        return hideInaccessibleFields(
            documentVersionMapper.map(saved),
            user,
            saved.getDocument()
        );
    }
}
