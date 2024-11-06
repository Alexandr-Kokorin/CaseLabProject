package caselab.service.voting_process;

import caselab.controller.voting_process.payload.VoteRequest;
import caselab.controller.voting_process.payload.VoteResponse;
import caselab.controller.voting_process.payload.VotingProcessRequest;
import caselab.controller.voting_process.payload.VotingProcessResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Vote;
import caselab.domain.entity.VotingProcess;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.VoteStatus;
import caselab.domain.entity.enums.VotingProcessStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.VoteRepository;
import caselab.domain.repository.VotingProcessRepository;
import caselab.exception.VotingProcessIsOverException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.exception.entity.not_found.VoteNotFoundException;
import caselab.exception.entity.not_found.VotingProcessNotFoundException;
import caselab.exception.status.StatusIncorrectForCreateVotingProcessException;
import caselab.service.document.facade.DocumentFacadeService;
import caselab.service.notification.email.EmailNotificationDetails;
import caselab.service.notification.email.EmailService;
import caselab.service.util.DocumentUtilService;
import caselab.service.util.UserUtilService;
import caselab.service.voting_process.mapper.VoteMapper;
import caselab.service.voting_process.mapper.VotingProcessMapper;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class VotingProcessService {

    private final DocumentFacadeService documentFacadeService;
    private final UserUtilService userUtilService;
    private final DocumentUtilService documentUtilService;
    private final EmailService emailService;

    private final ApplicationUserRepository applicationUserRepository;
    private final DocumentRepository documentRepository;
    private final VotingProcessRepository votingProcessRepository;
    private final VoteRepository voteRepository;

    private final VotingProcessMapper votingProcessMapper;
    private final VoteMapper voteMapper;

    public VotingProcessResponse createVotingProcess(VotingProcessRequest request, Authentication authentication) {
        var documentVersion = documentRepository.findById(request.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(request.documentId()))
            .getDocumentVersions().getFirst();

        var user = userUtilService.findUserByAuthentication(authentication);
        documentUtilService.assertHasPermission(
            user, documentVersion.getDocument(), DocumentPermissionName::isCreator, "Creator");

        documentUtilService.assertHasDocumentStatus(
            documentVersion.getDocument(), List.of(DocumentStatus.DRAFT),
            new StatusIncorrectForCreateVotingProcessException());

        var votingProcess = votingProcessMapper.requestToEntity(request);
        votingProcess.setStatus(VotingProcessStatus.IN_PROGRESS);
        votingProcess.setCreatedAt(OffsetDateTime.now());
        votingProcess.setDeadline(votingProcess.getCreatedAt().plus(request.deadline()));
        votingProcess.setDocumentVersion(documentVersion);

        validateEmails(request.emails());
        documentVersion.getDocument().setStatus(DocumentStatus.VOTING_IN_PROGRESS);
        votingProcessRepository.save(votingProcess);
        votingProcess.setVotes(saveVotes(request.emails(), votingProcess, authentication, user));

        votingProcess.getVotes().forEach(this::sendMessage);
        return votingProcessMapper.entityToResponse(votingProcess);
    }

    public VotingProcessResponse getVotingProcessByDocumentId(Long documentId) {
        var documentVersion = documentRepository.findById(documentId)
            .orElseThrow(() -> new DocumentNotFoundException(documentId))
            .getDocumentVersions().getFirst();

        return votingProcessMapper.entityToResponse(votingProcessRepository.findByDocumentVersion(documentVersion)
            .orElseThrow(VotingProcessNotFoundException::new));
    }

    public VoteResponse castVote(Authentication authentication, VoteRequest voteRequest) {
        var user = userUtilService.findUserByAuthentication(authentication);
        var documentVersion = documentRepository.findById(voteRequest.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(voteRequest.documentId()))
            .getDocumentVersions().getFirst();

        var votingProcess = votingProcessRepository.findByDocumentVersion(documentVersion)
            .orElseThrow(VotingProcessNotFoundException::new);
        var vote = voteRepository.findByApplicationUserIdAndVotingProcessId(
                user.getId(), votingProcess.getId())
            .orElseThrow(VoteNotFoundException::new);

        if (votingProcess.getStatus() != VotingProcessStatus.IN_PROGRESS) {
            throw new VotingProcessIsOverException(votingProcess.getId());
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
            .orElseThrow(() -> new UserNotFoundException(email)));
    }

    private List<Vote> saveVotes(
        List<String> emails, VotingProcess votingProcess,
        Authentication authentication, ApplicationUser user
    ) {
        List<Vote> votes = new ArrayList<>();
        for (String email : emails) {
            if (!email.equals(user.getEmail())) {
                documentFacadeService.grantPermission(
                    votingProcess.getDocumentVersion().getDocument().getId(), email, authentication);
            }
        }
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
