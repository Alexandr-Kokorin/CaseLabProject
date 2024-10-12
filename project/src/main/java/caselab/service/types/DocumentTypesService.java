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
    private final DocumentTypeMapper documentTypeMapper;

    private final DocumentTypesRepository documentTypesRepository;
    private final MessageSource messageSource;

    public DocumentTypeResponse findDocumentTypeById(Long id) {
        var documentType = documentTypesRepository.findById(id).orElseThrow(() ->
            getDocumentTypeNoSuchElementException(id));
        return documentTypeMapper.entityToResponse(documentType);
    }

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest documentTypeRequest) {
        DocumentType documentTypeForCreating = documentTypeMapper.requestToEntity(documentTypeRequest);
        return documentTypeMapper.entityToResponse(documentTypesRepository.save(documentTypeForCreating));
    }

    public DocumentTypeResponse updateDocumentType(Long id, DocumentTypeRequest documentTypeRequest) {
        var documentTypeExist = documentTypesRepository.existsById(id);
        if (!documentTypeExist) {
            throw getDocumentTypeNoSuchElementException(id);
        }
        var documentTypeForUpdating = documentTypeMapper.requestToEntity(documentTypeRequest);
        documentTypeForUpdating.setId(id);
        return documentTypeMapper.entityToResponse(documentTypesRepository.save(documentTypeForUpdating));
    }

    public void deleteDocumentTypeById(Long id) {
        var documentTypeExist = documentTypesRepository.existsById(id);
        if (!documentTypeExist) {
            throw getDocumentTypeNoSuchElementException(id);
        }
        documentTypesRepository.deleteById(id);
    }

    private NoSuchElementException getDocumentTypeNoSuchElementException(Long id) {
        return new NoSuchElementException(
            messageSource.getMessage("document.type.not.found", new Object[] {id}, Locale.getDefault())
        );
    }
}
