package caselab.service.document.facade;

import caselab.controller.document.facade.payload.CreateDocumentRequest;
import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.service.document.DocumentService;
import caselab.service.util.UserFromAuthenticationUtilService;
import caselab.service.version.DocumentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentFacadeService {
    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;

    private final UserFromAuthenticationUtilService authToUserService;

    public DocumentFacadeResponse getDocumentById(Long id, Authentication auth) {
        var user = authToUserService.findUserByAuthentication(auth);
        var documentResponse = documentService.getDocumentById(id, user);
        var latestVersion = documentVersionService.getDocumentVersionById(
            documentResponse.documentVersionIds().getFirst(), user
        );

        return new DocumentFacadeResponse(documentResponse, latestVersion);
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
        return new DocumentFacadeResponse(documentResponse, latestVersion);

    }
}
