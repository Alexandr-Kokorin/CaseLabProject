package caselab.domain.repository;

import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByDocumentType(DocumentType documentType);
}
