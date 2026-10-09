package com.ldn.authservice.auth;

import com.ldn.authservice.auth.dto.requests.*;
import com.ldn.authservice.auth.dto.responses.AuthResponse;
import com.ldn.authservice.auth.exceptions.*;
import com.ldn.authservice.auth.dto.VerifyDto;
import com.ldn.authservice.auth.dto.responses.RegisterResponse;
import com.ldn.authservice.auth.enums.AccountStatus;
import com.ldn.authservice.auth.entities.Account;
import com.ldn.authservice.auth.repositories.AccountRepository;
import com.ldn.authservice.token.utils.TokenUtils;
import com.ldn.authservice.notification.LoginLogsService;
import com.ldn.authservice.notification.MailService;
import com.ldn.authservice.token.exceptions.InvalidTokenException;
import com.ldn.authservice.token.JwtService;
import com.ldn.common.redis.RedisService;
import com.ldn.common.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${frontend.url}")
    private String frontendURL;

    public Account findById(Long id) {
        return this.accountRepository.findById(id).orElseThrow(InvalidCredentialsException::new);
    }

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
        return this.jwtService.issueTokens(account.getId(), account.getEmail(), "");
    }

    private String createAuthSession(String email) {
        String rawKey = TokenUtils.randomToken(32);
        this.redisService.set(
                "verify:resolve:%s".formatted(rawKey),
                email,
                5, TimeUnit.MINUTES
        );
        return rawKey;
    }

    public String verifyProcessRequest(VerifyProcessRequest verifyProcessRequest) {
        // Prepare data
        Account account = this.accountRepository.findByEmailOrPhone(verifyProcessRequest.email()).orElseThrow(InvalidCredentialsException::new);
        String otp = TokenUtils.randomOtp();
        String rawKey = this.createAuthSession(account.getEmail());
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

    private String verifyResolve(String token) {
        String email = this.redisService.get("verify:resolve:%s".formatted(token), String.class);
        if (email == null) throw new InvalidTokenException();
        return email;
    }

    // Use Case: User request Resend Verification Email
    public void verifyEmailRequest(VerifyEmailRequest verifyEmailRequest) {
        String rawKey = verifyEmailRequest.key();
        String email = this.verifyResolve(rawKey);
        Account account = this.accountRepository.findByEmailOrPhone(email).orElseThrow(InvalidCredentialsException::new);
        if (account.getStatus().equals(AccountStatus.ACTIVE)) return;
        String hashKey = TokenUtils.hmacsha256(rawKey, account.getMfaSecret());
        VerifyDto verifyDto = this.redisService.get("verify:%s".formatted(hashKey), VerifyDto.class);
        this.mailService.sendOtpEmail(account.getEmail(), verifyDto.otp());
    }

    public AuthResponse verifyProceedRequest(VerifyProceedRequest verifyProceedRequest) {
        // User make verify proceed request via frontend, send with request body { key (rawKey signed above), otp, email }
        String rawKey = verifyProceedRequest.key();
        String email = this.verifyResolve(rawKey);
        Account account = this.accountRepository.findByEmailOrPhone(email).orElseThrow(InvalidCredentialsException::new);
        String otp = verifyProceedRequest.otp();

        String hashKey = TokenUtils.hmacsha256(rawKey, account.getMfaSecret());
        VerifyDto verifyDto = this.redisService.get("verify:%s".formatted(hashKey), VerifyDto.class);

        if (verifyDto == null) throw new InvalidTokenException();

        String status = "success";

        // Wrong OTP Input
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
                    .currentAttempt(verifyDto.currentAttempt() + 1)
                    .maxAttempt(verifyDto.maxAttempt())
                    .build()
            );
        }

        // Logging attempt
        this.loginLogsService.log(account, "mfa", status);
        if (status.equals("failed")) throw new InvalidTokenException();

        // All passed
        account.setStatus(AccountStatus.ACTIVE);
        this.accountRepository.save(account);

        // Consume tokens
        this.redisService.delete("verify:resolve:%s".formatted(rawKey));
        this.redisService.delete("verify:%s".formatted(hashKey));

        // Auto login
        return this.jwtService.issueTokens(account.getId(), account.getEmail(), "");
    }

    public AuthResponse refreshTokens(String rawRefreshToken) {
        Long accountId = this.jwtService.rotateTokens(rawRefreshToken);
        Account account = this.accountRepository.findById(accountId).orElseThrow(InvalidCredentialsException::new);
        return this.jwtService.issueTokens(account.getId(), account.getEmail(), "");
    }

    public void forgotPasswordRequest(ForgotPasswordRequest forgotPasswordRequest) {
        Account account = this.accountRepository.findByEmailOrPhone(forgotPasswordRequest.email()).orElseThrow(InvalidCredentialsException::new);
        String rawToken = this.createAuthSession(account.getEmail());
        String hashToken = TokenUtils.hmacsha256(rawToken, account.getMfaSecret());

        StringBuilder magicLink = new StringBuilder();
        magicLink.append(frontendURL).append("/auth/resetPW?token=%s".formatted(rawToken));

        this.redisService.set("verify:%s".formatted(hashToken), magicLink.toString(), 5, TimeUnit.MINUTES);
        this.mailService.sendOtpEmail(account.getEmail(), magicLink.toString());
    }

    @Transactional
    public AuthResponse resetPasswordRequest(ResetPasswordRequest resetPasswordRequest) {
        String email = this.verifyResolve(resetPasswordRequest.token());
        Account account = this.accountRepository.findByEmailOrPhone(email).orElseThrow(InvalidCredentialsException::new);
        String hashKey = TokenUtils.hmacsha256(resetPasswordRequest.token(), account.getMfaSecret());
        boolean verified = this.redisService.hasKey("verify:%s".formatted(hashKey));
        if (!verified) throw new InvalidTokenException();

        boolean isOldPassword = this.encoder.matches(resetPasswordRequest.password(),  account.getPassword());
        if (isOldPassword) throw new OldPasswordException();

        String hashPassword = this.encoder.encode(resetPasswordRequest.password());
        account.setPassword(hashPassword);
        this.accountRepository.save(account);

        this.jwtService.revokeTokens(account.getId());

        // Consume tokens
        this.redisService.delete("verify:resolve:%s".formatted(resetPasswordRequest.token()));
        this.redisService.delete("verify:%s".formatted(hashKey));

        return this.jwtService.issueTokens(account.getId(), account.getEmail(), "");
    }

}
