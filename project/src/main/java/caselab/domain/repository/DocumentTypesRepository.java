package caselab.domain.repository;

import caselab.domain.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypesRepository extends JpaRepository<DocumentType, Long> {
}
