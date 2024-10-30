package caselab.service.document.facade;

import caselab.controller.document.facade.payload.CreateDocumentRequest;
import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.controller.document.facade.payload.UpdateDocumentRequest;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.signature.payload.SignatureResponse;
import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.service.document.DocumentService;
import caselab.service.signature.mapper.SignatureMapper;
import caselab.service.util.UserFromAuthenticationUtilService;
import caselab.service.version.DocumentVersionService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentFacadeService {

    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;
    private final UserFromAuthenticationUtilService authToUserService;

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
        var user = authToUserService.findUserByAuthentication(auth);
        var documentResponse = documentService.getDocumentById(id, user);
        return enrichResponse(documentResponse, user);
    }

    public DocumentFacadeResponse createDocument(CreateDocumentRequest body, MultipartFile file, Authentication auth) {
        var user = authToUserService.findUserByAuthentication(auth);
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

    public List<DocumentFacadeResponse> getAllDocuments(Authentication auth) {
        var user = authToUserService.findUserByAuthentication(auth);

        return documentService.getAllDocuments(user).stream()
            .map(
                documentResponse -> enrichResponse(documentResponse, user)
            ).toList();
    }

    public DocumentFacadeResponse updateDocument(
        Long id,
        UpdateDocumentRequest body,
        MultipartFile file,
        Authentication auth
    ) {
        var user = authToUserService.findUserByAuthentication(auth);
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
        var user = authToUserService.findUserByAuthentication(auth);
        var grantTo = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        var documentResponse = documentService.grantReadDocumentPermission(id, grantTo, user);
        return enrichResponse(documentResponse, user);
    }

    public void documentToArchive(Long id, Authentication auth) {
        var user = authToUserService.findUserByAuthentication(auth);
        documentService.documentToArchive(id, user);
    }
}
