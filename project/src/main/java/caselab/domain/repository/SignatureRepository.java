package caselab.domain.repository;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.Signature;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRepository extends JpaRepository<Signature, Long> {
    Optional<Signature> findByApplicationUserAndDocumentVersion(
        ApplicationUser applicationUser,
        DocumentVersion documentVersion
    );

    boolean existsByApplicationUserAndDocumentVersion(ApplicationUser applicationUser, DocumentVersion documentVersion);
}
