package caselab.domain.repository;

import caselab.domain.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    boolean existsByTenantId(String tenantId);
}
