package caselab.domain.repository;

import caselab.domain.entity.Document;
import caselab.domain.entity.DocumentType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {
    List<Document> findByDocumentType(DocumentType documentType);
}
