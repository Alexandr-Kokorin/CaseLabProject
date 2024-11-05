package caselab.domain.repository;

import caselab.domain.entity.DocumentVersion;
import caselab.domain.entity.VotingProcess;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingProcessRepository extends JpaRepository<VotingProcess, Long> {

    Optional<VotingProcess> findByDocumentVersion(DocumentVersion documentVersion);
}
