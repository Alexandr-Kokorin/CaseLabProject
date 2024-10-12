package caselab.service.voting_process;

import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.repository.VoteRepository;
import caselab.domain.repository.VotingProcessRepository;
import caselab.service.voting_process.mappers.VoteMapper;
import caselab.service.voting_process.mappers.VotingProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class VotingProcessService {

    private final VotingProcessRepository votingProcessRepository;
    private final VoteRepository voteRepository;
    private final VotingProcessMapper votingProcessMapper;
    private final VoteMapper voteMapper;

    public VotingProcessResponse updateVotingProcess(Long id, VotingProcessRequest votingProcessRequest) {
        return null;
    }

    public VoteResponse castVote(VoteRequest voteRequest) {
        return null;
    }

    @Scheduled(fixedDelayString = "#{@scheduler.interval}")
    private void deadlineProcessing() {
        log.info("Processing...");
    }
}
