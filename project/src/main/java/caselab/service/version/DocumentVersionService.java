package caselab.service.version;

import caselab.controller.version.payload.CreateDocumentVersionRequest;
import caselab.controller.version.payload.DocumentVersionResponse;
import caselab.controller.version.payload.UpdateDocumentVersionRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentVersionService {
    public DocumentVersionResponse createDocumentVersion(CreateDocumentVersionRequest body, Authentication auth) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public DocumentVersionResponse getDocumentVersionById(Integer id, Authentication auth) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public List<DocumentVersionResponse> getVersionDocuments(Authentication auth) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public DocumentVersionResponse updateDocumentVersion(
        Integer id,
        UpdateDocumentVersionRequest body,
        Authentication auth
    ) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void deleteDocumentVersion(Integer id, Authentication auth) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
