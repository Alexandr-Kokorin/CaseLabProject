package caselab.service.types;

import caselab.controller.types.payload.DocumentTypeRequest;
import caselab.controller.types.payload.DocumentTypeResponse;
import caselab.controller.types.payload.DocumentTypeToAttributeRequest;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttribute;
import caselab.domain.entity.document.type.to.attribute.DocumentTypeToAttributeId;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentTypeToAttributeRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.exception.entity.AttributeNotFoundException;
import caselab.exception.entity.DocumentTypeNotFoundException;
import caselab.service.types.mapper.DocumentTypeMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTypesService {

    private final DocumentTypeMapper documentTypeMapper;
    private final DocumentTypesRepository documentTypeRepository;
    private final AttributeRepository attributeRepository;
    private final DocumentTypeToAttributeRepository documentTypeToAttributeRepository;

    public DocumentTypeResponse findDocumentTypeById(Long id) {
        var documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
        return documentTypeMapper.entityToResponse(documentType);
    }

    public DocumentTypeResponse createDocumentType(DocumentTypeRequest documentTypeRequest) {
        DocumentType documentTypeForCreating = documentTypeMapper.requestToEntity(documentTypeRequest);
        return documentTypeMapper.entityToResponse(documentTypeRepository.save(documentTypeForCreating));
    }

    /*
    Если кто-то придумает более читабельный метод - welcome.
    Проверять по тесту DocumentTypesServiceTest.updateExistedDocumentType
     */
    @Transactional
    public DocumentTypeResponse updateDocumentType(Long id, DocumentTypeRequest documentTypeRequest) {
        // Получаем существующий DocumentType
        DocumentType existingDocumentType = getExistingDocumentType(id);

        // Обновляем название
        updateDocumentTypeName(existingDocumentType, documentTypeRequest.name());

        // Обрабатываем связанные атрибуты
        processAttributeAssociations(existingDocumentType, documentTypeRequest.attributeRequests());

        // Сохраняем обновленный DocumentType
        DocumentType savedDocumentType = saveDocumentType(existingDocumentType);

        // Маппим в ответ
        return documentTypeMapper.entityToResponse(savedDocumentType);
    }

    public void deleteDocumentTypeById(Long id) {
        var documentTypeExist = documentTypeRepository.existsById(id);
        if (!documentTypeExist) {
            throw new DocumentTypeNotFoundException(id);
        }
        documentTypeRepository.deleteById(id);
    }

    private DocumentType getExistingDocumentType(Long id) {
        return documentTypeRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }

    private void updateDocumentTypeName(DocumentType documentType, String name) {
        documentType.setName(name);
    }

    private void processAttributeAssociations(
        DocumentType documentType,
        List<DocumentTypeToAttributeRequest> attributeRequests
    ) {
        // Получаем текущие связи из базы данных
        List<DocumentTypeToAttribute> existingAttributes = getExistingAttributes(documentType.getId());

        // Создаем карту существующих связей attributeId -> DocumentTypeToAttribute
        Map<Long, DocumentTypeToAttribute> existingAttributesMap = buildExistingAttributesMap(existingAttributes);

        // Список связей, которые нужно сохранить или обновить
        List<DocumentTypeToAttribute> attributesToSave = new ArrayList<>();

        // Обрабатываем входящие запросы атрибутов
        for (DocumentTypeToAttributeRequest request : attributeRequests) {
            processAttributeRequest(request, documentType, existingAttributesMap, attributesToSave);
        }

        // Удаляем связи, которых нет в новых запросах
        deleteObsoleteAttributes(existingAttributesMap.values());

        // Сохраняем новые и обновленные связи
        saveAttributes(attributesToSave);

        // Обновляем коллекцию в DocumentType
        updateDocumentTypeAttributes(documentType);
    }

    private List<DocumentTypeToAttribute> getExistingAttributes(Long documentTypeId) {
        return documentTypeToAttributeRepository.findByDocumentTypeId(documentTypeId);
    }

    private Map<Long, DocumentTypeToAttribute> buildExistingAttributesMap(
        List<DocumentTypeToAttribute> existingAttributes
    ) {
        return existingAttributes.stream()
            .collect(Collectors.toMap(dtta -> dtta.getAttribute().getId(), dtta -> dtta));
    }

    private void processAttributeRequest(
        DocumentTypeToAttributeRequest request,
        DocumentType documentType,
        Map<Long, DocumentTypeToAttribute> existingAttributesMap,
        List<DocumentTypeToAttribute> attributesToSave
    ) {
        Long attributeId = request.attributeId();
        Boolean isOptional = request.isOptional();

        if (existingAttributesMap.containsKey(attributeId)) {
            // Обновляем существующую связь
            DocumentTypeToAttribute existingDtta = existingAttributesMap.get(attributeId);
            existingDtta.setOptional(isOptional != null ? isOptional : existingDtta.getOptional());
            attributesToSave.add(existingDtta);
            existingAttributesMap.remove(attributeId);
        } else {
            // Создаем новую связь
            DocumentTypeToAttribute newDtta = createNewAttributeAssociation(documentType, attributeId, isOptional);
            attributesToSave.add(newDtta);
        }
    }

    private DocumentTypeToAttribute createNewAttributeAssociation(
        DocumentType documentType,
        Long attributeId, Boolean isOptional
    ) {
        Attribute attribute = attributeRepository.findById(attributeId)
            .orElseThrow(() -> new AttributeNotFoundException(attributeId));

        DocumentTypeToAttribute newDtta = new DocumentTypeToAttribute();
        newDtta.setDocumentType(documentType);
        newDtta.setAttribute(attribute);
        newDtta.setOptional(isOptional != null ? isOptional : false);

        // Устанавливаем составной ключ
        DocumentTypeToAttributeId dttaId = new DocumentTypeToAttributeId();
        dttaId.setDocumentTypeId(documentType.getId());
        dttaId.setAttributeId(attributeId);
        newDtta.setId(dttaId);

        return newDtta;
    }

    private void deleteObsoleteAttributes(Collection<DocumentTypeToAttribute> attributesToDelete) {
        if (!attributesToDelete.isEmpty()) {
            attributesToDelete.forEach(dtta -> {
                // Удаляем связь между DocumentType и DocumentTypeToAttribute
                dtta.getDocumentType().getDocumentTypesToAttributes().remove(dtta);
            });
            documentTypeToAttributeRepository.deleteAll(attributesToDelete);
        }
    }

    private void saveAttributes(List<DocumentTypeToAttribute> attributesToSave) {
        if (!attributesToSave.isEmpty()) {
            documentTypeToAttributeRepository.saveAll(attributesToSave);
        }
    }

    private void updateDocumentTypeAttributes(DocumentType documentType) {
        List<DocumentTypeToAttribute> updatedAttributes = documentTypeToAttributeRepository
            .findByDocumentTypeId(documentType.getId());
        documentType.setDocumentTypesToAttributes(updatedAttributes);
    }

    private DocumentType saveDocumentType(DocumentType documentType) {
        return documentTypeRepository.save(documentType);
    }
}
