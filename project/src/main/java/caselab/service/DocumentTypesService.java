package caselab.service;

import caselab.domain.entity.DocumentType;
import caselab.domain.repositories.DocumentTypesRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentTypesService {
    private final DocumentTypesRepository documentTypesRepository;

    @Autowired
    public DocumentTypesService(DocumentTypesRepository documentTypesRepository) {
        this.documentTypesRepository = documentTypesRepository;
    }

    public Optional<DocumentType> findDocumentType(Long id) {
        return documentTypesRepository.findById(id);
    }

}
