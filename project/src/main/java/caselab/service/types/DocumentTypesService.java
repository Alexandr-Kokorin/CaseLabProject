package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypeToAttributeRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.ConflictException;
import caselab.exception.entity.AttributeNotFoundException;
import caselab.exception.entity.DocumentTypeNotFoundException;
import caselab.service.types.mapper.DocumentTypeMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentTypesService {

    private final DocumentTypeMapper documentTypeMapper;
    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypesRepository;
    private final AttributeRepository attributeRepository;
    private final DocumentTypeToAttributeRepository documentTypeToAttributeRepository;
    private final MessageSource messageSource;

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest request) {
        var documentType = documentTypeMapper.requestToEntity(request);
        documentTypesRepository.save(documentType); // Сохраняем документ, чтобы получить ID
        documentType.setDocumentTypesToAttributes(linkAttributesToDocument(request, documentType));
        return documentTypeMapper.entityToResponse(documentType);
    }

    public DocumentTypeResponse getDocumentTypeById(Long id) {
        return documentTypeMapper.entityToResponse(findDocumentTypeById(id));
    }

    public List<DocumentTypeResponse> getAllDocumentTypes() {
        return documentTypesRepository.findAll()
            .stream()
            .map(documentTypeMapper::entityToResponse)
            .toList();
    }

    public DocumentTypeResponse updateDocumentType(Long id, DocumentTypeRequest request) {
        var documentType = findDocumentTypeById(id);
        var updatedDocumentType = documentTypeMapper.requestToEntity(request);

        updatedDocumentType.setId(documentType.getId());
        updatedDocumentType.setDocuments(new ArrayList<>(documentType.getDocuments()));
        documentTypesRepository.save(updatedDocumentType);
        updatedDocumentType.setDocumentTypesToAttributes(linkAttributesToDocument(request, updatedDocumentType));

        return documentTypeMapper.entityToResponse(updatedDocumentType);
    }

    public void deleteDocumentType(Long id) {
        var documentType = findDocumentTypeById(id);

        List<Document> relatedDocuments = documentRepository.findByDocumentType(documentType);
        if (!relatedDocuments.isEmpty()) {
            throw new ConflictException(messageSource.getMessage("document.type.in.use.error",
                new Object[] {id}, Locale.getDefault()
            ));
        }

        documentTypesRepository.delete(documentType);
    }

    // Поиск и связывания атрибутов с документом
    private List<DocumentTypeToAttribute> linkAttributesToDocument(
        DocumentTypeRequest request,
        DocumentType documentType
    ) {
        List<DocumentTypeToAttribute> links = new ArrayList<>();
        for (DocumentTypeToAttributeRequest attributeRequest : request.attributeRequests()) {
            links.add(createAttributeLink(attributeRequest, documentType));
        }
        return documentTypeToAttributeRepository.saveAll(links);
    }

    // Создание или получение существующей связи DocumentTypeToAttribute
    private DocumentTypeToAttribute createAttributeLink(
        DocumentTypeToAttributeRequest attributeRequest,
        DocumentType documentType
    ) {
        var attribute = findAttributeById(attributeRequest.attributeId());

        return documentTypeToAttributeRepository.findByDocumentTypeIdAndAttributeId(
                documentType.getId(),
                attribute.getId()
            )
            .orElse(createNewLink(documentType, attribute, attributeRequest.isOptional()));
    }

    // Создание новой связи DocumentTypeToAttribute
    private DocumentTypeToAttribute createNewLink(DocumentType documentType, Attribute attribute, boolean isOptional) {
        return DocumentTypeToAttribute.builder()
            .id(new DocumentTypeToAttributeId(documentType.getId(), attribute.getId()))
            .attribute(attribute)
            .documentType(documentType)
            .isOptional(isOptional)
            .build();
    }

    private DocumentType findDocumentTypeById(Long id) {
        return documentTypesRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }

    private Attribute findAttributeById(Long id) {
        return attributeRepository.findById(id)
            .orElseThrow(() -> new AttributeNotFoundException(id));
    }
}
