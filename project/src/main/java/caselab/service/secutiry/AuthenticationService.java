package caselab.service.secutiry;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.AuthenticationResponse;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import caselab.domain.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final ApplicationUserRepository appUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationResponse register(RegisterRequest request) {
        var user = ApplicationUser.builder()
            .email(request.email())
            .displayName(request.displayName())
            .globalPermissions(List.of(GlobalPermission.builder().name(GlobalPermissionName.USER).build()))
            .hashedPassword(encodePassword(request.password()))
            .build();
        appUserRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
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
        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
