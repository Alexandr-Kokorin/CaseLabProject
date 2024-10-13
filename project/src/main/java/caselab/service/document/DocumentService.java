package caselab.service.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.entity.DocumentNotFoundException;
import caselab.exception.entity.DocumentTypeNotFoundException;
import caselab.service.user.to.document.UserToDocumentMapper;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final DocumentTypesRepository documentTypeRepository;
    private final UserToDocumentMapper userToDocumentMapper;
    private final UserToDocumentRepository userToDocumentRepository;

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        Document document = documentMapper.documentRequestToDocument(documentRequest);
        Document savedDocument = documentRepository.save(document);
        return documentMapper.documentToDocumentResponse(savedDocument);
    }

    public DocumentResponse getDocumentById(Long id) {
        return documentMapper.documentToDocumentResponse(documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id)));
    }

    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(documentMapper::documentToDocumentResponse);
    }

    public DocumentResponse updateDocument(Long documentId, DocumentRequest documentRequest) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId));

        // Обновляем поля документа
        DocumentType documentType = documentTypeRepository.findById(documentRequest.documentTypeId())
            .orElseThrow(() -> new DocumentTypeNotFoundException(documentRequest.documentTypeId()));
        document.setDocumentType(documentType);
        document.setName(documentRequest.name());

        // Обновляем пользователей и их права
        List<UserToDocument> usersToDocuments = documentRequest.usersPermissions().stream()
            .map(userToDocumentMapper::userToDocumentRequestToUserToDocument)
            .collect(Collectors.toList());

        // Устанавливаем связь между документом и пользователями
        Document finalDocument = document;
        usersToDocuments.forEach(userToDocument -> userToDocument.setDocument(finalDocument));

        // Сохраняем пользователей в базу данных
        usersToDocuments = userToDocumentRepository.saveAll(usersToDocuments);

        // Устанавливаем обновленный список пользователей в документ
        document.setUsersToDocuments(usersToDocuments);

        // Сохраняем документ
        document = documentRepository.save(document);

        // Возвращаем маппинг обратно в DTO
        return documentMapper.documentToDocumentResponse(document);
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new DocumentNotFoundException(id);
        }
    }

//    private EntityNotFoundException documentNotFound(Long id) {
//        return new EntityNotFoundException(
//            messageSource.getMessage("document.not.found", new Object[] {id}, Locale.getDefault())
//        );
//    }

//    private EntityNotFoundException documentTypeNotFound(Long id) {
//        return new EntityNotFoundException(
//            messageSource.getMessage("document.type.not.found", new Object[] {id}, Locale.getDefault())
//        );
//    }
}
