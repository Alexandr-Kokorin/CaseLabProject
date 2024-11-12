package caselab.controller.document;

import caselab.service.document.template.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/document-template")
@RequiredArgsConstructor
public class TemplateController {
    private static final String docxMimeType = "application/msword";

    private final TemplateService templateService;

    @GetMapping(value = "/{documentTypeId}", produces = docxMimeType)
    public byte[] getDocumentTemplate(
        @PathVariable("documentTypeId") Long documentTypeId,
        Authentication authentication
    ) {
        return templateService.getTemplate(documentTypeId, authentication);
    }

    @PostMapping("/{documentTypeId}")
    public void setTemplate(@PathVariable Long documentTypeId, MultipartFile file, Authentication authentication) {
        templateService.setTemplate(documentTypeId, file, authentication);
    }

    @GetMapping(value = "/instantiate/{documentVersionId}", produces = docxMimeType)
    public byte[] instantiateDocumentTemplate(@PathVariable Long documentVersionId, Authentication authentication) {
        return templateService.instantiateTemplate(documentVersionId, authentication);
    }

    @DeleteMapping("/{documentTypeId}")
    public void deleteDocumentTemplate(@PathVariable Long documentTypeId, Authentication authentication) {
        templateService.deleteTemplate(documentTypeId, authentication);
    }
}
