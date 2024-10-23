package caselab.service.document;

import caselab.controller.document.payload.DocumentRequest;
import caselab.controller.document.payload.DocumentResponse;
import caselab.controller.document.payload.UserToDocumentRequest;
import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.DocumentType;
import caselab.domain.entity.UserToDocument;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.DocumentTypesRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.exception.entity.DocumentNotFoundException;
import caselab.exception.entity.DocumentPermissionNotFoundException;
import caselab.exception.entity.DocumentTypeNotFoundException;
import caselab.service.document.mapper.DocumentMapper;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentTypesRepository documentTypesRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final DocumentPermissionRepository documentPermissionRepository;
    private final DocumentMapper documentMapper;

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        var document = documentMapper.requestToEntity(documentRequest);

        document.setDocumentType(getDocumentTypeById(documentRequest.documentTypeId()));
        document.setDocumentVersions(List.of());
        documentRepository.save(document);
        document.setUsersToDocuments(saveUserToDocuments(documentRequest, document));

        return documentMapper.entityToResponse(document);
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

    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        var document = documentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));
        var updateDocument = documentMapper.requestToEntity(documentRequest);

        updateDocument.setId(document.getId());
        updateDocument.setDocumentType(getDocumentTypeById(documentRequest.documentTypeId()));
        updateDocument.setDocumentVersions(new ArrayList<>(document.getDocumentVersions()));
        documentRepository.save(updateDocument);
        updateDocument.setUsersToDocuments(saveUserToDocuments(documentRequest, updateDocument));

        return documentMapper.entityToResponse(updateDocument);
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new DocumentNotFoundException(id);
        }
        documentRepository.deleteById(id);
    }

    private DocumentType getDocumentTypeById(Long id) {
        return documentTypesRepository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id));
    }

    private List<UserToDocument> saveUserToDocuments(DocumentRequest documentRequest, Document document) {
        List<UserToDocument> userToDocuments = new ArrayList<>();
        for (UserToDocumentRequest userToDocumentRequest : documentRequest.usersPermissions()) {
            userToDocuments.add(createUserToDocument(userToDocumentRequest, document));
        }
        return userToDocumentRepository.saveAll(userToDocuments);
    }

    private UserToDocument createUserToDocument(UserToDocumentRequest userToDocumentRequest, Document document) {
        var user = applicationUserRepository.findByEmail(userToDocumentRequest.email())
            .orElseThrow(() -> new UsernameNotFoundException(userToDocumentRequest.email()));
        return userToDocumentRepository.findByApplicationUserIdAndDocumentId(user.getId(), document.getId())
            .orElse(UserToDocument.builder()
                .applicationUser(user)
                .document(document)
                .documentPermissions(getDocumentPermissions(userToDocumentRequest.documentPermissionIds()))
                .build());
    }

    private List<DocumentPermission> getDocumentPermissions(List<Long> documentPermissionIds) {
        List<DocumentPermission> documentPermissions = new ArrayList<>();
        for (Long id : documentPermissionIds) {
            documentPermissions.add(documentPermissionRepository.findById(id)
                .orElseThrow(() -> new DocumentPermissionNotFoundException(id)));
        }
        return documentPermissions;
    }
}
