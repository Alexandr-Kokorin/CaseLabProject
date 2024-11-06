package caselab.service.secutiry;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.RefreshToken;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.RefreshTokenRepository;
import caselab.exception.entity.not_found.TokenNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import caselab.service.users.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${jwt.ttl-refresh}")
    private long jwtExpiration;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ApplicationUserService applicationUserService;
    private final ApplicationUserRepository applicationUserRepository;

    public RefreshToken getByToken(String token) {
        return refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new TokenNotFoundException(token));
    }

    public String create(String applicationUserEmail) {
        ApplicationUser applicationUser = applicationUserRepository.findByEmail(applicationUserEmail)
            .orElseThrow(() -> new UserNotFoundException(applicationUserEmail));
        RefreshToken token = RefreshToken
            .builder()
            .applicationUser(applicationUser)
            .expiresDate(
                Instant
                    .ofEpochMilli(System.currentTimeMillis() + jwtExpiration)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            )
            .token(UUID.randomUUID().toString())
            .build();
        return refreshTokenRepository.save(token).getToken();
    }

}
