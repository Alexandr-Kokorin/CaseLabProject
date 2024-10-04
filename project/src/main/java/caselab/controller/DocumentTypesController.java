package caselab.controller;

import caselab.domain.entity.DocumentType;
import caselab.service.DocumentTypesService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document_types")
@RequiredArgsConstructor
public class DocumentTypesController {

    private final DocumentTypesService documentTypesService;

    @Operation(summary = "Добавить")
    @PostMapping
    public ResponseEntity<Object> createDocumentType(@RequestBody DocumentType documentType) {
        return new ResponseEntity<>(documentTypesService.createDocumentType(documentType), HttpStatus.OK);
    }
}
