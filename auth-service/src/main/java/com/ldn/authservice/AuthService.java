package com.ldn.authservice;

import com.ldn.authservice.dto.RefreshTokenDto;
import com.ldn.authservice.dto.request.LoginRequest;
import com.ldn.authservice.dto.request.RegisterRequest;
import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.exception.AccountExistedException;
import com.ldn.authservice.exception.InvalidCredentialsException;
import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.repository.AccountRepository;
import com.ldn.authservice.security.JwtService;
import com.ldn.common.redis.RedisService;
import com.ldn.common.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private final JwtService jwtService;

    public RegisterResponse createAccount (RegisterRequest userInfo) {
        // Check uniqueness of email and phone
        boolean existed = this.accountRepository.existsByEmailOrPhone((userInfo.email()), userInfo.phone());
        if (existed) throw new AccountExistedException();

        // Hash password & generate mfa_secret
        String password_hash = this.encoder.encode(userInfo.password());
        String mfaSecret = Utils.generateBase32Secret();

        // Business
        Account account = Account.builder()
                .name(userInfo.name())
                .email(userInfo.email())
                .password(password_hash)
                .phone(userInfo.phone())
                .mfaSecret(mfaSecret)
                .build();
        accountRepository.save(account);

        return RegisterResponse.fromEntity(account);
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Account account = this.accountRepository.findByEmail(loginRequest.email())
                .orElseThrow(InvalidCredentialsException::new);
        boolean passwordMatched = this.encoder.matches(loginRequest.password(),account.getPassword());
        if (!passwordMatched) throw new InvalidCredentialsException();

        return this.issueTokens(account);
    }

    public AuthResponse issueTokens(Account account) {
        String accessToken = this.jwtService.generateAccessToken(account);
        String refreshToken = this.jwtService.generateRefreshToken(account);
        return new AuthResponse(accessToken, refreshToken, jwtService.accessTokenExpirationSeconds());
    }

}
