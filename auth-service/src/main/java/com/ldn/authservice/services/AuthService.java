package com.ldn.authservice.services;

import com.ldn.authservice.dto.VerifyDto;
import com.ldn.authservice.dto.request.*;
import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.enums.AccountStatus;
import com.ldn.authservice.exception.*;
import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.repository.AccountRepository;
import com.ldn.authservice.security.JwtService;
import com.ldn.authservice.utils.TokenUtils;
import com.ldn.common.redis.RedisService;
import com.ldn.common.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final LoginLogsService loginLogsService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final RedisService redisService;
    private final MailService mailService;

    private final JwtService jwtService;

    public RegisterResponse createAccount(RegisterRequest userInfo) {
        // Check uniqueness of email and phone
        boolean existed = this.accountRepository.existsByEmailOrPhone((userInfo.email()), userInfo.phone());
        if (existed) throw new AccountExistedException();

        // Hash password & generate mfa_secret
        String password_hash = this.encoder.encode(userInfo.password());
        String mfaSecret = Utils.generateBase32Secret();

        // Business
        Account account = Account.builder().name(userInfo.name()).email(userInfo.email()).password(password_hash).phone(userInfo.phone()).mfaSecret(mfaSecret).build();
        accountRepository.save(account);

        return RegisterResponse.fromEntity(account);
    }

    private String getIndentifierUsed(String s) {
        String phoneRegExp = "^0[1-9]{9}$";
        String emailRegExp = "^\\S+@\\S+\\.\\S+$";
        if (s.matches(phoneRegExp)) return "phone";
        else if (s.matches(emailRegExp)) return "email";
        else return "unknown";
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Account account = this.accountRepository.findByEmailOrPhone(loginRequest.emailOrPhone()).orElseThrow(InvalidCredentialsException::new);

        // Compare password
        boolean passwordMatched = this.encoder.matches(loginRequest.password(), account.getPassword());

        // Logging current attempt
        String loginStatus = passwordMatched ? "success" : "failed";
        this.loginLogsService.log(account, this.getIndentifierUsed(loginRequest.emailOrPhone()), loginStatus);

        // Checking logs to detect suspect
        int failedAttempts = this.loginLogsService.checkLatest5Attempts(account);
        if (failedAttempts >= 5) {
            // Flag account as suspected
            account.setStatus(AccountStatus.SUSPENDED);
            this.accountRepository.save(account);

            //TODO Write scheduled job to release suspicion under specific circumstances (e.g after x minute(s), or verify processes...)
        }

        // Throw error for wrong credentials input
        if (!passwordMatched) throw new InvalidCredentialsException();

        // Checking account status
        if (account.getStatus().equals(AccountStatus.INACTIVE)) throw new AccountInactivedException();
        if (account.getStatus().equals(AccountStatus.SUSPENDED)) throw new AccountSuspendedException();

        // Sign JWT tokens pair on success
        return this.issueTokens(account);
    }

    public AuthResponse issueTokens(Account account) {
        String accessToken = this.jwtService.generateAccessToken(account);
        String refreshToken = this.jwtService.generateRefreshToken(account);
        return new AuthResponse(accessToken, refreshToken, jwtService.accessTokenExpirationSeconds());
    }

    public AuthResponse refreshTokens(String rawRefreshTokens) {
        Long accountId = this.jwtService.rotateTokens(rawRefreshTokens);
        Account account = this.accountRepository.findById(accountId).orElseThrow(InvalidCredentialsException::new);
        return this.issueTokens(account);
    }

    public String verifyProcessRequest(VerifyProcessRequest verifyProcessRequest) {
        // Prepare data
        Account account = this.accountRepository.findByEmailOrPhone(verifyProcessRequest.email()).orElseThrow(InvalidCredentialsException::new);
        String otp = TokenUtils.randomOtp();
        String rawKey = TokenUtils.randomToken(32);
        String hashKey = TokenUtils.hmacsha256(rawKey, account.getMfaSecret());

        this.redisService.set(
                String.format("verify:%s", hashKey),
                new VerifyDto(otp),
                5, TimeUnit.MINUTES
        );

        this.mailService.sendOtpEmail(account.getEmail(), otp);

        // return rawkey to frontend making Url with params
        return rawKey;
    }

    // Uss Case: User request Resend Verification Email
    public void verifyEmailRequest(VerifyEmailRequest verifyEmailRequest) {
        String email = verifyEmailRequest.email();
        String rawKey = verifyEmailRequest.key();
        Account account = this.accountRepository.findByEmailOrPhone(email).orElseThrow(InvalidCredentialsException::new);
        if (account.getStatus().equals(AccountStatus.ACTIVE)) return;
        String hashKey = TokenUtils.hmacsha256(rawKey, account.getMfaSecret());
        VerifyDto verifyDto = this.redisService.get("verify:%s".formatted(hashKey), VerifyDto.class);
        this.mailService.sendOtpEmail(account.getEmail(), verifyDto.otp());
    }

    public AuthResponse verifyProceedRequest(VerifyProceedRequest verifyProceedRequest) {
        // User make verify proceed request via frontend, send with request body { key (rawKey signed above), otp, email }
        Account account = this.accountRepository.findByEmailOrPhone(verifyProceedRequest.email()).orElseThrow(InvalidCredentialsException::new);
        String rawKey = verifyProceedRequest.key();
        String otp = verifyProceedRequest.otp();

        String hashKey = TokenUtils.hmacsha256(rawKey, account.getMfaSecret());
        VerifyDto verifyDto = this.redisService.get("verify:%s".formatted(hashKey), VerifyDto.class);

        if (verifyDto == null) throw new InvalidTokenException();

        String status = "success";
        if (!verifyDto.otp().equals(otp)) {
            // Flag attempt as failed
            status = "failed";

            // Update attempt
            int updatedAttempt = verifyDto.currentAttempt() + 1;

            // If exceeds max attempts -> revoke OTP and throw exception
            if (updatedAttempt == 5) {
                this.redisService.delete("verify:%s".formatted(hashKey));
                throw new AccountMfaException();
            }

            // Else update redis
            this.redisService.setIfPresentKeepTTL("verify:%s".formatted(hashKey), VerifyDto.builder()
                    .otp(verifyDto.otp())
                    .currentAttempt(verifyDto.currentAttempt()+1)
                    .maxAttempt(verifyDto.maxAttempt())
                    .build()
            );
        }

        // Logging attempt
        this.loginLogsService.log(account, "mfa", status);
        if (status.equals("failed")) throw new InvalidCredentialsException();

        // All passed
        account.setStatus(AccountStatus.ACTIVE);
        this.accountRepository.save(account);

        // Auto login
        return this.issueTokens(account);
    }


}
