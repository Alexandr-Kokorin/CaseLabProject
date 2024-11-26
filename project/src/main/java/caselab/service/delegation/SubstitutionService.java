package caselab.service.delegation;

import caselab.controller.delegating.payload.SubstitutionRequest;
import caselab.controller.delegating.payload.SubstitutionResponse;
import caselab.domain.entity.Substitution;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.SubstitutionRepository;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.util.UserUtilService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubstitutionService {

    private final SubstitutionRepository delegatingRepository;
    private final UserUtilService userUtilService;
    private final ApplicationUserRepository applicationUserRepository;

    public SubstitutionResponse assignSubstitution(
        SubstitutionRequest substitutionRequest,
        Authentication authentication) {
        var currentUser = userUtilService.findUserByAuthentication(authentication);
        var substitutionUser = applicationUserRepository.findByEmail(substitutionRequest.delegatingUserEmail())
            .orElseThrow(() -> new UserNotFoundException(substitutionRequest.delegatingUserEmail()));

        var delegating = new Substitution();
        delegating.setCurrentUser(currentUser);
        delegating.setAssigned(OffsetDateTime.now());
        delegating.setSubstitutionUser(substitutionUser);

        var savedDelegating = delegatingRepository.save(delegating);
        return SubstitutionResponse
            .builder()
            .id(savedDelegating.getId())
            .assigned(savedDelegating.getAssigned())
            .currentUserEmail(savedDelegating.getCurrentUser().getEmail())
            .substitutionUserEmail(savedDelegating.getSubstitutionUser().getEmail())
            .build();
    }
}
