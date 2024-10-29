package caselab.service.voting_process;

import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.Vote;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.VoteStatus;
import caselab.domain.entity.enums.VotingProcessStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentVersionRepository;
import caselab.domain.repository.VoteRepository;
import caselab.domain.repository.VotingProcessRepository;
import caselab.exception.VotingProcessIsOverException;
import caselab.exception.entity.not_found.VoteNotFoundException;
import caselab.exception.entity.not_found.VotingProcessNotFoundException;
import caselab.exception.entity.not_found.DocumentVersionNotFoundException;
import caselab.exception.entity.status.StatusShouldBeDraftException;
import caselab.service.document.facade.DocumentFacadeService;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
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

    private final DocumentFacadeService documentFacadeService;
    private final ApplicationUserService applicationUserService;

    private final ApplicationUserRepository applicationUserRepository;
    private final DocumentVersionRepository documentVersionRepository;
    private final VotingProcessRepository votingProcessRepository;
    private final VoteRepository voteRepository;

    private final VotingProcessMapper votingProcessMapper;
    private final VoteMapper voteMapper;

    private final EmailService emailService;

    public VotingProcessResponse createVotingProcess(VotingProcessRequest request, Authentication authentication) {
        var documentVersion = documentVersionRepository.findById(request.documentVersionId())
            .orElseThrow(() -> new DocumentVersionNotFoundException(request.documentVersionId()));

        if (documentVersion.getDocument().getStatus() != DocumentStatus.DRAFT) {
            throw new StatusShouldBeDraftException();
        }

        var votingProcess = votingProcessMapper.requestToEntity(request);
        votingProcess.setStatus(VotingProcessStatus.IN_PROGRESS);
        votingProcess.setCreatedAt(OffsetDateTime.now());
        votingProcess.setDeadline(votingProcess.getCreatedAt().plus(request.deadline()));
        votingProcess.setDocumentVersion(documentVersion);

        validateEmails(request.emails());
        documentVersion.getDocument().setStatus(DocumentStatus.VOTING_IN_PROGRESS);
        votingProcessRepository.save(votingProcess);
        votingProcess.setVotes(saveVotes(request.emails(), votingProcess, authentication));

        votingProcess.getVotes().forEach(this::sendMessage);
        return votingProcessMapper.entityToResponse(votingProcess);
    }

    public VotingProcessResponse getVotingProcessById(Long id) {
        return votingProcessMapper.entityToResponse(votingProcessRepository.findById(id)
            .orElseThrow(() -> new VotingProcessNotFoundException(id)));
    }

    public VoteResponse castVote(Authentication authentication, VoteRequest voteRequest) {
        var user = applicationUserService.findUserByAuthentication(authentication);

        var vote = voteRepository.findByApplicationUserIdAndVotingProcessId(
                user.getId(), voteRequest.votingProcessId())
            .orElseThrow(() -> new VoteNotFoundException(user.getEmail()));

        if (vote.getVotingProcess().getStatus() != VotingProcessStatus.IN_PROGRESS) {
            throw new VotingProcessIsOverException(voteRequest.votingProcessId());
        }

        vote.setStatus(voteRequest.status());
        return voteMapper.entityToResponse(voteRepository.save(vote));
    }

    private void sendMessage(Vote vote) {
        var emailDetails = EmailNotificationDetails.builder()
            .sender("admin@solifex.ru")
            .recipient(vote.getApplicationUser().getEmail())
            .subject("Уведомление о начале голосования")
            .text("Началось голосование \"" + vote.getVotingProcess().getName() + "\" по документу \""
                + vote.getVotingProcess().getDocumentVersion().getName() + "\".")
            .attachments(List.of())
            .build();

        emailService.sendNotification(emailDetails);
    }

    private void validateEmails(List<String> emails) {
        emails.forEach(email -> applicationUserRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email)));
    }

    private List<Vote> saveVotes(List<String> emails, VotingProcess votingProcess, Authentication authentication) {
        List<Vote> votes = new ArrayList<>();
        emails.forEach(email -> documentFacadeService.grantPermission(
            votingProcess.getDocumentVersion().getDocument().getId(), email, authentication));
        emails.forEach(email -> votes.add(createVoteById(email, votingProcess)));
        return voteRepository.saveAll(votes);
    }

    private Vote createVoteById(String email, VotingProcess votingProcess) {
        var user = applicationUserRepository.findByEmail(email).orElseThrow();

        return Vote.builder()
                .status(VoteStatus.NOT_VOTED)
                .applicationUser(user)
                .votingProcess(votingProcess)
                .build();
    }
}
