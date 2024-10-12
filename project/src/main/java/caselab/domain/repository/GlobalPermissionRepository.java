package caselab.domain.repository;

import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalPermissionRepository extends JpaRepository<GlobalPermission, Long> {
    GlobalPermission findByName(GlobalPermissionName name);
}
