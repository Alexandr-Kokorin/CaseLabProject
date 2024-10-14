package caselab.service.voting_process;

import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.Vote;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.VoteStatus;
import caselab.domain.entity.enums.VotingProcessStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.VoteRepository;
import caselab.domain.repository.VotingProcessRepository;
import caselab.exception.VotingProcessIsOverException;
import caselab.exception.entity.DocumentVersionNotFoundException;
import caselab.exception.entity.UserNotFoundException;
import caselab.exception.entity.VoteNotFoundException;
import caselab.exception.entity.VotingProcessNotFoundException;
import caselab.service.voting_process.mappers.VoteMapper;
import caselab.service.voting_process.mappers.VotingProcessMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@SuppressWarnings("MissingSwitchDefault")
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class VotingProcessService {

    private final ApplicationUserRepository applicationUserRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final VotingProcessRepository votingProcessRepository;
    private final VoteRepository voteRepository;
    private final VotingProcessMapper votingProcessMapper;
    private final VoteMapper voteMapper;

    public VotingProcessResponse createVotingProcess(VotingProcessRequest votingProcessRequest) {
        var documentVersion = documentVersionRepository.findById(votingProcessRequest.documentVersionId())
            .orElseThrow(() -> new DocumentVersionNotFoundException(votingProcessRequest.documentVersionId()));

        var votingProcess = votingProcessMapper.requestToEntity(votingProcessRequest);
        votingProcess.setStatus(VotingProcessStatus.IN_PROGRESS);
        votingProcess.setCreatedAt(OffsetDateTime.now());
        votingProcess.setDocumentVersion(documentVersion);
        votingProcessRepository.save(votingProcess);
        votingProcess.setVotes(saveVotes(votingProcessRequest.userIds(), votingProcess));

        return votingProcessMapper.entityToResponse(votingProcess);
    }

    public VotingProcessResponse getVotingProcessById(Long id) {
        var votingProcess = votingProcessRepository.findById(id)
            .orElseThrow(() -> new VotingProcessNotFoundException(id));
        return votingProcessMapper.entityToResponse(votingProcess);
    }

    public void deleteVotingProcess(Long id) {
        if (!votingProcessRepository.existsById(id)) {
            throw new VotingProcessNotFoundException(id);
        }
        votingProcessRepository.deleteById(id);
    }

    public VotingProcessResponse updateVotingProcess(Long id, VotingProcessRequest votingProcessRequest) {
        if (!votingProcessRepository.existsById(id)) {
            throw new VotingProcessNotFoundException(id);
        }
        var votingProcess = votingProcessRepository.findById(id).orElseThrow();

        var updateVotingProcess = votingProcessMapper.requestToEntity(votingProcessRequest);
        updateVotingProcess.setId(votingProcess.getId());
        updateVotingProcess.setStatus(votingProcess.getStatus());
        updateVotingProcess.setCreatedAt(votingProcess.getCreatedAt());
        updateVotingProcess.setDocumentVersion(votingProcess.getDocumentVersion());
        votingProcessRepository.save(updateVotingProcess);
        updateVotingProcess.setVotes(saveVotes(votingProcessRequest.userIds(), updateVotingProcess));

        return votingProcessMapper.entityToResponse(updateVotingProcess);
    }

    public VoteResponse castVote(VoteRequest voteRequest) {
        var vote = voteRepository.findByApplicationUserIdAndVotingProcessId(
                voteRequest.applicationUserId(), voteRequest.votingProcessId())
            .orElseThrow(() -> new VoteNotFoundException(voteRequest.applicationUserId()));
        if (vote.getVotingProcess().getDeadline().isBefore(OffsetDateTime.now())) {
            throw new VotingProcessIsOverException(voteRequest.votingProcessId());
        }
        vote.setStatus(voteRequest.status());
        return voteMapper.entityToResponse(voteRepository.save(vote));
    }

    private List<Vote> saveVotes(List<Long> userIds, VotingProcess votingProcess) {
        List<Vote> votes = new ArrayList<>();
        for (Long userId : userIds) {
            votes.add(createVoteById(userId, votingProcess));
        }
        return voteRepository.saveAll(votes);
    }

    private Vote createVoteById(Long userId, VotingProcess votingProcess) {
        var user = applicationUserRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        return voteRepository.findByApplicationUserIdAndVotingProcessId(userId, votingProcess.getId())
            .orElse(Vote.builder()
                .status(VoteStatus.NOT_VOTED)
                .applicationUser(user)
                .votingProcess(votingProcess)
                .build());
    }

    @Scheduled(fixedDelayString = "#{@scheduler.interval}")
    public void deadlineProcessing() {
        log.info("Processing...");
        var votingProcesses = votingProcessRepository.findAll();
        for (VotingProcess votingProcess : votingProcesses) {
            if (votingProcess.getDeadline().isBefore(OffsetDateTime.now())) {
                var statistics = calculateResult(votingProcess);
                votingProcess.setStatus(statistics.getVotingProcessStatus());
                votingProcessRepository.save(votingProcess);
                // Отправить сообщение на почту о завершении голосования всем участникам
            }
        }
        log.info("Completed");
    }

    private VotingStatistics calculateResult(VotingProcess votingProcess) {
        var statistics = new VotingStatistics();
        for (Vote vote : votingProcess.getVotes()) {
            switch (vote.getStatus()) {
                case IN_FAVOUR -> statistics.incCountInFavour();
                case AGAINST -> statistics.incCountAgainst();
                case ABSTAINED -> statistics.incCountAbstained();
                case NOT_VOTED -> statistics.incCountNotVoted();
            }
        }
        statistics.calculateStatus(votingProcess.getThreshold());
        return statistics;
    }
}
