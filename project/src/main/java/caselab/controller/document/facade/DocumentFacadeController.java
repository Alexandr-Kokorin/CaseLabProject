package caselab.controller.document.facade;

import caselab.controller.document.facade.payload.GetDocumentResponse;
import caselab.service.document.facade.DocumentFacadeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentFacadeController {
    DocumentFacadeService documentFacadeService;

    @GetMapping("/{id}")
    GetDocumentResponse getDocumentById(@PathVariable("id") Long id, Authentication authentication) {
        return documentFacadeService.getDocumentById(id, authentication);
    }
}
