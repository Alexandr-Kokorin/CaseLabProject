package caselab.service.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.controller.document.payload.user.to.document.dto.UserToDocumentRequest;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.entity.DocumentNotFoundException;
import caselab.exception.entity.DocumentTypeNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypeRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final UserToDocumentMapper userToDocumentMapper;
    private final DocumentMapper documentMapper;

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        var document = documentMapper.requestToEntity(documentRequest);
        document.setDocumentType(documentTypeRepository.findById(documentRequest.documentTypeId())
            .orElseThrow(() -> new DocumentTypeNotFoundException(documentRequest.documentTypeId())));
        documentRepository.save(document);

        var userToDocument = UserToDocument.builder()
            .build(applicationUserRepository.findByEmail(documentRequest.usersPermissions().))

        Document savedDocument = documentRepository.save(document);
        return documentMapper.entityToResponse(savedDocument);
    }

    private List<UserToDocument> saveUserToDocuments(DocumentRequest documentRequest) {
        List<UserToDocument> userToDocuments = new ArrayList<>();
        for (UserToDocumentRequest userToDocumentRequest : documentRequest.usersPermissions()) {
            userToDocuments.add(create);
        }
    }

    public DocumentResponse getDocumentById(Long id) {
        return documentMapper.entityToResponse(documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id)));
    }

    public List<DocumentResponse> getAllDocuments() {
        var documentResponses = documentRepository.findAll();
        return documentResponses.stream()
            .map(documentMapper::entityToResponse)
            .toList();
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
        return documentMapper.entityToResponse(document);
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException(id);
        }
        documentRepository.deleteById(id);
    }
}
