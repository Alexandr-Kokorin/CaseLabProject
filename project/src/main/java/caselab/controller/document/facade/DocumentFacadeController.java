package caselab.controller.document.facade;

import caselab.controller.document.facade.payload.CreateDocumentRequest;
import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.service.document.facade.DocumentFacadeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentFacadeController {
    DocumentFacadeService documentFacadeService;

    @GetMapping("/{id}")
    DocumentFacadeResponse getDocumentById(@PathVariable("id") Long id, Authentication authentication) {
        return documentFacadeService.getDocumentById(id, authentication);
    }

    @PostMapping("/")
    DocumentFacadeResponse createDocument(
        @RequestPart("document_params") CreateDocumentRequest body,
        @RequestPart(value = "content", required = false) MultipartFile file,
        Authentication authentication
    ) {
        return documentFacadeService.createDocument(body, file, authentication);
    }
}
