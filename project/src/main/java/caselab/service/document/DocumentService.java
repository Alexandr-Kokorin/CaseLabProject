package caselab.service.document;

import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.AttributeValue;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private static final String DOCUMENT_NOT_FOUND = "Документ не найден с id = ";
    private static final String DOCUMENT_TYPE_NOT_FOUND = "Тип документа не найден с id = ";
    private static final String USERS_NOT_FOUND = "Некоторые пользователи не найдены";
    private static final String ATTRIBUTE_NOT_FOUND = "Атрибут не найден с id = ";

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypeRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final AttributeRepository attributeRepository;

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        Document document = toEntity(documentRequest);
        Document savedDocument = documentRepository.save(document);
        return toDTO(savedDocument);
    }

    public DocumentResponse getDocumentById(Long id) {
        return toDTO(documentRepository.findById(id).orElseThrow(
            () -> new NoSuchElementException(DOCUMENT_NOT_FOUND + id)
        ));
    }

    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(this::toDTO);
    }

    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        Document existingDocument = documentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(DOCUMENT_NOT_FOUND + id));
        updateEntityFromDTO(existingDocument, documentRequest);
        existingDocument = documentRepository.save(existingDocument);
        return toDTO(existingDocument);
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new NoSuchElementException(DOCUMENT_NOT_FOUND + id);
        }
    }

    // Методы для работы с сущностью Document

    private Document toEntity(DocumentRequest documentRequest) {
        Document document = new Document();
        setDocumentType(document, documentRequest.documentTypeId());
        setApplicationUsers(document, documentRequest.applicationUserIds());
        setAttributeValues(document, documentRequest.attributeValues());
        return document;
    }

    private void updateEntityFromDTO(Document document, DocumentRequest dto) {
        updateDocumentType(document, dto.documentTypeId());
        updateApplicationUsers(document, dto.applicationUserIds());
        updateAttributeValues(document, dto.attributeValues());
    }

    // Методы для работы с DocumentType

    private void setDocumentType(Document document, Long documentTypeId) {
        DocumentType documentType = documentTypeRepository.findById(documentTypeId)
            .orElseThrow(() -> new NoSuchElementException(DOCUMENT_TYPE_NOT_FOUND + documentTypeId));
        document.setDocumentType(documentType);
    }

    private void updateDocumentType(Document document, Long documentTypeId) {
        if (documentTypeId != null) {
            setDocumentType(document, documentTypeId);
        }
    }

    // Методы для работы с ApplicationUser

    private void setApplicationUsers(Document document, List<Long> userIds) {
        if (userIds != null) {
            List<ApplicationUser> users = applicationUserRepository.findAllById(userIds);
            if (users.size() != userIds.size()) {
                throw new NoSuchElementException(USERS_NOT_FOUND);
            }
            document.setApplicationUsers(users);
        }
    }

    private void updateApplicationUsers(Document document, List<Long> userIds) {
        if (userIds != null) {
            setApplicationUsers(document, userIds);
        }
    }

    // Методы для работы с Attribute и AttributeValue

    private void setAttributeValues(Document document, List<DocumentAttributeValueDTO> attributeValuesDTO) {
        if (attributeValuesDTO != null) {
            List<AttributeValue> attributeValues = attributeValuesDTO.stream()
                .map(dto -> createOrUpdateAttributeValue(document, dto))
                .collect(Collectors.toList());
            document.setAttributeValues(attributeValues);
        }
    }

    private void updateAttributeValues(Document document, List<DocumentAttributeValueDTO> attributeValuesDTO) {
        if (attributeValuesDTO != null) {
            Map<Long, AttributeValue> existingAttributeValues = document.getAttributeValues().stream()
                .collect(Collectors.toMap(av -> av.getAttribute().getId(), Function.identity()));

            List<AttributeValue> updatedValues = attributeValuesDTO.stream()
                .map(dto -> updateOrCreateAttributeValue(document, existingAttributeValues.get(dto.id()), dto))
                .collect(Collectors.toList());

            document.getAttributeValues().clear();
            document.getAttributeValues().addAll(updatedValues);
        }
    }

    private AttributeValue createOrUpdateAttributeValue(Document document, DocumentAttributeValueDTO dto) {
        Attribute attribute = attributeRepository.findById(dto.id())
            .orElseThrow(() -> new NoSuchElementException(ATTRIBUTE_NOT_FOUND + dto.id()));
        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setDocument(document);
        attributeValue.setAttribute(attribute);
        attributeValue.setAppValue(dto.value());
        return attributeValue;
    }

    private AttributeValue updateOrCreateAttributeValue(
        Document document,
        AttributeValue existingValue,
        DocumentAttributeValueDTO dto
    ) {
        if (existingValue != null) {
            existingValue.setAppValue(dto.value());
            return existingValue;
        } else {
            return createOrUpdateAttributeValue(document, dto);
        }
    }

    // Преобразование сущности Document в DTO

    private DocumentResponse toDTO(Document document) {
        return new DocumentResponse(
            document.getId(),
            getDocumentTypeId(document),
            getApplicationUserIds(document),
            getAttributeValuesDTO(document)
        );
    }

    private Long getDocumentTypeId(Document document) {
        return document.getDocumentType() != null ? document.getDocumentType().getId() : null;
    }

    private List<Long> getApplicationUserIds(Document document) {
        return document.getApplicationUsers() != null
            ? document.getApplicationUsers().stream().map(ApplicationUser::getId).collect(Collectors.toList())
            : null;
    }

    private List<DocumentAttributeValueDTO> getAttributeValuesDTO(Document document) {
        return document.getAttributeValues() != null
            ? document.getAttributeValues().stream()
            .map(av -> new DocumentAttributeValueDTO(av.getAttribute().getId(), av.getAppValue()))
            .collect(Collectors.toList())
            : null;
    }
}
