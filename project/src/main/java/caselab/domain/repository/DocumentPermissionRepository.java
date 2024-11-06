package caselab.domain.repository;

import caselab.domain.entity.DocumentPermission;
import caselab.domain.entity.enums.DocumentPermissionName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {
    DocumentPermission findDocumentPermissionByName(DocumentPermissionName name);
}
