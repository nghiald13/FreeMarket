package com.ldn.authservice.services;

import com.ldn.authservice.dto.request.LoginRequest;
import com.ldn.authservice.dto.request.RegisterRequest;
import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.enums.AccountStatus;
import com.ldn.authservice.exception.AccountExistedException;
import com.ldn.authservice.exception.AccountSuspendedException;
import com.ldn.authservice.exception.InvalidCredentialsException;
import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.pojo.LoginLogs;
import com.ldn.authservice.repository.AccountRepository;
import com.ldn.authservice.repository.LoginLogsRepository;
import com.ldn.authservice.security.JwtService;
import com.ldn.common.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final LoginLogsService loginLogsService;
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

    private String getIndentifierUsed(String s) {
        String phoneRegExp = "^0[1-9]{9}$";
        String emailRegExp = "^\\S+@\\S+\\.\\S+$";
        if (s.matches(phoneRegExp)) return "phone";
        else if (s.matches(emailRegExp)) return "emailOrPhone";
        else return "unknown";
    }

    public AuthResponse login(LoginRequest loginRequest) {
        Account account = this.accountRepository.findByEmailOrPhone(loginRequest.emailOrPhone())
                .orElseThrow(InvalidCredentialsException::new);
        if (account.getStatus().equals(AccountStatus.SUSPENDED)) throw new AccountSuspendedException();

        // Compare password
        boolean passwordMatched = this.encoder.matches(loginRequest.password(),account.getPassword());

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
        //TODO Change this placeholder InvalidCredentialsException
        Account account = this.accountRepository.findById(accountId).orElseThrow(InvalidCredentialsException::new);
        return this.issueTokens(account);
    }

}
