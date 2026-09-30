package com.ldn.authservice.security;

import com.ldn.authservice.dto.RefreshTokenDto;
import com.ldn.authservice.exception.InvalidCredentialsException;
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

import java.util.Map;
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
    public String generateAccessToken(Account account) {
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

    public String generateRefreshToken(Account account) {
        String rawToken = TokenUtils.randomToken(64);
        String cacheKey = String.format("refreshToken:%s", TokenUtils.sha256(rawToken));
        Map<String, Object> mapValue = Map.of(
                "accountId", account.getId(),
                "isRevoked", false
        );
        RefreshTokenDto cacheValue = RefreshTokenDto.fromMap(mapValue);
        this.redisService.set(cacheKey, cacheValue, 1, TimeUnit.DAYS);
        return rawToken;
    }

    public Long rotateTokens(String rawRefreshToken) {
        String cacheKey = String.format("refreshToken:%s", TokenUtils.sha256(rawRefreshToken));
        RefreshTokenDto refreshTokenDto = this.redisService.get(cacheKey, RefreshTokenDto.class);
        if (refreshTokenDto == null || refreshTokenDto.isRevoked())
            //TODO Change this placeholder InvalidCredentialsException
            throw new InvalidCredentialsException();

        this.redisService.delete(cacheKey);
        return refreshTokenDto.accountId();

    }

    public long accessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60;
    }
}
