package caselab.service.util;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.exception.PermissionDeniedException;
import caselab.exception.entity.not_found.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserUtilService {

    private final ApplicationUserRepository userRepository;

    public ApplicationUser findUserByAuthentication(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));
    }

    public void checkUserGlobalPermission(ApplicationUser user, GlobalPermissionName permission) {
        user.getGlobalPermissions()
            .stream()
            .map(GlobalPermission::getName)
            .filter(it -> it.equals(permission))
            .findFirst()
            .orElseThrow(PermissionDeniedException::new);
    }
}
