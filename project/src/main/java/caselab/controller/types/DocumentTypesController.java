package caselab.controller.types;

import caselab.controller.types.payload.DocumentTypeDTO;
import caselab.service.DocumentTypesService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
    @PostMapping
    public DocumentTypeDTO createDocumentType(@RequestBody DocumentTypeDTO documentTypeDTO) {
        return documentTypesService.createDocumentType(documentTypeDTO);
    }

    @Operation(summary = "Получить тип документа по id")
    @GetMapping("/{id}")
    public DocumentTypeDTO findDocumentTypeById(@PathVariable Long id) {
        return documentTypesService.findDocumentTypeById(id);
    }

    @Operation(summary = "Удалить тип документа по id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocumentTypeById(@PathVariable Long id) {
        documentTypesService.deleteDocumentTypeById(id);
        return ResponseEntity.ok().build();
    }
}
