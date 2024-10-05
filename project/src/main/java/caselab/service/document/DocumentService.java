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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {


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
            () -> new ResourceNotFoundException("Документ не найден с id = " + id)
        ));
    }

    public Page<DocumentDTO> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable)
            .map(this::toDTO);
    }

    public DocumentDTO updateDocument(Long id, DocumentDTO documentDTO) {
        Document existingDocument = documentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Документ не найден с id = " + id));

        Document updatedDocument = toEntity(documentDTO);
        updatedDocument.setId(existingDocument.getId());
        Document savedDocument = documentRepository.save(updatedDocument);
        return toDTO(savedDocument);
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Документ не найден с id = " + id);
        }
    }

    private Document toEntity(DocumentDTO documentDTO) {
        Document document = new Document();

        // Установка DocumentType
        DocumentType documentType = documentTypeRepository.findById(documentDTO.getDocumentTypeId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Тип документа не найден с id = " + documentDTO.getDocumentTypeId()));
        document.setDocumentType(documentType);

        // Установка ApplicationUsers
        if (documentDTO.getApplicationUserIds() != null) {
            List<ApplicationUser> users = applicationUserRepository.findAllById(documentDTO.getApplicationUserIds());
            if (users.size() != documentDTO.getApplicationUserIds().size()) {
                throw new ResourceNotFoundException("Некоторые пользователи не найдены");
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
                            throw new ResourceNotFoundException("Атрибут не найден с id = " + dto.getId());
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
