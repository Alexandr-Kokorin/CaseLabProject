package caselab.service;

import caselab.controller.types.payload.DocumentTypeDTO;
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

    public DocumentTypeDTO findDocumentTypeById(Long id) {
        Optional<DocumentType> optionalDocumentType = documentTypesRepository.findById(id);
        if (optionalDocumentType.isPresent()) {
            return convertDocumentTypeToDocumentTypeDTO(optionalDocumentType.get());
        } else {
            throw HttpClientErrorException.NotFound.create(HttpStatusCode.valueOf(404), String.format("Тип документа с id= %s не найден", id), HttpHeaders.EMPTY, null, null);
        }
    }

    public DocumentTypeDTO createDocumentType(DocumentTypeDTO documentTypeDTOForCreating) {
        DocumentType documentTypeForCreating = convertDocumentTypeDTOToDocumentType(documentTypeDTOForCreating);
        return convertDocumentTypeToDocumentTypeDTO(documentTypesRepository.save(documentTypeForCreating));
    }

    public void deleteDocumentTypeById(Long id) {
        Optional<DocumentType> optionalDocumentType = documentTypesRepository.findById(id);
        if (optionalDocumentType.isPresent()) {
            documentTypesRepository.deleteById(id);
        }
        throw HttpClientErrorException.NotFound.create(HttpStatusCode.valueOf(404),
            "Тип документа не существует", HttpHeaders.EMPTY, null, null);
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
