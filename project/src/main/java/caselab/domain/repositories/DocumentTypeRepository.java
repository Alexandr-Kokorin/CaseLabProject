package caselab.domain.repositories;

import caselab.domain.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeRepository extends JpaRepository<DocumentType,Long> {
}
