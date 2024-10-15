package caselab.controller.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.service.document.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // TODO создателю документа присваивался уровень доступа "CREATOR"

    @Operation(summary = "Создать документ")
    @PostMapping
    public DocumentResponse createDocument(@RequestBody DocumentRequest documentRequest) {
        return documentService.createDocument(documentRequest);
    }

    @Operation(summary = "Получить документ по id")
    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    @Operation(summary = "Получить список документов")
    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    @Operation(summary = "Обновить документ")
    @PutMapping("/{id}")
    public DocumentResponse updateDocument(
        @PathVariable Long id,
        @RequestBody DocumentRequest documentRequest
    ) {
        return documentService.updateDocument(id, documentRequest);
    }

    @Operation(summary = "Удалить документ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
