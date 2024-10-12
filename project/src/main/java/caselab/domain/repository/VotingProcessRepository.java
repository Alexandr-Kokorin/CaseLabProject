package caselab.domain.repository;

import caselab.domain.entity.VotingProcess;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingProcessRepository extends JpaRepository<VotingProcess, Long> {
}
