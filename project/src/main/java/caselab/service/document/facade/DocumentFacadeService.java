package caselab.service.document.facade;

import caselab.controller.document.facade.payload.GetDocumentResponse;
import caselab.service.document.DocumentService;
import caselab.service.util.UserFromAuthenticationUtilService;
import caselab.service.version.DocumentVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentFacadeService {
    private final DocumentService documentService;
    private final DocumentVersionService documentVersionService;

    private final UserFromAuthenticationUtilService authToUserService;

    public GetDocumentResponse getDocumentById(Long id, Authentication auth) {
        var user = authToUserService.findUserByAuthentication(auth);
        var documentResponse = documentService.getDocumentById(id, user);
        var latestVersion = documentVersionService.getDocumentVersionById(
            documentResponse.documentVersionIds().getFirst(), user
        );

        return new GetDocumentResponse(documentResponse, latestVersion);
    }
}
