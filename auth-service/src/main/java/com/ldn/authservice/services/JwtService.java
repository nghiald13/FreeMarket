package com.ldn.authservice.services;

import com.ldn.authservice.dto.RefreshTokenDto;
import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.utils.TokenUtils;
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
    private String generateAccessToken(Account account) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("auth-service")
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpirationMinutes, ChronoUnit.MINUTES))
                .subject(account.getId().toString())
                .claim("email", account.getEmail())
                //TODO Uncomment this when implemented Account roles
//                .claim("roles", account.getRoles())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateRefreshToken(Account account) {
        String rawToken = TokenUtils.randomToken(64);
        String hashToken = TokenUtils.sha256(rawToken);
        String cacheKey = "refreshTokens:%s".formatted(hashToken);
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(account.getId());
        this.redisService.set(cacheKey, refreshTokenDto, 1, TimeUnit.DAYS);

        String accountTokens = "tokens:account:%s".formatted(account.getId());
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

    public AuthResponse issueTokens(Account account) {
        String accessToken = this.generateAccessToken(account);
        String refreshToken = this.generateRefreshToken(account);
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
