package caselab.service.document.version;

import caselab.controller.document.version.payload.AttributeValueRequest;
import caselab.controller.document.version.payload.CreateDocumentVersionRequest;
import caselab.controller.document.version.payload.DocumentVersionResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.attribute.value.AttributeValue;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.enums.DocumentPermissionName;
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
import caselab.service.document.version.mapper.DocumentVersionMapper;
import caselab.service.util.DocumentUtilService;
import caselab.service.util.PageUtil;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
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

    private final UserUtilService userUtilService;
    private final DocumentUtilService documentUtilService;

    private final DocumentVersionRepository documentVersionRepository;
    private final DocumentRepository documentRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    private final DocumentVersionMapper documentVersionMapper;
    private final FileStorage documentVersionStorage;

    private DocumentVersionResponse hideInaccessibleFields(
        DocumentVersionResponse response,
        ApplicationUser user,
        Document document
    ) {
        if (documentUtilService.checkLacksPermission(user, document, DocumentPermissionName::canRead)) {
            response.setAttributes(null);
            response.setContentName(null);
        }

        return response;
    }

    private boolean checkLacksPermission(
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
        Set<Long> presentAttributesIds = body.attributes().stream()
            .map(AttributeValueRequest::attributeId)
            .collect(Collectors.toSet());

        Set<Long> mandatoryAttributesIds = document.getDocumentType()
            .getDocumentTypesToAttributes()
            .stream()
            .filter(dtta -> !dtta.getIsOptional())
            .map(DocumentTypeToAttribute::getAttribute)
            .map(Attribute::getId)
            .collect(Collectors.toSet());

        if (!presentAttributesIds.containsAll(mandatoryAttributesIds)) {
            throw new MissingAttributesException();
        }
    }

    public DocumentVersionResponse createDocumentVersion(
        CreateDocumentVersionRequest body,
        MultipartFile file,
        Authentication auth
    ) {
        ApplicationUser user = userUtilService.findUserByAuthentication(auth);
        return createDocumentVersion(body, file, user);
    }

    public DocumentVersionResponse createDocumentVersion(
        CreateDocumentVersionRequest body,
        MultipartFile file,
        ApplicationUser user
    ) {
        var document = findDocumentById(body.documentId());

        documentUtilService.assertHasPermission(user, document, DocumentPermissionName::canEdit, "Edit");

        Optional.ofNullable(body.attributes()).ifPresent(attributes -> checkMandatoryAttributesPresent(body, document));

        DocumentVersion documentVersion = new DocumentVersion();
        documentVersion.setName(generateVersionName(document));
        documentVersion.setCreatedAt(OffsetDateTime.now());
        documentVersion.setDocument(document);

        setDocumentContentName(documentVersion, file, document);

        List<AttributeValue> attributeValues = createAttributeValues(body, document, documentVersion);

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

        clearReaders(document);
        return versionResponse;
    }

    private String generateVersionName(Document document) {
        return String.format("%s v%d", document.getName(), document.getDocumentVersions().size() + 1);
    }

    private void setDocumentContentName(DocumentVersion documentVersion, MultipartFile file, Document document) {
        if (Objects.nonNull(file)) {
            String contentName = file.isEmpty() ? null : documentVersionStorage.put(file);
            documentVersion.setContentName(contentName);
        } else if (!document.getDocumentVersions().isEmpty()) {
            var newestDocVersion = document.getDocumentVersions().getFirst();
            documentVersion.setContentName(newestDocVersion.getContentName());
        }
    }

    private List<AttributeValue> createAttributeValues(
        CreateDocumentVersionRequest body,
        Document document,
        DocumentVersion version
    ) {
        if (Objects.nonNull(body.attributes())) {
            return body.attributes()
                .stream()
                .map(pair -> createAttributeValueFromPair(pair, version))
                .toList();
        } else {
            DocumentVersion newestVersion = document.getDocumentVersions().getFirst();
            return newestVersion.getAttributeValues()
                .stream()
                .map(pair -> createAttributeValueFromExisting(pair, version))
                .toList();
        }
    }

    private AttributeValue createAttributeValueFromPair(AttributeValueRequest pair, DocumentVersion documentVersion) {
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

    private AttributeValue createAttributeValueFromExisting(AttributeValue pair, DocumentVersion documentVersion) {
        var value = new AttributeValue();
        value.setAppValue(pair.getAppValue());
        value.setAttribute(pair.getAttribute());
        value.setDocumentVersion(documentVersion);
        return value;
    }

    public DocumentVersionResponse getDocumentVersionById(Long id, Authentication auth) {
        ApplicationUser user = userUtilService.findUserByAuthentication(auth);
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
        ApplicationUser user = userUtilService.findUserByAuthentication(auth);

        var document = findDocumentById(id);

        if (documentUtilService.checkLacksPermission(user, document, DocumentPermissionName::isCreator)) {
            userUtilService.checkUserGlobalPermission(user, GlobalPermissionName.ADMIN);
        }

        Page<DocumentVersion> versions = documentVersionRepository.findByDocumentId(
            PageUtil.toPageable(pageNum, pageSize, Sort.by("createdAt"), sortStrategy),
            id
        );

        return versions
            .map(docV -> hideInaccessibleFields(
                documentVersionMapper.map(docV),
                user,
                docV.getDocument()
            ));
    }

    public InputStream getDocumentVersionContent(Long id, Authentication auth) {
        ApplicationUser user = userUtilService.findUserByAuthentication(auth);

        DocumentVersion documentVersion = documentVersionRepository.findById(id)
            .orElseThrow(() -> new DocumentVersionNotFoundException(id));

        documentUtilService.assertHasPermission(
            user, documentVersion.getDocument(),
            DocumentPermissionName::canRead, "Read"
        );

        return documentVersionStorage.get(documentVersion.getContentName());
    }

    private Document findDocumentById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
    }
}
