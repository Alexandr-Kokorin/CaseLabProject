package caselab.service.document;

import caselab.domain.entity.Document;
import caselab.domain.entity.exception.ResourceNotFoundException;
import caselab.domain.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {


    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public Document createDocument(Document document) {
        return documentRepository.save(document);
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("Документ не найден с id = " + id)
        );
    }

    public Page<Document> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    public Document updateDocument(Long id, Document documentDetails) {
        return documentRepository.findById(id)
            .map(document -> {
                document.setDocumentType(documentDetails.getDocumentType());
                document.setApplicationUsers(documentDetails.getApplicationUsers());
                document.setAttributeValues(documentDetails.getAttributeValues());
                return documentRepository.save(document);
            })
            .orElseThrow(() -> new ResourceNotFoundException("Документ не найден с id = " + id));
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Документ не найден с id = " + id);
        }
    }
}
