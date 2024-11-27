package caselab.service.delegation;

import caselab.controller.substitution.payload.SubstitutionRequest;
import caselab.controller.substitution.payload.SubstitutionResponse;
import caselab.domain.entity.Substitution;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.SubstitutionRepository;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.util.UserUtilService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubstitutionService {

    private final SubstitutionRepository substitutionRepository;
    private final UserUtilService userUtilService;
    private final ApplicationUserRepository applicationUserRepository;

    public SubstitutionResponse assignSubstitution(
        SubstitutionRequest substitutionRequest,
        Authentication authentication) {
        var currentUser = userUtilService.findUserByAuthentication(authentication);
        var substitutionUser = applicationUserRepository.findByEmail(substitutionRequest.substitutionUserEmail())
            .orElseThrow(() -> new UserNotFoundException(substitutionRequest.substitutionUserEmail()));

        var substitution = new Substitution();
        substitution.setAssigned(substitutionRequest.assigned());
        substitution.setSubstitutionUserId(substitutionUser.getId());
        var savedSubstitution = substitutionRepository.save(substitution);

        var currentUserEntity = applicationUserRepository.findByEmail(currentUser.getEmail())
            .orElseThrow(() -> new UserNotFoundException(currentUser.getEmail()));;
        currentUserEntity.setSubstitution(savedSubstitution);
        applicationUserRepository.save(currentUserEntity);

        return SubstitutionResponse
            .builder()
            .id(savedSubstitution.getId())
            .assigned(savedSubstitution.getAssigned())
            .currentUserEmail(currentUser.getEmail())
            .substitutionUserEmail(substitutionUser.getEmail())
            .build();
    }
}
