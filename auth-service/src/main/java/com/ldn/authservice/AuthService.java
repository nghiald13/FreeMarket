package com.ldn.authservice;

import com.ldn.authservice.dto.request.LoginRequest;
import com.ldn.authservice.dto.request.RegisterRequest;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.exception.AccountExistedException;
import com.ldn.authservice.exception.InvalidCredentialsException;
import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.repository.AccountRepository;
import com.ldn.common.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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

    public String login(LoginRequest loginRequest) {
        Account account = this.accountRepository.findByEmail(loginRequest.email())
                .orElseThrow(InvalidCredentialsException::new);
        boolean passwordMatched = this.encoder.matches(loginRequest.password(),account.getPassword());
        if (!passwordMatched) throw new InvalidCredentialsException();
        //TODO Handle login success response

        return "";
    }

    //TODO Create Sign Payload
}
