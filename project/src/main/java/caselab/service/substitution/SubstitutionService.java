package caselab.service.substitution;

import caselab.controller.substitution.payload.DelegationRequest;
import caselab.controller.substitution.payload.SubstitutionRequest;
import caselab.controller.substitution.payload.SubstitutionResponse;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.Document;
import caselab.domain.entity.Substitution;
import caselab.domain.entity.UserToDocument;
import caselab.domain.entity.enums.DocumentPermissionName;
import caselab.domain.entity.enums.DocumentStatus;
import caselab.domain.entity.enums.SignatureStatus;
import caselab.domain.entity.enums.VoteStatus;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.DocumentPermissionRepository;
import caselab.domain.repository.DocumentRepository;
import caselab.domain.repository.SignatureRepository;
import caselab.domain.repository.SubstitutionRepository;
import caselab.domain.repository.UserToDocumentRepository;
import caselab.domain.repository.VoteRepository;
import caselab.exception.InvalidDocumentForDelegationException;
import caselab.exception.OnlyYourDepartmentException;
import caselab.exception.UserAlreadySubstituted;
import caselab.exception.entity.already_exists.SignatureAlreadyExistsException;
import caselab.exception.entity.already_exists.VoteAlreadyExistsException;
import caselab.exception.entity.not_found.ApplicationUserNotFoundException;
import caselab.exception.entity.not_found.DocumentNotFoundException;
import caselab.exception.entity.not_found.SignatureNotFoundException;
import caselab.exception.entity.not_found.SubstitutionNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.exception.entity.not_found.VoteNotFoundException;
import caselab.exception.status.StatusIncorrectForDelegationException;
import caselab.service.util.DocumentUtilService;
import caselab.service.util.UserUtilService;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class SubstitutionService {

    private final SubstitutionRepository substitutionRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final DocumentRepository documentRepository;
    private final VoteRepository voteRepository;
    private final DocumentPermissionRepository documentPermissionRepository;
    private final UserToDocumentRepository userToDocumentRepository;
    private final SignatureRepository signatureRepository;

    private final UserUtilService userUtilService;
    private final DocumentUtilService documentUtilService;

    public SubstitutionResponse assignSubstitution(
        SubstitutionRequest substitutionRequest,
        Authentication authentication
    ) {
        var currentUser = userUtilService.findUserByAuthentication(authentication);
        var substitutionUser = applicationUserRepository.findByEmail(substitutionRequest.substitutionUserEmail())
            .orElseThrow(() -> new UserNotFoundException(substitutionRequest.substitutionUserEmail()));

        checkDepartment(currentUser, substitutionUser);

        if (substitutionUser.getSubstitution() != null) {
            throw new UserAlreadySubstituted(substitutionRequest.substitutionUserEmail());
        }

        var substitution = new Substitution();
        substitution.setAssigned(substitutionRequest.assigned());
        substitution.setSubstitutionUserId(substitutionUser.getId());
        var savedSubstitution = substitutionRepository.save(substitution);

        currentUser.setSubstitution(savedSubstitution);
        applicationUserRepository.save(currentUser);

        return SubstitutionResponse
            .builder()
            .id(savedSubstitution.getId())
            .assigned(savedSubstitution.getAssigned())
            .currentUserEmail(currentUser.getEmail())
            .substitutionUserEmail(substitutionUser.getEmail())
            .build();
    }

    public SubstitutionRequest getSubstitution(Long id) {
        var substitution = substitutionRepository.findById(id)
            .orElseThrow(() -> new SubstitutionNotFoundException(id));
        var substitutionUser = applicationUserRepository.findById(substitution.getSubstitutionUserId())
            .orElseThrow(() -> new ApplicationUserNotFoundException(substitution.getSubstitutionUserId()));
        return SubstitutionRequest.builder()
            .substitutionUserEmail(substitutionUser.getEmail())
            .assigned(substitution.getAssigned())
            .build();
    }

    public void delegate(DelegationRequest delegationRequest, Authentication authentication) {
        var currentUser = userUtilService.findUserByAuthentication(authentication);
        var delegateUser = applicationUserRepository.findByEmail(delegationRequest.delegateEmail())
            .orElseThrow(() -> new UserNotFoundException(delegationRequest.delegateEmail()));

        checkDepartment(currentUser, delegateUser);

        var document = documentRepository.findById(delegationRequest.documentId())
            .orElseThrow(() -> new DocumentNotFoundException(delegationRequest.documentId()));

        if (!documentUtilService.checkLacksPermission(currentUser, document, DocumentPermissionName::isCreator)) {
            throw new InvalidDocumentForDelegationException();
        }
        documentUtilService.assertHasDocumentStatus(
            document, List.of(DocumentStatus.VOTING_IN_PROGRESS, DocumentStatus.SIGNATURE_IN_PROGRESS),
            new StatusIncorrectForDelegationException()
        );

        if (document.getStatus() == DocumentStatus.VOTING_IN_PROGRESS) {
            delegateVote(currentUser, delegateUser, document);
        } else {
            delegateSignature(currentUser, delegateUser, document);
        }
    }

    private void delegateVote(ApplicationUser currentUser, ApplicationUser delegateUser, Document document) {
        var voting = document.getDocumentVersions().getFirst().getVotingProcesses().getFirst();
        if (voteRepository.existsByApplicationUserAndVotingProcess(delegateUser, voting)) {
            throw new VoteAlreadyExistsException();
        }
        var vote = voteRepository.findByApplicationUserIdAndVotingProcessId(currentUser.getId(), voting.getId())
            .orElseThrow(VoteNotFoundException::new);

        grandPermission(delegateUser, document);

        vote.setStatus(VoteStatus.NOT_VOTED);
        vote.setApplicationUser(delegateUser);

        voteRepository.save(vote);
    }

    private void delegateSignature(ApplicationUser currentUser, ApplicationUser delegateUser, Document document) {
        var version = document.getDocumentVersions().getFirst();
        if (signatureRepository.existsByApplicationUserAndDocumentVersion(delegateUser, version)) {
            throw new SignatureAlreadyExistsException();
        }
        var signature = signatureRepository.findByApplicationUserAndDocumentVersion(currentUser, version)
            .orElseThrow(SignatureNotFoundException::new);

        grandPermission(delegateUser, document);

        signature.setApplicationUser(delegateUser);
        signature.setStatus(SignatureStatus.NOT_SIGNED);
        signature.setSentAt(OffsetDateTime.now());

        signatureRepository.save(signature);
    }

    private void grandPermission(ApplicationUser delegateUser, Document document) {
        var permission = documentPermissionRepository.findDocumentPermissionByName(DocumentPermissionName.READ);
        var userToDocument = new UserToDocument();
        userToDocument.setApplicationUser(delegateUser);
        userToDocument.setDocument(document);
        userToDocument.setDocumentPermissions(List.of(permission));
        userToDocumentRepository.save(userToDocument);
    }

    private void checkDepartment(ApplicationUser currentUser, ApplicationUser delegateUser) {
        if (!currentUser.getIsWorking() || !delegateUser.getIsWorking()
            || !Objects.equals(currentUser.getDepartment().getId(), delegateUser.getDepartment().getId())) {
            throw new OnlyYourDepartmentException();
        }
    }
}
