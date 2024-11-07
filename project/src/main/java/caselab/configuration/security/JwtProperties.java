package caselab.configuration.security;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties("jwt")
public record JwtProperties(
    String secret,
    @DurationUnit(ChronoUnit.MINUTES)
    Duration ttl,
    @DurationUnit(ChronoUnit.MINUTES)
    Duration refresh
) {

    public SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
