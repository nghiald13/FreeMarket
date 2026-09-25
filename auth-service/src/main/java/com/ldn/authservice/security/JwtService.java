package com.ldn.authservice.security;

import com.ldn.authservice.dto.RefreshTokenDto;
import com.ldn.authservice.pojo.Account;
import com.ldn.common.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final SecureRandom secureRandom = new SecureRandom();
    private final JwtEncoder jwtEncoder;
    private final RedisService redisService;

    @Value("${jwt.access-token-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    private String randomToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Issues a short-lived RS256 access token carrying the user's id/email/roles. */
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
        String rawToken = this.randomToken();
        String cacheKey = String.format("refreshToken:account:%s", account.getId().toString());
        Map<String, Object> mapValue = Map.of(
                "accountId", account.getId(),
                "token", this.hash(rawToken),
                "isRevoked", false
        );
        RefreshTokenDto cacheValue = RefreshTokenDto.fromMap(mapValue);
        this.redisService.set(cacheKey, cacheValue, 1, TimeUnit.DAYS);
        return rawToken;
    }

    public long accessTokenExpirationSeconds() {
        return accessTokenExpirationMinutes * 60;
    }
}
