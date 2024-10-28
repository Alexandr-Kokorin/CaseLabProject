package caselab.service.secutiry;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.GlobalPermissionRepository;
import caselab.exception.PermissionDeniedException;
import caselab.exception.entity.already_exists.UserAlreadyExistsException;
import caselab.exception.entity.not_found.UserNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService {

    private final GlobalPermissionRepository globalPermissionRepository;
    private final ApplicationUserRepository appUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationUserRepository userRepository;

    public AuthenticationResponse register(RegisterRequest request, Authentication authentication) {
        checkAdmin(authentication);

        checkEmail(request.email());

        var globalPermission = globalPermissionRepository.findByName(GlobalPermissionName.USER);
        var user = ApplicationUser.builder()
            .email(request.email())
            .displayName(request.displayName())
            .globalPermissions(List.of(globalPermission))
            .hashedPassword(encodePassword(request.password()))
            .build();
        appUserRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            ));

        var user = appUserRepository.findByEmail(request.email())
            .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private void checkEmail(String email) {
        var applicationUser = appUserRepository.findByEmail(email);
        if (applicationUser.isPresent()) {
            throw new UserAlreadyExistsException(email);
        }
    }
    public ApplicationUser findUserByAuthentication(Authentication authentication) {
        var userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));
    }

    public void checkAdmin(Authentication authentication){
        var applicationUser = findUserByAuthentication(authentication);
        if (applicationUser.getAuthorities().stream()
            .noneMatch(globalPermission -> globalPermission.getAuthority().equals(GlobalPermissionName.ADMIN.name()))) {
            throw new PermissionDeniedException();
        }
    }
}
