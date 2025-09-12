package dev.senna.infra;

import dev.senna.model.entity.RefreshTokenEntity;
import dev.senna.model.entity.UserEntity;
import dev.senna.repository.UserRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class JwtService {

    private static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(15);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(3);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;
    @Inject
    UserRepository userRepository;

    public String generateAccessToken(UserEntity user) {
        var now = Instant.now();
        var exp = now.plus(ACCESS_TOKEN_VALIDITY);

        return Jwt.issuer(issuer)
                .subject(user.getUserId().toString())
                .upn(user.getUsername())
                .groups(Set.of(user.getRole().name()))
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiresAt(exp)
                .sign();
    }

    @Transactional
    public String generateAndStoreRefreshToken(UserEntity user) {
        var raw = UUID.randomUUID() + "." + UUID.randomUUID();
        var hash = BcryptUtil.bcryptHash(raw);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.tokenId = UUID.randomUUID();
        refreshToken.userId = user.getUserId();
        refreshToken.tokenHash = hash;
        refreshToken.expiresAt = Instant.now().plus(REFRESH_TOKEN_VALIDITY);
        refreshToken.revoked = false;
        refreshToken.persist();

        return raw;
    }

    @Transactional
    public Optional<UserEntity> rotateRefreshToken(String presented) {
        List<RefreshTokenEntity> tokens = RefreshTokenEntity.listAll();
        for (RefreshTokenEntity t : tokens) {
            if (!t.revoked && t.expiresAt.isAfter(Instant.now()) &&
                    BcryptUtil.matches(presented, t.tokenHash)) {

                t.revoked = true; // rotação
                t.persist();

                return userRepository.findByIdOptional(t.userId);
            }
        }
        return Optional.empty();
    }

    public long accessTokenTtlSeconds() {
        return ACCESS_TOKEN_VALIDITY.toSeconds();
    }
}
