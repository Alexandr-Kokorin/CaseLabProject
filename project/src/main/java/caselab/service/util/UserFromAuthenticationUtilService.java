package caselab.service.util;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFromAuthenticationUtilService {
    private final ApplicationUserRepository userRepository;

    public ApplicationUser findUserByAuthentication(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException(userDetails.getUsername()));
    }
}
