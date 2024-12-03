package caselab.domain.repository;

import caselab.domain.entity.ApplicationUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long>,
    JpaSpecificationExecutor<ApplicationUser> {

    Optional<ApplicationUser> findByEmail(String email);
}
