package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.domain.entity.DocumentType;
import caselab.domain.repository.DocumentTypesRepository;
import java.util.Locale;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTypesService {

    private final DocumentTypesRepository documentTypesRepository;
    private final MessageSource messageSource;

    public DocumentTypeResponse findDocumentTypeById(Long id) {
        var optionalDocumentType = documentTypesRepository.findById(id).orElseThrow(() ->
            getDocumentTypeNoSuchElementException(id));
        return convertDocumentTypeToDocumentTypeResponse(optionalDocumentType);
    }

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest documentTypeRequest) {
        DocumentType documentTypeForCreating = convertDocumentTypeRequestToDocumentType(documentTypeRequest);
        return convertDocumentTypeToDocumentTypeResponse(documentTypesRepository.save(documentTypeForCreating));
    }

    public DocumentTypeResponse updateDocumentType(Long id, DocumentTypeRequest documentTypeRequest) {
        var documentTypeExist = documentTypesRepository.existsById(id);
        if (!documentTypeExist) {
            throw getDocumentTypeNoSuchElementException(id);
        }
        var documentTypeForUpdating = convertDocumentTypeRequestToDocumentType(documentTypeRequest);
        documentTypeForUpdating.setId(id);
        return convertDocumentTypeToDocumentTypeResponse(documentTypesRepository.save(documentTypeForUpdating));
    }

    public void deleteDocumentTypeById(Long id) {
        var documentTypeExist = documentTypesRepository.existsById(id);
        if (!documentTypeExist) {
            throw getDocumentTypeNoSuchElementException(id);
        }
        documentTypesRepository.deleteById(id);
    }

    private DocumentTypeResponse convertDocumentTypeToDocumentTypeResponse(DocumentType documentType) {
        return new DocumentTypeResponse(documentType.getId(), documentType.getName());
    }

    private DocumentType convertDocumentTypeRequestToDocumentType(DocumentTypeRequest documentTypeDTO) {
        DocumentType documentType = new DocumentType();
        documentType.setName(documentTypeDTO.name());
        return documentType;
    }

    private NoSuchElementException getDocumentTypeNoSuchElementException(Long id) {
        return new NoSuchElementException(
            messageSource.getMessage("document.type.not.found", new Object[] {id}, Locale.getDefault())
        );
    }
}
