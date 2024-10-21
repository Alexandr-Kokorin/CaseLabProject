package caselab.controller.version;

import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import caselab.service.version.DocumentVersionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/versions")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentVersionController {
    private final DocumentVersionService documentVersionService;

    @PostMapping
    public DocumentVersionResponse createDocumentVersion(
        @RequestBody CreateDocumentVersionRequest body,
        Authentication auth
    ) {
        return documentVersionService.createDocumentVersion(body, auth);
    }

    @GetMapping("/{id}")
    public DocumentVersionResponse getDocumentVersionById(@PathVariable("id") Long id, Authentication auth) {
        return documentVersionService.getDocumentVersionById(id, auth);
    }

    @GetMapping
    public List<DocumentVersionResponse> getDocumentVersions(Authentication auth) {
        return documentVersionService.getVersionDocuments(auth);
    }

    @PutMapping("/{id}")
    public DocumentVersionResponse updateDocumentVersion(
        @PathVariable("id") Long id, @RequestBody
    UpdateDocumentVersionRequest body, Authentication auth
    ) {
        return documentVersionService.updateDocumentVersion(id, body, auth);
    }

    @DeleteMapping("/{id}")
    public void deleteDocumentVersion(@PathVariable("id") Long id, Authentication auth) {
        documentVersionService.deleteDocumentVersion(id, auth);
    }
}
