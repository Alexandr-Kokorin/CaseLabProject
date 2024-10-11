package caselab.service.document;

import caselab.controller.document.payload.document.dto.DocumentRequest;
import caselab.controller.document.payload.document.dto.DocumentResponse;
import caselab.domain.entity.Document;
import caselab.domain.repository.DocumentRepository;
import jakarta.transaction.Transactional;
import java.util.Locale;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@SuppressWarnings("MultipleStringLiterals") @Service @RequiredArgsConstructor @Transactional
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MessageSource messageSource;
    private final DocumentMapper documentMapper;

    public DocumentResponse createDocument(DocumentRequest documentRequest) {
        Document document = documentMapper.documentRequestToDocument(documentRequest);
        Document savedDocument = documentRepository.save(document);
        return documentMapper.documentToDocumentResponse(savedDocument);
    }

    public DocumentResponse getDocumentById(Long id) {
        return documentMapper.documentToDocumentResponse(documentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Документ с id = " + id + " не найден")));
    }

    public Page<DocumentResponse> getAllDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable).map(documentMapper::documentToDocumentResponse);
    }

    public DocumentResponse updateDocument(Long id, DocumentRequest documentRequest) {
        Document existingDocument = documentRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Документ с id = " + id + " не найден"));
        Document updatedDocument = documentMapper.documentRequestToDocument(documentRequest);
        updatedDocument.setId(existingDocument.getId());
        updatedDocument.setDocumentVersions(existingDocument.getDocumentVersions());
        Document returningDocument = documentRepository.save(existingDocument);
        return documentMapper.documentToDocumentResponse(returningDocument);
    }

    public void deleteDocument(Long id) {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id);
        } else {
            throw new NoSuchElementException(messageSource.getMessage("document.not.found",
                new Object[] {id},
                Locale.getDefault()
            ));
        }
    }
}
