package caselab.service.document.facade;

import caselab.controller.document.facade.payload.CreateDocumentRequest;
import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.controller.document.facade.payload.UpdateDocumentRequest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.version.payload.CreateDocumentVersionRequest;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.service.document.DocumentService;
import caselab.service.document.version.DocumentVersionService;
import caselab.service.signature.mapper.SignatureMapper;
import caselab.service.util.PageUtil;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentFacadeService {

    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;
    private final UserUtilService userUtilService;

    private final DocumentVersionRepository documentVersionRepository;
    private final SignatureRepository signatureRepository;
    private final ApplicationUserRepository userRepository;

    private final SignatureMapper signatureMapper;

    private DocumentFacadeResponse enrichResponse(
        DocumentResponse documentResponse,
        ApplicationUser user
    ) {
        var latestVersionResponse = documentVersionService.getDocumentVersionById(
            documentResponse.documentVersionIds().getLast(), user
        );

        var signature = findSignatureByUserAndDocumentVersion(
            user.getId(),
            latestVersionResponse.getId()
        );

        return new DocumentFacadeResponse(documentResponse, latestVersionResponse, signature.orElse(null));
    }

    private DocumentFacadeResponse enrichResponse(
        DocumentResponse documentResponse
    ) {
        var latestVersionResponse = documentVersionService.getDocumentVersionById(
            documentResponse.documentVersionIds().getLast()
        );

        // TODO: Переделать логику, что бы админ получал список всех подписей

        return new DocumentFacadeResponse(documentResponse, latestVersionResponse, null);
    }

    private Optional<SignatureResponse> findSignatureByUserAndDocumentVersion(Long userId, Long documentVersionId) {
        var user = userRepository.findById(userId);
        var documentVersion = documentVersionRepository.findById(documentVersionId);

        return user.flatMap(applicationUser -> documentVersion
            .flatMap(version -> signatureRepository.findByApplicationUserAndDocumentVersion(
                    applicationUser,
                    version
                )
                .map(signatureMapper::entityToResponse)));
    }

    public DocumentFacadeResponse getDocumentById(Long id, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        var documentResponse = documentService.getDocumentById(id, user);
        return enrichResponse(documentResponse, user);
    }

    public DocumentFacadeResponse createDocument(CreateDocumentRequest body, MultipartFile file, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        var documentRequest = DocumentRequest.builder()
            .documentTypeId(body.getDocumentTypeId())
            .name(body.getName())
            .build();
        var documentResponse = documentService.createDocument(documentRequest, user);

        var documentVersionRequest = CreateDocumentVersionRequest.builder()
            .documentId(documentResponse.id())
            .name(body.getVersionName())
            .attributes(body.getAttributes())
            .build();
        var latestVersion = documentVersionService.createDocumentVersion(documentVersionRequest, file, user);
        documentResponse.documentVersionIds().add(latestVersion.getId());
        return new DocumentFacadeResponse(documentResponse, latestVersion, null);

    }

    public Page<DocumentFacadeResponse> getAllDocuments(
        Integer pageNum,
        Integer pageSize,
        String sortStrategy,
        Authentication auth
    ) {
        var user = userUtilService.findUserByAuthentication(auth);

        Optional<GlobalPermissionName> adminCheck = user.getGlobalPermissions().stream()
            .map(GlobalPermission::getName)
            .filter(it -> it.equals(GlobalPermissionName.ADMIN))
            .findFirst();

        PageRequest pageable = PageUtil.toPageable(pageNum, pageSize);

        if (adminCheck.isPresent()) {
            return getAllAdminDocuments(pageable, sortStrategy, user);
        }

        return getAllUserDocuments(pageable, sortStrategy, user);
    }

    private Page<DocumentFacadeResponse> getAllAdminDocuments(
        Pageable pageable,
        String sortStrategy,
        ApplicationUser user
    ) {

        List<DocumentResponse> allDocuments = documentService.getAllDocuments();

        List<DocumentFacadeResponse> responseList = allDocuments.stream()
            .map(this::enrichResponse)
            .sorted(Comparator.comparing(it -> it.getLatestVersion().getCreatedAt()))
            .toList();

        return toPage(responseList, pageable, sortStrategy);
    }

    private Page<DocumentFacadeResponse> getAllUserDocuments(
        Pageable pageable,
        String sortStrategy,
        ApplicationUser user
    ) {

        List<DocumentFacadeResponse> responseList = documentService.getAllDocuments(user).stream()
            .map(doc -> enrichResponse(doc, user))
            .sorted(Comparator.comparing(it -> it.getLatestVersion().getCreatedAt()))
            .toList();

        return toPage(responseList, pageable, sortStrategy);
    }

    private Page<DocumentFacadeResponse> toPage(
        List<DocumentFacadeResponse> list,
        Pageable pageable,
        String sortStrategy
    ) {
        List<DocumentFacadeResponse> validList = switch (sortStrategy.toLowerCase()) {
            case "desc" -> list;
            case "asc" -> list.reversed();
            default -> throw new IllegalArgumentException("Parameter sortStrategy: " + sortStrategy + " is not valid");
        };

        List<DocumentFacadeResponse> pageList = validList.stream()
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .toList();

        return new PageImpl<>(pageList, pageable, list.size());
    }

    public DocumentFacadeResponse updateDocument(
        Long id,
        UpdateDocumentRequest body,
        MultipartFile file,
        Authentication auth
    ) {
        var user = userUtilService.findUserByAuthentication(auth);
        var documentResponse = documentService.updateDocument(id, body, user);

        var documentVersionRequest = CreateDocumentVersionRequest.builder()
            .documentId(documentResponse.id())
            .name(body.getVersionName())
            .attributes(body.getAttributes())
            .build();
        var latestVersion = documentVersionService.createDocumentVersion(documentVersionRequest, file, user);
        documentResponse.documentVersionIds().add(latestVersion.getId());

        return enrichResponse(documentResponse, user);
    }

    public DocumentFacadeResponse grantPermission(Long id, String email, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        var grantTo = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        var documentResponse = documentService.grantReadDocumentPermission(id, grantTo, user);
        return enrichResponse(documentResponse, user);
    }

    public void documentToArchive(Long id, Authentication auth) {
        var user = userUtilService.findUserByAuthentication(auth);
        documentService.documentToArchive(id, user);
    }
}
