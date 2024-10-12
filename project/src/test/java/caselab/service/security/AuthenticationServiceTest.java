package caselab.service.security;

import caselab.controller.secutiry.payload.AuthenticationRequest;
import caselab.controller.secutiry.payload.RegisterRequest;
import caselab.domain.IntegrationTest;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.service.secutiry.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class AuthenticationServiceTest extends IntegrationTest {

    @Autowired
    private AuthenticationService authenticationService;
    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Test
    @Transactional
    @Rollback
    public void registerUser() {
        var request = new RegisterRequest("test@mail.ru", "displayName", "password");

        var response = authenticationService.register(request);
        var user = applicationUserRepository.findByEmail(request.email()).orElseThrow();

        assertAll(
            "Grouped assertions for register user",
            () -> assertThat(response.token()).isNotNull(),
            () -> assertEquals(user.getDisplayName(), request.displayName())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void authenticateExistedUser() {
        var registerRequest = new RegisterRequest("test@mail.ru", "displayName", "password");
        var authenticationRequest = AuthenticationRequest.builder().email("test@mail.ru").password("password").build();

        authenticationService.register(registerRequest);
        var authenticationResponse = authenticationService.authenticate(authenticationRequest);

        assertThat(authenticationResponse.token()).isNotNull();
    }

    @Test
    @Transactional
    @Rollback
    public void authenticateNotExistedUser() {
        var authenticationRequest = AuthenticationRequest.builder().email("test@mail.ru").password("password").build();

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(authenticationRequest));
    }
}
