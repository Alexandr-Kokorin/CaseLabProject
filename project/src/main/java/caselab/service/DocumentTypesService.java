package caselab.service;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.domain.entity.DocumentType;
import caselab.domain.repository.DocumentTypesRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@SuppressWarnings({"MagicNumber", "LineLength"})
@Service
@RequiredArgsConstructor
public class DocumentTypesService {
    private final DocumentTypesRepository documentTypesRepository;

    public DocumentTypeResponse findDocumentTypeById(Long id) {
        Optional<DocumentType> optionalDocumentType = documentTypesRepository.findById(id);
        if (optionalDocumentType.isPresent()) {
            return convertDocumentTypeToDocumentTypeResponse(optionalDocumentType.get());
        } else {
            throw HttpClientErrorException.NotFound.create(HttpStatusCode.valueOf(404), String.format("Тип документа с id= %s не найден", id), HttpHeaders.EMPTY, null, null);
        }
    }

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest documentTypeRequest) {
        DocumentType documentTypeForCreating = convertDocumentTypeRequestToDocumentType(documentTypeRequest);
        return convertDocumentTypeToDocumentTypeResponse(documentTypesRepository.save(documentTypeForCreating));
    }

    public void deleteDocumentTypeById(Long id) {
        Optional<DocumentType> optionalDocumentType = documentTypesRepository.findById(id);
        if (optionalDocumentType.isPresent()) {
            documentTypesRepository.deleteById(id);
        }
        throw HttpClientErrorException.NotFound.create(HttpStatusCode.valueOf(404),
            "Тип документа не существует", HttpHeaders.EMPTY, null, null);
    }

    private DocumentTypeResponse convertDocumentTypeToDocumentTypeResponse(DocumentType documentType) {
        return new DocumentTypeResponse(documentType.getId(), documentType.getName());
    }

    private DocumentType convertDocumentTypeRequestToDocumentType(DocumentTypeRequest documentTypeDTO) {
        DocumentType documentType = new DocumentType();
        documentType.setName(documentTypeDTO.name());
        return documentType;
    }

}
