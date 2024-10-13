package caselab.domain.repository;

import caselab.domain.entity.Vote;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByApplicationUserIdAndVotingProcessId(Long applicationUserId, Long votingProcessId);
}
