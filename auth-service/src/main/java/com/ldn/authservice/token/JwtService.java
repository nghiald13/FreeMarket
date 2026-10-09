package com.ldn.authservice.token;

import com.ldn.authservice.auth.dto.responses.AuthResponse;
import com.ldn.authservice.token.dto.RefreshTokenDto;
import com.ldn.authservice.token.utils.TokenUtils;
import com.ldn.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final RedisService redisService;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    /**
     * Issues a short-lived RS256 access token carrying the user's id/email/roles.
     */
    private String generateAccessToken(Long accountId, String email, String roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("auth-service")
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES))
                .subject(accountId.toString())
                .claim("email", email)
                //TODO Uncomment this when implemented Account roles
//                .claim("roles", roles)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateRefreshToken(Long accountId) {
        String rawToken = TokenUtils.randomToken(64);
        String hashToken = TokenUtils.sha256(rawToken);
        String cacheKey = "refreshTokens:%s".formatted(hashToken);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(accountId);
        this.redisService.set(cacheKey, refreshTokenDto, 1, TimeUnit.DAYS);

        String accountTokens = "tokens:account:%s".formatted(accountId);
        this.redisService.sSet(accountTokens, hashToken, 1, TimeUnit.DAYS);

        return rawToken;
    }

    public Long rotateTokens(String rawRefreshToken) {
        String hashToken = TokenUtils.sha256(rawRefreshToken);
        String cacheKey = "refreshToken:%s".formatted(hashToken);
        RefreshTokenDto refreshTokenDto = this.redisService.getAndDelete(cacheKey, RefreshTokenDto.class);

        String accountTokens = "token:account:%s".formatted(refreshTokenDto.accountId());
        this.redisService.sDelete(accountTokens, hashToken);

        return refreshTokenDto.accountId();
    }

    private long accessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60;
    }

    public AuthResponse issueTokens(Long accountId, String email, String roles) {
        String accessToken = this.generateAccessToken(accountId, email, roles);
        String refreshToken = this.generateRefreshToken(accountId);
        return new AuthResponse(accessToken, refreshToken, this.accessTokenExpirationSeconds());
    }

    public void revokeTokens(Long accountId) {
        String accountTokens = "tokens:account:%s".formatted(accountId);
        Set<String> tokens = this.redisService.sMember(accountTokens, String.class);
        tokens.forEach(token -> {
            this.redisService.delete("refreshTokens:%s".formatted(token));
        });
        this.redisService.delete(accountTokens);
    }
}
