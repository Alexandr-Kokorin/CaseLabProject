package caselab.controller.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.service.types.DocumentTypesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/document_types")
@RequiredArgsConstructor
public class DocumentTypesController {

    private final DocumentTypesService documentTypesService;

    @Operation(summary = "Добавить тип документа")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public DocumentTypeResponse createDocumentType(@RequestBody DocumentTypeRequest documentTypeRequest) {
        return documentTypesService.createDocumentType(documentTypeRequest);
    }

    @Operation(summary = "Получить тип документа по id")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    public DocumentTypeResponse findDocumentTypeById(@PathVariable Long id) {
        return documentTypesService.findDocumentTypeById(id);
    }

    @Operation(summary = "Обновить тип документа")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{id}")
    public DocumentTypeResponse updateDocumentType(@PathVariable Long id,
        @RequestBody DocumentTypeRequest documentTypeRequest) {
        return documentTypesService.updateDocumentType(id, documentTypeRequest);
    }

    @Operation(summary = "Удалить тип документа по id")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentTypeById(@PathVariable Long id) {
        documentTypesService.deleteDocumentTypeById(id);
        return ResponseEntity.ok().build();
    }
}
