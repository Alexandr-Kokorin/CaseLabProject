package caselab.domain.repository;

import caselab.domain.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DocumentTypesRepository extends JpaRepository<DocumentType, Long>,
    JpaSpecificationExecutor<DocumentType> {
}
