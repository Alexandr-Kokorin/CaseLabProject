package caselab.controller.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.service.types.DocumentTypesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v1/document_types")
@SecurityRequirement(name = "JWT")
@RequiredArgsConstructor
public class DocumentTypesController {

    private final DocumentTypesService documentTypesService;

    @Operation(summary = "Добавить тип документа")
    @PostMapping
    public DocumentTypeResponse createDocumentType(@Valid @RequestBody DocumentTypeRequest documentTypeRequest) {
        return documentTypesService.createDocumentType(documentTypeRequest);
    }

    @Operation(summary = "Получить тип документа по id")
    @GetMapping("/{id}")
    public DocumentTypeResponse findDocumentTypeById(@PathVariable Long id) {
        return documentTypesService.findDocumentTypeById(id);
    }

    @Operation(summary = "Получить все типы документов")
    @GetMapping
    public List<DocumentTypeResponse> findDocumentTypeAll() {
        return documentTypesService.findDocumentTypeAll();
    }

    @Operation(summary = "Обновить тип документа")
    @PutMapping("/{id}")
    public DocumentTypeResponse updateDocumentType(
        @PathVariable Long id,
        @Valid @RequestBody DocumentTypeRequest documentTypeRequest
    ) {
        return documentTypesService.updateDocumentType(id, documentTypeRequest);
    }

    @Operation(summary = "Удалить тип документа по id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentTypeById(@PathVariable Long id) {
        documentTypesService.deleteDocumentTypeById(id);
        return ResponseEntity.noContent().build();
    }
}
