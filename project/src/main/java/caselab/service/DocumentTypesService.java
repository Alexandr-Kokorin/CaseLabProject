package caselab.service;

import caselab.controller.types.payload.DocumentTypeDTO;
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

    public DocumentTypeDTO createDocumentType(DocumentTypeDTO documentTypeDTOForCreating) {
        DocumentType documentTypeForCreating = convertDocumentTypeDTOToDocumentType(documentTypeDTOForCreating);
        return convertDocumentTypeToDocumentTypeDTO(documentTypesRepository.save(documentTypeForCreating));
    }

    public void deleteDocumentTypeById(Long id) {
        documentTypesRepository.deleteById(id);
    }

    private DocumentTypeDTO convertDocumentTypeToDocumentTypeDTO(DocumentType documentType) {
        return new DocumentTypeDTO(documentType.getName());
    }

    private DocumentType convertDocumentTypeDTOToDocumentType(DocumentTypeDTO documentTypeDTO) {
        DocumentType documentType = new DocumentType();
        documentType.setName(documentTypeDTO.name());
        return documentType;
    }

}
