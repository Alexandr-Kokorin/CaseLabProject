package caselab.domain.repository;

import caselab.domain.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByApplicationUserIdAndVotingProcessId(Long ApplicationUserId, Long VotingProcessId);
}
