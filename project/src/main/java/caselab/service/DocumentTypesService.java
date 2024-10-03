package caselab.service;

import caselab.domain.entity.DocumentType;
import caselab.domain.repository.DocumentTypesRepository;
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

    public Optional<DocumentType> findDocumentTypeById(Long id) {
        return documentTypesRepository.findById(id);
    }

    public DocumentType createDocumentType(DocumentType documentType) {
        return documentTypesRepository.save(documentType);
    }

    public void deleteDocumentTypeById(Long id) {
        documentTypesRepository.deleteById(id);
    }

}
