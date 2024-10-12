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
import caselab.domain.repository.VoteRepository;
import caselab.domain.repository.VotingProcessRepository;
import caselab.exception.EntityNotFoundException;
import caselab.service.voting_process.mappers.VoteMapper;
import caselab.service.voting_process.mappers.VotingProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class VotingProcessService {

    private final ApplicationUserRepository applicationUserRepository;
    private final VotingProcessRepository votingProcessRepository;
    private final VoteRepository voteRepository;
    private final VotingProcessMapper votingProcessMapper;
    private final VoteMapper voteMapper;
    private final MessageSource messageSource;

    public VotingProcessResponse updateVotingProcess(Long id, VotingProcessRequest votingProcessRequest) {
        if (!votingProcessRepository.existsById(id)) {
            throw votingProcessNotFound(id);
        }
        var votingProcess = votingProcessRepository.findById(id).orElseThrow();
        var updateVotingProcess = votingProcessMapper.requestToEntity(votingProcessRequest);

        updateVotingProcess.setId(votingProcess.getId());
        updateVotingProcess.setCreatedAt(votingProcess.getCreatedAt());
        updateVotingProcess.setDocumentVersion(votingProcess.getDocumentVersion());
        List<Vote> votes = new ArrayList<>();
        for (Long userId : votingProcessRequest.userIds()) {
            votes.add(createVoteById(userId, updateVotingProcess));
        }
        updateVotingProcess.setVotes(votes);

        return votingProcessMapper.entityToResponse(votingProcessRepository.save(updateVotingProcess));
    }

    private Vote createVoteById(Long userId, VotingProcess votingProcess) {
        var user = applicationUserRepository.findById(userId)
            .orElseThrow(() -> userNotFound(userId));
        return Vote.builder()
            .status(VoteStatus.NOT_VOTED)
            .applicationUser(user)
            .votingProcess(votingProcess)
            .build();
    }

    public VoteResponse castVote(VoteRequest voteRequest) {
        var vote = voteRepository.findByApplicationUserIdAndVotingProcessId(
                voteRequest.applicationUserId(), voteRequest.VotingProcessId())
            .orElseThrow(() -> voteNotFound(voteRequest.VotingProcessId(), voteRequest.applicationUserId()));
        if (vote.getVotingProcess().getDeadline().isBefore(OffsetDateTime.now())) {
            throw votingProcessIsOver(voteRequest.VotingProcessId());
        }
        vote.setStatus(voteRequest.status());
        return voteMapper.entityToResponse(voteRepository.save(vote));
    }

    private EntityNotFoundException votingProcessNotFound(Long id) {
        return new EntityNotFoundException(
            messageSource.getMessage("voting.process.not.found", new Object[] {id}, Locale.getDefault())
        );
    }

    private EntityNotFoundException voteNotFound(Long votingProcessId, Long userId) {
        return new EntityNotFoundException(
            messageSource.getMessage("vote.not.found", new Object[] {votingProcessId, userId}, Locale.getDefault())
        );
    }

    private EntityNotFoundException userNotFound(Long id) {
        return new EntityNotFoundException(
            messageSource.getMessage("user.not.found", new Object[] {id}, Locale.getDefault())
        );
    }

    private EntityNotFoundException votingProcessIsOver(Long id) {
        return new EntityNotFoundException(
            messageSource.getMessage("voting.process.is.over", new Object[] {id}, Locale.getDefault())
        );
    }

    @Scheduled(fixedDelayString = "#{@scheduler.interval}")
    public void deadlineProcessing() {
        log.info("Processing...");
        var votingProcesses = votingProcessRepository.findAll();
        for (VotingProcess votingProcess : votingProcesses) {
            if (votingProcess.getDeadline().isBefore(OffsetDateTime.now())) {
                calculateResult(votingProcess);
                // Отправить сообщение на почту о завершении голосования всем участникам
            }
        }
        log.info("Completed");
    }

    private void calculateResult(VotingProcess votingProcess) {
        float countInFavour = 0;
        float countAgainst = 0;
        for (Vote vote : votingProcess.getVotes()) {
            if (vote.getStatus() == VoteStatus.IN_FAVOUR) {
                countInFavour++;
            }
            if (vote.getStatus() == VoteStatus.AGAINST) {
                countAgainst++;
            }
        }
        float result = countInFavour / (countInFavour + countAgainst);
        if (result < votingProcess.getThreshold()) {
            votingProcess.setStatus(VotingProcessStatus.DENIED);
        } else {
            votingProcess.setStatus(VotingProcessStatus.ACCEPTED);
        }
        votingProcessRepository.save(votingProcess);
    }
}
