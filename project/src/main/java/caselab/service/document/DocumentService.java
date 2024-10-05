package caselab.service.document;


import caselab.controller.document.payload.DocumentAttributeValueDTO;
import caselab.controller.document.payload.DocumentDTO;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Attribute;
import caselab.domain.entity.AttributeValue;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.exception.ResourceNotFoundException;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.AttributeRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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


    public DocumentDTO createDocument(DocumentDTO documentDTO) {
        Document document = toEntity(documentDTO);
        Document savedDocument = documentRepository.save(document);
        return toDTO(savedDocument);
    }

    public DocumentDTO getDocumentById(Long id) {
        return toDTO(documentRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException(DOCUMENT_NOT_FOUND + id)
        ));
    }

    public Page<DocumentDTO> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
            .map(this::toDTO);
    }

    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO) {
        Document existingDocument = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(DOCUMENT_NOT_FOUND + id));
        updateEntityFromDTO(existingDocument, documentDTO);
        existingDocument = documentRepository.save(existingDocument);
        return toDTO(existingDocument);
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException(DOCUMENT_NOT_FOUND + id);
        }
    }

    private void updateEntityFromDTO(Document document, DocumentDTO dto) {
        // Обновление DocumentType
        if (dto.getDocumentTypeId() != null) {
            DocumentType documentType = documentTypeRepository.findById(dto.getDocumentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    DOCUMENT_TYPE_NOT_FOUND + dto.getDocumentTypeId()));
            document.setDocumentType(documentType);
        }

        // Обновление ApplicationUsers
        if (dto.getApplicationUserIds() != null) {
            List<ApplicationUser> users = applicationUserRepository.findAllById(dto.getApplicationUserIds());
            if (users.size() != dto.getApplicationUserIds().size()) {
                throw new ResourceNotFoundException(USERS_NOT_FOUND);
            }
            document.setApplicationUsers(users);
        }

        // Обновление AttributeValues
        if (dto.getAttributeValues() != null) {
            // Создаём карту существующих AttributeValue по attributeId
            Map<Long, AttributeValue> existingAttributeValuesMap = document.getAttributeValues().stream()
                .collect(Collectors.toMap(av -> av.getAttribute().getId(), Function.identity()));

            List<AttributeValue> updatedAttributeValues = new ArrayList<>();

            for (DocumentAttributeValueDTO attributeValueDTO : dto.getAttributeValues()) {
                AttributeValue attributeValue = existingAttributeValuesMap.get(attributeValueDTO.getId());

                if (attributeValue != null) {
                    // Обновляем существующий AttributeValue
                    attributeValue.setAppValue(attributeValueDTO.getValue());
                    updatedAttributeValues.add(attributeValue);
                } else {
                    // Проверяем, существует ли атрибут
                    Attribute attribute = attributeRepository.findById(attributeValueDTO.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                            ATTRIBUTE_NOT_FOUND + attributeValueDTO.getId()));

                    // Создаём новый AttributeValue
                    AttributeValue newAttributeValue = new AttributeValue();
                    newAttributeValue.setDocument(document);
                    newAttributeValue.setAttribute(attribute);
                    newAttributeValue.setAppValue(attributeValueDTO.getValue());

                    updatedAttributeValues.add(newAttributeValue);
                }
            }

            // Обновляем список AttributeValues в документе
            document.getAttributeValues().clear();
            document.getAttributeValues().addAll(updatedAttributeValues);
        }
    }

    private Document toEntity(DocumentDTO documentDTO) {
        Document document = new Document();

        // Установка DocumentType
        DocumentType documentType = documentTypeRepository.findById(documentDTO.getDocumentTypeId())
            .orElseThrow(() -> new ResourceNotFoundException(
                DOCUMENT_TYPE_NOT_FOUND + documentDTO.getDocumentTypeId()));
        document.setDocumentType(documentType);

        // Установка ApplicationUsers
        if (documentDTO.getApplicationUserIds() != null) {
            List<ApplicationUser> users = applicationUserRepository.findAllById(documentDTO.getApplicationUserIds());
            if (users.size() != documentDTO.getApplicationUserIds().size()) {
                throw new ResourceNotFoundException(USERS_NOT_FOUND);
            }
            document.setApplicationUsers(users);
        }

        // Установка AttributeValues
        if (documentDTO.getAttributeValues() != null) {
            List<Long> attributeIds = documentDTO.getAttributeValues().stream()
                .map(DocumentAttributeValueDTO::getId)
                .collect(Collectors.toList());

            List<Attribute> attributes = attributeRepository.findAllById(attributeIds);

            Map<Long, Attribute> attributeMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, Function.identity()));

            if (attributes.size() != attributeIds.size()) {
                throw new ResourceNotFoundException("Некоторые атрибуты не найдены");
            }

            document.setAttributeValues(
                documentDTO.getAttributeValues().stream()
                    .map(dto -> {
                        AttributeValue attributeValue = new AttributeValue();
                        attributeValue.setDocument(document);

                        Attribute attribute = attributeMap.get(dto.getId());
                        if (attribute == null) {
                            throw new ResourceNotFoundException(ATTRIBUTE_NOT_FOUND + dto.getId());
                        }
                        attributeValue.setAttribute(attribute);
                        attributeValue.setAppValue(dto.getValue());
                        return attributeValue;
                    })
                    .collect(Collectors.toList())
            );
        }

        return document;
    }

    private DocumentDTO toDTO(Document document) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(document.getId());

        // Установка DocumentType
        if (document.getDocumentType() != null) {
            dto.setDocumentTypeId(document.getDocumentType().getId());
        }

        // Установка ApplicationUserIds
        if (document.getApplicationUsers() != null) {
            dto.setApplicationUserIds(
                document.getApplicationUsers().stream()
                    .map(ApplicationUser::getId)
                    .collect(Collectors.toList())
            );
        }

        // Установка AttributeValues
        if (document.getAttributeValues() != null) {
            dto.setAttributeValues(
                document.getAttributeValues().stream()
                    .map(attributeValue -> {
                        DocumentAttributeValueDTO attributeValueDTO = new DocumentAttributeValueDTO();
                        attributeValueDTO.setId(attributeValue.getAttribute().getId());
                        attributeValueDTO.setValue(attributeValue.getAppValue());
                        return attributeValueDTO;
                    })
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
