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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@SuppressWarnings("MultipleStringLiterals")
@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypeRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final AttributeRepository attributeRepository;
    private final MessageSource messageSource;

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        Document document = toEntity(documentRequest);
        Document savedDocument = documentRepository.save(document);
        return toDTO(savedDocument);
    }

    public DocumentResponse getDocumentById(Long id) {
        return toDTO(findDocumentById(id));
    }

    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(this::toDTO);
    }

    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        Document existingDocument = findDocumentById(id);
        updateEntityFromDTO(existingDocument, documentRequest);
        existingDocument = documentRepository.save(existingDocument);
        return toDTO(existingDocument);
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new NoSuchElementException(
                messageSource.getMessage("document.not.found", new Object[] {id}, Locale.getDefault())
            );
        }
    }

    // Методы для работы с сущностью Document

    private Document findDocumentById(Long id) {
        return documentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException(
                messageSource.getMessage("document.not.found", new Object[] {id}, Locale.getDefault())
            ));
    }

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
            .orElseThrow(() -> new NoSuchElementException(
                messageSource.getMessage("document.type.not.found", new Object[] {documentTypeId}, Locale.getDefault())
            ));
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
                throw new NoSuchElementException(
                    messageSource.getMessage("users.not.found", null, Locale.getDefault())
                );
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
                .toList();

            document.getAttributeValues().clear();
            document.getAttributeValues().addAll(updatedValues);
        }
    }

    private AttributeValue createOrUpdateAttributeValue(Document document, DocumentAttributeValueDTO dto) {
        Attribute attribute = attributeRepository.findById(dto.id())
            .orElseThrow(() -> new NoSuchElementException(
                messageSource.getMessage("attribute.not.found", new Object[] {dto.id()}, Locale.getDefault())
            ));
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
            getApplicationUserIds(document).orElse(Collections.emptyList()),
            getAttributeValuesDTO(document).orElse(Collections.emptyList())
        );
    }

    private Long getDocumentTypeId(Document document) {
        return document.getDocumentType() != null ? document.getDocumentType().getId() : null;
    }

    private Optional<List<Long>> getApplicationUserIds(Document document) {
        return Optional.ofNullable(document.getApplicationUsers())
            .map(users -> users.stream()
                .map(ApplicationUser::getId)
                .collect(Collectors.toList()));
    }

    private Optional<List<DocumentAttributeValueDTO>> getAttributeValuesDTO(Document document) {
        return Optional.ofNullable(document.getAttributeValues())
            .map(attributeValues -> attributeValues.stream()
                .map(av -> new DocumentAttributeValueDTO(av.getAttribute().getId(), av.getAppValue()))
                .collect(Collectors.toList()));
    }
}
