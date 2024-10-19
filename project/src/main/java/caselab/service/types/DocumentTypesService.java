package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentTypeToAttributeRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.entity.AttributeNotFoundException;
import caselab.exception.entity.DocumentTypeNotFoundException;
import caselab.service.types.mapper.DocumentTypeMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTypesService {

    private final DocumentTypeMapper documentTypeMapper;
    private final DocumentTypesRepository documentTypeRepository;
    private final AttributeRepository attributeRepository;
    private final DocumentTypeToAttributeRepository documentTypeToAttributeRepository;

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest documentTypeRequest) {
        var documentType = documentTypeMapper.requestToEntity(documentTypeRequest);

        documentType.setDocuments(List.of());
        documentTypeRepository.save(documentType);
        documentType.setDocumentTypesToAttributes(saveDocumentTypesToAttributes(documentTypeRequest, documentType));

        return documentTypeMapper.entityToResponse(documentType);
    }

    public DocumentTypeResponse findDocumentTypeById(Long id) {
        var documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
        return documentTypeMapper.entityToResponse(documentType);
    }

    public List<DocumentTypeResponse> findDocumentTypeAll() {
        var documentTypeResponses = documentTypeRepository.findAll();
        return documentTypeResponses.stream()
            .map(documentTypeMapper::entityToResponse)
            .toList();
    }

    public DocumentTypeResponse updateDocumentType(Long id, DocumentTypeRequest documentTypeRequest) {
        var documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
        var updateDocumentType = documentTypeMapper.requestToEntity(documentTypeRequest);

        updateDocumentType.setId(documentType.getId());
        updateDocumentType.setDocuments(new ArrayList<>(documentType.getDocuments()));
        documentTypeRepository.save(updateDocumentType);
        updateDocumentType.setDocumentTypesToAttributes(saveDocumentTypesToAttributes(
            documentTypeRequest,
            updateDocumentType
        ));

        return documentTypeMapper.entityToResponse(updateDocumentType);
    }

    public void deleteDocumentTypeById(Long id) {
        if (!documentTypeRepository.existsById(id)) {
            throw new DocumentTypeNotFoundException(id);
        }
        documentTypeRepository.deleteById(id);
    }

    private List<DocumentTypeToAttribute> saveDocumentTypesToAttributes(
        DocumentTypeRequest documentTypeRequest,
        DocumentType documentType
    ) {
        List<DocumentTypeToAttribute> documentTypesToAttributes = new ArrayList<>();
        for (DocumentTypeToAttributeRequest documentTypeToAttributeRequest : documentTypeRequest.attributeRequests()) {
            documentTypesToAttributes.add(createDocumentTypeToAttribute(documentTypeToAttributeRequest, documentType));
        }
        return documentTypeToAttributeRepository.saveAll(documentTypesToAttributes);
    }

    private DocumentTypeToAttribute createDocumentTypeToAttribute(
        DocumentTypeToAttributeRequest documentTypeToAttributeRequest,
        DocumentType documentType
    ) {
        var attribute = attributeRepository.findById(documentTypeToAttributeRequest.attributeId())
            .orElseThrow(() -> new AttributeNotFoundException(documentTypeToAttributeRequest.attributeId()));
        return documentTypeToAttributeRepository.findByDocumentTypeIdAndAttributeId(
                documentType.getId(),
                attribute.getId()
            )
            .orElse(DocumentTypeToAttribute.builder()
                .id(new DocumentTypeToAttributeId(documentType.getId(), attribute.getId()))
                .attribute(attribute)
                .documentType(documentType)
                .isOptional(documentTypeToAttributeRequest.isOptional())
                .build());
    }
}
