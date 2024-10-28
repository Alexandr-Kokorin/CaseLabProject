package caselab.controller.document.facade;

import caselab.controller.document.facade.payload.CreateDocumentRequest;
import caselab.controller.document.facade.payload.DocumentFacadeResponse;
import caselab.controller.document.facade.payload.UpdateDocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.elastic.service.DocumentElasticService;
import caselab.service.document.facade.DocumentFacadeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents-facade")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentFacadeController {

    private final DocumentFacadeService documentFacadeService;
    private final DocumentElasticService documentElasticService;

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

    @GetMapping("/")
    List<DocumentFacadeResponse> getAllDocuments(Authentication authentication) {
        return documentFacadeService.getAllDocuments(authentication);
    }

    @PutMapping("/{id}")
    public DocumentFacadeResponse updateDocument(
        @PathVariable Long id,
        @RequestBody UpdateDocumentRequest documentRequest,
        Authentication authentication
    ) {
        return documentFacadeService.updateDocument(id, documentRequest, authentication);
    }

    @PutMapping("/{id}/grant-access-to/{email}")
    public DocumentFacadeResponse grantAccess(
        @PathVariable Long id,
        @PathVariable String email,
        Authentication authentication
    ) {
        return documentFacadeService.grantPermission(id, email, authentication);
    }

    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id, Authentication authentication) {
        documentFacadeService.documentToArchive(id, authentication);
    }

    @GetMapping("/search")
    public Page<DocumentResponse> search(
        @Parameter(description = "Слово или фраза по которой будет осуществляться поиск", example = "Приказ")
        @RequestParam("query") String query,
        @Parameter(description = "Номер страницы для выдачи из всех найденных", example = "1")
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @Parameter(description = "Количество страниц в выдаче", example = "9")
        @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        return documentElasticService.searchValuesElastic(query, page, size);
    }
}
