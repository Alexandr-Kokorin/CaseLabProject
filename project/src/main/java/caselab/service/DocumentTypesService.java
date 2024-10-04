package caselab.service;

import caselab.domain.entity.DocumentType;
import caselab.domain.repository.DocumentTypesRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTypesService {
    private final DocumentTypesRepository documentTypesRepository;

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
