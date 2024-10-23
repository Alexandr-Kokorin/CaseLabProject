package caselab.controller.version;

import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import caselab.service.version.DocumentVersionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/versions")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentVersionController {
    private final DocumentVersionService documentVersionService;

    @PostMapping
    public DocumentVersionResponse createDocumentVersion(
        @RequestPart("version") CreateDocumentVersionRequest body,
        @RequestPart(value = "content", required = false) MultipartFile file,
        Authentication auth
    ) {
        return documentVersionService.createDocumentVersion(body, file, auth);
    }

    @GetMapping("/{id}")
    public DocumentVersionResponse getDocumentVersionById(@PathVariable("id") Long id, Authentication auth) {
        return documentVersionService.getDocumentVersionById(id, auth);
    }

    @GetMapping
    public List<DocumentVersionResponse> getDocumentVersions(Authentication auth) {
        return documentVersionService.getDocumentVersions(auth);
    }

    @GetMapping("/content/{id}")
    public ResponseEntity<Resource> getDocumentVersionContent(@PathVariable Long id, Authentication auth){
        try (InputStream stream = documentVersionService.getDocumentVersionContent(id, auth)) {
            var resource = new InputStreamResource(stream);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
