package caselab.controller.substitution;

import caselab.controller.substitution.payload.SubstitutionRequest;
import caselab.controller.substitution.payload.SubstitutionResponse;
import caselab.service.delegation.SubstitutionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(name = "JWT")
@RequestMapping("/api/v2/delegating")
@RequiredArgsConstructor
public class SubstitutionController {

    private final SubstitutionService substitutionService;

    @PostMapping("/assign")
    public SubstitutionResponse getAllDelegations(
        @Valid @RequestBody SubstitutionRequest substitutionRequest,
        Authentication authentication
    ) {
        return substitutionService.assignSubstitution(substitutionRequest, authentication);
    }
}
