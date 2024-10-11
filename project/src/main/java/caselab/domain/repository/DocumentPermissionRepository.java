package caselab.domain.repository;

import caselab.domain.entity.DocumentPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {
}
