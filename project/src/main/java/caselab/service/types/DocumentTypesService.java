package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypeToAttributeRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.DocumentTypeInUseException;
import caselab.exception.entity.not_found.AttributeNotFoundException;
import caselab.exception.entity.not_found.DocumentTypeNotFoundException;
import caselab.service.types.mapper.DocumentTypeMapper;
import caselab.service.util.PageUtil;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class DocumentTypesService {

    private final UserUtilService userUtilService;

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypesRepository;
    private final AttributeRepository attributeRepository;
    private final DocumentTypeToAttributeRepository documentTypeToAttributeRepository;

    private final DocumentTypeMapper documentTypeMapper;

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest request, Authentication authentication) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        var documentType = documentTypeMapper.requestToEntity(request);

        validateAttributesExistence(request);

        documentTypesRepository.save(documentType); // Сохраняем документ, чтобы получить ID
        documentType.setDocumentTypesToAttributes(linkAttributesToDocument(request, documentType));
        return documentTypeMapper.entityToResponse(documentType);
    }

    public DocumentTypeResponse getDocumentTypeById(Long id) {
        return documentTypeMapper.entityToResponse(findDocumentTypeById(id));
    }

    public Page<DocumentTypeResponse> getAllDocumentTypes(
        Integer pageNum,
        Integer pageSize,
        String sortStrategy
    ) {
        Pageable pageable = PageUtil.toPageable(pageNum, pageSize, Sort.by("name"), sortStrategy);

        return documentTypesRepository.findAll(pageable)
            .map(documentTypeMapper::entityToResponse);
    }

    public DocumentTypeResponse updateDocumentType(
        Long id, DocumentTypeRequest request,
        Authentication authentication
    ) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        var documentType = findDocumentTypeById(id);
        var updatedDocumentType = documentTypeMapper.requestToEntity(request);

        validateAttributesExistence(request);

        updatedDocumentType.setId(documentType.getId());
        updatedDocumentType.setDocuments(new ArrayList<>(documentType.getDocuments()));
        documentTypesRepository.save(updatedDocumentType);
        updatedDocumentType.setDocumentTypesToAttributes(linkAttributesToDocument(request, updatedDocumentType));

        return documentTypeMapper.entityToResponse(updatedDocumentType);
    }

    public void deleteDocumentType(Long id, Authentication authentication) {
        userUtilService.checkUserGlobalPermission(
            userUtilService.findUserByAuthentication(authentication), GlobalPermissionName.ADMIN);

        var documentType = findDocumentTypeById(id);

        List<Document> relatedDocuments = documentRepository.findByDocumentType(documentType);
        if (!relatedDocuments.isEmpty()) {
            throw new DocumentTypeInUseException(id);
        }

        documentTypesRepository.delete(documentType);
    }

    // Поиск и связывания атрибутов с документом
    private List<DocumentTypeToAttribute> linkAttributesToDocument(
        DocumentTypeRequest request,
        DocumentType documentType
    ) {
        List<DocumentTypeToAttribute> links = new ArrayList<>();

        request.attributeRequests()
            .forEach(attributeRequest -> links.add(createAttributeLink(attributeRequest, documentType)));

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

    private void validateAttributesExistence(DocumentTypeRequest request) {
        request.attributeRequests().forEach(attributeRequest -> findAttributeById(attributeRequest.attributeId()));
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
