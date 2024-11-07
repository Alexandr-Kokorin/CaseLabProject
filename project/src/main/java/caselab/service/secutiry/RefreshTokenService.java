package caselab.service.secutiry;

import caselab.configuration.security.JwtProperties;
import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.RefreshToken;
import caselab.domain.repository.ApplicationUserRepository;
import caselab.domain.repository.RefreshTokenRepository;
import caselab.exception.RefreshTokenExpirationException;
import caselab.exception.entity.not_found.TokenNotFoundException;
import caselab.exception.entity.not_found.UserNotFoundException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
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
                    .ofEpochMilli(System.currentTimeMillis() + jwtProperties.refresh().toMillis())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            )
            .token(UUID.randomUUID().toString())
            .build();
        return refreshTokenRepository.save(token).getToken();
    }

    public void delete(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken refreshToken) {
        if (refreshToken.getExpiresDate().isBefore(LocalDateTime.now())) {
            delete(refreshToken);
            throw new RefreshTokenExpirationException();
        }
        return refreshToken;
    }

}
