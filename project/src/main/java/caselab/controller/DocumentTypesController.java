package caselab.controller;

import caselab.domain.entity.DocumentType;
import caselab.service.DocumentTypesService;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document_types")
public class DocumentTypesController {

    private final DocumentTypesService documentTypesService;

    @Autowired
    public DocumentTypesController(DocumentTypesService documentTypesService) {
        this.documentTypesService = documentTypesService;
    }

    @PostMapping
    public ResponseEntity<Object> createDocumentType(@RequestBody DocumentType documentType) {
        return new ResponseEntity<>(documentTypesService.createDocumentType(documentType), HttpStatus.OK);
    }
}
