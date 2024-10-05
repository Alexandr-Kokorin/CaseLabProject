package caselab.controller.document;

import caselab.controller.document.payload.DocumentDTO;
import caselab.domain.entity.Document;
import caselab.service.document.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
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

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Создать документ")
    @PostMapping
    public DocumentDTO createDocument(@RequestBody DocumentDTO documentDTO) {
        return documentService.createDocument(documentDTO);
    }

    @Operation(summary = "Получить документ по id")
    @GetMapping("/{id}")
    public DocumentDTO getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    @Operation(summary = "Получить страницу документов")
    @GetMapping
    public Page<DocumentDTO> getAllDocuments(Pageable pageable) {
        return documentService.getAllDocuments(pageable);
    }

    @Operation(summary = "Обновить документ")
    @PutMapping("/{id}")
    public DocumentDTO updateDocument(@PathVariable Long id, @RequestBody DocumentDTO documentDTO) {
        return documentService.updateDocument(id, documentDTO);
    }

    @Operation(summary = "Удалить документ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

}
