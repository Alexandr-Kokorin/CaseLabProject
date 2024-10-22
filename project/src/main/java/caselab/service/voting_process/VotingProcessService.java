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
import caselab.exception.entity.VoteNotFoundException;
import caselab.exception.entity.VotingProcessNotFoundException;
import caselab.service.users.ApplicationUserService;
import caselab.service.voting_process.mapper.VoteMapper;
import caselab.service.voting_process.mapper.VotingProcessMapper;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class VotingProcessService {

    private final ApplicationUserService applicationUserService;
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
        votingProcess.setDeadline(votingProcess.getCreatedAt().plus(votingProcessRequest.deadline()));
        votingProcess.setDocumentVersion(documentVersion);

        validateEmails(votingProcessRequest.emails());
        votingProcessRepository.save(votingProcess);
        votingProcess.setVotes(saveVotes(votingProcessRequest.emails(), votingProcess));

        return votingProcessMapper.entityToResponse(votingProcess);
    }

    public VotingProcessResponse getVotingProcessById(Long id) {
        return votingProcessMapper.entityToResponse(findVotingProcessById(id));
    }

    public void deleteVotingProcess(Long id) {
        if (!votingProcessRepository.existsById(id)) {
            throw new VotingProcessNotFoundException(id);
        }
        votingProcessRepository.deleteById(id);
    }

    public VotingProcessResponse updateVotingProcess(Long id, VotingProcessRequest votingProcessRequest) {
        var votingProcess = findVotingProcessById(id);
        var updateVotingProcess = votingProcessMapper.requestToEntity(votingProcessRequest);

        updateVotingProcess.setId(votingProcess.getId());
        updateVotingProcess.setStatus(votingProcess.getStatus());
        updateVotingProcess.setCreatedAt(votingProcess.getCreatedAt());
        updateVotingProcess.setDeadline(votingProcess.getCreatedAt().plus(votingProcessRequest.deadline()));
        updateVotingProcess.setDocumentVersion(votingProcess.getDocumentVersion());

        validateEmails(votingProcessRequest.emails());
        votingProcessRepository.save(updateVotingProcess);
        updateVotingProcess.setVotes(saveVotes(votingProcessRequest.emails(), updateVotingProcess));

        return votingProcessMapper.entityToResponse(updateVotingProcess);
    }

    public VoteResponse castVote(Authentication authentication, VoteRequest voteRequest) {
        var user = applicationUserService.findUserByAuthentication(authentication);

        var vote = voteRepository.findByApplicationUserIdAndVotingProcessId(
                user.getId(), voteRequest.votingProcessId())
            .orElseThrow(() -> new VoteNotFoundException(user.getEmail()));

        if (vote.getVotingProcess().getDeadline().isBefore(OffsetDateTime.now())) {
            throw new VotingProcessIsOverException(voteRequest.votingProcessId());
        }

        vote.setStatus(voteRequest.status());
        return voteMapper.entityToResponse(voteRepository.save(vote));
    }

    private VotingProcess findVotingProcessById(Long id) {
        return votingProcessRepository.findById(id)
            .orElseThrow(() -> new VotingProcessNotFoundException(id));
    }

    private void validateEmails(List<String> emails) {
        emails.forEach((email) -> applicationUserRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email)));
    }

    private List<Vote> saveVotes(List<String> emails, VotingProcess votingProcess) {
        List<Vote> votes = new ArrayList<>();
        emails.forEach((email) -> votes.add(createVoteById(email, votingProcess)));
        return voteRepository.saveAll(votes);
    }

    private Vote createVoteById(String email, VotingProcess votingProcess) {
        var user = applicationUserRepository.findByEmail(email).orElseThrow();

        return voteRepository.findByApplicationUserIdAndVotingProcessId(user.getId(), votingProcess.getId())
            .orElse(Vote.builder()
                .status(VoteStatus.NOT_VOTED)
                .applicationUser(user)
                .votingProcess(votingProcess)
                .build());
    }
}
